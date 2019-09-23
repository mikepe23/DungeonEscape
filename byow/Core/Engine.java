package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.HelperFunction.*;
import edu.princeton.cs.introcs.StdDraw;
import static java.lang.Character.toLowerCase;


import java.awt.*;
import java.io.*;
import java.util.*;

public class Engine implements Serializable {
    TERenderer ter = new TERenderer();
    TETile[][] gameGrid;

    enum Direction {
        NORTH, SOUTH, EAST, WEST
    }

    private static final int WIDTH = 70;
    private static final int HEIGHT = 40;
    HashSet<Player> players;
    Hero hero;
    Element door;

    boolean endGame = false;
    boolean heroAlive = true;
    Random rng;
    Portal[] portals;
    HashMap<Integer, Coordinate> floorMap = new HashMap<>();
    HashMap<Integer, Coordinate> wallMap = new HashMap<>();

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */

    public void interactWithKeyboard() {

        /**Random # of adversaries in rooms to walk completely/stochastically at random EACH TURN
         * Add life to hero
         * add a key that opens a door
         * add Heads Up Display **/

        Menu menu = new Menu(this, WIDTH, HEIGHT);
        menu.initialize();
        heroAlive = true;
        endGame = false;
        menu.run();
        if (heroAlive) {
            menu.victoryScreen();
        } else {
            menu.losingScreen();
        }
    }

    private void hud() {
        StdDraw.enableDoubleBuffering();
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(3, 1, "Health: " + hero.health);
        StdDraw.text(9, 1, " Enemies Left: " + players.size());
        StdDraw.text(18, 1, " Keys Left: " + hero.numKeys);
        StdDraw.text(26, 1, " Bullets Left: " + hero.getBullets());
        int x = (int) Math.floor(StdDraw.mouseX());
        int y = (int) Math.floor(StdDraw.mouseY()) - 3;
        if (x < WIDTH && x >= 0 && y < HEIGHT && y >= 0) {
            StdDraw.text(36, 1, gameGrid[x][y].description());
        }
        StdDraw.show();
    }

    public TETile[][] runGame(String input, HashSet ps, boolean isKeyboard, boolean isLoad) {
        String rest = "";

        //ter.initialize(WIDTH, HEIGHT + 3, 0, 3);
        gameGrid = new TETile[WIDTH][HEIGHT];

        if (toLowerCase(input.charAt(0)) == 'l') {
            loadSavedGame(input.substring(1));
        }

        Object[] commands = generateSeed(input);
        // If seed could not be generated, return
        if (commands[0] == null || commands[1] == null) {
            return gameGrid;
        }

        rng = (Random) commands[0];
        int maxGenFactor = (WIDTH + HEIGHT) / 2;
        int minGenFactor = (WIDTH + HEIGHT) / 10;
        int numRooms = (Math.abs(rng.nextInt()) % (maxGenFactor - minGenFactor)) + minGenFactor;
        Room[] rooms = new Room[numRooms];
        createWorld(rooms);
        generateMaps();

        int numKeys = Math.max(4, rng.nextInt(floorMap.size() / 50));
        hero = placeHero(numKeys);
        door = placeElement(wallMap, Tileset.LOCKED_DOOR);
        for (int n = 0; n < numKeys; n++) {
            placeElement(floorMap, Tileset.TREE);
        }
        portals = new Portal[(WIDTH + HEIGHT) / Math.min(WIDTH, HEIGHT)];
        for (int n = 0; n < portals.length; n++) {
            portals[n] = placePortal();
        }

        if (isLoad) {
            rest = (String) commands[1];
            for (int j = 0; j < rest.length(); j++) {
                hero.play(rest.charAt(j));
            }
            players = ps;
            for (Player pl: players) {
                if (pl instanceof Warrior) {
                    placeExistingWarrior((Warrior) pl);
                }
            }
        } else {
            int numOfEnemies = Math.abs(rng.nextInt() % 10) + 1;
            players = new HashSet<>();
            for (int i = 0; i < numOfEnemies; i++) {
                players.add(placeWarrior(hero));
            }
            hero.setBullets(Math.max(0, numOfEnemies * 2 - 3));
        }

        //hud();
        //ter.renderFrame(gameGrid);

        String userInput = input;
        // Only if keyboard is allowed
        if (isKeyboard) {
            doWithKeyboard(userInput);
        } else {
            doWithoutKeyboard(userInput, rest);
        }
        return gameGrid;
    }

