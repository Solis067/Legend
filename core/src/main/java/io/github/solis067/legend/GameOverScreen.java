package io.github.solis067.legend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameOverScreen implements Screen {

    Sound gameOverSound = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/game_over.mp3"));

    private static final String TITLE_TEXT = "Game Over";
    private static final String PROMPT_TEXT = "Press ENTER or SPACE to restart";

    private final Main game;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();
    private final ScreenViewport uiViewport;

    public GameOverScreen(Main game) {
        this.game = game;
        this.font = new BitmapFont();
        this.font.getData().setScale(1f);
        this.uiViewport = new ScreenViewport();
        this.uiViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    @Override
    public void show() {
        gameOverSound.play();
    }

    @Override
    public void render(float delta) {
        handleInput();

        ScreenUtils.clear(Color.BLACK);
        uiViewport.apply();
        game.batch.setProjectionMatrix(uiViewport.getCamera().combined);

        game.batch.begin();
        font.setColor(Color.WHITE);

        float worldWidth = uiViewport.getWorldWidth();
        float worldHeight = uiViewport.getWorldHeight();

        layout.setText(font, TITLE_TEXT);
        float titleX = (worldWidth - layout.width) / 2f;
        float titleY = (worldHeight + layout.height) / 2f;
        font.draw(game.batch, layout, titleX, titleY);

        layout.setText(font, PROMPT_TEXT);
        float promptX = (worldWidth - layout.width) / 2f;
        float promptY = titleY - layout.height * 2f;
        font.draw(game.batch, layout, promptX, promptY);

        game.batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.setScreen(new GameScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        uiViewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        font.dispose();
        gameOverSound.dispose();
    }
}
