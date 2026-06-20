package moonlit.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Lightweight particle effects for hits, bombs, and phase changes.
 */
public class ParticleSystem {
    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random(7);

    public void update(double deltaSeconds) {
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.life -= deltaSeconds;
            particle.x += particle.vx * deltaSeconds;
            particle.y += particle.vy * deltaSeconds;
            if (particle.life <= 0) {
                iterator.remove();
            }
        }
    }

    public void render(GraphicsContext graphics) {
        for (Particle particle : particles) {
            graphics.setGlobalAlpha(Math.max(0, particle.life / particle.maxLife));
            graphics.setFill(particle.color);
            graphics.fillOval(particle.x - particle.size / 2.0, particle.y - particle.size / 2.0,
                    particle.size, particle.size);
        }
        graphics.setGlobalAlpha(1.0);
    }

    public void spawnSpark(double x, double y, Color color) {
        for (int i = 0; i < 5; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 35 + random.nextDouble() * 80;
            particles.add(new Particle(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed,
                    0.25 + random.nextDouble() * 0.25, 3 + random.nextDouble() * 3, color));
        }
    }

    public void spawnBurst(double x, double y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 45 + random.nextDouble() * 170;
            particles.add(new Particle(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed,
                    0.45 + random.nextDouble() * 0.55, 3 + random.nextDouble() * 5, color));
        }
    }

    public void spawnRing(double x, double y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2 * i / count;
            double speed = 120;
            particles.add(new Particle(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed,
                    0.7, 4, color));
        }
    }


    public void spawnSweepColumn(double x, double topY, double bottomY, Color color) {
        for (int i = 0; i < 10; i++) {
            double y = topY + random.nextDouble() * (bottomY - topY);
            double vx = -80 + random.nextDouble() * 160;
            double vy = -35 + random.nextDouble() * 70;
            particles.add(new Particle(x + random.nextDouble() * 28 - 14, y, vx, vy,
                    0.18 + random.nextDouble() * 0.22, 4 + random.nextDouble() * 8, color));
        }
    }
    public void clear() {
        particles.clear();
    }

    private static final class Particle {
        private double x;
        private double y;
        private final double vx;
        private final double vy;
        private double life;
        private final double maxLife;
        private final double size;
        private final Color color;

        private Particle(double x, double y, double vx, double vy, double life, double size, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
            this.maxLife = life;
            this.size = size;
            this.color = color;
        }
    }
}
