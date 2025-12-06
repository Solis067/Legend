package io.github.solis067.legend.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.solis067.legend.Player;
import io.github.solis067.legend.Slime;

/**
 * Manages the game UI using Scene2D
 */
public class GameUI {
    private Stage stage;
    private BitmapFont font;
    private Label playerHealthLabel;
    private Label slimeHealthLabel;
    private Label fpsLabel;
    private Player player;
    private Slime slime;

    public GameUI(Player player, Slime slime) {
        this.player = player;
        this.slime = slime;

        // Create stage with screen viewport for UI (separate from world camera)
        stage = new Stage(new ScreenViewport());

        // Create font for labels
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);

        // Create a table for organizing UI elements
        Table uiTable = new Table();
        uiTable.setFillParent(true); // Fill the entire screen
        uiTable.top().left(); // Align to top-left
        uiTable.pad(10); // Add padding

        // Create health labels
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);

        playerHealthLabel = new Label("Player HP: " + player.getHealth(), labelStyle);
        playerHealthLabel.setColor(Color.GREEN);

        slimeHealthLabel = new Label("Slime HP: " + slime.getHealth(), labelStyle);
        slimeHealthLabel.setColor(Color.RED);

        fpsLabel = new Label("FPS: 0", labelStyle);
        fpsLabel.setColor(Color.YELLOW);

        // Add labels to table
        uiTable.add(playerHealthLabel).row();
        uiTable.add(slimeHealthLabel).row();
        uiTable.add(fpsLabel).row();

        stage.addActor(uiTable);
    }

    public void update(float delta) {
        // Update labels with current values
        playerHealthLabel.setText("Player HP: " + player.getHealth());
        slimeHealthLabel.setText("Slime HP: " + slime.getHealth());
        fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());

        // Update stage (processes input and animations)
        stage.act(delta);
    }

    public void render() {
        // Draw the stage
        stage.draw();
    }

    public Stage getStage() {
        return stage;
    }

    public void dispose() {
        stage.dispose();
        font.dispose();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
}
