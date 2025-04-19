package com.biobox;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Main UI manager for the RPG World Generator.
 * Handles all UI components, user interactions, and input processing.
 */
public class WorldEditorUI {
    // Core components
    private SquareGrid grid;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private SquareTileRenderer tileRenderer;
    private BiomeGenerator worldGenerator;
    
    // Camera and viewport for the map display
    private OrthographicCamera mapCamera;
    private Viewport mapViewport;
    
    // Camera and viewport for the UI
    private OrthographicCamera uiCamera;
    private Viewport uiViewport;
    
    private GlyphLayout glyphLayout;
    
    // Input handling
    private InputMultiplexer inputMultiplexer;
    private MapInputProcessor mapInputProcessor;
    private UIInputProcessor uiInputProcessor;
    
    // UI State
    private enum EditorMode { MAIN_MENU, TERRAIN_EDITOR, WORLD_GENERATOR }
    private EditorMode currentMode = EditorMode.MAIN_MENU;
    
    // Layout constants
    private static final int SCREEN_WIDTH = 1280;
    private static final int SCREEN_HEIGHT = 720;
    private static final int BOTTOM_PANEL_HEIGHT = 150; // Height of the bottom panel
    
    // Map area (where the terrain is displayed)
    private Rectangle mapArea;
    
    // UI Components
    private Rectangle bottomPanel; // Bottom panel
    private Array<Rectangle> menuButtons = new Array<>();
    private Array<String> menuButtonLabels = new Array<>();
    
    // Editor components
    private Array<Rectangle> biomeButtons = new Array<>();
    private Array<String> biomeLabels = new Array<>();
    private BiomeType selectedBiome = BiomeType.GRASS;
    
    // World generator components
    private Array<Rectangle> worldTypeButtons = new Array<>();
    private Array<String> worldTypeLabels = new Array<>();
    private BiomeGenerator.WorldType selectedWorldType = BiomeGenerator.WorldType.CLASSIC;
    private Rectangle generateButton;
    
    // Editor tab components
    private Array<Rectangle> tabButtons = new Array<>();
    private Array<String> tabLabels = new Array<>();
    private int selectedTabIndex = 0;
    
    // Hover information
    private int hoveredTileX = -1;
    private int hoveredTileY = -1;
    
    // UI Constants
    private static final Color PANEL_COLOR = new Color(0.2f, 0.2f, 0.25f, 0.9f);
    private static final Color BUTTON_COLOR = new Color(0.3f, 0.3f, 0.4f, 1f);
    private static final Color BUTTON_HOVER_COLOR = new Color(0.4f, 0.4f, 0.5f, 1f);
    private static final Color BUTTON_ACTIVE_COLOR = new Color(0.3f, 0.5f, 0.8f, 1f);
    private static final Color TEXT_COLOR = new Color(0.9f, 0.9f, 0.9f, 1f);
    private static final Color TITLE_COLOR = new Color(1f, 1f, 1f, 1f);
    
    private static final int TAB_HEIGHT = 30;
    private static final int BUTTON_HEIGHT = 40;
    // Reduzido o tamanho dos botões e o espaçamento para melhor ajuste
    private static final int BIOME_BUTTON_SIZE = 40;
    private static final int BUTTON_SPACING = 8;
    
    /**
     * Constructor initializes the UI components and input processors
     */
    public WorldEditorUI(SquareGrid grid, ShapeRenderer shapeRenderer, SpriteBatch batch, 
                         BitmapFont font, SquareTileRenderer tileRenderer, 
                         BiomeGenerator worldGenerator) {
        this.grid = grid;
        this.shapeRenderer = shapeRenderer;
        this.batch = batch;
        this.font = font;
        this.tileRenderer = tileRenderer;
        this.worldGenerator = worldGenerator;
        this.glyphLayout = new GlyphLayout();
        
        // Setup the map camera and viewport - ALTERADO para usar o tamanho do grid
        this.mapCamera = new OrthographicCamera();
        this.mapViewport = new FitViewport(
            grid.getWidth() * SquareTileRenderer.TILE_SIZE,
            grid.getHeight() * SquareTileRenderer.TILE_SIZE,
            mapCamera
        );
        
        // Set up the UI camera and viewport
        this.uiCamera = new OrthographicCamera();
        this.uiViewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT, uiCamera);
        
