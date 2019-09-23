package byow.Core;

import java.io.Serializable;

public class Coordinate implements Serializable {
    private int X;
    private int Y;

    public Coordinate(int x, int y) {
        X = x;
        Y = y;
    }

    public int getX() {
        return X;
    }

    public int getY() {
        return Y;
    }

    public void setX(int x) {
        X = x;
    }

    public void setY(int y) {
        Y = y;
    }

    public Coordinate copy() {
        return new Coordinate(X, Y);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Coordinate) {
            Coordinate c = (Coordinate) o;
            return X == c.X && Y == c.Y;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
