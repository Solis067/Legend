package io.github.solis067.legend;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Entity {
    Vector2 pos;
    Vector2 vel;
    TextureRegion currentFrame;
    Body body;

    float stateTime;

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
}
