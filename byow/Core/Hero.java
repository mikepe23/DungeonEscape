package byow.Core;

import byow.TileEngine.TETile;

import java.util.Random;
import static java.lang.Character.toLowerCase;

public class Hero extends Player {
    int numKeys;
    int bullets;

    public Hero(Engine e, TETile tile, Coordinate c, Random r, int numKeys) {
        super(e, tile, c, r, 5);
        this.numKeys = numKeys;
        bullets = 10;
    }

    public void takeKey() {
        numKeys--;
    }

    public boolean hasAllKeys() {
        return numKeys == 0;
    }

    @Override
    public void play(char c) {
        keyboardInput(c);
    }


    public int getBullets() {
        return bullets;
    }

    public void setBullets(int n) {
        bullets = n;
    }

    public void keyboardInput(char input) {
        switch (toLowerCase(input)) {
            case 'w':
                move(Engine.Direction.NORTH);
                break;
            case 'a':
                move(Engine.Direction.WEST);
                break;
            case 's':
                move(Engine.Direction.SOUTH);
                break;
            case 'd':
                move(Engine.Direction.EAST);
                break;
            case 'i':
                shootEnemy(Engine.Direction.NORTH);
                break;
            case 'k':
                shootEnemy(Engine.Direction.SOUTH);
                break;
            case 'j':
                shootEnemy(Engine.Direction.WEST);
                break;
            case 'l':
                shootEnemy(Engine.Direction.EAST);
                break;
            case 't':
                // parameter is whether to show the paths
                // or revert
                engine.showPaths(false);
                break;
            default:
                return;
        }
    }

    private void shootEnemy(Engine.Direction dir) {
        if (bullets > 0) {
            bullets -= 1;
            engine.shoot(this, dir);
        }
    }

}
