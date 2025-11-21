package game.samples.demo03;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;


public class Main extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private Viewport viewport;
    private OrthographicCamera camera;

    private static final float VIRTUAL_WIDTH = 800f;
    private static final float VIRTUAL_HEIGHT = 600f;

    private CatmullRomSpline<Vector2> spline;
    private Vector2[] controlPoints;

    private SplineObject splineObject;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

        // Kontrollpunkte
        controlPoints = new Vector2[]{
            new Vector2(100, 150),
            new Vector2(150, 450),
            new Vector2(400, 500),
            new Vector2(650, 400),
            new Vector2(700, 150)
        };

        spline = new CatmullRomSpline<>(controlPoints, false);

        // Erstelle SplineObject
        splineObject = new SplineObject(spline, 0.2f, 20f);
        splineObject.setColor(0, 1, 0, 1);
        splineObject.setPingPong(true);

        // Standard Rotations-Einstellungen
        splineObject.setRotationSpeed(360f); // 360° pro Sekunde
        splineObject.setTotalRotationDegrees(360f); // Eine volle Umdrehung
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        handleInput();
        splineObject.update(deltaTime);

        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // Zeichne Kontrollpunkte
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1, 0, 0, 1);
        for (Vector2 point : controlPoints) {
            shapeRenderer.circle(point.x, point.y, 6);
        }
        shapeRenderer.end();

        // Zeichne Spline
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1);
        Gdx.gl.glLineWidth(2);

        Vector2 point = new Vector2();
        Vector2 previousPoint = new Vector2();
        int segments = 100;

        for (int i = 0; i <= segments; i++) {
            float t = (float) i / segments;
            spline.valueAt(point, t);

            if (i > 0) {
                shapeRenderer.line(previousPoint.x, previousPoint.y, point.x, point.y);
            }

            previousPoint.set(point);
        }

        shapeRenderer.end();

        // Zeichne Dreieck
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Färbe anders wenn rotierend
        if (splineObject.isRotating()) {
            splineObject.setColor(1, 1, 0, 1); // Gelb beim Rotieren
        } else {
            splineObject.setColor(0, 1, 0, 1); // Grün beim Bewegen
        }

        splineObject.render(shapeRenderer);
        shapeRenderer.end();

        // Text
        batch.begin();
        font.draw(batch, "SPACE = Start/Stop", 20, 580);
        font.draw(batch, "R = Reset", 20, 555);
        font.draw(batch, "M = Modus (Ping-Pong / Loop)", 20, 530);
        font.draw(batch, "", 20, 505);
        font.draw(batch, "Q/E = Rotationsgeschwindigkeit", 20, 480);
        font.draw(batch, "A/D = Rotationsgrad (wie viel)", 20, 455);
        font.draw(batch, "Pfeiltasten = Bewegungsgeschwindigkeit", 20, 430);

        font.setColor(Color.YELLOW);
        font.draw(batch, String.format("Reise-Progress: %.1f%%", splineObject.getProgress() * 100), 450, 220);
        font.draw(batch, String.format("Move Speed: %.2f", splineObject.getSpeed()), 450, 195);
        font.draw(batch, "Status: " + (splineObject.isRotating() ? "ROTIERT" : "BEWEGT"), 450, 170);

        if (splineObject.isRotating()) {
            font.draw(batch, String.format("Rot.Progress: %.1f%%", splineObject.getRotationProgress() * 100), 450, 145);
            font.draw(batch, String.format("Rotiert: %.0f° / %.0f°",
                splineObject.getCurrentRotationDegrees(),
                splineObject.getTotalRotationDegrees()), 450, 120);
        }

        font.draw(batch, String.format("Rotation: %.1f°", splineObject.getRotation()), 450, 95);
        font.draw(batch, String.format("Rot.Speed: %.0f°/s", splineObject.getRotationSpeed()), 450, 70);
        font.draw(batch, String.format("Rot.Grad: %.0f°", splineObject.getTotalRotationDegrees()), 450, 45);
        font.draw(batch, String.format("Rot.Dauer: %.2fs", splineObject.getRotationDuration()), 450, 20);

        font.setColor(Color.WHITE);
        batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            splineObject.toggle();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            splineObject.reset();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            splineObject.setPingPong(!splineObject.isPingPong());
        }

        // Bewegungsgeschwindigkeit
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            splineObject.setSpeed(splineObject.getSpeed() + 0.05f);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            splineObject.setSpeed(Math.max(0.05f, splineObject.getSpeed() - 0.05f));
        }

        // Rotationsgeschwindigkeit (Q/E)
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            float newSpeed = Math.max(45f, splineObject.getRotationSpeed() - 45f);
            splineObject.setRotationSpeed(newSpeed);
            System.out.println("Rotationsgeschwindigkeit: " + newSpeed + "°/s → Dauer: " + splineObject.getRotationDuration() + "s");
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            float newSpeed = Math.min(1440f, splineObject.getRotationSpeed() + 45f);
            splineObject.setRotationSpeed(newSpeed);
            System.out.println("Rotationsgeschwindigkeit: " + newSpeed + "°/s → Dauer: " + splineObject.getRotationDuration() + "s");
        }

        // Rotations-Grad / Anzahl Umdrehungen (A/D)
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            float newDegrees = Math.max(90f, splineObject.getTotalRotationDegrees() - 90f);
            splineObject.setTotalRotationDegrees(newDegrees);
            System.out.println("Rotationsgrad: " + newDegrees + "° → Dauer: " + splineObject.getRotationDuration() + "s");
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            float newDegrees = Math.min(1440f, splineObject.getTotalRotationDegrees() + 90f);
            splineObject.setTotalRotationDegrees(newDegrees);
            System.out.println("Rotationsgrad: " + newDegrees + "° → Dauer: " + splineObject.getRotationDuration() + "s");
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
