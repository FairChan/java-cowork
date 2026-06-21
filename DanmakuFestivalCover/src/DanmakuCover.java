import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.GraphicsConfiguration;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class DanmakuCover {
    public static void main(String[] args) {
        if (args.length >= 2 && "--export".equals(args[0])) {
            System.setProperty("java.awt.headless", "true");
            CoverPanel panel = new CoverPanel(false);
            panel.setSize(1280, 720);
            int previewTick = args.length >= 3 ? Integer.parseInt(args[2]) : 96;
            panel.setPreviewTick(previewTick);
            if (args.length >= 4 && "start".equalsIgnoreCase(args[3])) {
                panel.setStartPreview(0);
            }

            BufferedImage image = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            panel.paint(g);
            g.dispose();

            try {
                ImageIO.write(image, "png", new File(args[1]));
            } catch (IOException error) {
                System.err.println("Failed to export cover: " + error.getMessage());
                System.exit(1);
            }
            return;
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Fantasy Danmaku Festival");
            CoverPanel panel = new CoverPanel();

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
}

class CoverPanel extends JPanel {
    private static final int TIMER_DELAY_MS = 33;
    private static final int LOGICAL_TICKS_PER_FRAME = 2;
    private static final String TITLE = "FANTASY DANMAKU FESTIVAL";
    private static final String SUBTITLE = "Sakura Shrine Gate";

    private final MenuEntry[] menu = {
            new MenuEntry("START", "Begin the festival"),
            new MenuEntry("PRACTICE", "Warm up a route"),
            new MenuEntry("SPELL PRACTICE", "Train a pattern"),
            new MenuEntry("CHARACTER ARCHIVE", "View portraits"),
            new MenuEntry("MUSIC ROOM", "Listen again"),
            new MenuEntry("OPTIONS", "Adjust settings"),
            new MenuEntry("EXIT", "Close game")
    };

    private final Set<String> availableFonts = new HashSet<>(
            Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
    private final List<Petal> petals = new ArrayList<>();
    private final Random random = new Random(20260621L);
    private final Rectangle2D.Double[] menuBounds = new Rectangle2D.Double[menu.length];

    private final BufferedImage[] redHeroes;
    private final BufferedImage[] witchHeroes;
    private final BufferedImage[] redHeroShadows;
    private final BufferedImage[] witchHeroShadows;
    private final BufferedImage backgroundImage;
    private BufferedImage cachedStillBackground;
    private int cachedStillBackgroundWidth = -1;
    private int cachedStillBackgroundHeight = -1;

    private int selectedMenu = 0;
    private int tick = 0;
    private int startCameraTick = -1;

    CoverPanel() {
        this(true);
    }

    CoverPanel(boolean startAnimation) {
        setPreferredSize(new Dimension(1280, 720));
        setMinimumSize(new Dimension(960, 540));
        setBackground(new Color(9, 21, 39));
        setFocusable(true);

        backgroundImage = loadAsset("shrine_gate_background.png", new Color(83, 143, 118));
        redHeroes = loadPoseSet(new String[]{
                "poses/reimu_peace.png",
                "poses/reimu_talisman.png"
        }, new Color(160, 28, 38));
        witchHeroes = loadPoseSet(new String[]{
                "poses/marisa_hat.png",
                "poses/marisa_broom.png"
        }, new Color(245, 188, 79));
        redHeroShadows = createShadows(redHeroes);
        witchHeroShadows = createShadows(witchHeroes);

        for (int i = 0; i < 128; i++) {
            petals.add(new Petal(random));
        }

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent event) {
                int hit = hitMenu(event.getX(), event.getY());
                if (hit != selectedMenu) {
                    selectedMenu = hit >= 0 ? hit : selectedMenu;
                    repaint();
                }
                setCursor(hit >= 0 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                int hit = hitMenu(event.getX(), event.getY());
                if (hit >= 0) {
                    selectedMenu = hit;
                    activateMenu();
                }
            }
        };
        addMouseMotionListener(mouse);
        addMouseListener(mouse);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_UP) {
                    selectedMenu = (selectedMenu - 1 + menu.length) % menu.length;
                    repaint();
                } else if (event.getKeyCode() == KeyEvent.VK_DOWN) {
                    selectedMenu = (selectedMenu + 1) % menu.length;
                    repaint();
                } else if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    activateMenu();
                } else if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    selectedMenu = menu.length - 1;
                    repaint();
                }
            }
        });

        if (startAnimation) {
            new Timer(TIMER_DELAY_MS, event -> {
                tick += LOGICAL_TICKS_PER_FRAME;
                repaint();
            }).start();
        }
    }

    void setPreviewTick(int tick) {
        this.tick = tick;
    }

    void setStartPreview(int startTick) {
        this.startCameraTick = startTick;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        int width = getWidth();
        int height = getHeight();
        double time = tick / 60.0;
        double scale = Math.min(width / 1280.0, height / 720.0);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        double cameraProgress = startCameraProgress();
        drawBackground(g, width, height, time, scale, cameraProgress);

        float uiAlpha = (float) clamp(1.0 - cameraProgress * 1.85, 0.0, 1.0);
        if (uiAlpha > 0.01f) {
            Composite oldComposite = g.getComposite();
            g.setComposite(AlphaComposite.SrcOver.derive(uiAlpha));
            drawDanmaku(g, width, height, time, scale);
            drawCharacters(g, width, height, time, scale);
            drawPetals(g, width, height, time, scale);
            drawTitle(g, width, height, time, scale);
            drawMenu(g, width, height, time, scale);
            drawCornerHint(g, width, height, scale);
            g.setComposite(oldComposite);
        }

        if (cameraProgress > 0.0) {
            drawStartTransitionOverlay(g, width, height, cameraProgress, scale);
        }
        g.dispose();
    }

    private double startCameraProgress() {
        if (startCameraTick < 0) {
            return 0.0;
        }
        return clamp((tick - startCameraTick) / 150.0, 0.0, 1.0);
    }

    private double easeInOutCubic(double value) {
        double t = clamp(value, 0.0, 1.0);
        return t < 0.5 ? 4.0 * t * t * t : 1.0 - Math.pow(-2.0 * t + 2.0, 3.0) / 2.0;
    }

    private void drawBackground(Graphics2D g, int width, int height, double time, double scale, double cameraProgress) {
        Paint oldPaint = g.getPaint();
        Composite oldComposite = g.getComposite();
        Object oldInterpolation = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);

        if (cameraProgress <= 0.0001) {
            ensureStillBackground(width, height);
            g.drawImage(cachedStillBackground, 0, 0, null);
            return;
        }

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        drawCameraBackground(g, width, height, cameraProgress);
        drawBackgroundOverlays(g, width, height, cameraProgress);

        if (oldInterpolation != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterpolation);
        }
        g.setComposite(oldComposite);
        g.setPaint(oldPaint);
    }

    private void ensureStillBackground(int width, int height) {
        if (cachedStillBackground != null
                && cachedStillBackgroundWidth == width
                && cachedStillBackgroundHeight == height) {
            return;
        }

        cachedStillBackgroundWidth = width;
        cachedStillBackgroundHeight = height;
        cachedStillBackground = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D cacheGraphics = cachedStillBackground.createGraphics();
        cacheGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        cacheGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        drawCameraBackground(cacheGraphics, width, height, 0.0);
        drawBackgroundOverlays(cacheGraphics, width, height, 0.0);
        cacheGraphics.dispose();
    }

    private void drawBackgroundOverlays(Graphics2D g, int width, int height, double cameraProgress) {
        double settled = easeInOutCubic(cameraProgress);
        g.setComposite(AlphaComposite.SrcOver.derive((float) (0.26 - 0.14 * settled)));
        g.setPaint(new java.awt.RadialGradientPaint(
                new Point2D.Double(width * 0.50, height * (0.47 - 0.08 * settled)),
                (float) (height * (0.68 + 0.22 * settled)),
                new float[]{0f, 0.58f, 1f},
                new Color[]{new Color(255, 255, 232, 92), new Color(255, 180, 222, 42), new Color(0, 0, 0, 0)}));
        g.fillRect(0, 0, width, height);

        g.setComposite(AlphaComposite.SrcOver.derive((float) (0.36 + 0.24 * settled)));
        g.setPaint(new java.awt.RadialGradientPaint(
                new Point2D.Double(width * 0.50, height * 0.50),
                (float) (Math.max(width, height) * 0.72),
                new float[]{0f, 0.70f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(10, 16, 32, 60), new Color(3, 7, 17, 184)}));
        g.fillRect(0, 0, width, height);
    }

    private void drawCameraBackground(Graphics2D g, int width, int height, double cameraProgress) {
        double p = easeInOutCubic(cameraProgress);
        double targetAspect = width / (double) height;
        int imageWidth = backgroundImage.getWidth();
        int imageHeight = backgroundImage.getHeight();

        double baseCropWidth = imageWidth;
        double baseCropHeight = imageWidth / targetAspect;
        if (baseCropHeight > imageHeight) {
            baseCropHeight = imageHeight;
            baseCropWidth = imageHeight * targetAspect;
        }

        double zoom = 1.0 + p * 1.65;
        double cropWidth = baseCropWidth / zoom;
        double cropHeight = baseCropHeight / zoom;
        double focusX = imageWidth * 0.50;
        double focusY = imageHeight * (0.50 - 0.10 * p);
        double sourceX = clamp(focusX - cropWidth * 0.50, 0, imageWidth - cropWidth);
        double sourceY = clamp(focusY - cropHeight * 0.50, 0, imageHeight - cropHeight);

        g.drawImage(backgroundImage,
                0, 0, width, height,
                (int) Math.round(sourceX),
                (int) Math.round(sourceY),
                (int) Math.round(sourceX + cropWidth),
                (int) Math.round(sourceY + cropHeight),
                null);
    }

    private void drawStartTransitionOverlay(Graphics2D g, int width, int height, double cameraProgress, double scale) {
        Composite oldComposite = g.getComposite();
        Paint oldPaint = g.getPaint();
        double p = easeInOutCubic(cameraProgress);

        g.setComposite(AlphaComposite.SrcOver.derive((float) (0.10 + p * 0.18)));
        g.setPaint(new java.awt.RadialGradientPaint(
                new Point2D.Double(width * 0.50, height * (0.44 - 0.06 * p)),
                (float) (height * (0.20 + 0.18 * p)),
                new float[]{0f, 0.52f, 1f},
                new Color[]{new Color(255, 255, 238, 210), new Color(255, 230, 190, 92), new Color(255, 230, 190, 0)}));
        g.fillRect(0, 0, width, height);

        if (p > 0.62) {
            float textAlpha = (float) clamp((p - 0.62) / 0.38, 0.0, 1.0);
            Font loadingFont = chooseFont(Font.BOLD, (float) clamp(22 * scale, 16, 24),
                    "Georgia", "Times New Roman", "Serif");
            String text = "Entering the shrine...";
            FontMetrics metrics = g.getFontMetrics(loadingFont);
            g.setFont(loadingFont);
            g.setComposite(AlphaComposite.SrcOver.derive(textAlpha * 0.78f));
            g.setColor(new Color(255, 249, 230));
            g.drawString(text, (width - metrics.stringWidth(text)) / 2, height - (int) (54 * scale));
        }

        g.setPaint(oldPaint);
        g.setComposite(oldComposite);
    }

    private void drawMoonAndStars(Graphics2D g, int width, int height, double time, double scale) {
        Composite oldComposite = g.getComposite();
        Paint oldPaint = g.getPaint();

        double moonX = width * 0.62;
        double moonY = height * 0.17;
        double moonR = 68 * scale;
        g.setComposite(AlphaComposite.SrcOver.derive(0.34f));
        g.setPaint(new java.awt.RadialGradientPaint(
                new Point2D.Double(moonX, moonY),
                (float) (moonR * 2.8),
                new float[]{0f, 0.38f, 1f},
                new Color[]{new Color(250, 246, 220, 190), new Color(112, 207, 240, 72), new Color(112, 207, 240, 0)}));
        g.fill(new Ellipse2D.Double(moonX - moonR * 2.8, moonY - moonR * 2.8, moonR * 5.6, moonR * 5.6));

        g.setComposite(AlphaComposite.SrcOver.derive(0.88f));
        g.setPaint(new GradientPaint((float) moonX, (float) (moonY - moonR), new Color(255, 251, 223),
                (float) moonX, (float) (moonY + moonR), new Color(199, 232, 234)));
        g.fill(new Ellipse2D.Double(moonX - moonR, moonY - moonR, moonR * 2, moonR * 2));
        g.setComposite(AlphaComposite.SrcOver.derive(0.20f));
        g.setColor(new Color(75, 132, 158));
        g.fill(new Ellipse2D.Double(moonX - 20 * scale, moonY - 24 * scale, 22 * scale, 14 * scale));
        g.fill(new Ellipse2D.Double(moonX + 23 * scale, moonY + 12 * scale, 18 * scale, 10 * scale));
        g.fill(new Ellipse2D.Double(moonX - 44 * scale, moonY + 19 * scale, 13 * scale, 8 * scale));

        for (int i = 0; i < 80; i++) {
            double x = ((i * 173) % 1280) / 1280.0 * width;
            double y = ((i * 61) % 360) / 360.0 * height * 0.52;
            double pulse = 0.45 + 0.34 * Math.sin(time * 1.1 + i);
            g.setComposite(AlphaComposite.SrcOver.derive((float) (0.22 + pulse * 0.28)));
            g.setColor(i % 5 == 0 ? new Color(255, 204, 236) : new Color(202, 244, 255));
            double r = (0.9 + (i % 4) * 0.45) * scale;
            g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
        }

        g.setPaint(oldPaint);
        g.setComposite(oldComposite);
    }

    private void drawShrineSilhouette(Graphics2D g, int width, int height, double scale) {
        Composite oldComposite = g.getComposite();
        Paint oldPaint = g.getPaint();
        java.awt.Stroke oldStroke = g.getStroke();

        drawDistantWoods(g, width, height, scale);
        drawCherryBranches(g, width, height, scale);
        drawShrineAndTorii(g, width, height, scale);
        drawStonePath(g, width, height, scale);
        drawLanterns(g, width, height, scale);

        g.setStroke(oldStroke);
        g.setPaint(oldPaint);
        g.setComposite(oldComposite);
    }

    private void drawDistantWoods(Graphics2D g, int width, int height, double scale) {
        Composite oldComposite = g.getComposite();
        g.setComposite(AlphaComposite.SrcOver.derive(0.58f));

        Path2D mountain = new Path2D.Double();
        mountain.moveTo(0, height * 0.44);
        mountain.curveTo(width * 0.18, height * 0.30, width * 0.27, height * 0.40, width * 0.43, height * 0.28);
        mountain.curveTo(width * 0.57, height * 0.39, width * 0.69, height * 0.28, width * 0.82, height * 0.38);
        mountain.curveTo(width * 0.92, height * 0.46, width, height * 0.35, width, height * 0.45);
        mountain.lineTo(width, height);
        mountain.lineTo(0, height);
        mountain.closePath();
        g.setPaint(new GradientPaint(0, (float) (height * 0.28), new Color(15, 42, 67, 176),
                0, (float) (height * 0.72), new Color(4, 19, 35, 228)));
        g.fill(mountain);

        g.setColor(new Color(4, 18, 28, 190));
        for (int i = 0; i < 36; i++) {
            double x = width * (i / 35.0);
            double base = height * (0.48 + 0.05 * Math.sin(i * 0.7));
            double treeH = (72 + (i % 7) * 13) * scale;
            Path2D tree = new Path2D.Double();
            tree.moveTo(x, base - treeH);
            tree.lineTo(x - 22 * scale, base);
            tree.lineTo(x + 24 * scale, base);
            tree.closePath();
            g.fill(tree);
        }
        g.setComposite(oldComposite);
    }

    private void drawCherryBranches(Graphics2D g, int width, int height, double scale) {
        Composite oldComposite = g.getComposite();
        java.awt.Stroke oldStroke = g.getStroke();
        g.setComposite(AlphaComposite.SrcOver.derive(0.82f));
        g.setStroke(new BasicStroke((float) (15 * scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(28, 17, 37, 226));
        g.draw(new Line2D.Double(-30 * scale, height * 0.12, width * 0.34, height * 0.28));
        g.draw(new Line2D.Double(width + 35 * scale, height * 0.09, width * 0.70, height * 0.26));
        g.setStroke(new BasicStroke((float) (8 * scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(width * 0.07, height * 0.16, width * 0.24, height * 0.08));
        g.draw(new Line2D.Double(width * 0.83, height * 0.12, width * 0.67, height * 0.19));

        for (int i = 0; i < 48; i++) {
            double side = i % 2 == 0 ? 0.0 : 1.0;
            double x = side == 0.0
                    ? width * (0.02 + ((i * 37) % 38) / 100.0)
                    : width * (0.62 + ((i * 41) % 36) / 100.0);
            double y = height * (0.05 + ((i * 29) % 22) / 100.0);
            double r = (3.2 + (i % 5) * 1.05) * scale;
            g.setComposite(AlphaComposite.SrcOver.derive(0.18f));
            g.setColor(new Color(255, 158, 217));
            g.fill(new Ellipse2D.Double(x - r * 2.3, y - r * 1.5, r * 4.6, r * 3.0));
            g.setComposite(AlphaComposite.SrcOver.derive(0.62f));
            g.setColor(new Color(255, 206, 233));
            g.fill(new Ellipse2D.Double(x - r, y - r * 0.72, r * 2, r * 1.44));
        }

        g.setStroke(oldStroke);
        g.setComposite(oldComposite);
    }

    private void drawShrineAndTorii(Graphics2D g, int width, int height, double scale) {
        Composite oldComposite = g.getComposite();
        Paint oldPaint = g.getPaint();
        java.awt.Stroke oldStroke = g.getStroke();

        double cx = width * 0.50;
        double shrineY = height * 0.24;
        double shrineW = 360 * scale;
        double shrineH = 155 * scale;

        g.setComposite(AlphaComposite.SrcOver.derive(0.50f));
        g.setPaint(new GradientPaint((float) cx, (float) shrineY, new Color(248, 218, 157, 90),
                (float) cx, (float) (shrineY + shrineH), new Color(248, 218, 157, 0)));
        g.fill(new Rectangle2D.Double(cx - shrineW * 0.62, shrineY, shrineW * 1.24, shrineH * 1.7));

        Polygon roof = new Polygon();
        roof.addPoint((int) (cx - shrineW * 0.64), (int) (shrineY + 72 * scale));
        roof.addPoint((int) (cx - shrineW * 0.42), (int) (shrineY + 16 * scale));
        roof.addPoint((int) (cx + shrineW * 0.42), (int) (shrineY + 16 * scale));
        roof.addPoint((int) (cx + shrineW * 0.64), (int) (shrineY + 72 * scale));
        roof.addPoint((int) (cx + shrineW * 0.50), (int) (shrineY + 92 * scale));
        roof.addPoint((int) (cx - shrineW * 0.50), (int) (shrineY + 92 * scale));
        g.setComposite(AlphaComposite.SrcOver.derive(0.72f));
        g.setPaint(new GradientPaint((float) cx, (float) shrineY, new Color(24, 31, 49),
                (float) cx, (float) (shrineY + 96 * scale), new Color(7, 14, 28)));
        g.fillPolygon(roof);
        g.setColor(new Color(153, 52, 61, 185));
        g.fill(new Rectangle2D.Double(cx - shrineW * 0.38, shrineY + 88 * scale, shrineW * 0.76, shrineH * 0.54));
        g.setColor(new Color(255, 225, 150, 108));
        g.fill(new Rectangle2D.Double(cx - shrineW * 0.11, shrineY + 101 * scale, shrineW * 0.22, shrineH * 0.42));

        double toriiY = height * 0.34;
        double postTop = toriiY + 60 * scale;
        double postBottom = height * 0.66;
        g.setComposite(AlphaComposite.SrcOver.derive(0.68f));
        g.setColor(new Color(56, 12, 20, 185));
        g.fillRoundRect((int) (cx - 243 * scale), (int) (postTop + 6 * scale), (int) (35 * scale), (int) (postBottom - postTop), 8, 8);
        g.fillRoundRect((int) (cx + 208 * scale), (int) (postTop + 6 * scale), (int) (35 * scale), (int) (postBottom - postTop), 8, 8);
        g.setPaint(new GradientPaint((float) (cx - 235 * scale), (float) postTop, new Color(222, 50, 58),
                (float) (cx - 210 * scale), (float) postBottom, new Color(103, 22, 34)));
        g.fillRoundRect((int) (cx - 248 * scale), (int) postTop, (int) (32 * scale), (int) (postBottom - postTop), 8, 8);
        g.setPaint(new GradientPaint((float) (cx + 220 * scale), (float) postTop, new Color(238, 62, 67),
                (float) (cx + 244 * scale), (float) postBottom, new Color(113, 24, 36)));
        g.fillRoundRect((int) (cx + 216 * scale), (int) postTop, (int) (32 * scale), (int) (postBottom - postTop), 8, 8);

        g.setPaint(new GradientPaint((float) (cx - 306 * scale), (float) toriiY, new Color(249, 73, 75),
                (float) (cx + 306 * scale), (float) (toriiY + 35 * scale), new Color(117, 25, 39)));
        g.fillRoundRect((int) (cx - 314 * scale), (int) toriiY, (int) (628 * scale), (int) (34 * scale), 12, 12);
        g.setColor(new Color(49, 12, 24, 206));
        g.fillRoundRect((int) (cx - 288 * scale), (int) (toriiY + 35 * scale), (int) (576 * scale), (int) (22 * scale), 8, 8);
        g.setColor(new Color(246, 86, 80, 222));
        g.fillRoundRect((int) (cx - 226 * scale), (int) (toriiY + 61 * scale), (int) (452 * scale), (int) (22 * scale), 8, 8);

        g.setStroke(new BasicStroke((float) (2.2 * scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(255, 177, 125, 152));
        g.draw(new Line2D.Double(cx - 296 * scale, toriiY + 6 * scale, cx + 296 * scale, toriiY + 6 * scale));
        g.draw(new Line2D.Double(cx - 218 * scale, toriiY + 66 * scale, cx + 218 * scale, toriiY + 66 * scale));

        g.setStroke(oldStroke);
        g.setPaint(oldPaint);
        g.setComposite(oldComposite);
    }

    private void drawStonePath(Graphics2D g, int width, int height, double scale) {
        Composite oldComposite = g.getComposite();
        Paint oldPaint = g.getPaint();
        java.awt.Stroke oldStroke = g.getStroke();

        double topY = height * 0.55;
        double bottomY = height * 1.03;
        double cx = width * 0.50;
        Path2D path = new Path2D.Double();
        path.moveTo(cx - 128 * scale, topY);
        path.lineTo(cx + 128 * scale, topY);
        path.lineTo(width * 0.72, bottomY);
        path.lineTo(width * 0.28, bottomY);
        path.closePath();

        g.setComposite(AlphaComposite.SrcOver.derive(0.72f));
        g.setPaint(new GradientPaint((float) cx, (float) topY, new Color(52, 90, 110, 138),
                (float) cx, (float) bottomY, new Color(11, 31, 52, 236)));
        g.fill(path);

        g.setStroke(new BasicStroke((float) (1.4 * scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 10; i++) {
            double t = i / 9.0;
            double y = topY + Math.pow(t, 1.6) * (bottomY - topY);
            double half = (128 + 158 * Math.pow(t, 1.25)) * scale;
            g.setColor(new Color(185, 226, 238, 32 + i * 8));
            g.draw(new Line2D.Double(cx - half, y, cx + half, y));
        }

        g.setComposite(AlphaComposite.SrcOver.derive(0.20f));
        g.setStroke(new BasicStroke((float) (2.5 * scale)));
        for (int i = 0; i < 9; i++) {
            double offset = (i - 4) * 38 * scale;
            g.setColor(i % 2 == 0 ? new Color(255, 210, 230) : new Color(155, 232, 255));
            g.draw(new Line2D.Double(cx + offset * 0.28, topY + 8 * scale, cx + offset, bottomY));
        }

        g.setStroke(oldStroke);
        g.setPaint(oldPaint);
        g.setComposite(oldComposite);
    }

    private void drawLanterns(Graphics2D g, int width, int height, double scale) {
        Composite oldComposite = g.getComposite();
        Paint oldPaint = g.getPaint();
        java.awt.Stroke oldStroke = g.getStroke();

        double[][] points = {
                {0.30, 0.61, 0.75},
                {0.70, 0.61, 0.75},
                {0.20, 0.76, 1.0},
                {0.80, 0.76, 1.0}
        };
        for (double[] point : points) {
            double x = width * point[0];
            double y = height * point[1];
            double s = scale * point[2];
            g.setComposite(AlphaComposite.SrcOver.derive(0.30f));
            g.setColor(new Color(255, 190, 108));
            g.fill(new Ellipse2D.Double(x - 42 * s, y - 44 * s, 84 * s, 88 * s));

            g.setComposite(AlphaComposite.SrcOver.derive(0.72f));
            g.setColor(new Color(24, 28, 38));
            g.fill(new Rectangle2D.Double(x - 13 * s, y + 22 * s, 26 * s, 42 * s));
            g.fill(new Rectangle2D.Double(x - 22 * s, y + 58 * s, 44 * s, 9 * s));
            g.setColor(new Color(235, 184, 102));
            g.fill(new Ellipse2D.Double(x - 19 * s, y - 5 * s, 38 * s, 34 * s));
            g.setColor(new Color(255, 231, 160, 175));
            g.fill(new Ellipse2D.Double(x - 10 * s, y + 1 * s, 20 * s, 20 * s));
        }

        g.setStroke(oldStroke);
        g.setPaint(oldPaint);
        g.setComposite(oldComposite);
    }

    private void drawMist(Graphics2D g, int width, int height, double time, double scale) {
        Composite oldComposite = g.getComposite();
        g.setStroke(new BasicStroke((float) Math.max(1.2, 2.3 * scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 9; i++) {
            double y = height * (0.12 + i * 0.085);
            double drift = Math.sin(time * 0.28 + i * 1.7) * 42 * scale;
            double arcW = width * (0.65 + i * 0.04);
            double arcH = height * (0.23 + i * 0.03);
            g.setComposite(AlphaComposite.SrcOver.derive((float) (0.06 + i * 0.008)));
            g.setColor(i % 2 == 0 ? new Color(140, 240, 255) : new Color(255, 198, 232));
            g.draw(new Arc2D.Double(-width * 0.18 + drift, y, arcW, arcH, 5, 170, Arc2D.OPEN));
        }
        g.setComposite(oldComposite);
    }

    private void drawSpellCircles(Graphics2D g, int width, int height, double time, double scale) {
        Composite oldComposite = g.getComposite();
        double centerX = width * 0.54 + Math.sin(time * 0.17) * 16 * scale;
        double centerY = height * 0.56 + Math.cos(time * 0.13) * 12 * scale;

        g.setStroke(new BasicStroke((float) Math.max(1.0, 2.0 * scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 5; i++) {
            double radius = (140 + i * 58) * scale;
            float alpha = (float) (0.18 - i * 0.022);
            g.setComposite(AlphaComposite.SrcOver.derive(Math.max(0.04f, alpha)));
            g.setColor(i % 2 == 0 ? new Color(130, 236, 255) : new Color(251, 172, 229));
            g.draw(new Ellipse2D.Double(centerX - radius, centerY - radius * 0.74, radius * 2, radius * 1.48));
        }

        for (int ring = 0; ring < 3; ring++) {
            int bullets = 34 + ring * 8;
            double radius = (150 + ring * 85) * scale;
            for (int i = 0; i < bullets; i++) {
                double angle = (i / (double) bullets) * Math.PI * 2 + time * (0.12 + ring * 0.035);
                double x = centerX + Math.cos(angle) * radius;
                double y = centerY + Math.sin(angle) * radius * 0.64;
                float pulse = (float) (0.45 + 0.25 * Math.sin(time * 2.1 + i * 0.4));
                Color color = ring % 2 == 0 ? new Color(140, 240, 255) : new Color(255, 154, 219);
                drawGlowDot(g, x, y, 3.2 * scale, color, pulse * 0.55f);
            }
        }
        g.setComposite(oldComposite);
    }

    private void drawTitleFan(Graphics2D g, int width, int height, double time, double scale) {
        Composite oldComposite = g.getComposite();
        double cx = width * 0.52;
        double cy = height * 0.18;
        double radius = 230 * scale;

        g.setComposite(AlphaComposite.SrcOver.derive(0.42f));
        g.setPaint(new java.awt.RadialGradientPaint(
                new Point2D.Double(cx, cy),
                (float) radius,
                new float[]{0f, 0.64f, 1f},
                new Color[]{new Color(58, 178, 236, 210), new Color(34, 47, 132, 150), new Color(14, 20, 80, 0)}));
        g.fill(new Arc2D.Double(cx - radius, cy - radius, radius * 2, radius * 2, -6, 192, Arc2D.PIE));

        g.setComposite(AlphaComposite.SrcOver.derive(0.2f));
        g.setStroke(new BasicStroke((float) (2.0 * scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(245, 249, 255));
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(-2 + i * 16.5 + Math.sin(time * 0.4) * 1.5);
            double x = cx + Math.cos(angle) * radius * 0.95;
            double y = cy - Math.sin(angle) * radius * 0.55;
            g.draw(new Line2D.Double(cx, cy, x, y));
        }
        g.setComposite(oldComposite);
    }

    private void drawCharacters(Graphics2D g, int width, int height, double time, double scale) {
        int redPose = poseIndex(0, redHeroes.length);
        int witchPose = poseIndex(1, witchHeroes.length);
        BufferedImage redHero = redHeroes[redPose];
        BufferedImage witchHero = witchHeroes[witchPose];
        BufferedImage redHeroShadow = redHeroShadows[redPose];
        BufferedImage witchHeroShadow = witchHeroShadows[witchPose];

        double heroHeight = Math.min(height * 0.75, 540 * scale);
        double bob = Math.sin(time * 1.3) * 4 * scale;

        double redWidth = heroHeight * redHero.getWidth() / redHero.getHeight();
        double redX = 58 * scale;
        double redY = height - heroHeight - 10 * scale + bob;
        drawCharacterAura(g, redX + redWidth * 0.50, redY + heroHeight * 0.52, 215 * scale,
                new Color(255, 65, 92), new Color(255, 205, 226));
        drawCharacter(g, redHeroShadow, redHero, redX, redY, redWidth, heroHeight, false);
        drawCharacterPlaque(g, redX + 34 * scale, redY + heroHeight - 86 * scale,
                "Shrine Maiden", new Color(238, 59, 74), scale);

        double witchHeight = heroHeight * 1.02;
        double witchWidth = witchHeight * witchHero.getWidth() / witchHero.getHeight();
        double witchX = width - witchWidth - 58 * scale;
        double witchY = height - witchHeight - 8 * scale - bob;
        drawCharacterAura(g, witchX + witchWidth * 0.50, witchY + witchHeight * 0.52, 230 * scale,
                new Color(255, 204, 91), new Color(255, 250, 194));
        drawCharacter(g, witchHeroShadow, witchHero, witchX, witchY, witchWidth, witchHeight, true);
        drawCharacterPlaque(g, witchX + witchWidth - 178 * scale, witchY + witchHeight - 86 * scale,
                "Ordinary Magician", new Color(246, 196, 76), scale);
    }

    private int poseIndex(int offset, int count) {
        if (count <= 1) {
            return 0;
        }
        int period = 210;
        return (tick / period + offset) % count;
    }

    private void drawCharacterAura(Graphics2D g, double cx, double cy, double radius, Color inner, Color outer) {
        Composite oldComposite = g.getComposite();
        Paint oldPaint = g.getPaint();
        java.awt.Stroke oldStroke = g.getStroke();

        g.setComposite(AlphaComposite.SrcOver.derive(0.40f));
        g.setPaint(new java.awt.RadialGradientPaint(
                new Point2D.Double(cx, cy),
                (float) radius,
                new float[]{0f, 0.44f, 1f},
                new Color[]{new Color(inner.getRed(), inner.getGreen(), inner.getBlue(), 122),
                        new Color(outer.getRed(), outer.getGreen(), outer.getBlue(), 48),
                        new Color(0, 0, 0, 0)}));
        g.fill(new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2));

        g.setComposite(AlphaComposite.SrcOver.derive(0.30f));
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(outer);
        for (int i = 0; i < 4; i++) {
            double r = radius * (0.52 + i * 0.11);
            g.draw(new Ellipse2D.Double(cx - r, cy - r * 0.86, r * 2, r * 1.72));
        }

        g.setStroke(oldStroke);
        g.setPaint(oldPaint);
        g.setComposite(oldComposite);
    }

    private void drawCharacterPlaque(Graphics2D g, double x, double y, String label, Color accent, double scale) {
        Composite oldComposite = g.getComposite();
        Paint oldPaint = g.getPaint();
        java.awt.Stroke oldStroke = g.getStroke();

        double w = 168 * scale;
        double h = 33 * scale;
        g.setComposite(AlphaComposite.SrcOver.derive(0.58f));
        g.setPaint(new GradientPaint((float) x, (float) y, new Color(13, 18, 35, 220),
                (float) (x + w), (float) y, new Color(13, 18, 35, 38)));
        g.fill(new Rectangle2D.Double(x, y, w, h));
        g.setStroke(new BasicStroke((float) (1.2 * scale)));
        g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 190));
        g.draw(new Line2D.Double(x, y + h, x + w * 0.72, y + h));

        Font plaqueFont = chooseFont(Font.PLAIN, (float) clamp(15 * scale, 11, 15),
                "Georgia", "Times New Roman", "Serif");
        g.setFont(plaqueFont);
        g.setColor(new Color(252, 239, 226, 220));
        g.drawString(label, (float) (x + 12 * scale), (float) (y + 22 * scale));

        g.setStroke(oldStroke);
        g.setPaint(oldPaint);
        g.setComposite(oldComposite);
    }

    private void drawCharacter(Graphics2D g, BufferedImage shadow, BufferedImage image,
                               double x, double y, double width, double height, boolean leanLeft) {
        Object oldInterpolation = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        Composite oldComposite = g.getComposite();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setComposite(AlphaComposite.SrcOver.derive(0.38f));
        int shadowShift = leanLeft ? -18 : 18;
        g.drawImage(shadow, (int) Math.round(x + shadowShift), (int) Math.round(y + 24),
                (int) Math.round(width), (int) Math.round(height), null);

        g.setComposite(AlphaComposite.SrcOver);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(image, (int) Math.round(x), (int) Math.round(y), (int) Math.round(width),
                (int) Math.round(height), null);

        if (oldInterpolation != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterpolation);
        }
        g.setComposite(oldComposite);
    }

    private void drawDanmaku(Graphics2D g, int width, int height, double time, double scale) {
        Composite oldComposite = g.getComposite();
        double originX = width * 0.50;
        double originY = height * 0.48;

        for (int arm = 0; arm < 6; arm++) {
            Color color = arm % 2 == 0 ? new Color(105, 231, 255) : new Color(255, 126, 217);
            for (int i = 0; i < 28; i++) {
                double angle = arm * Math.PI / 3 + i * 0.18 + time * 0.9;
                double radius = (28 + i * 15) * scale;
                double x = originX + Math.cos(angle) * radius;
                double y = originY + Math.sin(angle) * radius * 0.72;
                float alpha = (float) Math.max(0, 0.38 - i * 0.008);
                drawGlowDot(g, x, y, (4.5 + i * 0.04) * scale, color, alpha);
            }
        }
        g.setComposite(oldComposite);
    }

    private void drawPetals(Graphics2D g, int width, int height, double time, double scale) {
        Composite oldComposite = g.getComposite();
        for (Petal petal : petals) {
            double progressY = (petal.startY + time * petal.speed) % (height + 130 * scale);
            double y = progressY - 70 * scale;
            double x = (petal.startX * width + Math.sin(time * petal.sway + petal.phase) * petal.drift * scale)
                    % (width + 80 * scale) - 40 * scale;
            double angle = petal.phase + time * petal.spin;
            float alpha = (float) (0.45 + 0.25 * Math.sin(time * 1.2 + petal.phase));

            g.setComposite(AlphaComposite.SrcOver.derive(Math.max(0.18f, Math.min(0.82f, alpha))));
            drawPetalShape(g, x, y, petal.size * scale, angle, petal.color);
        }
        g.setComposite(oldComposite);
    }

    private void drawTitle(Graphics2D g, int width, int height, double time, double scale) {
        float targetSize = (float) clamp(52 * scale, 34, 58);
        Font titleFont = chooseFont(Font.BOLD, targetSize, "Georgia", "Times New Roman", "Segoe UI", "Serif");
        titleFont = fitFont(g, titleFont, TITLE, width * 0.76);

        FontMetrics metrics = g.getFontMetrics(titleFont);
        float x = (float) ((width - metrics.stringWidth(TITLE)) * 0.5);
        float y = (float) (height * 0.145);

        Composite oldComposite = g.getComposite();
        Paint oldPaint = g.getPaint();
        g.setComposite(AlphaComposite.SrcOver.derive(0.38f));
        g.setPaint(new GradientPaint((float) (width * 0.23), y - 70 * (float) scale, new Color(4, 12, 28, 0),
                (float) (width * 0.50), y - 70 * (float) scale, new Color(4, 12, 28, 188)));
        g.fill(new Rectangle2D.Double(width * 0.18, y - 68 * scale, width * 0.32, 96 * scale));
        g.setPaint(new GradientPaint((float) (width * 0.50), y - 70 * (float) scale, new Color(4, 12, 28, 188),
                (float) (width * 0.77), y - 70 * (float) scale, new Color(4, 12, 28, 0)));
        g.fill(new Rectangle2D.Double(width * 0.50, y - 68 * scale, width * 0.32, 96 * scale));
        g.setPaint(oldPaint);
        g.setComposite(oldComposite);

        drawGlowingText(g, TITLE, titleFont, x, y, new Color(255, 255, 255),
                new Color(92, 36, 64), new Color(255, 171, 224));

        Font subFont = chooseFont(Font.PLAIN, (float) clamp(24 * scale, 16, 28),
                "Georgia", "Times New Roman", "Serif");
        String spacedSubtitle = "S a k u r a   S h r i n e   G a t e";
        FontMetrics subMetrics = g.getFontMetrics(subFont);
        float subX = (float) ((width - subMetrics.stringWidth(spacedSubtitle)) * 0.5);
        float subY = y + (float) (36 * scale);
        g.setFont(subFont);
        g.setColor(new Color(28, 20, 33, 190));
        g.drawString(spacedSubtitle, subX + 2, subY + 2);
        g.setColor(new Color(242, 231, 216, 218));
        g.drawString(spacedSubtitle, subX, subY);
    }

    private void drawMenu(Graphics2D g, int width, int height, double time, double scale) {
        double panelW = 360 * scale;
        double panelX = width * 0.50 - panelW * 0.50;
        double panelY = height * 0.315;
        float lineGap = (float) clamp(44 * scale, 35, 47);
        double panelH = lineGap * menu.length + 34 * scale;
        double menuCenterX = panelX + panelW * 0.50;
        Font menuFont = chooseFont(Font.BOLD, (float) clamp(25 * scale, 19, 28),
                "Georgia", "Times New Roman", "Segoe UI", "Serif");
        Font hintFont = chooseFont(Font.PLAIN, (float) clamp(12 * scale, 10, 13),
                "Segoe UI", "Georgia", "SansSerif");

        FontMetrics menuMetrics = g.getFontMetrics(menuFont);
        FontMetrics hintMetrics = g.getFontMetrics(hintFont);
        Composite oldComposite = g.getComposite();
        Paint oldPaint = g.getPaint();
        java.awt.Stroke oldStroke = g.getStroke();
        g.setComposite(AlphaComposite.SrcOver.derive(0.56f));
        g.setPaint(new GradientPaint((float) panelX, (float) panelY, new Color(14, 22, 36, 214),
                (float) (panelX + panelW), (float) (panelY + panelH), new Color(62, 32, 38, 92)));
        g.fillRoundRect((int) panelX, (int) panelY, (int) panelW, (int) panelH, (int) (8 * scale), (int) (8 * scale));
        g.setComposite(AlphaComposite.SrcOver.derive(0.70f));
        g.setStroke(new BasicStroke((float) (1.2 * scale)));
        g.setColor(new Color(255, 233, 194, 148));
        g.drawRoundRect((int) panelX, (int) panelY, (int) panelW, (int) panelH, (int) (8 * scale), (int) (8 * scale));
        g.setColor(new Color(255, 174, 207, 120));
        g.draw(new Line2D.Double(panelX + 22 * scale, panelY + 16 * scale,
                panelX + panelW - 22 * scale, panelY + 16 * scale));
        g.draw(new Line2D.Double(panelX + 22 * scale, panelY + panelH - 14 * scale,
                panelX + panelW - 22 * scale, panelY + panelH - 14 * scale));
        g.setStroke(oldStroke);
        g.setPaint(oldPaint);
        g.setComposite(oldComposite);

        for (int i = 0; i < menu.length; i++) {
            float baseline = (float) (panelY + 45 * scale + i * lineGap);
            int labelWidth = menuMetrics.stringWidth(menu[i].label);
            int hintWidth = hintMetrics.stringWidth(menu[i].hint);
            double boundsX = panelX + 28 * scale;
            menuBounds[i] = new Rectangle2D.Double(boundsX, baseline - menuMetrics.getAscent() - 5 * scale,
                    panelW - 56 * scale, lineGap * 0.88);

            boolean selected = i == selectedMenu;
            if (selected) {
                drawMenuSelection(g, menuBounds[i], time, scale);
            }

            float textX = (float) (menuCenterX - labelWidth * 0.50 + (selected ? 8 * scale : 0));
            Color glow = selected ? new Color(255, 224, 137) : new Color(255, 235, 205);
            Color edge = selected ? new Color(86, 28, 38) : new Color(28, 32, 42);
            Color fill = selected ? new Color(255, 249, 206) : new Color(244, 245, 238);
            drawSmallGlowText(g, menu[i].label, menuFont, textX, baseline, fill, edge, glow, selected);

            g.setFont(hintFont);
            g.setColor(selected ? new Color(255, 225, 236, 228) : new Color(231, 238, 246, 158));
            g.drawString(menu[i].hint, (float) (menuCenterX - hintWidth * 0.50),
                    baseline + 16 * (float) scale);
        }
    }

    private void drawMenuSelection(Graphics2D g, Rectangle2D bounds, double time, double scale) {
        Composite oldComposite = g.getComposite();
        Paint oldPaint = g.getPaint();
        double pulse = 0.55 + 0.25 * Math.sin(time * 4.4);

        g.setComposite(AlphaComposite.SrcOver.derive((float) (0.18 + pulse * 0.12)));
        g.setPaint(new GradientPaint((float) bounds.getX(), 0, new Color(63, 218, 255, 140),
                (float) bounds.getMaxX(), 0, new Color(255, 118, 226, 0)));
        g.fill(new Rectangle2D.Double(bounds.getX() - 8 * scale, bounds.getY() + bounds.getHeight() * 0.50,
                bounds.getWidth(), 3.5 * scale));

        double dotX = bounds.getX() + 6 * scale;
        double dotY = bounds.getY() + bounds.getHeight() * 0.52;
        drawGlowDot(g, dotX, dotY, 7 * scale, new Color(255, 168, 233), 0.72f);
        drawGlowDot(g, dotX + 18 * scale, dotY, 3.5 * scale, new Color(118, 237, 255), 0.65f);

        g.setPaint(oldPaint);
        g.setComposite(oldComposite);
    }

    private void drawCornerHint(Graphics2D g, int width, int height, double scale) {
        Font hintFont = chooseFont(Font.PLAIN, (float) clamp(13 * scale, 10, 14),
                "Microsoft YaHei UI", "Microsoft YaHei", "Segoe UI", "SansSerif");
        g.setFont(hintFont);
        g.setColor(new Color(242, 248, 255, 150));
        String hint = "↑↓ / Enter";
        FontMetrics metrics = g.getFontMetrics(hintFont);
        g.drawString(hint, width - metrics.stringWidth(hint) - (int) (24 * scale), height - (int) (20 * scale));
    }

    private void drawPetalShape(Graphics2D g, double x, double y, double size, double angle, Color color) {
        Graphics2D copy = (Graphics2D) g.create();
        copy.translate(x, y);
        copy.rotate(angle);
        copy.scale(size, size);

        Path2D petal = new Path2D.Double();
        petal.moveTo(0, -1.0);
        petal.curveTo(0.92, -0.55, 0.86, 0.54, 0.0, 1.04);
        petal.curveTo(-0.82, 0.52, -0.92, -0.54, 0, -1.0);
        petal.closePath();

        copy.setColor(color);
        copy.fill(petal);
        copy.setColor(new Color(255, 255, 255, 115));
        copy.setStroke(new BasicStroke(0.12f));
        copy.draw(petal);
        copy.dispose();
    }

    private void drawGlowDot(Graphics2D g, double x, double y, double radius, Color color, float alpha) {
        Composite oldComposite = g.getComposite();
        for (int i = 3; i >= 1; i--) {
            float glowAlpha = alpha * (0.11f / i);
            g.setComposite(AlphaComposite.SrcOver.derive(Math.max(0, Math.min(1, glowAlpha))));
            g.setColor(color);
            double r = radius * (i * 2.2);
            g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
        }
        g.setComposite(AlphaComposite.SrcOver.derive(Math.max(0, Math.min(1, alpha))));
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 230));
        g.fill(new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2));
        g.setComposite(oldComposite);
    }

    private void drawGlowingText(Graphics2D g, String text, Font font, float x, float y,
                                 Color fill, Color edge, Color glow) {
        FontRenderContext context = g.getFontRenderContext();
        GlyphVector vector = font.createGlyphVector(context, text);
        Shape outline = vector.getOutline(x, y);
        Composite oldComposite = g.getComposite();
        java.awt.Stroke oldStroke = g.getStroke();

        for (int i = 9; i >= 1; i--) {
            g.setComposite(AlphaComposite.SrcOver.derive(0.05f + i * 0.009f));
            g.setStroke(new BasicStroke(i * 3.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(glow);
            g.draw(outline);
        }

        g.setComposite(AlphaComposite.SrcOver.derive(0.95f));
        g.setStroke(new BasicStroke(5.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(edge);
        g.draw(outline);
        g.setColor(fill);
        g.fill(outline);

        g.setComposite(oldComposite);
        g.setStroke(oldStroke);
    }

    private void drawSmallGlowText(Graphics2D g, String text, Font font, float x, float y,
                                   Color fill, Color edge, Color glow, boolean selected) {
        FontRenderContext context = g.getFontRenderContext();
        GlyphVector vector = font.createGlyphVector(context, text);
        Shape outline = vector.getOutline(x, y);
        Composite oldComposite = g.getComposite();
        java.awt.Stroke oldStroke = g.getStroke();

        int glowPasses = selected ? 5 : 3;
        for (int i = glowPasses; i >= 1; i--) {
            g.setComposite(AlphaComposite.SrcOver.derive(selected ? 0.08f : 0.05f));
            g.setStroke(new BasicStroke(i * 2.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(glow);
            g.draw(outline);
        }

        g.setComposite(AlphaComposite.SrcOver.derive(0.92f));
        g.setStroke(new BasicStroke(3.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(edge);
        g.draw(outline);
        g.setColor(fill);
        g.fill(outline);

        g.setComposite(oldComposite);
        g.setStroke(oldStroke);
    }

    private int hitMenu(int mouseX, int mouseY) {
        if (startCameraTick >= 0) {
            return -1;
        }
        for (int i = 0; i < menuBounds.length; i++) {
            if (menuBounds[i] != null && menuBounds[i].contains(mouseX, mouseY)) {
                return i;
            }
        }
        return -1;
    }

    private void activateMenu() {
        if (selectedMenu == 0) {
            if (startCameraTick < 0) {
                startCameraTick = tick;
            }
            return;
        }
        if (selectedMenu == menu.length - 1) {
            System.exit(0);
        }
        JOptionPane.showMessageDialog(this,
                "Selected: " + menu[selectedMenu].label + "\nThis can be connected to the real game screen later.",
                TITLE,
                JOptionPane.INFORMATION_MESSAGE);
    }

    private BufferedImage[] loadPoseSet(String[] fileNames, Color fallbackColor) {
        BufferedImage[] images = new BufferedImage[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            images[i] = toCompatibleImage(trimTransparent(removeEdgePaper(loadAsset(fileNames[i], fallbackColor))));
        }
        return images;
    }

    private BufferedImage[] createShadows(BufferedImage[] sources) {
        BufferedImage[] shadows = new BufferedImage[sources.length];
        for (int i = 0; i < sources.length; i++) {
            shadows[i] = createShadow(sources[i]);
        }
        return shadows;
    }

    private BufferedImage trimTransparent(BufferedImage source) {
        int minX = source.getWidth();
        int minY = source.getHeight();
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int alpha = (source.getRGB(x, y) >>> 24) & 0xFF;
                if (alpha > 12) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            return source;
        }

        int padding = 10;
        minX = Math.max(0, minX - padding);
        minY = Math.max(0, minY - padding);
        maxX = Math.min(source.getWidth() - 1, maxX + padding);
        maxY = Math.min(source.getHeight() - 1, maxY + padding);

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = output.createGraphics();
        g.drawImage(source, 0, 0, width, height, minX, minY, maxX + 1, maxY + 1, null);
        g.dispose();
        return output;
    }

    private BufferedImage loadAsset(String fileName, Color fallbackColor) {
        List<File> candidates = new ArrayList<>();
        candidates.add(new File("assets", fileName));
        candidates.add(new File("DanmakuFestivalCover/assets", fileName));
        candidates.add(new File(new File(System.getProperty("user.dir")).getParentFile(), "assets/" + fileName));

        for (File candidate : candidates) {
            if (candidate != null && candidate.isFile()) {
                try {
                    return toCompatibleImage(ImageIO.read(candidate));
                } catch (IOException ignored) {
                    // Try the next location.
                }
            }
        }

        try (InputStream stream = CoverPanel.class.getResourceAsStream("/assets/" + fileName)) {
            if (stream != null) {
                return toCompatibleImage(ImageIO.read(stream));
            }
        } catch (IOException ignored) {
            // Fall through to a generated placeholder.
        }

        return toCompatibleImage(createPlaceholder(fallbackColor));
    }

    private BufferedImage toCompatibleImage(BufferedImage source) {
        if (source == null || GraphicsEnvironment.isHeadless()) {
            return source;
        }

        try {
            GraphicsConfiguration configuration = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration();
            BufferedImage compatible = configuration.createCompatibleImage(
                    source.getWidth(),
                    source.getHeight(),
                    source.getTransparency());
            Graphics2D g = compatible.createGraphics();
            g.setComposite(AlphaComposite.Src);
            g.drawImage(source, 0, 0, null);
            g.dispose();
            return compatible;
        } catch (RuntimeException ignored) {
            return source;
        }
    }

    private BufferedImage createPlaceholder(Color color) {
        BufferedImage image = new BufferedImage(300, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(255, 255, 255, 0));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.setColor(color);
        g.fillOval(85, 32, 130, 130);
        g.fillRoundRect(65, 145, 170, 215, 28, 28);
        g.setColor(new Color(255, 255, 255, 220));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        g.drawString("ASSET", 104, 218);
        g.drawString("MISSING", 83, 252);
        g.dispose();
        return image;
    }

    private BufferedImage removeEdgePaper(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        boolean[][] background = new boolean[height][width];
        ArrayDeque<Point> queue = new ArrayDeque<>();

        for (int x = 0; x < width; x++) {
            addBackgroundSeed(source, background, queue, x, 0);
            addBackgroundSeed(source, background, queue, x, height - 1);
        }
        for (int y = 0; y < height; y++) {
            addBackgroundSeed(source, background, queue, 0, y);
            addBackgroundSeed(source, background, queue, width - 1, y);
        }

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};
        while (!queue.isEmpty()) {
            Point point = queue.removeFirst();
            for (int i = 0; i < dx.length; i++) {
                int nx = point.x + dx[i];
                int ny = point.y + dy[i];
                if (nx >= 0 && nx < width && ny >= 0 && ny < height && !background[ny][nx]
                        && isPaperLike(source.getRGB(nx, ny))) {
                    background[ny][nx] = true;
                    queue.addLast(new Point(nx, ny));
                }
            }
        }

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = source.getRGB(x, y);
                if (background[y][x]) {
                    output.setRGB(x, y, argb & 0x00FFFFFF);
                } else {
                    output.setRGB(x, y, argb);
                }
            }
        }
        return output;
    }

    private void addBackgroundSeed(BufferedImage source, boolean[][] background, ArrayDeque<Point> queue, int x, int y) {
        if (!background[y][x] && isPaperLike(source.getRGB(x, y))) {
            background[y][x] = true;
            queue.addLast(new Point(x, y));
        }
    }

    private boolean isPaperLike(int argb) {
        int alpha = (argb >>> 24) & 0xFF;
        if (alpha < 12) {
            return true;
        }
        int red = (argb >>> 16) & 0xFF;
        int green = (argb >>> 8) & 0xFF;
        int blue = argb & 0xFF;
        int max = Math.max(red, Math.max(green, blue));
        int min = Math.min(red, Math.min(green, blue));
        return red > 232 && green > 226 && blue > 214 && max - min < 44;
    }

    private BufferedImage createShadow(BufferedImage source) {
        BufferedImage shadow = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int alpha = (source.getRGB(x, y) >>> 24) & 0xFF;
                int shadowAlpha = Math.min(150, (int) (alpha * 0.52));
                shadow.setRGB(x, y, (shadowAlpha << 24) | 0x081124);
            }
        }
        return shadow;
    }

    private Font chooseFont(int style, float size, String... names) {
        for (String name : names) {
            if (availableFonts.contains(name)) {
                return new Font(name, style, 1).deriveFont(size);
            }
        }
        return new Font(Font.SERIF, style, 1).deriveFont(size);
    }

    private Font fitFont(Graphics2D g, Font font, String text, double maxWidth) {
        Font fitted = font;
        while (g.getFontMetrics(fitted).stringWidth(text) > maxWidth && fitted.getSize2D() > 34f) {
            fitted = fitted.deriveFont(fitted.getSize2D() - 2f);
        }
        return fitted;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class MenuEntry {
        final String label;
        final String hint;

        MenuEntry(String label, String hint) {
            this.label = label;
            this.hint = hint;
        }
    }

    private static final class Petal {
        final double startX;
        final double startY;
        final double size;
        final double speed;
        final double sway;
        final double drift;
        final double spin;
        final double phase;
        final Color color;

        Petal(Random random) {
            startX = random.nextDouble();
            startY = random.nextDouble() * 850;
            size = 5.0 + random.nextDouble() * 7.5;
            speed = 18.0 + random.nextDouble() * 42.0;
            sway = 0.8 + random.nextDouble() * 1.8;
            drift = 28.0 + random.nextDouble() * 62.0;
            spin = -1.4 + random.nextDouble() * 2.8;
            phase = random.nextDouble() * Math.PI * 2.0;

            if (random.nextDouble() < 0.72) {
                color = new Color(255, 158 + random.nextInt(58), 205 + random.nextInt(38), 215);
            } else {
                color = new Color(255, 248, 252, 210);
            }
        }
    }
}
