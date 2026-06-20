import sys
import tempfile
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

from tools import pixel_video_asset_server
from tools.pixel_video_asset import ProcessResult


class PixelVideoAssetUiTest(unittest.TestCase):
    def test_options_from_payload_uses_ui_values(self):
        payload = {
            "frameHeight": "80",
            "fps": "10",
            "paletteColors": "32",
            "previewScale": "5",
            "cropPadding": "12",
            "forwardFrames": "18",
            "keepLargestComponent": False,
            "hueMin": "42",
            "hueMax": "88",
            "saturationMin": "70",
            "valueMin": "90",
        }

        options, preview_scale = pixel_video_asset_server.options_from_payload(payload)

        self.assertEqual(80, options.target_frame_height)
        self.assertEqual(10, options.output_fps)
        self.assertEqual(32, options.palette_colors)
        self.assertEqual(12, options.crop_padding)
        self.assertEqual(18, options.forward_frame_count)
        self.assertFalse(options.keep_largest_component)
        self.assertEqual(42, options.hue_min)
        self.assertEqual(88, options.hue_max)
        self.assertEqual(70, options.saturation_min)
        self.assertEqual(90, options.value_min)
        self.assertEqual(5, preview_scale)

    def test_process_video_uses_pipeline_and_returns_browser_urls(self):
        calls = []

        def fake_read(input_path):
            calls.append(("read", input_path.name))
            return ["frame"], 24

        def fake_process(frames, source_fps, options):
            calls.append(("process", frames, source_fps, options.target_frame_height))
            return ProcessResult(
                frames=["pixel-frame"],
                manifest={
                    "frame_count": 1,
                    "frame_size": {"width": 8, "height": 16},
                    "output_fps": options.output_fps,
                },
            )

        def fake_write(frames, manifest, out_dir, preview_scale):
            calls.append(("write", frames, manifest["frame_count"], out_dir.name, preview_scale))
            out_dir.mkdir(parents=True, exist_ok=True)
            (out_dir / "preview.png").write_bytes(b"png")
            (out_dir / "sheet.png").write_bytes(b"png")
            (out_dir / "manifest.json").write_text("{}", encoding="utf-8")

        with tempfile.TemporaryDirectory() as temp_dir:
            input_path = Path(temp_dir) / "source.mp4"
            input_path.write_bytes(b"video")
            out_dir = Path(temp_dir) / "asset"
            options = pixel_video_asset_server.ProcessingOptions(target_frame_height=16, output_fps=12)

            response = pixel_video_asset_server.process_video(
                input_path,
                out_dir,
                options,
                preview_scale=4,
                read_fn=fake_read,
                process_fn=fake_process,
                write_fn=fake_write,
            )

        self.assertEqual(
            [
                ("read", "source.mp4"),
                ("process", ["frame"], 24, 16),
                ("write", ["pixel-frame"], 1, "asset", 4),
            ],
            calls,
        )
        self.assertEqual(1, response["manifest"]["frame_count"])
        self.assertTrue(response["previewUrl"].startswith("/api/file?path="))
        self.assertTrue(response["sheetUrl"].startswith("/api/file?path="))
        self.assertTrue(response["manifestUrl"].startswith("/api/file?path="))

    def test_sanitize_upload_name_removes_path_segments(self):
        self.assertEqual("asset.mp4", pixel_video_asset_server.sanitize_upload_name("..\\demo/asset.mp4"))
        self.assertEqual("upload.mp4", pixel_video_asset_server.sanitize_upload_name(""))


if __name__ == "__main__":
    unittest.main()
