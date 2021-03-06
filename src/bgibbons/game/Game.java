package bgibbons.game;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.BufferStrategy;
import java.awt.Graphics;

import java.util.Random;

import javax.swing.JFrame;

import bgibbons.game.entities.*;
import bgibbons.game.graphics.Colors;
import bgibbons.game.graphics.Font;
import bgibbons.game.graphics.HUD;
import bgibbons.game.graphics.Screen;
import bgibbons.game.graphics.SpriteSheet;
import bgibbons.game.level.Level;
/**
 * Main class for the game engine.
 * @author Brad Gibbons
 * @author Jackson Schilmoeller
 * @author Rony Singh
 * @author Chris Porras
 * @version 1.1 0 December 2016
 */
public class Game extends Canvas implements Runnable
{
	private static final long serialVersionUID = 1L;

	public static final int WIDTH = 160;				// Width of the image to be displayed
	public static final int HEIGHT = WIDTH*4/5;			// Height of the image to be displayed
	public static final int SCALE = 3;					// Scale of the image to be displayed
	public static final String NAME = "Game";			// Name to displayed for the JFrame

	private JFrame frame;	// Declare JFrame object

	public boolean running = false;	// Variable to track if the game is running
	public int tickCount = 0;		// Variable to track the tick count

	private BufferedImage image = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);	// Initialize the Buffered image with a set width, height, and type
	private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();		// An array to hold the pixels of the image
	private int[] colors = new int[6*6*6];														// An array of the colors available to use for the image

	private long startTime;
	private long endTime;

	private Screen screen;		// Declare the Screen object.
	public InputHandler input;	// Decleare the InputHandler object.
	public Level main_level;	// Declare the Level object.
	public Level combatLevel;	// Declare the combat level object.
	public Level dungeon1;		// Declare the first dungeon level.
	public Level dungeon2;		// Declare the second dungeon level.
	public Combat combat; 		// Declare the combat object.
	public Player player;		// Declare the Player object.
	public Menu menu;			// Declare the Menu object.
	public Sound sound;			// Declare the Sound object.
	public Sound lootFX;  	// Declare a sound object for the loot.
	public enum States {START, CLASSES, RUNNING, PAUSED, COMBAT, POSTCOMBAT, OVER}
	public States state;
	public boolean boss = false;
	private int drop;	//Determines the loot to be dropped.
	private int rng1;	//Determines the vit stat of the loot.
	private int rng2;	//Determines the int stat of the loot.
	private int rng3;	//Determines the dex stat of the loot.
	/**
	 * Constructor for the Game object to initialize the JFrame.
	 */
	public Game()
	{

		setMinimumSize(new Dimension(WIDTH*SCALE, HEIGHT*SCALE));
		setMaximumSize(new Dimension(WIDTH*SCALE, HEIGHT*SCALE));
		setPreferredSize(new Dimension(WIDTH*SCALE, HEIGHT*SCALE));

		frame = new JFrame(NAME);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		frame.add(this, BorderLayout.CENTER);
		frame.pack();

		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	/*
	 * Method to initialize the Game object's properties.
	 */
	public void init()
	{
		int index = 0;
		for (int r=0; r<6; r++) {
			for (int g=0; g<6; g++) {
				for (int b=0; b<6; b++) {
					int rr = (r * 255/5);
					int gg = (g * 255/5);
					int bb = (b * 255/5);

					colors[index++] = rr << 16 | gg << 8 | bb;
				}
			}
		}

		screen = new Screen(WIDTH, HEIGHT, new SpriteSheet("/res/sprite_sheet.png"));	// Initialize the Screen with the width and height specified above and use the sprite sheet in the res/ folder.
		input = new InputHandler(this);													// Initialize the InputHandler to interact with the Game.
		main_level = new Level("/res/levels/main_level.png", "/res/entities/main_level.png", true);						// Initialize the Level object with the map and entities to be added on startup.
		combatLevel = new Level("/res/levels/combat_level.png", null, false);			// Initialize the combat level object with the map, but no entities.
		dungeon1 = new Level(null, null, false);												// Initialize the first dungeon level, map and entities to be added procedurally
		dungeon1.spawn(1, 40);
		dungeon2 = new Level(null, null, false);												// Initialize the second dungeon level, map and entities to be added procedurally
		dungeon2.spawn(2, 40);
		player = new Player(main_level, 16, main_level.height*8/2, input);				// Initialize the Player object with the level at the set coordinates interacting with the input handler.
		main_level.addEntity(player);													// Add the player to the level.
		menu = new Menu(input);															// Initialize the Menu object with the input handler.
		state = States.START;
		sound = new Sound("/res/sounds/BGM.wav"); 		//Intialize BGM sound object with path.
		lootFX = new Sound("/res/sounds/World/Pick.wav"); //Loot pickup SFX.
		//sound.play();				//Play the sound.
		for(int i = 0; i < main_level.area2Orcs.size(); i++){
			main_level.area2Orcs.get(i).setRank(5);
		}
		for(int i = 0; i < main_level.area3Orcs.size(); i++){
			main_level.area3Orcs.get(i).setRank(10);
		}
		startTime = System.currentTimeMillis();
	}

	/**
	 * Method to close the JFrame and stop the game.
	 */
	public void close() {
		frame.setVisible(false);
		frame.dispose();
		System.exit(0);
	}

	/**
	 * Method to start a thread to run the game.
	 */
	public synchronized void start() {
		running = true;
		new Thread(this).start();
	}

	/**
	 * Method to stop the the game.
	 */
	public synchronized void stop() {
		running = false;
	}

	/**
	 * Handles the running of the Game by rendering the level and performing game ticks.
	 */
	public void run() {
		long lastTime = System.nanoTime();	// Gets the current system time in nano seconds
		double nsPerTick = 1000000000D/60D;	//Sets the number of nano seconds per tick

		int ticks = 0;	// Initialize the number of ticks ran
		int frames = 0;	// Initialize the number of frames rendered

		double delta = 0;	// Time until next system tick

		init();

		while(running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / nsPerTick;
			lastTime = now;
			boolean shouldRender = true;

			while(delta >= 1) { // Limit the ticks per second
				ticks++;
				tick();
				delta --;
				shouldRender = true;
			}

			if (shouldRender) { // Can be used to limit FPS
				frames++;
				render();
			}
		}
	}

	/**
	 * Calls the tick method to update Game.
	 * Checks if player is in combat with another entity
	 */
	public void tick() {
		tickCount++;

		switch(state) {
			case START:
				menu.tick(this);
				break;
			case CLASSES:
				menu.state = Menu.MenuStates.CLASSES;
				menu.tick(this);
				break;
			case RUNNING:
				player.getLevel().tick();
				Entity e = player.getLevel().getTouching(player);
				if(e instanceof Mob) {
					if (e instanceof Boss) {
						boss = true;
					}
					player.mainX = player.x;
					player.mainY = player.y;
					player.x = 24;
					player.y = combatLevel.height*8/2;
					e.x = (combatLevel.width*8) - 24;
					e.y = combatLevel.height*8/2;
					player.setPrevLevel(player.getLevel());
					combatLevel.addEntity(player.getLevel().removeEntity(e));
					combatLevel.addEntity(player.getLevel().removeEntity(player));
					player.move(-1,0);
					player.move(1,0);
					((Mob)e).move(1,0);
					((Mob)e).move(1,0);
					((Mob)e).move(-1,0);
					((Mob)e).move(-1,0);
					state = States.COMBAT;
					menu.state = Menu.MenuStates.COMBAT;
					combat = new Combat(player, (Mob)e);
				} else if (e instanceof HealthPad) {
					player.heal(((HealthPad)e).activate());
				}
				//cases for level change
				if(player.x > 520 && player.x <= 528 && player.getLevel() == main_level){
					player.setLevel(dungeon1);
					player.x = 8;
					player.y = dungeon1.getStart()*8;
					dungeon1.addEntity(main_level.removeEntity(player));
				}
				else if(player.x > 1192 && player.x <= 1200 && player.getLevel() == main_level){
					player.setLevel(dungeon2);
					player.x = 8;
					player.y = dungeon2.getStart()*8;
					dungeon2.addEntity(main_level.removeEntity(player));
				}
				else if(player.x >= 506 && player.getLevel() == dungeon1){
					player.x = 672;
					player.y = main_level.height*8/2;
					main_level.addEntity(dungeon1.removeEntity(player));
				}
				else if(player.x >= 506 && player.getLevel() == dungeon2){
					player.x = 1344;
					player.y = main_level.height*8/2;
					main_level.addEntity(dungeon2.removeEntity(player));
				}
				else if(player.x < 8 && player.getLevel() == dungeon1){
					player.x = 496;
					player.y = main_level.height*8/2;
					main_level.addEntity(dungeon1.removeEntity(player));
				}
				else if(player.x < 8 && player.getLevel() == dungeon2){
					player.x = 1334;
					player.y = main_level.height*8/2;
					main_level.addEntity(dungeon2.removeEntity(player));
				}
				menu.tick(this);
				break;
			case PAUSED:
				menu.tick(this);
				break;
			case COMBAT:
				menu.tick(this);
				combat.tick();
				if (!combat.inCombat && player.getCurrentHealth() > 0) {
					state = States.POSTCOMBAT;
					menu.state = Menu.MenuStates.CLOSED;
					Random rand = new Random(System.currentTimeMillis());
					drop=rand.nextInt(5);	//Random for each item, Check case and drops an item on end of combat.
					rng1=rand.nextInt(3);	//RNG the stat.
				  rng2=rand.nextInt(3);	//RNG the stat.
					rng3=rand.nextInt(3);	//RNG the stat.
					if(drop==0)
					{
					 combatLevel.addEntity(new Helmet(combatLevel,"Helmet","Of doom!",combat.combatant2.mob.getRank()+rng1,combat.combatant2.mob.getRank()+rng2,combat.combatant2.mob.getRank()+rng3));
				 	}
					else if(drop==1)
					{
					 combatLevel.addEntity(new Chest(combatLevel,"Chest","Of doom!",combat.combatant2.mob.getRank()+rng1,combat.combatant2.mob.getRank()+rng2,combat.combatant2.mob.getRank()+rng3));
				 	}
					else if(drop==2)
					{
					 combatLevel.addEntity(new Legs(combatLevel,"Legs","Of doom!",combat.combatant2.mob.getRank()+rng1,combat.combatant2.mob.getRank()+rng2,combat.combatant2.mob.getRank()+rng3));
				 	}
					else if(drop==3)
					{
					 combatLevel.addEntity(new Shield(combatLevel,"Shield","Of doom!",combat.combatant2.mob.getRank()+rng1,combat.combatant2.mob.getRank()+rng2,combat.combatant2.mob.getRank()+rng3));
				 	}
					else if(drop==4)
					{
					 combatLevel.addEntity(new Weapon(combatLevel,"Weapon","Of doom!",combat.combatant2.mob.getRank()+rng1,combat.combatant2.mob.getRank()+rng2,combat.combatant2.mob.getRank()+rng3));
				 	}
					combatLevel.removeEntity(combat.combatant2.mob);
					player.addKill();
					player.addExp(20);
					if (boss) {
						endTime = System.currentTimeMillis();
						state = States.OVER;
					}
				} else if (!combat.inCombat) {
					endTime = System.currentTimeMillis();
					state = States.OVER;
				}
				break;
			case POSTCOMBAT:
				menu.tick(this);
				player.getLevel().tick();
				e = player.getLevel().getTouching(player);
				if (e instanceof Item) {
					if (player.pickUp((Item)e)) {
						combatLevel.removeEntity(e);
						lootFX.playFX();
					}
				}
				if (player.x >= 154) {
					state = States.RUNNING;
					combatLevel.removeEntity(player);
					player.x = player.mainX;
					player.y = player.mainY;
					combatLevel.entities.clear();
					player.getPrevLevel().addEntity(player);
				}
				break;
			case OVER:
				break;
			default:
				break;
		}
	}

	/**
	 * Render the level tiles and entities.
	 */
	public void render() {

		BufferStrategy bs = getBufferStrategy(); // Get the BufferStrategy
		if (bs == null) { // If no currently set BufferStrategy, use triple buffering
			createBufferStrategy(3);
			return;
		}
		// Set the offset of the screen based on the player location
		int xOffset = player.x - screen.width/2;
		int yOffset = player.y - screen.height/2;
		switch (state) {
			case START:
				for (int i=0; i<screen.width; i++) {
					for (int j=0; j<screen.height; j++) {
						screen.render(i, j, 0, Colors.get(0,0,0,0), 0x00, 1);
					}
				}
				menu.render(this, screen);
				break;
			case CLASSES:
				for (int i=0; i<screen.width; i++) {
					for (int j=0; j<screen.height; j++) {
						screen.render(i, j, 0, Colors.get(0,0,0,0), 0x00, 1);
					}
				}
				menu.render(this, screen);
				break;
			case RUNNING:
				player.getLevel().renderTiles(screen, xOffset, yOffset);
				player.getLevel().renderEntities(screen);

				HUD.render(screen, this);

				menu.render(this, screen);
				break;
			case PAUSED:
				player.getLevel().renderTiles(screen, xOffset, yOffset);
				player.getLevel().renderEntities(screen);

				HUD.render(screen, this);

				menu.render(this, screen);
				break;
			case COMBAT:
				player.getLevel().renderTiles(screen, xOffset, yOffset);
				player.getLevel().renderEntities(screen);

				combat.render(screen);

				HUD.render(screen, this);

				menu.render(this, screen);
				break;
			case POSTCOMBAT:
				player.getLevel().renderTiles(screen, xOffset, yOffset);
				player.getLevel().renderEntities(screen);

				HUD.render(screen, this);

				menu.render(this, screen);
				break;
			case OVER:
				for (int i=0; i<screen.width; i++) {
					for (int j=0; j<screen.height; j++) {
						screen.render(i, j, 0, Colors.get(0,0,0,0), 0x00, 1);
					}
				}
				if (player.getCurrentHealth() <= 0) {
					Font.render("Defeat", screen, screen.xOffset+7*8, screen.yOffset+1*8, Colors.get(-1,-1,-1,555), 1);
				} else {
					Font.render("Victory", screen, screen.xOffset+7*8, screen.yOffset+1*8, Colors.get(-1,-1,-1,555), 1);
				}
				Font.render("Class:" + player.getPlayerClass(), screen, screen.xOffset+1*8, screen.yOffset+3*8, Colors.get(-1,-1,-1,555), 1);
				Font.render("Rank:" + player.getRank(), screen, screen.xOffset+1*8, screen.yOffset+5*8, Colors.get(-1,-1,-1,555), 1);
				Font.render("Kills:" + player.getKillCount(), screen, screen.xOffset+1*8, screen.yOffset+7*8, Colors.get(-1,-1,-1,555), 1);
				String s_String = "";
				if ((endTime - startTime)/1000%60 < 10) {
					s_String = "0";
				}
				Font.render("Time:" + ((endTime - startTime)/60000) + ":" + s_String + ((endTime - startTime)/1000%60), screen, screen.xOffset+1*8, screen.yOffset+9*8, Colors.get(-1,-1,-1,555), 1);
				break;
			default:
				break;
		}


		// Set the values of the pixels
		for (int y=0; y<screen.height; y++) {
			for (int x=0; x<screen.width; x++) {
				int colorCode = screen.pixels[x+y*screen.width];
				if (colorCode < 255) pixels[x+y*WIDTH] = colors[colorCode];
			}
		}

		Graphics g = bs.getDrawGraphics();							// Creates a graphics context for the buffer
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);	// Draws the image from (0,0) to the (width,height) with no ImageObserver
		g.dispose();												// Diposes of the graphics context and releases any system resources that it is using
		bs.show();													// Make the next buffer visible
	}
	/**
	 * Main method to be ran for the program.
	 * @param args	Arguments to be passed into the program.
	 */
		public static void main(String[] args)
		{
			new Game().start();
		}
}
