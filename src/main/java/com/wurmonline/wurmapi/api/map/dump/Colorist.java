package com.wurmonline.wurmapi.api.map.dump;

import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.mesh.TreeData;

import java.awt.*;

/**
 * Provides colour information for map dumps.
 */
public interface Colorist {
    Color getFlowerColorFor(GrassData.FlowerType flowerType);

    Color getFlowerColorFor(int meshEncodedTile);

    Color getTreeColorFor(TreeData.TreeType treeType);

    Color getSurfaceColorFor(Tile tile);

    Color getCaveColorFor(Tile tile);

    /**
     * Color used for an unknown tile type on the surface.
     * @return Color for unknown tile type.
     */
    Color getSurfaceUnknownColor();

    /**
     * @return Color for unknown underground tile type.
     */
    Color getCaveUnknownColor();
}
