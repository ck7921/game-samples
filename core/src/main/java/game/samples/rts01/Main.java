package game.samples.rts01;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

/**
 * navigation over a map
 */
public class Main extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;
    private RTSCameraController cameraController;

    // Map
    private static final float MAP_WIDTH = 3200f;  // 4x viewport
    private static final float MAP_HEIGHT = 2400f; // 4x viewport

    // Gebäude
    private List<Building> buildings;
    private Building selectedBuilding;

    // Selection Box (für zukünftige Mehrfachauswahl)
    private boolean isSelecting;
    private Vector3 selectionStart;
    private Vector3 selectionEnd;
    private Rectangle selectionBox;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        // Kamera Setup
        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);

        // Starte in der Mitte der Map
        camera.position.set(MAP_WIDTH / 2f, MAP_HEIGHT / 2f, 0);
        camera.update();

        // Kamera-Controller
        cameraController = new RTSCameraController(camera, MAP_WIDTH, MAP_HEIGHT);
        Gdx.input.setInputProcessor(cameraController);

        buildings = new ArrayList<>();
        selectionBox = new Rectangle();
        isSelecting = false;
        selectionStart = new Vector3();
        selectionEnd = new Vector3();

        // Erstelle Gebäude
        generateBuildings();
    }

    private void generateBuildings() {
        // Platziere verschiedene Gebäude auf der Map

        // Häuser (klein)
        for (int i = 0; i < 20; i++) {
            float x = 100 + (float)Math.random() * (MAP_WIDTH - 200);
            float y = 100 + (float)Math.random() * (MAP_HEIGHT - 200);
            buildings.add(new Building(x, y, 60, 60, Building.BuildingType.HOUSE));
        }

        // Fabriken (mittel)
        for (int i = 0; i < 10; i++) {
            float x = 100 + (float)Math.random() * (MAP_WIDTH - 300);
            float y = 100 + (float)Math.random() * (MAP_HEIGHT - 300);
            buildings.add(new Building(x, y, 120, 100, Building.BuildingType.FACTORY));
        }

        // Kasernen (mittel)
        for (int i = 0; i < 8; i++) {
            float x = 100 + (float)Math.random() * (MAP_WIDTH - 300);
            float y = 100 + (float)Math.random() * (MAP_HEIGHT - 300);
            buildings.add(new Building(x, y, 100, 120, Building.BuildingType.BARRACKS));
        }

        // Türme (klein, hoch)
        for (int i = 0; i < 15; i++) {
            float x = 100 + (float)Math.random() * (MAP_WIDTH - 200);
            float y = 100 + (float)Math.random() * (MAP_HEIGHT - 200);
            buildings.add(new Building(x, y, 40, 80, Building.BuildingType.TOWER));
        }
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        handleInput();
        cameraController.update(deltaTime);

        Gdx.gl.glClearColor(0.1f, 0.3f, 0.1f, 1); // Grüner Hintergrund (Gras)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // Zeichne Map-Hintergrund
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Map-Grenze
        shapeRenderer.setColor(0.15f, 0.35f, 0.15f, 1);
        shapeRenderer.rect(0, 0, MAP_WIDTH, MAP_HEIGHT);

        // Grid (optional)
        drawGrid();

        shapeRenderer.end();

        // Zeichne Gebäude
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Building building : buildings) {
            building.render(shapeRenderer);
        }
        shapeRenderer.end();

        // Zeichne Gebäude-Umrisse
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        for (Building building : buildings) {
            building.renderOutline(shapeRenderer);
        }
        shapeRenderer.end();

        // Zeichne Selection Box
        if (isSelecting) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0, 1, 0, 1);
            Gdx.gl.glLineWidth(2);
            shapeRenderer.rect(selectionBox.x, selectionBox.y, selectionBox.width, selectionBox.height);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 1, 0, 0.2f);
            shapeRenderer.rect(selectionBox.x, selectionBox.y, selectionBox.width, selectionBox.height);
            shapeRenderer.end();
        }

        // Zeichne Labels
        batch.begin();
        for (Building building : buildings) {
            // Nur Labels für Gebäude in der Nähe zeichnen (Performance)
            if (building.isSelected()) {
                building.renderLabel(batch, font);
            }
        }
        batch.end();

        // UI (Fixed Position - nicht mit Kamera bewegen)
        renderUI();
    }

    private void drawGrid() {
        shapeRenderer.setColor(0.2f, 0.4f, 0.2f, 0.3f);

        float gridSize = 100f;

        // Vertikale Linien
        for (float x = 0; x <= MAP_WIDTH; x += gridSize) {
            shapeRenderer.rectLine(x, 0, x, MAP_HEIGHT, 1);
        }

        // Horizontale Linien
        for (float y = 0; y <= MAP_HEIGHT; y += gridSize) {
            shapeRenderer.rectLine(0, y, MAP_WIDTH, y, 1);
        }
    }

    private void renderUI() {
        // UI verwendet Screen-Koordinaten, nicht World-Koordinaten
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        font.setColor(Color.WHITE);
        font.draw(batch, "WASD/Pfeiltasten = Kamera bewegen", 10, viewport.getScreenHeight() - 10);
        font.draw(batch, "Rechte Maustaste/Mausrad = Panning", 10, viewport.getScreenHeight() - 35);
        font.draw(batch, "Scrollrad = Zoom", 10, viewport.getScreenHeight() - 60);
        font.draw(batch, "E = Edge Scrolling Toggle", 10, viewport.getScreenHeight() - 85);

        font.setColor(Color.YELLOW);
        font.draw(batch, "Zoom: " + (cameraController.getCurrentZoomIndex() + 1) + "/5 (" +
            String.format("%.2f", cameraController.getCurrentZoom()) + "x)", 10, viewport.getScreenHeight() - 125);
        font.draw(batch, String.format("Kamera: (%.0f, %.0f)", camera.position.x, camera.position.y),
            10, viewport.getScreenHeight() - 150);
        font.draw(batch, "Gebäude: " + buildings.size(), 10, viewport.getScreenHeight() - 175);
        font.draw(batch, "Edge Scroll: " + (cameraController.isEdgeScrollEnabled() ? "AN" : "AUS"),
            10, viewport.getScreenHeight() - 200);

        if (selectedBuilding != null) {
            font.setColor(Color.GREEN);
            font.draw(batch, "Selektiert: " + selectedBuilding.getName(), 10, viewport.getScreenHeight() - 240);
        }

        font.setColor(Color.WHITE);
        batch.end();
    }

    private void handleInput() {
        // Gebäude-Selektion mit linker Maustaste
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector3 worldCoords = cameraController.screenToWorld(Gdx.input.getX(), Gdx.input.getY());

            // Deselektiere alle
            for (Building building : buildings) {
                building.setSelected(false);
            }
            selectedBuilding = null;

            // Finde geklicktes Gebäude
            for (Building building : buildings) {
                if (building.contains(worldCoords.x, worldCoords.y)) {
                    building.setSelected(true);
                    selectedBuilding = building;
                    System.out.println("Selektiert: " + building.getName() +
                        " bei (" + (int)building.getBounds().x + ", " + (int)building.getBounds().y + ")");
                    break;
                }
            }
        }

        // Edge Scrolling Toggle
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            cameraController.setEdgeScrollEnabled(!cameraController.isEdgeScrollEnabled());
        }

        // ESC zum Deselektieren
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            for (Building building : buildings) {
                building.setSelected(false);
            }
            selectedBuilding = null;
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
