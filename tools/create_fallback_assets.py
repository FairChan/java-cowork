from pathlib import Path
from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
SPRITES = ROOT / "assets" / "sprites"
BACKGROUNDS = ROOT / "assets" / "backgrounds"


def ensure_dirs() -> None:
    SPRITES.mkdir(parents=True, exist_ok=True)
    BACKGROUNDS.mkdir(parents=True, exist_ok=True)


def draw_player(draw: ImageDraw.ImageDraw, cx: int, cy: int, frame: int, focused: bool) -> None:
    bob = int(round([0, -2, 0, 2][frame % 4]))
    aura = (132, 231, 255, 115 if focused else 0)
    if focused:
        draw.ellipse((cx - 19, cy - 19 + bob, cx + 19, cy + 19 + bob), outline=aura, width=2)
    draw.polygon([(cx, cy - 26 + bob), (cx - 7, cy - 10 + bob), (cx + 7, cy - 10 + bob)], fill=(255, 239, 142, 255))
    draw.ellipse((cx - 9, cy - 19 + bob, cx + 9, cy + 3 + bob), fill=(248, 241, 255, 255), outline=(90, 42, 98, 255))
    sleeve = 3 if frame % 2 == 0 else -1
    draw.polygon([(cx - 9, cy - 4 + bob), (cx - 24, cy + 5 + bob + sleeve), (cx - 14, cy + 13 + bob)], fill=(255, 246, 252, 255))
    draw.polygon([(cx + 9, cy - 4 + bob), (cx + 24, cy + 5 + bob - sleeve), (cx + 14, cy + 13 + bob)], fill=(255, 246, 252, 255))
    draw.polygon([(cx, cy - 2 + bob), (cx - 17, cy + 24 + bob), (cx + 17, cy + 24 + bob)], fill=(210, 28, 86, 255))
    draw.rectangle((cx - 5, cy + 2 + bob, cx + 5, cy + 18 + bob), fill=(255, 245, 195, 255))
    draw.ellipse((cx - 3, cy - 9 + bob, cx + 3, cy - 3 + bob), fill=(62, 35, 80, 255))


def draw_lantern(draw: ImageDraw.ImageDraw, cx: int, cy: int, frame: int) -> None:
    bob = int(round([0, -2, 0, 2][frame % 4]))
    glow = (255, 187, 89, 70)
    draw.ellipse((cx - 21, cy - 22 + bob, cx + 21, cy + 22 + bob), fill=glow)
    draw.rounded_rectangle((cx - 15, cy - 19 + bob, cx + 15, cy + 18 + bob), radius=8, fill=(245, 95, 90, 255), outline=(92, 32, 50, 255), width=2)
    draw.rectangle((cx - 11, cy - 10 + bob, cx + 11, cy + 9 + bob), fill=(255, 209, 114, 255))
    draw.arc((cx - 6, cy - 4 + bob, cx + 6, cy + 8 + bob), 0, 180, fill=(84, 34, 48, 255), width=2)
    draw.line((cx, cy + 18 + bob, cx, cy + 25 + bob), fill=(255, 226, 134, 255), width=2)


def draw_fairy(draw: ImageDraw.ImageDraw, cx: int, cy: int, frame: int) -> None:
    bob = int(round([1, -1, -2, 1][frame % 4]))
    flutter = 3 if frame % 2 == 0 else -3
    draw.polygon([(cx - 6, cy - 22 + bob), (cx - 22, cy - 5 + bob + flutter), (cx - 9, cy + 4 + bob)], fill=(226, 232, 255, 210))
    draw.polygon([(cx + 6, cy - 22 + bob), (cx + 22, cy - 5 + bob - flutter), (cx + 9, cy + 4 + bob)], fill=(226, 232, 255, 210))
    draw.rounded_rectangle((cx - 11, cy - 17 + bob, cx + 11, cy + 19 + bob), radius=4, fill=(250, 239, 188, 255), outline=(92, 62, 78, 255), width=2)
    draw.rectangle((cx - 8, cy - 10 + bob, cx + 8, cy - 4 + bob), fill=(220, 44, 82, 255))
    draw.line((cx - 14, cy + 15 + bob, cx - 22, cy + 25 + bob + flutter), fill=(248, 225, 126, 255), width=2)
    draw.line((cx + 14, cy + 15 + bob, cx + 22, cy + 25 + bob - flutter), fill=(248, 225, 126, 255), width=2)


