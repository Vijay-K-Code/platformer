package gamelogic.player;

import java.awt.Color;
import java.awt.Graphics;

import gameengine.PhysicsObject;
import gameengine.graphics.MyGraphics;
import gameengine.hitbox.RectHitbox;
import gamelogic.Main;
import gamelogic.level.Level;
import gamelogic.tiles.Tile;
import gamelogic.tiles.WaterTile;
import gamelogic.tiles.GasTile;

public class Player extends PhysicsObject {
    public float walkSpeed = 400;
    public float jumpPower = 1350;

    private boolean isJumping = false;
    private boolean canDoubleJump = false;  
    private boolean hasDoubleJumped = false;

    private boolean inGas = false;
    private float gasTimer = 0;

    private boolean alive = true;

    public Player(float x, float y, Level level) {
        super(x, y, level.getLevelData().getTileSize(), level.getLevelData().getTileSize(), level);
        int offset = (int)(level.getLevelData().getTileSize() * 0.1);
        this.hitbox = new RectHitbox(this, offset, offset, width - offset, height - offset);
    }

    @Override
    public void update(float tslf) {
        if (!alive) {
            // Player is dead, no movement
            movementVector.x = 0;
            return;
        }

        super.update(tslf);

        movementVector.x = 0;

        // Find tile under player (bottom center)
        int tileX = (int)((getX() + width / 2) / level.getLevelData().getTileSize());
        int tileY = (int)((getY() + height) / level.getLevelData().getTileSize());
        Tile currentTile = level.getLevelData().getTile(tileX, tileY);

        // If standing on water, slow down speed by half
        float currentSpeed = walkSpeed;
        if (currentTile instanceof WaterTile) {
            currentSpeed = walkSpeed / 2;
        }

        // Move left or right if keys pressed
        if (PlayerInput.isLeftKeyDown()) {
            movementVector.x = -currentSpeed;
        }
        if (PlayerInput.isRightKeyDown()) {
            movementVector.x = currentSpeed;
        }

        // Jump logic with double jump
        if (PlayerInput.isJumpKeyDown()) {
            if (!isJumping) {
                movementVector.y = -jumpPower;  // jump up
                isJumping = true;
                canDoubleJump = true;
                hasDoubleJumped = false;
            } else if (canDoubleJump && !hasDoubleJumped) {
                movementVector.y = -jumpPower;  // second jump in air
                hasDoubleJumped = true;
            }
        }

        // Reset jumping if player is on ground
        if (collisionMatrix[BOT] != null) {
            isJumping = false;
            canDoubleJump = false;
            hasDoubleJumped = false;
        }

        // Gas tile check
        if (currentTile instanceof GasTile) {
            inGas = true;
            gasTimer += tslf;  // increase time in gas
            if (gasTimer >= 3.0) {  // if in gas 3 seconds or more, die
                alive = false;
                System.out.println("Player died from gas!");
            }
        } else {
            inGas = false;
            gasTimer = 0;  // reset timer if not in gas
        }
    }

    @Override
    public void draw(Graphics g) {
        if (!alive) {
            g.setColor(Color.GRAY);  // dead player is gray
        } else {
            g.setColor(Color.YELLOW);
        }
        MyGraphics.fillRectWithOutline(g, (int)getX(), (int)getY(), width, height);

        if (Main.DEBUGGING) {
            for (int i = 0; i < closestMatrix.length; i++) {
                Tile t = closestMatrix[i];
                if (t != null) {
                    g.setColor(Color.RED);
                    g.drawRect((int)t.getX(), (int)t.getY(), t.getSize(), t.getSize());
                }
            }
        }

        hitbox.draw(g);
    }

    public boolean isAlive() {
        return alive;
    }
}
