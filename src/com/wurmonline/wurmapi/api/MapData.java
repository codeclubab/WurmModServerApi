package com.wurmonline.wurmapi.api;

import com.wurmonline.mesh.BushData.BushType;
import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.mesh.TreeData.TreeType;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MapData {
    
    private static final float MAP_HEIGHT = 1000;
    
    private final MeshIO surfaceMesh;
    private final MeshIO rockMesh;
    private final MeshIO flagsMesh;
    private final MeshIO caveMesh;
    private final MeshIO resourcesMesh;
    private final MeshIO[] allMeshes;
    
    MapData(String root) throws IOException {
        this.surfaceMesh = MeshIO.open(root + "top_layer.map");
        this.rockMesh = MeshIO.open(root + "rock_layer.map");
        this.flagsMesh = MeshIO.open(root + "flags.map");
        this.caveMesh = MeshIO.open(root + "map_cave.map");
        this.resourcesMesh = MeshIO.open(root + "resources.map");
        allMeshes = new MeshIO[] {surfaceMesh, rockMesh, flagsMesh, caveMesh, resourcesMesh};
    }
    
    MapData(String root, int powerOfTwo) throws IOException {
        this.surfaceMesh = createMap(root + "top_layer.map", powerOfTwo);
        this.rockMesh = createMap(root + "rock_layer.map", powerOfTwo);
        this.flagsMesh = createMap(root + "flags.map", powerOfTwo);
        this.caveMesh = createMap(root + "map_cave.map", powerOfTwo);
        this.resourcesMesh = createMap(root + "resources.map", powerOfTwo);
        allMeshes = new MeshIO[] {surfaceMesh, rockMesh, flagsMesh, caveMesh, resourcesMesh};
        
        int halfWidth = getWidth() / 2;
        int halfHeight = getHeight() / 2;
        
        for (int i = 0; i < getWidth(); i++) {
            for (int i2 = 0; i2 < getHeight(); i2++) {
                setCaveTile(i, i2, Tile.TILE_CAVE_WALL);
                
                int distToEdge = Math.min(halfWidth - Math.abs(i - halfWidth), halfHeight - Math.abs(i2 - halfHeight));
                if (distToEdge > 5) {
                    setRockHeight(i, i2, (short) (95));
                    setSurfaceTile(i, i2, Tile.TILE_DIRT, (short) (100));
                }
                else {
                    setRockHeight(i, i2, (short) (-200));
                    setSurfaceTile(i, i2, Tile.TILE_DIRT, (short) (-100));
                }
            }
        }
    }
    
    private MeshIO createMap(String dir, int powerOfTwo) throws IOException {
        File file = new File(dir);
        if (file.exists()) {
            file.delete();
            file.createNewFile();
        }
        else {
            file.createNewFile();
        }
        
        int realSize = 1 << powerOfTwo;
        int[] data = new int[realSize * realSize];
        
        return MeshIO.createMap(dir, powerOfTwo, data);
    }
    
    /**
     * Currently map width and height are always equal, but two methods exists in case if this will change in a future.
     * 
     * @return map width
     */
    public int getWidth() {
        return surfaceMesh.getSize();
    }
    
    /**
     * Currently map width and height are always equal, but two methods exists in case if this will change in a future.
     * 
     * @return map height
     */
    public int getHeight() {
        return surfaceMesh.getSize();
    }
    
    /**
     * 
     * @param x x location in game world.
     * @param y y location in game world.
     * @return surface tile type in location.
     */
    public Tile getSurfaceTile(int x, int y) {
        return Tiles.getTile(Tiles.decodeType(surfaceMesh.getTile(x, y)));
    }
    
    /**
     * 
     * @param x x location in game world.
     * @param y y location in game world.
     * @return surface tile height in location.
     */
    public short getSurfaceHeight(int x, int y) {
        return Tiles.decodeHeight(surfaceMesh.getTile(x, y));
    }
    
    private byte getSurfaceData(int x, int y) {
        // internal method, I don't think it should be exposed
        return Tiles.decodeData(surfaceMesh.getTile(x, y));
    }
    
    /**
     * @param x x location in game world.
     * @param y y location in game world.
     * @return height of dirt layer in location, or 0 if negative (rock layer being higher than surface layer).
     */
    public short getDirtLayerHeight(int x, int y) {
        short dirtHeight = (short) (getSurfaceHeight(x, y) - getRockHeight(x, y));
        return dirtHeight >= 0 ? dirtHeight : 0;
    }
    
    /**
     * This method changes height of surface layer without changing any other data.
     * 
     * @param x x location in game world.
     * @param y y location in game world.
     * @param height height of tile, be careful with very high or very low values as both seem to cause server to crash.
     */
    public void setSurfaceHeight(int x, int y, short height) {
        Tile type = getSurfaceTile(x, y);
        byte data = getSurfaceData(x, y);
        
        setSurfaceTile(x, y, type, height, data);
    }
    
    /**
     * Sets tile data on a surface.<br>
     * Please see {@link #setSurfaceTile(int, int, com.wurmonline.mesh.Tiles.Tile, short) this method} for more details.
     * 
     * @param x x location in game world.
     * @param y y location in game world.
     * @param tileType type of tile. Using cave, trees and bushes constants is not allowed.
     */
    public void setSurfaceTile(int x, int y, Tile tileType) {
        setSurfaceTile(x, y, tileType, getSurfaceHeight(x, y));
    }
    
    /**
     * Sets tile data on a surface.<br>
     * While it is possible to place practically everything using this method, it is recommended to set things like trees and bushes using specialized methods instead of this one.<br><br>
     * 
     * If tile is on lower height than rock layer height at the moment of saving map to file, it will be automatically set to rock layer height.<br>
     * If the whole tile is exposed (rock level equal to surface level in all four corners), its type will be changed to rock on map save.<br>
     * This method will NOT prevent doing illogical things like placing trees or grass under water.<br><br>
     * 
     * Under the hood, tile data is 32-bit value, where:<br>
     * Bits 1-8 : tile type data<br>
     * Bits 9-16 : additional data (used for example by trees, bushes and grass, varies from tile type to tile type, this method is not using these bits)<br>
     * Bits 17-32 : height data (as signed short value)
     * 
     * @param x x location in game world.
     * @param y y location in game world.
     * @param height height of tile, be careful with very high or very low values as both seem to cause server to crash.
     * @param tileType type of tile. Using cave, trees and bushes constants is not allowed.
     * 
     * @see #setTree(int, int, com.wurmonline.mesh.TreeData.TreeType, com.wurmonline.mesh.FoliageAge, com.wurmonline.mesh.GrassData.GrowthTreeStage)
     * @see #setBush(int, int, com.wurmonline.mesh.BushData.BushType, com.wurmonline.mesh.FoliageAge, com.wurmonline.mesh.GrassData.GrowthTreeStage)
     */
    public void setSurfaceTile(int x, int y, Tile tileType, short height) {
        if (tileType == null) {
            throw new IllegalArgumentException("Tile type is null");
        }
        else if (tileType.isCave()) {
            throw new IllegalArgumentException("Tile type is not surface type: "+tileType.toString());
        }
        else if (tileType.isTree() || tileType.isBush()) {
            throw new IllegalArgumentException("Tile type is tree or bush: please use specialized methods to put them on map instead.");
        }
        
        setSurfaceTile(x, y, tileType, height, (byte) 0);
    }
    
    private void setSurfaceTile(int x, int y, Tile tileType, short height, byte data) {
        surfaceMesh.setTile(x, y, Tiles.encode(height, (byte) tileType.getId(), data));
    }
    
    /**
     * Places tree in specified position in game world.<br><br>
     * 
     * Placing tree should happen after calling {@link #setSurfaceTile(int, int, short, com.wurmonline.mesh.Tiles.Tile) setSurfaceTile}, as it is using data from a tile (height and grass type) to generate the final tree data.<br>
     * This method will NOT prevent doing illogical things like placing trees under water.<br><br>
     * 
     * Under the hood, 1 byte of tree special data is composed of these values:<br>
     * Bits 1-2 : growth stage of grass<br>
     * Bit 3 : tree position (center or any)<br>
     * Bit 4 : contains fruit<br>
     * Bits 5-8 : age of tree (trees with sprouts count as stage of growth as well)
     * 
     * @param x x location in game world.
     * @param y y location in game world.
     * @param treeType type of tree
     * @param age age of tree
     * @param grassStage stage of grass growth on tree tile. It replaces grass height of normal grass tile if it was previously set.
     */
    public void setTree(int x, int y, TreeType treeType, FoliageAge age, GrassData.GrowthTreeStage grassStage) {
        if (treeType == null) {
            throw new IllegalArgumentException("Tree type is null");
        }
        else if (age == null) {
            throw new IllegalArgumentException("Foliage age is null");
        }
        else if (grassStage == null) {
            throw new IllegalArgumentException("Grass stage is null");
        }
        
        byte currentType = Tiles.decodeType(surfaceMesh.getTile(x, y));
        byte resultType;
        if (currentType == Tiles.TILE_TYPE_MYCELIUM) {
            resultType = treeType.asMyceliumTree();
        }
        else if (currentType == Tiles.TILE_TYPE_ENCHANTED_GRASS) {
            resultType = treeType.asEnchantedTree();
        }
        else {
            resultType = treeType.asNormalTree();
        }
        
        setFoliage(x, y, resultType, age, grassStage);
    }
    
    /**
     * Places bush in specified position in game world.<br><br>
     * 
     * Placing bush should happen after calling {@link #setSurfaceTile(int, int, short, int) setSurfaceTile}, as it is using data from a tile (height and grass type) to generate the final bush data.<br>
     * This method will NOT prevent doing illogical things like placing grass under water.<br><br>
     * 
     * Under the hood, 1 byte of bush special data is composed in the same way as tree data: {@link #setTree(int, int, com.wurmonline.mesh.TreeData.TreeType, com.wurmonline.mesh.FoliageAge, com.wurmonline.mesh.GrassData.GrowthTreeStage) setTree}.
     * 
     * @param x x location in game world.
     * @param y y location in game world.
     * @param bushType type of bush
     * @param age age of bush
     * @param grassStage stage of grass growth on bush tile. It replaces grass height of normal grass tile if it was previously set.
     */
    public void setBush(int x, int y, BushType bushType, FoliageAge age, GrassData.GrowthTreeStage grassStage) {
        if (bushType == null) {
            throw new IllegalArgumentException("Bush type is null");
        }
        else if (age == null) {
            throw new IllegalArgumentException("Foliage age is null");
        }
        else if (grassStage == null) {
            throw new IllegalArgumentException("Grass stage is null");
        }
        
        byte currentType = Tiles.decodeType(surfaceMesh.getTile(x, y));
        byte resultType;
        if (currentType == Tiles.TILE_TYPE_MYCELIUM) {
            resultType = bushType.asMyceliumBush();
        }
        else if (currentType == Tiles.TILE_TYPE_ENCHANTED_GRASS) {
            resultType = bushType.asEnchantedBush();
        }
        else {
            resultType = bushType.asNormalBush();
        }
        
        setFoliage(x, y, resultType, age, grassStage);
    }
    
    private void setFoliage(int x, int y, byte foliageType, FoliageAge age, GrassData.GrowthTreeStage grassStage) {
        int data = surfaceMesh.getTile(x, y);
        byte resultData = Tiles.encodeTreeData(age, false, false, grassStage);
        short currentHeight = Tiles.decodeHeight(data);

        surfaceMesh.setTile(x, y, Tiles.encode(currentHeight, foliageType, resultData));
    }
    
    /**
     * Sets grass data on given grass, mycelium, kelp or reed tile (for any other types of tiles it does nothing).<br><br>
     * 
     * Flower type can be set ONLY for grass tiles - in other cases method will return without doing anything.
     * 
     * @param x x location in game world.
     * @param y y location in game world.
     * @param grassStage growth stage of grass/kelp/reed
     * @param flower type of flower (must be NONE for tiles other than grass)
     */
    public void setGrass(int x, int y, GrassData.GrowthStage grassStage, GrassData.FlowerType flower) {
        if (grassStage == null) {
            throw new IllegalArgumentException("Grass stage is null");
        }
        else if (flower == null) {
            flower = GrassData.FlowerType.NONE;
        }
        
        int currentType = Tiles.decodeType(surfaceMesh.getTile(x, y));
        GrassData.GrassType grassType;
        if (currentType == Tiles.TILE_TYPE_GRASS) {
            grassType = GrassData.GrassType.GRASS;
        }
        else if (currentType == Tiles.TILE_TYPE_MYCELIUM) {
            grassType = GrassData.GrassType.GRASS;
        }
        else if (currentType == Tiles.TILE_TYPE_KELP) {
            grassType = GrassData.GrassType.KELP;
        }
        else if (currentType == Tiles.TILE_TYPE_REED) {
            grassType = GrassData.GrassType.REED;
        }
        else {
            return;
        }
        
        if (currentType != Tiles.TILE_TYPE_GRASS && flower != GrassData.FlowerType.NONE) {
            return;
        }
        
        short currentHeight = Tiles.decodeHeight(surfaceMesh.getTile(x, y));
        surfaceMesh.setTile(x, y, Tiles.encode(currentHeight,(byte) currentType, GrassData.encodeGrassTileData(grassStage, grassType, flower)));
    }
    
    public short getRockHeight(int x, int y) {
        return Tiles.decodeHeight(rockMesh.getTile(x, y));
    }
    
    /**
     * Sets height of rock layer at location.<br><br>
     * 
     * Under the hood, rock layer is almost identical to surface layer - this method sets rock as tile type, ignores tile special data and sets desired height.
     * 
     * @param x x location in game world.
     * @param y y location in game world.
     * @param height height of tile, be careful with very high or very low values as both seem to cause server to crash.
     */
    public void setRockHeight(int x, int y, short height) {
        rockMesh.setTile(x, y, Tiles.encode(height, (byte) Tiles.TILE_TYPE_ROCK, (byte) 0));
    }
    
    /**
     * 
     * @param x x location in game world.
     * @param y y location in game world.
     * @return cave tile type in location.
     */
    public Tile getCaveTile(int x, int y) {
        return Tiles.getTile(Tiles.decodeType(surfaceMesh.getTile(x, y)));
    }
    
    /**
     * Sets tile data inside cave.<br>
     * Only solid cave walls are allowed - exception will be thrown on attempt to set non-cave tile type or cave type which is not a wall.
     * 
     * @param x x location in game world.
     * @param y y location in game world.
     * @param tileType type of tile. Only cave walls constants are allowed.
     */
    public void setCaveTile(int x, int y, Tile tileType) {
        if (tileType == null || !tileType.isCave()) {
            throw new IllegalArgumentException("Tile type is null");
        }
        else if (!tileType.isCave()) {
            throw new IllegalArgumentException("Tile type is not cave type: "+tileType.toString());
        }
        else if (!tileType.isSolidCave()) {
            throw new IllegalArgumentException("Tile type is invalid cave type: "+tileType.toString());
        }
        
        setCaveTile(x, y, tileType, (short) 0, (byte) 0);
    }
    
    private void setCaveTile(int x, int y, Tile tileType, short height, byte data) {
        caveMesh.setTile(x, y, Tiles.encode(height, (byte) tileType.getId(), data));
    }
    
    /**
     * Creates classical Wurm Online map dump, with semi-3d terrain.<br>
     * You don't need to save map first to create updated map dump - it is using data from memory.
     * 
     * @return map image
     */
    public BufferedImage createMapDump() {
        int lWidth = 16384;
        if (lWidth > getWidth())
            lWidth = getWidth();
        int yo = getWidth() - lWidth;
        if (yo < 0)
            yo = 0;
        int xo = getWidth() - lWidth;
        if (xo < 0)
            xo = 0;

        final Random random = new Random();
        if (xo > 0)
            xo = random.nextInt(xo);
        if (yo > 0)
            yo = random.nextInt(yo);

        final BufferedImage bi2 = new BufferedImage(lWidth, lWidth, BufferedImage.TYPE_INT_RGB);
        final float[] data = new float[lWidth * lWidth * 3];
        
        for (int x = 0; x < lWidth; x++) {
            int alt = lWidth - 1;
            for (int y = lWidth - 1; y >= 0; y--) {
                float node = (float) (getSurfaceHeight(x + xo, y + yo) / (Short.MAX_VALUE / 3.3f));
                float node2 = x == lWidth - 1 || y == lWidth - 1 ? node : (float) (getSurfaceHeight(x + 1 + xo, y + 1 + yo) / (Short.MAX_VALUE / 3.3f));

                final byte tex = Tiles.decodeType(surfaceMesh.getTile(x + xo, y + yo));

                final float hh = node;

                float h = ((node2 - node) * 1500) / 256.0f * getWidth() / 128 + hh / 2 + 1.0f;
                h *= 0.4f;

                float r = h;
                float g = h;
                float b = h;

                final Tile tile = Tiles.getTile(tex);
                final Color color;
                if (tile != null) {
                    color = tile.getColor();
                }
                else {
                    color = Tile.TILE_DIRT.getColor();
                }
                r *= (color.getRed() / 255.0f) * 2;
                g *= (color.getGreen() / 255.0f) * 2;
                b *= (color.getBlue() / 255.0f) * 2;

                if (r < 0)
                    r = 0;
                if (r > 1)
                    r = 1;
                if (g < 0)
                    g = 0;
                if (g > 1)
                    g = 1;
                if (b < 0)
                    b = 0;
                if (b > 1)
                    b = 1;

                if (node < 0) {
                    r = r * 0.2f + 0.4f * 0.4f;
                    g = g * 0.2f + 0.5f * 0.4f;
                    b = b * 0.2f + 1.0f * 0.4f;
                }

                final int altTarget = y - (int) (Tiles.decodeHeight(surfaceMesh.getTile(x, y)) * MAP_HEIGHT / 4  / (Short.MAX_VALUE / 3.3f));
                while (alt > altTarget && alt >= 0) {
                    data[(x + alt * lWidth) * 3 + 0] = r * 255;
                    data[(x + alt * lWidth) * 3 + 1] = g * 255;
                    data[(x + alt * lWidth) * 3 + 2] = b * 255;
                    alt--;
                }
            }
        }

        bi2.getRaster().setPixels(0, 0, lWidth, lWidth, data);
        return bi2;
    }
    
    /**
     * Creates flat map dump, showing all terrain types in different colors.<br>
     * You don't need to save map first to create updated map dump - it is using data from memory.
     * 
     * @param showWater set true if you want to make water visible, false otherwise.
     * @return map image
     */
    public BufferedImage createTerrainDump(boolean showWater) {
        int lWidth = 16384;
        if (lWidth > getWidth())
            lWidth = getWidth();
        int yo = getWidth() - lWidth;
        if (yo < 0)
            yo = 0;
        int xo = getWidth() - lWidth;
        if (xo < 0)
            xo = 0;

        final Random random = new Random();
        if (xo > 0)
            xo = random.nextInt(xo);
        if (yo > 0)
            yo = random.nextInt(yo);

        final BufferedImage bi2 = new BufferedImage(lWidth, lWidth, BufferedImage.TYPE_INT_RGB);
        final float[] data = new float[lWidth * lWidth * 3];
        
        for (int x = 0; x < lWidth; x++) {
            for (int y = lWidth - 1; y >= 0; y--) {
                final short height = Tiles.decodeHeight(surfaceMesh.getTile(x + xo, y + yo));
                final byte tex = Tiles.decodeType(surfaceMesh.getTile(x + xo, y + yo));

                final Tile tile = Tiles.getTile(tex);
                final Color color;
                if (tile != null) {
                    color = tile.getColor();
                }
                else {
                    color = Tile.TILE_DIRT.getColor();
                }
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                if (height < 0 && showWater) {
                    r = (int) (r * 0.2f + 0.4f * 0.4f * 256f);
                    g = (int) (g * 0.2f + 0.5f * 0.4f * 256f);
                    b = (int) (b * 0.2f + 1.0f * 0.4f * 256f);
                }
                
                data[(x + y * lWidth) * 3 + 0] = r;
                data[(x + y * lWidth) * 3 + 1] = g;
                data[(x + y * lWidth) * 3 + 2] = b;
            }
        }

        bi2.getRaster().setPixels(0, 0, lWidth, lWidth, data);
        return bi2;
    }
    
    /**
     * Creates flat map dump, showing all terrain types in different colors and with contour lines.<br>
     * You don't need to save map first to create updated map dump - it is using data from memory.
     * 
     * @param showWater set true if you want to make water visible, false otherwise.
     * @param interval interval for next controur line
     * @return map image
     */
    public BufferedImage createTopographicDump(boolean showWater, short interval) {
        int lWidth = 16384;
        if (lWidth > getWidth())
            lWidth = getWidth();
        int yo = getWidth() - lWidth;
        if (yo < 0)
            yo = 0;
        int xo = getWidth() - lWidth;
        if (xo < 0)
            xo = 0;

        final Random random = new Random();
        if (xo > 0)
            xo = random.nextInt(xo);
        if (yo > 0)
            yo = random.nextInt(yo);

        final BufferedImage bi2 = new BufferedImage(lWidth, lWidth, BufferedImage.TYPE_INT_RGB);
        final float[] data = new float[lWidth * lWidth * 3];
        
        for (int x = 0; x < lWidth; x++) {
            for (int y = lWidth - 1; y >= 0; y--) {
                final short height = Tiles.decodeHeight(surfaceMesh.getTile(x + xo, y + yo));
                final short nearHeightNX = x == 0 ? height : Tiles.decodeHeight(surfaceMesh.getTile(x + xo - 1, y + yo));
                final short nearHeightNY = y == 0 ? height : Tiles.decodeHeight(surfaceMesh.getTile(x + xo, y + yo - 1));
                final short nearHeightX = x == lWidth - 1 ? height : Tiles.decodeHeight(surfaceMesh.getTile(x + xo + 1, y + yo));
                final short nearHeightY = y == lWidth - 1 ? height : Tiles.decodeHeight(surfaceMesh.getTile(x + xo, y + yo + 1));
                boolean isControur = checkContourLine(height, nearHeightNX, interval) || checkContourLine(height, nearHeightNY, interval) || checkContourLine(height, nearHeightX, interval) || checkContourLine(height, nearHeightY, interval);
                
                final byte tex = Tiles.decodeType(surfaceMesh.getTile(x + xo, y + yo));

                final Tile tile = Tiles.getTile(tex);
                final Color color;
                if (tile != null) {
                    color = tile.getColor();
                }
                else {
                    color = Tile.TILE_DIRT.getColor();
                }
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                if (isControur) {
                    r = 0;
                    g = 0;
                    b = 0;
                }
                else if (height < 0 && showWater) {
                    r = (int) (r * 0.2f + 0.4f * 0.4f * 256f);
                    g = (int) (g * 0.2f + 0.5f * 0.4f * 256f);
                    b = (int) (b * 0.2f + 1.0f * 0.4f * 256f);
                }
                
                data[(x + y * lWidth) * 3 + 0] = r;
                data[(x + y * lWidth) * 3 + 1] = g;
                data[(x + y * lWidth) * 3 + 2] = b;
            }
        }

        bi2.getRaster().setPixels(0, 0, lWidth, lWidth, data);
        return bi2;
    }
    
    private boolean checkContourLine(short h0, short h1, short interval) {
        if (h0 == h1) {
            return false;
        }
        for (int i = h0; i<=h1; i++) {
            if (i % interval == 0) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Saves all changes to file. Before saving, this method will remove some map errors like wrong terrain type on completely exposed tiles and surface layer being lower than rock layer.
     */
    public void saveChanges() {
        for (int i = 0; i < getWidth(); i++) {
            for (int i2 = 0; i2 < getHeight(); i2++) {
                short surfaceHeight = getSurfaceHeight(i, i2);
                short rockHeight = getRockHeight(i, i2);
                
                if (rockHeight > surfaceHeight) {
                    setSurfaceHeight(i, i2, rockHeight);
                }
            }
        }
        
        for (int i = 0; i < getWidth() - 1; i++) {
            for (int i2 = 0; i2 < getHeight() - 1; i2++) {
                short h00 = getDirtLayerHeight(i, i2);
                short h10 = getDirtLayerHeight(i + 1, i2);
                short h01 = getDirtLayerHeight(i, i2 + 1);
                short h11 = getDirtLayerHeight(i + 1, i2 + 1);
                int total = h00 + h10 + h01 + h11;
                if (total == 0) {
                    short height = getSurfaceHeight(i, i2);
                    surfaceMesh.setTile(i, i2, Tiles.encode(height, (byte) Tiles.TILE_TYPE_ROCK, (byte) 0));
                }
            }
        }
        
        try {
            for (MeshIO file : allMeshes) {
                file.saveAll();
            }
        } catch (IOException ex) {
            Logger.getLogger(MapData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void close() {
        try {
            for (MeshIO file : allMeshes) {
                file.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(MapData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
