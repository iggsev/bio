package com.biobox;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;

/**
 * Classe para gerenciar a UI do editor de mapa
 */
public class UIManager {
    // Botões da UI
    private Rectangle grassButton, wallButton, waterButton, sandButton, dirtButton, stoneButton;
    private Rectangle entityButton, playButton, resetButton, lodToggle;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private String hoverText = "";
    
    // Estado do editor
    private boolean editorMode = true;
    private boolean placingEntities = false;
    private TileType selectedTileType = TileType.GRASS;
    private boolean useLOD = true;
    private float fps = 0;
    
    public UIManager(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font) {
        this.shapeRenderer = shapeRenderer;
        this.batch = batch;
        this.font = font;
        
        // Inicializar posições dos botões
        setupUI();
    }
    
    public void setupUI() {
        int buttonHeight = 30;
        int buttonWidth = 80;
        int margin = 10;
        int startY = Gdx.graphics.getHeight() - buttonHeight - margin;
        
        // Botões de seleção de tile
        grassButton = new Rectangle(margin, startY, buttonWidth, buttonHeight);
        wallButton = new Rectangle(margin, startY - buttonHeight - 5, buttonWidth, buttonHeight);
        waterButton = new Rectangle(margin, startY - 2 * (buttonHeight + 5), buttonWidth, buttonHeight);
        sandButton = new Rectangle(margin, startY - 3 * (buttonHeight + 5), buttonWidth, buttonHeight);
        dirtButton = new Rectangle(margin, startY - 4 * (buttonHeight + 5), buttonWidth, buttonHeight);
        stoneButton = new Rectangle(margin, startY - 5 * (buttonHeight + 5), buttonWidth, buttonHeight);
        
        // Botões de ação
        entityButton = new Rectangle(margin, startY - 7 * (buttonHeight + 5), buttonWidth, buttonHeight);
        playButton = new Rectangle(margin, startY - 8 * (buttonHeight + 5), buttonWidth, buttonHeight);
        resetButton = new Rectangle(margin, startY - 9 * (buttonHeight + 5), buttonWidth, buttonHeight);
        
        // Toggle de qualidade gráfica
        lodToggle = new Rectangle(Gdx.graphics.getWidth() - buttonWidth - margin, startY, buttonWidth, buttonHeight);
    }
    
    public void render(OrthographicCamera uiCamera, int entityCount) {
        // Configurar UI rendering
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeType.Filled);
        
        // Botões de seleção de tile
        shapeRenderer.setColor(selectedTileType == TileType.GRASS && !placingEntities ? Color.LIME : Color.FOREST);
        shapeRenderer.rect(grassButton.x, grassButton.y, grassButton.width, grassButton.height);
        
        shapeRenderer.setColor(selectedTileType == TileType.WALL && !placingEntities ? Color.LIGHT_GRAY : Color.DARK_GRAY);
        shapeRenderer.rect(wallButton.x, wallButton.y, wallButton.width, wallButton.height);
        
        shapeRenderer.setColor(selectedTileType == TileType.WATER && !placingEntities ? Color.SKY : Color.NAVY);
        shapeRenderer.rect(waterButton.x, waterButton.y, waterButton.width, waterButton.height);
        
        shapeRenderer.setColor(selectedTileType == TileType.SAND && !placingEntities ? new Color(1, 0.9f, 0.5f, 1) : new Color(0.8f, 0.7f, 0.4f, 1));
        shapeRenderer.rect(sandButton.x, sandButton.y, sandButton.width, sandButton.height);
        
        shapeRenderer.setColor(selectedTileType == TileType.DIRT && !placingEntities ? new Color(0.7f, 0.5f, 0.3f, 1) : new Color(0.5f, 0.3f, 0.1f, 1));
        shapeRenderer.rect(dirtButton.x, dirtButton.y, dirtButton.width, dirtButton.height);
        
        shapeRenderer.setColor(selectedTileType == TileType.STONE && !placingEntities ? Color.LIGHT_GRAY : Color.GRAY);
        shapeRenderer.rect(stoneButton.x, stoneButton.y, stoneButton.width, stoneButton.height);
        
        // Botões de ação
        shapeRenderer.setColor(placingEntities ? Color.RED : Color.MAROON);
        shapeRenderer.rect(entityButton.x, entityButton.y, entityButton.width, entityButton.height);
        
        shapeRenderer.setColor(editorMode ? Color.GOLD : Color.ORANGE);
        shapeRenderer.rect(playButton.x, playButton.y, playButton.width, playButton.height);
        
        shapeRenderer.setColor(Color.CORAL);
        shapeRenderer.rect(resetButton.x, resetButton.y, resetButton.width, resetButton.height);
        
        // LOD toggle
        shapeRenderer.setColor(useLOD ? Color.GREEN : Color.RED);
        shapeRenderer.rect(lodToggle.x, lodToggle.y, lodToggle.width, lodToggle.height);
        
        shapeRenderer.end();
        
        // Desenhar contornos dos botões
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        
        shapeRenderer.rect(grassButton.x, grassButton.y, grassButton.width, grassButton.height);
        shapeRenderer.rect(wallButton.x, wallButton.y, wallButton.width, wallButton.height);
        shapeRenderer.rect(waterButton.x, waterButton.y, waterButton.width, waterButton.height);
        shapeRenderer.rect(sandButton.x, sandButton.y, sandButton.width, sandButton.height);
        shapeRenderer.rect(dirtButton.x, dirtButton.y, dirtButton.width, dirtButton.height);
        shapeRenderer.rect(stoneButton.x, stoneButton.y, stoneButton.width, stoneButton.height);
        shapeRenderer.rect(entityButton.x, entityButton.y, entityButton.width, entityButton.height);
        shapeRenderer.rect(playButton.x, playButton.y, playButton.width, playButton.height);
        shapeRenderer.rect(resetButton.x, resetButton.y, resetButton.width, resetButton.height);
        shapeRenderer.rect(lodToggle.x, lodToggle.y, lodToggle.width, lodToggle.height);
        
