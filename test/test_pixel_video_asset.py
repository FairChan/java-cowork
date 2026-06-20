import json
import sys
import tempfile
import unittest
from pathlib import Path

import numpy as np
from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

from tools import pixel_video_asset


class PixelVideoAssetTest(unittest.TestCase):
    def make_green_frame(self, width=64, height=64):
        frame = np.zeros((height, width, 3), dtype=np.uint8)
        frame[:, :] = [0, 255, 0]
        return frame

    def test_green_key_keeps_largest_subject_and_removes_small_watermark(self):
        frame = self.make_green_frame()
        frame[16:48, 24:40] = [220, 20, 30]
        frame[56:62, 54:62] = [255, 255, 255]

        options = pixel_video_asset.ProcessingOptions(target_frame_height=16)
        rgba, bbox = pixel_video_asset.extract_subject_rgba(frame, options)
        alpha = np.array(rgba)[:, :, 3]

        self.assertEqual(255, int(alpha[24, 30]))
        self.assertEqual(0, int(alpha[0, 0]))
        self.assertEqual(0, int(alpha[58, 58]))
        self.assertEqual((24, 16, 40, 48), bbox)

    def test_process_frames_samples_fps_and_outputs_consistent_nearest_neighbor_frames(self):
        frames = []
        for shift in (0, 4, 8, 12):
            frame = self.make_green_frame(80, 80)
            frame[20:60, 20 + shift:44 + shift] = [200, 0, 40]
            frames.append(frame)

        options = pixel_video_asset.ProcessingOptions(
            target_frame_height=16,
            output_fps=12,
            palette_colors=8,
            crop_padding=0,
        )
        result = pixel_video_asset.process_rgb_frames(frames, source_fps=24, options=options)

        self.assertEqual(2, len(result.frames))
        self.assertEqual([(15, 16), (15, 16)], [frame.size for frame in result.frames])
        self.assertEqual({"x": 20, "y": 20, "width": 36, "height": 40}, result.manifest["source_crop"])
        self.assertEqual(24, result.manifest["source_fps"])
        self.assertEqual(12, result.manifest["output_fps"])
        self.assertEqual(2, result.manifest["frame_count"])
        self.assertEqual({"width": 15, "height": 16}, result.manifest["frame_size"])

        alpha_values = set(np.unique(np.array(result.frames[0])[:, :, 3]).tolist())
        self.assertLessEqual(alpha_values, {0, 255})

    def test_forward_frame_count_controls_speed_and_exports_ping_pong_loop(self):
        frames = []
        colors = ([80, 0, 0], [110, 0, 0], [140, 0, 0], [170, 0, 0], [200, 0, 0], [230, 0, 0])
        for color in colors:
            frame = self.make_green_frame(24, 24)
            frame[6:18, 8:16] = color
            frames.append(frame)

        options = pixel_video_asset.ProcessingOptions(
            target_frame_height=12,
            output_fps=12,
            palette_colors=0,
            crop_padding=0,
            forward_frame_count=4,
        )

        result = pixel_video_asset.process_rgb_frames(frames, source_fps=24, options=options)
        sampled_reds = [int(np.array(frame)[6, 4, 0]) for frame in result.frames]

        self.assertEqual([80, 140, 170, 230, 170, 140], sampled_reds)
        self.assertEqual(4, result.manifest["forward_frame_count"])
        self.assertEqual("pingpong_no_duplicate_endpoints", result.manifest["loop_mode"])
        self.assertEqual(6, result.manifest["frame_count"])
        self.assertEqual([0, 2, 3, 5, 3, 2], result.manifest["loop_frame_indices"])

    def test_write_outputs_creates_sheet_sequence_preview_and_manifest(self):
        frames = [
            Image.new("RGBA", (4, 6), (255, 0, 0, 255)),
            Image.new("RGBA", (4, 6), (0, 0, 255, 255)),
        ]
        manifest = {
            "frame_count": 2,
            "frame_size": {"width": 4, "height": 6},
            "output_fps": 12,
        }

        with tempfile.TemporaryDirectory() as temp_dir:
            out_dir = Path(temp_dir)
            pixel_video_asset.write_outputs(frames, manifest, out_dir, preview_scale=3)

            self.assertTrue((out_dir / "sheet.png").exists())
            self.assertTrue((out_dir / "frames" / "frame_000.png").exists())
            self.assertTrue((out_dir / "frames" / "frame_001.png").exists())
            self.assertTrue((out_dir / "preview.png").exists())
            self.assertTrue((out_dir / "manifest.json").exists())

            with Image.open(out_dir / "sheet.png") as sheet:
                self.assertEqual((8, 6), sheet.size)
            with Image.open(out_dir / "preview.png") as preview:
                self.assertEqual((24, 18), preview.size)
            saved_manifest = json.loads((out_dir / "manifest.json").read_text(encoding="utf-8"))
            self.assertEqual(manifest["frame_size"], saved_manifest["frame_size"])
            self.assertEqual(manifest["frame_count"], saved_manifest["frame_count"])


if __name__ == "__main__":
    unittest.main()
