package game.samples.nav01;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Bewegung am Grid
 */
public class GridUnit {
    private Vector2 position;
    private Vector2 gridPosition;
    private Vector2 targetGridPosition;
    private Vector2 nextGridPosition;

    private float rotation;
    private float targetRotation;

    private float size;
    private Color color;

    private enum State {
        ROTATING,
        MOVING
    }
    private State state;

    private float moveSpeed;
    private float rotationSpeed;

    private float gridSpacing;
    private float mapWidth;
    private float mapHeight;

    private Vector2 moveDirection;

    public GridUnit(float gridX, float gridY, float gridSpacing, float mapWidth, float mapHeight) {
        this.gridPosition = new Vector2(gridX, gridY);
        this.nextGridPosition = new Vector2(gridX, gridY);
        this.position = new Vector2(gridX * gridSpacing, gridY * gridSpacing);
        this.gridSpacing = gridSpacing;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;

        this.size = 10f;
        this.color = new Color(
            0.5f + MathUtils.random() * 0.5f,
            0.5f + MathUtils.random() * 0.5f,
            0.5f + MathUtils.random() * 0.5f,
            1f
        );

        this.rotation = MathUtils.random(0f, 360f);
        this.targetRotation = 0f;
        this.state = State.ROTATING;

        this.moveSpeed = 100f;
        this.rotationSpeed = 180f;

        this.moveDirection = new Vector2();

        selectRandomTarget();

        System.out.println("Unit erstellt bei Grid (" + (int)gridX + ", " + (int)gridY + ")");
    }

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

