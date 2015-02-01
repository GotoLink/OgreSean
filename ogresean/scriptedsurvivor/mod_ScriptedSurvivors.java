package ogresean.scriptedsurvivor;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class mod_ScriptedSurvivors {
    /**
 * Each file named script1.txt, script2.txt, etc. Loaded in this arraylist via that order
 * add each script to this arrayList
 */
public static ArrayList<SSSurvivorScript> scripts;

    /**
     * Each byte designates the survivor's current condition: (this is saved in nbt)
     * 0-Not in existence/Dead
     * 1-Spawned and Alive
     * 2-Saved in Chunk
     * 3-Saved in Chunk?
     */
    public static ArrayList<Byte> survivors;
    private static boolean mod_fail;
    public static boolean saveList;
    public static boolean loadList;
    public static int errorCode;
    public static String errorLine;

    public static int survCount;

    //public static RenderBlocks globalRenderBlocks;
    /**
     * How actions will be described in text file: SAMPLE ACTION SCRIPT
     *
     * Wander:80 = Wander for 80 ticks (4 seconds)
     * FindBlock:1:160 = Look for a Stone block for 8 seconds
     * MineBlock:1:285,278,257,274,270 = Mine Stone blocks with a pickaxe
     * FindBlock:2,3:300 = Look for Grass/Dirt for 15 seconds
     * MineBlock:2,3:284,277,256,273,269 = Mine Grass/Dirt blocks with a shovel
     */

    /**
     * TOOL IDS - GOLD - DIAMOND - IRON - STONE - WOOD
     * Shovels: 284,277,256,273,269
     * Pickaxes: 285,278,257,274,270
     * Axes: 286,279,258,275,271
     * Swords: 283,276,267,272,268
     */

    //how animal spawning done (uses EnumCreatureType)
    //17 x 17 chunks surrounding player added to eligible chunks set
    //Game checks how many animals exist, whether animals are peaceful creature
    public mod_ScriptedSurvivors() {
        errorCode = -1;
        errorLine = "";
        survivors = new ArrayList<Byte>();
        scripts = new ArrayList<SSSurvivorScript>();
        loadScriptFile();
        EntityRegistry.registerModEntity(SSEntityScriptedSurvivor.class, "ScriptedSurvivor", 12, mod, 80, 3, true);
        saveList = false;
        loadList = false;
        survCount = 0;
    }

    //PROBLEM Related to spawning
    //Perhaps try removing BlockPathWeight Air thing for Survivor

    @SideOnly(Side.CLIENT)
    public void addRenderer() {
        RenderingRegistry.registerEntityRenderingHandler(SSEntityScriptedSurvivor.class, new SSRenderSurvivor(new ModelBiped()));
    }

    public boolean OnTickInGUI(float f, Minecraft game, GuiScreen gui) {
        //Save survivor info when at menu screen
        if (!saveList && gui instanceof GuiIngameMenu) {
            listSave();
            saveList = true;
        }

        return true;
    }

    public boolean OnTickInGame(float f, Minecraft game) {
        if ((world != null && world.multiplayerWorld) || (game.thePlayer != null && game.thePlayer.dimension != 0))
            return true;

        //if world has just been loaded, or has changed, set world variable to that world
        if (world != game.theWorld) {
            world = game.theWorld;
            survCount = 0;
            saveList = false;
            loadList = false;
            String s = survivorDebug ? "DEBUG MODE: " : "Mod functioning: ";
            if (mod_fail)
                game.ingameGUI.addChatMessage("Survivor Mod: Error: ".concat(Integer.toString(errorCode)).concat(" ").concat(errorLine));
            else
                game.ingameGUI.addChatMessage("Survivor Mod: ".concat(s).concat(Integer.toString(scripts.size())).concat(" survivor types loaded"));

        }

        if (game.currentScreen == null) {

            //reset save variable
            if (saveList)
                saveList = false;

                //if not loaded list, load list from file
            else if (!loadList) {
                listLoad();
                listFill();
                loadList = true;
            }
        }

        //spawn survivors every 20 ticks
        if (world.getWorldTime() % 16L == 0L) {
            performSurvivorSpawning(game);
        }
        if (survCount != 0) {
            updateSurvivorList(game);
        }

        survCount = getAliveSurvivorNumber();

        return true;
    }

    private int getAliveSurvivorNumber() {
        int count = 0;
        for (int i = 0; i < survivors.size(); i++)
            if (survivors.get(i) == 1) {
                count++;
            }

        return count;
    }

    private void updateSurvivorList(Minecraft game) {
        Entity e;
        byte b[] = new byte[survivors.size()];
        for (int i = 0; i < world.loadedEntityList.size(); i++) {
            e = (Entity) world.loadedEntityList.get(i);
            if (e instanceof SSEntityScriptedSurvivor) {
                SSEntityScriptedSurvivor ess = (SSEntityScriptedSurvivor) e;
                if (ess.type < 0 || ess.type >= survivors.size()) continue;
                if (survivors.get(ess.type) == 1 && ess.isDead) survivors.set(ess.type, (byte) 0);
                else if ((survivors.get(ess.type) == 0 || survivors.get(ess.type) == 2 || survivors.get(ess.type) == 3) && !ess.isDead)
                    survivors.set(ess.type, (byte) 1);
                b[ess.type] = 100;
            }
        }
        for (int i = 0; i < b.length; i++) {
            if (b[i] != 100) {
                if (survivors.get(i) == 1) {
                    survivors.set(i, (byte) 0);
                    String s = mod_ScriptedSurvivors.scripts.get(i).name.concat(" ").concat("was disconnected.");
                    ModLoader.getMinecraftInstance().ingameGUI.addChatMessage("\247e".concat(s));
                } else if (survivors.get(i) == 3) {
                    survivors.set(i, (byte) 2);
                    String s = mod_ScriptedSurvivors.scripts.get(i).name.concat(" ").concat("has left the game.");
                    ModLoader.getMinecraftInstance().ingameGUI.addChatMessage("\247e".concat(s));
                }
            }
        }
    }

    private void listFill() {
        while (survivors.size() < scripts.size())
            survivors.add((byte) 0);
    }

    //spawns survivors into world
    private void performSurvivorSpawning(Minecraft game) {
        int spawnType = -1;
        for (int i = 0; i < scripts.size(); i++) {
            if (survivors.size() <= i || survivors.get(i) == ((byte) 0)) {
                spawnType = i;
                break;
            }
        }
        if (spawnType == -1) return;

        int chunkX = MathHelper.floor_double(game.thePlayer.posX / 16.0D);
        int chunkZ = MathHelper.floor_double(game.thePlayer.posZ / 16.0D);

        //choose random chunk to spawn survivor at
        chunkX += world.rand.nextInt(5) - 2;
        chunkZ += world.rand.nextInt(5) - 2;

        //choose random coordinates in chunk to spawn survivors at
        int x = (chunkX * 16) + world.rand.nextInt(16);
        int y = world.rand.nextInt(60) + 64;
        int z = (chunkZ * 16) + world.rand.nextInt(16);
        while (game.thePlayer.getDistance(x, y, z) < 4D) //make survivor spawn somewhat far from player
            z += world.rand.nextInt(4) + 4;
        while (world.isAirBlock(x, y - 1, z) && y > 63)
            y -= 2;
        //attempt to spawn survivor up to 64 times
        SSEntityScriptedSurvivor ess = new SSEntityScriptedSurvivor(world);
        ess.type = spawnType;
        ess.setVars();
        boolean flag = false;
        for (int i = -2; i < 2 && !flag; i++)
            for (int j = -2; j < 2 && !flag; j++)
                for (int k = -2; k < 2 && !flag; k++) {
                    ess.setLocationAndAngles(x + i, y + j, z + k, world.rand.nextFloat() * 360.0F, 0.0F);
                    if (ess.getCanSpawnHere()) {
                        if (world.entityJoinedWorld(ess)) {
                            loadSurvivor(ess);
                            flag = true;
                        }
                    }
                }
        if (survivorDebug) {
            String s = new StringBuilder(flag ? "Successful" : "Failed").append(" Spawning at ").append(x).append(" ").append(y).append(" ").append(z).toString();
            ModLoader.getMinecraftInstance().ingameGUI.addChatMessage("\2476".concat(s));
        }
    }

    private void listSave() {
        //if survivor list empty or non-existent, exit
        if (survivors == null || survivors.size() < 1)
            return;
        try {
            //create or replace survivor file
            File file3 = new File(world.getSaveHandler.getWorldDirectory(), "survivors.dat"); //survivor file
            if (file3.exists())
                file3.delete();

            //copy survivor status from array to NBT
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            NBTTagList nbttaglist = new NBTTagList();
            for (int i = 0; i < survivors.size(); i++) {
                byte b = survivors.get(i);
                if (b >= 0) {
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    nbttagcompound1.setByte("status", b);
                    nbttaglist.setTag(nbttagcompound1);
                }
            }
            nbttagcompound.setTag("SurvivorInfo", nbttaglist);
            CompressedStreamTools.write(nbttagcompound, file3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listLoad() {
        //create fresh new survivor List
        survivors = new ArrayList<Byte>();
        try {
            //open saved survivor file; if does not exist, end method
            File file3 = new File(world.getSaveHandler.getWorldDirectory(), "survivors.dat"); //survivor file
            if (file3 == null || !file3.exists())
                return;

            NBTTagCompound nbttagcompound = CompressedStreamTools.read(file3);

            NBTTagList nbttaglist = nbttagcompound.getTagList("SurvivorInfo");
            //read survivor status from file to survivor array
            for (int i = 0; i < nbttaglist.tagCount(); i++) {
                NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist.tagAt(i);
                byte b = nbttagcompound1.getByte("status");
                survivors.add(b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadScriptFile() {
        mod_fail = false;
        File srcDirectory;
        File scriptFile;
        try {
            errorCode = 5;
            errorCode = 10;
            srcDirectory = new File(source.getParentFile().getParentFile(), "mods"); //.minecraft/mods
            errorCode = 15;
            scriptFile = new File(srcDirectory, "SurvivorScripts.txt");
            errorCode = 20;
            if (scriptFile.exists() && scriptFile.isFile()) {
                errorCode = 25;
                loadScripts(scriptFile);
            } else mod_fail = true;


        } catch (Exception e) {
            mod_fail = true;
        }
    }

    private void loadScripts(File f) {
        SSSurvivorScript ss;
        BufferedReader br = null;
        try {
            errorLine = "new buffered reader";
            errorCode = 30;
            br = new BufferedReader(new FileReader(f));
        } catch (FileNotFoundException e1) {
            mod_fail = true;
        }
        String thisLine = "start";
        errorCode = 100;
        errorLine = "start";
        while (thisLine != null) {
            try {
                errorCode++;
                errorLine = thisLine;
                ss = new SSSurvivorScript();
                if (thisLine.equalsIgnoreCase("start")) thisLine = br.readLine(); //skip line if starting

                //name
                errorCode++;
                errorLine = thisLine;
                thisLine = br.readLine();
                ss.name = thisLine.split(":")[1].trim();

                //max hp
                errorCode++;
                errorLine = thisLine;
                thisLine = br.readLine();
                ss.maxHP = Integer.valueOf(thisLine.split(":")[1].trim()).intValue();

                //movement speed
                errorCode++;
                errorLine = thisLine;
                thisLine = br.readLine();
                ss.movementSpeed = Float.valueOf(thisLine.split(":")[1].trim()).floatValue();

                //jump height
                errorCode++;
                errorLine = thisLine;
                thisLine = br.readLine();
                ss.jumpHeight = Double.valueOf(thisLine.split(":")[1].trim()).doubleValue();

                //texture
                errorCode++;
                errorLine = thisLine;
                thisLine = br.readLine();
                ss.texture = thisLine.split(":")[1].trim();

                //living sound
                errorCode++;
                errorLine = thisLine;
                thisLine = br.readLine();
                ss.livingSound = thisLine.split(":")[1].trim();

                //hurt sound
                errorCode++;
                errorLine = thisLine;
                thisLine = br.readLine();
                ss.hurtSound = thisLine.split(":")[1].trim();

                //death sound
                errorCode++;
                errorLine = thisLine;
                thisLine = br.readLine();
                ss.deathSound = thisLine.split(":")[1].trim();

                //inventory size
                errorCode++;
                errorLine = thisLine;
                thisLine = br.readLine();
                ss.inventorySize = Integer.valueOf(thisLine.split(":")[1].trim()).intValue();

                //idle statements
                errorCode++;
                errorLine = thisLine;
                thisLine = br.readLine();
                thisLine = thisLine.split(":", 2)[1];
                ss.idleStatements = thisLine.split(":");

                //action script
                errorCode++;
                errorLine = thisLine;
                thisLine = br.readLine();
                errorLine = thisLine;
                thisLine = br.readLine();
                errorLine = thisLine;
                while (!thisLine.equalsIgnoreCase("end action script")) {//error happens between here

                    errorCode++;
                    String s[] = thisLine.split(":", 3);
                    errorCode = s.length; //does not occur
                    if (SSAction.actionTypes == null) errorCode = 2000; //does not occur
                    SSAction ssa = SSAction.actionTypes.get(s[1].trim().toLowerCase());
                    errorCode++;
                    if (ssa == null) errorCode = 3000; //does not occur
                    ss.actions.add(ssa.createAction(s[2].trim())); //and here
                    errorCode++;
                    thisLine = br.readLine();
                    errorCode++;
                    errorLine = thisLine;
                }

                errorCode++;
                errorLine = thisLine;
                scripts.add(ss);


                errorCode += 100;
                thisLine = br.readLine();
                errorLine = thisLine;
            } catch (Exception e) {
                e.printStackTrace();
                mod_fail = true;
                //thisLine = "NONONO";
            }
        }
    }

    //survivor switched to chunk-save? mode
    public static void saveSurvivor(SSEntityScriptedSurvivor ess) {
        if (ess.type < 0 || ess.type > survivors.size()) return;

        if (ess.type == survivors.size()) survivors.add((byte) 3);
        else survivors.set(ess.type, (byte) 3);
    }

    //survivor active
    public static void loadSurvivor(SSEntityScriptedSurvivor ess) {
        if (ess.type < 0 || ess.type > survivors.size()) return;

        if (ess.type == survivors.size()) survivors.add((byte) 1);
        else survivors.set(ess.type, (byte) 1);

        String s = mod_ScriptedSurvivors.scripts.get(ess.type).name.concat(" ").concat("has joined the game.");

        int x = MathHelper.floor_double(ess.posX);
        int y = MathHelper.floor_double(ess.boundingBox.minY);
        int z = MathHelper.floor_double(ess.posZ);
        ModLoader.getMinecraftInstance().ingameGUI.addChatMessage("\247e".concat(s).concat(s2));
    }

    //survivor has died
    public static void killSurvivor(SSEntityScriptedSurvivor ess) {

        if (ess.type < 0 || ess.type > survivors.size()) return;

        if (ess.type == survivors.size()) survivors.add((byte) 0);
        else survivors.set(ess.type, (byte) 0);

        String s = mod_ScriptedSurvivors.scripts.get(ess.type).name.concat(" ").concat("was disconnected.");
        ModLoader.getMinecraftInstance().ingameGUI.addChatMessage("\247e".concat(s));
    }
}
