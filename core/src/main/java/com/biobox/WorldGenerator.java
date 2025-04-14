package com.biobox;

import com.badlogic.gdx.math.MathUtils;

public class WorldGenerator {
    public Map generateWorld(int width, int height) {
        // Use our custom PerlinNoiseGenerator
        PerlinNoiseGenerator noiseGen = new PerlinNoiseGenerator();
        Map map = new Map(width, height);
        
        // Use noise to determine terrain types
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Get noise value at this position
                float noiseValue = noiseGen.fbm(x * 0.1f, y * 0.1f, 4, 0.5f);
                
                // Map noise value to terrain type
                if (noiseValue < -0.3f) {
                    map.setTile(x, y, TileType.WATER);
                } else if (noiseValue < 0.3f) {
                    map.setTile(x, y, TileType.GRASS);
                } else {
                    map.setTile(x, y, TileType.WALL);
                }
            }
        }
        
        return map;
    }
}