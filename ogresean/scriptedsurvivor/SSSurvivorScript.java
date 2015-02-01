package ogresean.scriptedsurvivor;

import java.util.ArrayList;

public class SSSurvivorScript {

    public SSSurvivorScript() {
        //default values
        name = "Sean";
        maxHP = 20;
        movementSpeed = 0.7F;
        jumpHeight = 0.41999998688697815D;
        texture = "/mob/char.png";
        livingSound = null;
        hurtSound = "random.hurt";
        deathSound = "random.hurt";
        /*spawnMaxLight = 15F;
		spawnMinLight = 0.0F;
		spawnMaxHeight = 128F;
		spawnMinHeight = 0.0F;
		spawnBlockIDs = null;
		spawnFrequency = 10;*/
        inventorySize = 10;
        idleStatements = null;
        actions = new ArrayList<SSAction>();
    }


    //Stat Related
    public String name; //shows above Survivor, used in Render class
    public int maxHP;
    public float movementSpeed;
    public double jumpHeight;
    public String texture;

    //sound related - requires audiomod for additional functionality
    public String livingSound;
    public String hurtSound;
    public String deathSound;
	
	/* To be used later
	//spawn related - Lighting requirements, y axis requirements, blocks able to be spawned on
	public float spawnMaxLight; //inclusive
	public float spawnMinLight; //inclusive
	public float spawnMaxHeight; //inclusive
	public float spawnMinHeight; //inclusive
	public ArrayList<Integer> spawnBlockIDs; //if null, can spawn on any blocks.
	public int spawnFrequency; //must be 1 or higher
	*/

    //custom related
    public int inventorySize; //at least 1; used by SurvivorInventory class
    public ArrayList<SSAction> actions;

    //multiplayer simulation related
    public String idleStatements[];

    /**
     * possible scripts so far:
     * --Wanderer: wanders aimlessly
     * --Seeker: wanders and seeks specific blocks
     * --Talker: wanders and talks
     * --Miner: mines and obtains various blocks
     */
}