        shapeRenderer.end();
        
        // Desenhar texto dos botões com SpriteBatch para melhor performance
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        font.draw(batch, "Grass", grassButton.x + 20, grassButton.y + 20);
        font.draw(batch, "Wall", wallButton.x + 25, wallButton.y + 20);
        font.draw(batch, "Water", waterButton.x + 20, waterButton.y + 20);
        font.draw(batch, "Sand", sandButton.x + 25, sandButton.y + 20);
        font.draw(batch, "Dirt", dirtButton.x + 25, dirtButton.y + 20);
        font.draw(batch, "Stone", stoneButton.x + 20, stoneButton.y + 20);
        font.draw(batch, "Entity", entityButton.x + 20, entityButton.y + 20);
        font.draw(batch, editorMode ? "Play" : "Edit", playButton.x + 25, playButton.y + 20);
        font.draw(batch, "Reset", resetButton.x + 20, resetButton.y + 20);
        font.draw(batch, "LOD", lodToggle.x + 25, lodToggle.y + 20);
        
        // Desenhar informações do modo atual
        String modeText = editorMode ? "EDITOR MODE" : "GAME MODE";
        font.draw(batch, modeText, Gdx.graphics.getWidth() - 120, Gdx.graphics.getHeight() - 20);
        
        // Desenhar contagem de entidades e FPS
        font.draw(batch, "Entities: " + entityCount, Gdx.graphics.getWidth() - 120, Gdx.graphics.getHeight() - 40);
        font.draw(batch, String.format("FPS: %.1f", fps), Gdx.graphics.getWidth() - 120, Gdx.graphics.getHeight() - 60);
        
        // Desenhar texto de ajuda na parte inferior
        font.draw(batch, "WASD: Move Camera | QE: Zoom | Space: Toggle Mode", 
                Gdx.graphics.getWidth()/2 - 200, 30);
                
        // Desenhar texto de hover
        if (!hoverText.isEmpty()) {
            font.draw(batch, hoverText, Gdx.input.getX() + 15, Gdx.graphics.getHeight() - Gdx.input.getY() - 15);
        }
        
        batch.end();
    }
    
    public void handleUIHover(int mouseX, int mouseY) {
        float x = mouseX;
        float y = Gdx.graphics.getHeight() - mouseY; // Inverter Y para coordenadas de UI
        
        hoverText = "";
        
        // Verificar hover dos botões
        if (grassButton.contains(x, y)) hoverText = "Grass Tile (Walkable)";
        else if (wallButton.contains(x, y)) hoverText = "Wall Tile (Blocks movement)";
        else if (waterButton.contains(x, y)) hoverText = "Water Tile (Blocks movement)";
        else if (sandButton.contains(x, y)) hoverText = "Sand Tile (Walkable)";
        else if (dirtButton.contains(x, y)) hoverText = "Dirt Tile (Walkable)";
        else if (stoneButton.contains(x, y)) hoverText = "Stone Tile (Walkable)";
        else if (entityButton.contains(x, y)) hoverText = "Place Entities";
        else if (playButton.contains(x, y)) hoverText = editorMode ? "Switch to Game Mode" : "Switch to Editor Mode";
        else if (resetButton.contains(x, y)) hoverText = "Reset Map";
        else if (lodToggle.contains(x, y)) hoverText = "Toggle LOD Rendering (prevents flickering when zoomed out)";
    }
    
    public boolean checkUIClick(int mouseX, int mouseY) {
        float x = mouseX;
        float y = Gdx.graphics.getHeight() - mouseY; // Inverter Y para coordenadas de UI
        
        if (grassButton.contains(x, y)) {
            selectedTileType = TileType.GRASS;
            placingEntities = false;
            return true;
        } else if (wallButton.contains(x, y)) {
            selectedTileType = TileType.WALL;
            placingEntities = false;
            return true;
        } else if (waterButton.contains(x, y)) {
            selectedTileType = TileType.WATER;
            placingEntities = false;
            return true;
        } else if (sandButton.contains(x, y)) {
            selectedTileType = TileType.SAND;
            placingEntities = false;
            return true;
        } else if (dirtButton.contains(x, y)) {
            selectedTileType = TileType.DIRT;
            placingEntities = false;
            return true;
        } else if (stoneButton.contains(x, y)) {
            selectedTileType = TileType.STONE;
            placingEntities = false;
            return true;
        } else if (entityButton.contains(x, y)) {
            placingEntities = true;
            return true;
        } else if (playButton.contains(x, y)) {
            editorMode = !editorMode;
            return true;
        } else if (resetButton.contains(x, y)) {
            return true; // Reset será manipulado externamente
        } else if (lodToggle.contains(x, y)) {
            useLOD = !useLOD;
            return true;
        }
        
        return false;
    }
    
    /**
     * Verifica se o botão de reset foi clicado
     */
    public boolean isResetButtonClicked(int screenX, int screenY) {
        float y = Gdx.graphics.getHeight() - screenY; // Inverter Y para coordenadas de UI
        return resetButton.contains(screenX, y);
    }
    
    public void updateFPS(float fps) {
        this.fps = fps;
    }
    
    public void setEditorMode(boolean editorMode) {
        this.editorMode = editorMode;
    }
    
    public boolean isEditorMode() {
        return editorMode;
    }
    
    public boolean isPlacingEntities() {
        return placingEntities;
    }
    
    public TileType getSelectedTileType() {
        return selectedTileType;
    }
    
    public boolean isUsingLOD() {
        return useLOD;
    }
}