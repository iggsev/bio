package com.biobox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Tela principal do editor de mapas com suporte a mapas toroidais (wraparound)
 */
public class MapEditorScreen extends ApplicationAdapter {
    // Map generation types - for WorldGenerator compatibility
    public enum MapType {
        DEFAULT,
        ISLAND,
        CONTINENT,
        LAKES
    }
    
    // Definição do tamanho do mapa otimizado para tela padrão (16:9)
    private static final int MAP_WIDTH = 30;  // Tamanho do mapa reduzido ainda mais
    private static final int MAP_HEIGHT = 20; // Mantém proporção 3:2, mas com menos tiles
    
    private static final int LOD_REGION_SIZE = 4;
    private static final boolean USE_VSYNC = true;
    
    // Objetos de renderização
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private OrthographicCamera uiCamera;
    private BitmapFont font;
    private Viewport viewport;
    private Viewport uiViewport;
    
    // Componentes do jogo
    private GameMap map;
    private MapRenderer mapRenderer;
    private UIManager uiManager;
    private Array<SimpleEntity> entities = new Array<>();
    private SimpleEntity playerEntity;
    
    // Estado do jogo
    private Vector3 touchPos = new Vector3();
    private int lastPlacedTileX = -1;
    private int lastPlacedTileY = -1;
    private float timeAccumulator = 0;
    
    // Medição de performance
    private long lastFrameTime = 0;
    private long frameTimeSum = 0;
    private int frameCount = 0;
    private float fps = 0;
    
    @Override
    public void create() {
        try {
            System.out.println("Inicializando editor de mapas toroidal...");
            
            // Configurar OpenGL
            Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
            
            // Configurar VSync
            Gdx.graphics.setVSync(USE_VSYNC);
            
            // Inicializar objetos de renderização
            batch = new SpriteBatch();
            shapeRenderer = new ShapeRenderer();
            font = new BitmapFont();
            font.getData().setScale(1.2f);
            
            // Configurar câmeras com viewports para melhor estabilidade
            setupCameras();
            
            // Inicializar mapa
            map = new GameMap(MAP_WIDTH, MAP_HEIGHT, LOD_REGION_SIZE);
            
            // Inicializar renderizador do mapa
            mapRenderer = new MapRenderer(map, GameMap.TILE_SIZE, shapeRenderer);
            
            // Habilitar LOD para prevenir flickering
            mapRenderer.setUseLOD(true);
            
            // Inicializar gerenciador de UI
            uiManager = new UIManager(shapeRenderer, batch, font);
            
            // Criar jogador e algumas entidades
            createEntities();
            
            // Configurar processador de entrada
            Gdx.input.setInputProcessor(new GameInputProcessor());
            
            // Inicializar tempo do frame
            lastFrameTime = TimeUtils.millis();
            
            System.out.println("Inicialização completa");
        } catch (Exception e) {
            System.err.println("Erro durante inicialização: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupCameras() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        camera.position.set(MAP_WIDTH * GameMap.TILE_SIZE / 2f, MAP_HEIGHT * GameMap.TILE_SIZE / 2f, 0);
        camera.zoom = 1.0f; // Zoom mais próximo para ver menos tiles de uma vez
        
        uiCamera = new OrthographicCamera();
        uiViewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), uiCamera);
        uiCamera.position.set(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 0);
    }
    
    private void createEntities() {
        playerEntity = new SimpleEntity(map, MAP_WIDTH/2f, MAP_HEIGHT/2f, Color.RED);
        entities.add(playerEntity);
        
        // Criar entidades em vários lugares do mapa
        for (int i = 0; i < 30; i++) {
            float x = MathUtils.random(0, MAP_WIDTH-1);
            float y = MathUtils.random(0, MAP_HEIGHT-1);
            if (map.getTile((int)x, (int)y).isWalkable()) {
                Color entityColor = new Color(
                    MathUtils.random(0.7f, 1.0f),
                    MathUtils.random(0.3f, 0.6f),
                    MathUtils.random(0.3f, 0.6f),
                    1f
                );
                entities.add(new SimpleEntity(map, x, y, entityColor));
            }
        }
    }
    
    @Override
    public void render() {
        try {
            // Calcular delta time manualmente para movimentação consistente
            long currentTime = TimeUtils.millis();
            float deltaTime = (currentTime - lastFrameTime) / 1000f;
            lastFrameTime = currentTime;
            
            // Limitar delta time para evitar saltos grandes
            deltaTime = Math.min(deltaTime, 0.1f);
            
            // Rastrear tempo do frame para cálculo de FPS
            frameTimeSum += TimeUtils.timeSinceMillis(currentTime);
            frameCount++;
            if (frameCount >= 30) {
                fps = 1000f / (frameTimeSum / (float)frameCount);
                frameTimeSum = 0;
                frameCount = 0;
                uiManager.updateFPS(fps);
            }
            
            // Acumular tempo para atualizações de entidades
            timeAccumulator += deltaTime;
            
            // Limpar tela
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            
            // Atualizar
            handleCameraControls();
            uiManager.handleUIHover(Gdx.input.getX(), Gdx.input.getY());
            camera.update();
            uiCamera.update();
            
            // Atualizar entidades em passos de tempo fixos para evitar flickering
            if (!uiManager.isEditorMode() && timeAccumulator >= 0.016f) { // ~60 atualizações por segundo
                for (SimpleEntity entity : entities) {
                    if (entity != playerEntity) { // Não mover jogador automaticamente
                        entity.update(timeAccumulator);
                    }
                }
                timeAccumulator = 0;
            }
            
            // Atualizar viewports
            viewport.apply();
            uiViewport.apply();
            
            // Atualizar LOD no renderizador
            mapRenderer.setUseLOD(uiManager.isUsingLOD());
            
            // Renderizar mapa
            mapRenderer.renderMap(camera);
            
            // Renderizar entidades
            mapRenderer.renderEntities(camera, entities, uiManager.isEditorMode());
            
            // Renderizar UI
            uiManager.render(uiCamera, entities.size);
            
        } catch (Exception e) {
            System.err.println("Erro no loop de renderização: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleCameraControls() {
        float speed = 200f * Gdx.graphics.getDeltaTime() * camera.zoom;
        
        // Movimento da câmera
        if (Gdx.input.isKeyPressed(Input.Keys.W)) camera.position.y += speed;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) camera.position.y -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) camera.position.x -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) camera.position.x += speed;
        
