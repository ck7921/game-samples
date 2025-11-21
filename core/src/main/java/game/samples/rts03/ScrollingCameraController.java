package game.samples.rts03;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class ScrollingCameraController {
    private OrthographicCamera camera;
    private float mapWidth;
    private float mapHeight;

    private float scrollMargin; // Abstand vom Rand wo Kamera scrollt

    public ScrollingCameraController(OrthographicCamera camera, float mapWidth, float mapHeight) {
        this.camera = camera;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.scrollMargin = 100f; // 100 Pixel vom Rand
    }

    public void update(Vector2 playerPosition) {
        float halfWidth = camera.viewportWidth / 2f;
        float halfHeight = camera.viewportHeight / 2f;

        // Horizontal scrolling
        if (playerPosition.x < camera.position.x - halfWidth + scrollMargin) {
            camera.position.x = playerPosition.x + halfWidth - scrollMargin;
        } else if (playerPosition.x > camera.position.x + halfWidth - scrollMargin) {
            camera.position.x = playerPosition.x - halfWidth + scrollMargin;
        }

        // Vertical scrolling
        if (playerPosition.y < camera.position.y - halfHeight + scrollMargin) {
            camera.position.y = playerPosition.y + halfHeight - scrollMargin;
        } else if (playerPosition.y > camera.position.y + halfHeight - scrollMargin) {
            camera.position.y = playerPosition.y - halfHeight + scrollMargin;
        }

        // Clamp camera to map bounds
        camera.position.x = MathUtils.clamp(
            camera.position.x,
            halfWidth,
            mapWidth - halfWidth
        );

        camera.position.y = MathUtils.clamp(
            camera.position.y,
            halfHeight,
            mapHeight - halfHeight
        );

        camera.update();
    }
}

