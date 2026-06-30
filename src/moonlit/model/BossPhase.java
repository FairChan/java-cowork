package moonlit.model;

import moonlit.pattern.BulletPattern;

/**
 * Formal boss phase metadata: name, kind, HP, timer, and bullet behavior.
 */
public class BossPhase {
    public enum Kind {
        NONSPELL,
        SPELL
    }

    private final String name;
    private final Kind kind;
    private final int hp;
    private final double timeLimitSeconds;
    private final BulletPattern pattern;

    public BossPhase(String name, Kind kind, int hp, double timeLimitSeconds, BulletPattern pattern) {
        this.name = name;
        this.kind = kind;
        this.hp = hp;
        this.timeLimitSeconds = timeLimitSeconds;
        this.pattern = pattern;
    }

    public String getName() {
        return name;
    }

    public Kind getKind() {
        return kind;
    }

    public int getHp() {
        return hp;
    }

    public double getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public BulletPattern getPattern() {
        return pattern;
    }
}
