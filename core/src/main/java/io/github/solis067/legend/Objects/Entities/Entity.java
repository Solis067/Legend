package io.github.solis067.legend.Objects.Entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import io.github.solis067.legend.Main;

public abstract class Entity {
    protected Vector2 pos;
    protected Vector2 vel;
    protected TextureAtlas atlas;
    protected TextureRegion currentFrame;
    protected int health;

    public Body body;
    public String id;

    protected float stateTime = 0f;
    protected float damageTimer = 0f;

    final float ENTITY_WIDTH = Main.TILE_PIXELS * Main.UNIT_SCALE;
    final float ENTITY_HEIGHT = Main.TILE_PIXELS * Main.UNIT_SCALE;

    public abstract void update(float delta);
    public abstract void render(SpriteBatch batch);

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
