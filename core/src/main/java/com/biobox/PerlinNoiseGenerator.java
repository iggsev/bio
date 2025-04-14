package com.biobox;

import com.badlogic.gdx.math.MathUtils;

/**
 * A custom Perlin noise generator implementation for libGDX
 */
public class PerlinNoiseGenerator {
    private static final int PERMUTATION_SIZE = 256;
    private final int[] permutation = new int[PERMUTATION_SIZE * 2];
    
    /**
     * Creates a new Perlin noise generator with random permutation table
     */
    public PerlinNoiseGenerator() {
        this(MathUtils.random.nextLong());
    }
    
    /**
     * Creates a new Perlin noise generator with the given seed
     * 
     * @param seed Random seed for the noise generator
     */
    public PerlinNoiseGenerator(long seed) {
        // Initialize permutation with random numbers
        for (int i = 0; i < PERMUTATION_SIZE; i++) {
            permutation[i] = i;
        }
        
        // Shuffle permutation using Fisher-Yates algorithm
        MathUtils.random.setSeed(seed);
        for (int i = PERMUTATION_SIZE - 1; i > 0; i--) {
            int j = MathUtils.random.nextInt(i + 1);
            int temp = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = temp;
        }
        
        // Duplicate the permutation table
        for (int i = 0; i < PERMUTATION_SIZE; i++) {
            permutation[i + PERMUTATION_SIZE] = permutation[i];
        }
    }
    
    /**
     * Generates 2D Perlin noise value at the given coordinates
     * 
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @return A noise value between -1 and 1
     */
    public float noise(float x, float y) {
        int X = (int)Math.floor(x) & 255;
        int Y = (int)Math.floor(y) & 255;
        
        x -= Math.floor(x);
        y -= Math.floor(y);
        
        float u = fade(x);
        float v = fade(y);
        
        int A = permutation[X] + Y;
        int B = permutation[X + 1] + Y;
        
        return lerp(v, 
                lerp(u, grad(permutation[A], x, y), grad(permutation[B], x - 1, y)),
                lerp(u, grad(permutation[A + 1], x, y - 1), grad(permutation[B + 1], x - 1, y - 1)));
    }
    
    /**
     * Generates 2D noise with multiple octaves (fractal Brownian motion)
     * 
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @param octaves Number of octaves to generate
     * @param persistence How much each octave contributes to the final result
     * @return A noise value between -1 and 1
     */
    public float fbm(float x, float y, int octaves, float persistence) {
        float total = 0;
        float frequency = 1;
        float amplitude = 1;
        float maxValue = 0;
        
        for (int i = 0; i < octaves; i++) {
            total += noise(x * frequency, y * frequency) * amplitude;
            
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2;
        }
        
        return total / maxValue;
    }
    
    private static float fade(float t) {
        // Improved fade function: 6t^5 - 15t^4 + 10t^3
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
    
    private static float lerp(float t, float a, float b) {
        return a + t * (b - a);
    }
    
    private static float grad(int hash, float x, float y) {
        int h = hash & 3;
        float u = (h < 2) ? x : y;
        float v = (h < 2) ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}