package game.samples.rts02;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class BuildingPlacementHUD {
    private boolean isVisible;
    private Rectangle[] buildingButtons;
    private Building.BuildingType[] buildingTypes;
    private String[] buildingNames;
    private int[] buildingCosts;

    private float hudX, hudY;
    private float buttonWidth, buttonHeight;
    private float buttonSpacing;

    private int hoveredButtonIndex;

    public BuildingPlacementHUD() {
        this.isVisible = false;
        this.hoveredButtonIndex = -1;

        // HUD Position (zentriert am Bildschirm)
        this.buttonWidth = 150f;
        this.buttonHeight = 60f;
        this.buttonSpacing = 10f;

        // 5 Gebäude-Typen
        buildingTypes = new Building.BuildingType[] {
            Building.BuildingType.HOUSE,
            Building.BuildingType.FACTORY,
            Building.BuildingType.BARRACKS,
            Building.BuildingType.TOWER,
            Building.BuildingType.WALL
        };

        buildingNames = new String[] {
            "House",
            "Factory",
            "Barracks",
            "Tower",
            "Wall"
        };

        buildingCosts = new int[] {
            100,  // House
            250,  // Factory
            200,  // Barracks
            150,  // Tower
            50    // Wall
        };

        buildingButtons = new Rectangle[5];
        updateButtonPositions(800, 600); // Default screen size
    }

    public void updateButtonPositions(int screenWidth, int screenHeight) {
        // Zentriere HUD horizontal, platziere unten
        float totalWidth = (buttonWidth * 5) + (buttonSpacing * 4);
        hudX = (screenWidth - totalWidth) / 2f;
        hudY = 100f; // Von unten

        for (int i = 0; i < buildingButtons.length; i++) {
            float x = hudX + (i * (buttonWidth + buttonSpacing));
            buildingButtons[i] = new Rectangle(x, hudY, buttonWidth, buttonHeight);
        }
    }

    public void render(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font) {
        if (!isVisible) return;

        // Halbtransparenter Hintergrund
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        float totalWidth = (buttonWidth * 5) + (buttonSpacing * 4);
        shapeRenderer.rect(hudX - 20, hudY - 20, totalWidth + 40, buttonHeight + 80);
        shapeRenderer.end();

        // Buttons
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < buildingButtons.length; i++) {
            Rectangle button = buildingButtons[i];

            // Button-Farbe basierend auf Gebäude-Typ
            Color buttonColor = getBuildingColor(buildingTypes[i]);

            if (i == hoveredButtonIndex) {
                // Heller wenn gehovered
                shapeRenderer.setColor(
                    Math.min(1f, buttonColor.r * 1.3f),
                    Math.min(1f, buttonColor.g * 1.3f),
                    Math.min(1f, buttonColor.b * 1.3f),
                    1f
                );
            } else {
                shapeRenderer.setColor(buttonColor);
            }

            shapeRenderer.rect(button.x, button.y, button.width, button.height);
        }
        shapeRenderer.end();

        // Button-Rahmen
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        com.badlogic.gdx.Gdx.gl.glLineWidth(2);
        for (int i = 0; i < buildingButtons.length; i++) {
            Rectangle button = buildingButtons[i];

            if (i == hoveredButtonIndex) {
                shapeRenderer.setColor(1, 1, 0, 1); // Gelb
            } else {
                shapeRenderer.setColor(1, 1, 1, 1); // Weiß
            }

            shapeRenderer.rect(button.x, button.y, button.width, button.height);
        }
        shapeRenderer.end();

        // Text
        batch.begin();
        font.setColor(Color.WHITE);

        // Titel
        font.draw(batch, "SELECT BUILDING TYPE", hudX, hudY + buttonHeight + 50);

        // Button-Labels
        for (int i = 0; i < buildingButtons.length; i++) {
            Rectangle button = buildingButtons[i];

            // Name
            font.draw(batch, buildingNames[i],
                button.x + 10,
                button.y + buttonHeight - 10);

            // Kosten
            font.setColor(Color.YELLOW);
            font.draw(batch, "$" + buildingCosts[i],
                button.x + 10,
                button.y + 25);

            font.setColor(Color.WHITE);
        }

        // ESC Hinweis
        font.setColor(Color.GRAY);
        font.draw(batch, "Press ESC to cancel", hudX, hudY - 10);

        batch.end();
    }

    private Color getBuildingColor(Building.BuildingType type) {
        switch (type) {
            case HOUSE:
                return new Color(0.6f, 0.4f, 0.2f, 1f);
            case FACTORY:
                return new Color(0.5f, 0.5f, 0.5f, 1f);
            case BARRACKS:
                return new Color(0.3f, 0.3f, 0.6f, 1f);
            case TOWER:
                return new Color(0.7f, 0.2f, 0.2f, 1f);
            case WALL:
                return new Color(0.4f, 0.4f, 0.4f, 1f);
            default:
                return Color.GRAY;
        }
    }

    public Building.BuildingType checkButtonClick(float screenX, float screenY) {
        if (!isVisible) return null;

        for (int i = 0; i < buildingButtons.length; i++) {
            if (buildingButtons[i].contains(screenX, screenY)) {
                return buildingTypes[i];
            }
        }

        return null;
    }

    public void updateHover(float screenX, float screenY) {
        if (!isVisible) {
            hoveredButtonIndex = -1;
            return;
        }

        hoveredButtonIndex = -1;
        for (int i = 0; i < buildingButtons.length; i++) {
            if (buildingButtons[i].contains(screenX, screenY)) {
                hoveredButtonIndex = i;
                break;
            }
        }
    }

    public void show() {
        isVisible = true;
    }

    public void hide() {
        isVisible = false;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public int getCost(Building.BuildingType type) {
        for (int i = 0; i < buildingTypes.length; i++) {
            if (buildingTypes[i] == type) {
                return buildingCosts[i];
            }
        }
        return 0;
    }
}
