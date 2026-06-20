#!/usr/bin/env python
"""
Local browser UI for the green-screen pixel asset converter.
"""

from __future__ import annotations

import argparse
import json
import mimetypes
import os
import re
import subprocess
import sys
import webbrowser
from http import HTTPStatus
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from typing import Any, Callable
from urllib.parse import parse_qs, quote, unquote, urlparse

if __package__ in (None, ""):
    sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from tools.pixel_video_asset import (
    ProcessingOptions,
    process_rgb_frames,
    read_video_frames,
    write_outputs,
)


PROJECT_ROOT = Path(__file__).resolve().parents[1]
STATIC_DIR = Path(__file__).with_name("pixel_video_asset_ui")
UPLOAD_DIR = PROJECT_ROOT / "tmp" / "pixel_video_asset_ui" / "uploads"
DEFAULT_INPUT = Path(r"C:\Users\ssema\Downloads\d61903a7-2dff-424e-a35b-c65f264cb521.mp4")
DEFAULT_OUTPUT = PROJECT_ROOT / "output" / "pixel_assets" / "reimu"


def parse_int(payload: dict[str, Any], key: str, default: int, minimum: int, maximum: int | None = None) -> int:
    raw_value = payload.get(key, default)
    try:
        value = int(raw_value)
    except (TypeError, ValueError):
        value = default
    value = max(minimum, value)
    if maximum is not None:
        value = min(maximum, value)
    return value


def options_from_payload(payload: dict[str, Any]) -> tuple[ProcessingOptions, int]:
    preview_scale = parse_int(payload, "previewScale", 4, 1, 16)
    options = ProcessingOptions(
        target_frame_height=parse_int(payload, "frameHeight", 96, 8, 1024),
        output_fps=parse_int(payload, "fps", 12, 1, 240),
        palette_colors=parse_int(payload, "paletteColors", 48, 0, 256),
        crop_padding=parse_int(payload, "cropPadding", 8, 0, 256),
        hue_min=parse_int(payload, "hueMin", 45, 0, 179),
        hue_max=parse_int(payload, "hueMax", 85, 0, 179),
        saturation_min=parse_int(payload, "saturationMin", 60, 0, 255),
        value_min=parse_int(payload, "valueMin", 80, 0, 255),
        keep_largest_component=bool(payload.get("keepLargestComponent", True)),
    )
    return options, preview_scale


def resolve_user_path(raw_path: str | None, default: Path) -> Path:
    if not raw_path:
        return default
    path = Path(raw_path).expanduser()
    if not path.is_absolute():
        path = PROJECT_ROOT / path
    return path


def file_url(path: Path) -> str:
    return f"/api/file?path={quote(str(path.resolve()))}"


def process_video(
    input_path: Path,
    out_dir: Path,
    options: ProcessingOptions,
    preview_scale: int,
    read_fn: Callable[[Path], tuple[list[Any], float]] = read_video_frames,
    process_fn: Callable[[list[Any], float, ProcessingOptions], Any] = process_rgb_frames,
    write_fn: Callable[[list[Any], dict, Path, int], None] = write_outputs,
) -> dict[str, Any]:
    frames, source_fps = read_fn(input_path)
    result = process_fn(frames, source_fps, options)
    write_fn(result.frames, result.manifest, out_dir, preview_scale)

    return {
        "ok": True,
        "inputPath": str(input_path),
        "outDir": str(out_dir),
        "manifest": result.manifest,
        "previewUrl": file_url(out_dir / "preview.png"),
        "sheetUrl": file_url(out_dir / "sheet.png"),
        "manifestUrl": file_url(out_dir / "manifest.json"),
        "framesUrl": file_url(out_dir / "frames"),
    }


def sanitize_upload_name(file_name: str | None) -> str:
    name = Path((file_name or "").replace("\\", "/")).name
    name = re.sub(r"[^A-Za-z0-9._-]+", "_", name).strip("._")
    return name or "upload.mp4"


def open_directory(path: Path) -> None:
    path.mkdir(parents=True, exist_ok=True)
    if os.name == "nt":
        os.startfile(path)  # type: ignore[attr-defined]
    elif sys.platform == "darwin":
        subprocess.Popen(["open", str(path)])
    else:
        subprocess.Popen(["xdg-open", str(path)])


