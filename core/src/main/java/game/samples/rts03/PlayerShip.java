package game.samples.rts03;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class PlayerShip {
    private Vector2 position;
    private float rotation; // In Grad
    private float size;
    private float speed;

    private int maxHealth;
    private int currentHealth;

    private boolean isAlive;

    // Bewegung
    private Vector2 velocity;
    private float acceleration;
    private float maxSpeed;
    private float friction;
    private float turnSpeed;

    public PlayerShip(float x, float y) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2();
        this.rotation = 0f;
        this.size = 15f;

        this.maxHealth = 100;
        this.currentHealth = maxHealth;
        this.isAlive = true;

        // Bewegungs-Parameter
        this.acceleration = 400f;
        this.maxSpeed = 500f;
        this.friction = 0.92f;
        this.turnSpeed = 200f; // Grad pro Sekunde
    }

    public void update(float deltaTime) {
        if (!isAlive) return;

        // Position aktualisieren
        position.add(velocity.x * deltaTime, velocity.y * deltaTime);

        // Reibung anwenden
        velocity.scl(friction);

        // Kleine Geschwindigkeiten auf 0 setzen
        if (velocity.len() < 1f) {
            velocity.set(0, 0);
        }
    }

    public void moveForward(float deltaTime) {
        float angleRad = rotation * MathUtils.degreesToRadians;
        float dx = MathUtils.cos(angleRad) * acceleration * deltaTime;
        float dy = MathUtils.sin(angleRad) * acceleration * deltaTime;

        velocity.add(dx, dy);

        // Geschwindigkeit begrenzen
        if (velocity.len() > maxSpeed) {
            velocity.nor().scl(maxSpeed);
        }
    }

    public void moveBackward(float deltaTime) {
        float angleRad = rotation * MathUtils.degreesToRadians;
        float dx = MathUtils.cos(angleRad) * acceleration * deltaTime;
        float dy = MathUtils.sin(angleRad) * acceleration * deltaTime;

        velocity.sub(dx, dy);

        if (velocity.len() > maxSpeed) {
            velocity.nor().scl(maxSpeed);
        }
    }

    public void turnLeft(float deltaTime) {
        rotation += turnSpeed * deltaTime;
        normalizeRotation();
    }

    public void turnRight(float deltaTime) {
        rotation -= turnSpeed * deltaTime;
        normalizeRotation();
    }

    private void normalizeRotation() {
        while (rotation >= 360f) rotation -= 360f;
        while (rotation < 0f) rotation += 360f;
    }

    public void takeDamage(int damage) {
        if (!isAlive) return;

        currentHealth -= damage;

        if (currentHealth <= 0) {
            currentHealth = 0;
            isAlive = false;
            System.out.println("GAME OVER! Ship destroyed!");
        } else {
            System.out.println("Hit! Health: " + currentHealth + "/" + maxHealth);
        }
    }

    public void render(ShapeRenderer renderer) {
        if (!isAlive) return;

        // Farbe basierend auf Gesundheit
        float healthPercent = (float) currentHealth / maxHealth;
        Color color;

        if (healthPercent > 0.6f) {
            color = Color.GREEN;
        } else if (healthPercent > 0.3f) {
            color = Color.YELLOW;
        } else {
            color = Color.RED;
        }

        renderer.setColor(color);

        // Dreieck-Vertices
        float halfSize = size / 2f;

        // Spitze (vorne)
        float x1 = position.x + size;
        float y1 = position.y;

        // Linke hintere Ecke
        float x2 = position.x - halfSize;
        float y2 = position.y + halfSize;

        // Rechte hintere Ecke
        float x3 = position.x - halfSize;
        float y3 = position.y - halfSize;

        // Rotiere Punkte
        Vector2 p1 = rotatePoint(x1, y1, position.x, position.y, rotation);
        Vector2 p2 = rotatePoint(x2, y2, position.x, position.y, rotation);
        Vector2 p3 = rotatePoint(x3, y3, position.x, position.y, rotation);

        renderer.triangle(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);
    }

    private Vector2 rotatePoint(float x, float y, float centerX, float centerY, float angleDeg) {
        float angleRad = angleDeg * MathUtils.degreesToRadians;
        float cos = MathUtils.cos(angleRad);
        float sin = MathUtils.sin(angleRad);

        float translatedX = x - centerX;
        float translatedY = y - centerY;

        float rotatedX = translatedX * cos - translatedY * sin;
        float rotatedY = translatedX * sin + translatedY * cos;

        return new Vector2(rotatedX + centerX, rotatedY + centerY);
    }

    // Getter
    public Vector2 getPosition() {
        return position;
    }

    public float getRotation() {
        return rotation;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public float getSize() {
        return size;
    }
}

