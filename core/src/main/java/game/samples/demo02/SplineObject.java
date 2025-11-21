package game.samples.demo02;

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
                movingForward = false;
            }
        } else {
            progress -= speed * deltaTime;

            if (progress <= 0.0f) {
                progress = 0.0f;
                movingForward = true;
            }
        }
    }

    /**
     * Loop Modus: Springt zurück zum Anfang
     */
    private void updateLoop(float deltaTime) {
        progress += speed * deltaTime;

        if (progress >= 1.0f) {
            progress = 0f;
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
            // Berechne Winkel in Grad
            rotation = direction.angleDeg();
        }
    }

    /**
     * Zeichnet das Dreieck
     */
    public void render(ShapeRenderer renderer) {
        renderer.setColor(r, g, b, a);

        // Dreieck-Vertices berechnen (zeigt nach rechts bei 0°)
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

        // Rotiere Punkte um die Position
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

        // Verschiebe zum Ursprung
        float translatedX = x - centerX;
        float translatedY = y - centerY;

        // Rotiere
        float rotatedX = translatedX * cos - translatedY * sin;
        float rotatedY = translatedX * sin + translatedY * cos;

        // Verschiebe zurück
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
        updatePosition();
        lastPosition.set(position);
    }

    /**
     * Setzt die Spline (z.B. wenn Kontrollpunkte geändert wurden)
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
}
