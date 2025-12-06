package io.github.solis067.legend;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;


public class Player extends Entity {
    Texture[] idleTextures;
    Texture[] runTextures;
    Texture[] attackTextures;
    Texture[] hitTextures;
    Animation<TextureRegion>[] idleAnimations;
    Animation<TextureRegion>[] runAnimations;
    Animation<TextureRegion>[] attackAnimations;
    Animation<TextureRegion>[] hitAnimations;

    private final float playerWidth = ENTITY_WIDTH;
    private final float playerHeight = ENTITY_HEIGHT;

    private enum Direction { DOWN, LEFT, RIGHT, UP }
    private Direction currentDirection; // 0: down, 1: left, 2: right, 3: up
    
    boolean isAttacking = false;
    float attackTime = 0f;

    private final float PLAYER_SPEED = 5.0f;
    private final float PLAYER_ANIMATION_SPEED = 1.5f; // lower is faster
    
    public Player(World world, String id, int x , int y) {
        pos = new Vector2(x, y);
        vel = new Vector2(0, 0);
        this.id = id;
        health = 20;

        createBody(world, x, y);
        currentDirection = Direction.DOWN;
        setupAnimations();        
    }

    @SuppressWarnings("unchecked")
    private void setupAnimations() {
        // Setup the animations for each direction
        int dirCount = Direction.values().length;
        idleTextures = new Texture[dirCount];
        runTextures = new Texture[dirCount];
        attackTextures = new Texture[dirCount];
        hitTextures = new Texture[dirCount];

        idleAnimations = new Animation[dirCount];
        runAnimations = new Animation[dirCount];
        attackAnimations = new Animation[dirCount];
        hitAnimations = new Animation[dirCount];

        String[] dirNames = new String[] {"down", "left", "right", "up"};
        for (int i = 0; i < dirCount; i++) {
            idleTextures[i] = new Texture(Gdx.files.internal("Char_Sprites/char_idle_" + dirNames[i] + "_anim_strip_6.png"));
            idleAnimations[i] = makeAnimation(idleTextures[i], 6, 1, PLAYER_ANIMATION_SPEED);

            runTextures[i] = new Texture(Gdx.files.internal("Char_Sprites/char_run_" + dirNames[i] + "_anim_strip_6.png"));
            runAnimations[i] = makeAnimation(runTextures[i], 6, 1, PLAYER_ANIMATION_SPEED);

            attackTextures[i] = new Texture(Gdx.files.internal("Char_Sprites/char_attack_" + dirNames[i] + "_anim_strip_6.png"));
            attackAnimations[i] = makeAnimation(attackTextures[i], 6, 1, PLAYER_ANIMATION_SPEED / 1.5f);

            hitTextures[i] = new Texture(Gdx.files.internal("Char_Sprites/char_hit_" + dirNames[i] + "_anim_strip_3.png"));
            hitAnimations[i] = makeAnimation(hitTextures[i], 3, 1, PLAYER_ANIMATION_SPEED / 2f);
        }

        currentFrame = idleAnimations[currentDirection.ordinal()].getKeyFrame(0);
    }

    public void input() {
        vel.x = 0;
        vel.y = 0;
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z) && !isAttacking) {
            isAttacking = true;
            attackTime = 0f;
            vel.x = 0;
            vel.y = 0;
            return;
        }

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

        // If attacking
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
        }

        body.setLinearVelocity(vel.x, vel.y);
        pos.x = body.getPosition().x - playerWidth / 2f;
        pos.y = body.getPosition().y - (playerHeight / 2f) + 0.25f; // slight offset for better ground alignment

        if (isAttacking) {
            return;
        }

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
        float drawWidth = playerWidth;
        float drawHeight = playerHeight;
        float offsetX = 0f;
        float offsetY = 0f;

        if (isAttacking) {
            drawHeight *= 2f;
            drawWidth *= 2f;
            switch (currentDirection) {
                case DOWN:
                    offsetX = -1f;
                    offsetY = -1f;
                    drawWidth *= 1.5f;
                    break;
                case UP:
                    offsetX = -1f;
                    drawWidth *= 1.5f;
                    break;
                case LEFT:
                    offsetX = -1f;
                    offsetY = -1f;
                    drawHeight *= 1.5f;
                    break;
                case RIGHT:
                    offsetY = -1f;
                    drawHeight *= 1.5f;
                    break;
            }
        }

        game.batch.draw(currentFrame, pos.x + offsetX, pos.y + offsetY, drawWidth, drawHeight);
    }

    public float getWidth() {
        return playerWidth;
    }

    public float getHeight() {
        return playerHeight;
    }

    public void dispose() {
        disposeTextures(idleTextures);
        disposeTextures(runTextures);
        disposeTextures(attackTextures);
        disposeTextures(hitTextures);

        if (body != null && body.getWorld() != null) {
            body.getWorld().destroyBody(body);
            body = null;
        }
    } 

    private void createBody(World world, int x, int y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.fixedRotation = true;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x + ENTITY_WIDTH / 2f, y + ENTITY_HEIGHT / 2f);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(ENTITY_WIDTH / 2.8f, ENTITY_HEIGHT / 4f);

        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.density = 1.0f;
        fixture.friction = 0.2f;
        fixture.restitution = 0f;

        this.body = world.createBody(bodyDef);
        this.body.setLinearDamping(0.25f);
        
        this.body.createFixture(fixture).setUserData(this);

        shape.dispose();
    }

    public void takeDamage(int damage) {
        health -= damage;
        damageTimer = 0f; // reset damage timer to start hit animation from beginning

        if (health > 0) {
            Gdx.app.log("Player", "Player took " + damage + " damage. Health: " + health);
        }

        if (health <= 0) {
            Gdx.app.log("Player", "Player died!");
        }
    }
}