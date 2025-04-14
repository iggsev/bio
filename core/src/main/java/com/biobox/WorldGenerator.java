package com.biobox;

import com.badlogic.gdx.math.MathUtils;
import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.noise.NoiseGenerator;

public class WorldGenerator {
    public Map generateWorld(int width, int height) {
        Map map = new Map(width, height);
        
        // Configurar a grid para o gerador de ruído
        Grid grid = new Grid(width, height);
        
        // Criar e configurar o gerador de ruído
        NoiseGenerator noiseGenerator = new NoiseGenerator();
        // setSeed espera um int, não um long
        noiseGenerator.setSeed(MathUtils.random(Integer.MAX_VALUE));
        
        // Configurar parâmetros - verifique quais métodos estão realmente disponíveis
        // na biblioteca noise4j, estes podem estar errados
        noiseGenerator.setRadius(1f);         // Raio de influência 
        noiseGenerator.setModifier(0.5f);     // Modificador de intensidade
        
        // Aplicar o ruído à grid
        noiseGenerator.generate(grid);        // Método correto é generate, não fill
        
        // Converter os valores de ruído para tipos de terreno
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float noiseValue = grid.get(x, y);
                
                // Mapear o valor do ruído para o tipo de terreno
                if (noiseValue < 0.3f) {
                    map.setTile(x, y, TileType.WATER);
                } else if (noiseValue < 0.7f) {
                    map.setTile(x, y, TileType.GRASS);
                } else {
                    map.setTile(x, y, TileType.WALL);
                }
            }
        }
        
        return map;
    }
}