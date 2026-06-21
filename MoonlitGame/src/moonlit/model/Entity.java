package moonlit.model;

/**
 * Game object with health and damage behavior.
 */
public abstract class Entity extends GameObject {
    protected int hp;
    protected int maxHp;

    protected Entity(double x, double y, double radius, int maxHp) {
        super(x, y, radius);
        this.maxHp = maxHp;
        this.hp = maxHp;
    }

    public void takeDamage(int damage) {
        hp = Math.max(0, hp - Math.max(0, damage));
        if (hp == 0) {
            alive = false;
        }
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }
}
