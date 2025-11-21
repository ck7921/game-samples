package game.samples.demo04;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class TargetSeeker {
    private Vector2 position;
    private Vector2 target;
    private float rotation; // Aktuelle Rotation in Grad
    private float targetRotation; // Ziel-Rotation in Grad

    // Eigenschaften
    private float size;
    private float moveSpeed; // Pixel pro Sekunde
    private float rotationSpeed; // Grad pro Sekunde

    // Farbe
    private float r, g, b, a;

    // Zustand
    private enum State {
        ROTATING,  // Rotiert zum Ziel
        MOVING     // Bewegt sich zum Ziel
    }

    private State state;

    // Canvas-Grenzen für Ziel-Generierung
    private float canvasWidth;
    private float canvasHeight;
    private float margin; // Abstand vom Rand

    public TargetSeeker(float canvasWidth, float canvasHeight, float size, float moveSpeed, float rotationSpeed) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.size = size;
        this.moveSpeed = moveSpeed;
        this.rotationSpeed = rotationSpeed;
        this.margin = size * 2; // Doppelte Größe als Margin

        this.position = new Vector2();
        this.target = new Vector2();
        this.rotation = 0f;
        this.targetRotation = 0f;
        this.state = State.ROTATING;

        // Standard-Farbe: Grün
        this.r = 0f;
        this.g = 1f;
        this.b = 0f;
        this.a = 1f;

        // Zufällige Startposition
        randomizePosition();

        // Erstes Ziel
        generateNewTarget();
    }

    /**
     * Update-Logik
     */
    public void update(float deltaTime) {
        switch (state) {
            case ROTATING:
                updateRotation(deltaTime);
                break;
            case MOVING:
                updateMovement(deltaTime);
                break;
        }
    }

    /**
     * Rotiert zum Ziel
     */
    private void updateRotation(float deltaTime) {
        // Berechne Differenz zwischen aktueller und Ziel-Rotation
        float diff = targetRotation - rotation;

        // Normalisiere auf -180 bis 180 (kürzester Weg)
        while (diff > 180f) diff -= 360f;
        while (diff < -180f) diff += 360f;

        // Prüfe ob wir nahe genug am Ziel sind
        if (Math.abs(diff) < 1f) {
            rotation = targetRotation;
            state = State.MOVING;
            return;
        }

        // Rotiere in Richtung des Ziels
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

        // Normalisiere Rotation (0-360)
        normalizeRotation();
    }

    /**
     * Bewegt sich zum Ziel
     */
    private void updateMovement(float deltaTime) {
        // Berechne Richtung zum Ziel
        Vector2 direction = new Vector2(target).sub(position);
        float distance = direction.len();

        // Prüfe ob Ziel erreicht
        if (distance < 2f) {
            position.set(target);
            generateNewTarget();
            return;
        }

        // Normalisiere Richtung
        direction.nor();

        // Berechne Bewegung
        float moveDistance = moveSpeed * deltaTime;

        // Verhindere Überschießen
        if (moveDistance > distance) {
            moveDistance = distance;
        }

        // Bewege Position
        position.add(direction.x * moveDistance, direction.y * moveDistance);
    }

    /**
     * Generiert ein neues Ziel und berechnet die nötige Rotation
     */
    private void generateNewTarget() {
        // Generiere zufälliges Ziel mit Margin
        target.x = margin + MathUtils.random() * (canvasWidth - 2 * margin);
        target.y = margin + MathUtils.random() * (canvasHeight - 2 * margin);

        // Berechne Ziel-Rotation
        Vector2 direction = new Vector2(target).sub(position);
        targetRotation = direction.angleDeg();

        // Wechsle in Rotations-Zustand
        state = State.ROTATING;

        System.out.println("Neues Ziel: " + target + " | Rotation: " + targetRotation + "°");
    }

    /**
     * Setzt eine zufällige Start-Position
     */
    private void randomizePosition() {
        position.x = margin + MathUtils.random() * (canvasWidth - 2 * margin);
        position.y = margin + MathUtils.random() * (canvasHeight - 2 * margin);
        rotation = MathUtils.random(360f);
    }

    /**
     * Normalisiert Rotation auf 0-360°
     */
    private void normalizeRotation() {
        while (rotation >= 360f) rotation -= 360f;
        while (rotation < 0f) rotation += 360f;
    }

    /**
     * Zeichnet das Dreieck und das Ziel
     */
    public void render(ShapeRenderer renderer) {
        // Zeichne Dreieck
        renderer.setColor(r, g, b, a);

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

    /**
     * Zeichnet das Ziel
     */
    public void renderTarget(ShapeRenderer renderer) {
        renderer.setColor(1, 0, 0, 0.5f); // Rot, halbtransparent
        renderer.circle(target.x, target.y, 10);

        // Kreuz im Ziel
        renderer.setColor(1, 0, 0, 1);
        float crossSize = 15;
        renderer.line(target.x - crossSize, target.y, target.x + crossSize, target.y);
        renderer.line(target.x, target.y - crossSize, target.x, target.y + crossSize);
    }

    /**
     * Rotiert einen Punkt um einen Mittelpunkt
     */
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
    }

    /**
     * Generiert neues Ziel manuell
     */
    public void forceNewTarget() {
        generateNewTarget();
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
}
