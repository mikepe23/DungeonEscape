package byow.Core;

import byow.TileEngine.TETile;

public class Room extends Element {
    private int width;
    private int height;

    public Room(TETile tile, int x, int y, int w, int h) {
        super(tile, new Coordinate(x, y));
        this.width = w;
        this.height = h;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