class PixelAssetRequestHandler(BaseHTTPRequestHandler):
    server_version = "PixelAssetWorkbench/1.0"

    def do_GET(self) -> None:
        parsed = urlparse(self.path)
        if parsed.path == "/":
            self.serve_static_file(STATIC_DIR / "index.html")
        elif parsed.path == "/api/presets":
            self.send_json(
                {
                    "defaultInput": str(DEFAULT_INPUT),
                    "defaultOutput": str(DEFAULT_OUTPUT.relative_to(PROJECT_ROOT)),
                    "defaultInputExists": DEFAULT_INPUT.exists(),
                }
            )
        elif parsed.path == "/api/file":
            self.serve_user_file(parsed.query)
        else:
            target = STATIC_DIR / parsed.path.lstrip("/")
            self.serve_static_file(target)

    def do_POST(self) -> None:
        parsed = urlparse(self.path)
        try:
            if parsed.path == "/api/process-path":
                payload = self.read_json_body()
                input_path = resolve_user_path(payload.get("inputPath"), DEFAULT_INPUT)
                out_dir = resolve_user_path(payload.get("outDir"), DEFAULT_OUTPUT)
                self.run_processing_payload(payload, input_path, out_dir)
            elif parsed.path == "/api/process-upload":
                payload = {key: values[-1] for key, values in parse_qs(parsed.query).items()}
                file_name = sanitize_upload_name(self.headers.get("X-File-Name"))
                UPLOAD_DIR.mkdir(parents=True, exist_ok=True)
                input_path = UPLOAD_DIR / file_name
                input_path.write_bytes(self.rfile.read(int(self.headers.get("Content-Length", "0"))))
                out_dir = resolve_user_path(payload.get("outDir"), DEFAULT_OUTPUT)
                self.run_processing_payload(payload, input_path, out_dir)
            elif parsed.path == "/api/open-output":
                payload = self.read_json_body()
                out_dir = resolve_user_path(payload.get("outDir"), DEFAULT_OUTPUT)
                open_directory(out_dir)
                self.send_json({"ok": True, "outDir": str(out_dir)})
            else:
                self.send_error(HTTPStatus.NOT_FOUND, "Unknown endpoint")
        except Exception as error:  # noqa: BLE001 - local UI should return the readable cause.
            self.send_json({"ok": False, "error": str(error)}, HTTPStatus.BAD_REQUEST)

    def run_processing_payload(self, payload: dict[str, Any], input_path: Path, out_dir: Path) -> None:
        if not input_path.exists():
            raise FileNotFoundError(f"Input video not found: {input_path}")
        options, preview_scale = options_from_payload(payload)
        response = process_video(input_path, out_dir, options, preview_scale)
        self.send_json(response)

    def read_json_body(self) -> dict[str, Any]:
        content_length = int(self.headers.get("Content-Length", "0"))
        if content_length <= 0:
            return {}
        raw = self.rfile.read(content_length).decode("utf-8")
        return json.loads(raw)

    def serve_user_file(self, query: str) -> None:
        params = parse_qs(query)
        raw_path = params.get("path", [""])[-1]
        target = Path(unquote(raw_path))
        if not target.exists():
            self.send_error(HTTPStatus.NOT_FOUND, "File not found")
            return
        if target.is_dir():
            self.send_json({"path": str(target), "files": sorted(child.name for child in target.iterdir())})
            return
        self.send_file(target)

    def serve_static_file(self, path: Path) -> None:
        target = path.resolve()
        try:
            target.relative_to(STATIC_DIR.resolve())
        except ValueError:
            if target != (STATIC_DIR / "index.html").resolve():
                self.send_error(HTTPStatus.FORBIDDEN)
                return
        if not target.exists() or not target.is_file():
            self.send_error(HTTPStatus.NOT_FOUND)
            return
        self.send_file(target)

    def send_file(self, path: Path) -> None:
        content_type = mimetypes.guess_type(path.name)[0] or "application/octet-stream"
        data = path.read_bytes()
        self.send_response(HTTPStatus.OK)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def send_json(self, payload: dict[str, Any], status: HTTPStatus = HTTPStatus.OK) -> None:
        data = json.dumps(payload, ensure_ascii=False, indent=2).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def log_message(self, format: str, *args: Any) -> None:
        sys.stderr.write("[pixel-ui] " + format % args + "\n")


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Run the pixel video asset browser UI.")
    parser.add_argument("--host", default="127.0.0.1", help="Host to bind. Default: 127.0.0.1")
    parser.add_argument("--port", type=int, default=8765, help="Port to bind. Default: 8765")
    parser.add_argument("--no-open", action="store_true", help="Do not open the browser automatically.")
    return parser


def main(argv: list[str] | None = None) -> int:
    args = build_parser().parse_args(argv)
    address = (args.host, args.port)
    server = ThreadingHTTPServer(address, PixelAssetRequestHandler)
    url = f"http://{args.host}:{args.port}/"
    print(f"Pixel asset UI running at {url}")
    if not args.no_open:
        webbrowser.open(url)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nStopping pixel asset UI")
    finally:
        server.server_close()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
