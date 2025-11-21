package game.samples.demo05;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * collision detection with multiple units
 */
public class Main extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private Viewport viewport;
    private OrthographicCamera camera;

    private static final float VIRTUAL_WIDTH = 800f;
    private static final float VIRTUAL_HEIGHT = 600f;

    private List<TargetSeeker> seekers;
    private List<Explosion> explosions;

    private int totalSpawned;
    private int totalDestroyed;
    private boolean showCollisionRadius;

    private Sound collisionSound; // Optional

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
        explosions = new ArrayList<>();

        showCollisionRadius = false;
        totalSpawned = 0;
        totalDestroyed = 0;

        // Optional: Sound laden
        // collisionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/explosion.wav"));

        // Erstelle Start-Seeker
        spawnSeeker(0, 1, 0, 1); // Grün
        spawnSeeker(1, 0, 0, 1); // Rot
        spawnSeeker(0, 0, 1, 1); // Blau
    }

    private void spawnSeeker(float r, float g, float b, float a) {
        TargetSeeker seeker = new TargetSeeker(
            VIRTUAL_WIDTH,
            VIRTUAL_HEIGHT,
            15f + (float)Math.random() * 10f,  // Zufällige Größe 15-25
            100f + (float)Math.random() * 150f, // Zufällige Geschwindigkeit 100-250
            120f + (float)Math.random() * 180f  // Zufällige Rotation 120-300
        );
        seeker.setColor(r, g, b, a);
        seekers.add(seeker);
        totalSpawned++;
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        handleInput();
        updateGame(deltaTime);
        checkCollisions();

        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // Zeichne Ziele
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (TargetSeeker seeker : seekers) {
            seeker.renderTarget(shapeRenderer);
        }
        shapeRenderer.end();

        // Zeichne Linien zu Zielen
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(1);
        for (TargetSeeker seeker : seekers) {
            if (!seeker.isDestroyed()) {
                shapeRenderer.setColor(1, 1, 0, 0.2f);
                shapeRenderer.line(
                    seeker.getPosition().x,
                    seeker.getPosition().y,
                    seeker.getTarget().x,
                    seeker.getTarget().y
                );
            }
        }
        shapeRenderer.end();

        // Zeichne Kollisionsradien (Debug)
        if (showCollisionRadius) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(1);
            for (TargetSeeker seeker : seekers) {
                seeker.renderCollisionRadius(shapeRenderer);
            }
            shapeRenderer.end();
        }

        // Zeichne Explosionen
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Explosion explosion : explosions) {
            explosion.render(shapeRenderer);
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
        font.draw(batch, "SPACE = Neues Dreieck spawnen", 20, 580);
        font.draw(batch, "C = Kollisionsradius zeigen (Toggle)", 20, 555);
        font.draw(batch, "R = Reset", 20, 530);

        font.setColor(Color.YELLOW);
        font.draw(batch, "Aktiv: " + seekers.size(), 20, 150);
        font.draw(batch, "Gespawnt: " + totalSpawned, 20, 125);
        font.draw(batch, "Zerstört: " + totalDestroyed, 20, 100);
        font.draw(batch, "Kollisionsradius: " + (showCollisionRadius ? "AN" : "AUS"), 20, 75);

        font.setColor(Color.WHITE);
        batch.end();
    }

    private void handleInput() {
        // Neues Dreieck spawnen
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            float r = (float) Math.random();
            float g = (float) Math.random();
            float b = (float) Math.random();
            spawnSeeker(r, g, b, 1f);
        }

        // Kollisionsradius anzeigen
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            showCollisionRadius = !showCollisionRadius;
        }

        // Reset
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            seekers.clear();
            explosions.clear();
            totalSpawned = 0;
            totalDestroyed = 0;

            spawnSeeker(0, 1, 0, 1);
            spawnSeeker(1, 0, 0, 1);
            spawnSeeker(0, 0, 1, 1);
        }
    }

    private void updateGame(float deltaTime) {
        // Update alle Seeker
        for (TargetSeeker seeker : seekers) {
            seeker.update(deltaTime);
        }

        // Update Explosionen
        Iterator<Explosion> expIter = explosions.iterator();
        while (expIter.hasNext()) {
            Explosion explosion = expIter.next();
            explosion.update(deltaTime);

            if (explosion.isFinished()) {
                expIter.remove();
            }
        }
    }

    private void checkCollisions() {
        List<TargetSeeker> toRemove = new ArrayList<>();

        // Prüfe alle Paare von Seekern
        for (int i = 0; i < seekers.size(); i++) {
            TargetSeeker seeker1 = seekers.get(i);
            if (seeker1.isDestroyed()) continue;

            for (int j = i + 1; j < seekers.size(); j++) {
                TargetSeeker seeker2 = seekers.get(j);
                if (seeker2.isDestroyed()) continue;

                // Prüfe Kollision
                if (seeker1.collidesWith(seeker2)) {
                    // Markiere beide als zerstört
                    seeker1.destroy();
                    seeker2.destroy();

                    toRemove.add(seeker1);
                    toRemove.add(seeker2);

                    // Erstelle Explosion
                    Vector2 collisionPoint = new Vector2(seeker1.getPosition())
                        .add(seeker2.getPosition())
                        .scl(0.5f); // Mittelpunkt zwischen beiden

                    explosions.add(new Explosion(collisionPoint.x, collisionPoint.y));

                    // Spiele Sound
                    if (collisionSound != null) {
                        collisionSound.play(0.5f);
                    }

                    totalDestroyed += 2;

                    System.out.println("KOLLISION! " + totalDestroyed + " Dreiecke zerstört.");
                }
            }
        }

        // Entferne zerstörte Seeker
        seekers.removeAll(toRemove);
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

        if (collisionSound != null) {
            collisionSound.dispose();
        }
    }
}
