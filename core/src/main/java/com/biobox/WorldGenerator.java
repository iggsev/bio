package com.biobox;

import com.badlogic.gdx.math.MathUtils;
import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.noise.NoiseGenerator;

public class WorldGenerator {
    public Map generateWorld(int width, int height, MapEditorScreen.MapType type) {
        Map map = new Map(width, height);
        
        switch (type) {
            case ISLAND:
                generateIsland(map, width, height);
                break;
            case CONTINENT:
                generateContinent(map, width, height);
                break;
            case LAKES:
                generateLakes(map, width, height);
                break;
            case DEFAULT:
            default:
                generateDefault(map, width, height);
                break;
        }
        
        return map;
    }
    
    // For backward compatibility
    public Map generateWorld(int width, int height) {
        return generateWorld(width, height, MapEditorScreen.MapType.DEFAULT);
    }
    
    private void generateDefault(Map map, int width, int height) {
        // Basic noise-based terrain
        Grid grid = new Grid(width, height);
        NoiseGenerator noiseGenerator = new NoiseGenerator();
        noiseGenerator.setSeed(12345);
        noiseGenerator.setRadius(1);
        noiseGenerator.setModifier(1);
        noiseGenerator.generate(grid);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float noiseValue = grid.get(x, y);
                
                if (noiseValue < 0.3f) {
                    map.setTile(x, y, TileType.WATER);
                } else if (noiseValue < 0.7f) {
                    map.setTile(x, y, TileType.GRASS);
                } else {
                    map.setTile(x, y, TileType.WALL);
                }
            }
        }
    }
    
    private void generateIsland(Map map, int width, int height) {
        // Create an island surrounded by water
        Grid grid = new Grid(width, height);
        NoiseGenerator noiseGenerator = new NoiseGenerator();
        noiseGenerator.setSeed(54321);
        noiseGenerator.setRadius(2);
        noiseGenerator.setModifier(1);
        noiseGenerator.generate(grid);
        
        float centerX = width / 2f;
        float centerY = height / 2f;
        float maxDistance = Math.min(width, height) * 0.4f; // Island radius
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float distanceFromCenter = (float) Math.sqrt(
                    Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2)
                );
                
                // Distance factor (0 at center, 1 at edges)
                float distFactor = Math.min(1f, distanceFromCenter / maxDistance);
                
                // Combine noise with distance for natural-looking island
                float combinedValue = grid.get(x, y) - distFactor;
                
                if (combinedValue < 0.1f) {
                    map.setTile(x, y, TileType.WATER);
                } else if (combinedValue < 0.4f) {
                    map.setTile(x, y, TileType.GRASS);
                } else {
                    map.setTile(x, y, TileType.WALL);
                }
            }
        }
    }
    
    private void generateContinent(Map map, int width, int height) {
        // Create large landmass with coastal features
        Grid grid = new Grid(width, height);
        NoiseGenerator noiseGenerator = new NoiseGenerator();
        noiseGenerator.setSeed(98765);
        noiseGenerator.setRadius(3);
        noiseGenerator.setModifier(1);
        noiseGenerator.generate(grid);
        
        // Second noise layer for more natural terrain
        Grid detailGrid = new Grid(width, height);
        NoiseGenerator detailGenerator = new NoiseGenerator();
        detailGenerator.setSeed(12345);
        detailGenerator.setRadius(1);
        detailGenerator.setModifier(1);
        detailGenerator.generate(detailGrid);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Combine both noise layers
                float mainNoise = grid.get(x, y);
                float detailNoise = detailGrid.get(x, y) * 0.3f;
                float combinedNoise = mainNoise + detailNoise;
                
                if (combinedNoise < 0.25f) {
                    map.setTile(x, y, TileType.WATER);
                } else if (combinedNoise < 0.6f) {
                    map.setTile(x, y, TileType.GRASS);
                } else {
                    map.setTile(x, y, TileType.WALL);
                }
            }
        }
    }
    
    private void generateLakes(Map map, int width, int height) {
        // Create terrain with many lakes
        Grid grid = new Grid(width, height);
        NoiseGenerator noiseGenerator = new NoiseGenerator();
        noiseGenerator.setSeed(11111);
        noiseGenerator.setRadius(1);
        noiseGenerator.setModifier(1);
        noiseGenerator.generate(grid);
        
        // Additional layer for lake placement
        Grid lakeGrid = new Grid(width, height);
        NoiseGenerator lakeGenerator = new NoiseGenerator();
        lakeGenerator.setSeed(22222);
        lakeGenerator.setRadius(2);
        lakeGenerator.setModifier(1);
        lakeGenerator.generate(lakeGrid);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float terrainNoise = grid.get(x, y);
                float lakeNoise = lakeGrid.get(x, y);
                
                // Create lakes where the lake noise is low but terrain noise is high
                if (lakeNoise < 0.2f && terrainNoise > 0.4f) {
                    map.setTile(x, y, TileType.WATER);
                } else if (terrainNoise < 0.6f) {
                    map.setTile(x, y, TileType.GRASS);
                } else {
                    map.setTile(x, y, TileType.WALL);
                }
            }
        }
    }
}