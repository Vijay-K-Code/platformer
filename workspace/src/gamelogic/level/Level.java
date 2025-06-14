package gamelogic.level;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import gameengine.PhysicsObject;
import gameengine.graphics.Camera;
import gameengine.loaders.Mapdata;
import gameengine.loaders.Tileset;
import gamelogic.GameResources;
import gamelogic.Main;
import gamelogic.enemies.Enemy;
import gamelogic.player.Player;
import gamelogic.tiledMap.Map;
import gamelogic.tiles.Flag;
import gamelogic.tiles.Flower;
import gamelogic.tiles.Gas;
import gamelogic.tiles.SolidTile;
import gamelogic.tiles.Spikes;
import gamelogic.tiles.Tile;
import gamelogic.tiles.Water;

public class Level {

	private LevelData leveldata;
	private Map map;
	private Enemy[] enemies;
	public static Player player;
	private Camera camera;

	private boolean active;
	private boolean playerDead;
	private boolean playerWin;

	private ArrayList<Enemy> enemiesList = new ArrayList<>();
	private ArrayList<Flower> flowers = new ArrayList<>();

	private List<PlayerDieListener> dieListeners = new ArrayList<>();
	private List<PlayerWinListener> winListeners = new ArrayList<>();

	private Mapdata mapdata;
	private int width;
	private int height;
	private int tileSize;
	private Tileset tileset;
	public static float GRAVITY = 70;

	public Level(LevelData leveldata) {
		this.leveldata = leveldata;
		mapdata = leveldata.getMapdata();
		width = mapdata.getWidth();
		height = mapdata.getHeight();
		tileSize = mapdata.getTileSize();
		restartLevel();
	}

	public LevelData getLevelData(){
		return leveldata;
	}

	public void restartLevel() {
		int[][] values = mapdata.getValues();
		Tile[][] tiles = new Tile[width][height];

		for (int x = 0; x < width; x++) {
			int xPosition = x;
			for (int y = 0; y < height; y++) {
				int yPosition = y;

				tileset = GameResources.tileset;

				tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this);
				if (values[x][y] == 0)
					tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this); // Air
				else if (values[x][y] == 1)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid"), this);

				else if (values[x][y] == 2)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_DOWNWARDS, this);
				else if (values[x][y] == 3)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_UPWARDS, this);
				else if (values[x][y] == 4)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_LEFTWARDS, this);
				else if (values[x][y] == 5)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_RIGHTWARDS, this);
				else if (values[x][y] == 6)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Dirt"), this);
				else if (values[x][y] == 7)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Grass"), this);
				else if (values[x][y] == 8)
					enemiesList.add(new Enemy(xPosition*tileSize, yPosition*tileSize, this)); // TODO: objects vs tiles
				else if (values[x][y] == 9)
					tiles[x][y] = new Flag(xPosition, yPosition, tileSize, tileset.getImage("Flag"), this);
				else if (values[x][y] == 10) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower1"), this, 1);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 11) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower2"), this, 2);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 12)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_down"), this);
				else if (values[x][y] == 13)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_up"), this);
				else if (values[x][y] == 14)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_middle"), this);
				else if (values[x][y] == 15)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasOne"), this, 1);
				else if (values[x][y] == 16)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasTwo"), this, 2);
				else if (values[x][y] == 17)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasThree"), this, 3);
				else if (values[x][y] == 18)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Falling_water"), this, 0);
				else if (values[x][y] == 19)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Full_water"), this, 3);
				else if (values[x][y] == 20)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Half_water"), this, 2);
				else if (values[x][y] == 21)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Quarter_water"), this, 1);
			}

		}
		enemies = new Enemy[enemiesList.size()];
		map = new Map(width, height, tileSize, tiles);
		camera = new Camera(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT, 0, map.getFullWidth(), map.getFullHeight());
		for (int i = 0; i < enemiesList.size(); i++) {
			enemies[i] = new Enemy(enemiesList.get(i).getX(), enemiesList.get(i).getY(), this);
		}
		player = new Player(leveldata.getPlayerX() * map.getTileSize(), leveldata.getPlayerY() * map.getTileSize(),
				this);
		camera.setFocusedObject(player);

		active = true;
		playerDead = false;
		playerWin = false;
	}

	public void onPlayerDeath() {
		active = false;
		playerDead = true;
		throwPlayerDieEvent();
	}

	public void onPlayerWin() {
		active = false;
		playerWin = true;
		throwPlayerWinEvent();
	}

	public void update(float tslf) {
		if (active) {
			// Update the player
			player.update(tslf);

			// Player death
			if (map.getFullHeight() + 100 < player.getY())
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.BOT] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.TOP] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.LEF] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.RIG] instanceof Spikes)
				onPlayerDeath();

			for (int i = 0; i < flowers.size(); i++) {
				if (flowers.get(i).getHitbox().isIntersecting(player.getHitbox())) {
					if(flowers.get(i).getType() == 1)
						water(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 3);
					else
						addGas(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 20, new ArrayList<Gas>());
					flowers.remove(i);
					i--;
				}
			}

			// Update the enemies
			for (int i = 0; i < enemies.length; i++) {
				enemies[i].update(tslf);
				if (player.getHitbox().isIntersecting(enemies[i].getHitbox())) {
					onPlayerDeath();
				}
			}

			// Update the map
			map.update(tslf);

			// Update the camera
			camera.update(tslf);
		}
	}
	
	
	//#############################################################################################################
	//Your code goes here! 
	//Please make sure you read the rubric/directions carefully and implement the solution recursively!
	/**
 * Recursively simulates water flow in the game world starting at (col, row).
 * @param col the x-position (column) in the map grid
 * @param row the y-position (row) in the map grid
 * @param map the current level's map object
 * @param fullness how full the water is (3 = Full, 2 = Half, 1 = Quarter, 0 = Falling)
 */