    private void updateRotation(float deltaTime) {
        float diff = targetRotation - rotation;

        while (diff > 180f) diff -= 360f;
        while (diff < -180f) diff += 360f;

        if (Math.abs(diff) < 2f) {
            rotation = targetRotation;
            state = State.MOVING;
            System.out.println("Rotation fertig. Bewege zu Grid (" +
                (int)nextGridPosition.x + ", " + (int)nextGridPosition.y + ")");
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
        // Ziel-Position in World-Koordinaten (nächster Grid-Punkt)
        Vector2 nextWorldPos = new Vector2(
            nextGridPosition.x * gridSpacing,
            nextGridPosition.y * gridSpacing
        );

        // Berechne Distanz
        Vector2 toNext = new Vector2(nextWorldPos).sub(position);
        float distance = toNext.len();

        // Bewegungsdistanz diesen Frame
        float moveDistance = moveSpeed * deltaTime;

        // Wenn wir den nächsten Grid-Punkt erreichen
        if (distance <= moveDistance) {
            // Snap zur Grid-Position
            position.set(nextWorldPos);
            gridPosition.set(nextGridPosition);

            System.out.println("Grid-Punkt erreicht: (" + (int)gridPosition.x + ", " + (int)gridPosition.y + ")");

            // Prüfe ob finales Ziel erreicht
            if (gridPosition.equals(targetGridPosition)) {
                System.out.println("FINALES ZIEL ERREICHT!");
                selectRandomTarget();
            } else {
                // Berechne nächsten Schritt zum Ziel
                calculateNextStep();
            }

            return;
        }

        // Normale Bewegung zum nächsten Grid-Punkt
        toNext.nor();
        position.add(toNext.x * moveDistance, toNext.y * moveDistance);
    }

    private void calculateNextStep() {
        // Berechne Richtung zum finalen Ziel
        Vector2 toTarget = new Vector2(targetGridPosition).sub(gridPosition);

        float dx = 0;
        float dy = 0;

        // NUR horizontal oder vertikal (KEINE Diagonale!)
        // Entscheide welche Achse zuerst bewegt wird
        if (Math.abs(toTarget.x) > Math.abs(toTarget.y)) {
            // Horizontale Bewegung hat Priorität
            dx = Math.signum(toTarget.x);
            dy = 0;
        } else {
            // Vertikale Bewegung hat Priorität
            dx = 0;
            dy = Math.signum(toTarget.y);
        }

        // Setze nächsten Grid-Punkt
        nextGridPosition.set(
            gridPosition.x + dx,
            gridPosition.y + dy
        );

        // Berechne Bewegungsrichtung
        moveDirection.set(dx, dy).nor();

        // Berechne benötigte Rotation
        float newTargetRotation = (float)Math.toDegrees(Math.atan2(dy, dx));

        // Prüfe ob Rotation nötig
        float rotDiff = newTargetRotation - rotation;
        while (rotDiff > 180f) rotDiff -= 360f;
        while (rotDiff < -180f) rotDiff += 360f;

        if (Math.abs(rotDiff) > 5f) {
            // Richtungswechsel - rotieren
            targetRotation = newTargetRotation;
            state = State.ROTATING;
            System.out.println("Richtungswechsel bei Grid (" + (int)gridPosition.x + ", " + (int)gridPosition.y +
                ") - Rotiere zu " + targetRotation + "°");
        } else {
            // Keine Rotation nötig, weiterbewegen
            rotation = newTargetRotation;
            System.out.println("Bewege weiter zu Grid (" + (int)nextGridPosition.x + ", " + (int)nextGridPosition.y + ")");
        }
    }

    private void selectRandomTarget() {
        int maxGridX = (int)(mapWidth / gridSpacing);
        int maxGridY = (int)(mapHeight / gridSpacing);

        // Wähle zufällige Grid-Position
        int targetX, targetY;
        int attempts = 0;
        do {
            targetX = MathUtils.random(0, maxGridX - 1);
            targetY = MathUtils.random(0, maxGridY - 1);
            attempts++;
        } while ((targetX == (int)gridPosition.x && targetY == (int)gridPosition.y) && attempts < 10);

        targetGridPosition = new Vector2(targetX, targetY);

        System.out.println("=== NEUES ZIEL ===");
        System.out.println("Von Grid (" + (int)gridPosition.x + ", " + (int)gridPosition.y + ")");
        System.out.println("Zu Grid (" + (int)targetGridPosition.x + ", " + (int)targetGridPosition.y + ")");
        System.out.println("==================");

        // Berechne ersten Schritt
        calculateNextStep();
    }

    private void normalizeRotation() {
        while (rotation >= 360f) rotation -= 360f;
        while (rotation < 0f) rotation += 360f;
    }

    public void render(ShapeRenderer renderer) {
        renderer.setColor(color);

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

    public void renderDebug(ShapeRenderer renderer) {
        // Aktuelle Grid-Position (grün)
        float currentX = gridPosition.x * gridSpacing;
        float currentY = gridPosition.y * gridSpacing;
        renderer.setColor(0, 1, 0, 0.3f);
        renderer.circle(currentX, currentY, 8);

        // Nächster Grid-Punkt (gelb)
        float nextX = nextGridPosition.x * gridSpacing;
        float nextY = nextGridPosition.y * gridSpacing;
        renderer.setColor(1, 1, 0, 0.5f);
        renderer.circle(nextX, nextY, 6);

        // Finales Ziel (rot)
        float targetX = targetGridPosition.x * gridSpacing;
        float targetY = targetGridPosition.y * gridSpacing;
        renderer.setColor(1, 0, 0, 0.5f);
        renderer.circle(targetX, targetY, 10);

        // Pfad zum finalen Ziel (gerade Linie)
        renderer.setColor(color.r, color.g, color.b, 0.3f);
        renderer.line(position.x, position.y, targetX, targetY);

        // Pfad zum nächsten Grid-Punkt
        if (state == State.MOVING) {
            renderer.setColor(1, 1, 0, 0.8f);
            renderer.line(position.x, position.y, nextX, nextY);
        }

        // Manhattan-Pfad visualisieren (nur horizontal/vertikal)
        renderer.setColor(color.r, color.g, color.b, 0.2f);
        Vector2 current = new Vector2(gridPosition);
        int steps = 0;
        while (!current.equals(targetGridPosition) && steps < 200) {
            Vector2 toTarget = new Vector2(targetGridPosition).sub(current);

            float dx = 0;
            float dy = 0;

            // Entscheide Richtung (horizontal oder vertikal)
            if (Math.abs(toTarget.x) > Math.abs(toTarget.y)) {
                dx = Math.signum(toTarget.x);
            } else {
                dy = Math.signum(toTarget.y);
            }

            Vector2 next = new Vector2(current.x + dx, current.y + dy);

            renderer.line(
                current.x * gridSpacing, current.y * gridSpacing,
                next.x * gridSpacing, next.y * gridSpacing
            );

            current.set(next);
            steps++;
        }
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

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getGridPosition() {
        return gridPosition;
    }

    public boolean isRotating() {
        return state == State.ROTATING;
    }

    public boolean isMoving() {
        return state == State.MOVING;
    }
}
