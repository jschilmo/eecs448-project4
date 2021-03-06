package bgibbons.game.abilities;
import java.lang.RuntimeException;
import bgibbons.game.Sound;
import bgibbons.game.entities.Mob;
/**
 * Base ability class to be extended by other abilities.
 * @author Brad Gibbons
 * @author Rony Singh
 * @version 1.0. 50 November 2016.
 */
public abstract class Ability
{
	//Knight's abilities
	public static final Ability[] abilities = new Ability[256];
	public static final Ability STRIKE = new OffensiveAbility(0, "Strike      ", 1, 1, "vit", 10, false, 0);
	public static final Ability SHIELD_BASH = new OffensiveAbility(1, "ShieldBash  ", 6, 1, "vit", 15, true, 0);
	public static final Ability DIVINE_CALL = new DefensiveAbility(2, "DivineCall  ", 10, 2, "int", 20, 1);
	public static final Ability HOLY_SMITE = new OffensiveAbility(3, "Holy  Smite ", 12, 10, "int", 10, false, 0);
	//Wizard's abilities
	public static final Ability BONK = new OffensiveAbility(4, "BONK        ", 1, 1, "vit", 20, false, 0);
	public static final Ability SCORCH = new OffensiveAbility(5, "Scorch        ", 4, 1, "int", 10, false, 4);
	public static final Ability FIRE_WALL = new OffensiveAbility(6, "Fire  Wall  ", 10, 2, "int", 10, false, 5);
	public static final Ability KABOOM = new OffensiveAbility(7, "KABOOM      ", 12, 15, "int", 10, false, 0);
	//Hunter's abilities
	public static final Ability STAB = new OffensiveAbility(8, "Stab        ", 1, 1, "dex", 15, false, 0);
	public static final Ability KNIFE_THROW = new OffensiveAbility(9, "Knife Throw ", 6, 1, "dex", 5, false, 0);
	public static final Ability SMOKE_BOMB = new OffensiveAbility(10, "Smoke Bomb  ", 10, 0, "dex", 5, false, 6);
	public static final Ability MARK = new OffensiveAbility(11, "Mark        ", 6, 0, "dex", 5, false, 5);

	protected byte id;
	protected String name;
	protected int cooldown;
	public static Sound sound;
	/**
	 * Constructor for the Ability class.
	 * @param id 		Unique id for the ability.
	 * @param name 		Name of the ability.
	 * @param cooldown 	Cooldown for the ability.
	 */
	public Ability(int id, String name, int cooldown) {
		this.id = (byte) id;
		this.name = name;
		this.cooldown = cooldown;

		if (abilities[id] != null) throw new RuntimeException("Duplicate ability id on " + id);

		abilities[id] = this;
	}
	/**
	 * Plays ability 1 sound.
	 */
	public static void playAbility1Sound()
	{
		if(Mob.ability1.getName()=="Strike      "){
		sound = new Sound("/res/sounds/Combat/Knight/Strike.wav"); 		//Intialize SFX sound object with path.
		sound.playFX();	}			//Play the sound.

		if(Mob.ability1.getName()=="BONK        "){
		sound = new Sound("/res/sounds/Combat/Wizard/Bonk.wav"); 		//Intialize SFX sound object with path.
		sound.playFX();}				//Play the sound.

		if(Mob.ability1.getName()=="Stab        "){
		sound = new Sound("/res/sounds/Combat/Hunter/Stab.wav"); 		//Intialize SFX sound object with path.
		sound.playFX();}				//Play the sound.
	}
	/**
	 * Plays ability 2 sound.
	 */
	public static void playAbility2Sound()
	{
		if(Mob.ability2.getName()=="ShieldBash  "){
		sound = new Sound("/res/sounds/Combat/Knight/ShieldBash.wav"); 		//Intialize SFX sound object with path.
		sound.playFX();	}			//Play the sound.

		if(Mob.ability2.getName()=="Scorch        "){
		sound = new Sound("/res/sounds/Combat/Wizard/Scorch.wav"); 		//Intialize SFX sound object with path.
		sound.playFX();	}			//Play the sound.

		if(Mob.ability2.getName()=="Knife Throw "){
		sound = new Sound("/res/sounds/Combat/Hunter/KnifeThrow.wav"); 		//Intialize SFX sound object with path.
		sound.playFX();	}			//Play the sound.
	}
	/**
	 * Plays ability 3 sound.
	 */
	public static void playAbility3Sound()
	{
		if(Mob.ability3.getName()=="DivineCall  "){
		sound = new Sound("/res/sounds/Combat/Knight/DivineCall.wav"); 		//Intialize SFX sound object with path.
		sound.playFX();}				//Play the sound.

		if(Mob.ability3.getName()=="Fire  Wall  "){
		sound = new Sound("/res/sounds/Combat/Wizard/FireWall.wav"); 		//Intialize SFX sound object with path.
		sound.playFX();}				//Play the sound.

		if(Mob.ability3.getName()=="Smoke Bomb  "){
		sound = new Sound("/res/sounds/Combat/Hunter/SmokeBomb.wav"); 		//Intialize SFX sound object with path.
		sound.playFX();}				//Play the sound.
	}
	/**
	 * Plays ability 4 sound.
	 */
	public static void playAbility4Sound()
	{
		if(Mob.ability4.getName()=="Holy  Smite "){
		sound = new Sound("/res/sounds/Combat/Knight/HolySmite.wav"); 		//Intialize SFX sound object with path.
		sound.playFX();}				//Play the sound.

		if(Mob.ability4.getName()=="KABOOM      "){
		sound = new Sound("/res/sounds/Combat/Wizard/KABOOM.wav"); 		//Intialize SFX sound object with path.
		sound.playFX();}				//Play the sound.

		if(Mob.ability4.getName()=="Mark        "){
		sound = new Sound("/res/sounds/Combat/Hunter/Mark.wav"); 		//Intialize SFX sound object with path.
		sound.playFX();}				//Play the sound.
	}
	/**
	 * Returns the Id of the Ability.
	 * @return The Id of the Ability.
	 */
	public byte getId() {
		return id;
	}

	/**
	 * Returns the name of the Ability.
	 * @return The name of the Ability.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the cooldown of the Ability.
	 * @return the cooldown of the Ability.
	 */
	public int getCooldown() {
		return cooldown;
	}

	/**
	 * Returns the damage of the ability.
	 * @return The damage of the ability.
	 */
	public abstract int getDamage();

	/**
	 * Returns whether or the ability stuns or not.
	 * @return A boolean telling if the ability stuns or not.
	 */
	public abstract boolean getStun();

	/**
	 * Returns the heal the ability gives.
	 * @return The heal the ability gives.
	 */
	public abstract int getHeal();

	/**
	 * Returns the shield the ability gives.
	 * @return the shield the ability gives.
	 */
	public abstract int getShield();

	/**
	 * Returns the type of stat the ability scales with.
	 * @return A string of the stat to scale with, empty string for no scaling, rank for scale on rank.
	 */
	public abstract String getType();

	/**
	 * Returns the scaling of the ability. Specifically, the number of x stats required to increase the damage/heal by 1 point.
	 * @return The scaling of the ability.
	 */
	public abstract int getScale();

	/**
	 * Returns the duration of the ability.
	 * @return An int that specifies the duration of the ability.
	 */
	public abstract int getDuration();
}
