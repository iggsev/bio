package com.biobox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MapEditorScreen extends ApplicationAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Map map;
    private Entity entity;
    private static final int MAP_SIZE = 100;
    private static final int TILE_SIZE = 32;
    private float timeSinceLastMove = 0;
    private Vector3 touchPos = new Vector3();
    private TileType selectedTileType = TileType.GRASS;
    
    // Texturas
    private Texture tilesTexture;
    private TextureRegion grassRegion;
    private TextureRegion wallRegion;
    private TextureRegion waterRegion;
    
    // UI
    private Stage stage;
    private Skin skin;
    private Window toolbox;
    private boolean isDragging = false;
    private boolean editorMode = true;
    
    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        
        // Configurar câmera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(MAP_SIZE * TILE_SIZE / 2f, MAP_SIZE * TILE_SIZE / 2f, 0);
        
        // Carregar texturas
        try {
            // Tente carregar texturas existentes ou use renderização simples
            tilesTexture = new Texture(Gdx.files.internal("tiles.png"));
            grassRegion = new TextureRegion(tilesTexture, 0, 0, 32, 32);
            wallRegion = new TextureRegion(tilesTexture, 32, 0, 32, 32);
            waterRegion = new TextureRegion(tilesTexture, 64, 0, 32, 32);
        } catch (Exception e) {
            System.out.println("Texturas não encontradas, usando renderização básica");
            // Continuaremos com a renderização baseada em ShapeRenderer
        }
        
        // Gerar um novo mapa com o WorldGenerator
        WorldGenerator worldGen = new WorldGenerator();
        map = worldGen.generateWorld(MAP_SIZE, MAP_SIZE);
        
        // Colocar entidade em um tile caminhável
        int startX = MAP_SIZE / 2;
        int startY = MAP_SIZE / 2;
        
        // Ter certeza que a entidade começa em terreno caminhável
        while (!map.getTile(startX, startY).isWalkable()) {
            startX = MathUtils.random(0, MAP_SIZE - 1);
            startY = MathUtils.random(0, MAP_SIZE - 1);
        }
        
        entity = new Entity(map, startX, startY);
        
        // Configurar a interface de usuário
        setupUI();
        
        // Configurar o gerenciamento de entrada para lidar com UI e jogo
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(new MapInputProcessor(this));
        Gdx.input.setInputProcessor(inputMultiplexer);
    }
    
    private void setupUI() {
        // Carregar skin para a UI
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        
        // Configurar o stage
        stage = new Stage(new ScreenViewport());
        
        // Criar janela de ferramentas
        toolbox = new Window("Ferramentas do Editor", skin);
        toolbox.setMovable(true);
        toolbox.setResizable(false);
        toolbox.setPosition(20, Gdx.graphics.getHeight() - 220);
        toolbox.setSize(200, 200);
        
        // Adicionar botões para selecionar o tipo de tile
        VerticalGroup tileButtons = new VerticalGroup();
        tileButtons.space(10);
        tileButtons.pad(10);
        tileButtons.fill();
        
        // Label para o título da seção
        Label tileTypesLabel = new Label("Tipos de Terreno:", skin);
        tileButtons.addActor(tileTypesLabel);
        
        // Grupo de botões para seleção de tipo de tile
        ButtonGroup<TextButton> buttonGroup = new ButtonGroup<TextButton>();
        
        // Botão para Grama
        TextButton grassButton = new TextButton("Grama", skin, "toggle");
        grassButton.setChecked(true); // Selecionado por padrão
        grassButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (grassButton.isChecked()) {
                    selectedTileType = TileType.GRASS;
                }
            }
        });
        
        // Botão para Parede
        TextButton wallButton = new TextButton("Parede", skin, "toggle");
        wallButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (wallButton.isChecked()) {
                    selectedTileType = TileType.WALL;
                }
            }
        });
        
        // Botão para Água
        TextButton waterButton = new TextButton("Água", skin, "toggle");
        waterButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (waterButton.isChecked()) {
                    selectedTileType = TileType.WATER;
                }
            }
        });
        
        // Adicionar botões ao grupo
        buttonGroup.add(grassButton);
        buttonGroup.add(wallButton);
        buttonGroup.add(waterButton);
        
        tileButtons.addActor(grassButton);
        tileButtons.addActor(wallButton);
        tileButtons.addActor(waterButton);
        
        // Adicionar espaço
        tileButtons.addActor(new Label("", skin));
        
        // Botão para alternar modo de edição/jogo
        TextButton toggleModeButton = new TextButton("Modo: Editor", skin);
        toggleModeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                editorMode = !editorMode;
                toggleModeButton.setText("Modo: " + (editorMode ? "Editor" : "Jogo"));
            }
        });
        tileButtons.addActor(toggleModeButton);
        
        // Botão para gerar novo mapa
        TextButton newMapButton = new TextButton("Novo Mapa", skin);
        newMapButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                regenerateMap();
            }
        });
        tileButtons.addActor(newMapButton);
        
        toolbox.add(tileButtons).expand().fill();
        stage.addActor(toolbox);
    }
    
    public void regenerateMap() {
        WorldGenerator worldGen = new WorldGenerator();
        map = worldGen.generateWorld(MAP_SIZE, MAP_SIZE);
        
        // Reposicionar entidade
        int startX = MAP_SIZE / 2;
        int startY = MAP_SIZE / 2;
        while (!map.getTile(startX, startY).isWalkable()) {
            startX = MathUtils.random(0, MAP_SIZE - 1);
            startY = MathUtils.random(0, MAP_SIZE - 1);
        }
        entity = new Entity(map, startX, startY);
    }

    @Override
    public void render() {
        // Limpar tela
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // No modo de jogo, a entidade se move aleatoriamente
        if (!editorMode) {
            timeSinceLastMove += Gdx.graphics.getDeltaTime();
            if (timeSinceLastMove > 0.5f) {
                entity.moveRandomly();
                timeSinceLastMove = 0;
            }
        }
        
        // Atualizar a câmera
        handleCameraControls();
        camera.update();
        
        // Renderizar o mapa
        renderMap();
        
        // Atualizar e desenhar a UI
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }
    
    private void handleCameraControls() {
        float speed = 10f;
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            speed = 20f;
        }
        
        // Movimento da câmera
        if (Gdx.input.isKeyPressed(Input.Keys.W)) camera.position.y += speed;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) camera.position.y -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) camera.position.x -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) camera.position.x += speed;
        
        // Zoom
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) camera.zoom += 0.02f;
        if (Gdx.input.isKeyPressed(Input.Keys.E)) camera.zoom -= 0.02f;
        camera.zoom = MathUtils.clamp(camera.zoom, 0.1f, 3f);
    }
    
    public void handleMapClick(int screenX, int screenY) {
        if (!editorMode) return;
        
        touchPos.set(screenX, screenY, 0);
        camera.unproject(touchPos);
        
        int tileX = (int)(touchPos.x / TILE_SIZE);
        int tileY = (int)(touchPos.y / TILE_SIZE);
        
        if (tileX >= 0 && tileX < MAP_SIZE && tileY >= 0 && tileY < MAP_SIZE) {
            map.setTile(tileX, tileY, selectedTileType);
        }
    }
    
    public void handleMapDrag(int screenX, int screenY) {
        if (editorMode && isDragging) {
            handleMapClick(screenX, screenY);
        }
    }
    
    public void startDragging() {
        isDragging = true;
    }
    
    public void stopDragging() {
        isDragging = false;
    }

    private void renderMap() {
        // Calcular região visível para melhor desempenho
        int startX = Math.max(0, (int)((camera.position.x - camera.viewportWidth/2 * camera.zoom) / TILE_SIZE) - 1);
        int endX = Math.min(MAP_SIZE - 1, (int)((camera.position.x + camera.viewportWidth/2 * camera.zoom) / TILE_SIZE) + 1);
        int startY = Math.max(0, (int)((camera.position.y - camera.viewportHeight/2 * camera.zoom) / TILE_SIZE) - 1);
        int endY = Math.min(MAP_SIZE - 1, (int)((camera.position.y + camera.viewportHeight/2 * camera.zoom) / TILE_SIZE) + 1);
        
        // Usar SpriteBatch se tivermos texturas, caso contrário usar ShapeRenderer
        if (tilesTexture != null) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    TileType tileType = map.getTile(x, y);
                    
                    // Selecionar textura baseado no tipo de tile
                    TextureRegion region = grassRegion;
                    if (tileType == TileType.WALL) {
                        region = wallRegion;
                    } else if (tileType == TileType.WATER) {
                        region = waterRegion;
                    }
                    
                    batch.draw(region, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
            
            // Desenhar entidade
            batch.setColor(1, 0, 0, 1);
            batch.draw(grassRegion, entity.getX() * TILE_SIZE, entity.getY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            batch.setColor(1, 1, 1, 1);
            
            batch.end();
        } else {
            // Renderização baseada em formas se não temos texturas
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    TileType tileType = map.getTile(x, y);
                    
                    // Definir cor baseado no tipo de tile
                    shapeRenderer.setColor(tileType.getColor());
                    shapeRenderer.rect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
            
            // Desenhar entidade
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.circle(
                entity.getX() * TILE_SIZE + TILE_SIZE / 2f, 
                entity.getY() * TILE_SIZE + TILE_SIZE / 2f, 
                TILE_SIZE / 2f
            );
            
            shapeRenderer.end();
            
            // Desenhar a grade
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 0.5f);
            
            for (int x = startX; x <= endX + 1; x++) {
                shapeRenderer.line(x * TILE_SIZE, startY * TILE_SIZE, x * TILE_SIZE, (endY + 1) * TILE_SIZE);
            }
            
            for (int y = startY; y <= endY + 1; y++) {
                shapeRenderer.line(startX * TILE_SIZE, y * TILE_SIZE, (endX + 1) * TILE_SIZE, y * TILE_SIZE);
            }
            
            shapeRenderer.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (batch != null) batch.dispose();
        if (tilesTexture != null) tilesTexture.dispose();
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
    }
    
    // Classe interna para lidar com entrada do mapa
    private class MapInputProcessor extends com.badlogic.gdx.InputAdapter {
        private MapEditorScreen screen;
        
        public MapInputProcessor(MapEditorScreen screen) {
            this.screen = screen;
        }
        
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (button == Input.Buttons.LEFT) {
                screen.handleMapClick(screenX, screenY);
                screen.startDragging();
                return true;
            }
            return false;
        }
        
        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            screen.handleMapDrag(screenX, screenY);
            return true;
        }
        
        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (button == Input.Buttons.LEFT) {
                screen.stopDragging();
                return true;
            }
            return false;
        }
    }
}