def draw_boss(draw: ImageDraw.ImageDraw, cx: int, cy: int, frame: int) -> None:
    bob = int(round([0, -2, -3, -2, 0, 2][frame % 6]))
    cast = frame >= 3
    draw.ellipse((cx - 34, cy - 34 + bob, cx + 34, cy + 34 + bob), outline=(248, 215, 255, 140), width=2)
    if cast:
        draw.arc((cx - 46, cy - 46 + bob, cx + 46, cy + 46 + bob), 200, 330, fill=(255, 245, 152, 255), width=5)
    draw.ellipse((cx - 20, cy - 28 + bob, cx + 20, cy + 24 + bob), fill=(225, 211, 255, 255), outline=(49, 28, 80, 255), width=2)
    draw.polygon([(cx, cy - 5 + bob), (cx - 29, cy + 34 + bob), (cx + 29, cy + 34 + bob)], fill=(85, 51, 166, 255))
    sleeve_shift = 5 if cast else 0
    draw.polygon([(cx - 14, cy - 8 + bob), (cx - 42, cy + 3 + bob - sleeve_shift), (cx - 22, cy + 20 + bob)], fill=(184, 153, 235, 255))
    draw.polygon([(cx + 14, cy - 8 + bob), (cx + 42, cy + 3 + bob - sleeve_shift), (cx + 22, cy + 20 + bob)], fill=(184, 153, 235, 255))
    draw.ellipse((cx - 5, cy - 15 + bob, cx + 5, cy - 5 + bob), fill=(43, 26, 68, 255))


def make_sheet(path: Path, frame_w: int, frame_h: int, frames: int, painter) -> None:
    img = Image.new("RGBA", (frame_w * frames, frame_h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    for frame in range(frames):
        cx = frame * frame_w + frame_w // 2
        cy = frame_h // 2
        painter(draw, cx, cy, frame)
    img.save(path)


def make_background(path: Path) -> None:
    w, h = 620, 652
    img = Image.new("RGBA", (w, h), (8, 9, 28, 255))
    draw = ImageDraw.Draw(img)
    for y in range(h):
        ratio = y / h
        color = (
            int(9 + 10 * ratio),
            int(11 + 13 * ratio),
            int(34 + 30 * ratio),
            255,
        )
        draw.line((0, y, w, y), fill=color)
    draw.ellipse((w - 140, 42, w - 70, 112), fill=(248, 230, 158, 230))
    draw.ellipse((w - 116, 33, w - 47, 102), fill=(9, 11, 34, 240))
    for i in range(36):
        x = (i * 97) % w
        y = 34 + ((i * 53) % 360)
        draw.ellipse((x, y, x + 2, y + 2), fill=(207, 224, 255, 180))
    draw.polygon([(48, h - 80), (w // 2, h - 164), (w - 48, h - 80)], fill=(22, 17, 41, 255))
    draw.rectangle((82, h - 82, w - 82, h - 40), fill=(18, 15, 34, 255))
    draw.rectangle((168, h - 138, 188, h - 38), fill=(42, 30, 56, 255))
    draw.rectangle((432, h - 138, 452, h - 38), fill=(42, 30, 56, 255))
    draw.line((w // 2, 170, w // 2, h), fill=(55, 49, 75, 120), width=46)
    draw.line((w // 2, 170, w // 2, h), fill=(110, 98, 130, 95), width=12)
    img.save(path)


def main() -> None:
    ensure_dirs()
    make_sheet(SPRITES / "player_flight.png", 64, 64, 4, lambda d, x, y, f: draw_player(d, x, y, f, False))
    make_sheet(SPRITES / "player_focus.png", 64, 64, 4, lambda d, x, y, f: draw_player(d, x, y, f, True))
    make_sheet(SPRITES / "enemy_lantern.png", 56, 56, 4, draw_lantern)
    make_sheet(SPRITES / "enemy_charm_fairy.png", 56, 56, 4, draw_fairy)
    make_sheet(SPRITES / "boss_moon_spirit.png", 96, 96, 6, draw_boss)
    make_background(BACKGROUNDS / "stage1_moonlit_shrine.png")
    print("Fallback stage 1 assets written to assets/")


if __name__ == "__main__":
    main()
