package game.samples;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

/**
 * demonstrate sound playback
 */
public class Main6 extends ApplicationAdapter {

    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private Color color;

    private Rectangle rect;
    private float rectX, rectY;
    private float moveSpeed = 3f;

    // Sound-Effect
    private Sound collideSound;
    private int collisionOnSide = -1;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        color = new Color(1f, 0f, 0f, 1f);
        rectX = 300;
        rectY = 200;
        rect = new Rectangle(rectX, rectY, 200, 200);
        collideSound = Gdx.audio.newSound(Gdx.files.internal("laser_shooting_sfx.wav")); // laser_shooting_sfx.wav
        collideSound.play(0);
        collideSound.stop();
    }

    @Override
    public void render() {
        handleInput();

        // clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // draw rectangle
        shapeRenderer.setColor(color);
        shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
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

        // draw text
        batch.begin();
        font.draw(batch, "Now with sound. Try to move outside of border.", 50, 580);
        batch.end();

    }

    private void handleInput() {
        // Bewegung mit Cursor-Tasten
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            final boolean isInside = isRectInsideRect(rectX, rectY+moveSpeed, 200, 200, 35, 55, 730, 485);
            if(isInside) rectY += moveSpeed;
            else {
                rectY = 55 + 285;
                playSound(0);
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            final boolean isInside = isRectInsideRect(rectX, rectY-moveSpeed, 200, 200, 35, 55, 730, 485);
            if(isInside) rectY -= moveSpeed;
            else {
                rectY = 55;
                playSound(1);
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            final boolean isInside = isRectInsideRect(rectX-moveSpeed, rectY, 200, 200, 35, 55, 730, 485);
            if(isInside) rectX -= moveSpeed;
            else {
                rectX = 35;
                playSound(2);
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            final boolean isInside = isRectInsideRect(rectX+moveSpeed, rectY, 200, 200, 35, 55, 730, 485);
            if(isInside) rectX += moveSpeed;
            else {
                rectX = 35 + 730-200;
                playSound(3);
            }
        }

        // Rechteck-Position aktualisieren
        rect.setPosition(rectX, rectY);
    }

    private void playSound(int side) {
        if(collisionOnSide!=side) {
            collisionOnSide = side;
            collideSound.play(1.0f); // 1.0f = volle Lautstärke
        }
    }

    private boolean isRectInsideRect(float innerX, float innerY, float innerWidth, float innerHeight,
                                     float outerX, float outerY, float outerWidth, float outerHeight) {

        return innerX >= outerX &&
            innerY >= outerY &&
            innerX + innerWidth <= outerX + outerWidth &&
            innerY + innerHeight <= outerY + outerHeight;

    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        collideSound.dispose();
    }

}
