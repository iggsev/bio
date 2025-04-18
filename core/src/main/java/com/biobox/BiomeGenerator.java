package com.biobox;

import com.badlogic.gdx.math.MathUtils;
import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.noise.NoiseGenerator;

/**
 * Generator for RPG world maps with different biome patterns
 */
public class BiomeGenerator {
    
    // Different world types
    public enum WorldType {
        CLASSIC,
        ARCHIPELAGO,
        CONTINENTS,
        PANGEA,
        ISLANDS,
        VOLCANIC
    }
    
    private int seed;
    
    public BiomeGenerator() {
        this.seed = MathUtils.random(1, 100000);
    }
    
    /**
     * Set a specific seed for deterministic generation
     */
    public void setSeed(int seed) {
        this.seed = seed;
    }
    
    /**
     * Generate a completely new world
     */
    public void generateWorld(SquareGrid grid, WorldType type) {
        // Get new random seed if not explicitly set
        if (seed <= 0) {
            seed = MathUtils.random(1, 100000);
        }
        
        // Choose generation type
        switch (type) {
            case CLASSIC:
                generateClassic(grid);
                break;
            case ARCHIPELAGO:
                generateArchipelago(grid);
                break;
            case CONTINENTS:
                generateContinents(grid);
                break;
            case PANGEA:
                generatePangea(grid);
                break;
            case ISLANDS:
                generateIslands(grid);
                break;
            case VOLCANIC:
                generateVolcanic(grid);
                break;
            default:
                generateClassic(grid);
                break;
        }
        
        // Apply finishing touches
        applyFinishingTouches(grid, type);
    }
    
