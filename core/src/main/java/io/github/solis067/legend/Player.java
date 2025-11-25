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
    
    Texture[] idleTextures;
    Texture[] runTextures;
    Texture[] attackTextures;
    Animation<TextureRegion>[] idleAnimations;
    Animation<TextureRegion>[] runAnimations;
    Animation<TextureRegion>[] attackAnimations;
    TextureRegion currentFrame;

    private final float textureWidth = Main.TILE_PIXELS;
    private final float textureHeight = Main.TILE_PIXELS;
    private final float playerWidth = textureWidth * Main.UNIT_SCALE;
    private final float playerHeight = textureHeight * Main.UNIT_SCALE;


    private enum Direction { DOWN, LEFT, RIGHT, UP }
    private Direction currentDirection; // 0: down, 1: left, 2: right, 3: up
    boolean isAttacking = false;
    float attackTime = 0f;

    private final float PLAYER_SPEED = 5.0f;
    private final float PLAYER_ANIMATION_SPEED = 1.5f; // lower is faster

    private final int FRAME_COLS = 6;
    private final int FRAME_ROWS = 1;

    float stateTime;

    public Player(int x , int y) {
        pos = new Vector2(x, y);
        vel = new Vector2(0, 0);
        currentDirection = Direction.DOWN;
        stateTime = 0f;
        setupAnimations();        
    }

    private Animation<TextureRegion> makeAnimation(Texture texture, float speed) {
        // determine per-texture frame dimensions so different-sized source images split correctly
        int frameWidth = texture.getWidth() / FRAME_COLS;
        int frameHeight = texture.getHeight() / FRAME_ROWS;

        TextureRegion[][] tmpFrames = TextureRegion.split(texture, frameWidth, frameHeight);
        int cols = tmpFrames[0].length;
        TextureRegion[] animationFrames = new TextureRegion[cols];
        for (int i = 0; i < cols; i++) {
            animationFrames[i] = tmpFrames[0][i];
        }

        return new Animation<TextureRegion>(0.1f * speed, animationFrames);
    }

    @SuppressWarnings("unchecked")
    private void setupAnimations() {
        // Setup the animations for each direction
        int dirCount = Direction.values().length;
        idleTextures = new Texture[dirCount];
        runTextures = new Texture[dirCount];
        attackTextures = new Texture[dirCount];

        idleAnimations = new Animation[dirCount];
        runAnimations = new Animation[dirCount];
        attackAnimations = new Animation[dirCount];

        String[] dirNames = new String[] {"down", "left", "right", "up"};
        for (int i = 0; i < dirCount; i++) {
            idleTextures[i] = new Texture(Gdx.files.internal("Char_Sprites/char_idle_" + dirNames[i] + "_anim_strip_6.png"));
            idleAnimations[i] = makeAnimation(idleTextures[i], PLAYER_ANIMATION_SPEED);

            runTextures[i] = new Texture(Gdx.files.internal("Char_Sprites/char_run_" + dirNames[i] + "_anim_strip_6.png"));
            runAnimations[i] = makeAnimation(runTextures[i], PLAYER_ANIMATION_SPEED);

            attackTextures[i] = new Texture(Gdx.files.internal("Char_Sprites/char_attack_" + dirNames[i] + "_anim_strip_6.png"));
            attackAnimations[i] = makeAnimation(attackTextures[i], PLAYER_ANIMATION_SPEED / 1.5f);
        }

        currentFrame = idleAnimations[currentDirection.ordinal()].getKeyFrame(0);
    }

    public void input() {
        vel.x = 0;
        vel.y = 0;
        // If attack just started, lock movement
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z) && !isAttacking) {
            isAttacking = true;
            attackTime = 0f;
            // stop movement immediately
            vel.x = 0;
            vel.y = 0;
            return;
        }

        // While attacking, ignore movement input
        if (isAttacking) {
            return;
        }

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

        // If attacking, advance attack timer and play attack animation. Player can't move while attacking.
        if (isAttacking) {
            attackTime += delta;
            Animation<TextureRegion> atk = attackAnimations[currentDirection.ordinal()];
            currentFrame = atk.getKeyFrame(attackTime, false);
            // When the attack animation finishes, stop attacking and reset timers so normal animations resume cleanly
            if (atk.isAnimationFinished(attackTime)) {
                isAttacking = false;
                attackTime = 0f;
                stateTime = 0f;
            }
            return;
        }

        // Movement and other animations when not attacking
        pos.x += vel.x * delta;
        pos.y += vel.y * delta;

        if (vel.x > 0) {
            currentFrame = runAnimations[Direction.RIGHT.ordinal()].getKeyFrame(stateTime, true);
            currentDirection = Direction.RIGHT;
        }
        else if (vel.x < 0) {
            currentFrame = runAnimations[Direction.LEFT.ordinal()].getKeyFrame(stateTime, true);
            currentDirection = Direction.LEFT;
        }
        else if (vel.y > 0) {
            currentFrame = runAnimations[Direction.UP.ordinal()].getKeyFrame(stateTime, true);
            currentDirection = Direction.UP;
        }
        else if (vel.y < 0) {
            currentFrame = runAnimations[Direction.DOWN.ordinal()].getKeyFrame(stateTime, true);
            currentDirection = Direction.DOWN;
        }
        else {
            currentFrame = idleAnimations[currentDirection.ordinal()].getKeyFrame(stateTime, true);
        }
    }

    public void draw(Main game) {
        float drawWidth;
        float drawHeight;
        if (isAttacking) {
            drawWidth = playerWidth * 2;
            drawHeight = playerHeight * 2;
        }
        else {
            drawWidth = playerWidth;
            drawHeight = playerHeight;
        }

        game.batch.draw(currentFrame, pos.x, pos.y, drawWidth, drawHeight);
    }

    public float getWidth() {
        return playerWidth;
    }

    public float getHeight() {
        return playerHeight;
    }

    public void dispose() {
        if (idleTextures != null) {
            for (Texture t : idleTextures) {
                if (t != null) t.dispose();
            }
        }
        if (runTextures != null) {
            for (Texture t : runTextures) {
                if (t != null) t.dispose();
            }
        }
        if (attackTextures != null) {
            for (Texture t : attackTextures) {
                if (t != null) t.dispose();
            }
        }
    }
}