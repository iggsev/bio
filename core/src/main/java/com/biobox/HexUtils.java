package com.biobox;

import com.badlogic.gdx.math.Vector2;

/**
 * Utility class for hexagon calculations
 */
public class HexUtils {
    // Hexagon constants
    public static final float HEX_SIZE = 10f; // Size of hexagon (from center to corner)
    public static final float HEX_WIDTH = HEX_SIZE * 2;
    public static final float HEX_HEIGHT = (float) (Math.sqrt(3) * HEX_SIZE);
    
    // Neighbor direction vectors (axial coordinates)
    private static final int[][] NEIGHBORS = {
        {1, 0}, {1, -1}, {0, -1},
        {-1, 0}, {-1, 1}, {0, 1}
    };
    
    /**
     * Convert axial coordinates (q,r) to world position (x,y)
     */
    public static Vector2 axialToWorld(int q, int r) {
        float x = HEX_SIZE * (3f/2f * q);
        float y = HEX_SIZE * ((float)Math.sqrt(3)/2f * q + (float)Math.sqrt(3) * r);
        return new Vector2(x, y);
    }
    
    /**
     * Convert world position (x,y) to axial coordinates (q,r)
     */
    public static int[] worldToAxial(float x, float y) {
        float q = (2f/3f * x) / HEX_SIZE;
        float r = (-1f/3f * x + (float)Math.sqrt(3)/3f * y) / HEX_SIZE;
        
        // Convert to cube coordinates for rounding
        float cubeX = q;
        float cubeZ = r;
        float cubeY = -cubeX - cubeZ;
        
        // Round cube coordinates
        int rx = Math.round(cubeX);
        int ry = Math.round(cubeY);
        int rz = Math.round(cubeZ);
        
        // Fix rounding errors
        float xDiff = Math.abs(rx - cubeX);
        float yDiff = Math.abs(ry - cubeY);
        float zDiff = Math.abs(rz - cubeZ);
        
        if (xDiff > yDiff && xDiff > zDiff) {
            rx = -ry - rz;
        } else if (yDiff > zDiff) {
            ry = -rx - rz;
        } else {
            rz = -rx - ry;
        }
        
        // Convert back to axial
        return new int[] {rx, rz};
    }
    
    /**
     * Get the coordinates of a neighbor in a specific direction
     * Direction: 0 = east, 1 = northeast, 2 = northwest, 3 = west, 4 = southwest, 5 = southeast
     */
    public static int[] getNeighborCoordinates(int q, int r, int direction) {
        direction = ((direction % 6) + 6) % 6; // Ensure direction is 0-5
        int[] neighbor = NEIGHBORS[direction];
        return new int[] {q + neighbor[0], r + neighbor[1]};
    }
    
    /**
     * Calculate distance between two hex tiles in axial coordinates
     */
    public static int getDistance(int q1, int r1, int q2, int r2) {
        // Convert to cube coordinates
        int x1 = q1;
        int z1 = r1;
        int y1 = -x1 - z1;
        
        int x2 = q2;
        int z2 = r2;
        int y2 = -x2 - z2;
        
        // Calculate Manhattan distance in cube coordinates
        return (Math.abs(x1 - x2) + Math.abs(y1 - y2) + Math.abs(z1 - z2)) / 2;
    }
    
    /**
     * Get the six corners of a hexagon at world position (x,y)
     */
    public static Vector2[] getHexCorners(float x, float y) {
        Vector2[] corners = new Vector2[6];
        
        for (int i = 0; i < 6; i++) {
            float angle = (float)(2 * Math.PI / 6 * i);
            corners[i] = new Vector2(
                x + HEX_SIZE * (float)Math.cos(angle),
                y + HEX_SIZE * (float)Math.sin(angle)
            );
        }
        
        return corners;
    }
    
    /**
     * Get the six corners of a hexagon at axial coordinates (q,r)
     */
    public static Vector2[] getHexCorners(int q, int r) {
        Vector2 center = axialToWorld(q, r);
        return getHexCorners(center.x, center.y);
    }
    
    /**
     * Return all axial coordinates in a radius around center (q,r)
     */
    public static int[][] getAxialCircle(int q, int r, int radius) {
        int count = 1;
        for (int i = 1; i <= radius; i++) {
            count += i * 6;
        }
        
        int[][] results = new int[count][2];
        int index = 0;
        
        // Add center
        results[index][0] = q;
        results[index][1] = r;
        index++;
        
        // Add rings
        for (int rad = 1; rad <= radius; rad++) {
            // Start at west and move clockwise
            int ringQ = q - rad;
            int ringR = r + rad;
            
            // Iterate through the 6 sides of the ring
            for (int side = 0; side < 6; side++) {
                // Move along each side of the ring
                for (int i = 0; i < rad; i++) {
                    results[index][0] = ringQ;
                    results[index][1] = ringR;
                    index++;
                    
                    // Move to next position along the side
                    int[] nextPos = getNeighborCoordinates(ringQ, ringR, side);
                    ringQ = nextPos[0];
                    ringR = nextPos[1];
                }
            }
        }
        
        return results;
    }
}