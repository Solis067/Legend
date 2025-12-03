package io.github.solis067.legend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;

/** First screen of the application. Displayed after the application is created. */
public class GameScreen implements Screen {
    final Main game;

    Texture playerTexture;
    Player player;
    Slime slime;
    GameMap gameMap;
    OrthographicCamera camera;

    World world;
    Box2DDebugRenderer debugRenderer;

    public GameScreen(Main game) {
        // Initialize your screen here. Store a reference to the "game" instance if needed.
        this.game = game;

        // Load assets here.
        world = new World(new Vector2(0f, 0f), true);
        debugRenderer = new Box2DDebugRenderer();

        player = new Player(world, 0, 0);
        slime = new Slime(world, 5, 5);
        
        gameMap = new GameMap("Tiled/grassland.tmx", world);

        camera = (OrthographicCamera) game.viewport.getCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.zoom = 1f; // less is closer, more is farther
    }

    @Override
    public void show() {
        // Prepare your screen here.
    }

    @Override
    public void render(float delta) {
        // Draw your screen here. "delta" is the time since last render in seconds.
        input();
        logic(delta);
        draw();
    }

    private void input() {
        player.input();
    }

    private void logic(float delta) {
        world.step(delta, 6, 2);
        slime.update(delta);
        player.update(delta);
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        game.viewport.apply();

        // Camera update
        camera.position.set(player.pos.x + player.getWidth() / 2f, player.pos.y + player.getHeight() / 2f, 0f);
        // If you need to clamp the camera to map bounds, compute min/max x/y here and clamp camera.position.
        camera.position.x = MathUtils.clamp(camera.position.x, 0, gameMap.getSizeX());
        camera.position.y = MathUtils.clamp(camera.position.y, 0, gameMap.getSizeY());
        camera.update();

        // Render Map
        gameMap.render(camera);
        debugRenderer.render(world, camera.combined);

        // Sprites
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        
        slime.draw(game);
        player.draw(game);

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        player.dispose();
        gameMap.dispose();
        world.dispose();
    }
}