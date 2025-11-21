package game.samples;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.LinkedList;
import java.util.Queue;

public class Main8 extends ApplicationAdapter {

    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    // spline
    private CatmullRomSpline<Vector2> spline;
    private Vector2[] controlPoints;


    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();

        // Kontrollpunkte definieren
        controlPoints = new Vector2[] {
            new Vector2(100, 300),
            new Vector2(200, 500),
            new Vector2(400, 450),
            new Vector2(600, 200),
            new Vector2(650, 250),
            new Vector2(700, 400)
        };

        // Catmull-Rom Spline erstellen
        spline = new CatmullRomSpline<>(controlPoints, false); // false = nicht geschlossen

    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        // clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // draw spline
        shapeRenderer.setColor(1, 0, 0, 1); // Rot
        for (Vector2 point : controlPoints) {
            shapeRenderer.circle(point.x, point.y, 8);
        }

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

        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0, 1, 0, 1); // Grün
        Gdx.gl.glLineWidth(3);

        Vector2 point = new Vector2();
        Vector2 previousPoint = new Vector2();

        // Zeichne Spline mit vielen kleinen Linien-Segmenten
        System.out.println("---");
        int segments = 100;
        for (int i = 0; i <= segments; i++) {
            float t = (float) i / segments; // 0.0 bis 1.0
            spline.valueAt(point, t);

            System.out.println(point);

            if (i > 0) {
                shapeRenderer.line(previousPoint.x, previousPoint.y, point.x, point.y);
            }

            previousPoint.set(point);
        }

        shapeRenderer.end();

        // draw text
        batch.begin();
        font.draw(batch, "Click to set" , 50, 580);
        batch.end();

    }



    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }

}
