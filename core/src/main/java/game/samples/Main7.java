package game.samples;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.LinkedList;
import java.util.Queue;

public class Main7 extends ApplicationAdapter {

    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private Color color;

    private Rectangle rect;
    private float rectX, rectY;
    private int rectDim = 30;

    // way points
    private Queue<Vector2> waypoints = new LinkedList<>();
    private float speed = 30;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        color = new Color(1f, 0f, 0f, 1f);
        rectX = 300;
        rectY = 200;
        rect = new Rectangle(rectX, rectY, rectDim, rectDim);

        // InputProcessor clicks & scroll wheel
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (waypoints.size() < 5) {
                    if (screenX > 35 + (rectDim / 2)
                        && screenX < 730 + 35 - (rectDim / 2)
                        && 600 - screenY > 60 + (rectDim / 2)
                        && screenY > 60 + (rectDim / 2)) {
                        waypoints.add(new Vector2(screenX, 600 - screenY));
                    }
                }
                return true;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                speed -= (int) amountY;
                return true;
            }
        });

    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        updateMovement(deltaTime);

        // clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // draw rectangle
        shapeRenderer.setColor(color);
        shapeRenderer.rect(rect.x - (rectDim / 2), rect.y - (rectDim / 2), rect.width, rect.height);

        // draw border
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(20, 40, 760, 15); // bottom: w: 720 h: 430
        shapeRenderer.rect(20, 540, 760, 15); // top
        shapeRenderer.rect(20, 40, 15, 500); // left
        shapeRenderer.rect(765, 40, 15, 500); // right
        // draw collider line
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(35, 55, 1, 485); // left
        shapeRenderer.rect(765, 55, 1, 485); // right
        shapeRenderer.rect(35, 540, 730, 1); // top
        shapeRenderer.rect(35, 55, 730, 1); // bottom

        // draw waypoints
        shapeRenderer.setColor(Color.OLIVE);
        for (Vector2 waypoint : waypoints) {
            shapeRenderer.circle(waypoint.x, waypoint.y, 10);
        }

        shapeRenderer.end();

        // draw text
        batch.begin();
        font.draw(batch, "Click to set waypoint, scroll for speed. Speed=" + speed + ", waypoints: " + waypoints.size(), 50, 580);
        batch.end();

    }

    private void updateMovement(float deltaTime) {
        if (waypoints.isEmpty()) return;

        final Vector2 position = new Vector2(rect.x, rect.y);
        final float distance = position.dst(waypoints.peek()); // dst() = distance

        if (distance < 1f) {
            rect.setPosition(waypoints.peek().x, waypoints.peek().y);
            waypoints.poll();
            return;
        }

        float moveDistance = speed * deltaTime;
        if (moveDistance > distance) {
            moveDistance = distance;
        }

        Vector2 direction = new Vector2(waypoints.peek().x, waypoints.peek().y).sub(position).nor();
        Vector2 delta = new Vector2(direction.x * moveDistance, direction.y * moveDistance);
        rect.setPosition(rect.x + delta.x, rect.y + delta.y);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }

}
