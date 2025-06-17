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
	
// The player can jump twice before touching the ground again.
private int jumpCount = 0;
private final int maxJumps = 2;


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

private boolean isInWater() {
	if (level == null || level.getLevelData() == null || level.getLevelData().getTiles() == null) {
		return false;
	}
	Tile[][] tiles = getLevel().getLevelData().getTiles();
	int tileSize = getLevel().getLevelData().getTileSize();

	int left = (int)(getX() / tileSize);
	int right = (int)((getX() + width - 1) / tileSize);
	int top = (int)(getY() / tileSize);
	int bottom = (int)((getY() + height - 1) / tileSize);

	for (int col = left; col <= right; col++) {
		for (int row = top; row <= bottom; row++) {
			if (col >= 0 && row >= 0 && col < tiles.length && row < tiles[0].length) {
				if (tiles[col][row] instanceof gamelogic.tiles.Water) {
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

boolean inWater = isInWater();
float speedMultiplier;
if (inWater) {
	speedMultiplier = 0.3f;
} else {
	speedMultiplier = 1.0f;
}

if(PlayerInput.isLeftKeyDown()) {
	movementVector.x = -walkSpeed * speedMultiplier;
}
if(PlayerInput.isRightKeyDown()) {
	movementVector.x = +walkSpeed * speedMultiplier;
}

	if(PlayerInput.isJumpKeyDown() && !isJumping) {
		int allowedJumps = canDoubleJump ? maxJumps : 1;
if (jumpCount < allowedJumps) {
	movementVector.y = -jumpPower;
	jumpCount++;
	// Use collisionMatrix[BOT] to check if player is on the ground
	if (collisionMatrix[BOT] != null) {
		jumpCount = 0;
		canDoubleJump = false; // Remove this line if you want double jump to persist after landing
	}
}
		movementVector.y = -jumpPower;
		isJumping = true;
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