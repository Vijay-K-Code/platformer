package gamelogic.player;

import java.awt.Color;
import java.awt.Graphics;

import gameengine.PhysicsObject;
import gameengine.graphics.MyGraphics;
import gameengine.hitbox.RectHitbox;
import gamelogic.Main;
import gamelogic.level.Level;
import gamelogic.tiles.SpecialTile;
import gamelogic.tiles.Tile;


public class Player extends PhysicsObject{
	public float walkSpeed = 400;
	public float jumpPower = 1350;
	private boolean canDoubleJump = false;
	public boolean playerTouchingWater = false; 
	
// The player can jump twice before touching the ground again.
private int jumpCount = 0;
private int maxJumps = 2;


	private boolean isJumping = false;

	public void checkTileCollision(Tile tile) {
    if (tile instanceof SpecialTile) {
        canDoubleJump = true;}
    }

	public Player(float x, float y, Level level) {
	
		super(x, y, level.getLevelData().getTileSize(), level.getLevelData().getTileSize(), level);
		int offset =(int)(level.getLevelData().getTileSize()*0.1); //hitbox is offset by 10% of the player size.
		this.hitbox = new RectHitbox(this, offset,offset, width -offset, height - offset);
	}


public boolean isInWater() {
    if (level == null || level.getLevelData() == null || level.getLevelData().getTiles() == null) {
        return false;
    }
    Tile[][] tiles = level.getLevelData().getTiles();

    for (int row = 0; row < tiles.length; row++) {
        for (int col = 0; col < tiles[row].length; col++) {
            Tile tile = tiles[row][col];
            if (tile instanceof gamelogic.tiles.Water && tile.getHitbox() != null && this.hitbox != null) {
                if (this.hitbox.isIntersecting(tile.getHitbox())) {
                    return true;
                }
            }
        }
    }
    return false;
}


	@Override
	public void update(float tslf) {
		super.update(tslf);
		
		movementVector.x = 0;
		if (playerTouchingWater){
			movementVector.x = 0.2f; // slow horizontal movement in water
		}

boolean inWater = isInWater();
float speedMultiplier;
if (playerTouchingWater) {
	speedMultiplier = 0.1f;
} else {
	speedMultiplier = 1.0f;
}

if(PlayerInput.isLeftKeyDown()) {
	if (playerTouchingWater){
		movementVector.x = -walkSpeed * speedMultiplier * 0.05f; // Slower movement in water
	} else {
		movementVector.x = -walkSpeed * speedMultiplier;
	}
	
}
if(PlayerInput.isRightKeyDown()) {
	if (isInWater()){
		movementVector.x = +walkSpeed * speedMultiplier * 0.05f; // Slower movement in water
	} else {
	movementVector.x = +walkSpeed * speedMultiplier;
}}

	if(PlayerInput.isJumpKeyDown() && !isJumping) {
		
maxJumps=2;
jumpCount = 1;
		movementVector.y = -jumpPower;
		isJumping = true;
		 // Reset maxJumps when the player jumps
		 // Reset jump count to 1 when the player jumps
	}
	if (PlayerInput.isJumpKeyDown() && isJumping && jumpCount<maxJumps) {
		
	movementVector.y = -jumpPower;
	jumpCount++;
	// Use collisionMatrix[BOT] to check if player is on the ground
	if (collisionMatrix[BOT] != null) {
		isJumping = false; // Reset jumping state if player is on the ground
		jumpCount = 0; // Reset jump count when touching the ground
	}
	} else if (PlayerInput.isJumpKeyDown() && isJumping && jumpCount >= maxJumps) {
		// If the player is already jumping and has reached the max jumps, do nothing
	} else if (collisionMatrix[BOT] != null) {
		// If the player is on the ground, reset jumping state and jump count
		isJumping = false;
		jumpCount = 0;
	} else {
		isJumping = true; // Player is in the air
		jumpCount = 1; // Reset jump count to 1 when jumping
}
        if (inWater) {
	
	
	
	// Dampens gravity/movement in water
	movementVector.y *= 0.85f;
}

		
		isJumping = true;
		if(collisionMatrix[BOT] != null) isJumping = false;

        
	}
	

	@Override
	public void draw(Graphics g) {
		g.setColor(Color.YELLOW);
		MyGraphics.fillRectWithOutline(g, (int)getX(), (int)getY(), width, height);
		
		if(Main.DEBUGGING) {
			for (int i = 0; i < closestMatrix.length; i++) {
				Tile t = closestMatrix[i];
				if(t != null) {
					g.setColor(Color.RED);
					g.drawRect((int)t.getX(), (int)t.getY(), t.getSize(), t.getSize());
				}
			}
		}
		
		hitbox.draw(g);
	}
}