package com.biobox;

/**
 * A simple grid structure using square tiles (replacing HexGrid)
 */
public class SquareGrid {
    private BiomeType[][] tiles;
    private final int width;
    private final int height;

    public SquareGrid(int width, int height) {
        this.width = width;
        this.height = height;
        tiles = new BiomeType[width][height];
        
        // Initialize with default biome
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = BiomeType.GRASS;
            }
        }
    }

    public BiomeType getTile(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return tiles[x][y];
        }
        // Return null for out-of-bounds
        return null;
    }

    public void setTile(int x, int y, BiomeType type) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            tiles[x][y] = type;
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}