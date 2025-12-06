package io.github.solis067.legend.handlers;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import io.github.solis067.legend.Player;
import io.github.solis067.legend.Slime;

public class MyContectListener implements ContactListener {
    private static final float PLAYER_HIT_COOLDOWN = 0.5f; // cooldown between hits in seconds
    private float playerHitCooldown = 0f;

    @Override
    public void beginContact(Contact contact) {
        Fixture fA = contact.getFixtureA();
        Fixture fB = contact.getFixtureB();
        
        if (fA == null || fB == null) return;
        if (fA.getUserData() == null || fB.getUserData() == null) return;

        if (PlayerTouchesSlime(fA, fB)) {
            // Only apply damage if cooldown has elapsed
            if (playerHitCooldown <= 0f) {
                // Determine which is player and which is slime
                Fixture playerFixture = (fA.getUserData() instanceof Player) ? fA : fB;
                
                Player player = (Player) playerFixture.getUserData();
                player.takeDamage(2);
                
                // Start cooldown
                playerHitCooldown = PLAYER_HIT_COOLDOWN;
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

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        // Handle pre-solve events
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        // Handle post-solve events
    }

    private boolean PlayerTouchesSlime(Fixture a, Fixture b) {
        if (a.getUserData() instanceof Player || b.getUserData() instanceof Player) {
            if (a.getUserData() instanceof Slime || b.getUserData() instanceof Slime) {
                return true;
            }
        }
        return false;
    }
}