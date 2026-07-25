package io.github.solis067.legend.Objects.Entities;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.Fixture;
import io.github.solis067.legend.Main;

public class Slime extends Entity {

    Animation<TextureRegion> idleAnimation;
    Animation<TextureRegion> hitAnimation;
    Animation<TextureRegion> runAnimation;
    Animation<TextureRegion> deathAnimation;

    Fixture slimeFixture;

    private final Player player;

    boolean isTakingDamage = false;
    boolean isKnockedBack = false;
    boolean isDying = false;
    boolean isDead = false;
    float damageTimer = 0f;
    float knockbackTimer = 0f;
    float KNOCKBACK_DURATION = 0.2f;

    final float ANIMATION_SPEED = 1.5f;
    final float MOVEMENT_SPEED = 3f;
    final float STARTUP_DELAY = 5f;

    float startupCountdown = STARTUP_DELAY;

    private int spawnX;
    private int spawnY;
    private World world;

    public Slime(World world, String id, int x , int y, Player player) {
        this.pos = new Vector2(x, y);
        this.vel = new Vector2(0, 0);
        this.id = id;
        this.player = player;
        this.world = world;
        this.spawnX = x;
        this.spawnY = y;
        this.health = 50;
        this.atlas = new TextureAtlas(Gdx.files.internal("Enemies/Pinkslime/Atlas/slime.atlas"));

        createBody(world, x, y);
        setupAnimations();
    }

    private void setupAnimations() {
        idleAnimation = new Animation<>(0.1f * ANIMATION_SPEED, atlas.findRegions("idle"));
        hitAnimation = new Animation<>(0.1f * ANIMATION_SPEED / 1.5f, atlas.findRegions("hit"));
        runAnimation = new Animation<>(0.1f * ANIMATION_SPEED, atlas.findRegions("run"));
        deathAnimation = new Animation<>(0.1f * ANIMATION_SPEED / 2f, atlas.findRegions("death"));
        currentFrame = idleAnimation.getKeyFrame(0);
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
        fixture.restitution = 0.0f; // No bounce for smoother knockback

        this.body = world.createBody(bodyDef);
        this.body.setLinearDamping(5.0f); // Higher damping for smoother deceleration
        this.body.createFixture(fixture).setUserData(this);

        shape.dispose();
    }

    @Override
    public void update(float delta) {
        if (isDead) return; // Don't update if already dead

        stateTime += delta;

        // Decrement startup countdown
        if (startupCountdown > 0) {
            startupCountdown -= delta;
        }

        // Handle death animation
        if (isDying) {
            damageTimer += delta;
            currentFrame = deathAnimation.getKeyFrame(damageTimer, false);

            // When death animation finishes, mark as dead
            if (deathAnimation.isAnimationFinished(damageTimer)) {
                isDead = true;
                // Destroy physics body
                if (body != null && body.getWorld() != null) {
                    body.getWorld().destroyBody(body);
                    body = null;
                }
            }
            return;
        }

        if (isTakingDamage) {
            damageTimer += delta;
            currentFrame = hitAnimation.getKeyFrame(damageTimer, false);
            // When the hit animation finishes, stop taking damage and reset timers so normal animations resume cleanly
            if (hitAnimation.isAnimationFinished(damageTimer)) {
                isTakingDamage = false;
                damageTimer = 0f;
                stateTime = 0f;
            }
        }

        // Update knockback state
        if (isKnockedBack) {
            knockbackTimer += delta;
            if (knockbackTimer >= KNOCKBACK_DURATION) {
                isKnockedBack = false;
                knockbackTimer = 0f;
            }
        }

        // Only set velocity if not being knocked back and not dying and startup delay is over
        if (!isKnockedBack && !isDying && startupCountdown <= 0) {
            // Move towards player
            if (player != null && player.body != null) {
                Vector2 playerPos = player.body.getPosition();
                Vector2 slimePos = body.getPosition();

                // Calculate direction to player
                Vector2 direction = playerPos.cpy().sub(slimePos).nor();

                // Set velocity towards player
                vel = direction.scl(MOVEMENT_SPEED);
            }
            body.setLinearVelocity(vel.x, vel.y);
        }
        pos.x = body.getPosition().x - ENTITY_WIDTH / 2f;
        pos.y = body.getPosition().y - (ENTITY_HEIGHT / 2f) + 0.15f;

        if (isTakingDamage) {
            return; // skip normal animation updates while taking damage
        }

        // Use run animation when moving, idle when stationary
        if (vel.len() > 0.1f) {
            currentFrame = runAnimation.getKeyFrame(stateTime, true);
        } else {
            currentFrame = idleAnimation.getKeyFrame(stateTime, true);
        }
    }

    @Override
    public void draw(Main game) {
        // Don't draw if dead
        if (isDead) return;

        // Drawing logic goes here
        game.batch.draw(currentFrame, pos.x, pos.y, ENTITY_WIDTH, ENTITY_HEIGHT);
    }

    public void takeDamage(int damage, Vector2 knockbackDirection) {
        if (isDead || isDying || startupCountdown > 0) return;

        health = Math.max(health - damage, 0);

        if (health == 0) {
            // Start death animation
            isDying = true;
            damageTimer = 0f;
            stateTime = 0f;
            isTakingDamage = false;
            return; // Don't play hit animation or knockback when dying
        }

        damageTimer = 0f;
        isTakingDamage = true;

        // Apply smooth knockback
        if (knockbackDirection != null && body != null) {
            isKnockedBack = true;
            knockbackTimer = 0f;
            float knockbackSpeed = 8f; // Smooth velocity-based knockback
            Vector2 knockback = knockbackDirection.cpy().nor().scl(knockbackSpeed);
            body.setLinearVelocity(knockback);
        }
    }

    // Overload for backward compatibility (if needed elsewhere)
    public void takeDamage(int damage) {
        takeDamage(damage, null);
    }

    public boolean isDead() {
        return isDead;
    }

    public boolean isDying() {
        return isDying;
    }

    public float getStartupCountdown() {
        return startupCountdown;
    }

    public void respawn() {
        // Reset state
        health = 50;
        isDead = false;
        isDying = false;
        isTakingDamage = false;
        isKnockedBack = false;
        damageTimer = 0f;
        knockbackTimer = 0f;
        startupCountdown = STARTUP_DELAY;
        stateTime = 0f;
        vel = new Vector2(0, 0);

        // Recreate body at spawn location
        if (body != null && body.getWorld() != null) {
            body.getWorld().destroyBody(body);
        }
        createBody(world, spawnX, spawnY);
        pos = new Vector2(spawnX, spawnY);
    }

    public void dispose() {
        atlas.dispose();
    }
}
