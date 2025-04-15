package com.biobox;

import com.badlogic.gdx.math.MathUtils;
import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.noise.NoiseGenerator;

public class WorldGenerator {
    
    // Different noise seeds for variety
    private final int DEFAULT_SEED = 12345;
    private final int ISLAND_SEED = 54321;
    private final int CONTINENT_SEED = 98765;
    private final int LAKES_SEED = 11111;
    
    // Terrain generation thresholds
    private static final float WATER_THRESHOLD = 0.3f;
    private static final float SAND_THRESHOLD = 0.35f;
    private static final float GRASS_THRESHOLD = 0.7f;
    private static final float DIRT_THRESHOLD = 0.8f;
    private static final float STONE_THRESHOLD = 0.9f;
    
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
        
        // Add some decorative features to make the map more interesting
        addFeatures(map, width, height, type);
        
        return map;
    }
    
    // For backward compatibility
    public Map generateWorld(int width, int height) {
        return generateWorld(width, height, MapEditorScreen.MapType.DEFAULT);
    }
    
    private void generateDefault(Map map, int width, int height) {
        // Basic noise-based terrain with more tile types
        Grid grid = new Grid(width, height);
        NoiseGenerator noiseGenerator = new NoiseGenerator();
        noiseGenerator.setSeed(DEFAULT_SEED);
        noiseGenerator.setRadius(2);
        noiseGenerator.setModifier(0.6f);
        noiseGenerator.generate(grid);
        
        // Second noise layer for more natural terrain
        Grid detailGrid = new Grid(width, height);
        NoiseGenerator detailGenerator = new NoiseGenerator();
        detailGenerator.setSeed(DEFAULT_SEED + 1);
        detailGenerator.setRadius(1);
        detailGenerator.setModifier(0.4f);
        detailGenerator.generate(detailGrid);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float noiseValue = grid.get(x, y) + detailGrid.get(x, y) * 0.3f;
                setTileBasedOnElevation(map, x, y, noiseValue);
            }
        }
    }
    
    private void generateIsland(Map map, int width, int height) {
        // Create an island surrounded by water
        Grid grid = new Grid(width, height);
        NoiseGenerator noiseGenerator = new NoiseGenerator();
        noiseGenerator.setSeed(ISLAND_SEED);
        noiseGenerator.setRadius(2);
        noiseGenerator.setModifier(0.7f);
        noiseGenerator.generate(grid);
        
        // Detail noise for variety
        Grid detailGrid = new Grid(width, height);
        NoiseGenerator detailGenerator = new NoiseGenerator();
        detailGenerator.setSeed(ISLAND_SEED + 1);
        detailGenerator.setRadius(1);
        detailGenerator.setModifier(0.3f);
        detailGenerator.generate(detailGrid);
        
        float centerX = width / 2f;
        float centerY = height / 2f;
        float maxDistance = Math.min(width, height) * 0.4f; // Island radius
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float distanceFromCenter = distanceToCenter(x, y, centerX, centerY);
                
                // Distance factor (0 at center, 1 at edges)
                float distFactor = Math.min(1f, distanceFromCenter / maxDistance);
                
                // Combine noise with distance for natural-looking island
                float combinedValue = (grid.get(x, y) + detailGrid.get(x, y) * 0.2f) - distFactor * 0.8f;
                
                setTileBasedOnElevation(map, x, y, combinedValue);
                
                // Force water at the edges
                if (distFactor > 0.95f) {
                    map.setTile(x, y, TileType.WATER);
                }
                // Add sandy beaches around the perimeter
                else if (distFactor > 0.8f && map.getTile(x, y) == TileType.GRASS) {
                    map.setTile(x, y, TileType.SAND);
                }
            }
        }
    }
    
    private void generateContinent(Map map, int width, int height) {
        // Create large landmass with coastal features
        Grid grid = new Grid(width, height);
        NoiseGenerator noiseGenerator = new NoiseGenerator();
        noiseGenerator.setSeed(CONTINENT_SEED);
        noiseGenerator.setRadius(3);
        noiseGenerator.setModifier(0.8f);
        noiseGenerator.generate(grid);
        
        // Second noise layer for more natural terrain
        Grid detailGrid = new Grid(width, height);
        NoiseGenerator detailGenerator = new NoiseGenerator();
        detailGenerator.setSeed(CONTINENT_SEED + 1);
        detailGenerator.setRadius(1);
        detailGenerator.setModifier(0.3f);
        detailGenerator.generate(detailGrid);
        
        // Third noise layer for mountains and valleys
        Grid mountainGrid = new Grid(width, height);
        NoiseGenerator mountainGenerator = new NoiseGenerator();
        mountainGenerator.setSeed(CONTINENT_SEED + 2);
        mountainGenerator.setRadius(4);
        mountainGenerator.setModifier(0.5f);
        mountainGenerator.generate(mountainGrid);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Combine all noise layers
                float mainNoise = grid.get(x, y);
                float detailNoise = detailGrid.get(x, y) * 0.3f;
                float mountainNoise = mountainGrid.get(x, y) * 0.5f;
                float combinedNoise = mainNoise + detailNoise + mountainNoise;
                
                setTileBasedOnElevation(map, x, y, combinedNoise);
                
                // Create beaches near water
                if (map.getTile(x, y) == TileType.GRASS) {
                    // Check if near water
                    boolean nearWater = false;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if (x + dx >= 0 && x + dx < width && y + dy >= 0 && y + dy < height) {
                                if (map.getTile(x + dx, y + dy) == TileType.WATER) {
                                    nearWater = true;
                                    break;
                                }
                            }
                        }
                        if (nearWater) break;
                    }
                    
                    if (nearWater) {
                        map.setTile(x, y, TileType.SAND);
                    }
                }
            }
        }
    }
    
    private void generateLakes(Map map, int width, int height) {
        // Create terrain with many lakes
        Grid grid = new Grid(width, height);
        NoiseGenerator noiseGenerator = new NoiseGenerator();
        noiseGenerator.setSeed(LAKES_SEED);
        noiseGenerator.setRadius(2);
        noiseGenerator.setModifier(0.6f);
        noiseGenerator.generate(grid);
        
        // Additional layer for lake placement
        Grid lakeGrid = new Grid(width, height);
        NoiseGenerator lakeGenerator = new NoiseGenerator();
        lakeGenerator.setSeed(LAKES_SEED + 1);
        lakeGenerator.setRadius(3);
        lakeGenerator.setModifier(0.7f);
        lakeGenerator.generate(lakeGrid);
        
        // Layer for terrain variety
        Grid varietyGrid = new Grid(width, height);
        NoiseGenerator varietyGenerator = new NoiseGenerator();
        varietyGenerator.setSeed(LAKES_SEED + 2);
        varietyGenerator.setRadius(1);
        varietyGenerator.setModifier(0.3f);
        varietyGenerator.generate(varietyGrid);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float terrainNoise = grid.get(x, y) + varietyGrid.get(x, y) * 0.2f;
                float lakeNoise = lakeGrid.get(x, y);
                
                // Create lakes where the lake noise is low
                if (lakeNoise < 0.2f && terrainNoise > 0.3f) {
                    map.setTile(x, y, TileType.WATER);
                } else {
                    setTileBasedOnElevation(map, x, y, terrainNoise);
                }
                
                // Add sand around lakes
                if (map.getTile(x, y) == TileType.GRASS) {
                    // Check if near water
                    boolean nearWater = false;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if (x + dx >= 0 && x + dx < width && y + dy >= 0 && y + dy < height) {
                                if (map.getTile(x + dx, y + dy) == TileType.WATER) {
                                    nearWater = true;
                                    break;
                                }
                            }
                        }
                        if (nearWater) break;
                    }
                    
                    if (nearWater && MathUtils.randomBoolean(0.7f)) {
                        map.setTile(x, y, TileType.SAND);
                    }
                }
            }
        }
    }
    
    /**
     * Add decorative features to the map based on its type
     */
    private void addFeatures(Map map, int width, int height, MapEditorScreen.MapType type) {
        // Add stone patches/mountains
        Grid stoneGrid = new Grid(width, height);
        NoiseGenerator stoneGenerator = new NoiseGenerator();
        stoneGenerator.setSeed(87654);
        stoneGenerator.setRadius(2);
        stoneGenerator.setModifier(0.7f);
        stoneGenerator.generate(stoneGrid);
        
        // Add dirt patches
        Grid dirtGrid = new Grid(width, height);
        NoiseGenerator dirtGenerator = new NoiseGenerator();
        dirtGenerator.setSeed(34567);
        dirtGenerator.setRadius(1);
        dirtGenerator.setModifier(0.5f);
        dirtGenerator.generate(dirtGrid);
        
        try {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    TileType currentTile = map.getTile(x, y);
                    
                    // Skip water and non-walkable tiles
                    if (!currentTile.isWalkable()) continue;
                    
                    float stoneNoise = stoneGrid.get(x, y);
                    float dirtNoise = dirtGrid.get(x, y);
                    
                    // Add stone features
                    if (currentTile == TileType.GRASS && stoneNoise > 0.85f) {
                        map.setTile(x, y, TileType.STONE);
                    }
                    
                    // Add dirt paths
                    if (currentTile == TileType.GRASS && dirtNoise > 0.88f) {
                        map.setTile(x, y, TileType.DIRT);
                    }
                    
                    // Add more sand near existing sand
                    if (currentTile == TileType.GRASS) {
                        int sandNeighbors = 0;
                        for (int dx = -1; dx <= 1; dx++) {
                            for (int dy = -1; dy <= 1; dy++) {
                                if (x + dx >= 0 && x + dx < width && y + dy >= 0 && y + dy < height) {
                                    if (map.getTile(x + dx, y + dy) == TileType.SAND) {
                                        sandNeighbors++;
                                    }
                                }
                            }
                        }
                        
                        if (sandNeighbors >= 3 && MathUtils.randomBoolean(0.7f)) {
                            map.setTile(x, y, TileType.SAND);
                        }
                    }
                }
            }
            
            // Add special features based on map type
            switch (type) {
                case ISLAND:
                    // Add stone circle in the island center (like a volcano or ancient monument)
                    addStoneCircle(map, width/2, height/2, 5 + MathUtils.random(3));
                    break;
                    
                case CONTINENT:
                    // Add stone mountain ranges
                    for (int i = 0; i < 3; i++) {
                        int mountainX = MathUtils.random(width/4, width*3/4);
                        int mountainY = MathUtils.random(height/4, height*3/4);
                        addMountainRange(map, mountainX, mountainY, 10 + MathUtils.random(10));
                    }
                    break;
                    
                case LAKES:
                    // Add stone bridges between some lakes
                    for (int i = 0; i < 5; i++) {
                        int x1 = MathUtils.random(width/4, width*3/4);
                        int y1 = MathUtils.random(height/4, height*3/4);
                        int x2 = x1 + MathUtils.random(-15, 15);
                        int y2 = y1 + MathUtils.random(-15, 15);
                        
                        // Only add if near water
                        boolean nearWater1 = false;
                        boolean nearWater2 = false;
                        
                        for (int dx = -2; dx <= 2; dx++) {
                            for (int dy = -2; dy <= 2; dy++) {
                                if (isValidCoordinate(x1 + dx, y1 + dy, width, height) && 
                                    map.getTile(x1 + dx, y1 + dy) == TileType.WATER) {
                                    nearWater1 = true;
                                }
                                if (isValidCoordinate(x2 + dx, y2 + dy, width, height) && 
                                    map.getTile(x2 + dx, y2 + dy) == TileType.WATER) {
                                    nearWater2 = true;
                                }
                            }
                        }
                        
                        if (nearWater1 && nearWater2) {
                            addStonePath(map, x1, y1, x2, y2);
                        }
                    }
                    break;
                
                default:
                    // Add some random stone paths
                    for (int i = 0; i < 3; i++) {
                        int x1 = MathUtils.random(width/4, width*3/4);
                        int y1 = MathUtils.random(height/4, height*3/4);
                        int x2 = x1 + MathUtils.random(-20, 20);
                        int y2 = y1 + MathUtils.random(-20, 20);
                        
                        addStonePath(map, x1, y1, x2, y2);
                    }
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error adding features: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Set tile type based on "elevation" (noise value)
     */
    private void setTileBasedOnElevation(Map map, int x, int y, float elevation) {
        try {
            if (elevation < WATER_THRESHOLD) {
                map.setTile(x, y, TileType.WATER);
            } else if (elevation < SAND_THRESHOLD) {
                map.setTile(x, y, TileType.SAND);
            } else if (elevation < GRASS_THRESHOLD) {
                map.setTile(x, y, TileType.GRASS);
            } else if (elevation < DIRT_THRESHOLD) {
                map.setTile(x, y, TileType.DIRT);
            } else if (elevation < STONE_THRESHOLD) {
                map.setTile(x, y, TileType.STONE);
            } else {
                map.setTile(x, y, TileType.WALL); // Highest elevation = cliffs/walls
            }
        } catch (Exception e) {
            System.err.println("Error setting tile at (" + x + "," + y + "): " + e.getMessage());
            // Default to grass if there's an error
            try {
                map.setTile(x, y, TileType.GRASS);
            } catch (Exception ex) {
                // Ignore if we can't even set it to grass
            }
        }
    }
    
    private float distanceToCenter(float x, float y, float centerX, float centerY) {
        return (float) Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
    }
    
    private boolean isValidCoordinate(int x, int y, int width, int height) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    
    /**
     * Add a stone circle centered at the given position
     */
    private void addStoneCircle(Map map, int centerX, int centerY, int radius) {
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                if (isValidCoordinate(x, y, map.getWidth(), map.getHeight())) {
                    float distance = distanceToCenter(x, y, centerX, centerY);
                    
                    if (distance <= radius && distance >= radius - 1) {
                        map.setTile(x, y, TileType.STONE);
                    } else if (distance <= radius - 1 && distance >= radius - 2) {
                        // Inner ring can be different
                        if (MathUtils.randomBoolean(0.7f)) {
                            map.setTile(x, y, TileType.STONE);
                        } else {
                            map.setTile(x, y, TileType.DIRT);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Add a mountain range centered at the given position
     */
    private void addMountainRange(Map map, int centerX, int centerY, int size) {
        // Create a noise-based mountain range
        NoiseGenerator noiseGenerator = new NoiseGenerator();
        noiseGenerator.setSeed(MathUtils.random(1, 99999));
        noiseGenerator.setRadius(2);
        
        Grid mountainGrid = new Grid(size * 2, size * 2);
        noiseGenerator.generate(mountainGrid);
        
        for (int x = centerX - size; x <= centerX + size; x++) {
            for (int y = centerY - size; y <= centerY + size; y++) {
                if (isValidCoordinate(x, y, map.getWidth(), map.getHeight())) {
                    float distance = distanceToCenter(x, y, centerX, centerY) / size;
                    
                    if (distance < 1.0f) {
                        // Get a noise value for this position in the mountain range
                        int localX = x - (centerX - size);
                        int localY = y - (centerY - size);
                        
                        float noise = mountainGrid.get(
                            MathUtils.clamp(localX, 0, size * 2 - 1), 
                            MathUtils.clamp(localY, 0, size * 2 - 1)
                        );
                        
                        // Combine with distance to create a believable mountain
                        float mountainValue = noise - (distance * 0.8f);
                        
                        if (mountainValue > 0.6f) {
                            map.setTile(x, y, TileType.WALL); // Mountain peak
                        } else if (mountainValue > 0.4f) {
                            map.setTile(x, y, TileType.STONE); // Mountain slope
                        } else if (mountainValue > 0.2f && MathUtils.randomBoolean(0.7f)) {
                            map.setTile(x, y, TileType.STONE); // Lower slope
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Add a stone path between two points
     */
    private void addStonePath(Map map, int x1, int y1, int x2, int y2) {
        // Use Bresenham's line algorithm with some randomness
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        
        while (true) {
            if (isValidCoordinate(x1, y1, map.getWidth(), map.getHeight())) {
                // Only replace walkable tiles
                if (map.getTile(x1, y1).isWalkable()) {
                    map.setTile(x1, y1, TileType.STONE);
                    
                    // Add some width to the path
                    for (int wx = -1; wx <= 1; wx++) {
                        for (int wy = -1; wy <= 1; wy++) {
                            if (wx == 0 && wy == 0) continue;
                            
                            int nx = x1 + wx;
                            int ny = y1 + wy;
                            
                            if (isValidCoordinate(nx, ny, map.getWidth(), map.getHeight()) && 
                                map.getTile(nx, ny).isWalkable() && 
                                MathUtils.randomBoolean(0.3f)) {
                                map.setTile(nx, ny, TileType.STONE);
                            }
                        }
                    }
                }
            }
            
            if (x1 == x2 && y1 == y2) break;
            
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
                // Add some randomness to the path
                if (MathUtils.randomBoolean(0.2f)) {
                    y1 += (MathUtils.randomBoolean() ? 1 : -1);
                }
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
                // Add some randomness to the path
                if (MathUtils.randomBoolean(0.2f)) {
                    x1 += (MathUtils.randomBoolean() ? 1 : -1);
                }
            }
        }
    }
}