/**
 * Recursively simulates water flow in the game world starting at (col, row).
 * @param col the x-position (column) in the map grid
 * @param row the y-position (row) in the map grid
 * @param map the current level's map object
 * @param fullness how full the water is (3 = Full, 2 = Half, 1 = Quarter, 0 = Falling)
 */
private void water(int col, int row, Map map, int fullness) {
    Tile[][] tiles = map.getTiles();

    // Bounds check
    if (col < 0 || col >= tiles.length || row < 0 || row >= tiles[0].length)
        return;

    Tile current = tiles[col][row];

    // Stop if it's solid
    if (current.isSolid()) return;

    // Stop if it's already water with equal or greater fullness
    if (current instanceof Water) {
        Water existing = (Water) current;
        if (existing.getFullness() >= fullness) return;
    }

    // Fix: If this water is sitting on a solid tile (2 tiles down), it should be full
    if (fullness < 3 && row + 2 < tiles[0].length && tiles[col][row + 2].isSolid()) {
        fullness = 3;
    }

    // Determine image name
    String imageName;
    if (fullness == 3) imageName = "Full_water";
    else if (fullness == 2) imageName = "Half_water";
    else if (fullness == 1) imageName = "Quarter_water";
    else imageName = "Falling_water";

    // Place the water tile
    Water w = new Water(col, row, tileSize, tileset.getImage(imageName), this, fullness);
    map.addTile(col, row, w);

    // Try to flow downward first
    if (row + 1 < tiles[0].length && !tiles[col][row + 1].isSolid()) {
        water(col, row + 1, map, 0); // Falling water
        return;
    }

    // Spread sideways if not falling water
    if (fullness > 0) {
        // Spread right
        if (col + 1 < tiles.length && !tiles[col + 1][row].isSolid()) {
            if (row + 1 < tiles[0].length && tiles[col + 1][row + 1].isSolid()) {
                // Only flow right if there's support below
                water(col + 1, row, map, fullness);
            } else if (row + 1 < tiles[0].length && !tiles[col + 1][row + 1].isSolid()) {
                water(col + 1, row + 1, map, 0);
            }
        }

        // Spread left
        if (col - 1 >= 0 && !tiles[col - 1][row].isSolid()) {
            if (row + 1 < tiles[0].length && tiles[col - 1][row + 1].isSolid()) {
                water(col - 1, row, map, fullness);
            } else if (row + 1 < tiles[0].length && !tiles[col - 1][row + 1].isSolid()) {
                water(col - 1, row + 1, map, 0);
            }
        }
    }
}






	public void draw(Graphics g) {
	   	 g.translate((int) -camera.getX(), (int) -camera.getY());
	   	 // Draw the map
	   	 for (int x = 0; x < map.getWidth(); x++) {
	   		 for (int y = 0; y < map.getHeight(); y++) {
	   			 Tile tile = map.getTiles()[x][y];
	   			 if (tile == null)
	   				 continue;
	   			 if(tile instanceof Gas) {
	   				
	   				 int adjacencyCount =0;
	   				 for(int i=-1; i<2; i++) {
	   					 for(int j =-1; j<2; j++) {
	   						 if(j!=0 || i!=0) {
	   							 if((x+i)>=0 && (x+i)<map.getTiles().length && (y+j)>=0 && (y+j)<map.getTiles()[x].length) {
	   								 if(map.getTiles()[x+i][y+j] instanceof Gas) {
	   									 adjacencyCount++;
	   								 }
	   							 }
	   						 }
	   					 }
	   				 }
	   				 if(adjacencyCount == 8) {
	   					 ((Gas)(tile)).setIntensity(2);
	   					 tile.setImage(tileset.getImage("GasThree"));
	   				 }
	   				 else if(adjacencyCount >5) {
	   					 ((Gas)(tile)).setIntensity(1);
	   					tile.setImage(tileset.getImage("GasTwo"));
	   				 }
	   				 else {
	   					 ((Gas)(tile)).setIntensity(0);
	   					tile.setImage(tileset.getImage("GasOne"));
	   				 }
	   			 }
	   			 if (camera.isVisibleOnCamera(tile.getX(), tile.getY(), tile.getSize(), tile.getSize()))
	   				 tile.draw(g);
	   		 }
	   	 }


	   	 // Draw the enemies
	   	 for (int i = 0; i < enemies.length; i++) {
	   		 enemies[i].draw(g);
	   	 }


	   	 // Draw the player
	   	 player.draw(g);




	   	 // used for debugging
	   	 if (Camera.SHOW_CAMERA)
	   		 camera.draw(g);
	   	 g.translate((int) +camera.getX(), (int) +camera.getY());
	    }


	// --------------------------Die-Listener
	public void throwPlayerDieEvent() {
		for (PlayerDieListener playerDieListener : dieListeners) {
			playerDieListener.onPlayerDeath();
		}
	}

	public void addPlayerDieListener(PlayerDieListener listener) {
		dieListeners.add(listener);
	}

	// ------------------------Win-Listener
	public void throwPlayerWinEvent() {
		for (PlayerWinListener playerWinListener : winListeners) {
			playerWinListener.onPlayerWin();
		}
	}

	public void addPlayerWinListener(PlayerWinListener listener) {
		winListeners.add(listener);
	}

	// ---------------------------------------------------------Getters
	public boolean isActive() {
		return active;
	}

	public boolean isPlayerDead() {
		return playerDead;
	}

	public boolean isPlayerWin() {
		return playerWin;
	}

	public Map getMap() {
		return map;
	}

	public Player getPlayer() {
		return player;
	}
}

