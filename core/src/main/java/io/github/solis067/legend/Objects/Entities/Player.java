package io.github.solis067.legend.Objects.Entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.audio.Sound;
import io.github.solis067.legend.Objects.Tools.Sword;


public class Player extends Entity {
    Animation<TextureRegion>[] idleAnimations;
    Animation<TextureRegion>[] runAnimations;
    Animation<TextureRegion>[] attackAnimations;
    Animation<TextureRegion>[] hitAnimations;
    Sound attackSound;
    Sound hitSound;
    Sound grassRunSound;

    Slime slimeInRange = null;

    private final float playerWidth = ENTITY_WIDTH;
    private final float playerHeight = ENTITY_HEIGHT;

    private enum Direction { DOWN, LEFT, RIGHT, UP }
    private Direction currentDirection; // 0: down, 1: left, 2: right, 3: up

    private boolean isAttacking = false;
    private boolean isTakingDamage = false;
    private boolean isKnockedBack = false;
    private boolean slimeIsInRange = false;
    private boolean dead = false;

    private float attackTime = 0f;
    private float damageTimer = 0f;
    private float knockbackTimer = 0f;
    private float runStepTimer = 0f;

    private final float PLAYER_SPEED = 5.0f;
    private static final float RUN_STEP_INTERVAL = 0.4f; // seconds between footstep sounds

    // Sword with hitbox sensor
    private Sword sword;

    public Player(World world, String id, int x , int y) {
        pos = new Vector2(x, y);
        vel = new Vector2(0, 0);
        this.id = id;
        health = 10;
        currentDirection = Direction.DOWN;

        attackSound = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/attack.mp3"));
        hitSound = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/hit.mp3"));
        grassRunSound = Gdx.audio.newSound(Gdx.files.internal("Audio/Sounds/run_grass.mp3"));

        createBody(world, x, y);
        sword = new Sword(this, body);
        this.atlas = new TextureAtlas(Gdx.files.internal("Character/Atlas/player.atlas"));
        setupAnimations();

    }

    @SuppressWarnings("unchecked")
    private void setupAnimations() {
        // Set up the animations for each direction
        int dirCount = Direction.values().length;
        idleAnimations = new Animation[dirCount];
        runAnimations = new Animation[dirCount];
        attackAnimations = new Animation[dirCount];
        hitAnimations = new Animation[dirCount];

        String[] dirNames = new String[] {"down", "left", "right", "up"};
        for (int i = 0; i < dirCount; i++) {
            // lower is faster
            float PLAYER_ANIMATION_SPEED = 1.5f;
            idleAnimations[i] = new Animation<>(0.1f * PLAYER_ANIMATION_SPEED, atlas.findRegions("idle_" + dirNames[i]));
            runAnimations[i] = new Animation<>(0.1f * PLAYER_ANIMATION_SPEED, atlas.findRegions("run_" + dirNames[i]));
            attackAnimations[i] = new Animation<>(0.1f * PLAYER_ANIMATION_SPEED / 1.5f, atlas.findRegions("attack_" + dirNames[i]));
            hitAnimations[i] = new Animation<>(0.1f * PLAYER_ANIMATION_SPEED / 2f, atlas.findRegions("hit_" + dirNames[i]));
        }
        currentFrame = idleAnimations[currentDirection.ordinal()].getKeyFrame(0);
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
        this.body.setLinearDamping(5.0f); // Higher damping for smoother knockback deceleration

        this.body.createFixture(fixture).setUserData(this);

        shape.dispose();
    }

    public void resetVelocity() {
        vel.x = 0;
        vel.y = 0;
    }

    public void attack(){
        if (isAttacking) return;
        isAttacking = true;
        attackTime = 0f;
        vel.x = 0;
        vel.y = 0;

        if (slimeIsInRange) {
            if (slimeInRange != null) {
                // Calculate knockback direction from player to slime
                Vector2 knockbackDir = new Vector2(
                    slimeInRange.body.getPosition().x - body.getPosition().x,
                    slimeInRange.body.getPosition().y - body.getPosition().y
                );
                slimeInRange.takeDamage(5, knockbackDir);
            }
        }
        if (attackSound != null) attackSound.play();
    }

