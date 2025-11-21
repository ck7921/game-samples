package game.samples.rts01;

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

    public enum BuildingType {
        HOUSE,
        FACTORY,
        BARRACKS,
        TOWER
    }

    private BuildingType type;

    public Building(float x, float y, float width, float height, BuildingType type) {
        this.bounds = new Rectangle(x, y, width, height);
        this.type = type;
        this.isSelected = false;

        // Farbe basierend auf Typ
        switch (type) {
            case HOUSE:
                this.color = new Color(0.6f, 0.4f, 0.2f, 1f); // Braun
                this.name = "House";
                break;
            case FACTORY:
                this.color = new Color(0.5f, 0.5f, 0.5f, 1f); // Grau
                this.name = "Factory";
                break;
            case BARRACKS:
                this.color = new Color(0.3f, 0.3f, 0.6f, 1f); // Blau
                this.name = "Barracks";
                break;
            case TOWER:
                this.color = new Color(0.7f, 0.2f, 0.2f, 1f); // Rot
                this.name = "Tower";
                break;
            default:
                this.color = Color.GRAY;
                this.name = "Building";
        }
    }

    public void render(ShapeRenderer renderer) {
        // Gebäude
        renderer.setColor(color);
        renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        // Rahmen (heller wenn selektiert)
        if (isSelected) {
            renderer.setColor(1, 1, 0, 1); // Gelb
        } else {
            renderer.setColor(color.r * 0.5f, color.g * 0.5f, color.b * 0.5f, 1f);
        }
    }

    public void renderOutline(ShapeRenderer renderer) {
        if (isSelected) {
            renderer.setColor(1, 1, 0, 1); // Gelb wenn selektiert
        } else {
            renderer.setColor(0, 0, 0, 0.5f); // Schwarz
        }
        renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public void renderLabel(SpriteBatch batch, BitmapFont font) {
        font.setColor(Color.WHITE);
        font.draw(batch, name, bounds.x + 5, bounds.y + bounds.height - 5);
    }

    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }

    // Getter/Setter
    public Rectangle getBounds() {
        return bounds;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public BuildingType getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}

