package io.github.solis067.legend;

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

public class Slime extends Entity {

    // Textures and animations
    Texture idleTexture;
    Texture hitTexture;
    Animation<TextureRegion> idleAnimation;
    Animation<TextureRegion> hitAnimation;

    // Variables

    final float ANIMATION_SPEED = 1.5f;

    boolean isTakingDamage = false;
    float damageTimer = 0f;
    boolean isKnockedBack = false;
    float knockbackTimer = 0f;
    float KNOCKBACK_DURATION = 0.2f;

    Fixture slimeFixture;

    public Slime(World world, String id, int x , int y) {
        pos = new Vector2(x, y);
        vel = new Vector2(0, 0);
        this.id = id;

        health = 20;

        createBody(world, x, y);
        setupAnimations();
    }

    private void setupAnimations() {
        idleTexture = new Texture(Gdx.files.internal("Enemies_Sprites/Pinkslime_Sprites/pinkslime_idle_anim_all_dir_strip_6.png"));
        hitTexture = new Texture(Gdx.files.internal("Enemies_Sprites/Pinkslime_Sprites/pinkslime_hit_anim_all_dir_strip_4.png"));

        idleAnimation = makeAnimation(idleTexture, 6, 1, ANIMATION_SPEED);
        hitAnimation = makeAnimation(hitTexture, 4, 1, ANIMATION_SPEED / 1.5f);

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

    public void update(float delta) {
        stateTime += delta;

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

        // Only set velocity if not being knocked back
        if (!isKnockedBack) {
            body.setLinearVelocity(vel.x, vel.y);
        }
        pos.x = body.getPosition().x - ENTITY_WIDTH / 2f;
        pos.y = body.getPosition().y - (ENTITY_HEIGHT / 2f) + 0.15f;

        if (isTakingDamage) {
            return; // skip normal animation updates while taking damage
        }

        currentFrame = idleAnimation.getKeyFrame(stateTime, true);
    }

    public void draw(Main game) {
        // Drawing logic goes here
        game.batch.draw(currentFrame, pos.x, pos.y, ENTITY_WIDTH, ENTITY_HEIGHT);
    }

    public void takeDamage(int damage, Vector2 knockbackDirection) {
        damageTimer = 0f;
        isTakingDamage = true;
        health = Math.max(health - damage, 0);
        
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

    public void dispose() {
        idleTexture.dispose();
        hitTexture.dispose();
    }
}
