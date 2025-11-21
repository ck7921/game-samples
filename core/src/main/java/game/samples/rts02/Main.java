package game.samples.rts02;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
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
 * building placement
 */
public class Main extends ApplicationAdapter implements InputProcessor {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    // World Camera (für Map und Gebäude)
    private OrthographicCamera worldCamera;
    private Viewport worldViewport;
    private RTSCameraController cameraController;

    // UI Camera (für HUD - unabhängig vom Zoom)
    private OrthographicCamera uiCamera;
    private Viewport uiViewport;

    private static final float MAP_WIDTH = 3200f;
    private static final float MAP_HEIGHT = 2400f;

    private List<Building> buildings;
    private Building selectedBuilding;

    private BuildingPlacementHUD placementHUD;
    private Building.BuildingType selectedBuildingType;
    private Building ghostBuilding;
    private boolean isPlacingBuilding;

    private int money;

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

        // World Camera für Map und Gebäude
        worldCamera = new OrthographicCamera();
        worldViewport = new ScreenViewport(worldCamera);
        worldCamera.position.set(MAP_WIDTH / 2f, MAP_HEIGHT / 2f, 0);
        worldCamera.update();

        // UI Camera für HUD (bleibt immer gleich)
        uiCamera = new OrthographicCamera();
        uiViewport = new ScreenViewport(uiCamera);

