package game.samples.demo06;

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
 * shooting units
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
    private List<Projectile> projectiles;

    private int totalSpawned;
    private int totalDestroyed;
    private int totalShots;
    private int totalHits;
    private boolean showCollisionRadius;
    private boolean showWeaponRange;

    private Sound shootSound;
    private Sound hitSound;
    private Sound collisionSound;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.3f);

        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

        seekers = new ArrayList<>();
        explosions = new ArrayList<>();
        projectiles = new ArrayList<>();

        showCollisionRadius = false;
        showWeaponRange = false;
        totalSpawned = 0;
        totalDestroyed = 0;
        totalShots = 0;
        totalHits = 0;

        // Optional: Sounds laden
        // shootSound = Gdx.audio.newSound(Gdx.files.internal("sounds/shoot.wav"));
        // hitSound = Gdx.audio.newSound(Gdx.files.internal("sounds/hit.wav"));
        // collisionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/explosion.wav"));

        // Erstelle Start-Seeker
        spawnSeeker(0, 1, 0, 1);
        spawnSeeker(1, 0, 0, 1);
        spawnSeeker(0, 0, 1, 1);
        spawnSeeker(1, 1, 0, 1);
        spawnSeeker(1, 0, 1, 1);
    }

    private void spawnSeeker(float r, float g, float b, float a) {
        TargetSeeker seeker = new TargetSeeker(
            VIRTUAL_WIDTH,
            VIRTUAL_HEIGHT,
            15f + (float)Math.random() * 10f,
            100f + (float)Math.random() * 150f,
            120f + (float)Math.random() * 180f
        );
        seeker.setColor(r, g, b, a);
        seeker.setWeaponRange(120f + (float)Math.random() * 80f); // 120-200 Reichweite
        seeker.setHitChance(0.75f); // 75% Trefferchance
        seekers.add(seeker);
        totalSpawned++;
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        handleInput();
        updateGame(deltaTime);
        handleCombat();
        checkProjectileHits();
        checkCollisions();

        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // Zeichne Waffenreichweiten (Debug)
        if (showWeaponRange) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (TargetSeeker seeker : seekers) {
                seeker.renderWeaponRange(shapeRenderer);
            }
            shapeRenderer.end();
        }

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

        // Zeichne Projektile
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Projectile projectile : projectiles) {
            projectile.render(shapeRenderer);
        }
        shapeRenderer.end();

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
        font.draw(batch, "SPACE = Neues Dreieck", 20, 580);
        font.draw(batch, "C = Kollisionsradius", 20, 557);
        font.draw(batch, "W = Waffenreichweite", 20, 534);
        font.draw(batch, "R = Reset", 20, 511);

        font.setColor(Color.YELLOW);
        font.draw(batch, "Aktiv: " + seekers.size(), 20, 180);
        font.draw(batch, "Zerstört: " + totalDestroyed, 20, 157);
        font.draw(batch, "Schüsse: " + totalShots, 20, 134);
        font.draw(batch, "Treffer: " + totalHits, 20, 111);

        if (totalShots > 0) {
            float accuracy = (float)totalHits / totalShots * 100f;
            font.draw(batch, String.format("Genauigkeit: %.1f%%", accuracy), 20, 88);
        }

        font.draw(batch, "Projektile: " + projectiles.size(), 20, 65);

        font.setColor(Color.WHITE);
        batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            float r = (float) Math.random();
            float g = (float) Math.random();
            float b = (float) Math.random();
            spawnSeeker(r, g, b, 1f);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            showCollisionRadius = !showCollisionRadius;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            showWeaponRange = !showWeaponRange;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            seekers.clear();
            explosions.clear();
            projectiles.clear();
            totalSpawned = 0;
            totalDestroyed = 0;
            totalShots = 0;
            totalHits = 0;

            for (int i = 0; i < 5; i++) {
                float r = (float) Math.random();
                float g = (float) Math.random();
                float b = (float) Math.random();
                spawnSeeker(r, g, b, 1f);
            }
        }
    }

    private void updateGame(float deltaTime) {
        // Update alle Seeker
        for (TargetSeeker seeker : seekers) {
            seeker.update(deltaTime);
        }

        // Update Projektile
        Iterator<Projectile> projIter = projectiles.iterator();
        while (projIter.hasNext()) {
            Projectile projectile = projIter.next();
            projectile.update(deltaTime);

            if (!projectile.isAlive()) {
                projIter.remove();
            }
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

    /**
     * Lässt Seeker aufeinander schießen
     */
    private void handleCombat() {
        for (TargetSeeker seeker : seekers) {
            if (seeker.isDestroyed()) continue;

            // Suche Feind in Reichweite
            TargetSeeker enemy = seeker.findEnemyInRange(seekers);

            if (enemy != null) {
                // Versuche zu schießen
                Projectile projectile = seeker.tryShoot(enemy);

                if (projectile != null) {
                    projectiles.add(projectile);
                    totalShots++;

                    if (shootSound != null) {
                        shootSound.play(0.3f);
                    }
                }
            }
        }
    }

    /**
     * Prüft ob Projektile Seeker treffen
     */
    private void checkProjectileHits() {
        Iterator<Projectile> projIter = projectiles.iterator();

        while (projIter.hasNext()) {
            Projectile projectile = projIter.next();

            for (TargetSeeker seeker : seekers) {
                if (projectile.hits(seeker)) {
                    // Treffer!
                    seeker.destroy();
                    projectile.destroy();

                    // Explosion
                    explosions.add(new Explosion(seeker.getPosition().x, seeker.getPosition().y));

                    totalHits++;

                    if (hitSound != null) {
                        hitSound.play(0.5f);
                    }

                    System.out.println("TREFFER! Genauigkeit: " + (totalHits * 100f / totalShots) + "%");

                    break;
                }
            }
        }

        // Entferne tote Projektile
        projectiles.removeIf(p -> !p.isAlive());

        // Entferne zerstörte Seeker
        Iterator<TargetSeeker> seekerIter = seekers.iterator();
        while (seekerIter.hasNext()) {
            TargetSeeker seeker = seekerIter.next();
            if (seeker.isDestroyed()) {
                seekerIter.remove();
                totalDestroyed++;
            }
        }
    }

    private void checkCollisions() {
        List<TargetSeeker> toRemove = new ArrayList<>();

        for (int i = 0; i < seekers.size(); i++) {
            TargetSeeker seeker1 = seekers.get(i);
            if (seeker1.isDestroyed()) continue;

            for (int j = i + 1; j < seekers.size(); j++) {
                TargetSeeker seeker2 = seekers.get(j);
                if (seeker2.isDestroyed()) continue;

                if (seeker1.collidesWith(seeker2)) {
                    seeker1.destroy();
                    seeker2.destroy();

                    toRemove.add(seeker1);
                    toRemove.add(seeker2);

                    Vector2 collisionPoint = new Vector2(seeker1.getPosition())
                        .add(seeker2.getPosition())
                        .scl(0.5f);

                    explosions.add(new Explosion(collisionPoint.x, collisionPoint.y));

                    if (collisionSound != null) {
                        collisionSound.play(0.5f);
                    }

                    totalDestroyed += 2;
                }
            }
        }

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

        if (shootSound != null) shootSound.dispose();
        if (hitSound != null) hitSound.dispose();
        if (collisionSound != null) collisionSound.dispose();
    }
}
