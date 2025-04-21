package com.biobox.ui;

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

import com.biobox.BiomeType;
import com.biobox.BiomeGenerator;
import com.biobox.SquareGrid;
import com.biobox.SquareTileRenderer;
import com.biobox.input.InputController;

/**
 * UI responsiva para o editor de mundos RPG.
 * Implementa soluções para os problemas de sincronização de toque e posicionamento em telas de diferentes tamanhos.
 */
public class ResponsiveWorldEditorUI implements EditorUI {
    // Componentes principais
    private final SquareGrid grid;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final SquareTileRenderer tileRenderer;
    private final BiomeGenerator worldGenerator;
    
    // Câmera e viewport para o mapa
    private final OrthographicCamera mapCamera;
    private final Viewport mapViewport;
    
    // Câmera e viewport para a UI
    private final OrthographicCamera uiCamera;
    private final Viewport uiViewport;
    
    // Layout para medição de texto
    private final GlyphLayout glyphLayout;
    
    // Processamento de entrada
    private final InputMultiplexer inputMultiplexer;
    private final MapInputProcessor mapInputProcessor;
    private final UIInputProcessor uiInputProcessor;
    
    // Estado da UI
    private boolean showMainMenu = true;
    private int selectedTabIndex = 0; // 0 = editor de terreno, 1 = gerador de mundo
    private int editorMode = 0;
    
    // Constantes de layout
    private static final float SCREEN_WIDTH = 1280;
    private static final float SCREEN_HEIGHT = 720;
    private static final float PANEL_HEIGHT_RATIO = 0.2f; // 20% da altura da tela para o painel inferior
    
    // Constantes de menu
    private static final float MENU_BUTTON_WIDTH = 200;
    private static final float MENU_BUTTON_HEIGHT = 40;
    private static final float MENU_PADDING = 15;
    
    // Áreas da interface
    private Rectangle mapArea;
    private Rectangle bottomPanel;
    
    // Componentes da UI
    private Array<Rectangle> menuButtons = new Array<>();
    private Array<String> menuButtonLabels = new Array<>();
    private Array<Rectangle> tabButtons = new Array<>();
    private Array<String> tabLabels = new Array<>();
    private Array<Rectangle> biomeButtons = new Array<>();
    private Array<String> biomeButtonLabels = new Array<>();
    
    // Editor de terreno
    private BiomeType selectedBiome = BiomeType.GRASS;
    private int hoveredTileX = -1;
    private int hoveredTileY = -1;
    
    // Gerador de mundo
    private Array<Rectangle> worldTypeButtons = new Array<>();
    private Array<String> worldTypeLabels = new Array<>();
    private BiomeGenerator.WorldType selectedWorldType = BiomeGenerator.WorldType.CLASSIC;
    
    // Cores da UI
    private static final Color PANEL_COLOR = new Color(0.2f, 0.2f, 0.25f, 0.9f);
    private static final Color BUTTON_COLOR = new Color(0.3f, 0.3f, 0.4f, 1f);
    private static final Color BUTTON_HOVER_COLOR = new Color(0.4f, 0.4f, 0.5f, 1f);
    private static final Color BUTTON_ACTIVE_COLOR = new Color(0.3f, 0.5f, 0.8f, 1f);
    private static final Color TEXT_COLOR = new Color(0.9f, 0.9f, 0.9f, 1f);
    
    // Status da orientação
    private boolean isPortrait = false;
    
    // Controlador de zoom
    private ZoomController zoomController;
    
    // Construtor
    public ResponsiveWorldEditorUI(SquareGrid grid, ShapeRenderer shapeRenderer, 
                                 SpriteBatch batch, BitmapFont font, 
                                 SquareTileRenderer tileRenderer, 
                                 BiomeGenerator worldGenerator) {
        this.grid = grid;
        this.shapeRenderer = shapeRenderer;
        this.batch = batch;
        this.font = font;
        this.tileRenderer = tileRenderer;
        this.worldGenerator = worldGenerator;
        this.glyphLayout = new GlyphLayout();
        
        // Configurar câmeras e viewports
        this.mapCamera = new OrthographicCamera();
        this.mapViewport = new FitViewport(
            grid.getWidth() * SquareTileRenderer.TILE_SIZE,
            grid.getHeight() * SquareTileRenderer.TILE_SIZE,
            mapCamera
        );
        
        this.uiCamera = new OrthographicCamera();
        this.uiViewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT, uiCamera);
        
