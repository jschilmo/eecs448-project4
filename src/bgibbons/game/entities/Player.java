package bgibbons.game.entities;

import bgibbons.game.*;
import bgibbons.game.InputHandler;
import bgibbons.game.entities.*;
import bgibbons.game.graphics.Colors;
import bgibbons.game.graphics.Font;
import bgibbons.game.graphics.Screen;
import bgibbons.game.level.Level;
import bgibbons.game.abilities.*;

/**
 * An extension of the Mob class for a player.
 * @author Brad Gibbons
 * @author Chris Porras
 * @author Rony Singh
 * @version 1.0 50 November 2016.
 */
public class Player extends Mob {

	public int mainX;
	public int mainY;
	private InputHandler input;
	private int color;
	private int scale = 1;
	private Item helmet = null;
	private Item chest = null;
	private Item legs = null;
	private Item weapon = null;
	private Item shield = null;
	private Item[] inventory = new Item[6];
	private int attributePoints = 0;
	public Sound sound;			//Declare the Sound object.
	public String playerClass;

	/**
	 * Constructor the Player object.
	 * @param level 	Level for the player to be added to.
	 * @param x 		The x coordinate the player will start at.
	 * @param y 		The y coordinate the player will start at.
	 * @param input 	The InputHandler used to control the player.
	 */
	public Player(Level level, int x, int y, InputHandler input) {
		super(level, "Player", x, y, 10, 1, 1, 100);
		this.mainX = x;
		this.mainY = y;
		this.input = input;
		this.playerClass = "Standard";
	}

	/**
	 * Ticks the player.
	 */
	public void tick() {
		// Level player
		if (currentExp >= maxExp) {
			rankUp();
		}

		// Move player
		int xa = 0;
		int ya = 0;

		if (input.up.isPressed()) {
			ya--;
		}
		if (input.down.isPressed()) {
			ya++;
		}
		if (input.left.isPressed()) {
			xa--;
		}
		if (input.right.isPressed()) {
			xa++;
		}

		if (xa != 0 || ya != 0) {
			move(xa, ya);
			isMoving = true;
		} else {
			isMoving = false;
		}

		if (level.getTile(this.x >> 3, this.y >> 3).getId() == 3) {
			isSwimming = true;
		}
		if  (isSwimming && level.getTile(this.x >> 3, this.y >> 3).getId() != 3) {
			isSwimming = false;
		}
		tickCount++;
	}

	/**
	 * Renders the player to the screen
	 * @param screen 	The screen to render the player to.
	 */
	public void render(Screen screen) {
		int xTile = 0;
		int yTile = 28;
		int walkingSpeed = 4;
		int flipTop = (numSteps >> walkingSpeed) & 1;
		int flipBottom = (numSteps >> walkingSpeed) & 1;

		if (movingDir == 0) {
			xTile += 2;
		} else if (movingDir == 1) {
			xTile += 6;
		} else if (movingDir > 1) {
			xTile += 8 + ((numSteps >> walkingSpeed) & 1) * 2;
			flipTop = (movingDir - 1) % 2;
		}

		if (!isMoving) {
			if (lastDir == 0) {
				xTile = 0;
			} else if (lastDir == 1) {
				xTile = 4;
			} else {
				xTile = 8;
			}
		}

		int modifier = 8 * scale;
		int xOffset = x - modifier/2 - 4;
		int yOffset = y - modifier/2 - 4;

		if (isSwimming) {
			int waterColor = 0;
			yOffset += 4;
			if (tickCount % 60 < 15) {
				yOffset -= 1;
				waterColor = Colors.get(-1,-1,225,-1);
			} else if (15 <= tickCount%60 && tickCount%60 < 30) {
				waterColor = Colors.get(-1, 225, 115, -1);
			} else if (30 <= tickCount%60 && tickCount%60 < 45) {
				waterColor = Colors.get(-1, 115, -1, 225);
			} else {
				waterColor = Colors.get(-1, 225, 115, -1);
			}
			screen.render(xOffset, yOffset + 3, 31+29*32, waterColor, 0x00, 1);
			screen.render(xOffset+8, yOffset + 3, 31+29*32, waterColor, 0x01, 1);
		}

		screen.render(xOffset + (modifier * flipTop), yOffset, xTile + yTile * 32, color, flipTop, scale); //Top left
		screen.render(xOffset + modifier - (modifier * flipTop), yOffset, xTile + 1 + yTile * 32, color, flipTop, scale); //Top right

		if (!isSwimming) {
			screen.render(xOffset + (modifier * flipBottom), yOffset + modifier, xTile + (yTile + 1) * 32, color, flipBottom, scale); //Bottom left
			screen.render(xOffset + modifier - (modifier * flipBottom), yOffset + modifier, xTile + 1 + (yTile + 1) * 32, color, flipBottom, scale); //Bottom right
		}
	}

