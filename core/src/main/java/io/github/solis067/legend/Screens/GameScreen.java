package io.github.solis067.legend.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.audio.Music;

import io.github.solis067.legend.Main;
import io.github.solis067.legend.Objects.Entities.Player;
import io.github.solis067.legend.Objects.Entities.Slime;
import io.github.solis067.legend.Objects.Maps.GameMap;
import io.github.solis067.legend.handlers.MyContectListener;
import io.github.solis067.legend.Objects.Ui.GameUI;

/** First screen of the application. Displayed after the application is created. */
public class GameScreen implements Screen {
    private final Main game;
    private final SpriteBatch batch;
    private final Player player;
    private final Slime slime;

    GameMap gameMap;
    OrthographicCamera camera;
    Music overworldTheme;

    World world;

    MyContectListener contactListener;
    GameUI gameUI;

    public GameScreen(Main game) {
        this.game = game;
        this.batch = game.batch;

        this.world = new World(new Vector2(0f, 0f), true);
        this.contactListener = new MyContectListener();
        this.world.setContactListener(contactListener);

        this.player = new Player(world, "PLAYER", 40, 20);
        this.slime = new Slime(world, "SLIME", 40, 25, player);
        this.gameMap = new GameMap("Tiled/overworld1.tmx", world);
        this.gameUI = new GameUI(player, slime);

        this.overworldTheme = Gdx.audio.newMusic(Gdx.files.internal("Audio/Music/nes_07-jazz.wav"));

        this.camera = (OrthographicCamera) game.viewport.getCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.camera.zoom = 1f; // less is closer, more is farther
    }

    @Override
    public void show() {
        overworldTheme.setLooping(true);
        overworldTheme.setVolume(0.4f);
        overworldTheme.play();
    }

    @Override
    public void render(float delta) {
        input();
        logic(delta);
        draw();
    }

    private void input() {
        player.resetVelocity();
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) player.attack();
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) player.moveLeft();
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) player.moveRight();
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) player.moveUp();
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) player.moveDown();

        if (slime.isDead()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                slime.respawn();
                player.refillHealth();
            }
        }
    }

    private void logic(float delta) {
        if (player.isDead()) GameOver();
        world.step(delta, 6, 2);
        slime.update(delta);
        player.update(delta);
        contactListener.update(delta);
        gameUI.update(delta);
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        game.viewport.apply();

        // Camera update
        camera.position.set(player.getPosX() + player.getWidth() / 2f, player.getPosY() + player.getHeight() / 2f, 0f);
        // If you need to clamp the camera to map bounds, compute min/max x/y here and clamp camera.position.
        camera.position.x = MathUtils.clamp(camera.position.x, 0, gameMap.getSizeX());
        camera.position.y = MathUtils.clamp(camera.position.y, 0, gameMap.getSizeY());
        camera.update();

        gameMap.render(camera);

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // Draw entities based on Y position
        if (player.getPosY() > slime.getPosY()) {
            player.render(batch);
            slime.render(batch);
        } else {
            slime.render(batch);
            player.render(batch);
        }

        game.batch.end();
        gameUI.render();
    }

    private void GameOver() {
        game.setScreen(new GameOverScreen(game));
    }

    @Override
    public void resize(int width, int height) {
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
        overworldTheme.stop();
    }

    @Override
    public void dispose() {
        player.dispose();
        slime.dispose();
        gameMap.dispose();
        gameUI.dispose();
        world.dispose();
        overworldTheme.dispose();
    }
}
