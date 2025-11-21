package game.samples.rts02;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;


public class RTSCameraController extends InputAdapter {
    private OrthographicCamera camera;

    private float mapWidth;
    private float mapHeight;

    private float[] zoomLevels = {0.5f, 0.75f, 1.0f, 1.5f, 2.0f};
    private int currentZoomIndex = 2;

    private boolean isPanning;
    private Vector2 lastPanPosition;

    private boolean edgeScrollEnabled = true;
    private float edgeScrollSpeed = 300f;
    private float edgeScrollMargin = 50f;

    private float keyboardScrollSpeed = 400f;

    public RTSCameraController(OrthographicCamera camera, float mapWidth, float mapHeight) {
        this.camera = camera;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.lastPanPosition = new Vector2();
        this.isPanning = false;

        updateZoom();
    }

    public void update(float deltaTime) {
        if (edgeScrollEnabled) {
            handleEdgeScrolling(deltaTime);
        }

        handleKeyboardMovement(deltaTime);
        clampCamera();
        camera.update();
    }

    private void handleEdgeScrolling(float deltaTime) {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        float moveAmount = edgeScrollSpeed * deltaTime * camera.zoom;

        if (mouseX < edgeScrollMargin) {
            camera.position.x -= moveAmount;
        }
        if (mouseX > screenWidth - edgeScrollMargin) {
            camera.position.x += moveAmount;
        }
        if (mouseY < edgeScrollMargin) {
            camera.position.y += moveAmount;
        }
        if (mouseY > screenHeight - edgeScrollMargin) {
            camera.position.y -= moveAmount;
        }
    }

    private void handleKeyboardMovement(float deltaTime) {
        float moveAmount = keyboardScrollSpeed * deltaTime * camera.zoom;

        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.position.y += moveAmount;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.position.y -= moveAmount;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.position.x -= moveAmount;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.position.x += moveAmount;
        }
    }

    private void clampCamera() {
        float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
        float effectiveViewportHeight = camera.viewportHeight * camera.zoom;

        // WICHTIG: Prüfe ob Viewport größer als Map ist
        // Wenn ja, zentriere die Kamera auf der Map

        if (effectiveViewportWidth >= mapWidth) {
            // Viewport ist breiter als Map - zentriere horizontal
            camera.position.x = mapWidth / 2f;
        } else {
            // Normale Clamping-Logik
            camera.position.x = MathUtils.clamp(
                camera.position.x,
                effectiveViewportWidth / 2f,
                mapWidth - effectiveViewportWidth / 2f
            );
        }

        if (effectiveViewportHeight >= mapHeight) {
            // Viewport ist höher als Map - zentriere vertikal
            camera.position.y = mapHeight / 2f;
        } else {
            // Normale Clamping-Logik
            camera.position.y = MathUtils.clamp(
                camera.position.y,
                effectiveViewportHeight / 2f,
                mapHeight - effectiveViewportHeight / 2f
            );
        }
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (amountY > 0) {
            currentZoomIndex = Math.min(zoomLevels.length - 1, currentZoomIndex + 1);
        } else {
            currentZoomIndex = Math.max(0, currentZoomIndex - 1);
        }

        updateZoom();
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.MIDDLE || button == Input.Buttons.RIGHT) {
            isPanning = true;
            lastPanPosition.set(screenX, screenY);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.MIDDLE || button == Input.Buttons.RIGHT) {
            isPanning = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (isPanning) {
            float deltaX = (screenX - lastPanPosition.x) * camera.zoom;
            float deltaY = (screenY - lastPanPosition.y) * camera.zoom;

            camera.position.x -= deltaX;
            camera.position.y += deltaY;

            lastPanPosition.set(screenX, screenY);
            return true;
        }
        return false;
    }

    private void updateZoom() {
        camera.zoom = zoomLevels[currentZoomIndex];
        clampCamera();
    }

    public Vector3 screenToWorld(int screenX, int screenY) {
        return camera.unproject(new Vector3(screenX, screenY, 0));
    }

    // Getter/Setter
    public void setEdgeScrollEnabled(boolean enabled) {
        this.edgeScrollEnabled = enabled;
    }

    public boolean isEdgeScrollEnabled() {
        return edgeScrollEnabled;
    }

    public float getCurrentZoom() {
        return camera.zoom;
    }

    public int getCurrentZoomIndex() {
        return currentZoomIndex;
    }

    public int getMaxZoomIndex() {
        return zoomLevels.length - 1;
    }
}
