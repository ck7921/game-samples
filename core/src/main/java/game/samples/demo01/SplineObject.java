package game.samples.demo01;

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
    private float radius;
    private float r, g, b, a; // Farbe

    public SplineObject(CatmullRomSpline<Vector2> spline, float speed, float radius) {
        this.spline = spline;
        this.speed = speed;
        this.radius = radius;
        this.position = new Vector2();
        this.progress = 0f;
        this.isMoving = false;
        this.movingForward = true;
        this.pingPong = true; // Standard: Ping-Pong Modus

        // Standard-Farbe: Grün
        this.r = 0f;
        this.g = 1f;
        this.b = 0f;
        this.a = 1f;

        // Startposition
        updatePosition();
    }

    /**
     * Update Bewegung entlang der Spline
     */
    public void update(float deltaTime) {
        if (!isMoving) return;

        if (pingPong) {
            updatePingPong(deltaTime);
        } else {
            updateLoop(deltaTime);
        }

        updatePosition();
    }

    /**
     * Ping-Pong Modus: Bewegt sich hin und zurück
     */
    private void updatePingPong(float deltaTime) {
        if (movingForward) {
            progress += speed * deltaTime;

            if (progress >= 1.0f) {
                progress = 1.0f;
                movingForward = false; // Richtung umkehren
            }
        } else {
            progress -= speed * deltaTime;

            if (progress <= 0.0f) {
                progress = 0.0f;
                movingForward = true; // Richtung umkehren
            }
        }
    }

    /**
     * Loop Modus: Springt zurück zum Anfang
     */
    private void updateLoop(float deltaTime) {
        progress += speed * deltaTime;

        if (progress >= 1.0f) {
            progress = 0f; // Zurück zum Anfang (mit Sprung)
        }
    }

    /**
     * Aktualisiert die Position basierend auf dem aktuellen Progress
     */
    private void updatePosition() {
        spline.valueAt(position, progress);
    }

    /**
     * Zeichnet das Objekt
     */
    public void render(ShapeRenderer renderer) {
        renderer.setColor(r, g, b, a);
        renderer.circle(position.x, position.y, radius);
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
     * @param pingPong true = hin und zurück, false = loop mit Sprung
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

    public float getRadius() {
        return radius;
    }

    public boolean isPingPong() {
        return pingPong;
    }
}