	/**
	 * Checks if the player will collide when moving.
	 * @param xa 	The x direction the player wants to move.
	 * @param ya 	The y direction the player wants to move.
	 */
	public boolean hasCollided(int xa, int ya) {
		int xMin = -4;
		int xMax = 3;
		int yMin = 3;
		int yMax = 7;

		for (int x=xMin; x<xMax; x++) {
			if (isSolidTile(xa, ya, x, yMin)) {
				return true;
			}
		}

		for (int x=xMin; x<xMax; x++) {
			if (isSolidTile(xa, ya, x, yMax)) {
				return true;
			}
		}

		for (int y=yMin; y<yMax; y++) {
			if (isSolidTile(xa, ya, xMin, y)) {
				return true;
			}
		}

		for (int y=yMin; y<yMax; y++) {
			if (isSolidTile(xa, ya, xMax, y)) {
				return true;
			}
		}


		return false;
	}

	/**
	 * Level up the mob once max exp is reached.
	 */
	public void rankUp() {
		currentExp = 0;
		rank++;
		attributePoints += 3;
		if (maxHealth < 40 && rank % 2 == 1) {
			maxHealth+=2;
		}
		if (currentHealth < maxHealth) {
			currentHealth++;
		}
	}

	/**
	 * Get the item in the player's helmet slot.
	 * @return The Item object in the player's helmet slot.
	 */
	public Item getHead() {
		return helmet;
	}

	/**
	 * Get the item in the player's chest slot.
	 * @return The Item object in the player's chest slot.
	 */
	public Item getChest() {
		return chest;
	}

	/**
	 * Get the item in the player's legs slot.
	 * @return The Item object in the player's legs slot.
	 */
	public Item getLegs() {
		return legs;
	}

	/**
	 * Get the item in the player's weapon slot.
	 * @return The Item object in the player's weapon slot.
	 */
	public Item getWeapon() {
		return weapon;
	}

	/**
	 * Get the item in the player's shield slot.
	 * @return The Item object in the player's shield slot.
	 */
	public Item getShield() {
		return shield;
	}

	/**
	 * Get the item from the given inventory slot.
	 * @param index 	The slot to remove the item from.
	 * @return The Item from the given index.
	 */
	public Item getInventory(int index) {
		return inventory[index];
	}

