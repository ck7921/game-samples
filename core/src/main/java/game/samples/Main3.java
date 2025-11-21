package game.samples;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class Main3 extends ApplicationAdapter {

    private ShapeRenderer shapeRenderer;
    private Color color;

    private Rectangle rect;
    private float rectX, rectY;
    private float moveSpeed = 3f;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        color = new Color(1f, 0f, 0f, 1f);
        rectX = 300;
        rectY = 200;
        rect = new Rectangle(rectX, rectY, 200, 200);
    }

    @Override
    public void render() {
        handleInput();

        // clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
        shapeRenderer.end();
    }

    private void handleInput() {
        // Bewegung mit Cursor-Tasten
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            rectY += moveSpeed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            rectY -= moveSpeed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            rectX -= moveSpeed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            rectX += moveSpeed;
        }

        // Rechteck-Position aktualisieren
        rect.setPosition(rectX, rectY);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }

}