    public void doWithKeyboard(String u) {
        char prevChar = 'a';
        while (!endGame) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                if (quitSequence(c, prevChar)) {
                    quitAndSave(u, players);
                }
                hero.play(c);
                // If non-action key pressed, Nothing should happen
                if (actionKeyPressed(c)) {
                    u += c;
                    for (Player w : players) {
                        w.play(c);
                        if (hero.isDead()) {
                            endGame = true;
                            heroAlive = false;
                        }
                    }
                }
                if (c == ':') {
                    prevChar = ':';
                } else {
                    prevChar = 'a';
                }
            }
            //hud();
            //ter.renderFrame(gameGrid);
        }
    }
    public void doWithoutKeyboard(String u, String commands) {
        char prevChar = 'a';
        for (int n = 0; n < commands.length(); n++) {
            char c = commands.charAt(n);
            if (quitSequence(c, prevChar)) {
                quitAndSave(u.substring(0, u.length() - 2), players);
                break;
            }
            if (actionKeyPressed(c)) {
                for (Player w : players) {
                    w.play(c);
                    if (hero.isDead()) {
                        endGame = true;
                        heroAlive = false;
                    }
                }
            }
            if (c == ':') {
                prevChar = ':';
            } else {
                prevChar = 'a';
            }
        }
        //hud();
        //ter.renderFrame(gameGrid);
    }
    /**
     * Prohibits enemies from moving if a non-action key was pressed
     * Checking if a valid key was pressed.
     * @param c
     * @return
     */
    private boolean actionKeyPressed(char c) {
        c = toLowerCase(c);
        if (c == 'a' || c == 'w' || c == 's' || c == 'd'
                || c == 'i' || c == 'l' || c == 'k' || c == 'j') {
            return true;
        }
        return false;
    }

    /**
     * Given all previous inputs and the latest key inserted, check
     * if quit sequence was pressed
     *
     * @param c: very last char inputted by user
     * @param prev: sequence of previous inputs by user.
     * @return returns true if player inputs correct sequence to quit, false if not
     */
    private boolean quitSequence(char c, char prev) {
        return prev == ':' && (c == 'q' || c == 'Q');
    }

    /**
     * Function returns an array with Random object and "rest" string.
     * Used to initialize a word with an appropriate seed.
     *
     * @param input input String
     * @return returns an array of a rng and a string of inputs
     */
    private Object[] generateSeed(String input) {
        Object[] result = new Object[2];
        char prefix = input.charAt(0);
        String seedString = "";
        int i = 1;
        while (Character.isDigit(input.charAt(i))) {
            seedString += input.charAt(i);
            i += 1;
        }
        char endSeedChar = input.charAt(i);
        result[1] = input.substring(i + 1); // "rest string - commands to execute

        // Check prefix, suffix, and that input contains only digits.
        if ((prefix != 'N' && prefix != 'n') || (endSeedChar != 'S'
                && endSeedChar != 's') || !seedString.matches("\\d+")) {
            return result;
        }
        long seed = Long.parseLong(seedString);
        System.out.println("Seed is: " + seed);
        result[0] = new Random(seed);

        return result;
    }

    /**
     * Load saved game by loading the string of inputs previously
     * entered by user and creating a new game with these inputs.
     * @param loadedString: if a previous game was loaded, that is the string
     *                    that's suceeding it.
     */
    // @Source: Editor class
    public void loadSavedGame(String loadedString) {
        String savedInput = "";
        HashSet ps = new HashSet();
        File f = new File("./save.txt");
        if (f.exists()) {
            try {
                FileInputStream fs = new FileInputStream(f);
                ObjectInputStream os = new ObjectInputStream(fs);
                ArrayList<Object> woi = (ArrayList<Object>) os.readObject();
                savedInput = (String) woi.get(0);
                ps = (HashSet) woi.get(1);
            } catch (FileNotFoundException e) {
                System.out.println("file not found");
                System.exit(0);
            } catch (IOException e) {
                System.out.println(e);
                System.exit(0);
            } catch (ClassNotFoundException e) {
                System.out.println("class not found");
                System.exit(0);
            }
            boolean keyboard = true;
            if (loadedString.length() > 0) {
                keyboard = false;
            }
            System.out.println(loadedString);
            runGame(savedInput + loadedString, ps, keyboard, true);
            // run startNewGame running the "rest" commands

        }
    }

    /**
     * Save file by writing the string of input sequence to a file.
     *
     * @param s: string of all user input including seed
     */
    // @Source: Editor class
    private void quitAndSave(String s, HashSet<Player> ps) {
        File f = new File("./save.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            ArrayList<Object> woi = new ArrayList<>();
            woi.add(s);
            woi.add(ps);
            os.writeObject(woi);
            System.out.println("file saved successfully, thanks for playing!");
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
            //System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
            //System.exit(0);
        }
        // System.exit(0);
    }

    /**
     * Find a valid coordinate to place the hero, initialize at this coordinate
     * and return
     *
     * @return a new Hero
     */
    private Hero placeHero(int numKeys) {
        int place = rng.nextInt(floorMap.size());
        Coordinate coord = floorMap.get(place);
        gameGrid[coord.getX()][coord.getY()] = Tileset.INDIANA;
        return new Hero(this, Tileset.INDIANA, coord.copy(), rng, numKeys);
    }

    /**
     * Finds a valid coordinate to place element on given set of tiles
     *
     * @param tiles Valid tiles to initialize elements on
     * @param tile  tile to represent the element
     * @return returns the element
     */
    private Element placeElement(HashMap<Integer, Coordinate> tiles, TETile tile) {
        int place = rng.nextInt(tiles.size());
        Coordinate coord = tiles.get(place);
        gameGrid[coord.getX()][coord.getY()] = tile;
        return new Element(tile, coord.copy());
    }

    /**
     * Find two valid walls to place portals
     *
     * @return returns a portal
     */
    private Portal placePortal() {
        int place1 = rng.nextInt(wallMap.size());
        int place2 = rng.nextInt(wallMap.size());

        Coordinate coord1 = wallMap.get(place1);
        Coordinate coord2 = wallMap.get(place2);
        gameGrid[coord1.getX()][coord1.getY()] = Tileset.WATER;
        gameGrid[coord2.getX()][coord2.getY()] = Tileset.WATER;
        return new Portal(Tileset.WATER, coord1.copy(), coord2.copy());
    }

    /**
     * Find a valid coordinate to place a warrior, initialize at this coordinate
     * and return
     *
     * @return a new Hero
     */
    private Warrior placeWarrior(Hero h) {
        int place = rng.nextInt(floorMap.size());
        Coordinate coord = floorMap.get(place);
        gameGrid[coord.getX()][coord.getY()] = Tileset.WARRIOR;
        return new Warrior(this, Tileset.WARRIOR, coord.copy(), rng, h);
    }

    private void placeExistingWarrior(Warrior w) {
        gameGrid[w.getX()][w.getY()] = Tileset.WARRIOR;
    }
    /**
     * Hero can shoot enemies in a range of 3 steps.  Hits with .66 chance.
     * Effectively, function will look for a range of 3 tiles in the
     * direction specified, and if a warrior is spotted, it will hit them.
     *
     * @param p - hero
     * @return If some creature was shot or not
     */
    public boolean shoot(Player p, Direction dir) {
        int range = 10;
        double probability = 1;
        // shot misses with 0 probability
        if (rng.nextDouble() > probability) {
            return false;
        }

        boolean hit = false;
        switch (dir) {
            case NORTH:
                for (int i = 1; i < range + 1; i++) {
                    if (p.getY() + i > HEIGHT - 1) {
                        break;
                    } else if (gameGrid[p.getX()][p.getY() + i] == Tileset.WARRIOR) {
                        hitWarrior(p.getX(), p.getY() + i);
                        hit = true;
                    }
                }
                break;
            case SOUTH:
                for (int i = 1; i < range + 1; i++) {
                    if (p.getY() - i < 0) {
                        break;
                    } else if (gameGrid[p.getX()][p.getY() - i] == Tileset.WARRIOR) {
                        hitWarrior(p.getX(), p.getY() - i);
                        hit = true;
                    }
                }
                break;
            case EAST:
                for (int i = 1; i < range + 1; i++) {
                    if (p.getX() + i > WIDTH - 1) {
                        break;
                    } else if (gameGrid[p.getX() + i][p.getY()] == Tileset.WARRIOR) {
                        hitWarrior(p.getX() + i, p.getY());
                        hit = true;
                    }
                }
                break;
            case WEST:
                for (int i = 1; i < range + 1; i++) {
                    if (p.getX() - i < 0) {
                        break;
                    } else if (gameGrid[p.getX() - i][p.getY()] == Tileset.WARRIOR) {
                        hitWarrior(p.getX() - i, p.getY());
                        hit = true;
                    }
                }
                break;
            default:
                return hit;
        }
        return hit;
    }

    // Hit a warrior at x, y.
    private void hitWarrior(int x, int y) {
        Iterator<Player> it = players.iterator();
        while (it.hasNext()) {
            Player w = it.next();
            if (w instanceof Warrior && w.getX() == x && w.getY() == y) {
                w.takeHit();
                if (w.isDead()) {
                    gameGrid[x][y] = Tileset.FLOOR;
                    it.remove();
                }
            }
        }
    }

    /**
     * Moves character to another adjacent tile given move direction and successfully moves if
     * coordinate is a floor. Update character's position if move is successful. Assumes coordinate
     * moving to is always in bounds of the grid
     *
     * @param moveX Change in coordinate in X direction
     * @param moveY Change in coordinate in Y direction
     */
    public boolean move(int moveX, int moveY, Player p) {
        int pX = p.getX();
        int pY = p.getY();
        Coordinate coord = new Coordinate(pX + moveX, pY + moveY);
        if (p.getID() == Tileset.INDIANA) {
            Hero indy = (Hero) p;
            if (!indy.hasAllKeys()) {
                pickUpKey(pX + moveX, pY + moveY);
                if (indy.hasAllKeys()) {
                    door.id = Tileset.UNLOCKED_DOOR;
                    gameGrid[door.getX()][door.getY()] = door.id;
                }
            } else {
                if (checkBoundary(new Room(Tileset.UNLOCKED_DOOR, pX, pY, 1, 1),
                        Tileset.UNLOCKED_DOOR)) {
                    endGame = true;
                }
            }
        }
        for (int n = 0; n < portals.length; n++) {
            if (portals[n].equals(coord)) {
                return portals[n].transport(coord, p);
            }
        }
        if (gameGrid[pX + moveX][pY + moveY] == Tileset.FLOOR) {
            gameGrid[pX + moveX][pY + moveY] = p.getID();
            gameGrid[pX][pY] = Tileset.FLOOR;
            p.setCoords(pX + moveX, pY + moveY);
            return true;
        }
        return false;
    }

    /**
     * If the coordinate to be moved to is a key then have the hero pick it up
     *
     * @param x    x-Coordinate
     * @param y    y-Coordinate
     */
    private void pickUpKey(int x, int y) {
        if (gameGrid[x][y] == Tileset.TREE) {
            hero.takeKey();
            gameGrid[x][y] = Tileset.FLOOR;
        }
    }

    private Coordinate convertToCoordinate(int n) {
        int x = n / HEIGHT;
        int y = n % HEIGHT;
        return new Coordinate(x, y);
    }

    private int convertToInt(Coordinate c) {
        return c.getX() * HEIGHT + c.getY();
    }

    /**
     * Show the paths of all warriors to hero.
     * @param active: When false, the path would show. When true, revert
     *              back to the original world.
     */
    public void showPaths(boolean active) {
        for (Player w: players) {
            if (w instanceof Warrior) {
                LinkedList<Coordinate> path = ((Warrior) w).getPath();
                if (!path.isEmpty()) {
                    colorPath(path, active);
                }
            }
        }
        ter.renderFrame(gameGrid);
        while (!active) {
            char c = 'a';
            if (StdDraw.hasNextKeyTyped()) {
                c = StdDraw.nextKeyTyped();
            }
            if (toLowerCase(c) == 't') {
                active = true;
                showPaths(active);
            }
        }
    }

    private void colorPath(LinkedList<Coordinate> path, boolean active) {
        TETile tile;
        if (active) {
            tile = Tileset.FLOOR;
        } else {
            tile = Tileset.SAND;
        }
        if (path.isEmpty()) {
            return;
        }
        // We ignore the hero tile.
        path.removeLast();

        while (!path.isEmpty()) {
            Coordinate c = path.removeFirst();
            gameGrid[c.getX()][c.getY()] = tile;
        }
    }

    public ArrayList<Coordinate> performSearch(Coordinate begin, Coordinate finish) {
        int start = convertToInt(begin);
        int end = convertToInt(finish);

        ArrayHeapMinPQ<Integer> fringe = new ArrayHeapMinPQ<>();
        HashMap<Integer, Double> distTo = new HashMap<>();
        HashMap<Integer, Integer> edgeTo = new HashMap<>();
        HashSet<Integer> visited = new HashSet<>();

        fringe.add(start, 0);
        distTo.put(start, 0.0);

        while (fringe.size() > 0) {
            int c = fringe.removeSmallest();
            visited.add(c);

            if (c == end) {
                return extractPath(edgeTo, start, end);
            }
            for (int n : getNeighbors(c)) {
                relax(fringe, edgeTo, distTo, c, n, visited);
            }
        }
        return new ArrayList<>(); // if search failed, return empty array
    }

    private void relax(ArrayHeapMinPQ<Integer> pQ, HashMap<Integer, Integer> path,
                       HashMap<Integer, Double> distTo, int from, int to,
                       HashSet<Integer> visited) {
        if (distTo.get(from) == null) {
            return;
        } else if ((distTo.get(to) == null || distTo.get(from) + 1 < distTo.get(to))
                && !visited.contains(to)) {
            distTo.put(to, distTo.get(from) + 1);
            path.put(to, from);
            if (!pQ.contains(to)) {
                pQ.add(to, distTo.get(to));
            }
        }
    }

    public ArrayList<Coordinate> extractPath(HashMap<Integer, Integer> edgeTo,
                                             Integer start, Integer end) {
        ArrayList<Coordinate> path = new ArrayList<>();
        Stack<Integer> s = new Stack<>();
        int c = end;
        while (c != start) {
            s.push(c);
            c = edgeTo.get(c);
        }
        while (!s.isEmpty()) {
            path.add(convertToCoordinate(s.pop()));
        }
        return path;
    }

    private ArrayList<Integer> getNeighbors(int val) {
        Coordinate c = convertToCoordinate(val);
        ArrayList<Integer> result = new ArrayList<>();
        try {
            if (c.getX() + 1 < WIDTH && gameGrid[c.getX() + 1][c.getY()] != Tileset.WALL
                    && gameGrid[c.getX() + 1][c.getY()] != Tileset.TREE
                    && gameGrid[c.getX() + 1][c.getY()] != Tileset.WARRIOR) {
                result.add(convertToInt(new Coordinate(c.getX() + 1, c.getY())));
            }
            if (c.getX() - 1 >= 0 && gameGrid[c.getX() - 1][c.getY()] != Tileset.WALL
                    && gameGrid[c.getX() - 1][c.getY()] != Tileset.TREE
                    && gameGrid[c.getX() - 1][c.getY()] != Tileset.WARRIOR) {
                result.add(convertToInt(new Coordinate(c.getX() - 1, c.getY())));
            }
            if (c.getY() + 1 < HEIGHT && gameGrid[c.getX()][c.getY() + 1] != Tileset.WALL
                    && gameGrid[c.getX()][c.getY() + 1] != Tileset.TREE
                    && gameGrid[c.getX()][c.getY() + 1] != Tileset.WARRIOR) {
                result.add(convertToInt(new Coordinate(c.getX(), c.getY() + 1)));
            }
            if (c.getY() - 1 >= 0 && gameGrid[c.getX()][c.getY() - 1] != Tileset.WALL
                    && gameGrid[c.getX()][c.getY() - 1] != Tileset.TREE
                    && gameGrid[c.getX()][c.getY() - 1] != Tileset.WARRIOR) {
                result.add(convertToInt(new Coordinate(c.getX(), c.getY() - 1)));
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Search Error " + e);
            return new ArrayList<>();
        }
        return result;
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * <p>
     * In other words, both of these calls:
     * - interactWithInputString("n123sss:q")
     * - interactWithInputString("lww")
     * <p>
     * should yield the exact same world state as:
     * - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.

        return runGame(input, new HashSet(), false, true);
    }

    /**
     * 1) Initialize all tiles to NOTHING
     * 2) Add the number of Rooms based off passed in Room array size
     * 3) Room size height and width are limited to the sum of the
     * HEIGHT and WIDTH over 16 CAN CHANGE
     * 4) Connect initialized room wiht previously initialized room if it is not already connected
     *
     * @param rooms Room to create.
     */
    private void createWorld(Room[] rooms) {
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                gameGrid[x][y] = Tileset.NOTHING;
            }
        }

        int limit = (WIDTH + HEIGHT) / 16;
        TETile roomTile = Tileset.FLOOR;
        TETile hallwayTile = Tileset.FLOOR;
        for (int n = 0; n < rooms.length; n++) {
            int width = Math.abs(rng.nextInt()) % limit + 2;
            int height = Math.abs(rng.nextInt()) % limit + 2;
            rooms[n] = addRoom(width, height, roomTile);
            if (n != 0 && !checkBoundary(rooms[n], roomTile)
                    && !checkBoundary(rooms[n], hallwayTile)) {
                connectRooms(rooms[n - 1], rooms[n], hallwayTile);
            }
        }
    }

    /**
     * Checks borders if any tile equals the tile parameter
     *
     * @param room Room to check border on
     * @param tile Tile to look for around the room
     * @return Returns true if tile found, false if not
     */
    private boolean checkBoundary(Room room, TETile tile) {
        int roomX = room.getX();
        int roomY = room.getY();
        int roomHeight = room.getHeight();
        int roomWidth = room.getWidth();

        int lowerBoundX = Math.max(0, roomX - 1);
        int upperBoundX = Math.min(roomX + roomWidth, WIDTH - 1);
        int lowerBoundY = Math.max(0, roomY - 1);
        int upperBoundY = Math.min(roomY + roomHeight, HEIGHT - 1);
        for (int col = roomX; col < upperBoundX; col++) {
            if (gameGrid[col][lowerBoundY] == tile || gameGrid[col][upperBoundY] == tile) {
                return true;
            }
        }
        for (int row = roomY; row < upperBoundY; row++) {
            if (gameGrid[lowerBoundX][row] == tile || gameGrid[upperBoundX][row] == tile) {
                return true;
            }
        }
        return false;
    }

    /**
     * 1) Set all points within area to be a FLOOR
     * 2) Bound area with WALL if tile is NOTHING
     *
     * @param lLX    Lower Left X-coordinate
     * @param lLY    Lower Left Y-coordinate
     * @param width  Width of the are to be created
     * @param height Height of the area to be created
     */
    private void addArea(int lLX, int lLY,
                         int width, int height, TETile tile) {
        for (int row = Math.max(1, lLY); row < lLY + height && row < HEIGHT - 1; row++) {
            for (int col = Math.max(1, lLX); col < lLX + width && col < WIDTH - 1; col++) {
                gameGrid[col][row] = tile;
            }
        }

        int lowerBoundX = Math.max(0, lLX - 1);
        int upperBoundX = Math.min(lLX + width, WIDTH - 1);
        int lowerBoundY = Math.max(0, lLY - 1);
        int upperBoundY = Math.min(lLY + height, HEIGHT - 1);

        for (int col = lowerBoundX; col < upperBoundX + 1; col++) {
            if (gameGrid[col][lowerBoundY] == Tileset.NOTHING) {
                gameGrid[col][lowerBoundY] = Tileset.WALL;
            }
            if (gameGrid[col][upperBoundY] == Tileset.NOTHING) {
                gameGrid[col][upperBoundY] = Tileset.WALL;
            }
        }

        for (int row = lowerBoundY; row < upperBoundY + 1; row++) {
            if (gameGrid[lowerBoundX][row] == Tileset.NOTHING) {
                gameGrid[lowerBoundX][row] = Tileset.WALL;
            }
            if (gameGrid[upperBoundX][row] == Tileset.NOTHING) {
                gameGrid[upperBoundX][row] = Tileset.WALL;
            }
        }
    }

    /**
     * 1) Get two random numbers for lower left corner point to create area on
     * //TODO ENSURE ROOM AREA LIES COMPLETELY WITHIN GRID
     * 2) Set all points within area to be a FLOOR
     * 3) Bound area with WALL if tile is NOTHING
     *
     * @param width  Width of the are to be created
     * @param height Height of the area to be created
     * @return Returns initialized Room object
     */
    private Room addRoom(int width, int height, TETile tile) {
        int lLX = Math.max(1, Math.abs(rng.nextInt()) % (WIDTH - width - 1));
        int lLY = Math.max(1, Math.abs(rng.nextInt()) % (HEIGHT - height - 1));
        addArea(lLX, lLY, width, height, tile);
        return new Room(tile, lLX, lLY, width, height);
    }

    /**
     * Connects the given rooms by creating hallways
     * Creates hallways based off orientation given by RNG
     * if orientation is true, create hallways in a mirrored L shape or upside-down mirrored L shape
     * else create hallways in a create hallways in a L shape or upside-down L shape
     *
     * @param room1 room to connect
     * @param room2 other room to connect
     */
    private void connectRooms(Room room1, Room room2, TETile tile) {
        Room leftMost = (room1.getX() < room2.getX()) ? room1 : room2;
        Room rightMost = (leftMost == room1) ? room2 : room1;

        boolean orientation = rng.nextBoolean();
        if (orientation) {
            addHallway(leftMost, rightMost, tile);
        } else {
            addHallway(rightMost, leftMost, tile);
        }
    }

    /**
     * Add hallways connecting the two rooms
     * Length limits are set to the horizontal/vertical
     * distance respectively with added lengths based on room sizes
     *
     * @param room1 room to add hallway to
     * @param room2 other room to add hallway to
     */
    private void addHallway(Room room1, Room room2, TETile tile) {
        int widthGive = Math.max(room1.getWidth(), room2.getWidth());
        int heightGive = Math.max(room1.getHeight(), room2.getHeight());
        int widthLimit = Math.abs(room1.getX() - room2.getX()) + widthGive;
        int heightLimit = Math.abs(room1.getY() - room2.getY()) + heightGive;

        int Y = (Math.abs(rng.nextInt()) % room1.getHeight()) + room1.getY();
        int X = (Math.abs(rng.nextInt()) % room2.getWidth()) + room2.getX();

        if (room1.getX() > room2.getX()) {
            addArea(Math.max(1, room1.getX() - widthLimit), Y, widthLimit, 1, tile);
        } else {
            addArea(room1.getX() + room1.getWidth(), Y, widthLimit, 1, tile);
        }
        if (Y < room2.getY()) {
            addArea(X, Math.max(1, room2.getY() - heightLimit), 1, heightLimit, tile);
        } else {
            addArea(X, Math.max(1, Y - heightLimit), 1, heightLimit, tile);
        }
    }

    /**
     * Generates floorMap and wallMap. All points in the grid that are floors are added in the
     * floorMap. All points in the grid that are walls and is adjacent to a floor is added to the
     * wallMap.
     */
    private void generateMaps() {
        floorMap.clear();
        wallMap.clear();
        for (int col = 0; col < gameGrid.length; col++) {
            for (int row = 0; row < gameGrid[0].length; row++) {
                if (gameGrid[col][row] == Tileset.FLOOR) {
                    floorMap.put(floorMap.size(), new Coordinate(col, row));
                } else if (gameGrid[col][row] == Tileset.WALL
                        && checkBoundary(new Room(Tileset.FLOOR, col, row, 1, 1), Tileset.FLOOR)
                        && checkBoundary(new Room(Tileset.NOTHING, col, row, 1, 1),
                        Tileset.NOTHING)) {
                    wallMap.put(wallMap.size(), new Coordinate(col, row));
                }
            }
        }
    }
}
