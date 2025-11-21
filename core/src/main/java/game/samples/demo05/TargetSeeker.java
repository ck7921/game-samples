package game.samples.demo05;

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

    // Für Collision Detection
    private float collisionRadius; // Radius für Kreis-basierte Kollision
    private boolean isDestroyed;

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

        // Kollisionsradius (etwas kleiner als die tatsächliche Größe für besseres Gameplay)
        this.collisionRadius = size * 0.8f;

        this.r = 0f;
        this.g = 1f;
        this.b = 0f;
        this.a = 1f;

        randomizePosition();
        generateNewTarget();
    }

    public void update(float deltaTime) {
        if (isDestroyed) return;

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
     * Prüft Kollision mit einem anderen TargetSeeker
     */
    public boolean collidesWith(TargetSeeker other) {
        if (this.isDestroyed || other.isDestroyed) return false;
        if (this == other) return false;

        float distance = this.position.dst(other.position);
        return distance < (this.collisionRadius + other.collisionRadius);
    }

    /**
     * Markiert diesen Seeker als zerstört
     */
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
     * Zeichnet den Kollisionsradius (für Debug)
     */
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
}

