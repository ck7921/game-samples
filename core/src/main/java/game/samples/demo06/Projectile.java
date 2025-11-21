package game.samples.demo06;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Projectile {
    private Vector2 position;
    private Vector2 velocity;
    private float speed;
    private float lifetime;
    private float maxLifetime;
    private boolean isAlive;
    private float size;

    // Wer hat geschossen?
    private TargetSeeker shooter;

    public Projectile(float x, float y, float angle, float speed, TargetSeeker shooter) {
        this.position = new Vector2(x, y);
        this.speed = speed;
        this.shooter = shooter;
        this.lifetime = 0f;
        this.maxLifetime = 2f; // 2 Sekunden maximal
        this.isAlive = true;
        this.size = 4f;

        // Berechne Geschwindigkeit aus Winkel
        float angleRad = (float) Math.toRadians(angle);
        this.velocity = new Vector2(
            (float) Math.cos(angleRad) * speed,
            (float) Math.sin(angleRad) * speed
        );
    }

    public void update(float deltaTime) {
        if (!isAlive) return;

        lifetime += deltaTime;

        // Bewege Projektil
        position.add(velocity.x * deltaTime, velocity.y * deltaTime);

        // Prüfe Lebensdauer
        if (lifetime >= maxLifetime) {
            isAlive = false;
        }
    }

    public void render(ShapeRenderer renderer) {
        if (!isAlive) return;

        // Zeichne Projektil als kleiner Kreis
        renderer.setColor(1, 1, 0, 1); // Gelb
        renderer.circle(position.x, position.y, size);
    }

    /**
     * Prüft ob dieses Projektil einen Seeker trifft
     */
    public boolean hits(TargetSeeker target) {
        if (!isAlive) return false;
        if (target.isDestroyed()) return false;
        if (target == shooter) return false; // Kann sich nicht selbst treffen

        float distance = position.dst(target.getPosition());
        return distance < (size + target.getCollisionRadius());
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

    public TargetSeeker getShooter() {
        return shooter;
    }
}
