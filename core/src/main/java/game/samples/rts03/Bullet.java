package game.samples.rts03;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Bullet {
    private Vector2 position;
    private Vector2 velocity;
    private float speed;
    private float lifetime;
    private float maxLifetime;
    private boolean isAlive;
    private float size;

    private int damage;
    private boolean willHit; // Ob dieser Schuss treffen wird

    public Bullet(float x, float y, float angle, float speed, int damage, boolean willHit) {
        this.position = new Vector2(x, y);
        this.speed = speed;
        this.damage = damage;
        this.willHit = willHit;
        this.lifetime = 0f;
        this.maxLifetime = 3f; // 3 Sekunden maximal
        this.isAlive = true;
        this.size = 4f;

        // Berechne Velocity aus Winkel
        float angleRad = angle * MathUtils.degreesToRadians;
        this.velocity = new Vector2(
            MathUtils.cos(angleRad) * speed,
            MathUtils.sin(angleRad) * speed
        );
    }

    public void update(float deltaTime) {
        if (!isAlive) return;

        lifetime += deltaTime;

        // Bewege Bullet
        position.add(velocity.x * deltaTime, velocity.y * deltaTime);

        // Prüfe Lebensdauer
        if (lifetime >= maxLifetime) {
            isAlive = false;
        }
    }

    public void render(ShapeRenderer renderer) {
        if (!isAlive) return;

        // Farbe basierend auf Trefferwahrscheinlichkeit (für Debug)
        if (willHit) {
            renderer.setColor(1, 0.5f, 0, 1); // Orange für Treffer
        } else {
            renderer.setColor(1, 1, 0, 1); // Gelb für Fehlschuss
        }

        renderer.circle(position.x, position.y, size);
    }

    /**
     * Prüft ob diese Bullet das Ziel trifft
     */
    public boolean checkHit(PlayerShip target) {
        if (!isAlive || !willHit) return false;
        if (!target.isAlive()) return false;

        float distance = position.dst(target.getPosition());
        return distance < (size + target.getSize());
    }

    public void destroy() {
        isAlive = false;
    }

    // Getter
    public boolean isAlive() {
        return isAlive;
    }

    public Vector2 getPosition() {
        return position;
    }

    public int getDamage() {
        return damage;
    }

    public boolean willHit() {
        return willHit;
    }
}
