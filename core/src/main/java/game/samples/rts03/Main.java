package game.samples.rts03;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * turret towers and player ship
 *
 */
public class Main extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    private OrthographicCamera worldCamera;
    private Viewport worldViewport;
    private ScrollingCameraController cameraController;

    private OrthographicCamera uiCamera;
    private Viewport uiViewport;

    private static final float MAP_WIDTH = 3200f;
    private static final float MAP_HEIGHT = 2400f;

    private PlayerShip player;
    private List<TurretTower> turrets;
    private List<Bullet> bullets;

    private boolean showRanges;
    private boolean placingTurret;
    private Vector2 ghostTurretPos;

    // Statistiken
    private int totalShots;
    private int totalHits;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        worldCamera = new OrthographicCamera();
        worldViewport = new ScreenViewport(worldCamera);

        uiCamera = new OrthographicCamera();
        uiViewport = new ScreenViewport(uiCamera);

        player = new PlayerShip(MAP_WIDTH / 2f, MAP_HEIGHT / 2f);

        cameraController = new ScrollingCameraController(worldCamera, MAP_WIDTH, MAP_HEIGHT);
        worldCamera.position.set(player.getPosition().x, player.getPosition().y, 0);
        worldCamera.update();

        turrets = new ArrayList<>();
        bullets = new ArrayList<>();

        // Start-Türme
        turrets.add(new TurretTower(400, 300));
        turrets.add(new TurretTower(800, 600));
        turrets.add(new TurretTower(1200, 400));
        turrets.add(new TurretTower(1600, 800));
        turrets.add(new TurretTower(2000, 1200));
        turrets.add(new TurretTower(2400, 600));
        turrets.add(new TurretTower(2800, 1400));

        showRanges = true;
        placingTurret = false;
        ghostTurretPos = new Vector2();

        totalShots = 0;
        totalHits = 0;

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                if (placingTurret) {
                    Vector3 worldCoords = worldCamera.unproject(new Vector3(screenX, screenY, 0));
                    ghostTurretPos.set(worldCoords.x, worldCoords.y);
                }
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.LEFT && placingTurret) {
                    Vector3 worldCoords = worldCamera.unproject(new Vector3(screenX, screenY, 0));
                    turrets.add(new TurretTower(worldCoords.x, worldCoords.y));
                    System.out.println("Turret placed at: " + worldCoords.x + ", " + worldCoords.y);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        handleInput(deltaTime);
        updateGame(deltaTime);

        Gdx.gl.glClearColor(0.1f, 0.15f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // === WORLD RENDERING ===
        worldCamera.update();
        shapeRenderer.setProjectionMatrix(worldCamera.combined);
        batch.setProjectionMatrix(worldCamera.combined);

        // Map
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.15f, 0.2f, 0.15f, 1);
        shapeRenderer.rect(0, 0, MAP_WIDTH, MAP_HEIGHT);
        drawGrid();
        shapeRenderer.end();

        // Turret Ranges
        if (showRanges) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (TurretTower turret : turrets) {
                turret.renderRanges(shapeRenderer);
            }
            shapeRenderer.end();
        }

        // Turrets
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (TurretTower turret : turrets) {
            turret.render(shapeRenderer);
        }

        // Ghost Turret
        if (placingTurret) {
            shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.5f);
            shapeRenderer.rect(ghostTurretPos.x - 12.5f, ghostTurretPos.y - 12.5f, 25, 25);
        }

        // Bullets
        for (Bullet bullet : bullets) {
            bullet.render(shapeRenderer);
        }

        shapeRenderer.end();

        // Range Outlines
        if (showRanges) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(1);
            for (TurretTower turret : turrets) {
                turret.renderRangeOutlines(shapeRenderer);
            }
            shapeRenderer.end();
        }

        // Player
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        player.render(shapeRenderer);
        shapeRenderer.end();

        // === UI RENDERING ===
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);

        renderUI();
    }

    private void handleInput(float deltaTime) {
        if (!player.isAlive()) return;

        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            player.moveForward(deltaTime);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            player.moveBackward(deltaTime);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            player.turnLeft(deltaTime);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            player.turnRight(deltaTime);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            showRanges = !showRanges;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            placingTurret = !placingTurret;
            System.out.println("Turret placement: " + (placingTurret ? "ON" : "OFF"));
        }
    }

    private void updateGame(float deltaTime) {
        // Update Player
        player.update(deltaTime);

        // Update Camera
        cameraController.update(player.getPosition());

        // Update Turrets und Schüsse
        for (TurretTower turret : turrets) {
            turret.update(deltaTime, player);

            // Turret versucht zu schießen
            if (turret.hasTarget() && turret.isAimedAtTarget() &&
                turret.isInFireRange(player) && turret.canFire()) {

                Bullet bullet = turret.fire(player);
                if (bullet != null) {
                    bullets.add(bullet);
                    totalShots++;
                }
            }
        }

        // Update Bullets
        Iterator<Bullet> bulletIter = bullets.iterator();
        while (bulletIter.hasNext()) {
            Bullet bullet = bulletIter.next();
            bullet.update(deltaTime);

            // Prüfe Treffer
            if (bullet.checkHit(player)) {
                player.takeDamage(bullet.getDamage());
                bullet.destroy();
                totalHits++;
            }

            // Entferne tote Bullets
            if (!bullet.isAlive()) {
                bulletIter.remove();
            }
        }
    }

    private void drawGrid() {
        shapeRenderer.setColor(0.2f, 0.25f, 0.2f, 0.3f);
        float gridSize = 100f;

        for (float x = 0; x <= MAP_WIDTH; x += gridSize) {
            shapeRenderer.rectLine(x, 0, x, MAP_HEIGHT, 1);
        }

        for (float y = 0; y <= MAP_HEIGHT; y += gridSize) {
            shapeRenderer.rectLine(0, y, MAP_WIDTH, y, 1);
        }
    }

    private void renderUI() {
        batch.begin();

        font.setColor(Color.WHITE);
        font.draw(batch, "WASD/Arrows = Move & Rotate", 10, uiViewport.getScreenHeight() - 10);
        font.draw(batch, "T = Place Turret (click to place)", 10, uiViewport.getScreenHeight() - 35);
        font.draw(batch, "R = Toggle Ranges", 10, uiViewport.getScreenHeight() - 60);

        font.setColor(Color.YELLOW);
        font.draw(batch, "Health: " + player.getCurrentHealth() + "/" + player.getMaxHealth(),
            10, uiViewport.getScreenHeight() - 100);

        batch.end();

        // Health Bar
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float barWidth = 200f;
        float barHeight = 20f;
        float barX = 10f;
        float barY = uiViewport.getScreenHeight() - 130f;

        shapeRenderer.setColor(0.3f, 0, 0, 1);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        float healthPercent = (float) player.getCurrentHealth() / player.getMaxHealth();
        if (healthPercent > 0.6f) {
            shapeRenderer.setColor(0, 1, 0, 1);
        } else if (healthPercent > 0.3f) {
            shapeRenderer.setColor(1, 1, 0, 1);
        } else {
            shapeRenderer.setColor(1, 0, 0, 1);
        }
        shapeRenderer.rect(barX, barY, barWidth * healthPercent, barHeight);

        shapeRenderer.end();

        batch.begin();

        font.setColor(Color.CYAN);
        font.draw(batch, "Turrets: " + turrets.size(), 10, uiViewport.getScreenHeight() - 160);
        font.draw(batch, "Bullets: " + bullets.size(), 10, uiViewport.getScreenHeight() - 185);
        font.draw(batch, "Shots: " + totalShots + " | Hits: " + totalHits,
            10, uiViewport.getScreenHeight() - 210);

        if (totalShots > 0) {
            float accuracy = (float)totalHits / totalShots * 100f;
            font.draw(batch, String.format("Accuracy: %.1f%%", accuracy),
                10, uiViewport.getScreenHeight() - 235);
        }

        font.draw(batch, "Position: " + (int)player.getPosition().x + ", " + (int)player.getPosition().y,
            10, uiViewport.getScreenHeight() - 260);

        if (placingTurret) {
            font.setColor(Color.GREEN);
            font.draw(batch, "PLACING TURRET - Click to place",
                uiViewport.getScreenWidth() / 2f - 150, uiViewport.getScreenHeight() - 10);
        }

        if (!player.isAlive()) {
            font.setColor(Color.RED);
            font.getData().setScale(3f);
            font.draw(batch, "GAME OVER!",
                uiViewport.getScreenWidth() / 2f - 100, uiViewport.getScreenHeight() / 2f);
            font.getData().setScale(1.5f);
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height, true);
        uiViewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
