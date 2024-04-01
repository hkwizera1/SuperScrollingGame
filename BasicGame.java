import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Random;

//A Basic version of the scrolling game, featuring Avoids, Gets, and RareGets
//Players must reach a score threshold to win
//If player runs out of HP (via too many Avoid collisions) they lose
public class BasicGame extends AbstractGame {

    //Dimensions of game window
    private static final int DEFAULT_WIDTH = 900;
    private static final int DEFAULT_HEIGHT = 600;

    //Starting Player coordinates
    private static final int STARTING_PLAYER_X = 0;
    private static final int STARTING_PLAYER_Y = 100;

    //Score needed to win the game
    private static final int SCORE_TO_WIN = 300;

    //Maximum that the game speed can be increased to
    //(a percentage, ex: a value of 300 = 300% speed, or 3x regular speed)
    private static final int MAX_GAME_SPEED = 300;
    //Interval that the speed changes when pressing speed up/down keys
    private static final int SPEED_CHANGE = 20;

    private static final String INTRO_SPLASH_FILE = "assets/splash.gif";
    //Key pressed to advance past the splash screen
    public static final int ADVANCE_SPLASH_KEY = KeyEvent.VK_ENTER;

    //Interval that Entities get spawned in the game window
    //ie: once every how many ticks does the game attempt to spawn new Entities
    private static final int SPAWN_INTERVAL = 45;
    //Maximum Entities that can be spawned on a single call to spawnEntities
    private static final int MAX_SPAWNS = 3;


    //A Random object for all your random number generation needs!
    public static final Random rand = new Random();

    //Player's current score
    private int score;

    //Stores a reference to game's Player object for quick reference
    //(This Player will also be in the displayList)
    private Player player;


    public BasicGame() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public BasicGame(int gameWidth, int gameHeight) {
        super(gameWidth, gameHeight);
    }

    //Performs all of the initialization operations that need to be done before the game starts
    protected void preGame() {
        this.setBackgroundColor(Color.BLACK);
        player = new Player(STARTING_PLAYER_X, STARTING_PLAYER_Y);
        displayList.add(player);
        player.setHP(3);
        score = 0;
        setSplashImage(INTRO_SPLASH_FILE);

    }

    //Called on each game tick
    protected void updateGame() {
        if (!isGameOver()) {
            //scroll all scrollable Entities on the game board
            scrollEntities();
            //Spawn new entities only at a certain interval
            if (ticksElapsed % SPAWN_INTERVAL == 0)
                spawnEntities();

            //Update the title text on the top of the window
            setTitleText("HP" + player.getHP() + ", Score:" + score);
            Entity collision = checkCollision(player);
            if (collision != null) {
                handleCollision((Consumable) collision);
            }
        }
    }


    //Scroll all scrollable entities per their respective scroll speeds
    protected void scrollEntities() {
        for (int i = 0; i < displayList.size(); i++) {
            if (!(displayList.get(i) instanceof Player)) {
                ((Scrollable)(displayList.get(i))).scroll();
            }

        }
    }

    //Spawn new Entities on the right edge of the game board
    protected void spawnEntities() {
        Random rand = new Random();

        int number = rand.nextInt(MAX_SPAWNS + 1);
        int width = super.getWindowWidth();
        int height = super.getWindowHeight();

        for (int i = 0; i < number; i++) {
            int randNum = rand.nextInt(11);
            int ycoord;

            if (randNum < 3) {
                Get get = new Get();
                ycoord = rand.nextInt(height - get.getHeight());
                get.setY(ycoord);
                get.setX(width);
                if (checkCollision(get) == null) {
                    displayList.add(get);
                }
            } else if (randNum < 9) {
                Avoid avoid = new Avoid();
                ycoord = rand.nextInt(height - avoid.getHeight());
                avoid.setY(ycoord);
                avoid.setX(width);
                if (checkCollision(avoid) == null)
                    displayList.add(avoid);
            } else {
                RareGet rareget = new RareGet();
                ycoord = rand.nextInt(height - rareget.getHeight());
                rareget.setY(ycoord);
                rareget.setX(width);
                if (checkCollision(rareget) == null) {
                    displayList.add(rareget);
                }
            }
        }
    }


    //Called whenever it has been determined that the Player collided with a consumable
    protected void handleCollision(Consumable collidedWith) {
        //throw new IllegalStateException("Hey 102 Student! You need to implement handleCollision in BasicGame!"); 
        for (int i = 0; i < displayList.size(); i++) {
            if (displayList.get(i) == collidedWith) {
                player.modifyHP(collidedWith.getDamageValue());
                score += collidedWith.getPointsValue();
                if (collidedWith instanceof RareGet) {
                    player.modifyHP(1);
                }
                displayList.remove(i);
            }
        }
    }


    //Called once the game is over, performs any end-of-game operations
    protected void postGame() {
        if (score >= 300) {
            super.setTitleText("GAME OVER - You Win!");
        } else {
            super.setTitleText("GAME OVER- You Lose!");
        }
    }

    //Determines if the game is over or not
    //Game can be over due to either a win or lose state
    protected boolean isGameOver() {
        if (score < 300 && player.getHP() > 0) {
            return false;
        } else if (score >= 300 && player.getHP() > 0) {
            return true;
        }
        return true;
    }

    //Reacts to a single key press on the keyboard
    //Override's AbstractGame's handleKey
    protected void handleKey(int key) {
        //first, call AbstractGame's handleKey to deal with any of the 
        //fundamental key press operations
        super.handleKey(key);
        setDebugText("Key Pressed!: " + KeyEvent.getKeyText(key));
        //if a splash screen is up, only react to the advance splash key
        if (getSplashImage() != null) {
            if (key == ADVANCE_SPLASH_KEY)
                System.out.println("In Handle Key");
            super.setSplashImage(null);
            return;
        }
        if (!isPaused) {
            if (key == RIGHT_KEY && ((player.getX()+player.getWidth()) < DEFAULT_WIDTH)) {
                player.setX(player.getX() + 1);

            }
            if (key == LEFT_KEY && player.getX()>0) {
                player.setX(player.getX() - 1);
            }
            if (key == UP_KEY && player.getY() > 0) {
                player.setY(player.getY() - 1);
            }

            if (key == DOWN_KEY && ((player.getY()+player.getHeight()) < DEFAULT_HEIGHT)) {
                player.setY(player.getY() + 1);
            }

        }
        if (key == KEY_PAUSE_GAME) {
            if (isPaused == true) {
                isPaused = false;
            } else {
                isPaused = true;
            }

        }
        if ((key == SPEED_DOWN_KEY) && (getGameSpeed() > SPEED_CHANGE)) {
            setGameSpeed(getGameSpeed() - SPEED_CHANGE);
        }
        if ((key == SPEED_UP_KEY) && (getGameSpeed() < 300)) {
            setGameSpeed(getGameSpeed() + SPEED_CHANGE);
        }
    }
}