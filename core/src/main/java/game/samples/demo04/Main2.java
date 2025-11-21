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

import java.util.ArrayList;
import java.util.List;

/**
 * mehrere einheiten bewegen sich
 */
public class Main2 extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private Viewport viewport;
    private OrthographicCamera camera;

    private static final float VIRTUAL_WIDTH = 800f;
    private static final float VIRTUAL_HEIGHT = 600f;

    private List<TargetSeeker> seekers;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

        seekers = new ArrayList<>();

        // Erstelle mehrere TargetSeeker mit verschiedenen Eigenschaften

        // Seeker 1: Grün, langsam
        TargetSeeker seeker1 = new TargetSeeker(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, 18f, 100f, 120f);
        seeker1.setColor(0, 1, 0, 1);
        seekers.add(seeker1);

        // Seeker 2: Rot, schnell
        TargetSeeker seeker2 = new TargetSeeker(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, 15f, 200f, 240f);
        seeker2.setColor(1, 0, 0, 1);
        seekers.add(seeker2);

        // Seeker 3: Blau, mittel
        TargetSeeker seeker3 = new TargetSeeker(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, 20f, 150f, 180f);
        seeker3.setColor(0, 0, 1, 1);
        seekers.add(seeker3);
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        handleInput();

        // Update alle Seeker
        for (TargetSeeker seeker : seekers) {
            seeker.update(deltaTime);
        }

        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // Zeichne Ziele und Linien
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (TargetSeeker seeker : seekers) {
            seeker.renderTarget(shapeRenderer);
        }
        shapeRenderer.end();

        // Zeichne Linien zu Zielen
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(1);
        for (TargetSeeker seeker : seekers) {
            shapeRenderer.setColor(1, 1, 0, 0.2f);
            shapeRenderer.line(
                seeker.getPosition().x,
                seeker.getPosition().y,
                seeker.getTarget().x,
                seeker.getTarget().y
            );
        }
        shapeRenderer.end();

        // Zeichne Dreiecke
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (TargetSeeker seeker : seekers) {
            seeker.render(shapeRenderer);
        }
        shapeRenderer.end();

        // Text
        batch.begin();
        font.draw(batch, "N = Neue Ziele für alle", 20, 580);
        font.draw(batch, "Grün = langsam, Rot = schnell, Blau = mittel", 20, 555);
        batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            for (TargetSeeker seeker : seekers) {
                seeker.forceNewTarget();
            }
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
