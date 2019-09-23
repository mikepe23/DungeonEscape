package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Portal extends Element {
    Coordinate other;

    public Portal(TETile t, Coordinate c1, Coordinate c2) {
        super(t, c1);
        other = c2;
    }

    /**
     * Transport the character to the other end of the portal
     *
     * @param c
     * @param p
     */
    public boolean transport(Coordinate c, Player p) {
        TETile[][] grid = p.engine.gameGrid;
        Coordinate drop;

        if (c.equals(other)) {
            drop = dropSpot(coord, p);
        } else {
            drop = dropSpot(other, p);
        }
        if (drop == null) {
            return false;
        }
        grid[drop.getX()][drop.getY()] = p.getID();
        grid[p.getX()][p.getY()] = Tileset.FLOOR;
        p.setCoords(drop.getX(), drop.getY());
        return true;
    }

    /**
     * Finds a valid spot to drop the character coming out of the other end of the portal
     *
     * @param c
     * @param p
     * @return
     */
    private Coordinate dropSpot(Coordinate c, Player p) {
        TETile[][] grid = p.engine.gameGrid;
        int lowerBoundX = Math.max(0, c.getX() - 1);
        int upperBoundX = Math.min(grid.length - 1, c.getX() + 1);
        int lowerBoundY = Math.max(0, c.getY() - 1);
        int upperBoundY = Math.min(grid[0].length - 1, c.getY() + 1);

        Coordinate drop = new Coordinate(c.getX(), c.getY());
        if (grid[c.getX()][lowerBoundY] == Tileset.FLOOR) {
            drop.setY(lowerBoundY);
        } else if (grid[c.getX()][upperBoundY] == Tileset.FLOOR) {
            drop.setY(upperBoundY);
        } else if (grid[lowerBoundX][c.getY()] == Tileset.FLOOR) {
            drop.setX(lowerBoundX);
        } else if (grid[upperBoundX][c.getY()] == Tileset.FLOOR) {
            drop.setX(upperBoundX);
        } else {
            return null;
        }
        return drop;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Coordinate) {
            Coordinate c = (Coordinate) o;
            return (c.getX() == coord.getX() && c.getY() == coord.getY())
                    || (c.getX() == other.getX() && c.getY() == other.getY());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
