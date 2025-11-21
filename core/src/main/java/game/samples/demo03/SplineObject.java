package game.samples.demo03;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Vector2;

public class SplineObject {
    private CatmullRomSpline<Vector2> spline;
    private Vector2 position;
    private float progress; // 0.0 bis 1.0
    private float speed;
    private boolean isMoving;
    private boolean movingForward; // true = vorwärts, false = rückwärts
    private boolean pingPong; // true = hin und zurück, false = loop

    // Visuelle Eigenschaften
    private float size; // Größe des Dreiecks
    private float r, g, b, a; // Farbe
    private float rotation; // Rotation in Grad

    // Für Richtungsberechnung
    private Vector2 direction;
    private Vector2 lastPosition;

    // Rotation am Ende
    private enum State {
        MOVING,           // Normale Bewegung
        ROTATING_AT_END   // Rotiert am Ende
    }
    private State state;
    private float rotationSpeed; // Grad pro Sekunde
    private float startRotation; // Rotation zu Beginn der End-Rotation
    private float totalRotationDegrees; // Wie viel Grad soll rotiert werden
    private float currentRotationDegrees; // Wie viel wurde bereits rotiert

    public SplineObject(CatmullRomSpline<Vector2> spline, float speed, float size) {
        this.spline = spline;
        this.speed = speed;
        this.size = size;
        this.position = new Vector2();
        this.lastPosition = new Vector2();
        this.direction = new Vector2();
        this.progress = 0f;
        this.isMoving = false;
        this.movingForward = true;
        this.pingPong = true;
        this.rotation = 0f;
        this.state = State.MOVING;

        // Rotation-Einstellungen (Standard: 360° mit 360°/s)
        this.rotationSpeed = 360f; // Grad pro Sekunde
        this.totalRotationDegrees = 360f; // Eine volle Umdrehung
        this.currentRotationDegrees = 0f;

        // Standard-Farbe: Grün
        this.r = 0f;
        this.g = 1f;
        this.b = 0f;
        this.a = 1f;

        // Startposition
        updatePosition();
        lastPosition.set(position);
    }

    /**
     * Update Bewegung entlang der Spline
     */
    public void update(float deltaTime) {
        if (!isMoving) return;

        switch (state) {
            case MOVING:
                updateMovement(deltaTime);
                break;
            case ROTATING_AT_END:
                updateRotationAtEnd(deltaTime);
                break;
        }
    }

    /**
     * Update normale Bewegung
     */
    private void updateMovement(float deltaTime) {
        lastPosition.set(position);

        if (pingPong) {
            updatePingPong(deltaTime);
        } else {
            updateLoop(deltaTime);
        }

        updatePosition();
        updateRotation();
    }

    /**
     * Ping-Pong Modus: Bewegt sich hin und zurück
     */
    private void updatePingPong(float deltaTime) {
        if (movingForward) {
            progress += speed * deltaTime;

            if (progress >= 1.0f) {
                progress = 1.0f;
                updatePosition();

                // Starte Rotation am Ende
                startRotationAtEnd();
            }
        } else {
            progress -= speed * deltaTime;

            if (progress <= 0.0f) {
                progress = 0.0f;
                updatePosition();

                // Starte Rotation am Ende
                startRotationAtEnd();
            }
        }
    }

    /**
     * Loop Modus: Springt zurück zum Anfang
     */
    private void updateLoop(float deltaTime) {
        progress += speed * deltaTime;

        if (progress >= 1.0f) {
            progress = 1.0f;
            updatePosition();

            // Starte Rotation am Ende
            startRotationAtEnd();
        }
    }

    /**
     * Startet die Rotation am Ende
     */
    private void startRotationAtEnd() {
        state = State.ROTATING_AT_END;
        startRotation = rotation;
        currentRotationDegrees = 0f;
    }

