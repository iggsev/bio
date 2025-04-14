package com.biobox;

import com.badlogic.gdx.math.MathUtils;

public class Map {
    private TileType[][] tiles;
    private final int width;
    private final int height;

    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        tiles = new TileType[width][height];
        
        // Initialize map with grass
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = TileType.GRASS;
            }
        }
    }

    public TileType getTile(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return tiles[x][y];
        }
        return TileType.WALL; // Default to wall for out-of-bounds
    }

    public void setTile(int x, int y, TileType type) {
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
}