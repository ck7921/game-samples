package game.samples.rts03;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;


public class TurretTower {
    private Vector2 position;
    private float size;

    private float detectionRange;
    private float fireRange;
    private float barrelRotation;
    private float rotationSpeed;

    private float fireRate;
    private float fireCooldown;

    private float hitChance;

    private PlayerShip target;
    private boolean hasTarget;
    private float targetRotation;

    private Color baseColor;
    private Color barrelColor;

    // Für visuelle Effekte
    private float recoilTimer;
    private float recoilAmount;

    public TurretTower(float x, float y) {
        this.position = new Vector2(x, y);
        this.size = 25f;

        this.detectionRange = 300f;
        this.fireRange = 200f;
        this.rotationSpeed = 90f;

        this.fireRate = 1.5f;
        this.fireCooldown = 0f;

        this.hitChance = 0.75f;

        this.barrelRotation = 0f;
        this.hasTarget = false;

        this.baseColor = new Color(0.3f, 0.3f, 0.3f, 1f);
        this.barrelColor = new Color(0.5f, 0.5f, 0.5f, 1f);

        this.recoilTimer = 0f;
        this.recoilAmount = 0f;
    }

    public void update(float deltaTime, PlayerShip player) {
        // Update Cooldown
        if (fireCooldown > 0) {
            fireCooldown -= deltaTime;
        }

        // Update Recoil
        if (recoilTimer > 0) {
            recoilTimer -= deltaTime;
            recoilAmount = recoilTimer * 5f; // Rückstoß-Effekt
        } else {
            recoilAmount = 0f;
        }

        if (!player.isAlive()) {
            hasTarget = false;
            return;
        }

        float distanceToPlayer = position.dst(player.getPosition());

        if (distanceToPlayer <= detectionRange) {
            hasTarget = true;

            Vector2 toPlayer = new Vector2(player.getPosition()).sub(position);
            targetRotation = toPlayer.angleDeg();

            rotateBarrelToTarget(deltaTime);

            // Schieße nicht mehr direkt, sondern gebe ein Signal zurück
            // Die Bullet-Erstellung passiert jetzt im Hauptspiel
        } else {
            hasTarget = false;
        }
    }

    private void rotateBarrelToTarget(float deltaTime) {
        float diff = targetRotation - barrelRotation;

        while (diff > 180f) diff -= 360f;
        while (diff < -180f) diff += 360f;

        float rotationThisFrame = rotationSpeed * deltaTime;

        if (Math.abs(diff) < rotationThisFrame) {
            barrelRotation = targetRotation;
        } else {
            if (diff > 0) {
                barrelRotation += rotationThisFrame;
            } else {
                barrelRotation -= rotationThisFrame;
            }
        }

        while (barrelRotation >= 360f) barrelRotation -= 360f;
        while (barrelRotation < 0f) barrelRotation += 360f;
    }

    public boolean isAimedAtTarget() {
        if (!hasTarget) return false;

        float diff = targetRotation - barrelRotation;
        while (diff > 180f) diff -= 360f;
        while (diff < -180f) diff += 360f;

        return Math.abs(diff) < 5f;
    }

    public boolean canFire() {
        return fireCooldown <= 0;
    }

    public boolean isInFireRange(PlayerShip player) {
        return position.dst(player.getPosition()) <= fireRange;
    }

    /**
     * Erstellt eine Bullet
     */
    public Bullet fire(PlayerShip player) {
        if (!canFire() || !hasTarget) return null;

        fireCooldown = 1f / fireRate;
        recoilTimer = 0.1f;

        // Trefferchance prüfen
        boolean willHit = MathUtils.random() <= hitChance;

        // Schaden zwischen 7 und 11
        int damage = MathUtils.random(7, 11);

        // Spawn-Position: Am Ende der Kanone
        float barrelLength = size * 0.8f;
        float angleRad = barrelRotation * MathUtils.degreesToRadians;
        float spawnX = position.x + MathUtils.cos(angleRad) * barrelLength;
        float spawnY = position.y + MathUtils.sin(angleRad) * barrelLength;

        // Schusswinkel mit optionaler Abweichung
        float shootAngle = barrelRotation;
        if (!willHit) {
            // Fehlschuss - füge zufällige Abweichung hinzu
            shootAngle += MathUtils.random(-20f, 20f);
        }

        return new Bullet(spawnX, spawnY, shootAngle, 400f, damage, willHit);
    }

    public void render(ShapeRenderer renderer) {
        // Basis (Quadrat)
        renderer.setColor(baseColor);
        renderer.rect(
            position.x - size / 2f,
            position.y - size / 2f,
            size,
            size
        );

        // Kanone (mit Rückstoß)
        renderer.setColor(barrelColor);

        float barrelLength = size * 0.8f - recoilAmount;
        float barrelWidth = size * 0.3f;

        Vector2 barrelEnd = new Vector2();
        float angleRad = barrelRotation * MathUtils.degreesToRadians;
        barrelEnd.x = position.x + MathUtils.cos(angleRad) * barrelLength;
        barrelEnd.y = position.y + MathUtils.sin(angleRad) * barrelLength;

        renderer.rectLine(position.x, position.y, barrelEnd.x, barrelEnd.y, barrelWidth);

        // Zielpunkt (wenn aktiv)
        if (hasTarget) {
            renderer.setColor(1, 0, 0, 0.5f);
            renderer.circle(position.x, position.y, 5);
        }
    }

    public void renderRanges(ShapeRenderer renderer) {
        renderer.setColor(0.5f, 0.5f, 1f, 0.1f);
        renderer.circle(position.x, position.y, detectionRange);

        renderer.setColor(1f, 0.3f, 0.3f, 0.15f);
        renderer.circle(position.x, position.y, fireRange);
    }

    public void renderRangeOutlines(ShapeRenderer renderer) {
        renderer.setColor(0.5f, 0.5f, 1f, 0.3f);
        renderer.circle(position.x, position.y, detectionRange);

        renderer.setColor(1f, 0.3f, 0.3f, 0.5f);
        renderer.circle(position.x, position.y, fireRange);
    }

    // Getter
    public Vector2 getPosition() {
        return position;
    }

    public boolean hasTarget() {
        return hasTarget;
    }

    public float getBarrelRotation() {
        return barrelRotation;
    }
}