        // Centralizar câmeras
        centerMapCamera();
        uiCamera.position.set(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 0);
        
        // Atualizar câmeras
        mapCamera.update();
        uiCamera.update();
        
        // Verificar orientação inicial
        updateOrientation();
        
        // Configurar áreas da UI
        setupUI();
        
        // Controlador de zoom
        zoomController = new ZoomController();
        
        // Configurar processadores de entrada
        mapInputProcessor = new MapInputProcessor();
        uiInputProcessor = new UIInputProcessor();
        inputMultiplexer = new InputMultiplexer(uiInputProcessor, mapInputProcessor);
    }
    
    /**
     * Verifica e atualiza a orientação da tela (retrato/paisagem)
     */
    private void updateOrientation() {
        isPortrait = Gdx.graphics.getHeight() > Gdx.graphics.getWidth();
    }
    
    /**
     * Configura as áreas da UI com base na orientação da tela
     */
    private void setupUI() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float panelHeight = screenHeight * PANEL_HEIGHT_RATIO;
        float panelWidth = screenWidth * PANEL_HEIGHT_RATIO;
        
        if (isPortrait) {
            // Em modo retrato, o painel fica na parte inferior
            bottomPanel = new Rectangle(0, 0, screenWidth, panelHeight);
            mapArea = new Rectangle(0, panelHeight, screenWidth, screenHeight - panelHeight);
        } else {
            // Em modo paisagem, o painel fica à direita
            bottomPanel = new Rectangle(screenWidth - panelWidth, 0, panelWidth, screenHeight);
            mapArea = new Rectangle(0, 0, screenWidth - panelWidth, screenHeight);
        }
        
        setupMainMenu();
        setupTabs();
        setupBiomeButtons();
        setupWorldTypeButtons();
    }
    
    /**
     * Configura o menu principal com centralização correta
     */
    private void setupMainMenu() {
        menuButtons.clear();
        menuButtonLabels.clear();
        
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        String[] labels = {"Novo Mundo", "Editar Terreno", "Sair"};
        int numButtons = labels.length;
        
        // Calcular a altura total do menu (todos os botões + espaçamento)
        float totalHeight = numButtons * MENU_BUTTON_HEIGHT + (numButtons - 1) * MENU_PADDING;
        float startY = (screenHeight - totalHeight) / 2; // Centralizar verticalmente
        
        for (int i = 0; i < numButtons; i++) {
            Rectangle button = new Rectangle(
                (screenWidth - MENU_BUTTON_WIDTH) / 2, // Centralizar horizontalmente
                startY + (numButtons - 1 - i) * (MENU_BUTTON_HEIGHT + MENU_PADDING), // Ordem de cima para baixo
                MENU_BUTTON_WIDTH,
                MENU_BUTTON_HEIGHT
            );
            menuButtons.add(button);
            menuButtonLabels.add(labels[i]);
        }
    }
    
    /**
     * Configura as abas para alternar entre modos do editor
     */
    private void setupTabs() {
        tabButtons.clear();
        tabLabels.clear();
        
        String[] tabs = {"Editar Terreno", "Gerar Mundo"};
        float tabWidth = 120;
        float tabHeight = 30;
        float tabX = bottomPanel.x + 10;
        float tabY = bottomPanel.y + bottomPanel.height - tabHeight - 10;
        
        for (int i = 0; i < tabs.length; i++) {
            Rectangle tab = new Rectangle(
                tabX + i * (tabWidth + 10),
                tabY,
                tabWidth,
                tabHeight
            );
            
            tabButtons.add(tab);
            tabLabels.add(tabs[i]);
        }
    }
    
    /**
     * Configura os botões de bioma para o editor de terreno
     */
    private void setupBiomeButtons() {
        biomeButtons.clear();
        biomeButtonLabels.clear();
        
        BiomeType[] biomes = BiomeType.values();
        float buttonSize = 32;
        float spacing = 8;
        // Calcular quantos botões cabem por linha
        int buttonsPerRow = Math.max(1, (int)((bottomPanel.width - 20) / (buttonSize + spacing)));
        float startX = bottomPanel.x + 10;
        float startY = bottomPanel.y + bottomPanel.height - 70;
        
        for (int i = 0; i < biomes.length; i++) {
            int row = i / buttonsPerRow;
            int col = i % buttonsPerRow;
            
            Rectangle button = new Rectangle(
                startX + col * (buttonSize + spacing),
                startY - row * (buttonSize + spacing) - buttonSize,
                buttonSize,
                buttonSize
            );
            
            biomeButtons.add(button);
            biomeButtonLabels.add(biomes[i].name());
        }
    }
    
    /**
     * Configura os botões de tipo de mundo para o gerador
     */
    private void setupWorldTypeButtons() {
        worldTypeButtons.clear();
        worldTypeLabels.clear();
        
        BiomeGenerator.WorldType[] worldTypes = BiomeGenerator.WorldType.values();
        float buttonWidth = Math.min(180, (bottomPanel.width - 30) / 2);
        float buttonHeight = 35;
        float spacing = 10;
        int buttonsPerRow = Math.max(1, (int)((bottomPanel.width - 20) / (buttonWidth + spacing)));
        float startX = bottomPanel.x + 10;
        float startY = bottomPanel.y + bottomPanel.height - 70;
        
        for (int i = 0; i < worldTypes.length; i++) {
            int row = i / buttonsPerRow;
            int col = i % buttonsPerRow;
            
            Rectangle button = new Rectangle(
                startX + col * (buttonWidth + spacing),
                startY - row * (buttonHeight + spacing) - buttonHeight,
                buttonWidth,
                buttonHeight
            );
            
            worldTypeButtons.add(button);
            worldTypeLabels.add(worldTypes[i].name());
        }
    }
    
    /**
     * Centraliza a câmera do mapa
     */
    private void centerMapCamera() {
        float width = grid.getWidth() * SquareTileRenderer.TILE_SIZE;
        float height = grid.getHeight() * SquareTileRenderer.TILE_SIZE;
        mapCamera.position.set(width / 2f, height / 2f, 0);
        
        // Encontrar zoom ideal para caber na área do mapa
        float mapWidthRatio = mapArea != null ? mapArea.width / width : 1.0f;
        float mapHeightRatio = mapArea != null ? mapArea.height / height : 1.0f;
        float zoom = Math.min(mapWidthRatio, mapHeightRatio) * 0.9f;
        
        mapCamera.zoom = 1f / zoom;
        mapCamera.update();
    }
    
    /**
     * Processa a entrada do usuário
     */
    @Override
    public void update(float deltaTime, InputController inputController) {
        // Atualizar coordenadas de hover se no modo editor
        if (!showMainMenu && editorMode == 0) {
            updateHoveredTile();
        }
        
        // Atualizar zoom controller
        zoomController.update(deltaTime);
    }
    
    /**
     * Atualiza o tile hovered com base na posição do mouse/toque
     */
    private void updateHoveredTile() {
        if (!isMouseInMapArea()) {
            hoveredTileX = -1;
            hoveredTileY = -1;
            return;
        }
        
        // Obter coordenadas do mouse em espaço de tela
        float screenX = Gdx.input.getX();
        float screenY = Gdx.input.getY();
        
        // Converter para coordenadas no espaço do mapa
        Vector3 worldCoords = new Vector3(screenX, screenY, 0);
        mapViewport.unproject(worldCoords);
        
        // Calcular coordenadas do tile
        int tileX = (int)(worldCoords.x / SquareTileRenderer.TILE_SIZE);
        int tileY = (int)(worldCoords.y / SquareTileRenderer.TILE_SIZE);
        
        // Verificar se está dentro dos limites
        if (grid.isInBounds(tileX, tileY)) {
            hoveredTileX = tileX;
            hoveredTileY = tileY;
        } else {
            hoveredTileX = -1;
            hoveredTileY = -1;
        }
    }
    
    /**
     * Verifica se o mouse está na área do mapa
     */
    private boolean isMouseInMapArea() {
        // Converter coordenadas do mouse para coordenadas de UI
        Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        uiViewport.unproject(touchPos);
        return mapArea != null && mapArea.contains(touchPos.x, touchPos.y);
    }
    
    /**
     * Renderiza a interface
     */
    @Override
    public void render() {
        // Limpar a tela
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        if (showMainMenu) {
            renderMainMenu();
        } else {
            renderEditor();
        }
    }
    
    /**
     * Renderiza o menu principal centralizado
     */
    private void renderMainMenu() {
        uiViewport.apply();
        
        // Renderizar fundo do menu
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(PANEL_COLOR);
        
        float menuWidth = MENU_BUTTON_WIDTH + 40;
        float menuHeight = menuButtons.size * MENU_BUTTON_HEIGHT + 
                          (menuButtons.size - 1) * MENU_PADDING + 80;
        
        shapeRenderer.rect(
            SCREEN_WIDTH / 2 - menuWidth / 2,
            SCREEN_HEIGHT / 2 - menuHeight / 2,
            menuWidth,
            menuHeight
        );
        
        // Renderizar botões
        for (int i = 0; i < menuButtons.size; i++) {
            Rectangle button = menuButtons.get(i);
            
            // Verificar se o mouse está sobre o botão
            boolean isHovered = isMouseOver(button);
            shapeRenderer.setColor(isHovered ? BUTTON_HOVER_COLOR : BUTTON_COLOR);
            
            shapeRenderer.rect(button.x, button.y, button.width, button.height);
        }
        shapeRenderer.end();
        
        // Renderizar bordas e contornos
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(
            SCREEN_WIDTH / 2 - menuWidth / 2,
            SCREEN_HEIGHT / 2 - menuHeight / 2,
            menuWidth,
            menuHeight
        );
        for (Rectangle button : menuButtons) {
            shapeRenderer.rect(button.x, button.y, button.width, button.height);
        }
        shapeRenderer.end();
        
        // Renderizar texto
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        
        // Título
        font.setColor(Color.WHITE);
        String title = "RPG World Generator";
        glyphLayout.setText(font, title);
        font.draw(batch, title, 
            SCREEN_WIDTH / 2 - glyphLayout.width / 2,
            SCREEN_HEIGHT / 2 + menuHeight / 2 - 30
        );
        
        // Textos dos botões
        font.setColor(TEXT_COLOR);
        for (int i = 0; i < menuButtons.size; i++) {
            Rectangle button = menuButtons.get(i);
            String label = menuButtonLabels.get(i);
            
            glyphLayout.setText(font, label);
            font.draw(batch, label,
                button.x + (button.width - glyphLayout.width) / 2,
                button.y + (button.height + glyphLayout.height) / 2
            );
        }
        
        batch.end();
    }
    
    /**
     * Renderiza o editor e suas ferramentas
     */
    private void renderEditor() {
        // Renderizar o mapa
        renderMap();
        
        // Renderizar o painel inferior
        renderBottomPanel();
    }
    
    /**
     * Renderiza o mapa com o viewport correto
     */
    private void renderMap() {
        mapViewport.apply();
        
        // Renderizar o mapa com a câmera do mapa
        tileRenderer.render(mapCamera);
        
        // Renderizar highlight do tile selecionado
        if (editorMode == 0 && hoveredTileX >= 0 && hoveredTileY >= 0) {
            shapeRenderer.setProjectionMatrix(mapCamera.combined);
            shapeRenderer.begin(ShapeType.Line);
            shapeRenderer.setColor(Color.WHITE);
            
            float x = hoveredTileX * SquareTileRenderer.TILE_SIZE;
            float y = hoveredTileY * SquareTileRenderer.TILE_SIZE;
            
            shapeRenderer.rect(x, y, SquareTileRenderer.TILE_SIZE, SquareTileRenderer.TILE_SIZE);
            shapeRenderer.end();
        }
    }
    
    /**
     * Renderiza o painel inferior com as ferramentas do editor
     */
    private void renderBottomPanel() {
        uiViewport.apply();
        
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeType.Filled);
        
        // Fundo do painel
        shapeRenderer.setColor(PANEL_COLOR);
        shapeRenderer.rect(bottomPanel.x, bottomPanel.y, bottomPanel.width, bottomPanel.height);
        
        // Abas
        for (int i = 0; i < tabButtons.size; i++) {
            if (i == editorMode) {
                shapeRenderer.setColor(BUTTON_ACTIVE_COLOR);
            } else {
                shapeRenderer.setColor(BUTTON_COLOR);
            }
            
            Rectangle tab = tabButtons.get(i);
            shapeRenderer.rect(tab.x, tab.y, tab.width, tab.height);
        }
        
        // Conteúdo específico para cada aba
        if (editorMode == 0) {
            renderTerrainTools();
        } else {
            renderWorldGeneratorTools();
        }
        
        shapeRenderer.end();
        
        // Renderizar texto das abas
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        font.setColor(TEXT_COLOR);
        
        for (int i = 0; i < tabButtons.size; i++) {
            Rectangle tab = tabButtons.get(i);
            String label = tabLabels.get(i);
            
            glyphLayout.setText(font, label);
            font.draw(batch, label,
                tab.x + (tab.width - glyphLayout.width) / 2,
                tab.y + (tab.height + glyphLayout.height) / 2
            );
        }
        
        // Informações adicionais
        String info = "Modo: " + (editorMode == 0 ? "Editor de Terreno" : "Gerador de Mundo");
        font.draw(batch, info, bottomPanel.x + 10, bottomPanel.y + bottomPanel.height - 40);
        
        if (hoveredTileX >= 0 && hoveredTileY >= 0) {
            String tileInfo = "Tile: " + hoveredTileX + ", " + hoveredTileY + 
                            " - " + grid.getTile(hoveredTileX, hoveredTileY);
            font.draw(batch, tileInfo, bottomPanel.x + 10, bottomPanel.y + 20);
        }
        
        batch.end();
    }
    
    /**
     * Renderiza as ferramentas do editor de terreno
     */
    private void renderTerrainTools() {
        BiomeType[] biomes = BiomeType.values();
        
        // Renderizar botões de bioma
        for (int i = 0; i < biomeButtons.size; i++) {
            Rectangle button = biomeButtons.get(i);
            
            // Pular se o índice estiver fora dos limites
            if (i >= biomes.length) continue;
            
            // Cor baseada no bioma
            Color biomeColor = biomes[i].getBaseColor();
            
            // Destaque para o bioma selecionado
            if (biomes[i] == selectedBiome) {
                shapeRenderer.setColor(Color.WHITE);
                shapeRenderer.rect(button.x - 2, button.y - 2, button.width + 4, button.height + 4);
            }
            
            // Renderizar o botão
            shapeRenderer.setColor(biomeColor);
            shapeRenderer.rect(button.x, button.y, button.width, button.height);
        }
    }
    
    /**
     * Renderiza as ferramentas do gerador de mundo
     */
    private void renderWorldGeneratorTools() {
        // Renderizar botões de tipo de mundo
        for (int i = 0; i < worldTypeButtons.size; i++) {
            Rectangle button = worldTypeButtons.get(i);
            
            // Verificar se está selecionado
            if (i < BiomeGenerator.WorldType.values().length && 
                BiomeGenerator.WorldType.values()[i] == selectedWorldType) {
                shapeRenderer.setColor(BUTTON_ACTIVE_COLOR);
            } else {
                boolean isHovered = isMouseOver(button);
                shapeRenderer.setColor(isHovered ? BUTTON_HOVER_COLOR : BUTTON_COLOR);
            }
            
            shapeRenderer.rect(button.x, button.y, button.width, button.height);
        }
        
        // Botão de gerar
        Rectangle generateButton = new Rectangle(
            bottomPanel.x + bottomPanel.width - 110,
            bottomPanel.y + 20,
            100,
            40
        );
        
        boolean isGenerateHovered = isMouseOver(generateButton);
        shapeRenderer.setColor(isGenerateHovered ? 
            new Color(0.9f, 0.4f, 0.4f, 1f) : new Color(0.8f, 0.3f, 0.3f, 1f));
        shapeRenderer.rect(generateButton.x, generateButton.y, generateButton.width, generateButton.height);
    }
    
    /**
     * Verifica se o mouse está sobre um retângulo
     */
    private boolean isMouseOver(Rectangle rect) {
        Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        uiViewport.unproject(touchPos);
        return rect.contains(touchPos.x, touchPos.y);
    }
    
    /**
     * Processa um toque na tela
     */
    public boolean handleTouch(float screenX, float screenY, boolean isDown) {
        // Converter coordenadas de tela para coordenadas da UI
        Vector3 touchPos = new Vector3(screenX, screenY, 0);
        uiViewport.unproject(touchPos);
        float uiX = touchPos.x;
        float uiY = touchPos.y;
        
        // Primeiro, verificar se o zoomController lida com o toque
        if (zoomController.handleTouch(screenX, screenY, isDown)) {
            return true;
        }
        
        // Ignorar eventos que não sejam "touch down" para interações da UI
        if (!isDown) {
            return false;
        }
        
        if (showMainMenu) {
            // Verificar botões do menu principal
            for (int i = 0; i < menuButtons.size; i++) {
                if (menuButtons.get(i).contains(uiX, uiY)) {
                    handleMainMenuButtonClick(i);
                    return true;
                }
            }
        } else {
            // Verificar se o toque está no painel inferior
            if (bottomPanel.contains(uiX, uiY)) {
                for (int i = 0; i < tabButtons.size; i++) {
                    if (tabButtons.get(i).contains(uiX, uiY)) {
                        selectedTabIndex = i;
                        editorMode = i;
                        return true;
                    }
                }
                if (selectedTabIndex == 0) {
                    return handleTerrainEditorClick(uiX, uiY);
                } else {
                    return handleWorldGeneratorClick(uiX, uiY);
                }
            } else if (mapArea.contains(uiX, uiY) && selectedTabIndex == 0 && 
                       hoveredTileX >= 0 && hoveredTileY >= 0) {
                // Colocar terreno no tile clicado
                grid.setTile(hoveredTileX, hoveredTileY, selectedBiome);
                tileRenderer.markDirty();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Processa cliques em botões do menu principal
     */
    private void handleMainMenuButtonClick(int buttonIndex) {
        if (buttonIndex >= 0 && buttonIndex < menuButtonLabels.size) {
            String label = menuButtonLabels.get(buttonIndex);
            
            if (label.equals("Novo Mundo") || label.equals("New World")) {
                showMainMenu = false;
                editorMode = 1; // Modo gerador de mundo
                selectedTabIndex = 1;
            } else if (label.equals("Editar Terreno") || label.equals("Edit Terrain")) {
                showMainMenu = false;
                editorMode = 0; // Modo editor de terreno
                selectedTabIndex = 0;
            } else if (label.equals("Sair") || label.equals("Exit")) {
                Gdx.app.exit();
            }
        }
    }
    
    /**
     * Processa cliques no editor de terreno
     */
    private boolean handleTerrainEditorClick(float uiX, float uiY) {
        BiomeType[] biomes = BiomeType.values();
        
        // Verificar botões de bioma
        for (int i = 0; i < biomeButtons.size && i < biomes.length; i++) {
            if (biomeButtons.get(i).contains(uiX, uiY)) {
                selectedBiome = biomes[i];
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Processa cliques no gerador de mundo
     */
    private boolean handleWorldGeneratorClick(float uiX, float uiY) {
        BiomeGenerator.WorldType[] worldTypes = BiomeGenerator.WorldType.values();
        
        // Verificar botões de tipo de mundo
        for (int i = 0; i < worldTypeButtons.size && i < worldTypes.length; i++) {
            if (worldTypeButtons.get(i).contains(uiX, uiY)) {
                selectedWorldType = worldTypes[i];
                return true;
            }
        }
        
        // Verificar botão de gerar
        Rectangle generateButton = new Rectangle(
            bottomPanel.x + bottomPanel.width - 110,
            bottomPanel.y + 20,
            100,
            40
        );
        
        if (generateButton.contains(uiX, uiY)) {
            generateWorld();
            return true;
        }
        
        return false;
    }
    
    /**
     * Gera um novo mundo com o tipo selecionado
     */
    private void generateWorld() {
        worldGenerator.generateWorld(grid, selectedWorldType);
        tileRenderer.markDirty();
        
        // Mudar para o modo de edição após gerar
        editorMode = 0;
        selectedTabIndex = 0;
    }
    
    @Override
    public void resize(int width, int height) {
        uiViewport.update(width, height, true);
        updateOrientation();
        if (mapArea != null) {
            mapViewport.update((int)mapArea.width, (int)mapArea.height, false);
        }
        setupUI();
        zoomController.resize(width, height);
        centerMapCamera();
    }
    
    @Override
    public void dispose() {
        // Sem recursos específicos para liberar
    }
    
    @Override
    public OrthographicCamera getCamera() {
        return mapCamera;
    }
    
    @Override
    public void toggleGrid() {
        tileRenderer.toggleGrid();
    }
    
    @Override
    public boolean isGridVisible() {
        return tileRenderer.isShowingGrid();
    }
    
    @Override
    public void setEditorMode(int mode) {
        this.editorMode = mode;
        this.selectedTabIndex = mode;
    }
    
    @Override
    public int getEditorMode() {
        return editorMode;
    }
    
    @Override
    public void generateWorld(int worldType) {
        if (worldType >= 0 && worldType < BiomeGenerator.WorldType.values().length) {
            selectedWorldType = BiomeGenerator.WorldType.values()[worldType];
            generateWorld();
        }
    }
    
    @Override
    public boolean isInMapArea(float x, float y) {
        Vector3 touchPos = new Vector3(x, y, 0);
        uiViewport.unproject(touchPos);
        return mapArea != null && mapArea.contains(touchPos.x, touchPos.y);
    }
    
    @Override
    public float getUIWidth() {
        return SCREEN_WIDTH;
    }
    
    @Override
    public float getUIHeight() {
        return SCREEN_HEIGHT;
    }
    
    /**
     * Retorna o processador de entrada para este UI
     */
    public InputProcessor getInputProcessor() {
        return inputMultiplexer;
    }
    
    /**
     * Classe para controlar o zoom do mapa
     */
    private class ZoomController {
        private float targetZoom;
        private float zoomSpeed = 2.0f;
        
        // Botões de zoom
        private Rectangle zoomInButton;
        private Rectangle zoomOutButton;
        
        public ZoomController() {
            this.targetZoom = mapCamera.zoom;
            
            // Posicionar botões
            updateButtonPositions();
        }
        
        public void resize(int width, int height) {
            updateButtonPositions();
        }
        
        private void updateButtonPositions() {
            float buttonSize = 40;
            float margin = 10;
            
            zoomInButton = new Rectangle(
                margin, 
                Gdx.graphics.getHeight() - buttonSize - margin,
                buttonSize,
                buttonSize
            );
            
            zoomOutButton = new Rectangle(
                margin + buttonSize + 5,
                Gdx.graphics.getHeight() - buttonSize - margin,
                buttonSize,
                buttonSize
            );
        }
        
        public void update(float deltaTime) {
            // Aproximar o zoom atual do zoom alvo
            if (Math.abs(mapCamera.zoom - targetZoom) > 0.01f) {
                mapCamera.zoom = MathUtils.lerp(mapCamera.zoom, targetZoom, zoomSpeed * deltaTime);
                mapCamera.update();
            }
        }
        
        public boolean handleTouch(float screenX, float screenY, boolean isDown) {
            if (!isDown) return false;
            
            // Converter para coordenadas da UI
            Vector3 touchPos = new Vector3(screenX, screenY, 0);
            uiViewport.unproject(touchPos);
            
            // Verificar botões de zoom
            if (zoomInButton.contains(touchPos.x, touchPos.y)) {
                zoomIn();
                return true;
            } else if (zoomOutButton.contains(touchPos.x, touchPos.y)) {
                zoomOut();
                return true;
            }
            
            return false;
        }
        
        public void render(ShapeRenderer renderer, SpriteBatch batch, BitmapFont font) {
            uiViewport.apply();
            
            renderer.begin(ShapeType.Filled);
            renderer.setColor(BUTTON_COLOR);
            renderer.rect(zoomInButton.x, zoomInButton.y, zoomInButton.width, zoomInButton.height);
            renderer.rect(zoomOutButton.x, zoomOutButton.y, zoomOutButton.width, zoomOutButton.height);
            renderer.end();
            
            batch.begin();
            font.setColor(Color.WHITE);
            font.draw(batch, "+", zoomInButton.x + 15, zoomInButton.y + 25);
            font.draw(batch, "-", zoomOutButton.x + 17, zoomOutButton.y + 25);
            batch.end();
        }
        
        public void zoomIn() {
            targetZoom = Math.max(0.2f, mapCamera.zoom * 0.8f);
        }
        
        public void zoomOut() {
            targetZoom = Math.min(5.0f, mapCamera.zoom * 1.2f);
        }
    }
    
    /**
     * Processador de entrada para o mapa
     */
    private class MapInputProcessor extends InputAdapter {
        private Vector3 lastTouch = new Vector3();
        private boolean dragging = false;
        
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (button != Input.Buttons.LEFT) return false;
            
            Vector3 touchPos = new Vector3(screenX, screenY, 0);
            uiViewport.unproject(touchPos);
            
            if (mapArea != null && mapArea.contains(touchPos.x, touchPos.y) && !showMainMenu) {
                dragging = true;
                lastTouch.set(screenX, screenY, 0);
                return true;
            }
            
            return false;
        }
        
        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            dragging = false;
            return false;
        }
        
        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (!dragging) return false;
            
            float deltaX = (screenX - lastTouch.x) * mapCamera.zoom;
            float deltaY = (lastTouch.y - screenY) * mapCamera.zoom;
            
            mapCamera.position.x -= deltaX;
            mapCamera.position.y -= deltaY;
            mapCamera.update();
            
            lastTouch.set(screenX, screenY, 0);
            return true;
        }
        
        @Override
        public boolean scrolled(float amountX, float amountY) {
            Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            uiViewport.unproject(mousePos);
            
            if (mapArea != null && mapArea.contains(mousePos.x, mousePos.y) && !showMainMenu) {
                if (amountY > 0) {
                    zoomController.zoomOut();
                } else if (amountY < 0) {
                    zoomController.zoomIn();
                }
                return true;
            }
            
            return false;
        }
    }
    
    /**
     * Processador de entrada para a UI
     */
    private class UIInputProcessor extends InputAdapter {
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (button != Input.Buttons.LEFT) return false;
            return handleTouch(screenX, screenY, true);
        }
        
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.ESCAPE) {
                if (!showMainMenu) {
                    showMainMenu = true;
                    return true;
                }
            } else if (keycode == Input.Keys.G) {
                toggleGrid();
                return true;
            } else if (keycode == Input.Keys.TAB) {
                if (!showMainMenu) {
                    editorMode = (editorMode + 1) % 2;
                    selectedTabIndex = editorMode;
                    return true;
                }
            }
            
            return false;
        }
    }
}