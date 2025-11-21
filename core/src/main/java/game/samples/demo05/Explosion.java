package game.samples.demo05;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Explosion {
    private float x, y;
    private float radius;
    private float maxRadius;
    private float alpha;
    private float duration;
    private float timer;

    public Explosion(float x, float y) {
        this.x = x;
        this.y = y;
        this.radius = 5f;
        this.maxRadius = 40f;
        this.alpha = 1f;
        this.duration = 0.5f; // 0.5 Sekunden
        this.timer = 0f;
    }

    public void update(float deltaTime) {
        timer += deltaTime;

        float progress = timer / duration;

        // Radius wächst
        radius = 5f + (maxRadius - 5f) * progress;

        // Alpha nimmt ab
        alpha = 1f - progress;
    }

    public void render(ShapeRenderer renderer) {
        // Äußerer Ring (rot-orange)
        renderer.setColor(1f, 0.5f, 0f, alpha * 0.6f);
        renderer.circle(x, y, radius);

        // Innerer Ring (gelb)
        renderer.setColor(1f, 1f, 0f, alpha);
        renderer.circle(x, y, radius * 0.6f);

        // Kern (weiß)
        renderer.setColor(1f, 1f, 1f, alpha);
        renderer.circle(x, y, radius * 0.3f);
    }

    public boolean isFinished() {
        return timer >= duration;
    }
}
