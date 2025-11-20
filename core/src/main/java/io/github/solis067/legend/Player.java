package io.github.solis067.legend;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;


public class Player {
    Vector2 pos;
    Vector2 vel;
    
    Texture idleDownTexture;
    Texture idleLeftTexture;
    Texture idleRightTexture;
    Texture idleUpTexture;
    Texture runRightTexture;
    Texture runLeftTexture;
    Texture runUpTexture;
    Texture runDownTexture;
    Animation<TextureRegion> idleDownAnimation;
    Animation<TextureRegion> idleLeftAnimation;
    Animation<TextureRegion> idleRightAnimation;
    Animation<TextureRegion> idleUpAnimation;
    Animation<TextureRegion> runRightAnimation;
    Animation<TextureRegion> runLeftAnimation;
    Animation<TextureRegion> runUpAnimation;
    Animation<TextureRegion> runDownAnimation;
    TextureRegion currentFrame;

    private final float textureWidth = Main.TILE_PIXELS;
    private final float textureHeight = Main.TILE_PIXELS;
    private final float playerWidth = textureWidth * Main.UNIT_SCALE;
    private final float playerHeight = textureHeight * Main.UNIT_SCALE;


    private enum Direction { DOWN, LEFT, RIGHT, UP }
    private Direction currentDirection; // 0: down, 1: left, 2: right, 3: up

    private final float PLAYER_SPEED = 5.0f;
    private final float PLAYER_ANIMATION_SPEED = 1.5f;

    private final int FRAME_COLS = 6;

    float stateTime;

    public Player(int x , int y) {
        pos = new Vector2(x, y);
        vel = new Vector2(0, 0);
        currentDirection = Direction.DOWN;
        stateTime = 0f;
        setupAnimations();        
    }

    private Animation<TextureRegion> makeAnimation(Texture texture) {
        TextureRegion[][] tmpFrames = TextureRegion.split(texture, (int)textureWidth, (int)textureHeight);
        TextureRegion[] animationFrames = new TextureRegion[FRAME_COLS];
        for (int i = 0; i < FRAME_COLS; i++) {
            animationFrames[i] = tmpFrames[0][i];
        }

        return new Animation<TextureRegion>(0.1f * PLAYER_ANIMATION_SPEED, animationFrames);
    }

    private void setupAnimations() {

        idleDownTexture = new Texture(Gdx.files.internal("Char_Sprites/char_idle_down_anim_strip_6.png"));
        idleDownAnimation = makeAnimation(idleDownTexture);

        idleLeftTexture = new Texture(Gdx.files.internal("Char_Sprites/char_idle_left_anim_strip_6.png"));
        idleLeftAnimation = makeAnimation(idleLeftTexture);

        idleRightTexture = new Texture(Gdx.files.internal("Char_Sprites/char_idle_right_anim_strip_6.png"));
        idleRightAnimation = makeAnimation(idleRightTexture);

        idleUpTexture = new Texture(Gdx.files.internal("Char_Sprites/char_idle_up_anim_strip_6.png"));
        idleUpAnimation = makeAnimation(idleUpTexture);

        runRightTexture = new Texture(Gdx.files.internal("Char_Sprites/char_run_right_anim_strip_6.png"));
        runRightAnimation = makeAnimation(runRightTexture);

        runLeftTexture = new Texture(Gdx.files.internal("Char_Sprites/char_run_left_anim_strip_6.png"));
        runLeftAnimation = makeAnimation(runLeftTexture);

        runUpTexture = new Texture(Gdx.files.internal("Char_Sprites/char_run_up_anim_strip_6.png"));
        runUpAnimation = makeAnimation(runUpTexture);

        runDownTexture = new Texture(Gdx.files.internal("Char_Sprites/char_run_down_anim_strip_6.png"));
        runDownAnimation = makeAnimation(runDownTexture);

        currentFrame = idleDownAnimation.getKeyFrame(0);
    }

    public void input() {
        vel.x = 0;
        vel.y = 0;

        // Left and right input
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            vel.x = PLAYER_SPEED;
        } 
        else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            vel.x = -PLAYER_SPEED;
        }
        // Up and down input
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            vel.y = PLAYER_SPEED;
        } 
        else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            vel.y = -PLAYER_SPEED;
        }
    }

    public void update(float delta) {
        stateTime += delta;

        pos.x += vel.x * delta;
        pos.y += vel.y * delta;

        if (vel.x > 0) {
            currentFrame = runRightAnimation.getKeyFrame(stateTime, true);
            currentDirection = Direction.RIGHT;
        }
        else if (vel.x < 0) {
            currentFrame = runLeftAnimation.getKeyFrame(stateTime, true);
            currentDirection = Direction.LEFT;
        }
        else if (vel.y > 0) {
            currentFrame = runUpAnimation.getKeyFrame(stateTime, true);
            currentDirection = Direction.UP;
        }
        else if (vel.y < 0) {
            currentFrame = runDownAnimation.getKeyFrame(stateTime, true);
            currentDirection = Direction.DOWN;
        }
        else {
            switch (currentDirection) {
                case DOWN:
                    currentFrame = idleDownAnimation.getKeyFrame(stateTime, true);
                    break;
                case LEFT:
                    currentFrame = idleLeftAnimation.getKeyFrame(stateTime, true);
                    break;
                case RIGHT:
                    currentFrame = idleRightAnimation.getKeyFrame(stateTime, true);
                    break;
                case UP:
                    currentFrame = idleUpAnimation.getKeyFrame(stateTime, true);
                    break;
            }
        }
    }

    public void draw(Main game) {
        game.batch.draw(currentFrame, pos.x, pos.y, playerWidth, playerHeight);
    }

    public float getWidth() {
        return playerWidth;
    }

    public float getHeight() {
        return playerHeight;
    }

    public void dispose() {
        idleDownTexture.dispose();
        idleLeftTexture.dispose();
        idleRightTexture.dispose();
        idleUpTexture.dispose();
        runRightTexture.dispose();
        runLeftTexture.dispose();
        runUpTexture.dispose();
        runDownTexture.dispose();
    }
}