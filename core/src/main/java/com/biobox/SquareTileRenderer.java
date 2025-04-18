package com.biobox;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;

/**
 * Renderer that creates textured square tiles for an RPG world map
 */
public class SquareTileRenderer {
    // Constants
    public static final int PIXEL_SIZE = 3;
    public static final int TILE_SIZE = 16; // Size of each tile in pixels
    public static final float BORDER_WIDTH = 1.5f; // Width of tile borders
    
    // Components
    private final SquareGrid grid;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    
    // Texture components
    private Pixmap pixmap;
    private Texture texture;
    private boolean textureNeedsUpdate = true;
    
    // Rendering options
    private boolean showGrid = true;
    private Color borderColor = new Color(0.2f, 0.2f, 0.2f, 0.8f);
    
    // Cached colors for each biome
    private Color[][] biomePixelColors;
    
    public SquareTileRenderer(SquareGrid grid, ShapeRenderer shapeRenderer, SpriteBatch batch) {
        this.grid = grid;
        this.shapeRenderer = shapeRenderer;
        this.batch = batch;
        
        // Calculate world dimensions in pixels
        int worldWidth = grid.getWidth() * TILE_SIZE;
        int worldHeight = grid.getHeight() * TILE_SIZE;
        
        // Create pixmap for the texture
        pixmap = new Pixmap(worldWidth, worldHeight, Pixmap.Format.RGBA8888);
        texture = new Texture(pixmap);
        
        // Initialize color variations
        initBiomeColors();
    }
    
    /**
     * Initialize color variations for each biome
     */
    private void initBiomeColors() {
        BiomeType[] biomeTypes = BiomeType.values();
        biomePixelColors = new Color[biomeTypes.length][64]; // Store 64 variations per biome
        
        for (BiomeType biome : biomeTypes) {
            Color baseColor = biome.getBaseColor();
            
            // Generate variations
            for (int i = 0; i < biomePixelColors[biome.ordinal()].length; i++) {
                biomePixelColors[biome.ordinal()][i] = new Color(
                    MathUtils.clamp(baseColor.r + MathUtils.random(-0.07f, 0.07f), 0, 1),
                    MathUtils.clamp(baseColor.g + MathUtils.random(-0.07f, 0.07f), 0, 1),
                    MathUtils.clamp(baseColor.b + MathUtils.random(-0.07f, 0.07f), 0, 1),
                    1f
                );
            }
        }
    }
    
    /**
     * Render the world
     */
    public void render(OrthographicCamera camera) {
        // Update texture if needed
        if (textureNeedsUpdate) {
            updateTexture();
            textureNeedsUpdate = false;
        }
        
        // Draw background texture
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(texture, 0, 0);
        batch.end();
        
        // Draw grid lines if enabled
        if (showGrid) {
            renderGridLines(camera);
        }
    }
    
    /**
     * Update the texture with pixel variations
     */
    private void updateTexture() {
        // Clear the pixmap
        pixmap.setColor(0, 0, 0, 1);
        pixmap.fill();
        
        // Draw each tile
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                BiomeType biome = grid.getTile(x, y);
                if (biome != null) {
                    renderSquareTile(x, y, biome);
                }
            }
        }
        
        // Update the texture
        if (texture != null) texture.dispose();
        texture = new Texture(pixmap);
    }
    
    /**
     * Render a single square tile with pixel texture
     */
    private void renderSquareTile(int x, int y, BiomeType biome) {
        // Get the biome's colors
        Color[] colors = biomePixelColors[biome.ordinal()];
        
        // Calculate tile position in pixels
        int tileX = x * TILE_SIZE;
        int tileY = y * TILE_SIZE;
        
        // Size of the internal area (excluding border)
        int internalSize = showGrid ? TILE_SIZE - 2 : TILE_SIZE;
        
        // Draw pixels inside the tile
        for (int px = 0; px < internalSize; px++) {
            for (int py = 0; py < internalSize; py++) {
                // Calculate pixel coordinates
                int pixelX = tileX + (showGrid ? 1 : 0) + px;
                int pixelY = tileY + (showGrid ? 1 : 0) + py;
                
                // Make sure we're in pixmap bounds
                if (pixelX >= 0 && pixelX < pixmap.getWidth() && 
                    pixelY >= 0 && pixelY < pixmap.getHeight()) {
                    
                    // Use deterministic random color based on position
                    int colorIndex = Math.abs((pixelX * 31 + pixelY * 17) % colors.length);
                    Color pixelColor = colors[colorIndex];
                    
                    // Draw the pixel
                    pixmap.drawPixel(pixelX, pixelY, Color.rgba8888(pixelColor));
                }
            }
        }
        
        // Draw border if enabled
        if (showGrid) {
            drawTileBorder(tileX, tileY);
        }
    }
    
    /**
     * Draw a border around a tile
     */
    private void drawTileBorder(int tileX, int tileY) {
        int borderColor = Color.rgba8888(this.borderColor);
        
        // Draw top and bottom borders
        for (int x = 0; x < TILE_SIZE; x++) {
            // Top border
            pixmap.drawPixel(tileX + x, tileY, borderColor);
            // Bottom border
            pixmap.drawPixel(tileX + x, tileY + TILE_SIZE - 1, borderColor);
        }
        
        // Draw left and right borders
        for (int y = 0; y < TILE_SIZE; y++) {
            // Left border
            pixmap.drawPixel(tileX, tileY + y, borderColor);
            // Right border
            pixmap.drawPixel(tileX + TILE_SIZE - 1, tileY + y, borderColor);
        }
    }
    
    /**
     * Render grid lines with shape renderer for crisp edges
     */
    private void renderGridLines(OrthographicCamera camera) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(borderColor);
        
        // Adjust line width for grid lines
        Gdx.gl.glLineWidth(BORDER_WIDTH);
        
        // Draw vertical lines
        for (int x = 0; x <= grid.getWidth(); x++) {
            int pixelX = x * TILE_SIZE;
            shapeRenderer.line(pixelX, 0, pixelX, grid.getHeight() * TILE_SIZE);
        }
        
        // Draw horizontal lines
        for (int y = 0; y <= grid.getHeight(); y++) {
            int pixelY = y * TILE_SIZE;
            shapeRenderer.line(0, pixelY, grid.getWidth() * TILE_SIZE, pixelY);
        }
        
        // Reset line width
        Gdx.gl.glLineWidth(1.0f);
        
        shapeRenderer.end();
    }
    
    /**
     * Toggle grid display
     */
    public void toggleGrid() {
        showGrid = !showGrid;
        markDirty(); // Update the texture when grid is toggled
    }
    
    /**
     * Check if grid is showing
     */
    public boolean isShowingGrid() {
        return showGrid;
    }
    
    /**
     * Mark the texture as needing update
     */
    public void markDirty() {
        textureNeedsUpdate = true;
    }
    
    /**
     * Dispose resources
     */
    public void dispose() {
        if (pixmap != null) pixmap.dispose();
        if (texture != null) texture.dispose();
    }
}