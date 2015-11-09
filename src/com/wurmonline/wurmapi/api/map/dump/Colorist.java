package com.wurmonline.wurmapi.api.map.dump;

import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.Tiles.Tile;

import java.awt.*;

/**
 * Provides colour information for map dumps.
 */
public interface Colorist {
    Color getFlowerColorFor(GrassData.FlowerType flowerType);

    Color getFlowerColorFor(int meshEncodedTile);

    Color getTreeColorFor(int meshEncodedTile);

    Color getSurfaceColorFor(Tile tile);

    Color getCaveColorFor(Tile tile);

    /**
     * Color used for an unknown tile type on the surface.
     * @return Color for unknown tile type.
     */
    Color getSurfaceUnknownColor();

    /**
     *
     * @return
     */
    Color getCaveUnknownColor();
}
