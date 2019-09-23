package byow.TileEngine;

import java.awt.Color;
import java.io.Serializable;

/**
 * Contains constant tile objects, to avoid having to remake the same tiles in different parts of
 * the code.
 *
 * You are free to (and encouraged to) create and add your own tiles to this file. This file will
 * be turned in with the rest of your code.
 *
 * Ex:
 *      world[x][y] = Tileset.FLOOR;
 *
 * The style checker may crash when you try to style check this file due to use of unicode
 * characters. This is OK.
 */

public class Tileset implements Serializable {
    public static final TETile AVATAR = new TETile('@', Color.white, Color.black, "You");
    public static final TETile WALL = new TETile('#', new Color(216, 128, 128), Color.darkGray,
            "Wall");
    public static final TETile FLOOR = new TETile('·', new Color(128, 192, 128), Color.black,
            "Floor");
    public static final TETile NOTHING = new TETile(' ', Color.black, Color.black, "Nothing");
    public static final TETile GRASS = new TETile('"', Color.green, Color.black, "Grass");
    public static final TETile WATER = new TETile('≈', Color.blue, Color.black, "Portal");
    public static final TETile FLOWER = new TETile('❀', Color.magenta, Color.pink, "Flower");
    public static final TETile LOCKED_DOOR = new TETile('█', Color.orange, Color.black,
            "Locked Door");
    public static final TETile UNLOCKED_DOOR = new TETile('▢', Color.orange, Color.black,
            "Unlocked Door");
    public static final TETile SAND = new TETile('▒', Color.yellow, Color.black, "Sand");
    public static final TETile MOUNTAIN = new TETile('▲', Color.gray, Color.black, "Mountain");
    public static final TETile TREE = new TETile('♠', Color.green, Color.black, "Key");
    public static final TETile INDIANA = new TETile('^', Color.red, Color.black, "Indiana",
            "byow/graphics/indiana.png");
    public static final TETile WARRIOR = new TETile('*', Color.blue, Color.black, "Warrior",
            "byow/graphics/warrior.png");
}


