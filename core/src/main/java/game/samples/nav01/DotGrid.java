package game.samples.nav01;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.math.MathUtils;

public class DotGrid {
    private List<Vector2> gridPoints;
    private float dotRadius;
    private Color dotColor;
    private float spacing;
    private float mapWidth;
    private float mapHeight;

    private int gridWidth;  // Anzahl Grid-Punkte horizontal
    private int gridHeight; // Anzahl Grid-Punkte vertikal

    public DotGrid(float mapWidth, float mapHeight, float spacing) {
        this.spacing = spacing;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.dotRadius = 1.5f;
        this.dotColor = Color.LIGHT_GRAY;
        this.gridPoints = new ArrayList<>();

        generateGrid(mapWidth, mapHeight);
    }

    private void generateGrid(float mapWidth, float mapHeight) {
        gridPoints.clear();

        gridWidth = (int)(mapWidth / spacing) + 1;
        gridHeight = (int)(mapHeight / spacing) + 1;

        for (float x = 0; x <= mapWidth; x += spacing) {
            for (float y = 0; y <= mapHeight; y += spacing) {
                gridPoints.add(new Vector2(x, y));
            }
        }

        System.out.println("Grid generiert: " + gridPoints.size() + " Punkte (" +
            gridWidth + " x " + gridHeight + ")");
    }

    public void render(ShapeRenderer renderer) {
        renderer.setColor(dotColor);

        for (Vector2 point : gridPoints) {
            renderer.circle(point.x, point.y, dotRadius);
        }
    }

    /**
     * Gibt eine zufällige Grid-Koordinate zurück
     */
    public Vector2 getRandomGridCoordinate() {
        int gridX = MathUtils.random(0, gridWidth - 1);
        int gridY = MathUtils.random(0, gridHeight - 1);
        return new Vector2(gridX, gridY);
    }

    /**
     * Konvertiert Grid-Koordinaten zu Welt-Koordinaten
     */
    public Vector2 gridToWorld(int gridX, int gridY) {
        return new Vector2(gridX * spacing, gridY * spacing);
    }

    /**
     * Konvertiert Welt-Koordinaten zu Grid-Koordinaten
     */
    public Vector2 worldToGrid(float worldX, float worldY) {
        return new Vector2(
            Math.round(worldX / spacing),
            Math.round(worldY / spacing)
        );
    }

    // Getter
    public float getSpacing() {
        return spacing;
    }

    public int getPointCount() {
        return gridPoints.size();
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }
}