        // Position the cameras
        uiCamera.position.set(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 0);
        
        // Define map and UI areas
        mapArea = new Rectangle(0, BOTTOM_PANEL_HEIGHT, SCREEN_WIDTH, SCREEN_HEIGHT - BOTTOM_PANEL_HEIGHT);
        bottomPanel = new Rectangle(0, 0, SCREEN_WIDTH, BOTTOM_PANEL_HEIGHT);
        
        // Center the map camera on the grid (in world space)
        centerMapCamera();
        
        // Update cameras 
        mapCamera.update();
        uiCamera.update();
        
        // Initialize input processing
        mapInputProcessor = new MapInputProcessor();
        uiInputProcessor = new UIInputProcessor();
        inputMultiplexer = new InputMultiplexer(uiInputProcessor, mapInputProcessor);
        
        // Initialize UI elements
        setupMainMenu();
        setupEditorUI();
    }
    
    /**
     * Center the map camera on the grid - REESCRITO para melhor zoom
     */
    private void centerMapCamera() {
        int gridWidthInPixels = grid.getWidth() * SquareTileRenderer.TILE_SIZE;
        int gridHeightInPixels = grid.getHeight() * SquareTileRenderer.TILE_SIZE;
        
        // Calcular o zoom ideal para preencher a área disponível
        float widthRatio = SCREEN_WIDTH / (float)gridWidthInPixels;
        float heightRatio = (SCREEN_HEIGHT - BOTTOM_PANEL_HEIGHT) / (float)gridHeightInPixels;
        
        // Usar o menor valor para garantir que todo o mapa seja visível
        float zoom = Math.min(widthRatio, heightRatio) * 0.95f; // 5% de margem
        
        mapCamera.position.set(gridWidthInPixels / 2f, gridHeightInPixels / 2f, 0);
        // Ajustar o zoom (usando o inverso porque valores menores = mais zoom)
        mapCamera.zoom = 1.0f / zoom;
        mapCamera.update();
        
        // Debug info - print grid size in pixels
        System.out.println("Grid size in pixels: " + gridWidthInPixels + "x" + gridHeightInPixels);
        System.out.println("Map camera zoom: " + mapCamera.zoom);
    }
    
    /**
     * Set up the main menu UI
     */
    private void setupMainMenu() {
        menuButtons.clear();
        menuButtonLabels.clear();
        
        int centerX = SCREEN_WIDTH / 2;
        int centerY = SCREEN_HEIGHT / 2;
        int buttonWidth = 200;
        
        // Main menu buttons
        String[] labels = {"New World", "Edit Terrain", "Exit"};
        
        for (int i = 0; i < labels.length; i++) {
            Rectangle button = new Rectangle(
                centerX - buttonWidth / 2,
                centerY + 50 - i * (BUTTON_HEIGHT + BUTTON_SPACING),
                buttonWidth,
                BUTTON_HEIGHT
            );
            
            menuButtons.add(button);
            menuButtonLabels.add(labels[i]);
        }
    }
    
    /**
     * Set up the editor UI components
     */
    private void setupEditorUI() {
        // Set up tabs
        setupTabs();
        
        // Set up biome buttons for terrain editing
        setupBiomeButtons();
        
        // Set up world type buttons for world generation
        setupWorldTypeButtons();
    }
    
    /**
     * Set up the tab buttons
     */
    private void setupTabs() {
        tabButtons.clear();
        tabLabels.clear();
        
        String[] tabs = {"Edit Terrain", "Generate World"};
        int tabWidth = 120;
        
        for (int i = 0; i < tabs.length; i++) {
            Rectangle tab = new Rectangle(
                10 + i * (tabWidth + BUTTON_SPACING),
                BOTTOM_PANEL_HEIGHT - TAB_HEIGHT - 5,
                tabWidth,
                TAB_HEIGHT
            );
            
            tabButtons.add(tab);
            tabLabels.add(tabs[i]);
        }
    }
    
    /**
     * Set up the biome selection buttons - REESCRITO para mais botões por linha
     */
    private void setupBiomeButtons() {
        biomeButtons.clear();
        biomeLabels.clear();
        
        BiomeType[] biomes = BiomeType.values();
        // Aumentar o número de botões por linha
        int buttonsPerRow = 8;
        int startX = 10;
        int startY = BOTTOM_PANEL_HEIGHT - TAB_HEIGHT - BUTTON_SPACING - BIOME_BUTTON_SIZE - 10;
        
        for (int i = 0; i < biomes.length; i++) {
            int row = i / buttonsPerRow;
            int col = i % buttonsPerRow;
            
            // Calcular a posição Y com espaçamento ajustado para garantir que caiba
            float y = startY - row * (BIOME_BUTTON_SIZE + BUTTON_SPACING);
            
            // Verificar se o botão ficaria abaixo da área visível
            if (y < 5) {
                // Ajustar para a última linha visível
                y = 5;
            }
            
            Rectangle button = new Rectangle(
                startX + col * (BIOME_BUTTON_SIZE + BUTTON_SPACING),
                y,
                BIOME_BUTTON_SIZE,
                BIOME_BUTTON_SIZE
            );
            
            biomeButtons.add(button);
            biomeLabels.add(biomes[i].name());
        }
    }
    
    /**
     * Set up the world type selection buttons
     */
    private void setupWorldTypeButtons() {
        worldTypeButtons.clear();
        worldTypeLabels.clear();
        
        BiomeGenerator.WorldType[] worldTypes = BiomeGenerator.WorldType.values();
        int buttonWidth = 180;
        int buttonHeight = 35;
        int buttonsPerRow = 3;
        int startX = 10;
        int startY = BOTTOM_PANEL_HEIGHT - TAB_HEIGHT - BUTTON_SPACING - buttonHeight - 10;
        
        for (int i = 0; i < worldTypes.length; i++) {
            int row = i / buttonsPerRow;
            int col = i % buttonsPerRow;
            
            Rectangle button = new Rectangle(
                startX + col * (buttonWidth + BUTTON_SPACING),
                startY - row * (buttonHeight + BUTTON_SPACING),
                buttonWidth,
                buttonHeight
            );
            
            worldTypeButtons.add(button);
            worldTypeLabels.add(worldTypes[i].name());
        }
        
        // Generate button
        generateButton = new Rectangle(
            SCREEN_WIDTH - 200,
            BOTTOM_PANEL_HEIGHT / 2 - buttonHeight / 2,
            180,
            buttonHeight
        );
    }
    
    /**
     * Handle user input
     */
    public void handleInput() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        
        // Handle camera movement if not in main menu
        if (currentMode != EditorMode.MAIN_MENU) {
            // Camera movement
            float speed = 400f * deltaTime * mapCamera.zoom;
            
            if (Gdx.input.isKeyPressed(Input.Keys.W)) mapCamera.position.y += speed;
            if (Gdx.input.isKeyPressed(Input.Keys.S)) mapCamera.position.y -= speed;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) mapCamera.position.x -= speed;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) mapCamera.position.x += speed;
            
            // Zoom control
            if (Gdx.input.isKeyPressed(Input.Keys.Q)) mapCamera.zoom += deltaTime * 2f;
            if (Gdx.input.isKeyPressed(Input.Keys.E)) mapCamera.zoom -= deltaTime * 2f;
            mapCamera.zoom = MathUtils.clamp(mapCamera.zoom, 0.2f, 10f);
            
            // Toggle grid with G
            if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
                tileRenderer.toggleGrid();
            }
            
            // ESC key to return to main menu
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                currentMode = EditorMode.MAIN_MENU;
            }
            
            // Update cameras
            mapCamera.update();
            uiCamera.update();
        }
        
        // Update hovered tile
        if (currentMode == EditorMode.TERRAIN_EDITOR) {
            updateHoveredTile();
        }
    }
    
    /**
     * Update the currently hovered tile based on mouse position
     */
    private void updateHoveredTile() {
        // Only update hover if mouse is in map area
        if (!isMouseInMapArea()) {
            hoveredTileX = -1;
            hoveredTileY = -1;
            return;
        }
        
        // Get mouse coordinates in screen space
        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();
        
        // Convert screen coordinates to world coordinates
        Vector3 worldCoords = new Vector3(screenX, screenY, 0);
        mapViewport.unproject(worldCoords);        
        // Calculate tile coordinates
        int tileX = (int)(worldCoords.x / SquareTileRenderer.TILE_SIZE);
        
        // Invert Y coordinate to fix the inversion issue
        int tileY = grid.getHeight() - 1 - (int)(worldCoords.y / SquareTileRenderer.TILE_SIZE);
        
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
     * Check if the mouse is in the map area
     */
    private boolean isMouseInMapArea() {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY(); // Invert Y for UI coordinates
        
        return mapArea.contains(mouseX, mouseY);
    }
    
    /**
     * Render the UI and map
     */
    public void render() {
        // Clear viewport configurations
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        if (currentMode == EditorMode.MAIN_MENU) {
            // For main menu, just render the UI
            renderMainMenu();
        } else {
            // Render map area first
            renderMap();
            
            // Then render UI elements on top
            renderBottomPanelUI();
            
            // Render hover highlight for terrain editor
            if (currentMode == EditorMode.TERRAIN_EDITOR && hoveredTileX >= 0 && hoveredTileY >= 0) {
                renderTileHover();
            }
        }
    }
    
    /**
     * Render the map - REESCRITO para configurar o viewport corretamente
     */
    private void renderMap() {
        // Configurar a posição e tamanho do viewport
        mapViewport.setScreenPosition(0, BOTTOM_PANEL_HEIGHT);
        mapViewport.setScreenSize((int)mapArea.width, (int)mapArea.height);
        mapViewport.apply();
        
        // Limpar a área com uma cor de fundo para visualizar melhor
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Render the world with the map camera
        tileRenderer.render(mapCamera);
    }
    
    /**
     * Render the main menu
     */
    private void renderMainMenu() {
        uiViewport.apply();
        
        // Draw background panel
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(PANEL_COLOR);
        
        // Draw panel in center of screen
        int centerX = SCREEN_WIDTH / 2;
        int centerY = SCREEN_HEIGHT / 2;
        int panelWidth = 300;
        int panelHeight = 250;
        
        shapeRenderer.rect(
            centerX - panelWidth / 2,
            centerY - panelHeight / 2,
            panelWidth,
            panelHeight
        );
        
        // Draw buttons
        for (int i = 0; i < menuButtons.size; i++) {
            Rectangle button = menuButtons.get(i);
            
            // Check if mouse is over button
            boolean isHovered = isMouseOver(button);
            shapeRenderer.setColor(isHovered ? BUTTON_HOVER_COLOR : BUTTON_COLOR);
            
            shapeRenderer.rect(button.x, button.y, button.width, button.height);
        }
        shapeRenderer.end();
        
        // Draw button outlines
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        for (Rectangle button : menuButtons) {
            shapeRenderer.rect(button.x, button.y, button.width, button.height);
        }
        shapeRenderer.end();
        
        // Draw text
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        // Draw title
        String title = "RPG World Generator";
        glyphLayout.setText(font, title);
        font.setColor(TITLE_COLOR);
        font.draw(batch, title, 
            centerX - glyphLayout.width / 2,
            centerY + panelHeight / 2 - 20
        );
        
        // Draw button labels
        font.setColor(TEXT_COLOR);
        for (int i = 0; i < menuButtons.size; i++) {
            Rectangle button = menuButtons.get(i);
            String label = menuButtonLabels.get(i);
            
            glyphLayout.setText(font, label);
            font.draw(batch, label,
                button.x + (button.width - glyphLayout.width) / 2,
                button.y + button.height - (button.height - glyphLayout.height) / 2
            );
        }
        
        batch.end();
    }
    
    /**
     * Render the bottom panel UI
     */
    private void renderBottomPanelUI() {
        // Set the correct viewport for the UI
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiViewport.apply();
        
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        
        // Draw bottom panel
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(PANEL_COLOR);
        shapeRenderer.rect(bottomPanel.x, bottomPanel.y, bottomPanel.width, bottomPanel.height);
        
        // Draw tabs
        for (int i = 0; i < tabButtons.size; i++) {
            Rectangle tab = tabButtons.get(i);
            
            if (i == selectedTabIndex) {
                shapeRenderer.setColor(BUTTON_ACTIVE_COLOR);
            } else {
                boolean isHovered = isMouseOver(tab);
                shapeRenderer.setColor(isHovered ? BUTTON_HOVER_COLOR : BUTTON_COLOR);
            }
            
            shapeRenderer.rect(tab.x, tab.y, tab.width, tab.height);
        }
        
        // Draw tab content based on selected tab
        if (selectedTabIndex == 0) {
            renderTerrainEditorTab();
        } else {
            renderWorldGeneratorTab();
        }
        
        shapeRenderer.end();
        
        // Draw tab outlines
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        for (Rectangle tab : tabButtons) {
            shapeRenderer.rect(tab.x, tab.y, tab.width, tab.height);
        }
        shapeRenderer.end();
        
        // Draw text for tabs
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        font.setColor(TEXT_COLOR);
        
        for (int i = 0; i < tabButtons.size; i++) {
            Rectangle tab = tabButtons.get(i);
            String label = tabLabels.get(i);
            
            glyphLayout.setText(font, label);
            font.draw(batch, label,
                tab.x + (tab.width - glyphLayout.width) / 2,
                tab.y + tab.height - (tab.height - glyphLayout.height) / 2
            );
        }
        
        // Draw additional status info
        String title = selectedTabIndex == 0 ? "Terrain Editor" : "World Generator";
        glyphLayout.setText(font, title);
        font.setColor(TITLE_COLOR);
        font.draw(batch, title, 270, BOTTOM_PANEL_HEIGHT - 15);
        
        // Draw selected biome/world type info
        font.setColor(TEXT_COLOR);
        if (selectedTabIndex == 0 && selectedBiome != null) {
            String biomeInfo = "Selected Biome: " + selectedBiome.name();
            font.draw(batch, biomeInfo, 450, BOTTOM_PANEL_HEIGHT - 15);
        } else if (selectedTabIndex == 1 && selectedWorldType != null) {
            String worldTypeInfo = "Selected World Type: " + selectedWorldType.name();
            font.draw(batch, worldTypeInfo, 450, BOTTOM_PANEL_HEIGHT - 15);
        }
        
        // Draw instructions
        String instructions = selectedTabIndex == 0 ? 
            "Click on the map to place terrain. Select a biome below." : 
            "Select a world type below, then click Generate.";
        font.draw(batch, instructions, 270, BOTTOM_PANEL_HEIGHT - 40);
        
        // Draw hover info if applicable
        if (hoveredTileX >= 0 && hoveredTileY >= 0) {
            String positionInfo = String.format("Tile: %d, %d - Type: %s", 
                hoveredTileX, hoveredTileY,
                grid.getTile(hoveredTileX, hoveredTileY).name());
            font.draw(batch, positionInfo, SCREEN_WIDTH - 250, BOTTOM_PANEL_HEIGHT - 15);
        }
        
        // Draw controls help
        String[] controls = {
            "WASD: Move Camera",
            "QE: Zoom",
            "G: Toggle Grid",
            "ESC: Menu"
        };
        
        float controlX = SCREEN_WIDTH - 120;
        float controlY = BOTTOM_PANEL_HEIGHT - 15;
        
        for (String control : controls) {
            font.draw(batch, control, controlX, controlY);
            controlY -= 20;
        }
        
        // Draw biome labels for terrain tab - MODIFICADO para evitar corte
        if (selectedTabIndex == 0) {
            for (int i = 0; i < biomeButtons.size && i < biomeLabels.size; i++) {
                Rectangle button = biomeButtons.get(i);
                String label = biomeLabels.get(i);
                
                // Simplify label if needed
                if (label.length() > 10) {
                    label = label.substring(0, Math.min(label.length(), 10));
                }
                
                // Pular rótulos para botões que estão muito baixos
                if (button.y < 15) continue;
                
                glyphLayout.setText(font, label);
                float labelX = button.x + (button.width - glyphLayout.width) / 2;
                float labelY = button.y - 5;
                
                font.draw(batch, label, labelX, labelY);
            }
        }
        
        // Draw world type labels for generator tab
        if (selectedTabIndex == 1) {
            for (int i = 0; i < worldTypeButtons.size && i < worldTypeLabels.size; i++) {
                Rectangle button = worldTypeButtons.get(i);
                String label = worldTypeLabels.get(i);
                
                glyphLayout.setText(font, label);
                float labelX = button.x + (button.width - glyphLayout.width) / 2;
                float labelY = button.y + button.height / 2 + 5;
                
                font.draw(batch, label, labelX, labelY);
            }
            
            // Draw generate button label
            glyphLayout.setText(font, "Generate Map");
            float genLabelX = generateButton.x + (generateButton.width - glyphLayout.width) / 2;
            float genLabelY = generateButton.y + generateButton.height / 2 + 5;
            font.draw(batch, "Generate Map", genLabelX, genLabelY);
        }
        
        batch.end();
    }
    
    /**
     * Render the terrain editor tab content
     */
    private void renderTerrainEditorTab() {
        // Draw biome buttons
        BiomeType[] biomes = BiomeType.values();
        for (int i = 0; i < biomeButtons.size && i < biomes.length; i++) {
            // Set color based on biome
            Color color = biomes[i].getBaseColor();
            
            // Highlight selected biome
            if (biomes[i] == selectedBiome) {
                shapeRenderer.setColor(Color.WHITE);
                Rectangle button = biomeButtons.get(i);
                shapeRenderer.rect(button.x - 3, button.y - 3, button.width + 6, button.height + 6);
            }
            
            // Draw biome button
            shapeRenderer.setColor(color);
            Rectangle button = biomeButtons.get(i);
            shapeRenderer.rect(button.x, button.y, button.width, button.height);
        }
    }
    
    /**
     * Render the world generator tab content
     */
    private void renderWorldGeneratorTab() {
        BiomeGenerator.WorldType[] worldTypes = BiomeGenerator.WorldType.values();
        for (int i = 0; i < worldTypeButtons.size && i < worldTypes.length; i++) {
            if (worldTypes[i] == selectedWorldType) {
                shapeRenderer.setColor(BUTTON_ACTIVE_COLOR);
            } else {
                boolean isHovered = isMouseOver(worldTypeButtons.get(i));
                shapeRenderer.setColor(isHovered ? BUTTON_HOVER_COLOR : BUTTON_COLOR);
            }
            
            Rectangle button = worldTypeButtons.get(i);
            shapeRenderer.rect(button.x, button.y, button.width, button.height);
        }
        
        // Draw generate button
        boolean generateHovered = isMouseOver(generateButton);
        shapeRenderer.setColor(generateHovered ? new Color(0.8f, 0.3f, 0.3f, 1f) : new Color(0.7f, 0.3f, 0.3f, 1f));
        shapeRenderer.rect(generateButton.x, generateButton.y, generateButton.width, generateButton.height);
    }
    
    /**
     * Render a hover indicator over the tile under the mouse
     */
    private void renderTileHover() {
        // Set the correct viewport for the map area
        mapViewport.apply();
        
        shapeRenderer.setProjectionMatrix(mapCamera.combined);
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        
        float x = hoveredTileX * SquareTileRenderer.TILE_SIZE;
        float y = (grid.getHeight() - 1 - hoveredTileY) * SquareTileRenderer.TILE_SIZE; // Invert Y for rendering
        float size = SquareTileRenderer.TILE_SIZE;
        
        shapeRenderer.rect(x, y, size, size);
        
        shapeRenderer.end();
    }
    
    /**
     * Check if the mouse is over a rectangle
     */
    private boolean isMouseOver(Rectangle rect) {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY(); // Invert Y for UI coordinates
        
        return rect.contains(mouseX, mouseY);
    }
    
    /**
     * Resize the UI components when the window is resized
     */
    public void resize(int width, int height) {
        // Update UI viewport
        uiViewport.update(width, height, false);
        
        // Update map viewport
        mapViewport.setScreenSize(width, height - BOTTOM_PANEL_HEIGHT);
        mapViewport.setScreenPosition(0, BOTTOM_PANEL_HEIGHT);
        mapViewport.update(width, height - BOTTOM_PANEL_HEIGHT, false);
        
        // Update UI areas
        mapArea.width = width;
        mapArea.height = height - BOTTOM_PANEL_HEIGHT;
        bottomPanel.width = width;
        
        // Update UI elements
        setupMainMenu();
        setupEditorUI();
        
        // Recalcular o zoom do mapa após redimensionar
        centerMapCamera();
    }
    
    /**
     * Get the input processor for this UI
     */
    public InputProcessor getInputProcessor() {
        return inputMultiplexer;
    }
    
    /**
     * Clean up resources
     */
    public void dispose() {
        // No resources to dispose in this class
    }
    
    /**
     * InputProcessor for map controls
     */
    private class MapInputProcessor extends InputAdapter {
        private Vector3 lastMousePos = new Vector3();
        private boolean isDragging = false;
        
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (button == Input.Buttons.LEFT && currentMode != EditorMode.MAIN_MENU) {
                // Only start dragging if mouse is in map area
                if (isMouseInMapArea()) {
                    isDragging = true;
                    lastMousePos.set(screenX, screenY, 0);
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (button == Input.Buttons.LEFT) {
                isDragging = false;
                return true;
            }
            return false;
        }
        
        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (isDragging && currentMode != EditorMode.MAIN_MENU) {
                float deltaX = (screenX - lastMousePos.x) * mapCamera.zoom;
                float deltaY = (lastMousePos.y - screenY) * mapCamera.zoom;
                
                mapCamera.position.x -= deltaX;
                mapCamera.position.y -= deltaY;
                mapCamera.update();
                
                lastMousePos.set(screenX, screenY, 0);
                return true;
            }
            return false;
        }
        
        @Override
        public boolean scrolled(float amountX, float amountY) {
            if (currentMode != EditorMode.MAIN_MENU && isMouseInMapArea()) {
                mapCamera.zoom += amountY * mapCamera.zoom * 0.1f;
                mapCamera.zoom = MathUtils.clamp(mapCamera.zoom, 0.2f, 10f);
                mapCamera.update();
                return true;
            }
            return false;
        }
    }
    
    /**
     * InputProcessor for UI interactions
     */
    private class UIInputProcessor extends InputAdapter {
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (button == Input.Buttons.LEFT) {
                int invertedY = Gdx.graphics.getHeight() - screenY;
                
                if (currentMode == EditorMode.MAIN_MENU) {
                    // Check main menu buttons
                    for (int i = 0; i < menuButtons.size; i++) {
                        if (menuButtons.get(i).contains(screenX, invertedY)) {
                            handleMainMenuButtonClick(i);
                            return true;
                        }
                    }
                } else {
                    // Check if click is in bottom panel
                    if (invertedY < BOTTOM_PANEL_HEIGHT) {
                        // Check tabs
                        for (int i = 0; i < tabButtons.size; i++) {
                            if (tabButtons.get(i).contains(screenX, invertedY)) {
                                selectedTabIndex = i;
                                currentMode = (i == 0) ? EditorMode.TERRAIN_EDITOR : EditorMode.WORLD_GENERATOR;
                                return true;
                            }
                        }
                        
                        // Handle tab-specific interactions
                        if (selectedTabIndex == 0) {
                            // Terrain editor tab
                            return handleTerrainEditorClick(screenX, invertedY);
                        } else {
                            // World generator tab
                            return handleWorldGeneratorClick(screenX, invertedY);
                        }
                    } 
                    // Handle map area clicks for terrain editing
                    else if (mapArea.contains(screenX, invertedY) && 
                             currentMode == EditorMode.TERRAIN_EDITOR) {
                        // Place terrain at hovered tile
                        if (hoveredTileX >= 0 && hoveredTileY >= 0) {
                            System.out.println("Placing terrain at: " + hoveredTileX + ", " + hoveredTileY);
                            grid.setTile(hoveredTileX, hoveredTileY, selectedBiome);
                            tileRenderer.markDirty();
                            return true;
                        }
                    }
                }
            }
            
            return false;
        }
        
        /**
         * Handle clicks on main menu buttons
         */
        private void handleMainMenuButtonClick(int buttonIndex) {
            String label = menuButtonLabels.get(buttonIndex);
            
            if (label.equals("New World")) {
                currentMode = EditorMode.WORLD_GENERATOR;
                selectedTabIndex = 1; // World generator tab
            } else if (label.equals("Edit Terrain")) {
                currentMode = EditorMode.TERRAIN_EDITOR;
                selectedTabIndex = 0; // Terrain editor tab
            } else if (label.equals("Exit")) {
                Gdx.app.exit();
            }
        }
        
        /**
         * Handle clicks on terrain editor tab
         */
        private boolean handleTerrainEditorClick(int screenX, int invertedY) {
            // Check biome buttons
            BiomeType[] biomes = BiomeType.values();
            for (int i = 0; i < biomeButtons.size && i < biomes.length; i++) {
                if (biomeButtons.get(i).contains(screenX, invertedY)) {
                    selectedBiome = biomes[i];
                    return true;
                }
            }
            
            return false;
        }
        
        /**
         * Handle clicks on world generator tab
         */
        private boolean handleWorldGeneratorClick(int screenX, int invertedY) {
            // Check world type buttons
            BiomeGenerator.WorldType[] worldTypes = BiomeGenerator.WorldType.values();
            for (int i = 0; i < worldTypeButtons.size && i < worldTypes.length; i++) {
                if (worldTypeButtons.get(i).contains(screenX, invertedY)) {
                    selectedWorldType = worldTypes[i];
                    return true;
                }
            }
            
            // Check generate button
            if (generateButton.contains(screenX, invertedY)) {
                generateNewWorld();
                return true;
            }
            
            return false;
        }
        
        /**
         * Generate a new world using the selected world type
         */
        private void generateNewWorld() {
            worldGenerator.generateWorld(grid, selectedWorldType);
            tileRenderer.markDirty();
            
            // Switch to terrain editing mode after generating
            currentMode = EditorMode.TERRAIN_EDITOR;
            selectedTabIndex = 0;
        }
    }
}