    /**
     * Update Rotation am Ende
     */
    private void updateRotationAtEnd(float deltaTime) {
        // Berechne wie viel rotiert werden soll in diesem Frame
        float rotationThisFrame = rotationSpeed * deltaTime;

        // Prüfe ob wir über das Ziel hinausschießen würden
        if (currentRotationDegrees + rotationThisFrame >= totalRotationDegrees) {
            // Nur noch bis zum Ziel rotieren
            rotationThisFrame = totalRotationDegrees - currentRotationDegrees;
            currentRotationDegrees = totalRotationDegrees;
        } else {
            currentRotationDegrees += rotationThisFrame;
        }

        // Rotiere
        rotation += rotationThisFrame;

        // Normalisiere Rotation (0-360)
        while (rotation >= 360f) {
            rotation -= 360f;
        }
        while (rotation < 0f) {
            rotation += 360f;
        }

        // Prüfe ob Rotation fertig
        if (currentRotationDegrees >= totalRotationDegrees) {
            finishRotation();
        }
    }

    /**
     * Beendet die Rotation und setzt Bewegung fort
     */
    private void finishRotation() {
        state = State.MOVING;
        currentRotationDegrees = 0f;

        if (pingPong) {
            // Richtung umkehren
            movingForward = !movingForward;
        } else {
            // Zurück zum Anfang
            progress = 0f;
            updatePosition();
        }
    }

    /**
     * Aktualisiert die Position basierend auf dem aktuellen Progress
     */
    private void updatePosition() {
        spline.valueAt(position, progress);
    }

    /**
     * Berechnet die Rotation basierend auf der Bewegungsrichtung
     */
    private void updateRotation() {
        direction.set(position).sub(lastPosition);

        if (direction.len() > 0.01f) {
            rotation = direction.angleDeg();
        }
    }

    /**
     * Zeichnet das Dreieck
     */
    public void render(ShapeRenderer renderer) {
        renderer.setColor(r, g, b, a);

        // Dreieck-Vertices berechnen
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

        // Zeichne Dreieck
        renderer.triangle(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);
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

    /**
     * Startet die Bewegung
     */
    public void start() {
        isMoving = true;
    }

    /**
     * Stoppt die Bewegung
     */
    public void stop() {
        isMoving = false;
    }

    /**
     * Toggle zwischen start/stop
     */
    public void toggle() {
        isMoving = !isMoving;
    }

    /**
     * Setzt Progress zurück auf 0
     */
    public void reset() {
        progress = 0f;
        movingForward = true;
        isMoving = false;
        state = State.MOVING;
        currentRotationDegrees = 0f;
        updatePosition();
        lastPosition.set(position);
    }

    /**
     * Setzt die Spline
     */
    public void setSpline(CatmullRomSpline<Vector2> spline) {
        this.spline = spline;
        updatePosition();
    }

    /**
     * Setzt die Farbe
     */
    public void setColor(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    /**
     * Setzt die Geschwindigkeit
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * Setzt den Bewegungsmodus
     */
    public void setPingPong(boolean pingPong) {
        this.pingPong = pingPong;
    }

    /**
     * Setzt den Progress manuell (0.0 bis 1.0)
     */
    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
        updatePosition();
    }

    /**
     * Setzt die Größe
     */
    public void setSize(float size) {
        this.size = size;
    }

    /**
     * Setzt die Rotations-Geschwindigkeit (Grad pro Sekunde)
     */
    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = Math.max(1f, rotationSpeed);
    }

    /**
     * Setzt wie viele Grad rotiert werden sollen
     */
    public void setTotalRotationDegrees(float degrees) {
        this.totalRotationDegrees = Math.max(0f, degrees);
    }

    /**
     * Berechnet die Rotationsdauer basierend auf Grad und Geschwindigkeit
     */
    public float getRotationDuration() {
        return totalRotationDegrees / rotationSpeed;
    }

    // Getter
    public Vector2 getPosition() {
        return position;
    }

    public float getProgress() {
        return progress;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public boolean isMovingForward() {
        return movingForward;
    }

    public float getSpeed() {
        return speed;
    }

    public float getSize() {
        return size;
    }

    public boolean isPingPong() {
        return pingPong;
    }

    public float getRotation() {
        return rotation;
    }

    public boolean isRotating() {
        return state == State.ROTATING_AT_END;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public float getTotalRotationDegrees() {
        return totalRotationDegrees;
    }

    public float getCurrentRotationDegrees() {
        return currentRotationDegrees;
    }

    public float getRotationProgress() {
        if (totalRotationDegrees == 0) return 1f;
        return currentRotationDegrees / totalRotationDegrees;
    }
}
