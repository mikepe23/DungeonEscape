package byow.Core;

import byow.TileEngine.TETile;

import java.io.Serializable;

public class Element implements Serializable {
    protected Coordinate coord;
    TETile id;

    public Element(TETile tile, Coordinate c) {
        coord = c;
        id = tile;
    }

    public int getX() {
        return coord.getX();
    }

    public int getY() {
        return coord.getY();
    }

    public Coordinate getCoord() {
        return coord.copy();
    }

    public void setCoords(int x, int y) {
        coord.setX(x);
        coord.setY(y);
    }

    public TETile getID() {
        return id;
    }
}
