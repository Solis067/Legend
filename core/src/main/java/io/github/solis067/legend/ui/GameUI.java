package io.github.solis067.legend.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.solis067.legend.Player;
import io.github.solis067.legend.Slime;

/**
 * Manages the game UI using Scene2D and custom health bars
 */
public class GameUI {
    private Stage stage;
    private BitmapFont font;
    private Label fpsLabel;
    private Player player;
    private Slime slime;
    
    // Health bar textures and properties
    private Texture healthBarTexture;
    private Texture redPixelTexture;
    private SpriteBatch batch;
    private static final int PLAYER_MAX_HEALTH = 10;
    private static final int SLIME_MAX_HEALTH = 20;
    private float healthBarWidth = 260f;
    private float healthBarHeight = 60f;
    private float playerHealthX;
    private float playerHealthY;
    private float slimeHealthX;
    private float slimeHealthY;

    public GameUI(Player player, Slime slime) {
        this.player = player;
        this.slime = slime;

        // Create stage with screen viewport for UI
        stage = new Stage(new ScreenViewport());
        batch = new SpriteBatch();

        // Load health bar texture
        healthBarTexture = new Texture(Gdx.files.internal("Hud_Ui/health_bar_hud.png"));

        // Create a 1x1 red pixel texture for health fill
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.RED);
        pixmap.fill();
        redPixelTexture = new Texture(pixmap);
        pixmap.dispose();

        // Position health bar centered at the bottom of the screen
        computeHealthBarPosition(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Create font for labels
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);

        // Create a table for FPS counter
        Table uiTable = new Table();
        uiTable.setFillParent(true);
        uiTable.top().left();
        uiTable.pad(10);

        // Create FPS label
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        fpsLabel = new Label("FPS: 0", labelStyle);
        fpsLabel.setColor(Color.YELLOW);

        // Add label to table
        uiTable.add(fpsLabel).row();

        stage.addActor(uiTable);
    }

    public void update(float delta) {
        // Update FPS label
        fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());

        // Update stage
        stage.act(delta);
    }

    public void render() {
        // Draw health bars with custom rendering
        drawHealthBars();
        
        // Draw the stage (FPS label)
        stage.draw();
    }

    private void drawHealthBars() {
        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();

        // Draw player health bar
        drawHealthBar(playerHealthX, playerHealthY, player.getHealth(), PLAYER_MAX_HEALTH);

        // Draw player health text just above the bar
        font.setColor(Color.WHITE);
        float textY = playerHealthY + healthBarHeight + font.getLineHeight();
        font.draw(batch, "HP: " + player.getHealth() + " / " + PLAYER_MAX_HEALTH, playerHealthX, textY);

        // Check if slime is dead
        if (slime.getHealth() <= 0) {
            // Draw "You Win!" message instead of health bar
            font.getData().setScale(2.5f);
            font.setColor(Color.YELLOW);
            String winMessage = "You Win!";
            com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, winMessage);
            float centerX = (Gdx.graphics.getWidth() - layout.width) / 2f;
            float centerY = Gdx.graphics.getHeight() - 30f;
            font.draw(batch, winMessage, centerX, centerY);
            font.getData().setScale(1.5f);
        } else {
            // Draw slime health bar at the top (only red bar, no frame)
            drawSlimeHealthBar(slimeHealthX, slimeHealthY, slime.getHealth(), SLIME_MAX_HEALTH);

            // Draw slime health text below the bar
            font.setColor(Color.GREEN);
            float slimeTextY = slimeHealthY - 5f;
            font.draw(batch, "Slime HP: " + slime.getHealth() + " / " + SLIME_MAX_HEALTH, slimeHealthX, slimeTextY);
        }

        batch.end();
    }

    private void drawHealthBar(float x, float y, int currentHealth, int maxHealth) {
        batch.setColor(Color.WHITE);

        // Draw red fill based on health percentage (bar is half the frame height)
        float healthPercentage = (float) currentHealth / maxHealth;
        float fillWidth = (healthBarWidth * 0.64f) * healthPercentage; // width scaled down to 60%
        float fillHeight = healthBarHeight * 0.6f; // slightly taller fill
        float offsetY = (healthBarHeight - fillHeight) / 2f;
        float offsetX = 66f; // nudge fill slightly further to the right

        // Draw red rectangle for health
        batch.setColor(Color.RED);
        // Use a simple 1x1 white pixel texture to draw the fill
        drawRedFill(x + offsetX, y + offsetY, fillWidth, fillHeight);
        batch.setColor(Color.WHITE);

        // Draw health bar frame
        batch.draw(healthBarTexture, x, y, healthBarWidth, healthBarHeight);
    }

    private void drawRedFill(float x, float y, float width, float height) {
        // Draw red rectangle for health fill
        if (width > 0 && height > 0) {
            batch.draw(redPixelTexture, x + 2, y + 2, width - 4, height - 4);
        }
    }

    private void drawSlimeHealthBar(float x, float y, int currentHealth, int maxHealth) {
        // Draw only the red fill bar for slime health (no frame)
        float healthPercentage = (float) currentHealth / maxHealth;
        float barWidth = healthBarWidth * 0.64f; // 2x longer than before
        float barHeight = healthBarHeight * 0.4f; // slightly bigger height
        float fillWidth = barWidth * healthPercentage;
        
        // Draw the red health bar
        batch.setColor(Color.RED);
        if (fillWidth > 0 && barHeight > 0) {
            batch.draw(redPixelTexture, x, y, fillWidth, barHeight);
        }
        batch.setColor(Color.WHITE);
    }

    public Stage getStage() {
        return stage;
    }

    public void dispose() {
        stage.dispose();
        font.dispose();
        batch.dispose();
        healthBarTexture.dispose();
        redPixelTexture.dispose();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        computeHealthBarPosition(width, height);
    }

    private void computeHealthBarPosition(int screenWidth, int screenHeight) {
        playerHealthX = 10f; // bottom-left padding
        playerHealthY = 10f; // bottom padding
        
        // Position slime health bar at top center (using updated dimensions)
        float slimeBarWidth = healthBarWidth * 0.64f;
        slimeHealthX = (screenWidth - slimeBarWidth) / 2f - 80f;
        slimeHealthY = screenHeight - (healthBarHeight * 0.4f) - 10f; // top padding
    }
}
