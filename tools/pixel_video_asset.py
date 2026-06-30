#!/usr/bin/env python
"""
Convert green-screen game animation videos into crisp pixel-art PNG assets.

The main output is a transparent horizontal spritesheet. The tool also writes
an individual PNG sequence, an enlarged nearest-neighbor preview, and a JSON
manifest with the parameters needed by game code or video editors.
"""

from __future__ import annotations

import argparse
import json
import math
from dataclasses import asdict, dataclass
from pathlib import Path
from typing import Iterable

import cv2
import numpy as np
from PIL import Image


@dataclass(frozen=True)
class ProcessingOptions:
    target_frame_height: int = 96
    output_fps: int = 12
    palette_colors: int = 48
    crop_padding: int = 8
    forward_frame_count: int = 0
    hue_min: int = 45
    hue_max: int = 85
    saturation_min: int = 60
    value_min: int = 80
    keep_largest_component: bool = True
    clean_kernel_size: int = 3


@dataclass(frozen=True)
class ProcessResult:
    frames: list[Image.Image]
    manifest: dict


def read_video_frames(video_path: Path) -> tuple[list[np.ndarray], float]:
    cap = cv2.VideoCapture(str(video_path))
    if not cap.isOpened():
        raise ValueError(f"Could not open video: {video_path}")

    source_fps = float(cap.get(cv2.CAP_PROP_FPS) or 0)
    if source_fps <= 0:
        source_fps = 24.0

    frames: list[np.ndarray] = []
    while True:
        ok, bgr = cap.read()
        if not ok:
            break
        frames.append(cv2.cvtColor(bgr, cv2.COLOR_BGR2RGB))
    cap.release()

    if not frames:
        raise ValueError(f"Video has no readable frames: {video_path}")
    return frames, source_fps


def select_frames(
    frames: list[np.ndarray],
    source_fps: float,
    output_fps: int,
    forward_frame_count: int = 0,
) -> list[np.ndarray]:
    return [frames[index] for index in select_frame_indices(len(frames), source_fps, output_fps, forward_frame_count)]


def select_frame_indices(
    frame_count: int,
    source_fps: float,
    output_fps: int,
    forward_frame_count: int = 0,
) -> list[int]:
    if frame_count <= 0:
        return []
    if output_fps <= 0:
        raise ValueError("--fps must be greater than 0")
    if forward_frame_count > 0:
        count = min(frame_count, forward_frame_count)
        if count == 1:
            return [0]
        return [int(round(index * (frame_count - 1) / (count - 1))) for index in range(count)]
    if output_fps >= source_fps:
        return list(range(frame_count))

    step = source_fps / output_fps
    selected: list[int] = []
    index = 0.0
    while round(index) < frame_count:
        selected.append(int(round(index)))
        index += step
    return selected


def make_ping_pong_indices(forward_indices: list[int]) -> list[int]:
    if len(forward_indices) <= 2:
        return list(forward_indices)
    return list(forward_indices) + list(reversed(forward_indices[1:-1]))


def extract_subject_rgba(frame_rgb: np.ndarray, options: ProcessingOptions) -> tuple[Image.Image, tuple[int, int, int, int]]:
    if frame_rgb.ndim != 3 or frame_rgb.shape[2] != 3:
        raise ValueError("frame_rgb must be an RGB image array with shape (height, width, 3)")

    hsv = cv2.cvtColor(frame_rgb, cv2.COLOR_RGB2HSV)
    green_mask = cv2.inRange(
        hsv,
        np.array([options.hue_min, options.saturation_min, options.value_min], dtype=np.uint8),
        np.array([options.hue_max, 255, 255], dtype=np.uint8),
    )
    foreground = (green_mask == 0).astype(np.uint8)

    if options.clean_kernel_size > 1:
        kernel = np.ones((options.clean_kernel_size, options.clean_kernel_size), dtype=np.uint8)
        foreground = cv2.morphologyEx(foreground, cv2.MORPH_OPEN, kernel)
        foreground = cv2.morphologyEx(foreground, cv2.MORPH_CLOSE, kernel)

    if options.keep_largest_component:
        foreground = keep_largest_foreground_component(foreground)

    alpha = (foreground * 255).astype(np.uint8)
    bbox = alpha_bbox(alpha)
    rgb = suppress_green_spill(frame_rgb, alpha)
    rgba = np.dstack([rgb, alpha])
    return Image.fromarray(rgba, "RGBA"), bbox


