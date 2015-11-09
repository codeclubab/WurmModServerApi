package com.wurmonline.wurmapi.api.map.dump;

import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.wurmapi.internal.CaveColors;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides colour information for map dumps
 */
public class Colorist {
    private static final Color PURPLE = Color.getHSBColor(0.7638888888888889f, 0.7f, 0.6f);

    private static final Color YELLOW_GREEN = Color.getHSBColor(0.188888f, 1.0f, 0.9f);

    private static final Map<GrassData.FlowerType, Color> FLOWER_COLOUR;
    static {
        Map<GrassData.FlowerType, Color> flowerColour = new HashMap<>();
        flowerColour.put(GrassData.FlowerType.FLOWER_1, Color.YELLOW);
        flowerColour.put(GrassData.FlowerType.FLOWER_2, Color.ORANGE);
        flowerColour.put(GrassData.FlowerType.FLOWER_3, PURPLE);
        flowerColour.put(GrassData.FlowerType.FLOWER_4, Color.WHITE);
        flowerColour.put(GrassData.FlowerType.FLOWER_5, Color.BLUE);
        flowerColour.put(GrassData.FlowerType.FLOWER_6, YELLOW_GREEN);
        flowerColour.put(GrassData.FlowerType.FLOWER_7, Color.PINK);
        flowerColour.put(GrassData.FlowerType.NONE, Tile.TILE_GRASS.getColor());
        FLOWER_COLOUR = Collections.unmodifiableMap(flowerColour);
    }

    public Color getFlowerColor(GrassData.FlowerType flowerType) {
        return FLOWER_COLOUR.containsKey(flowerType) ?
                FLOWER_COLOUR.get(flowerType) :
                FLOWER_COLOUR.get(GrassData.FlowerType.NONE);
    }

    public Color getFlowerColor(int meshEncodedTile) {
        byte grassData = Tiles.decodeData(meshEncodedTile);
        return getFlowerColor(GrassData.FlowerType.decodeTileData(grassData));
    }

    public Color getSurfaceColorFor(Tile tile) {
        return tile.getColor();
    }

    public Color getCaveColorFor(Tile tile) {
        return CaveColors.getColorFor(tile);
    }

    /**
     * Color used for an unknown tile type
     * @return
     */
    public Color getSurfaceUnknownColor() {
        return Tile.TILE_DIRT.getColor();
    }

    public Color getCaveUnknownColor() {
        return CaveColors.getColorFor(Tile.TILE_CAVE);
    }
}