	/**
	 * Equips the item in the given inventory slot.
	 * @param index 	Index of the item to equip.
	 */
	public void equip(int index)
	{
		Item e = inventory[index];
		if (e instanceof Helmet)
		{
			if(getHead() instanceof Helmet)
			{
				unEquip(getHead());
			}
			Item temp = helmet;
			helmet = e;
			inventory[index] = temp;
			this.dexterity += this.helmet.dexterity;
			this.intelligence += this.helmet.intelligence;
			this.vitality += this.helmet.vitality;
			sound = new Sound("/res/sounds/Menu/item.wav"); 		//Intialize SFX sound object with path.
			sound.playFX();				//Play the sound.
		}
		else if (e instanceof Chest)
		{
			if(getChest() instanceof Chest)
			{
				unEquip(getChest());
			}
			Item temp = chest;
			chest = e;
			inventory[index] = temp;
			this.dexterity += this.chest.dexterity;
			this.intelligence += this.chest.intelligence;
			this.vitality += this.chest.vitality;
			sound = new Sound("/res/sounds/Menu/item.wav"); 		//Intialize SFX sound object with path.
			sound.playFX();				//Play the sound.
		}
		else if (e instanceof Legs)
		{
			if(getLegs() instanceof Legs)
			{
				unEquip(getLegs());
			}
			Item temp = legs;
			legs = e;
			inventory[index] = temp;
			this.dexterity += this.legs.dexterity;
			this.intelligence += this.legs.intelligence;
			this.vitality += this.legs.vitality;
			sound = new Sound("/res/sounds/Menu/item.wav"); 		//Intialize SFX sound object with path.
			sound.playFX();				//Play the sound.
		}
		else if (e instanceof Weapon)
		{
			if(getWeapon() instanceof Weapon)
			{
				unEquip(getWeapon());
			}
			Item temp = weapon;
			weapon = e;
			inventory[index] = temp;
			this.dexterity += this.weapon.dexterity;
			this.intelligence += this.weapon.intelligence;
			this.vitality += this.weapon.vitality;
			sound = new Sound("/res/sounds/Menu/item.wav"); 		//Intialize SFX sound object with path.
			sound.playFX();				//Play the sound.
		}
		else if (e instanceof Shield)
		{
			if(getShield() instanceof Shield)
			{
				unEquip(getShield());
			}
			Item temp = shield;
			shield = e;
			inventory[index] = temp;

			this.dexterity += this.shield.dexterity;
			this.intelligence += this.shield.intelligence;
			this.vitality += this.shield.vitality;
			sound = new Sound("/res/sounds/Menu/item.wav"); 		//Intialize SFX sound object with path.
			sound.playFX();				//Play the sound.

		}
	}

	/**
	 * Unequips the item in the given inventory slot.
	 * @param e 	The item to unequip
	 */
	public void unEquip(Item e) {
		for (int i=0; i<6; i++) {
			if (inventory[i] == null) {
				if (e instanceof Helmet) {
					inventory[i] = e;
					this.dexterity -= this.helmet.dexterity;
					this.intelligence -= this.helmet.intelligence;
					this.vitality -= this.helmet.vitality;
					helmet = null;
					sound = new Sound("/res/sounds/Menu/item.wav"); 		//Intialize SFX sound object with path.
					sound.playFX();				//Play the sound.
				} else if (e instanceof Chest) {
					inventory[i] = e;
					this.dexterity -= this.chest.dexterity;
					this.intelligence -= this.chest.intelligence;
					this.vitality -= this.chest.vitality;
					chest = null;
					sound = new Sound("/res/sounds/Menu/item.wav"); 		//Intialize SFX sound object with path.
					sound.playFX();				//Play the sound.
				} else if (e instanceof Legs) {
					inventory[i] = e;
					this.dexterity -= this.legs.dexterity;
					this.intelligence -= this.legs.intelligence;
					this.vitality -= this.legs.vitality;
					legs = null;
					sound = new Sound("/res/sounds/Menu/item.wav"); 		//Intialize SFX sound object with path.
					sound.playFX();				//Play the sound.
				} else if (e instanceof Weapon) {
					inventory[i] = e;
					this.dexterity -= this.weapon.dexterity;
					this.intelligence -= this.weapon.intelligence;
					this.vitality -= this.weapon.vitality;
					weapon = null;
					sound = new Sound("/res/sounds/Menu/item.wav"); 		//Sound set
					sound.playFX();				//Play the sound.
				} else if (e instanceof Shield) {
					inventory[i] = e;
					this.dexterity -= this.shield.dexterity;
					this.intelligence -= this.shield.intelligence;
					this.vitality -= this.shield.vitality;
					shield = null;
					sound = new Sound("/res/sounds/Menu/item.wav"); 		//Sound set
					sound.playFX();				//Play the sound.
				}
				return;
			}
		}
	}
	/**
	 * Drop the item from the corresponding slot (equiped slots included).
	 * @param slot 	Slot to drop the item from.
	 */
	public void drop(int slot) {
		switch (slot) {
			case 0:
				helmet = null;
				break;
			case 1:
				chest = null;
				break;
			case 2:
				legs = null;
				break;
			case 3:
				weapon = null;
				break;
			case 4:
				shield = null;
				break;
			case 5:
				inventory[0] = null;
				break;
			case 6:
				inventory[1] = null;
				break;
			case 7:
				inventory[2] = null;
				break;
			case 8:
				inventory[3] = null;
				break;
			case 9:
				inventory[4] = null;
				break;
			case 10:
				inventory[5] = null;
				break;
			default:
				break;
		}
	}

