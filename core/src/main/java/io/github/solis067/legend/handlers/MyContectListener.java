package io.github.solis067.legend.handlers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import io.github.solis067.legend.Player;
import io.github.solis067.legend.Slime;
import io.github.solis067.legend.Sword;

public class MyContectListener implements ContactListener {
    private static final float PLAYER_HIT_COOLDOWN = 0.1f; // cooldown between hits in seconds
    private float playerHitCooldown = 0f;

    @Override
    public void beginContact(Contact contact) {
        Fixture fA = contact.getFixtureA();
        Fixture fB = contact.getFixtureB();
        
        if (fA == null || fB == null) return;
        if (fA.getUserData() == null || fB.getUserData() == null) return;

        // Player touches slime -> damage player (with cooldown)
        if (playerTouchesSlime(fA, fB)) {
            if (playerHitCooldown <= 0f) {
                Player player = (Player) ((fA.getUserData() instanceof Player) ? fA.getUserData() : fB.getUserData());
                Slime slime = (Slime) ((fA.getUserData() instanceof Slime) ? fA.getUserData() : fB.getUserData());
                // Calculate knockback direction from slime to player
                if (slime.isDying()) return;
                Vector2 knockbackDir = new Vector2(
                    player.body.getPosition().x - slime.body.getPosition().x,
                    player.body.getPosition().y - slime.body.getPosition().y
                );
                player.takeDamage(2, knockbackDir);
                playerHitCooldown = PLAYER_HIT_COOLDOWN;
            }
        }

        // Player hitbox sensor touches slime while attacking -> damage slime
        if (swordHitboxTouchesSlime(fA, fB)) {
            Player player = extractPlayerFromHitbox(fA, fB);
            Slime slime = extractSlime(fA, fB);
            if (player != null && slime != null) {
                player.setSlimeinRange(true, slime);
            }
        }
    }
    
    public void update(float delta) {
        if (playerHitCooldown > 0f) {
            playerHitCooldown -= delta;
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fA = contact.getFixtureA();
        Fixture fB = contact.getFixtureB();
        
        if (fA == null || fB == null) return;
        if (fA.getUserData() == null || fB.getUserData() == null) return;


        if (swordHitboxTouchesSlime(fA, fB)) {
            Player player = extractPlayerFromHitbox(fA, fB);
            Slime slime = extractSlime(fA, fB);
            if (player != null && slime != null) {
                player.setSlimeinRange(false, null);
            }
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        // Handle pre-solve events
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        // Handle post-solve events
    }

    private boolean playerTouchesSlime(Fixture a, Fixture b) {
        return (a.getUserData() instanceof Player && b.getUserData() instanceof Slime)
            || (b.getUserData() instanceof Player && a.getUserData() instanceof Slime);
    }

    private boolean swordHitboxTouchesSlime(Fixture a, Fixture b) {
        boolean aHitbox = a.getUserData() instanceof Sword;
        boolean bHitbox = b.getUserData() instanceof Sword;
        boolean aSlime = a.getUserData() instanceof Slime;
        boolean bSlime = b.getUserData() instanceof Slime;
        return (aHitbox && bSlime) || (bHitbox && aSlime);
    }

    private Player extractPlayerFromHitbox(Fixture a, Fixture b) {
        // The hitbox is a sensor attached to the player's body; find the player's main fixture
        if (a.getUserData() instanceof Sword && b.getBody() != null) {
            return ((Sword) a.getUserData()).getPlayer();
        }
        if (b.getUserData() instanceof Sword && a.getBody() != null) {
            return ((Sword) b.getUserData()).getPlayer();
        }
        return null;
    }

    private Slime extractSlime(Fixture a, Fixture b) {
        if (a.getUserData() instanceof Slime) return (Slime) a.getUserData();
        if (b.getUserData() instanceof Slime) return (Slime) b.getUserData();
        return null;
    }
}