def keep_largest_foreground_component(foreground: np.ndarray) -> np.ndarray:
    component_count, labels, stats, _ = cv2.connectedComponentsWithStats(foreground, 8)
    if component_count <= 1:
        return foreground

    areas = stats[1:, cv2.CC_STAT_AREA]
    largest_label = int(np.argmax(areas) + 1)
    return (labels == largest_label).astype(np.uint8)


def alpha_bbox(alpha: np.ndarray) -> tuple[int, int, int, int]:
    ys, xs = np.where(alpha > 0)
    if len(xs) == 0:
        return (0, 0, alpha.shape[1], alpha.shape[0])
    left = int(xs.min())
    top = int(ys.min())
    right = int(xs.max()) + 1
    bottom = int(ys.max()) + 1
    return (left, top, right, bottom)


def suppress_green_spill(frame_rgb: np.ndarray, alpha: np.ndarray) -> np.ndarray:
    rgb = frame_rgb.copy()
    foreground = alpha > 0
    if not np.any(foreground):
        return rgb

    red = rgb[:, :, 0].astype(np.int16)
    green = rgb[:, :, 1].astype(np.int16)
    blue = rgb[:, :, 2].astype(np.int16)
    green_limit = np.maximum(red, blue) + 24
    rgb[:, :, 1] = np.where(foreground & (green > green_limit), green_limit, green).clip(0, 255).astype(np.uint8)
    return rgb


def process_rgb_frames(frames: Iterable[np.ndarray], source_fps: float, options: ProcessingOptions) -> ProcessResult:
    source_frames = list(frames)
    if not source_frames:
        raise ValueError("No frames selected for processing")

    extracted = [extract_subject_rgba(frame, options) for frame in source_frames]
    forward_indices = select_frame_indices(
        len(source_frames),
        source_fps,
        options.output_fps,
        options.forward_frame_count,
    )
    loop_indices = make_ping_pong_indices(forward_indices)
    if not loop_indices:
        raise ValueError("No frames selected for processing")

    crop = union_bbox([bbox for _, bbox in extracted], source_frames[0].shape, options.crop_padding)
    processed_frames = [
        pixelize_rgba(rgba.crop(crop), options.target_frame_height, options.palette_colors)
        for rgba, _ in (extracted[index] for index in loop_indices)
    ]

    frame_width, frame_height = processed_frames[0].size
    manifest = {
        "source_fps": int(source_fps) if float(source_fps).is_integer() else source_fps,
        "output_fps": options.output_fps,
        "frame_count": len(processed_frames),
        "forward_frame_count": len(forward_indices),
        "requested_forward_frame_count": options.forward_frame_count,
        "loop_mode": "pingpong_no_duplicate_endpoints",
        "loop_frame_indices": loop_indices,
        "frame_size": {"width": frame_width, "height": frame_height},
        "source_crop": {
            "x": crop[0],
            "y": crop[1],
            "width": crop[2] - crop[0],
            "height": crop[3] - crop[1],
        },
        "options": asdict(options),
    }
    return ProcessResult(frames=processed_frames, manifest=manifest)


def union_bbox(
    bboxes: Iterable[tuple[int, int, int, int]],
    frame_shape: tuple[int, int, int],
    padding: int,
) -> tuple[int, int, int, int]:
    boxes = list(bboxes)
    left = max(0, min(box[0] for box in boxes) - padding)
    top = max(0, min(box[1] for box in boxes) - padding)
    right = min(frame_shape[1], max(box[2] for box in boxes) + padding)
    bottom = min(frame_shape[0], max(box[3] for box in boxes) + padding)
    return (left, top, right, bottom)


def pixelize_rgba(rgba: Image.Image, target_height: int, palette_colors: int | None = None) -> Image.Image:
    if target_height <= 0:
        raise ValueError("--frame-height must be greater than 0")

    width, height = rgba.size
    if height <= 0:
        raise ValueError("Cannot pixelize an empty frame")
    target_width = max(1, int(math.ceil(width * target_height / height)))

    pixel_frame = rgba.resize((target_width, target_height), Image.Resampling.NEAREST)
    if palette_colors and palette_colors > 0:
        pixel_frame = quantize_rgba(pixel_frame, palette_colors)
    return pixel_frame