        // Controle de zoom
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) camera.zoom += 0.02f;
        if (Gdx.input.isKeyPressed(Input.Keys.E)) camera.zoom -= 0.02f;
        camera.zoom = MathUtils.clamp(camera.zoom, 0.1f, 4f);
        
        // Alternar entre modo editor/jogo com barra de espaço
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            uiManager.setEditorMode(!uiManager.isEditorMode());
        }
        
        // Não precisamos limitar a câmera dentro dos limites do mapa
        // já que o mapa é toroidal (wraparound) e pode ser visto de qualquer ponto
    }
    
    private void handleMapClick(int screenX, int screenY) {
        // Converter coordenadas da tela para coordenadas do mundo
        touchPos.set(screenX, screenY, 0);
        camera.unproject(touchPos);
        
        // Converter para coordenadas de tile
        int tileX = MathUtils.floor(touchPos.x / GameMap.TILE_SIZE);
        int tileY = MathUtils.floor(touchPos.y / GameMap.TILE_SIZE);
        
        // Aplicar transformação toroidal para garantir que estamos dentro do mapa
        tileX = (tileX % MAP_WIDTH + MAP_WIDTH) % MAP_WIDTH;
        tileY = (tileY % MAP_HEIGHT + MAP_HEIGHT) % MAP_HEIGHT;
        
        // Evitar colocar no mesmo local duas vezes seguidas
        if (tileX == lastPlacedTileX && tileY == lastPlacedTileY) {
            return;
        }
        
        if (uiManager.isEditorMode()) {
            // No modo editor, colocar tiles ou entidades
            if (uiManager.isPlacingEntities()) {
                // Colocar entidade
                if (map.getTile(tileX, tileY).isWalkable()) {
                    SimpleEntity entity = new SimpleEntity(map, tileX + 0.5f, tileY + 0.5f, 
                        new Color(MathUtils.random(0.7f, 1.0f), 
                                  MathUtils.random(0.3f, 0.6f), 
                                  MathUtils.random(0.3f, 0.6f), 1f));
                    entities.add(entity);
                }
            } else {
                // Colocar tile
                map.setTile(tileX, tileY, uiManager.getSelectedTileType());
            }
            
            // Lembrar a última posição de tile colocado
            lastPlacedTileX = tileX;
            lastPlacedTileY = tileY;
        } else {
            // No modo jogo, mover entidade do jogador
            if (map.getTile(tileX, tileY).isWalkable()) {
                playerEntity.x = tileX + 0.5f;
                playerEntity.y = tileY + 0.5f;
            }
        }
    }
    
    private void resetMap() {
        // Resetar para grama com variação de cor
        map.fillWithGrass();
        
        // Resetar entidades
        entities.clear();
        playerEntity = new SimpleEntity(map, MAP_WIDTH/2f, MAP_HEIGHT/2f, Color.RED);
        entities.add(playerEntity);
        
        for (int i = 0; i < 30; i++) {
            float x = MathUtils.random(0, MAP_WIDTH-1);
            float y = MathUtils.random(0, MAP_HEIGHT-1);
            Color entityColor = new Color(
                MathUtils.random(0.7f, 1.0f),
                MathUtils.random(0.3f, 0.6f),
                MathUtils.random(0.3f, 0.6f),
                1f
            );
            entities.add(new SimpleEntity(map, x, y, entityColor));
        }
    }
    
    @Override
    public void resize(int width, int height) {
        // Atualizar viewports
        viewport.update(width, height, false);
        uiViewport.update(width, height, false);
        
        // Recalcular posições da UI
        uiManager.setupUI();
        
        camera.update();
        uiCamera.update();
    }
    
    @Override
    public void pause() {
        // Manipular pausa da aplicação
    }
    
    @Override
    public void resume() {
        // Manipular retomada da aplicação
        lastFrameTime = TimeUtils.millis();
    }
    
    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
    }
    
    /**
     * Processador de entrada para interações do jogo
     */
    private class GameInputProcessor extends InputAdapter {
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            // Verificar cliques na UI primeiro
            boolean uiClicked = uiManager.checkUIClick(screenX, screenY);
            
            if (uiClicked) {
                // Se clicou no botão de reset, resetar o mapa
                if (uiManager.isResetButtonClicked(screenX, screenY)) {
                    resetMap();
                }
                return true;
            }
            
            // Reset last placed tile position
            lastPlacedTileX = -1;
            lastPlacedTileY = -1;
            
            // Manipular interação com o mapa
            if (button == Input.Buttons.LEFT) {
                handleMapClick(screenX, screenY);
            }
            return true;
        }
        
        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            // Manipular colocação contínua de tiles no modo editor
            if (uiManager.isEditorMode()) {
                handleMapClick(screenX, screenY);
            }
            return true;
        }
    }
}