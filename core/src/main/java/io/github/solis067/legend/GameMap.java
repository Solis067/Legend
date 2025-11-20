package io.github.solis067.legend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class GameMap {
    TiledMap tiledMap;
    TmxMapLoader mapLoader;
    
    OrthogonalTiledMapRenderer mapRenderer;

    public GameMap(String mapFilePath) {
        mapLoader = new TmxMapLoader();
        tiledMap = mapLoader.load(mapFilePath);

        // Use unit scale so map pixels convert to world units (1 world unit = TILE_PIXELS pixels)
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, Main.UNIT_SCALE);
    }

    public float getSizeX() {
        return tiledMap.getProperties().get("width", Integer.class) * tiledMap.getProperties().get("tilewidth", Integer.class) * Main.UNIT_SCALE;
    }

    public float getSizeY() {
        return tiledMap.getProperties().get("height", Integer.class) * tiledMap.getProperties().get("tileheight", Integer.class) * Main.UNIT_SCALE;
    }

    public void render(OrthographicCamera camera) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mapRenderer.setView(camera);
        mapRenderer.render();
    }

    public void dispose() {
        tiledMap.dispose();
        mapRenderer.dispose();
    }

}
