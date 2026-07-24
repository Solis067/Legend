package io.github.solis067.legend.Objects.Tools;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import io.github.solis067.legend.Main;
import io.github.solis067.legend.Objects.Entities.Player;

public class Sword {
    Player player;

    private Body playerBody;
    private com.badlogic.gdx.physics.box2d.Fixture hitboxSensor;

    // Hitbox dimensions
    private final float ENTITY_WIDTH = Main.TILE_PIXELS * Main.UNIT_SCALE;
    private final float HITBOX_WIDTH = ENTITY_WIDTH * 1.2f;
    private final float HITBOX_HEIGHT = ENTITY_WIDTH * 0.7f;
    private final float HITBOX_DISTANCE = ENTITY_WIDTH * 0.7f;

    private enum Direction { DOWN, LEFT, RIGHT, UP }
    private Direction currentDirection = Direction.DOWN;
    private Direction hitboxDirection = Direction.DOWN;

    public Sword(Player player, Body playerBody) {
        this.player = player;
        this.playerBody = playerBody;
        createHitboxSensor();
    }

    public void createHitboxSensor() {
        destroyHitboxSensor();
        if (playerBody == null) return;

        PolygonShape hitboxShape = new PolygonShape();
        Vector2 offset = hitboxOffsetForDirection(currentDirection);
        float rotation = getRotationForDirection(currentDirection);
        hitboxShape.setAsBox(HITBOX_WIDTH / 2f, HITBOX_HEIGHT / 2f, offset, rotation);

        FixtureDef hitboxDef = new FixtureDef();
        hitboxDef.shape = hitboxShape;
        hitboxDef.isSensor = true;
        hitboxDef.density = 0f;
        hitboxSensor = playerBody.createFixture(hitboxDef);
        hitboxSensor.setUserData(this);
        hitboxShape.dispose();
        hitboxDirection = currentDirection;
    }

    public void destroyHitboxSensor() {
        if (hitboxSensor != null && playerBody != null) {
            playerBody.destroyFixture(hitboxSensor);
            hitboxSensor = null;
        }
    }

    private Vector2 hitboxOffsetForDirection(Direction direction) {
        switch (direction) {
            case DOWN: return new Vector2(0, -HITBOX_DISTANCE);
            case UP: return new Vector2(0, HITBOX_DISTANCE);
            case LEFT: return new Vector2(-HITBOX_DISTANCE, 0);
            case RIGHT: return new Vector2(HITBOX_DISTANCE, 0);
            default: return new Vector2(0, -HITBOX_DISTANCE);
        }
    }

    private float getRotationForDirection(Direction direction) {
        switch (direction) {
            case DOWN: return 0f;
            case UP: return (float) Math.PI;
            case LEFT: return (float) Math.PI / 2f;
            case RIGHT: return -(float) Math.PI / 2f;
            default: return 0f;
        }
    }

    public void setDirection(int directionOrdinal) {
        Direction[] dirs = Direction.values();
        if (directionOrdinal >= 0 && directionOrdinal < dirs.length) {
            currentDirection = dirs[directionOrdinal];
        }
    }

    public void update() {
        if (playerBody == null) return;
        if (hitboxSensor == null) {
            createHitboxSensor();
            return;
        }
        if (currentDirection != hitboxDirection) {
            createHitboxSensor();
        }
    }

    public com.badlogic.gdx.physics.box2d.Fixture getHitboxSensor() {
        return hitboxSensor;
    }

    public Player getPlayer() {
        return player;
    }

    public void dispose() {
        destroyHitboxSensor();
    }
}
