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

public class Slime extends Entity {

    Texture idleTexture;
    Animation<TextureRegion> idleAnimation;

    final float ANIMATION_SPEED = 1.5f;

    
    public Slime(World world, int x , int y) {
        pos = new Vector2(x, y);
        vel = new Vector2(0, 0);
        createBody(world, x, y);
        setupAnimations();
    }

    private void setupAnimations() {
        idleTexture = new Texture(Gdx.files.internal("Enemies_Sprites/Pinkslime_Sprites/pinkslime_idle_anim_all_dir_strip_6.png"));

        idleAnimation = makeAnimation(idleTexture, 6, 1, ANIMATION_SPEED);

        currentFrame = idleAnimation.getKeyFrame(0);
    }

    public void update(float delta) {
        stateTime += delta;

        body.setLinearVelocity(vel.x, vel.y);
        pos.x = body.getPosition().x - ENTITY_WIDTH / 2f;
        pos.y = body.getPosition().y - (ENTITY_HEIGHT / 2f) + 0.15f;

        currentFrame = idleAnimation.getKeyFrame(stateTime, true);
    }

    public void draw(Main game) {
        // Drawing logic goes here
        game.batch.draw(currentFrame, pos.x, pos.y, ENTITY_WIDTH, ENTITY_HEIGHT);
    }

    private void createBody(World world, int x, int y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        // Body positioned at center of sprite
        bodyDef.position.set(x + ENTITY_WIDTH / 2f, y + ENTITY_HEIGHT / 2f);
        body = world.createBody(bodyDef);
        body.setFixedRotation(true);
        body.setLinearDamping(0.25f);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(ENTITY_WIDTH / 2.8f, ENTITY_HEIGHT / 4f);

        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.density = 1f;
        fixture.friction = 0.2f;
        fixture.restitution = 0f;
        body.createFixture(fixture);
        shape.dispose();
    }

    public void dispose() {
        idleTexture.dispose();
    }
}
