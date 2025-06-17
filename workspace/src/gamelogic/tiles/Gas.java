package gamelogic.tiles;


import java.awt.image.BufferedImage;


import gameengine.hitbox.RectHitbox;
import gamelogic.level.Level;



public class Gas extends Tile{
    private int intensity;
    private long touchStartTime = -1;
    public Gas(float x, float y, int size, BufferedImage image, Level level, int intensity) {
     super(x, y, size, image, false, level);
     this.intensity = intensity;
     this.hitbox = new RectHitbox(x*size , y*size, 0, 10, size, size);
    }
    public int getIntensity() {
   	 return intensity;
    }
    public void setIntensity(int intensity) {
   	 this.intensity = intensity;
    }
    public void update(float tslf, BufferedImage image, Object playerObj) {
        // Check if the object is a Player instance
        if (playerObj != null && playerObj.getClass().getSimpleName().equals("Player")) {
            // Use reflection to avoid direct Player import
            try {
                // Check intersection
                boolean intersects = (boolean) this.getClass().getMethod("intersects", Object.class).invoke(this, playerObj);
                if (intersects) {
                    if (touchStartTime == -1) {
                        touchStartTime = System.currentTimeMillis();
                    }
                    long elapsed = System.currentTimeMillis() - touchStartTime;
                    if (elapsed >= 3000) {
                        // Use reflection to call setGameOver(true)
                        playerObj.getClass().getMethod("setGameOver", boolean.class).invoke(playerObj, true);
                    }
                } else {
                    touchStartTime = -1;
                }
            } catch (Exception e) {
                // Handle exception or log
            }
        } else {
            touchStartTime = -1;
        }
        super.update(tslf);
        super.setImage(image);
    }
} 
