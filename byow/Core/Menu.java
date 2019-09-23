package byow.Core;

import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.util.HashSet;

public class Menu {

    private int width;
    private int height;
    private Engine engine;
    private String newGameInput;

    public Menu(Engine e, int w, int h) {
        this.width = w;
        this.height = h;
        this.engine = e;
        this.newGameInput = "";
    }

    public void initialize() {
        StdDraw.setCanvasSize(width * 16, height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, width);
        StdDraw.setYscale(0, height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
    }

    public void showMainMenu() {
        int midWidth = width / 2;
        int titleHeight = height * 4 / 5;
        int firstHeight = height / 2;
        int secondHeight = firstHeight - 3;
        int thirdHeight = secondHeight - 3;

        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 30));
        StdDraw.text(midWidth, titleHeight, "Welcome to KP World!");
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 18));
        StdDraw.text(midWidth, firstHeight, "New Game (N)");
        StdDraw.text(midWidth, secondHeight, "Load Game (L)");
        StdDraw.text(midWidth, thirdHeight, "Quit (Q)");
        StdDraw.show();
    }

    public void newGameMenu() {
        int midWidth = width / 2;
        int titleHeight = height * 4 / 5;
        int midHeight = height / 2;


        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(midWidth, titleHeight, "Please Enter Seed, to finish press S");
        StdDraw.text(midWidth, midHeight, newGameInput);
        StdDraw.show();
    }

    public void addChar(char s) {
        newGameInput = newGameInput + s;
    }

    public void run() {
        boolean flag = true;
        while (flag) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                switch (c) {
                    case 'N':
                    case 'n':
                        flag = false;
                        addChar('N');
                        startNewGameMenu();
                        break;
                    case 'Q':
                    case 'q':
                        System.exit(0);
                        break;
                    case 'L':
                    case 'l':
                        flag = false;
                        engine.loadSavedGame("");
                        break;
                    default:
                        break;
                }
            }
            showMainMenu();
        }
    }

    private void startNewGameMenu() {
        boolean flag = true;
        while (flag) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                if (!Character.isDigit(c) && c != 's' & c != 'S') {
                    continue;
                }
                switch (c) {
                    case 'S':
                    case 's':
                        flag = false;
                        break;
                    default:
                        addChar(c);
                }
            }
            newGameMenu();
        }
        if (isSeedValid()) {
            addChar('S');
            engine.runGame(newGameInput, new HashSet(), true, false);
        } else {
            System.out.println("Invalid seed input");
            System.exit(0);
        }
    }

    private boolean isSeedValid() {
        // If input is empty or no digits were inserted for the seed, return false.
        if (newGameInput.length() == 0
                || !Character.isDigit(newGameInput.charAt(newGameInput.length() - 1))) {
            return false;
        }
        return true;
    }

    public void victoryScreen() {
        boolean flag = true;
        while (flag) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                switch (c) {
                    case 'M':
                    case 'm':
                        engine.interactWithKeyboard();
                        flag = false;
                        break;
                    default:
                }
            }
            int midWidth = width / 2;
            int midHeight = height / 2;
            newGameInput = "";

            StdDraw.clear(Color.BLACK);
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.text(midWidth, midHeight, "You win! Press M to the main menu!");
            StdDraw.show();
        }
    }

    public void losingScreen() {
        boolean flag = true;
        while (flag) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                switch (c) {
                    case 'M':
                    case 'm':
                        engine.interactWithKeyboard();
                        flag = false;
                        break;
                    case 'Q':
                    case 'q':
                        System.exit(0);
                        break;
                    default:
                        break;
                }
            }
            int midWidth = width / 2;
            int midHeight = height / 2;
            newGameInput = "";

            StdDraw.clear(Color.BLACK);
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.text(midWidth, midHeight,
                    "You lose. Press Q to quit game or M to start a new one.");
            StdDraw.show();
        }
    }
}
