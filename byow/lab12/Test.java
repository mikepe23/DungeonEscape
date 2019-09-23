//package byow.lab12;
//
//import byow.Core.Room;
//import byow.TileEngine.TERenderer;
//import byow.TileEngine.TETile;
//import byow.TileEngine.Tileset;
//
//import java.util.Random;
//
//
//public class Test {
//    private static final int WIDTH = 60;
//    private static final int HEIGHT = 60;
//
//    private static final long SEED = 287313;
//    private static final Random RANDOM = new Random(SEED);
//
//
//    public static void main(String[] args) {
//        TERenderer ter = new TERenderer();
//        ter.initialize(WIDTH, HEIGHT);
//        TETile[][] world = new TETile[WIDTH][HEIGHT];
//
//        int numRooms = Math.max((WIDTH + HEIGHT) / 10, Math.abs(RANDOM.nextInt()) % ((WIDTH + HEIGHT) / 2));
//        int numHalls = Math.abs(RANDOM.nextInt()) % ((WIDTH + HEIGHT) / 2);
//        Room[] rooms = new Room[numRooms];
//        //ArrayList<Hallway> hallways = new ArrayList<>();
//        createWorld(world, rooms);//, hallways);
//        ter.renderFrame(world);
//    }
//
//    /**
//     * 1) Initialize all tiles to NOTHING
//     * 2) Add the number of Rooms based off passed in Room array size
//     * 3) Room size height and width are limited to the sum of the HEIGHT and WIDTH over 16 CAN CHANGE
//     * 4) Connect initialized room wiht previously initialized room if it is not already connected
//     * //TODO CREATE HALLWAY OBJECTS?
//     * @param grid World to create on.
//     * @param rooms Room to create.
//     */
//    public static void createWorld(TETile[][] grid, Room[] rooms) {//, ArrayList<Hallway> hallways) {
//        for (int x = 0; x < WIDTH; x += 1) {
//            for (int y = 0; y < HEIGHT; y += 1) {
//                grid[x][y] = Tileset.NOTHING;
//            }
//        }
//
//        int limit = ((WIDTH + HEIGHT) / 16);
//        for (int n = 0; n < rooms.length; n++) {
//            int width = Math.abs(RANDOM.nextInt()) % limit + 2;
//            int height = Math.abs(RANDOM.nextInt()) % limit + 2;
//            rooms[n] = addRoom(grid, width, height);
//            if (n != 0 && !checkBoundary(grid, rooms[n], Tileset.FLOOR)) {
//                connectRooms(grid, rooms[n - 1], rooms[n]);
//            }
//        }
//    }
//
//    /**
//     * Checks borders if any tile equals the tile parameter
//     * @param grid Grid to check on
//     * @param room Room to check border on
//     * @param tile Tile to look for around the room
//     * @return Returns true if tile found, false if not
//     */
//    public static boolean checkBoundary(TETile[][] grid, Room room, TETile tile) {
//        for (int col = room.x; col < room.x + room.width && col < WIDTH; col++) {
//            if ((room.y - 1 >= 0 && grid[col][room.y - 1] == tile) ||
//                    (room.y + room.height < HEIGHT && grid[col][room.y + room.height] == tile)) {
//                return true;
//            }
//        }
//        for (int row = room.y; row < room.y + room.height && row < grid.length; row++) {
//            if ((room.x - 1 >= 0 && grid[room.x - 1][row] == tile) ||
//                    (room.x + room.width < WIDTH && grid[room.x + room.width][row] == tile)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * 1) Set all points within area to be a FLOOR
//     * 2) Bound area with WALL if tile is NOTHING
//     *
//     * @param grid   World to create on
//     * @param LLX    Lower Left X-coordinate
//     * @param LLY    Lower Left Y-coordinate
//     * @param width  Width of the are to be created
//     * @param height Height of the area to be created
//     */
//    public static void addArea(TETile[][] grid, int LLX, int LLY, int width, int height) {
//        for (int row = LLY; row < LLY + height && row < HEIGHT; row++) {
//            for (int col = LLX; col < LLX + width && col < WIDTH; col++) {
//                grid[col][row] = Tileset.FLOOR;
//            }
//        }
//        for (int col = Math.max(0, LLX - 1); col < WIDTH && col < LLX + width + 1; col++) {
//            System.out.println(LLY - 1);
//            if (LLY - 1 >= 0 && grid[col][LLY - 1] == Tileset.NOTHING) {
//                grid[col][LLY - 1] = Tileset.WALL;
//            }
//            if (LLY + height < HEIGHT && grid[col][LLY + height] == Tileset.NOTHING) {
//                grid[col][LLY + height] = Tileset.WALL;
//            }
//        }
//
//        for (int row = LLY; row < LLY + height && row < HEIGHT; row++) {
//            if (LLX - 1 >= 0 && grid[LLX - 1][row] == Tileset.NOTHING) {
//                grid[LLX - 1][row] = Tileset.WALL;
//            }
//            if (LLX + width < WIDTH && grid[LLX + width][row] == Tileset.NOTHING) {
//                grid[LLX + width][row] = Tileset.WALL;
//            }
//        }
//    }
//
//    /**
//     * 1) Get two random numbers for lower left corner point to create area on
//     * //TODO ENSURE ROOM AREA LIES COMPLETELY WITHIN GRID
//     * 2) Set all points within area to be a FLOOR
//     * 3) Bound area with WALL if tile is NOTHING
//     *
//     * @param grid   World to create on
//     * @param width  Width of the are to be created
//     * @param height Height of the area to be created
//     * @return Returns initialized Room object
//     */
//    public static Room addRoom(TETile[][] grid, int width, int height) {
//        int LLX = Math.max(1, Math.abs(RANDOM.nextInt()) % (WIDTH - width - 1));
//        int LLY = Math.max(1, Math.abs(RANDOM.nextInt()) % (HEIGHT - height - 1));
//        addArea(grid, LLX, LLY, width, height);
//        return new Room(LLX, LLY, width, height, -1);
//    }
//
//    /**
//     * Connects the given rooms by creating hallways
//     * Creates hallways based off orientation given by RNG
//     * if orientation is true, create hallways in a mirrored L shape or upside-down mirrored L shape
//     * else create hallways in a create hallways in a L shape or upside-down L shape
//     *
//     * @param grid
//     * @param room1
//     * @param room2
//     */
//    public static void connectRooms(TETile[][] grid, Room room1, Room room2) {
//        Room leftMost = (room1.x < room2.x) ? room1 : room2;
//        Room rightMost = (leftMost == room1) ? room2 : room1;
//
//        boolean orientation = RANDOM.nextBoolean();
//        if (orientation) {
//            addHallway(grid, leftMost, rightMost);
//        } else {
//            addHallway(grid, rightMost, leftMost);
//        }
//    }
//
//    /**
//     * Add hallways connecting the two rooms
//     * Length limits are set to the horizontal/vertical distance respectively with added lengths based on room sizes
//     *
//     * @param grid
//     * @param room1
//     * @param room2
//     */
//    public static void addHallway(TETile[][] grid, Room room1, Room room2) {
//        int widthLimit = Math.abs(room1.x - room2.x) + (Math.abs(RANDOM.nextInt()) % Math.max(room1.width, room2.width));
//        int heightLimit = Math.abs(room1.y - room2.y) + (Math.abs(RANDOM.nextInt()) % Math.max(room1.height, room2.height));
//
//        int Y = (Math.abs(RANDOM.nextInt()) % room1.height) + room1.y;
//        int X = (Math.abs(RANDOM.nextInt()) % room2.width) + room2.x;
//
//        if (room1.x > room2.x) {
//            addArea(grid, Math.max(0, room1.x - widthLimit), Y, widthLimit, 1);
//        } else {
//            addArea(grid, room1.x + room1.width, Y, widthLimit, 1);
//        }
//        if (room1.y < room2.y) {
//            addArea(grid, X, Math.max(0, room2.y - heightLimit), 1, heightLimit);
//        } else {
//            addArea(grid, X, Math.max(0, Y - heightLimit), 1, heightLimit);
//        }
//    }
//}
