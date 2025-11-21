package game.samples.nav01;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

public class Main extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    private OrthographicCamera worldCamera;
    private Viewport worldViewport;

    private OrthographicCamera uiCamera;
    private Viewport uiViewport;

    private static final float MAP_WIDTH = 3200f;
    private static final float MAP_HEIGHT = 2400f;

    private DotGrid grid;
    private List<GridUnit> units;

    private float currentSpacing;
    private float cameraSpeed = 300f;

    private boolean showDebug = true;

    // FPS Counter für Debugging
    private float fpsTimer = 0;
    private int frameCount = 0;
    private int fps = 0;

    @Override
    public void create() {
        // WICHTIG: Setze kontinuierliches Rendering
        Gdx.graphics.setContinuousRendering(true);
        Gdx.graphics.setVSync(true);

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        worldCamera = new OrthographicCamera();
        worldViewport = new ScreenViewport(worldCamera);
        worldCamera.position.set(MAP_WIDTH / 2f, MAP_HEIGHT / 2f, 0);
        worldCamera.update();

        uiCamera = new OrthographicCamera();
        uiViewport = new ScreenViewport(uiCamera);

        currentSpacing = 50f;
        grid = new DotGrid(MAP_WIDTH, MAP_HEIGHT, currentSpacing);
        units = new ArrayList<>();

        System.out.println("===========================================");
        System.out.println("Grid-System initialisiert!");
        System.out.println("Continuous Rendering: " + Gdx.graphics.isContinuousRendering());
        System.out.println("Drücke H um Einheiten zu spawnen");
        System.out.println("===========================================");
    }

    @Override
    public void render() {
        // FPS Counter
        float deltaTime = Gdx.graphics.getDeltaTime();
        fpsTimer += deltaTime;
        frameCount++;

        if (fpsTimer >= 1.0f) {
            fps = frameCount;
            frameCount = 0;
            fpsTimer = 0;
        }

        // UPDATE PHASE
        handleInput(deltaTime);
        updateUnits(deltaTime);

        // RENDER PHASE
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // === WORLD RENDERING ===
        worldCamera.update();
        shapeRenderer.setProjectionMatrix(worldCamera.combined);
        batch.setProjectionMatrix(worldCamera.combined);

        // Hintergrund
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 1);
        shapeRenderer.rect(0, 0, MAP_WIDTH, MAP_HEIGHT);
        shapeRenderer.end();

        // Grid
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        grid.render(shapeRenderer);
        shapeRenderer.end();

        // Debug (Ziele und Pfade)
        if (showDebug && units.size() > 0) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (GridUnit unit : units) {
                unit.renderDebug(shapeRenderer);
            }
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(2);
            for (GridUnit unit : units) {
                unit.renderDebug(shapeRenderer);
            }
            shapeRenderer.end();
        }

        // Einheiten
        if (units.size() > 0) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (GridUnit unit : units) {
                unit.render(shapeRenderer);
            }
            shapeRenderer.end();
        }

        // Map-Rahmen
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(0, 0, MAP_WIDTH, MAP_HEIGHT);
        shapeRenderer.end();

        // === UI RENDERING ===
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.setProjectionMatrix(uiCamera.combined);

        renderUI();
    }

    private void handleInput(float deltaTime) {
        // Kamera bewegen
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            worldCamera.position.y += cameraSpeed * deltaTime;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            worldCamera.position.y -= cameraSpeed * deltaTime;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            worldCamera.position.y -= cameraSpeed * deltaTime;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            worldCamera.position.x -= cameraSpeed * deltaTime;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            worldCamera.position.x += cameraSpeed * deltaTime;
        }

        // Einheit spawnen
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            spawnRandomUnit();
        }

        // Spacing ändern
        if (Gdx.input.isKeyJustPressed(Input.Keys.PLUS) || Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)) {
            currentSpacing += 10f;
            if (currentSpacing > 100f) currentSpacing = 100f;
            grid = new DotGrid(MAP_WIDTH, MAP_HEIGHT, currentSpacing);
            units.clear();
            System.out.println("Grid-Abstand: " + currentSpacing + " px");
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) {
            currentSpacing -= 10f;
            if (currentSpacing < 20f) currentSpacing = 20f;
            grid = new DotGrid(MAP_WIDTH, MAP_HEIGHT, currentSpacing);
            units.clear();
            System.out.println("Grid-Abstand: " + currentSpacing + " px");
        }

        // Debug Toggle
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            showDebug = !showDebug;
            System.out.println("Debug: " + (showDebug ? "AN" : "AUS"));
        }

        // Alle Einheiten löschen
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            System.out.println("Alle " + units.size() + " Einheiten gelöscht");
            units.clear();
        }

        // Kamera in Grenzen halten
        float halfWidth = worldCamera.viewportWidth / 2f;
        float halfHeight = worldCamera.viewportHeight / 2f;

        worldCamera.position.x = Math.max(halfWidth, Math.min(MAP_WIDTH - halfWidth, worldCamera.position.x));
        worldCamera.position.y = Math.max(halfHeight, Math.min(MAP_HEIGHT - halfHeight, worldCamera.position.y));
    }

    private void spawnRandomUnit() {
        Vector2 randomGridPos = grid.getRandomGridCoordinate();
        GridUnit unit = new GridUnit(
            randomGridPos.x,
            randomGridPos.y,
            currentSpacing,
            MAP_WIDTH,
            MAP_HEIGHT
        );
        units.add(unit);

        System.out.println("===========================================");
        System.out.println("Einheit #" + units.size() + " gespawnt bei Grid (" +
            (int)randomGridPos.x + ", " + (int)randomGridPos.y + ")");
        System.out.println("World Position: (" + unit.getPosition().x + ", " + unit.getPosition().y + ")");
        System.out.println("===========================================");
    }

    private void updateUnits(float deltaTime) {
        if (units.isEmpty()) return;

        for (GridUnit unit : units) {
            unit.update(deltaTime);
        }
    }

    private void renderUI() {
        batch.begin();

        font.setColor(Color.WHITE);
        font.draw(batch, "Grid-basierte Einheiten", 10, uiViewport.getScreenHeight() - 10);
        font.draw(batch, "WASD = Kamera", 10, uiViewport.getScreenHeight() - 35);
        font.draw(batch, "H = Einheit spawnen", 10, uiViewport.getScreenHeight() - 60);
        font.draw(batch, "C = Alle löschen", 10, uiViewport.getScreenHeight() - 85);
        font.draw(batch, "F = Debug Toggle", 10, uiViewport.getScreenHeight() - 110);
        font.draw(batch, "+/- = Grid-Abstand", 10, uiViewport.getScreenHeight() - 135);

        font.setColor(Color.YELLOW);
        font.draw(batch, "Grid-Abstand: " + (int)currentSpacing + " px", 10, uiViewport.getScreenHeight() - 175);
        font.draw(batch, "Grid-Punkte: " + grid.getPointCount(), 10, uiViewport.getScreenHeight() - 200);
        font.draw(batch, "Grid-Größe: " + grid.getGridWidth() + " x " + grid.getGridHeight(),
            10, uiViewport.getScreenHeight() - 225);
        font.draw(batch, "Einheiten: " + units.size(), 10, uiViewport.getScreenHeight() - 250);

        font.setColor(Color.CYAN);
        int rotating = 0;
        int moving = 0;
        for (GridUnit unit : units) {
            if (unit.isRotating()) rotating++;
            if (unit.isMoving()) moving++;
        }
        font.draw(batch, "Rotierend: " + rotating, 10, uiViewport.getScreenHeight() - 290);
        font.draw(batch, "Bewegend: " + moving, 10, uiViewport.getScreenHeight() - 315);
        font.draw(batch, "Debug: " + (showDebug ? "AN" : "AUS"), 10, uiViewport.getScreenHeight() - 340);

        // FPS Anzeige
        font.setColor(Color.GREEN);
        font.draw(batch, "FPS: " + fps, 10, uiViewport.getScreenHeight() - 380);
        font.draw(batch, "DeltaTime: " + String.format("%.3f", Gdx.graphics.getDeltaTime()),
            10, uiViewport.getScreenHeight() - 405);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        System.out.println("Window resized: " + width + "x" + height);
        worldViewport.update(width, height, true);
        uiViewport.update(width, height, true);
    }

    @Override
    public void pause() {
        System.out.println("Game paused");
    }

    @Override
    public void resume() {
        System.out.println("Game resumed");
    }

    @Override
    public void dispose() {
        System.out.println("Disposing resources...");
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
