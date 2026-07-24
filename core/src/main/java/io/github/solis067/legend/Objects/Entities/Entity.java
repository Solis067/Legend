package io.github.solis067.legend.Objects.Entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import io.github.solis067.legend.Main;

public class Entity {
    protected Vector2 pos;
    protected Vector2 vel;
    protected TextureRegion currentFrame;

    public Body body;
    public String id;

    protected int health;

    protected float stateTime = 0f;
    protected float damageTimer = 0f;

    final float ENTITY_WIDTH = Main.TILE_PIXELS * Main.UNIT_SCALE;
    final float ENTITY_HEIGHT = Main.TILE_PIXELS * Main.UNIT_SCALE;

    protected Animation<TextureRegion> makeAnimation(Texture texture, int cols, int rows, float speed) {
        // determine per-texture frame dimensions so different-sized source images split correctly
        int frameWidth = texture.getWidth() / cols;
        int frameHeight = texture.getHeight() / rows;

        TextureRegion[][] tmpFrames = TextureRegion.split(texture, frameWidth, frameHeight);
        TextureRegion[] animationFrames = new TextureRegion[cols];

        for (int i = 0; i < cols; i++) {
            animationFrames[i] = tmpFrames[0][i];
        }

        return new Animation<TextureRegion>(0.1f * speed, animationFrames);
    }

    protected void disposeTextures(Texture[] textures) {
        if (textures != null) {
            for (Texture t : textures) {
                if (t != null) t.dispose();
            }
        }
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public float getPosX() {
        return pos.x;
    }
    public float getPosY() {
        return pos.y;
    }
}
