package byow.Core;

import byow.TileEngine.TETile;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;


public class Warrior extends Player {
    Hero hero;
    boolean freeze = false; // After warrior hits hero, freezes for 1 turn
    private ArrayList<Coordinate> path;

    public Warrior(Engine e, TETile tile, Coordinate c, Random r, Hero h) {
        super(e, tile, c, r, 2);
        this.hero = h;
        path = new ArrayList<>();
    }

    @Override
    public void play(char c) {
        if (freeze) {
            freeze = false;
            return;
        }
        double nextMove = rnd.nextDouble();
        if (manhattanDistance(hero, this) < 14) {
            path = engine.performSearch(getCoord(), hero.getCoord());

            if (path.isEmpty() || nextMove > 0.85) {
                moveRandomly();
            } else {
                Coordinate nextCoor = path.get(0);
                Engine.Direction nextDir;
                if ((nextCoor.getX() != getX() && nextCoor.getY() != getY())) {
                    moveRandomly();
                    return;
                }
                if (nextCoor.getX() > getX()) {
                    nextDir = Engine.Direction.EAST;
                } else if (nextCoor.getX() < getX()) {
                    nextDir = Engine.Direction.WEST;
                } else if (nextCoor.getY() > getY()) {
                    nextDir = Engine.Direction.NORTH;
                } else {
                    nextDir = Engine.Direction.SOUTH;
                }
                move(nextDir);
                path = engine.performSearch(getCoord(), hero.getCoord());
            }
        } else {
            // If hero is too far, clear the path
            path = new ArrayList<>();
            if (nextMove > 0.9) {
                return; // don't move
            }
            moveRandomly();
        }
        // If hero is exactly around warrior, hit hero
        if (manhattanDistance(this, hero) <= 1 && nextMove > 0.5) {
            freeze = true;
            hero.takeHit();
        }
    }

    // Returns a copy of path
    public LinkedList<Coordinate> getPath() {
        return new LinkedList<>(path);
    }

    private void moveRandomly() {
        ArrayList<Engine.Direction> directions = new ArrayList<>();
        directions.add(Engine.Direction.NORTH);
        directions.add(Engine.Direction.SOUTH);
        directions.add(Engine.Direction.EAST);
        directions.add(Engine.Direction.WEST);
        moveValid(directions);
        if (manhattanDistance(hero, this) < 14) {
            path = engine.performSearch(getCoord(), hero.getCoord());
        }
    }

    private void moveValid(ArrayList<Engine.Direction> directions) {
        if (directions.isEmpty()) {
            return;
        }
        int nextMove = rnd.nextInt(directions.size());
        if (!move((directions.get(nextMove)))) {
            directions.remove(nextMove);
            moveValid(directions);
        }
    }

}
