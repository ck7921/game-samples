package game.samples.rts02;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class Building {
    private Rectangle bounds;
    private String name;
    private Color color;
    private boolean isSelected;
    private boolean isGhost; // Für Platzierungs-Vorschau

    public enum BuildingType {
        HOUSE,
        FACTORY,
        BARRACKS,
        TOWER,
        WALL
    }

    private BuildingType type;

    public Building(float x, float y, float width, float height, BuildingType type) {
        this.bounds = new Rectangle(x, y, width, height);
        this.type = type;
        this.isSelected = false;
        this.isGhost = false;

        switch (type) {
            case HOUSE:
                this.color = new Color(0.6f, 0.4f, 0.2f, 1f);
                this.name = "House";
                break;
            case FACTORY:
                this.color = new Color(0.5f, 0.5f, 0.5f, 1f);
                this.name = "Factory";
                break;
            case BARRACKS:
                this.color = new Color(0.3f, 0.3f, 0.6f, 1f);
                this.name = "Barracks";
                break;
            case TOWER:
                this.color = new Color(0.7f, 0.2f, 0.2f, 1f);
                this.name = "Tower";
                break;
            case WALL:
                this.color = new Color(0.4f, 0.4f, 0.4f, 1f);
                this.name = "Wall";
                break;
            default:
                this.color = Color.GRAY;
                this.name = "Building";
        }
    }

    public void render(ShapeRenderer renderer) {
        if (isGhost) {
            // Halbtransparent für Ghost-Building
            renderer.setColor(color.r, color.g, color.b, 0.5f);
        } else {
            renderer.setColor(color);
        }
        renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public void renderOutline(ShapeRenderer renderer) {
        if (isSelected) {
            renderer.setColor(1, 1, 0, 1);
        } else if (isGhost) {
            renderer.setColor(0, 1, 0, 0.8f); // Grün für Ghost
        } else {
            renderer.setColor(0, 0, 0, 0.5f);
        }
        renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public void renderLabel(SpriteBatch batch, BitmapFont font) {
        if (isGhost) return; // Keine Labels für Ghosts

        font.setColor(Color.WHITE);
        font.draw(batch, name, bounds.x + 5, bounds.y + bounds.height - 5);
    }

    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }

    public boolean overlaps(Building other) {
        return this.bounds.overlaps(other.bounds);
    }

    public static float getDefaultWidth(BuildingType type) {
        switch (type) {
            case HOUSE: return 60f;
            case FACTORY: return 120f;
            case BARRACKS: return 100f;
            case TOWER: return 40f;
            case WALL: return 80f;
            default: return 60f;
        }
    }

    public static float getDefaultHeight(BuildingType type) {
        switch (type) {
            case HOUSE: return 60f;
            case FACTORY: return 100f;
            case BARRACKS: return 120f;
            case TOWER: return 80f;
            case WALL: return 20f;
            default: return 60f;
        }
    }

    // Getter/Setter
    public Rectangle getBounds() {
        return bounds;
    }

    public void setPosition(float x, float y) {
        bounds.x = x;
        bounds.y = y;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setGhost(boolean ghost) {
        this.isGhost = ghost;
    }

    public boolean isGhost() {
        return isGhost;
    }

    public BuildingType getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}