def quantize_rgba(rgba: Image.Image, palette_colors: int) -> Image.Image:
    rgb = Image.new("RGB", rgba.size, (0, 0, 0))
    rgb.paste(rgba.convert("RGB"), mask=rgba.getchannel("A"))
    quantized = rgb.quantize(colors=max(2, palette_colors), method=Image.Quantize.MEDIANCUT).convert("RGBA")
    quantized.putalpha(rgba.getchannel("A"))
    return quantized


def write_outputs(frames: list[Image.Image], manifest: dict, out_dir: Path, preview_scale: int = 4) -> None:
    if not frames:
        raise ValueError("No frames to write")
    if preview_scale <= 0:
        raise ValueError("--preview-scale must be greater than 0")

    frames_dir = out_dir / "frames"
    frames_dir.mkdir(parents=True, exist_ok=True)

    for index, frame in enumerate(frames):
        frame.save(frames_dir / f"frame_{index:03d}.png")

    sheet = make_sheet(frames)
    sheet.save(out_dir / "sheet.png")

    preview = sheet.resize(
        (sheet.width * preview_scale, sheet.height * preview_scale),
        Image.Resampling.NEAREST,
    )
    preview.save(out_dir / "preview.png")

    (out_dir / "manifest.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )


def make_sheet(frames: list[Image.Image]) -> Image.Image:
    frame_width, frame_height = frames[0].size
    sheet = Image.new("RGBA", (frame_width * len(frames), frame_height), (0, 0, 0, 0))
    for index, frame in enumerate(frames):
        if frame.size != (frame_width, frame_height):
            raise ValueError("All frames must have the same size")
        sheet.alpha_composite(frame, (index * frame_width, 0))
    return sheet


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Convert green-screen MP4 animation into crisp pixel PNG assets.")
    parser.add_argument("--input", required=True, type=Path, help="Input green-screen video path.")
    parser.add_argument("--out-dir", required=True, type=Path, help="Output directory for sheet, frames, preview, manifest.")
    parser.add_argument("--frame-height", type=int, default=96, help="Target pixel-art frame height. Default: 96.")
    parser.add_argument("--fps", type=int, default=12, help="Output frame rate used for frame sampling. Default: 12.")
    parser.add_argument("--palette-colors", type=int, default=48, help="Quantize RGB colors to this count. Use 0 to disable.")
    parser.add_argument(
        "--forward-frames",
        "--frame-count",
        dest="forward_frames",
        type=int,
        default=0,
        help="Frames sampled for the forward pass. Lower values play faster. Default: 0 uses FPS sampling.",
    )
    parser.add_argument("--preview-scale", type=int, default=4, help="Nearest-neighbor preview scale. Default: 4.")
    parser.add_argument("--crop-padding", type=int, default=8, help="Padding around the union subject crop. Default: 8.")
    parser.add_argument("--keep-all-components", action="store_true", help="Keep disconnected foreground components.")
    parser.add_argument("--hue-min", type=int, default=45, help="Minimum HSV green hue. Default: 45.")
    parser.add_argument("--hue-max", type=int, default=85, help="Maximum HSV green hue. Default: 85.")
    parser.add_argument("--saturation-min", type=int, default=60, help="Minimum saturation for green screen. Default: 60.")
    parser.add_argument("--value-min", type=int, default=80, help="Minimum value for green screen. Default: 80.")
    return parser


def main(argv: list[str] | None = None) -> int:
    args = build_parser().parse_args(argv)
    options = ProcessingOptions(
        target_frame_height=args.frame_height,
        output_fps=args.fps,
        palette_colors=args.palette_colors,
        crop_padding=args.crop_padding,
        forward_frame_count=args.forward_frames,
        hue_min=args.hue_min,
        hue_max=args.hue_max,
        saturation_min=args.saturation_min,
        value_min=args.value_min,
        keep_largest_component=not args.keep_all_components,
    )

    frames, source_fps = read_video_frames(args.input)
    result = process_rgb_frames(frames, source_fps, options)
    write_outputs(result.frames, result.manifest, args.out_dir, args.preview_scale)
    print(f"Wrote {result.manifest['frame_count']} frames to {args.out_dir}")
    print(f"Frame size: {result.manifest['frame_size']['width']}x{result.manifest['frame_size']['height']}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