	/**
	 * Picks up the item the player ran over.
	 * @param item 	The item to pick up.
	 * @return True if the item was picked up, false otherwise.
	 */
	public boolean pickUp(Item item) {
		for (int i=0; i<6; i++) {
			if (inventory[i] == null) {
				inventory[i] = item;
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the remaining attribute points of the player.
	 * @return The remaining attribute points of the player.
	 */
	public int getAttributePoints() {
		return attributePoints;
	}

	/**
	 * Increase Dexterity.
	 */
	public void increaseDexterity() {
		dexterity++;
		attributePoints--;
	}

	/**
	 * Increase Intelligence.
	 */
	public void increaseIntelligence() {
		intelligence++;
		attributePoints--;
	}

	/**
	 * Increase Vitality.
	 */
	public void increaseVitality() {
		vitality++;
		attributePoints--;
	}
	/**
	 * Set Knight color, inventory and calls to set abilities.
	 */
	public void setKnight()
	{
		playerClass = "Knight";
		color = Colors.get(-1, 111, 250, 543);
		inventory[0] = new Weapon(level,"Weapon","Standard",5,4,6);
		inventory[1] = new Shield(level,"Shield","Standard",5,4,6);
		setKnightAbilities();
	}
	/**
	 * Set Wizard color, inventory and calls to set abilities.
	 */
	public void setWizard()
	{
		playerClass = "Wizard";
		color = Colors.get(-1, 500, 0, 543);
		inventory[0] = new Helmet(level,"Helmet","Standard",5,6,4);
		inventory[1] = new Weapon(level,"Weapon","Standard",5,6,4);
		setWizardAbilities();
	}
	/**
	 * Set Wizard color, inventory and calls to set abilities.
	 */
	public void setHunter()
	{
		playerClass = "Hunter";
		color = Colors.get(-1, 0, 300, 543);
		inventory[0] = new Legs(level,"Legs","Standard",6,5,5);
		inventory[1] = new Weapon(level,"Weapon","Standard",6,5,5);
		setHunterAbilities();
	}
	/**
	 * Set Knight abilities.
	 */
	public void setKnightAbilities()
	{
		Mob.ability1 = Ability.STRIKE;
		Mob.ability2 = Ability.SHIELD_BASH;
		Mob.ability3 = Ability.DIVINE_CALL;
		Mob.ability4 = Ability.HOLY_SMITE;
	}
	/**
	 * Set Wizard abilities.
	 */
	public void setWizardAbilities()
	{
		Mob.ability1 = Ability.BONK;
		Mob.ability2 = Ability.SCORCH;
		Mob.ability3 = Ability.FIRE_WALL;
		Mob.ability4 = Ability.KABOOM;
	}
	/**
	 * Set Hunter abilities.
	 */
	public void setHunterAbilities()
	{
		Mob.ability1 = Ability.STAB;
		Mob.ability2 = Ability.KNIFE_THROW;
		Mob.ability3 = Ability.SMOKE_BOMB;
		Mob.ability4 = Ability.MARK;
	}

	/**
	 * Returns the player's class.
	 * @return The player's class.
	 */
	public String getPlayerClass() {
		return playerClass;
	}
}