    public void moveRight(){
        if (isAttacking || isTakingDamage) return;
        vel.x = PLAYER_SPEED;
    }
    public void moveLeft() {
        if (isAttacking || isTakingDamage) return;
        vel.x = -PLAYER_SPEED;
    }
    public void moveUp() {
        if (isAttacking || isTakingDamage) return;
        vel.y = PLAYER_SPEED;
    }
    public void moveDown() {
        if (isAttacking || isTakingDamage) return;
        vel.y = -PLAYER_SPEED;
    }

    @Override
    public void update(float delta) {
        stateTime += delta;

        if (dead) {
            body.setLinearVelocity(0f, 0f);
            pos.x = body.getPosition().x - playerWidth / 2f;
            pos.y = body.getPosition().y - (playerHeight / 2f) + 0.25f;
            return;
        }

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

        if (isTakingDamage) {
            damageTimer += delta;
            currentFrame = hitAnimations[currentDirection.ordinal()].getKeyFrame(damageTimer, false);
            // When the hit animation finishes, stop taking damage and reset timers so normal animations resume cleanly
            if (hitAnimations[currentDirection.ordinal()].isAnimationFinished(damageTimer)) {
                isTakingDamage = false;
                damageTimer = 0f;
                stateTime = 0f;
            }
        }

        // Update knockback state
        if (isKnockedBack) {
            knockbackTimer += delta;
            float KNOCKBACK_DURATION = 0.2f;
            if (knockbackTimer >= KNOCKBACK_DURATION) {
                isKnockedBack = false;
                knockbackTimer = 0f;
            }
        }

        // Only set velocity if not being knocked back
        if (!isKnockedBack) {
            body.setLinearVelocity(vel.x, vel.y);
        }
        pos.x = body.getPosition().x - playerWidth / 2f;
        pos.y = body.getPosition().y - (playerHeight / 2f) + 0.25f; // slight offset for better ground alignment

        if (isAttacking || isTakingDamage) {
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

        // Update sword hitbox position based on facing direction
        if (sword != null) {
            sword.setDirection(currentDirection.ordinal());
            sword.update();
        }

        handleRunSound(delta);
    }

    private void handleRunSound(float delta) {
        boolean moving = vel.x != 0 || vel.y != 0;
        if (moving) {
            runStepTimer += delta;
            if (runStepTimer >= RUN_STEP_INTERVAL) {
                if (grassRunSound != null) {
                    grassRunSound.play();
                }
                runStepTimer = 0f;
            }
        } else {
            runStepTimer = 0f;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        float drawWidth = playerWidth;
        float drawHeight = playerHeight;
        float offsetX = 0f;
        float offsetY = 0f;

        if (isAttacking && !isTakingDamage) {
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

        batch.draw(currentFrame, pos.x + offsetX, pos.y + offsetY, drawWidth, drawHeight);
    }

    public void takeDamage(int damage, Vector2 knockbackDirection) {
        if (dead) {
            return;
        }

        damageTimer = 0f; // reset damage timer to start hit animation from beginning
        isTakingDamage = true;
        health = Math.max(health - damage, 0);
        if (hitSound != null) hitSound.play();

        if (health <= 0) {
            dead = true;
            vel.setZero();
        }

        // Apply smooth knockback
        if (knockbackDirection != null && body != null && !dead) {
            isKnockedBack = true;
            knockbackTimer = 0f;
            float knockbackSpeed = 8f; // Smooth velocity-based knockback
            Vector2 knockback = knockbackDirection.cpy().nor().scl(knockbackSpeed);
            body.setLinearVelocity(knockback);
        }
    }

    public boolean isDead() {
        return dead;
    }

    public float getWidth() {
        return playerWidth;
    }

    public float getHeight() {
        return playerHeight;
    }

    public void setSlimeinRange(boolean inRange, Slime slime) {
        slimeInRange = slime;
        slimeIsInRange = inRange;
    }

    public void refillHealth() {
        health = 10;
    }

    public void dispose() {
        atlas.dispose();
        attackSound.dispose();
        hitSound.dispose();
        grassRunSound.dispose();

        if (sword != null) {
            sword.dispose();
            sword = null;
        }
        if (body != null && body.getWorld() != null) {
            body.getWorld().destroyBody(body);
            body = null;
        }
    }
}
