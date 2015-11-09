package com.wurmonline.wurmapi.internal;

import com.wurmonline.mesh.Tiles.Tile;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class CaveColors {
    
    private static final Map<Tile, Color> mappings = new HashMap<>();
    
    static {
        addMapping(Tile.TILE_CAVE_WALL, Color.DARK_GRAY);
        addMapping(Tile.TILE_CAVE_WALL_REINFORCED, Color.DARK_GRAY);
        addMapping(Tile.TILE_CAVE, Color.PINK);
        addMapping(Tile.TILE_CAVE_FLOOR_REINFORCED, Color.PINK);
        addMapping(Tile.TILE_CAVE_EXIT, Color.PINK);
        addMapping(Tile.TILE_CAVE_WALL_ORE_IRON, Color.RED.darker());
        addMapping(Tile.TILE_CAVE_WALL_LAVA, Color.RED);
        addMapping(Tile.TILE_CAVE_WALL_ORE_COPPER, Color.GREEN);
        addMapping(Tile.TILE_CAVE_WALL_ORE_TIN, Color.GRAY);
        addMapping(Tile.TILE_CAVE_WALL_ORE_GOLD, Color.YELLOW.darker());
        addMapping(Tile.TILE_CAVE_WALL_ORE_ADAMANTINE, Color.CYAN);
        addMapping(Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL, Color.YELLOW.brighter());
        addMapping(Tile.TILE_CAVE_WALL_ORE_SILVER, Color.LIGHT_GRAY);
        addMapping(Tile.TILE_CAVE_WALL_ORE_LEAD, Color.PINK.darker().darker());
        addMapping(Tile.TILE_CAVE_WALL_ORE_ZINC, new Color(235, 235, 235));
        addMapping(Tile.TILE_CAVE_WALL_SLATE, Color.BLACK);
        addMapping(Tile.TILE_CAVE_WALL_MARBLE, Color.WHITE);
    }
    
    private static void addMapping(Tile tile, Color color) {
        mappings.put(tile, color);
    }

    public static Color getColorFor(Tile tile) {
        return mappings.getOrDefault(tile, Color.PINK);
    }
    
}
