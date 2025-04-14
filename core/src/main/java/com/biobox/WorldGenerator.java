package com.biobox;

import com.badlogic.gdx.math.PerlinNoiseGenerator;

public class WorldGenerator {
    public static TileType[][] generateIsland(int width, int height) {
        TileType[][] map = new TileType[width][height];
        PerlinNoiseGenerator noiseGen = new PerlinNoiseGenerator();
        float scale = 10.0f;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float nx = (float) x / width * scale;
                float ny = (float) y / height * scale;
                float noise = noiseGen.generate(nx, ny, 5, 0.5f, 1.0f);
                if (noise < 0.3f) {
                    map[x][y] = TileType.WATER;
                } else if (noise < 0.7f) {
                    map[x][y] = TileType.GRASS;
                } else {
                    map[x][y] = TileType.WALL;
                }
            }
        }
        return map;
    }
}