package game.samples.demo04;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * einheit sucht sich ein ziel und bewegt sich darauf zu
 */
public class Main extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private Viewport viewport;
    private OrthographicCamera camera;

    private static final float VIRTUAL_WIDTH = 800f;
    private static final float VIRTUAL_HEIGHT = 600f;

    private TargetSeeker seeker;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

        // Erstelle TargetSeeker
        seeker = new TargetSeeker(
            VIRTUAL_WIDTH,      // Canvas-Breite
            VIRTUAL_HEIGHT,     // Canvas-Höhe
            20f,                // Größe
            150f,               // Bewegungsgeschwindigkeit (Pixel/Sekunde)
            180f                // Rotationsgeschwindigkeit (Grad/Sekunde)
        );

        seeker.setColor(0, 1, 0, 1); // Grün
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        handleInput();
        seeker.update(deltaTime);

        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // Zeichne Ziel
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        seeker.renderTarget(shapeRenderer);
        shapeRenderer.end();

        // Zeichne Linie zum Ziel
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 1, 0, 0.3f);
        Gdx.gl.glLineWidth(2);
        shapeRenderer.line(
            seeker.getPosition().x,
            seeker.getPosition().y,
            seeker.getTarget().x,
            seeker.getTarget().y
        );
        shapeRenderer.end();

        // Zeichne Dreieck
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Färbe anders je nach Zustand
        if (seeker.isRotating()) {
            seeker.setColor(1, 1, 0, 1); // Gelb beim Rotieren
        } else {
            seeker.setColor(0, 1, 0, 1); // Grün beim Bewegen
        }

        seeker.render(shapeRenderer);
        shapeRenderer.end();

        // Text
        batch.begin();
        font.draw(batch, "N = Neues Ziel", 20, 580);
        font.draw(batch, "Pfeiltasten HOCH/RUNTER = Bewegungsgeschw.", 20, 555);
        font.draw(batch, "Q/E = Rotationsgeschwindigkeit", 20, 530);

        font.setColor(Color.YELLOW);
        font.draw(batch, "Status: " + (seeker.isRotating() ? "ROTIERT" : "BEWEGT"), 20, 150);
        font.draw(batch, String.format("Position: (%.0f, %.0f)",
            seeker.getPosition().x, seeker.getPosition().y), 20, 125);
        font.draw(batch, String.format("Ziel: (%.0f, %.0f)",
            seeker.getTarget().x, seeker.getTarget().y), 20, 100);
        font.draw(batch, String.format("Distanz: %.0f", seeker.getDistanceToTarget()), 20, 75);
        font.draw(batch, String.format("Rotation: %.1f°", seeker.getRotation()), 20, 50);

        if (seeker.isRotating()) {
            font.draw(batch, String.format("Ziel-Rotation: %.1f°", seeker.getTargetRotation()), 20, 25);
        }

        font.draw(batch, String.format("Move Speed: %.0f px/s", seeker.getMoveSpeed()), 450, 100);
        font.draw(batch, String.format("Rot Speed: %.0f °/s", seeker.getRotationSpeed()), 450, 75);

        font.setColor(Color.WHITE);
        batch.end();
    }

    private void handleInput() {
        // Neues Ziel
        if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            seeker.forceNewTarget();
        }

        // Bewegungsgeschwindigkeit
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            seeker.setMoveSpeed(seeker.getMoveSpeed() + 25f);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            seeker.setMoveSpeed(Math.max(25f, seeker.getMoveSpeed() - 25f));
        }

        // Rotationsgeschwindigkeit
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            seeker.setRotationSpeed(Math.max(45f, seeker.getRotationSpeed() - 45f));
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            seeker.setRotationSpeed(Math.min(720f, seeker.getRotationSpeed() + 45f));
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
