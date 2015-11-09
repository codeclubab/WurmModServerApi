package com.wurmonline.wurmapi.api;

import com.wurmonline.wurmapi.api.map.dump.Colorist;

import java.io.File;
import java.io.IOException;

public class WurmAPI {
    
    /**
     * Creates new WurmAPI instance. This method must be used on existing and valid world directory.
     * 
     * @param worldDirectory path to existing world directory.
     * @return WurmAPI instance
     */
    public static WurmAPI open(String worldDirectory) throws IOException {
        return new WurmAPI(worldDirectory);
    }
    
    /**
     * Creates new WurmAPI instance.
     * 
     * @param worldDirectory path to new or existing world directory.
     * @param powerOfTwo power of two of new map (must be between 10 and 15)
     * @return WurmAPI instance
     */
    public static WurmAPI create(String worldDirectory, int powerOfTwo) throws IOException {
        return new WurmAPI(worldDirectory, powerOfTwo);
    }
    
    private final String rootDir;
    private final MapData mapData;
    
    private WurmAPI(String worldDirectory) throws IOException {
        this.rootDir = worldDirectory + File.separator;
        File file = new File(rootDir);
        file.mkdirs();
        
        this.mapData = new MapData(rootDir);
    }
    
    private WurmAPI(String worldDirectory, int powerOfTwo) throws IOException {
        if (powerOfTwo < 10 || powerOfTwo > 15) {
            throw new IllegalArgumentException("Invalid map size: map with size 2^" + powerOfTwo + " cannot be created");
        }
        
        this.rootDir = worldDirectory + File.separator;
        File file = new File(rootDir);
        file.mkdirs();
        
        this.mapData = new MapData(rootDir, powerOfTwo);
    }
    
    public MapData getMapData() {
        return mapData;
    }
    
    /**
     * Releases all native resources used by WurmAPI. It shouldn't be used after calling this method.
     */
    public void close() {
        mapData.close();
    }
    
}