/**
 * Adds gas tiles spreading from (col, row) on the given map.
 *
 * Precondition: 
 *   - col and row are valid indices inside map bounds.
 *   - map is not null and tiles are properly initialized.
 *   - numSquaresToFill > 0.
 *   - placedThisRound is an ArrayList tracking the newly placed gas tiles.
 *
 * Postcondition:
 *   - Up to numSquaresToFill gas tiles have been placed.
 *   - Gas tiles spread in this order from each placed tile:
 *       up, up-right, up-left, right, left, down, down-right, down-left.
 *   - Only placed on non-solid, non-gas tiles within the map.
 *   - placedThisRound contains all new gas tiles.
 */
private void addGas(int col, int row, Map map, int numSquaresToFill, ArrayList<Gas> placedThisRound) {
    Tile[][] tiles = map.getTiles();

    // Validate starting position
    if (col < 0 || col >= tiles.length || row < 0 || row >= tiles[0].length) {
        return;
    }
    if (tiles[col][row] == null || tiles[col][row].isSolid()) {
        return;
    }

    // Place initial gas tile
    Gas startGas = new Gas(col, row, tileSize, tileset.getImage("GasOne"), this, 0);
    map.addTile(col, row, startGas);
    placedThisRound.add(startGas);
    numSquaresToFill--;

    int index = 0;

    // Directions in the required order: up, up-right, up-left, right, left, down, down-right, down-left
    int[][] directions = {
        {0, -1},   // up
        {1, -1},   // up-right
        {-1, -1},  // up-left
        {1, 0},    // right
        {-1, 0},   // left
        {0, 1},    // down
        {1, 1},    // down-right
        {-1, 1}    // down-left
    };

    while (numSquaresToFill > 0 && index < placedThisRound.size()) {
        Gas currentGas = placedThisRound.get(index);
        int c = currentGas.getCol();
        int r = currentGas.getRow();

        // Check all directions in order
        for (int i = 0; i < directions.length; i++) {
            int newCol = c + directions[i][0];
            int newRow = r + directions[i][1];

            // Check bounds
            if (newCol >= 0 && newCol < tiles.length && newRow >= 0 && newRow < tiles[0].length) {
                Tile targetTile = tiles[newCol][newRow];

                // Check if the tile is valid for gas placement
                if (targetTile != null && !targetTile.isSolid() && !(targetTile instanceof Gas)) {
                    Gas newGas = new Gas(newCol, newRow, tileSize, tileset.getImage("GasOne"), this, 0);
                    map.addTile(newCol, newRow, newGas);
                    placedThisRound.add(newGas);
                    numSquaresToFill--;

                    // Refresh tiles array after adding gas
                    tiles = map.getTiles();

                    if (numSquaresToFill == 0) {
                        break;  // Stop if no more gas tiles to place
                    }
                }
            }
        }
        index++;  // Move to next gas tile to spread from
    }
}