    /**
     * Generate a classic RPG map with balanced biomes
     */
    private void generateClassic(SquareGrid grid) {
        // Create height map for determining land vs water
        Grid heightGrid = createNoiseGrid(grid.getWidth(), grid.getHeight(), 3, 0.7f);
        
        // Create another noise map for biome variety
        Grid biomeGrid = createNoiseGrid(grid.getWidth(), grid.getHeight(), 4, 0.6f, seed + 1);
        
        // Apply to tiles
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                float height = heightGrid.get(x, y);
                float biomeVar = biomeGrid.get(x, y);
                
                if (height < 0.3f) {
                    if (height < 0.15f) {
                        grid.setTile(x, y, BiomeType.DEEP_WATER);
                    } else {
                        grid.setTile(x, y, BiomeType.SHALLOW_WATER);
                    }
                } else if (height < 0.4f) {
                    grid.setTile(x, y, BiomeType.SAND);
                } else if (height < 0.8f) {
                    if (biomeVar < 0.3f) {
                        grid.setTile(x, y, BiomeType.SAVANNA);
                    } else if (biomeVar < 0.6f) {
                        grid.setTile(x, y, BiomeType.GRASS);
                    } else if (biomeVar < 0.85f) {
                        grid.setTile(x, y, BiomeType.FOREST);
                    } else {
                        grid.setTile(x, y, BiomeType.JUNGLE);
                    }
                } else {
                    if (height > 0.9f) {
                        grid.setTile(x, y, BiomeType.SNOW);
                    } else if (biomeVar < 0.5f) {
                        grid.setTile(x, y, BiomeType.HILLS);
                    } else {
                        grid.setTile(x, y, BiomeType.MOUNTAINS);
                    }
                }
            }
        }
    }
    
    /**
     * Generate archipelago world with many small islands
     */
    private void generateArchipelago(SquareGrid grid) {
        // Create height map
        Grid heightGrid = createNoiseGrid(grid.getWidth(), grid.getHeight(), 3, 0.7f);
        
        // Apply radial gradient to create island-like shapes
        applyRadialGradient(heightGrid, 0.4f);
        
        // Create another noise map for biome variety
        Grid biomeGrid = createNoiseGrid(grid.getWidth(), grid.getHeight(), 4, 0.6f, seed + 1);
        
        // Apply to tiles
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                float height = heightGrid.get(x, y);
                float biomeVar = biomeGrid.get(x, y);
                
                if (height < 0.3f) {
                    if (height < 0.15f) {
                        grid.setTile(x, y, BiomeType.DEEP_WATER);
                    } else {
                        grid.setTile(x, y, BiomeType.SHALLOW_WATER);
                    }
                } else if (height < 0.4f) {
                    grid.setTile(x, y, BiomeType.SAND);
                } else if (height < 0.7f) {
                    if (biomeVar < 0.4f) {
                        grid.setTile(x, y, BiomeType.GRASS);
                    } else if (biomeVar < 0.8f) {
                        grid.setTile(x, y, BiomeType.FOREST);
                    } else {
                        grid.setTile(x, y, BiomeType.JUNGLE);
                    }
                } else {
                    if (biomeVar < 0.5f) {
                        grid.setTile(x, y, BiomeType.HILLS);
                    } else {
                        grid.setTile(x, y, BiomeType.MOUNTAINS);
                    }
                }
            }
        }
    }
    
    /**
     * Generate continents world with large landmasses
     */
    private void generateContinents(SquareGrid grid) {
        // Create height map with larger features
        Grid heightGrid = createNoiseGrid(grid.getWidth(), grid.getHeight(), 5, 0.8f);
        
        // Create biome variation map
        Grid biomeGrid = createNoiseGrid(grid.getWidth(), grid.getHeight(), 4, 0.6f, seed + 1);
        
        // Create temperature variation (north-south gradient)
        Grid tempGrid = new Grid(grid.getWidth(), grid.getHeight());
        applyLatitudeGradient(tempGrid);
        
        // Apply to tiles
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                float height = heightGrid.get(x, y);
                float biomeVar = biomeGrid.get(x, y);
                float temp = tempGrid.get(x, y);
                
                if (height < 0.35f) {
                    if (height < 0.2f) {
                        grid.setTile(x, y, BiomeType.DEEP_WATER);
                    } else {
                        grid.setTile(x, y, BiomeType.SHALLOW_WATER);
                    }
                } else if (height < 0.45f) {
                    grid.setTile(x, y, BiomeType.SAND);
                } else if (height < 0.75f) {
                    // Northern areas (colder)
                    if (temp < 0.3f) {
                        if (biomeVar < 0.5f) {
                            grid.setTile(x, y, BiomeType.GRASS);
                        } else {
                            grid.setTile(x, y, BiomeType.FOREST);
                        }
                    }
                    // Middle areas (temperate)
                    else if (temp < 0.7f) {
                        if (biomeVar < 0.3f) {
                            grid.setTile(x, y, BiomeType.GRASS);
                        } else if (biomeVar < 0.6f) {
                            grid.setTile(x, y, BiomeType.SAVANNA);
                        } else if (biomeVar < 0.9f) {
                            grid.setTile(x, y, BiomeType.FOREST);
                        } else {
                            grid.setTile(x, y, BiomeType.SWAMP);
                        }
                    }
                    // Southern areas (hotter)
                    else {
                        if (biomeVar < 0.4f) {
                            grid.setTile(x, y, BiomeType.DESERT);
                        } else if (biomeVar < 0.7f) {
                            grid.setTile(x, y, BiomeType.SAVANNA);
                        } else {
                            grid.setTile(x, y, BiomeType.JUNGLE);
                        }
                    }
                } else {
                    if (temp < 0.4f || height > 0.9f) {
                        grid.setTile(x, y, BiomeType.SNOW);
                    } else if (height < 0.85f) {
                        grid.setTile(x, y, BiomeType.HILLS);
                    } else {
                        grid.setTile(x, y, BiomeType.MOUNTAINS);
                    }
                }
            }
        }
    }
    
    /**
     * Generate pangea world (one massive continent)
     */
    private void generatePangea(SquareGrid grid) {
        // Create height map
        Grid heightGrid = createNoiseGrid(grid.getWidth(), grid.getHeight(), 6, 0.9f);
        
        // Create a large central continent
        applyCentralContinent(heightGrid, 0.7f);
        
        // Create biome variation map
        Grid biomeGrid = createNoiseGrid(grid.getWidth(), grid.getHeight(), 4, 0.6f, seed + 1);
        
        // Create moisture map
        Grid moistureGrid = createNoiseGrid(grid.getWidth(), grid.getHeight(), 3, 0.7f, seed + 2);
        
        // Apply to tiles
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                float height = heightGrid.get(x, y);
                float biomeVar = biomeGrid.get(x, y);
                float moisture = moistureGrid.get(x, y);
                
                if (height < 0.25f) {
                    if (height < 0.1f) {
                        grid.setTile(x, y, BiomeType.DEEP_WATER);
                    } else {
                        grid.setTile(x, y, BiomeType.SHALLOW_WATER);
                    }
                } else if (height < 0.35f) {
                    grid.setTile(x, y, BiomeType.SAND);
                } else {
                    // Use moisture to determine inland biomes
                    if (moisture < 0.3f) {
                        // Dry areas
                        if (biomeVar < 0.3f) {
                            grid.setTile(x, y, BiomeType.DESERT);
                        } else {
                            grid.setTile(x, y, BiomeType.SAVANNA);
                        }
                    } else if (moisture < 0.6f) {
                        // Medium moisture
                        if (biomeVar < 0.5f) {
                            grid.setTile(x, y, BiomeType.GRASS);
                        } else if (height > 0.7f) {
                            grid.setTile(x, y, BiomeType.HILLS);
                        } else {
                            grid.setTile(x, y, BiomeType.FOREST);
                        }
                    } else {
                        // Wet areas
                        if (biomeVar < 0.3f) {
                            grid.setTile(x, y, BiomeType.SWAMP);
                        } else if (biomeVar < 0.7f) {
                            grid.setTile(x, y, BiomeType.FOREST);
                        } else if (height > 0.8f) {
                            grid.setTile(x, y, BiomeType.MOUNTAINS);
                        } else {
                            grid.setTile(x, y, BiomeType.JUNGLE);
                        }
                    }
                    
                    // Snow caps on high elevations
                    if (height > 0.85f) {
                        grid.setTile(x, y, BiomeType.SNOW);
                    }
                }
            }
        }
    }
    
    /**
     * Generate island world type
     */
    private void generateIslands(SquareGrid grid) {
        // Fill with deep water
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                grid.setTile(x, y, BiomeType.DEEP_WATER);
            }
        }
        
        // Create several distinct islands
        int numIslands = MathUtils.random(10, 20);
        for (int i = 0; i < numIslands; i++) {
            createIsland(grid, 
                MathUtils.random(grid.getWidth() / 8, grid.getWidth() * 7 / 8),
                MathUtils.random(grid.getHeight() / 8, grid.getHeight() * 7 / 8),
                MathUtils.random(3, Math.min(grid.getWidth(), grid.getHeight()) / 8),
                seed + i * 100);
        }
    }
    
    /**
     * Create a single island
     */
    private void createIsland(SquareGrid grid, int centerX, int centerY, int radius, int noiseSeed) {
        // Create noise grid for the island
        Grid noiseGrid = createNoiseGrid(grid.getWidth(), grid.getHeight(), 2, 0.5f, noiseSeed);
        
        // Create the island
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                float distance = distance(x, y, centerX, centerY);
                if (distance <= radius) {
                    float factor = 1.0f - (distance / radius);
                    float noise = noiseGrid.get(x, y);
                    
                    // Combine noise and distance for natural island shape
                    float value = factor * 0.7f + noise * 0.3f;
                    
                    if (value > 0.7f) {
                        // Center - higher ground
                        if (MathUtils.randomBoolean(0.3f)) {
                            grid.setTile(x, y, BiomeType.MOUNTAINS);
                        } else {
                            grid.setTile(x, y, BiomeType.FOREST);
                        }
                    } else if (value > 0.5f) {
                        // Middle area
                        if (MathUtils.randomBoolean(0.7f)) {
                            grid.setTile(x, y, BiomeType.GRASS);
                        } else {
                            grid.setTile(x, y, BiomeType.FOREST);
                        }
                    } else if (value > 0.4f) {
                        // Beach
                        grid.setTile(x, y, BiomeType.SAND);
                    } else if (value > 0.35f) {
                        // Shallow water
                        grid.setTile(x, y, BiomeType.SHALLOW_WATER);
                    }
                }
            }
        }
    }
    
    /**
     * Generate volcanic world type
     */
    private void generateVolcanic(SquareGrid grid) {
        // Create height map
        Grid heightGrid = createNoiseGrid(grid.getWidth(), grid.getHeight(), 4, 0.8f);
        
        // Apply radial gradient for volcanic island theme
        applyRadialGradient(heightGrid, 0.5f);
        
        // Create biome variation map
        Grid biomeGrid = createNoiseGrid(grid.getWidth(), grid.getHeight(), 3, 0.6f, seed + 1);
        
        // Add volcanic features - lava flows and ash
        Grid volcanoGrid = createNoiseGrid(grid.getWidth(), grid.getHeight(), 2, 0.9f, seed + 100);
        
        // Apply to tiles
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                float height = heightGrid.get(x, y);
                float biomeVar = biomeGrid.get(x, y);
                float volcanic = volcanoGrid.get(x, y);
                
                if (height < 0.3f) {
                    if (height < 0.15f) {
                        grid.setTile(x, y, BiomeType.DEEP_WATER);
                    } else {
                        grid.setTile(x, y, BiomeType.SHALLOW_WATER);
                    }
                } else if (height < 0.4f) {
                    grid.setTile(x, y, BiomeType.SAND);
                } else {
                    // Volcanic features
                    if (volcanic > 0.85f && height > 0.6f) {
                        grid.setTile(x, y, BiomeType.LAVA);
                    } else if (volcanic > 0.75f) {
                        // Ash and rock
                        grid.setTile(x, y, BiomeType.HILLS);
                    } else {
                        // Regular biomes
                        if (biomeVar < 0.5f) {
                            grid.setTile(x, y, BiomeType.GRASS);
                        } else if (biomeVar < 0.8f) {
                            grid.setTile(x, y, BiomeType.FOREST);
                        } else if (height > 0.8f) {
                            grid.setTile(x, y, BiomeType.MOUNTAINS);
                        } else {
                            grid.setTile(x, y, BiomeType.JUNGLE);
                        }
                    }
                    
                    // Mountain peaks
                    if (height > 0.9f && volcanic < 0.7f) {
                        grid.setTile(x, y, BiomeType.SNOW);
                    }
                }
            }
        }
    }
    
    /**
     * Create a noise grid
     */
    private Grid createNoiseGrid(int width, int height, int radius, float modifier) {
        return createNoiseGrid(width, height, radius, modifier, seed);
    }
    
    /**
     * Create a noise grid with a specific seed
     */
    private Grid createNoiseGrid(int width, int height, int radius, float modifier, int noiseSeed) {
        Grid noiseGrid = new Grid(width, height);
        
        NoiseGenerator noiseGenerator = new NoiseGenerator();
        noiseGenerator.setSeed(noiseSeed);
        noiseGenerator.setRadius(radius);
        noiseGenerator.setModifier(modifier);
        noiseGenerator.generate(noiseGrid);
        
        return noiseGrid;
    }
    
    /**
     * Apply a radial gradient to create island-like shapes
     */
    private void applyRadialGradient(Grid grid, float strength) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        float centerX = width / 2f;
        float centerY = height / 2f;
        float maxDist = (float) Math.sqrt(centerX * centerX + centerY * centerY);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float dist = (float) Math.sqrt(
                    (x - centerX) * (x - centerX) + 
                    (y - centerY) * (y - centerY)
                );
                float factor = dist / maxDist * strength;
                
                // Apply gradient: further from center = lower elevation
                grid.set(x, y, grid.get(x, y) - factor);
            }
        }
    }
    
    /**
     * Apply central continent effect
     */
    private void applyCentralContinent(Grid grid, float strength) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        float centerX = width / 2f;
        float centerY = height / 2f;
        float maxDist = (float) Math.sqrt(centerX * centerX + centerY * centerY);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float dist = (float) Math.sqrt(
                    (x - centerX) * (x - centerX) + 
                    (y - centerY) * (y - centerY)
                );
                
                // Create continent in central area
                float continentFactor;
                if (dist < maxDist * 0.4f) {
                    // Central area - raise height significantly
                    continentFactor = 0.5f;
                } else if (dist < maxDist * 0.7f) {
                    // Transition zone
                    float normalizedDist = (dist - maxDist * 0.4f) / (maxDist * 0.3f);
                    continentFactor = 0.5f - normalizedDist * 0.7f;
                } else {
                    // Outer area - deep ocean
                    continentFactor = -0.2f;
                }
                
                grid.set(x, y, grid.get(x, y) + continentFactor * strength);
            }
        }
    }
    
    /**
     * Apply a north-south gradient for temperature simulation
     */
    private void applyLatitudeGradient(Grid grid) {
        int height = grid.getHeight();
        
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < height; y++) {
                // Calculate normalized latitude (0 at equator, 1 at poles)
                float latitudeFactor = Math.abs(y - height / 2f) / (height / 2f);
                grid.set(x, y, latitudeFactor);
            }
        }
    }
    
    /**
     * Apply finishing touches to the world
     */
    private void applyFinishingTouches(SquareGrid grid, WorldType type) {
        // Add beaches around water
        addBeaches(grid);
        
        // Add type-specific features
        switch (type) {
            case VOLCANIC:
                addVolcanoes(grid);
                break;
            case PANGEA:
                addMountainRanges(grid);
                break;
            case CONTINENTS:
                addWalls(grid);
                break;
        }
    }
    
    /**
     * Add beaches around water
     */
    private void addBeaches(SquareGrid grid) {
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                if (grid.getTile(x, y) == BiomeType.GRASS) {
                    // Check if near water
                    boolean nearWater = false;
                    
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int nx = x + dx;
                            int ny = y + dy;
                            
                            if (grid.isInBounds(nx, ny)) {
                                BiomeType neighbor = grid.getTile(nx, ny);
                                if (neighbor == BiomeType.SHALLOW_WATER || 
                                    neighbor == BiomeType.DEEP_WATER) {
                                    nearWater = true;
                                    break;
                                }
                            }
                        }
                        if (nearWater) break;
                    }
                    
                    if (nearWater) {
                        grid.setTile(x, y, BiomeType.SAND);
                    }
                }
            }
        }
    }
    
    /**
     * Add volcanoes to volcanic world type
     */
    private void addVolcanoes(SquareGrid grid) {
        // Add some lava pools and volcanic mountains
        int numVolcanoes = MathUtils.random(3, 8);
        
        for (int i = 0; i < numVolcanoes; i++) {
            int x = MathUtils.random(grid.getWidth() / 4, grid.getWidth() * 3 / 4);
            int y = MathUtils.random(grid.getHeight() / 4, grid.getHeight() * 3 / 4);
            
            // Create volcano crater
            int craterRadius = MathUtils.random(2, 4);
            
            for (int dx = -craterRadius; dx <= craterRadius; dx++) {
                for (int dy = -craterRadius; dy <= craterRadius; dy++) {
                    int nx = x + dx;
                    int ny = y + dy;
                    
                    if (grid.isInBounds(nx, ny)) {
                        float dist = distance(nx, ny, x, y);
                        
                        if (dist <= craterRadius) {
                            if (dist < craterRadius * 0.6f) {
                                // Center - lava
                                grid.setTile(nx, ny, BiomeType.LAVA);
                            } else {
                                // Rim - mountains
                                grid.setTile(nx, ny, BiomeType.MOUNTAINS);
                            }
                        }
                    }
                }
            }
            
            // Create lava flow
            addLavaFlow(grid, x, y, MathUtils.random(5, 15));
        }
    }
    
    /**
     * Add a lava flow from a volcano
     */
    private void addLavaFlow(SquareGrid grid, int startX, int startY, int length) {
        // Pick a random direction
        float angle = MathUtils.random(MathUtils.PI2);
        int dx = (int)Math.round(Math.cos(angle));
        int dy = (int)Math.round(Math.sin(angle));
        
        int x = startX;
        int y = startY;
        
        for (int i = 0; i < length; i++) {
            x += dx;
            y += dy;
            
            if (!grid.isInBounds(x, y)) break;
            
            // Add lava
            grid.setTile(x, y, BiomeType.LAVA);
            
            // Randomly change direction slightly
            if (MathUtils.randomBoolean(0.3f)) {
                angle += MathUtils.random(-0.5f, 0.5f);
                dx = (int)Math.round(Math.cos(angle));
                dy = (int)Math.round(Math.sin(angle));
            }
        }
    }
    
    /**
     * Add mountain ranges to pangea world
     */
    private void addMountainRanges(SquareGrid grid) {
        int numRanges = MathUtils.random(2, 5);
        
        for (int i = 0; i < numRanges; i++) {
            int startX = MathUtils.random(grid.getWidth() / 4, grid.getWidth() * 3 / 4);
            int startY = MathUtils.random(grid.getHeight() / 4, grid.getHeight() * 3 / 4);
            int length = MathUtils.random(10, 30);
            
            // Create mountain range
            float angle = MathUtils.random(MathUtils.PI2);
            int dx = (int)Math.round(Math.cos(angle));
            int dy = (int)Math.round(Math.sin(angle));
            
            int x = startX;
            int y = startY;
            
            for (int j = 0; j < length; j++) {
                // Width of the range
                int width = MathUtils.random(1, 3);
                
                for (int wx = -width; wx <= width; wx++) {
                    for (int wy = -width; wy <= width; wy++) {
                        int nx = x + wx;
                        int ny = y + wy;
                        
                        if (grid.isInBounds(nx, ny)) {
                            float dist = distance(nx, ny, x, y);
                            
                            if (dist <= width) {
                                // Use mountains/snow/hills based on distance from center
                                if (dist < width * 0.3f) {
                                    grid.setTile(nx, ny, BiomeType.SNOW);
                                } else if (dist < width * 0.7f) {
                                    grid.setTile(nx, ny, BiomeType.MOUNTAINS);
                                } else {
                                    grid.setTile(nx, ny, BiomeType.HILLS);
                                }
                            }
                        }
                    }
                }
                
                // Move along the range
                x += dx;
                y += dy;
                
                if (!grid.isInBounds(x, y)) break;
                
                // Randomly change direction slightly
                if (MathUtils.randomBoolean(0.2f)) {
                    angle += MathUtils.random(-0.3f, 0.3f);
                    dx = (int)Math.round(Math.cos(angle));
                    dy = (int)Math.round(Math.sin(angle));
                }
            }
        }
    }
    
    /**
     * Add walls to continents world
     */
    private void addWalls(SquareGrid grid) {
        // Add some wall structures
        int numStructures = MathUtils.random(3, 8);
        
        for (int i = 0; i < numStructures; i++) {
            int startX = MathUtils.random(grid.getWidth() / 4, grid.getWidth() * 3 / 4);
            int startY = MathUtils.random(grid.getHeight() / 4, grid.getHeight() * 3 / 4);
            
            // Choose structure type
            int type = MathUtils.random(2);
            
            switch (type) {
                case 0:
                    // Wall line
                    addWallLine(grid, startX, startY, MathUtils.random(5, 15));
                    break;
                case 1:
                    // Wall circle
                    addWallCircle(grid, startX, startY, MathUtils.random(3, 7));
                    break;
                case 2:
                    // Wall rectangle
                    addWallRectangle(grid, startX, startY, 
                                   MathUtils.random(4, 10), MathUtils.random(4, 10));
                    break;
            }
        }
    }
    
    /**
     * Add a line of wall tiles
     */
    private void addWallLine(SquareGrid grid, int startX, int startY, int length) {
        float angle = MathUtils.random(MathUtils.PI2);
        int dx = (int)Math.round(Math.cos(angle));
        int dy = (int)Math.round(Math.sin(angle));
        
        int x = startX;
        int y = startY;
        
        for (int i = 0; i < length; i++) {
            if (grid.isInBounds(x, y)) {
                grid.setTile(x, y, BiomeType.WALL);
            }
            
            x += dx;
            y += dy;
            
            if (!grid.isInBounds(x, y)) break;
        }
    }
    
    /**
     * Add a circle of wall tiles
     */
    private void addWallCircle(SquareGrid grid, int centerX, int centerY, int radius) {
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                if (grid.isInBounds(x, y)) {
                    float dist = distance(x, y, centerX, centerY);
                    
                    // Only place walls on the circle edge
                    if (Math.abs(dist - radius) < 0.8f) {
                        grid.setTile(x, y, BiomeType.WALL);
                    }
                }
            }
        }
    }
    
    /**
     * Add a rectangle of wall tiles
     */
    private void addWallRectangle(SquareGrid grid, int centerX, int centerY, int width, int height) {
        int startX = centerX - width / 2;
        int startY = centerY - height / 2;
        int endX = startX + width;
        int endY = startY + height;
        
        // Draw the rectangle borders
        for (int x = startX; x <= endX; x++) {
            if (grid.isInBounds(x, startY)) {
                grid.setTile(x, startY, BiomeType.WALL);
            }
            if (grid.isInBounds(x, endY)) {
                grid.setTile(x, endY, BiomeType.WALL);
            }
        }
        
        for (int y = startY + 1; y < endY; y++) {
            if (grid.isInBounds(startX, y)) {
                grid.setTile(startX, y, BiomeType.WALL);
            }
            if (grid.isInBounds(endX, y)) {
                grid.setTile(endX, y, BiomeType.WALL);
            }
        }
    }
    
    /**
     * Calculate distance between two points
     */
    private float distance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }
    
    /**
     * Get a random world type
     */
    public WorldType getRandomWorldType() {
        return WorldType.values()[MathUtils.random(WorldType.values().length - 1)];
    }
}