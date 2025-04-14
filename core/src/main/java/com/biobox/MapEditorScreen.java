package com.biobox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MapEditorScreen extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private TileType[][] map;
    private int entityX, entityY;
    private static final int MAP_SIZE = 100;
    private static final int TILE_SIZE = 16;
    private Stage stage;
    private Skin skin;
    private int brushSize = 1;
    private TileType selectedTileType = TileType.GRASS;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        map = WorldGenerator.generateIsland(MAP_SIZE, MAP_SIZE);
        
        entityX = MAP_SIZE / 2;
        entityY = MAP_SIZE / 2;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        TextButton grassButton = new TextButton("Grama", skin);
        grassButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectedTileType = TileType.GRASS;
            }
        });
        table.add(grassButton).pad(5);

        TextButton wallButton = new TextButton("Parede", skin);
        wallButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectedTileType = TileType.WALL;
            }
        });
        table.add(wallButton).pad(5);

        TextButton waterButton = new TextButton("Agua", skin);
        waterButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectedTileType = TileType.WATER;
            }
        });
        table.add(waterButton).pad(5);

        Slider brushSizeSlider = new Slider(1, 5, 1, false, skin);
        brushSizeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                brushSize = (int) brushSizeSlider.getValue();
            }
        });
        table.row();
        table.add(brushSizeSlider).colspan(3).pad(5);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.5f, 0.8f, 1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (MathUtils.randomBoolean(0.1f)) {
            moveEntity();
        }

        renderMap();

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            int mapX = Gdx.input.getX() / TILE_SIZE;
            int mapY = (Gdx.graphics.getHeight() - Gdx.input.getY()) / TILE_SIZE;
            paint(mapX, mapY, brushSize, selectedTileType);
        }

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    private void moveEntity() {
        int direction = MathUtils.random(0, 3);
        int newX = entityX, newY = entityY;

        switch (direction) {
            case 0: newX++; break; // Right
            case 1: newX--; break; // Left
            case 2: newY++; break; // Up
            case 3: newY--; break; // Down
        }

        if (newX >= 0 && newX < MAP_SIZE && newY >= 0 && newY < MAP_SIZE) {
            TileType tile = map[newX][newY];
            if (tile.isWalkable()) {
                entityX = newX;
                entityY = newY;
            }
        }
    }

    private void renderMap() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (int x = 0; x < MAP_SIZE; x++) {
            for (int y = 0; y < MAP_SIZE; y++) {
                TileType type = map[x][y];
                shapeRenderer.setColor(type.getColor());
                shapeRenderer.rect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(
            entityX * TILE_SIZE + TILE_SIZE / 2f, 
            entityY * TILE_SIZE + TILE_SIZE / 2f, 
            TILE_SIZE / 2f
        );

        shapeRenderer.end();
    }

    private void paint(int centerX, int centerY, int size, TileType type) {
        int startX = Math.max(0, centerX - size / 2);
        int startY = Math.max(0, centerY - size / 2);
        int endX = Math.min(MAP_SIZE - 1, centerX + size / 2);
        int endY = Math.min(MAP_SIZE - 1, centerY + size / 2);
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                map[x][y] = type;
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        stage.dispose();
        skin.dispose();
    }
}