package game.samples.demo01;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Vector2;

/**
 * objekt entwlang spline bewegen
 */
public class Main extends ApplicationAdapter {

    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    private CatmullRomSpline<Vector2> spline;
    private Vector2[] controlPoints;

    // Unser bewegendes Objekt
    private SplineObject splineObject;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);

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
        splineObject = new SplineObject(spline, 0.2f, 15f);
        splineObject.setColor(0, 1, 0, 1); // Grün
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        handleInput();
        splineObject.update(deltaTime);

        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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

        // Zeichne SplineObject
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        splineObject.render(shapeRenderer);
        shapeRenderer.end();

        // Text
        batch.begin();
        font.draw(batch, "SPACE = Start/Stop", 50, 580);
        font.draw(batch, "R = Reset", 50, 540);
        font.draw(batch, "M = Modus wechseln (Ping-Pong / Loop)", 50, 500);
        font.draw(batch, "Pfeiltasten HOCH/RUNTER = Geschwindigkeit", 50, 460);

        font.draw(batch, String.format("Progress: %.1f%%", splineObject.getProgress() * 100), 50, 180);
        font.draw(batch, String.format("Speed: %.2f", splineObject.getSpeed()), 50, 140);
        font.draw(batch, "Richtung: " + (splineObject.isMovingForward() ? "VORWÄRTS" : "RÜCKWÄRTS"), 50, 100);
        font.draw(batch, "Modus: " + (splineObject.isPingPong() ? "PING-PONG" : "LOOP"), 50, 60);

        batch.end();
    }


    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            splineObject.toggle();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            splineObject.reset();
        }

        // Modus wechseln
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            splineObject.setPingPong(!splineObject.isPingPong());
        }

        // Geschwindigkeit anpassen
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            splineObject.setSpeed(splineObject.getSpeed() + 0.05f);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            splineObject.setSpeed(Math.max(0.05f, splineObject.getSpeed() - 0.05f));
        }
    }


    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }

}
