package io.github.solis067.legend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;


/** First screen of the application. Displayed after the application is created. */
public class GameScreen implements Screen {
    final Main game;

    Texture playerTexture;
    Player player;
    GameMap gameMap;
    OrthographicCamera camera;


    public GameScreen(Main game) {
        // Initialize your screen here. Store a reference to the "game" instance if needed.
        this.game = game;

        // Load assets here.
        player = new Player(0, 0);
        gameMap = new GameMap("Tiled/overworld1.tmx");

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

        // Sprites
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

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
    }
}