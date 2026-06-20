from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE_DIR = ROOT / "output" / "pixel_assets" / "reimu-card" / "frames"
OUT = ROOT / "assets" / "sprites" / "player_card.png"


def main() -> None:
    frames = sorted(SOURCE_DIR.glob("*.png"))
    if not frames:
        raise SystemExit(f"No frames found in {SOURCE_DIR}")
    images = [Image.open(frame).convert("RGBA") for frame in frames]
    width, height = images[0].size
    for frame, image in zip(frames, images):
        if image.size != (width, height):
            raise SystemExit(f"{frame} has size {image.size}; expected {(width, height)}")
    sheet = Image.new("RGBA", (width * len(images), height), (0, 0, 0, 0))
    for index, image in enumerate(images):
        sheet.alpha_composite(image, (index * width, 0))
    OUT.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(OUT)
    print(f"Wrote {OUT} from {len(images)} frames of {width}x{height}")


if __name__ == "__main__":
    main()
