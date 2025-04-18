package com.biobox;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Editor para posicionar terrenos no mapa
 */
public class WorldEditor {
    // Componentes
    private SquareGrid grid;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private SquareTileRenderer tileRenderer;
    private BiomeGenerator worldGenerator;
    private BiomeGenerator.WorldType selectedWorldType = BiomeGenerator.WorldType.CLASSIC;

    // Estado
    private BiomeType selectedBiome = BiomeType.GRASS;
    private boolean active = true;
    private int hoveredTileX = -1;
    private int hoveredTileY = -1;

    // Elementos de UI
    private Stage stage;
    private Skin skin;
    private Table panel;

    // Abas
    private enum EditorTab { TERRAIN, NEW_WORLD }
    private EditorTab currentTab = EditorTab.TERRAIN;

    public WorldEditor(SquareGrid grid, ShapeRenderer shapeRenderer, SpriteBatch batch, 
                      BitmapFont font, SquareTileRenderer tileRenderer, BiomeGenerator worldGenerator) {
        this.grid = grid;
        this.shapeRenderer = shapeRenderer;
        this.batch = batch;
        this.font = font;
        this.tileRenderer = tileRenderer;
        this.worldGenerator = worldGenerator;

        // Inicializar UI Scene2D
        this.stage = new Stage(new ScreenViewport());
        this.skin = new Skin(Gdx.files.internal("uiskin.json")); // Certifique-se de que este arquivo existe
        setupUI();
    }

    /**
     * Configura os elementos de UI usando Scene2D
     */
    public void setupUI() {
        panel = new Table();
        panel.setWidth(Gdx.graphics.getWidth());
        panel.setHeight(150);
        panel.setPosition(0, 0);

        updateUIContent();

        stage.addActor(panel);
    }

    /**
     * Atualiza o conteúdo da UI com base na aba atual
     */
    private void updateUIContent() {
        panel.clearChildren();

        // Adicionar abas
        Table tabTable = new Table();
        TextButton terrainTab = new TextButton("Edit Terrain", skin);
        terrainTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                currentTab = EditorTab.TERRAIN;
                updateUIContent();
            }
        });
        tabTable.add(terrainTab).width(120).height(30).pad(5);

        TextButton newWorldTab = new TextButton("New World", skin);
        newWorldTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                currentTab = EditorTab.NEW_WORLD;
                updateUIContent();
            }
        });
        tabTable.add(newWorldTab).width(120).height(30).pad(5);

        panel.add(tabTable).colspan(2).padBottom(10);
        panel.row();

        if (currentTab == EditorTab.TERRAIN) {
            Table buttonTable = new Table();
            BiomeType[] biomes = BiomeType.values();
            for (int i = 0; i < biomes.length; i++) {
                BiomeType biome = biomes[i];
                TextButton button = new TextButton(biome.name(), skin);
                button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        selectedBiome = biome;
                    }
                });
                buttonTable.add(button).width(100).height(40).pad(5);
                if ((i + 1) % 4 == 0) buttonTable.row();
            }
            panel.add(buttonTable);
        } else {
            Table buttonTable = new Table();
            BiomeGenerator.WorldType[] worldTypes = BiomeGenerator.WorldType.values();
            for (BiomeGenerator.WorldType worldType : worldTypes) {
                TextButton button = new TextButton(worldType.name(), skin);
                button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        selectedWorldType = worldType;
                    }
                });
                buttonTable.add(button).width(140).height(40).pad(5);
            }
            panel.add(buttonTable);

            TextButton generateButton = new TextButton("Generate Map", skin);
            generateButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    generateNewMap();
                }
            });
            panel.add(generateButton).width(150).height(40).pad(5);
        }
    }

    /**
     * Renderiza a UI do editor
     */
    public void render(OrthographicCamera camera) {
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        if (currentTab == EditorTab.TERRAIN) {
            renderTileHover(camera);
        }
    }

    /**
     * Renderiza um indicador de hover sobre o tile sob o mouse
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
     * Atualiza o tile atualmente sob o mouse
     */
    private void updateHoveredTile(OrthographicCamera camera) {
        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();

        Vector3 worldPos = new Vector3(screenX, screenY, 0);
        camera.unproject(worldPos);

        int tileX = (int)(worldPos.x / SquareTileRenderer.TILE_SIZE);
        int tileY = (int)(worldPos.y / SquareTileRenderer.TILE_SIZE);

        if (grid.isInBounds(tileX, tileY)) {
            hoveredTileX = tileX;
            hoveredTileY = tileY;
        } else {
            hoveredTileX = -1;
            hoveredTileY = -1;
        }
    }

    /**
     * Processa entrada do mouse para o editor
     */
    public boolean handleInput(OrthographicCamera camera) {
        stage.act(Gdx.graphics.getDeltaTime());

        // Verifica se o clique foi no stage
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            if (stage.hit(touchPos.x, touchPos.y, true) != null) {
                return true; // Input processado pelo stage
            }
        }

        // Processa edição de terreno
        if (active && currentTab == EditorTab.TERRAIN && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            updateHoveredTile(camera);
            if (hoveredTileX >= 0 && hoveredTileY >= 0) {
                grid.setTile(hoveredTileX, hoveredTileY, selectedBiome); // Corrigido para setTile
                tileRenderer.markDirty();
                return true;
            }
        }
        return false;
    }

    /**
     * Gera um novo mapa usando o tipo de mundo selecionado
     */
    private void generateNewMap() {
        if (worldGenerator != null) {
            worldGenerator.generateWorld(grid, selectedWorldType);
            tileRenderer.markDirty();
        }
    }

    public BiomeGenerator.WorldType getSelectedWorldType() {
        return selectedWorldType;
    }

    public void toggle() {
        active = !active;
        System.out.println("Editor active: " + active);
    }

    public boolean isActive() {
        return active;
    }

    public BiomeType getSelectedBiome() {
        return selectedBiome;
    }

    public Stage getStage() {
        return stage;
    }
}