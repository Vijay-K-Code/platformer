package gamelogic.tiles;

public class SpecialTile extends Tile {
    private float x;
    private float y;
    private int size;
    private boolean isActive;

    public SpecialTile(float x, float y, int size) {
        super(x, y, size); // Call the appropriate Tile constructor
        this.x = x;
        this.y = y;
        this.size = size;
        this.isActive = true; // Initially active
    }

    public void activate() {
        isActive = true;
    }

    public void deactivate() {
        isActive = false;
    }

    public boolean isActive() {
        return isActive;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getSize() {
        return size;
    }
}
