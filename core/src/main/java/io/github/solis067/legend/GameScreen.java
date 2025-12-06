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

import io.github.solis067.legend.handlers.MyContectListener;
import io.github.solis067.legend.ui.GameUI;

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

    MyContectListener contactListener;
    GameUI gameUI;
    private boolean gameOverTriggered = false;

    public GameScreen(Main game) {
        // Initialize your screen here. Store a reference to the "game" instance if needed.
        this.game = game;

        // Load assets here.
        world = new World(new Vector2(0f, 0f), true);
        debugRenderer = new Box2DDebugRenderer();

        contactListener = new MyContectListener();
        world.setContactListener(contactListener);

        player = new Player(world, "PLAYER", 40, 20);
        slime = new Slime(world, "SLIME", 40, 25);
        
        gameMap = new GameMap("Tiled/overworld1.tmx", world);

        gameUI = new GameUI(player, slime);

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
        if (checkGameOver()) {
            return;
        }
        draw();
    }

    private void input() {
        player.input();
    }

    private void logic(float delta) {
        world.step(delta, 6, 2);
        slime.update(delta);
        player.update(delta);
        contactListener.update(delta);
        gameUI.update(delta);
    }

    private boolean checkGameOver() {
        if (gameOverTriggered) {
            return true;
        }

        if (player.isDead()) {
            gameOverTriggered = true;
            game.setScreen(new GameOverScreen(game));
            dispose();
            return true;
        }

        return false;
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

        // Sprites - draw entities in Y-order (higher Y = further back = drawn first)


        game.batch.setProjectionMatrix(camera.combined);


        game.batch.begin();



        // Draw entities based on Y position


        if (player.pos.y > slime.pos.y) {


            player.draw(game);


            slime.draw(game);


        } else {


            slime.draw(game);


            player.draw(game);


        }

        game.batch.end();

        // Render UI
        gameUI.render();
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
        gameUI.resize(width, height);
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
        gameUI.dispose();
        world.dispose();
    }
}