// Constants for tile types (make sure these match your existing tile IDs)
private static final int TILE_WATER = 1;  // example water tile ID
private static final int TILE_GAS = 2;    // example gas tile ID

// State variables for gas exposure tracking
private int gasContactTicks = 0;  // how many game ticks player has been in gas
private static final int GAS_KILL_TICKS = 180; // 3 seconds if 60 FPS

// State variables for double jump
private boolean canDoubleJump = false;
private boolean hasDoubleJumped = false;

// Call this method in your main game loop to update player effects based on current tile
public void updatePlayerTileEffects() {
    int playerX = player.getTileX(); // get player's current tile X coordinate
    int playerY = player.getTileY(); // get player's current tile Y coordinate
    
    int currentTile = getTile(playerX, playerY);

    // WATER: Slow down the player when standing on water tile
    if (currentTile == TILE_WATER) {
        player.setSpeedMultiplier(0.5);  // slow speed by half on water
    } else {
        player.setSpeedMultiplier(1.0);  // normal speed otherwise
    }

    // GAS: If player is on gas tile, start counting ticks, else reset
    if (currentTile == TILE_GAS) {
        gasContactTicks++;
        if (gasContactTicks >= GAS_KILL_TICKS) {
            player.kill(); // kill the player after 3 seconds in gas
        }
    } else {
        gasContactTicks = 0;  // reset timer if player leaves gas tile
    }
}

// Call this method when player jumps (hook into your player jump method)
public void handlePlayerJump() {
    if (player.isOnGround()) {
        // Reset double jump availability when touching the ground
        canDoubleJump = true;
        hasDoubleJumped = false;
        player.jump();
    } else if (canDoubleJump && !hasDoubleJumped) {
        // Allow one double jump in the air
        player.jump();
        hasDoubleJumped = true;
    }
}

// Make sure to call updatePlayerTileEffects() each game update cycle
// and replace your player's normal jump with handlePlayerJump()
