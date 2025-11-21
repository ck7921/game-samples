package game.samples.demo06;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class TargetSeeker {
    private Vector2 position;
    private Vector2 target;
    private float rotation;
    private float targetRotation;

    private float size;
    private float moveSpeed;
    private float rotationSpeed;

    private float r, g, b, a;

    private enum State {
        ROTATING,
        MOVING
    }
    private State state;

    private float canvasWidth;
    private float canvasHeight;
    private float margin;

    private float collisionRadius;
    private boolean isDestroyed;

    // Waffen-System
    private float weaponRange; // Reichweite der Waffe
    private float weaponCooldown; // Zeit zwischen Schüssen
    private float weaponCooldownTimer;
    private float hitChance; // Trefferchance (0.0 - 1.0)

    public TargetSeeker(float canvasWidth, float canvasHeight, float size, float moveSpeed, float rotationSpeed) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.size = size;
        this.moveSpeed = moveSpeed;
        this.rotationSpeed = rotationSpeed;
        this.margin = size * 2;

        this.position = new Vector2();
        this.target = new Vector2();
        this.rotation = 0f;
        this.targetRotation = 0f;
        this.state = State.ROTATING;
        this.isDestroyed = false;

        this.collisionRadius = size * 0.8f;

        // Waffen-Einstellungen
        this.weaponRange = 150f; // 150 Pixel Reichweite
        this.weaponCooldown = 0.5f; // Alle 0.5 Sekunden schießen
        this.weaponCooldownTimer = 0f;
        this.hitChance = 0.75f; // 75% Trefferchance

        this.r = 0f;
        this.g = 1f;
        this.b = 0f;
        this.a = 1f;

        randomizePosition();
        generateNewTarget();
    }

    public void update(float deltaTime) {
        if (isDestroyed) return;

        // Update Waffen-Cooldown
        if (weaponCooldownTimer > 0) {
            weaponCooldownTimer -= deltaTime;
        }

        switch (state) {
            case ROTATING:
                updateRotation(deltaTime);
                break;
            case MOVING:
                updateMovement(deltaTime);
                break;
        }
    }

    private void updateRotation(float deltaTime) {
        float diff = targetRotation - rotation;

        while (diff > 180f) diff -= 360f;
        while (diff < -180f) diff += 360f;

        if (Math.abs(diff) < 1f) {
            rotation = targetRotation;
            state = State.MOVING;
            return;
        }

        float rotationThisFrame = rotationSpeed * deltaTime;

        if (Math.abs(diff) < rotationThisFrame) {
            rotation = targetRotation;
            state = State.MOVING;
        } else {
            if (diff > 0) {
                rotation += rotationThisFrame;
            } else {
                rotation -= rotationThisFrame;
            }
        }

        normalizeRotation();
    }

    private void updateMovement(float deltaTime) {
        Vector2 direction = new Vector2(target).sub(position);
        float distance = direction.len();

        if (distance < 2f) {
            position.set(target);
            generateNewTarget();
            return;
        }

        direction.nor();
        float moveDistance = moveSpeed * deltaTime;

        if (moveDistance > distance) {
            moveDistance = distance;
        }

        position.add(direction.x * moveDistance, direction.y * moveDistance);
    }

    private void generateNewTarget() {
        target.x = margin + MathUtils.random() * (canvasWidth - 2 * margin);
        target.y = margin + MathUtils.random() * (canvasHeight - 2 * margin);

        Vector2 direction = new Vector2(target).sub(position);
        targetRotation = direction.angleDeg();

        state = State.ROTATING;
    }

    private void randomizePosition() {
        position.x = margin + MathUtils.random() * (canvasWidth - 2 * margin);
        position.y = margin + MathUtils.random() * (canvasHeight - 2 * margin);
        rotation = MathUtils.random(360f);
    }

    private void normalizeRotation() {
        while (rotation >= 360f) rotation -= 360f;
        while (rotation < 0f) rotation += 360f;
    }

    /**
     * Prüft ob ein Feind in Schussreichweite ist
     */
    public TargetSeeker findEnemyInRange(java.util.List<TargetSeeker> allSeekers) {
        if (isDestroyed) return null;
        if (!canShoot()) return null;

        TargetSeeker closestEnemy = null;
        float closestDistance = Float.MAX_VALUE;

        for (TargetSeeker other : allSeekers) {
            if (other == this) continue;
            if (other.isDestroyed()) continue;

            float distance = this.position.dst(other.position);

            if (distance <= weaponRange && distance < closestDistance) {
                closestEnemy = other;
                closestDistance = distance;
            }
        }

        return closestEnemy;
    }

    /**
     * Versucht auf ein Ziel zu schießen
     * @return Projektil wenn geschossen wurde, sonst null
     */
    public Projectile tryShoot(TargetSeeker enemy) {
        if (!canShoot()) return null;
        if (enemy == null || enemy.isDestroyed()) return null;

        // Berechne Richtung zum Feind
        Vector2 directionToEnemy = new Vector2(enemy.position).sub(this.position);
        float angleToEnemy = directionToEnemy.angleDeg();

        // Zufälligkeit: 75% Chance zu treffen
        float randomFactor = MathUtils.random(-10f, 10f); // ±10 Grad Abweichung
        if (MathUtils.random() > hitChance) {
            // Verfehlt - größere Abweichung
            randomFactor = MathUtils.random(-30f, 30f);
        }

        float shootAngle = angleToEnemy + randomFactor;

        // Erstelle Projektil
        weaponCooldownTimer = weaponCooldown;

        // Spawn-Position: Etwas vor dem Dreieck
        float spawnDistance = size;
        float angleRad = (float) Math.toRadians(shootAngle);
        float spawnX = position.x + (float) Math.cos(angleRad) * spawnDistance;
        float spawnY = position.y + (float) Math.sin(angleRad) * spawnDistance;

        return new Projectile(spawnX, spawnY, shootAngle, 300f, this);
    }

    /**
     * Prüft ob geschossen werden kann
     */
    public boolean canShoot() {
        return weaponCooldownTimer <= 0;
    }

    public boolean collidesWith(TargetSeeker other) {
        if (this.isDestroyed || other.isDestroyed) return false;
        if (this == other) return false;

        float distance = this.position.dst(other.position);
        return distance < (this.collisionRadius + other.collisionRadius);
    }

    public void destroy() {
        this.isDestroyed = true;
    }

    public void render(ShapeRenderer renderer) {
        if (isDestroyed) return;

        renderer.setColor(r, g, b, a);

        float halfSize = size / 2f;

        float x1 = position.x + size;
        float y1 = position.y;

        float x2 = position.x - halfSize;
        float y2 = position.y + halfSize;

        float x3 = position.x - halfSize;
        float y3 = position.y - halfSize;

        Vector2 p1 = rotatePoint(x1, y1, position.x, position.y, rotation);
        Vector2 p2 = rotatePoint(x2, y2, position.x, position.y, rotation);
        Vector2 p3 = rotatePoint(x3, y3, position.x, position.y, rotation);

        renderer.triangle(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);
    }

    /**
     * Zeichnet Waffenreichweite (Debug)
     */
    public void renderWeaponRange(ShapeRenderer renderer) {
        if (isDestroyed) return;

        renderer.setColor(1, 0, 0, 0.1f);
        renderer.circle(position.x, position.y, weaponRange);
    }

    public void renderCollisionRadius(ShapeRenderer renderer) {
        if (isDestroyed) return;

        renderer.setColor(1, 1, 1, 0.2f);
        renderer.circle(position.x, position.y, collisionRadius);
    }

    public void renderTarget(ShapeRenderer renderer) {
        if (isDestroyed) return;

        renderer.setColor(1, 0, 0, 0.5f);
        renderer.circle(target.x, target.y, 10);

        renderer.setColor(1, 0, 0, 1);
        float crossSize = 15;
        renderer.line(target.x - crossSize, target.y, target.x + crossSize, target.y);
        renderer.line(target.x, target.y - crossSize, target.x, target.y + crossSize);
    }

    private Vector2 rotatePoint(float x, float y, float centerX, float centerY, float angleDeg) {
        float angleRad = (float) Math.toRadians(angleDeg);
        float cos = (float) Math.cos(angleRad);
        float sin = (float) Math.sin(angleRad);

        float translatedX = x - centerX;
        float translatedY = y - centerY;

        float rotatedX = translatedX * cos - translatedY * sin;
        float rotatedY = translatedX * sin + translatedY * cos;

        return new Vector2(rotatedX + centerX, rotatedY + centerY);
    }

    // Setter
    public void setColor(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public void setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public void setSize(float size) {
        this.size = size;
        this.margin = size * 2;
        this.collisionRadius = size * 0.8f;
    }

    public void setWeaponRange(float range) {
        this.weaponRange = range;
    }

    public void setWeaponCooldown(float cooldown) {
        this.weaponCooldown = cooldown;
    }

    public void setHitChance(float chance) {
        this.hitChance = Math.max(0f, Math.min(1f, chance));
    }

    public void forceNewTarget() {
        if (!isDestroyed) {
            generateNewTarget();
        }
    }

    // Getter
    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getTarget() {
        return target;
    }

    public float getRotation() {
        return rotation;
    }

    public float getTargetRotation() {
        return targetRotation;
    }

    public boolean isRotating() {
        return state == State.ROTATING;
    }

    public boolean isMoving() {
        return state == State.MOVING;
    }

    public float getDistanceToTarget() {
        return position.dst(target);
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public float getCollisionRadius() {
        return collisionRadius;
    }

    public float getWeaponRange() {
        return weaponRange;
    }

    public float getWeaponCooldownTimer() {
        return weaponCooldownTimer;
    }

    public float getHitChance() {
        return hitChance;
    }
}
