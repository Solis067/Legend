package io.github.solis067.legend.Objects.Maps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import io.github.solis067.legend.Main;

import java.util.ArrayList;
import java.util.List;

public class GameMap {
    TiledMap tiledMap;
    TmxMapLoader mapLoader;
    OrthogonalTiledMapRenderer mapRenderer;

    World world;
    List<Body> createdBodies = new ArrayList<>();

    public GameMap(String mapFilePath, World world) {
        mapLoader = new TmxMapLoader();
        tiledMap = mapLoader.load(mapFilePath);
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, Main.UNIT_SCALE);

        this.world = world;

        if (world == null) return;

        String[] layerNames = new String[] {"collisions"};
        for (String layerName : layerNames) {
            MapLayer layer = tiledMap.getLayers().get(layerName);
            if (layer == null) continue;

            for (MapObject obj : layer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                    BodyDef bodyDef = new BodyDef();
                    bodyDef.type = BodyDef.BodyType.StaticBody;
                    float bodyX = (rect.x + rect.width / 2f) * Main.UNIT_SCALE;
                    float bodyY = (rect.y + rect.height / 2f) * Main.UNIT_SCALE;
                    bodyDef.position.set(bodyX, bodyY);

                    Body body = world.createBody(bodyDef);

                    PolygonShape shape = new PolygonShape();
                    shape.setAsBox((rect.width / 2f) * Main.UNIT_SCALE, (rect.height / 2f) * Main.UNIT_SCALE);

                    FixtureDef fixtureDef = new FixtureDef();
                    fixtureDef.shape = shape;
                    fixtureDef.friction = 0f;
                    fixtureDef.restitution = 0f;
                    body.createFixture(fixtureDef);
                    shape.dispose();
                    createdBodies.add(body);
                }
            }
        }
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
        if (world != null) {
            for (Body b : createdBodies) {
                if (b != null && b.getWorld() != null) {
                    world.destroyBody(b);
                }
            }
            createdBodies.clear();
        }
    }
}
