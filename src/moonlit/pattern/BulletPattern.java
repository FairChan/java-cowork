package moonlit.pattern;

import moonlit.engine.GameEngine;
import moonlit.model.Boss;

/**
 * Strategy interface for boss bullet patterns.
 */
public interface BulletPattern {
    void update(Boss boss, GameEngine engine, double deltaSeconds);

    default void reset() {
    }
}
