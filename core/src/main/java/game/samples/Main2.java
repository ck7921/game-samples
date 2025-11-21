package game.samples;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class Main2 extends ApplicationAdapter {

    private ShapeRenderer shapeRenderer;
    private Color color;
    private Rectangle rect;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        color = new Color(1f, 0f, 0f, 1f);
        rect = new Rectangle(300, 200, 200, 200);
    }

    @Override
    public void render() {

        // clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }

}
