package com.biobox;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * Editor for placing terrain on the map
 */
public class WorldEditor {
    // Components
    private SquareGrid grid;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private SquareTileRenderer tileRenderer;
    private GlyphLayout glyphLayout; // For measuring text
    
    // State
    private BiomeType selectedBiome = BiomeType.GRASS;
    private boolean active = false;
    private int hoveredTileX = -1;
    private int hoveredTileY = -1;
    
    // UI elements
    private Array<Rectangle> biomeButtons = new Array<>();
    private Array<String> biomeLabels = new Array<>();
    
    public WorldEditor(SquareGrid grid, ShapeRenderer shapeRenderer, SpriteBatch batch, 
                      BitmapFont font, SquareTileRenderer tileRenderer) {
        this.grid = grid;
        this.shapeRenderer = shapeRenderer;
        this.batch = batch;
        this.font = font;
        this.tileRenderer = tileRenderer;
        this.glyphLayout = new GlyphLayout(); // Initialize GlyphLayout
        
        setupUI();
    }
    
    /**
     * Setup the UI elements
     */
    private void setupUI() {
        BiomeType[] biomes = BiomeType.values();
        int buttonWidth = 120;
        int buttonHeight = 25;
        int margin = 10;
        int spacing = 5;
        int buttonsPerColumn = 9;
        
        // Create buttons for each biome type
        for (int i = 0; i < biomes.length; i++) {
            int column = i / buttonsPerColumn;
            int row = i % buttonsPerColumn;
            
            Rectangle button = new Rectangle(
                margin + column * (buttonWidth + spacing), 
                Gdx.graphics.getHeight() - margin - (row + 1) * (buttonHeight + spacing),
                buttonWidth,
                buttonHeight
            );
            
            biomeButtons.add(button);
            biomeLabels.add(biomes[i].name());
        }
    }
    
    /**
     * Render the editor UI
     */
    public void render(OrthographicCamera camera) {
        if (!active) return;
        
        // Render biome buttons
        renderBiomeButtons();
        
        // Render tile hover indicator
        renderTileHover(camera);
    }
    
    /**
     * Render the biome selection buttons
     */
    private void renderBiomeButtons() {
        // Draw buttons
        shapeRenderer.begin(ShapeType.Filled);
        
        BiomeType[] biomes = BiomeType.values();
        for (int i = 0; i < biomeButtons.size; i++) {
            // Set color based on biome
            Color color = biomes[i].getBaseColor();
            
            // Highlight selected biome
            if (biomes[i] == selectedBiome) {
                color = new Color(
                    Math.min(1f, color.r + 0.2f),
                    Math.min(1f, color.g + 0.2f),
                    Math.min(1f, color.b + 0.2f),
                    1f
                );
            }
            
            shapeRenderer.setColor(color);
            Rectangle button = biomeButtons.get(i);
            shapeRenderer.rect(button.x, button.y, button.width, button.height);
        }
        
        shapeRenderer.end();
        
        // Draw button outlines
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        
        for (Rectangle button : biomeButtons) {
            shapeRenderer.rect(button.x, button.y, button.width, button.height);
        }
        
        shapeRenderer.end();
        
        // Draw button labels
        batch.begin();
        for (int i = 0; i < biomeButtons.size; i++) {
            Rectangle button = biomeButtons.get(i);
            String label = biomeLabels.get(i);
            
            // Get text width using GlyphLayout
            glyphLayout.setText(font, label);
            float textWidth = glyphLayout.width;
            
            // Position the text in the center of the button
            float textX = button.x + (button.width - textWidth) / 2;
            float textY = button.y + button.height - 5;
            
            font.draw(batch, label, textX, textY);
        }
        
        // Draw editor instructions
        String instructions = "Click to place terrain | TAB to toggle editor";
        font.draw(batch, instructions, 10, 30);
        
        batch.end();
    }
    
    /**
     * Render a hover indicator over the tile under the mouse
     */
    private void renderTileHover(OrthographicCamera camera) {
        updateHoveredTile(camera);
        
        if (hoveredTileX >= 0 && hoveredTileY >= 0) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeType.Line);
            shapeRenderer.setColor(Color.WHITE);
            
            float x = hoveredTileX * SquareTileRenderer.TILE_SIZE;
            float y = hoveredTileY * SquareTileRenderer.TILE_SIZE;
            float size = SquareTileRenderer.TILE_SIZE;
            
            shapeRenderer.rect(x, y, size, size);
            
            shapeRenderer.end();
        }
    }
    
    /**
     * Update the currently hovered tile based on mouse position
     */
    private void updateHoveredTile(OrthographicCamera camera) {
        // Convert mouse coordinates to world coordinates
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);
        
        // Calculate tile coordinates
        int tileX = (int)(mousePos.x / SquareTileRenderer.TILE_SIZE);
        int tileY = (int)(mousePos.y / SquareTileRenderer.TILE_SIZE);
        
        // Update only if in bounds
        if (grid.isInBounds(tileX, tileY)) {
            hoveredTileX = tileX;
            hoveredTileY = tileY;
        } else {
            hoveredTileX = -1;
            hoveredTileY = -1;
        }
    }
    
    /**
     * Handle mouse input for the editor
     */
    public boolean handleInput(OrthographicCamera camera) {
        if (!active) return false;
        
        // Handle clicking on UI elements
        if (Gdx.input.justTouched()) {
            int screenX = Gdx.input.getX();
            int screenY = Gdx.input.getY();
            
            // Check biome buttons first
            BiomeType[] biomes = BiomeType.values();
            for (int i = 0; i < biomeButtons.size; i++) {
                Rectangle button = biomeButtons.get(i);
                if (button.contains(screenX, Gdx.graphics.getHeight() - screenY)) {
                    selectedBiome = biomes[i];
                    return true;
                }
            }
            
            // If not on buttons, place terrain at the hovered tile
            if (hoveredTileX >= 0 && hoveredTileY >= 0) {
                grid.setTile(hoveredTileX, hoveredTileY, selectedBiome);
                tileRenderer.markDirty();
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Toggle the editor active state
     */
    public void toggle() {
        active = !active;
    }
    
    /**
     * Check if the editor is active
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Get the currently selected biome
     */
    public BiomeType getSelectedBiome() {
        return selectedBiome;
    }
}