        cameraController = new RTSCameraController(worldCamera, MAP_WIDTH, MAP_HEIGHT);

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this);
        multiplexer.addProcessor(cameraController);
        Gdx.input.setInputProcessor(multiplexer);

        buildings = new ArrayList<>();
        placementHUD = new BuildingPlacementHUD();
        selectionBox = new Rectangle();
        isSelecting = false;
        selectionStart = new Vector3();
        selectionEnd = new Vector3();

        isPlacingBuilding = false;
        selectedBuildingType = null;
        ghostBuilding = null;

        money = 1000;

        generateBuildings();
    }

    private void generateBuildings() {
        for (int i = 0; i < 15; i++) {
            float x = 100 + (float)Math.random() * (MAP_WIDTH - 200);
            float y = 100 + (float)Math.random() * (MAP_HEIGHT - 200);
            buildings.add(new Building(x, y, 60, 60, Building.BuildingType.HOUSE));
        }

        for (int i = 0; i < 8; i++) {
            float x = 100 + (float)Math.random() * (MAP_WIDTH - 300);
            float y = 100 + (float)Math.random() * (MAP_HEIGHT - 300);
            buildings.add(new Building(x, y, 120, 100, Building.BuildingType.FACTORY));
        }
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        handleKeyboard();
        updateGhostBuilding();
        cameraController.update(deltaTime);

        Gdx.gl.glClearColor(0.1f, 0.3f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // === WORLD RENDERING (mit World Camera) ===
        worldCamera.update();
        shapeRenderer.setProjectionMatrix(worldCamera.combined);
        batch.setProjectionMatrix(worldCamera.combined);

        // Zeichne Map
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.15f, 0.35f, 0.15f, 1);
        shapeRenderer.rect(0, 0, MAP_WIDTH, MAP_HEIGHT);
        drawGrid();
        shapeRenderer.end();

        // Zeichne Gebäude
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Building building : buildings) {
            building.render(shapeRenderer);
        }

        if (ghostBuilding != null) {
            ghostBuilding.render(shapeRenderer);
        }

        shapeRenderer.end();

        // Zeichne Umrisse
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        for (Building building : buildings) {
            building.renderOutline(shapeRenderer);
        }

        if (ghostBuilding != null) {
            ghostBuilding.renderOutline(shapeRenderer);
        }

        shapeRenderer.end();

        // Labels
        batch.begin();
        for (Building building : buildings) {
            if (building.isSelected()) {
                building.renderLabel(batch, font);
            }
        }
        batch.end();

        // === UI RENDERING (mit UI Camera - unabhängig vom Zoom) ===
        uiCamera.update();
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        batch.setProjectionMatrix(uiCamera.combined);

        renderUI();
        placementHUD.render(shapeRenderer, batch, font);
    }

    private void updateGhostBuilding() {
        if (!isPlacingBuilding || selectedBuildingType == null) {
            ghostBuilding = null;
            return;
        }

        Vector3 worldCoords = cameraController.screenToWorld(Gdx.input.getX(), Gdx.input.getY());

        float width = Building.getDefaultWidth(selectedBuildingType);
        float height = Building.getDefaultHeight(selectedBuildingType);

        float x = worldCoords.x - width / 2f;
        float y = worldCoords.y - height / 2f;

        if (ghostBuilding == null) {
            ghostBuilding = new Building(x, y, width, height, selectedBuildingType);
            ghostBuilding.setGhost(true);
        } else {
            ghostBuilding.setPosition(x, y);
        }
    }

    private void tryPlaceBuilding() {
        if (ghostBuilding == null) return;

        int cost = placementHUD.getCost(selectedBuildingType);

        if (money < cost) {
            System.out.println("Nicht genug Geld! Benötigt: $" + cost + ", Verfügbar: $" + money);
            return;
        }

        for (Building building : buildings) {
            if (ghostBuilding.overlaps(building)) {
                System.out.println("Gebäude überlappen sich!");
                return;
            }
        }

        Rectangle bounds = ghostBuilding.getBounds();
        if (bounds.x < 0 || bounds.y < 0 ||
            bounds.x + bounds.width > MAP_WIDTH ||
            bounds.y + bounds.height > MAP_HEIGHT) {
            System.out.println("Außerhalb der Map!");
            return;
        }

        Building newBuilding = new Building(
            bounds.x, bounds.y, bounds.width, bounds.height, selectedBuildingType
        );
        buildings.add(newBuilding);

        money -= cost;

        System.out.println("Gebäude platziert! Verbleibend: $" + money);
    }

    private void handleKeyboard() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
            if (placementHUD.isVisible()) {
                placementHUD.hide();
                cancelPlacement();
            } else {
                placementHUD.show();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            placementHUD.hide();
            cancelPlacement();
        }
    }

    private void cancelPlacement() {
        isPlacingBuilding = false;
        selectedBuildingType = null;
        ghostBuilding = null;
    }

    private void drawGrid() {
        shapeRenderer.setColor(0.2f, 0.4f, 0.2f, 0.3f);
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
        font.draw(batch, "B = Build Menu", 10, uiViewport.getScreenHeight() - 10);
        font.draw(batch, "WASD/Arrows = Move", 10, uiViewport.getScreenHeight() - 35);
        font.draw(batch, "Scroll = Zoom", 10, uiViewport.getScreenHeight() - 60);
        font.draw(batch, "ESC = Cancel", 10, uiViewport.getScreenHeight() - 85);

        font.setColor(Color.YELLOW);
        font.draw(batch, "Money: $" + money, 10, uiViewport.getScreenHeight() - 125);
        font.draw(batch, "Buildings: " + buildings.size(), 10, uiViewport.getScreenHeight() - 150);
        font.draw(batch, "Zoom: " + (cameraController.getCurrentZoomIndex() + 1) + "/5",
            10, uiViewport.getScreenHeight() - 175);

        if (isPlacingBuilding && selectedBuildingType != null) {
            font.setColor(Color.GREEN);
            font.draw(batch, "Placing: " + selectedBuildingType.name(), 10, uiViewport.getScreenHeight() - 215);
            font.draw(batch, "Click to place", 10, uiViewport.getScreenHeight() - 240);
        }

        font.setColor(Color.WHITE);
        batch.end();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            // Prüfe HUD-Klick (verwende Screen-Koordinaten direkt)
            if (placementHUD.isVisible()) {
                // Konvertiere Y-Koordinate (Bildschirmkoordinaten sind von oben)
                float uiY = uiViewport.getScreenHeight() - screenY;
                Building.BuildingType clickedType = placementHUD.checkButtonClick(screenX, uiY);

                if (clickedType != null) {
                    selectedBuildingType = clickedType;
                    isPlacingBuilding = true;
                    placementHUD.hide();
                    System.out.println("Ausgewählt: " + clickedType);
                    return true;
                }
            }

            if (isPlacingBuilding) {
                tryPlaceBuilding();
                return true;
            }

            // Gebäude selektieren (verwende World-Koordinaten)
            Vector3 worldCoords = cameraController.screenToWorld(screenX, screenY);

            for (Building building : buildings) {
                building.setSelected(false);
            }
            selectedBuilding = null;

            for (Building building : buildings) {
                if (building.contains(worldCoords.x, worldCoords.y)) {
                    building.setSelected(true);
                    selectedBuilding = building;
                    break;
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        // Update HUD Hover (konvertiere Y-Koordinate)
        float uiY = uiViewport.getScreenHeight() - screenY;
        placementHUD.updateHover(screenX, uiY);
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height, true);
        uiViewport.update(width, height, true);
        placementHUD.updateButtonPositions(width, height);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
