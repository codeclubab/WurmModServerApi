package com.wurmonline.wurmapi.api.map.dump;

import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.TreeData;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides default colours for a map dump.
 */
public class DefaultColorist implements Colorist {
    private static final Color PURPLE = new Color(108, 46, 153);
    private static final Color YELLOW_GREEN = new Color(200, 230, 0);

    private static final Map<GrassData.FlowerType, Color> FLOWER_COLOR;
    private static final Color FLOWER_COLOR_UNKNOWN = Tiles.Tile.TILE_GRASS.getColor();
    static {
        Map<GrassData.FlowerType, Color> flowerColour = new HashMap<>();
        flowerColour.put(GrassData.FlowerType.FLOWER_1, Color.YELLOW);
        flowerColour.put(GrassData.FlowerType.FLOWER_2, Color.ORANGE);
        flowerColour.put(GrassData.FlowerType.FLOWER_3, PURPLE);
        flowerColour.put(GrassData.FlowerType.FLOWER_4, Color.WHITE);
        flowerColour.put(GrassData.FlowerType.FLOWER_5, Color.BLUE);
        flowerColour.put(GrassData.FlowerType.FLOWER_6, YELLOW_GREEN);
        flowerColour.put(GrassData.FlowerType.FLOWER_7, Color.PINK);
        flowerColour.put(GrassData.FlowerType.NONE, FLOWER_COLOR_UNKNOWN);
        FLOWER_COLOR = Collections.unmodifiableMap(flowerColour);
    }

    private static final Map<TreeData.TreeType, Color> TREE_COLOR;
    private static final Color TREE_COLOR_UNKNOWN = Tiles.Tile.TILE_GRASS.getColor();
    static {
        // Some colours will be similar to the material but some will not, to aid in differentiating tree types.
        Map<TreeData.TreeType, Color> treeColour = new HashMap<>();
        treeColour.put(TreeData.TreeType.BIRCH, Color.WHITE);
        treeColour.put(TreeData.TreeType.PINE, Color.YELLOW.brighter());
        treeColour.put(TreeData.TreeType.OAK, Color.BLACK);
        treeColour.put(TreeData.TreeType.CEDAR, Color.CYAN);
        treeColour.put(TreeData.TreeType.WILLOW, Color.DARK_GRAY);
        treeColour.put(TreeData.TreeType.MAPLE, PURPLE);
        treeColour.put(TreeData.TreeType.APPLE, YELLOW_GREEN);
        treeColour.put(TreeData.TreeType.LEMON, Color.YELLOW);
        treeColour.put(TreeData.TreeType.OLIVE, new Color(80, 80, 0));
        treeColour.put(TreeData.TreeType.CHERRY, Color.RED);
        treeColour.put(TreeData.TreeType.CHESTNUT, Color.LIGHT_GRAY);
        treeColour.put(TreeData.TreeType.WALNUT, Color.ORANGE);
        treeColour.put(TreeData.TreeType.FIR, Color.GREEN.darker());
        treeColour.put(TreeData.TreeType.LINDEN, Color.PINK);
        treeColour.put(TreeData.TreeType.ORANGE, Color.ORANGE.brighter());
        TREE_COLOR = Collections.unmodifiableMap(treeColour);
    }

    private static final Map<Tiles.Tile, Color> CAVE_COLORS;
    private static final Color CAVE_COLOR_UNKNOWN = Color.PINK;
    static {
        Map<Tiles.Tile, Color> caveColors = new HashMap<>();
        caveColors.put(Tiles.Tile.TILE_CAVE_WALL, Color.DARK_GRAY);
        caveColors.put(Tiles.Tile.TILE_CAVE_WALL_REINFORCED, Color.DARK_GRAY);
        caveColors.put(Tiles.Tile.TILE_CAVE, Color.PINK);
        caveColors.put(Tiles.Tile.TILE_CAVE_FLOOR_REINFORCED, Color.PINK);
        caveColors.put(Tiles.Tile.TILE_CAVE_EXIT, Color.PINK);
        caveColors.put(Tiles.Tile.TILE_CAVE_WALL_ORE_IRON, Color.RED.darker());
        caveColors.put(Tiles.Tile.TILE_CAVE_WALL_LAVA, Color.RED);
        caveColors.put(Tiles.Tile.TILE_CAVE_WALL_ORE_COPPER, Color.GREEN);
        caveColors.put(Tiles.Tile.TILE_CAVE_WALL_ORE_TIN, Color.GRAY);
        caveColors.put(Tiles.Tile.TILE_CAVE_WALL_ORE_GOLD, Color.YELLOW.darker());
        caveColors.put(Tiles.Tile.TILE_CAVE_WALL_ORE_ADAMANTINE, Color.CYAN);
        caveColors.put(Tiles.Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL, Color.YELLOW.brighter());
        caveColors.put(Tiles.Tile.TILE_CAVE_WALL_ORE_SILVER, Color.LIGHT_GRAY);
        caveColors.put(Tiles.Tile.TILE_CAVE_WALL_ORE_LEAD, Color.PINK.darker().darker());
        caveColors.put(Tiles.Tile.TILE_CAVE_WALL_ORE_ZINC, new Color(235, 235, 235));
        caveColors.put(Tiles.Tile.TILE_CAVE_WALL_SLATE, Color.BLACK);
        caveColors.put(Tiles.Tile.TILE_CAVE_WALL_MARBLE, Color.WHITE);
        caveColors.put(Tiles.Tile.TILE_CAVE_WALL_ROCKSALT, Color.CYAN.brighter());
        caveColors.put(Tiles.Tile.TILE_CAVE_WALL_SANDSTONE, Color.ORANGE.darker());
        CAVE_COLORS = Collections.unmodifiableMap(caveColors);
    }

    private static final Color SURFACE_COLOR_UNKNOWN = Tiles.Tile.TILE_DIRT.getColor();

    @Override
    public Color getFlowerColorFor(GrassData.FlowerType flowerType) {
        return FLOWER_COLOR.getOrDefault(flowerType, FLOWER_COLOR_UNKNOWN);
    }

    @Override
    public Color getFlowerColorFor(int meshEncodedTile) {
        byte grassData = Tiles.decodeData(meshEncodedTile);
        return getFlowerColorFor(GrassData.FlowerType.decodeTileData(grassData));
    }

    @Override
    public Color getTreeColorFor(TreeData.TreeType treeType) {
        return TREE_COLOR.getOrDefault(treeType, TREE_COLOR_UNKNOWN);
    }

    @Override
    public Color getSurfaceColorFor(Tiles.Tile tile) {
        return tile.getColor();
    }

    @Override
    public Color getCaveColorFor(Tiles.Tile tile) {
        return CAVE_COLORS.getOrDefault(tile, CAVE_COLOR_UNKNOWN);
    }

    /**
     * @return Color for unknown surface tile type.
     */
    @Override
    public Color getSurfaceUnknownColor() {
        return SURFACE_COLOR_UNKNOWN;
    }

    @Override
    public Color getCaveUnknownColor() {
        return CAVE_COLOR_UNKNOWN;
    }
}
