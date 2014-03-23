package ogresean.scriptedsurvivor;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EntityPickupFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

/**
 * 
 * @name EntityScriptedSurvivor
 * @descrip Based on customized text documents provided by user, Survivor's stats and actions are predetermined
 * @note1 There is only 1 kind of each Survivor at a time.  Survivors act like multiplayer Players. They are spawned every X ticks using the mod_ class.
 * @note2 If a survivor "despawns", it is instead saved into different Entity list; MESSAGE: "X disconnected"
 * @note3 If player goes back within
 *
 */
public class SSEntityScriptedSurvivor extends EntityCreature {
	
	public SSEntityScriptedSurvivor(World world){
		super(world);
		damageRemainder = 0;
		type = mod_ScriptedSurvivors.survivors.size() > 0 ? rand.nextInt(mod_ScriptedSurvivors.survivors.size()) : 0;
		actionTimer = -1;
		coords = null;
		currentAction = 0;
		pack = new SSSurvivorInventory(this);
		path = null;
		viewTarget = null;
		idleStatementDelay = 699;
		setVars();
	}
	
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        pack.readFromNBT(nbttagcompound.getTagList("Pack"));
        type = nbttagcompound.getInteger("Type");
        currentAction = nbttagcompound.getInteger("CurrentAction");
        setVars();
        mod_ScriptedSurvivors.loadSurvivor(this);
    }
	
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setTag("Pack", pack.writeToNBT(new NBTTagList()));
        nbttagcompound.setInteger("Type", type);
        nbttagcompound.setInteger("CurrentAction", currentAction);
        mod_ScriptedSurvivors.saveSurvivor(this);
    }
	
	public float getBlockPathWeight(int i, int j, int k)
    {
        return 5F;
    }
	
	public void setDead(){
		super.setDead();
		mod_ScriptedSurvivors.killSurvivor(this);
	}
	
	public void onDeath(DamageSource d)
    {
		super.onDeath(d);
		pack.dropAllItems();
    }
	
	public ItemStack getHeldItem()
    {
        return pack.getCurrentItem();
    }
	
	public void moveFlying(float f, float f1, float f2)
    {
        if(moveSpeed <= 1.0F)
        	super.moveFlying(f, f1, f2);
        else
        	super.moveFlying(f, f1, f2 * moveSpeed);
    }
	
	protected String getLivingSound()
    {
        return mod_ScriptedSurvivors.scripts.get(type).livingSound;
    }

    protected String getHurtSound()
    {
        return mod_ScriptedSurvivors.scripts.get(type).hurtSound;
    }

    protected String getDeathSound()
    {
        return mod_ScriptedSurvivors.scripts.get(type).deathSound;
    }
    
    protected void jump()
    {
        motionY = mod_ScriptedSurvivors.scripts.get(type).jumpHeight;
        field_35118_ao = true;
    }
    
    protected void fall(float f){
    	super.fall((float) (f * (0.41999998688697815D / mod_ScriptedSurvivors.scripts.get(type).jumpHeight)));
    }
    
    public void applyItemCollection(EntityItem ei)
    {
    	if(ei.delayBeforeCanPickup == 0 && pack.addItemStackToInventory(ei.getEntityItem()))
            {
                worldObj.playSoundAtEntity(this, "random.pop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                ModLoader.getMinecraftInstance().effectRenderer.addEffect(new EntityPickupFX(worldObj, ei, this, -0.5F));
                if(ei.getEntityItem().stackSize <= 0)
                {
                    ei.setDead();
                }
            }
    }
    
    public void collectNearbyItems(){
    	List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(1D, 0.2D, 1D));
        if(list != null && list.size() > 0)
        {
            for(int i = 0; i < list.size(); i++)
            {
                Entity entity = (Entity)list.get(i);
                if(entity instanceof EntityItem)
                {
                    applyItemCollection((EntityItem) entity);
                }
            }

        }
    }
	
	public void onUpdate()
    {
		super.onUpdate();
		collectNearbyItems();
		if(mod_ScriptedSurvivors.scripts.get(type).actions.get(currentAction).isActionComplete(this)){
			mod_ScriptedSurvivors.scripts.get(type).actions.get(currentAction).exitAction(this, false);
			currentAction = currentAction + 1 >= mod_ScriptedSurvivors.scripts.get(type).actions.size() ? 0 : currentAction + 1;
		}
		mod_ScriptedSurvivors.scripts.get(type).actions.get(currentAction).doAction(this);
		
		if((idleStatementDelay++ >= 400 && rand.nextInt(250) == 0) || idleStatementDelay > 715){
			int x = MathHelper.floor_double(posX);
			int y = MathHelper.floor_double(boundingBox.minY);
			int z = MathHelper.floor_double(posZ);
			idleStatementDelay = 0;
			String message = mod_ScriptedSurvivors.scripts.get(type).idleStatements[rand.nextInt(mod_ScriptedSurvivors.scripts.get(type).idleStatements.length)].trim();
			String s = mod_ScriptedSurvivors.scripts.get(type).name.concat(": ").concat(message);
			String s2 = mod_ScriptedSurvivors.survivorDebug ? (new StringBuilder(" ").append(x).append(" ").append(y).append(" ").append(z)).toString() : "";
			this.worldObj.getClosestPlayerToEntity(this, 16D).addChatMessage(new ChatComponentText(s.concat(s2)));
		}

		mod_ScriptedSurvivors.survCount--;
    }
	

	protected boolean canDespawn(){
		return false;
	}
	
	protected void updateEntityActionState()
    {
        hasAttacked = isMovementCeased();
        float f = 20F;
        /////Wander movement and attacking movement handled by actions//////////
        /*if(playerToAttack == null)
        {
            playerToAttack = findPlayerToAttack();*/
        if(getEntityToAttack() != null)
            {
                setPathToEntity(worldObj.getPathToEntity(this, getEntityToAttack(), f));
                if(!getEntityToAttack().isEntityAlive())
                {
                	setEntityToAttack(null);
                }
            }
        	/* else
        {
            float f1 = playerToAttack.getDistanceToEntity(this);
            if(canEntityBeSeen(playerToAttack))
            {
                attackEntity(playerToAttack, f1);
            } else
            {
                func_28022_b(playerToAttack, f1);
            }
        }
        if(!hasAttacked && playerToAttack != null && (path == null || rand.nextInt(20) == 0))
        {
            path = worldObj.getPathToEntity(this, playerToAttack, f);
        } else
        if(!hasAttacked && (path == null && rand.nextInt(80) == 0 || rand.nextInt(80) == 0))
        {
            boolean flag = false;
            int j = -1;
            int k = -1;
            int l = -1;
            float f2 = -99999F;
            for(int i1 = 0; i1 < 10; i1++)
            {
                int j1 = MathHelper.floor_double((posX + (double)rand.nextInt(13)) - 6D);
                int k1 = MathHelper.floor_double((posY + (double)rand.nextInt(7)) - 3D);
                int l1 = MathHelper.floor_double((posZ + (double)rand.nextInt(13)) - 6D);
                float f3 = getBlockPathWeight(j1, k1, l1);
                if(f3 > f2)
                {
                    f2 = f3;
                    j = j1;
                    k = k1;
                    l = l1;
                    flag = true;
                }
            }

            if(flag)
            {
                path = worldObj.getEntityPathToXYZ(this, j, k, l, 10F);
            }
        }*/
        int i = MathHelper.floor_double(boundingBox.minY + 0.5D);
        boolean flag1 = isInWater();
        boolean flag2 = handleLavaMovement();
        rotationPitch = 0.0F;
        if(path == null)
        {
            updatePlayerActionState2();
            return;
        }
        Vec3 vec3d = path.getPosition(this);
        for(double d = width * 2.0F; vec3d != null && vec3d.squareDistanceTo(posX, vec3d.yCoord, posZ) < d * d;)
        {
            path.incrementPathIndex();
            if(path.isFinished())
            {
                vec3d = null;
                path = null;
            } else
            {
                vec3d = path.getPosition(this);
            }
        }

        isJumping = false;
        if(vec3d != null)
        {
            double d1 = vec3d.xCoord - posX;
            double d2 = vec3d.zCoord - posZ;
            double d3 = vec3d.yCoord - (double)i;
            float f4 = (float)((Math.atan2(d2, d1) * 180D) / 3.1415927410125732D) - 90F;
            float f5 = f4 - rotationYaw;
            moveForward = moveSpeed;
            for(; f5 < -180F; f5 += 360F) { }
            for(; f5 >= 180F; f5 -= 360F) { }
            if(f5 > 30F)
            {
                f5 = 30F;
            }
            if(f5 < -30F)
            {
                f5 = -30F;
            }
            rotationYaw += f5;
            if(hasAttacked && getEntityToAttack() != null)
            {
                double d4 = getEntityToAttack().posX - posX;
                double d5 = getEntityToAttack().posZ - posZ;
                float f7 = rotationYaw;
                rotationYaw = (float)((Math.atan2(d5, d4) * 180D) / 3.1415927410125732D) - 90F;
                float f6 = (((f7 - rotationYaw) + 90F) * 3.141593F) / 180F;
                moveStrafing = -MathHelper.sin(f6) * moveForward * 1.0F;
                moveForward = MathHelper.cos(f6) * moveForward * 1.0F;
            }
            if(d3 > 0.0D)
            {
                isJumping = true;
            }
        }
        if(getEntityToAttack() != null)
        {
            faceEntity(getEntityToAttack(), 30F, 30F);
        }
        if(isCollidedHorizontally && !hasPath())
        {
            isJumping = true;
        }
        if(rand.nextFloat() < 0.8F && (flag1 || flag2))
        {
            isJumping = true;
        }
    }
	
	/**
	 * From EntityLiving
	 */
	protected void updatePlayerActionState2()
    {
		entityAge++;
        //EntityPlayer entityplayer = worldObj.getClosestPlayerToEntity(this, -1D);
		despawnEntity();
        moveStrafing = 0.0F;
        moveForward = 0.0F;
        float f = 8F;
        if(rand.nextFloat() < 0.02F)
        {
            EntityPlayer entityplayer1 = worldObj.getClosestPlayerToEntity(this, f);
            if(entityplayer1 != null)
            {
                viewTarget = entityplayer1;
                numTicksToChaseTarget = 10 + rand.nextInt(20);
            } else
            {
                randomYawVelocity = (rand.nextFloat() - 0.5F) * 20F;
            }
        }
        if(viewTarget != null)
        {
            faceEntity(viewTarget, 10F, getVerticalFaceSpeed());
            if(numTicksToChaseTarget-- <= 0 || viewTarget.isDead || viewTarget.getDistanceSqToEntity(this) > (double)(f * f))
            {
                viewTarget = null;
            }
        } else
        {
            if(rand.nextFloat() < 0.05F)
            {
                randomYawVelocity = (rand.nextFloat() - 0.5F) * 20F;
            }
            rotationYaw += randomYawVelocity;
            rotationPitch = defaultPitch;
        }
        boolean flag = isInWater();
        boolean flag1 = handleLavaMovement();
        if(flag || flag1)
        {
            isJumping = rand.nextFloat() < 0.8F;
        }
    }
	
	public boolean hasPath()
    {
        return path != null;
    }

    public void setPathToEntity(PathEntity pathentity)
    {
        path = pathentity;
    }
    
    public void dropItemWithRandomChoice(ItemStack itemstack, boolean flag)
    {
        if(itemstack == null)
        {
            return;
        }
        EntityItem entityitem = new EntityItem(worldObj, posX, (posY - 0.30000001192092896D) + (double)getEyeHeight(), posZ, itemstack);
        entityitem.delayBeforeCanPickup = 40;
        float f = 0.1F;
        if(flag)
        {
            float f2 = rand.nextFloat() * 0.5F;
            float f4 = rand.nextFloat() * 3.141593F * 2.0F;
            entityitem.motionX = -MathHelper.sin(f4) * f2;
            entityitem.motionZ = MathHelper.cos(f4) * f2;
            entityitem.motionY = 0.20000000298023224D;
        } else
        {
            float f1 = 0.3F;
            entityitem.motionX = -MathHelper.sin((rotationYaw / 180F) * 3.141593F) * MathHelper.cos((rotationPitch / 180F) * 3.141593F) * f1;
            entityitem.motionZ = MathHelper.cos((rotationYaw / 180F) * 3.141593F) * MathHelper.cos((rotationPitch / 180F) * 3.141593F) * f1;
            entityitem.motionY = -MathHelper.sin((rotationPitch / 180F) * 3.141593F) * f1 + 0.1F;
            f1 = 0.02F;
            float f3 = rand.nextFloat() * 3.141593F * 2.0F;
            f1 *= rand.nextFloat();
            entityitem.motionX += Math.cos(f3) * (double)f1;
            entityitem.motionY += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
            entityitem.motionZ += Math.sin(f3) * (double)f1;
        }
        worldObj.entityJoinedWorld(entityitem);
    }
    
    public float getCurrentSurvivorStrVsBlock(Block block)
    {
        float f = pack.getStrVsBlock(block);
        if(isInsideOfMaterial(Material.water))
        {
            f /= 5F;
        }
        if(!onGround)
        {
            f /= 5F;
        }
        return f;
    }
    
    public boolean canHarvestBlock(Block block)
    {
        return pack.canHarvestBlock(block);
    }
    
    public float blockStrength(Block b)
    {
        if(b.blockHardness < 0.0F)
        {
            return 0.0F;
        }
        if(!canHarvestBlock(b))
        {
            return 1.0F / b.blockHardness / 100F;
        } else
        {
            return getCurrentSurvivorStrVsBlock(b) / b.blockHardness / 30F;
        }
    }
    
    public void damageEntity(DamageSource d, int i)
    {
        int j = 25 - pack.getTotalArmorValue();
        int k = i * j + damageRemainder;
        pack.damageArmor(i);
        i = k / 25;
        damageRemainder = (byte) (k % 25);
        super.damageEntity(d, i);
    }
    
    public void setVars(){
    	//set various other characteristics based on type
		health = mod_ScriptedSurvivors.scripts.get(type).maxHP;
		moveSpeed = mod_ScriptedSurvivors.scripts.get(type).movementSpeed;
		texture = mod_ScriptedSurvivors.scripts.get(type).texture;
    }
	
	//This Survivor holds whatever item it is using, and drops its entire inventory at death
	//This Survivor will equip whatever armor it finds/obtains
	
	public int type; //holds surviver script list index number
	public int currentAction;
    public Entity viewTarget;
    public SSSurvivorInventory pack;
    private byte damageRemainder;
    //variables used by actions, not saved. Actions reset to beginning when survivor reloaded
	public int actionTimer;
    public int coords[];
	public PathEntity path;
	public float nums[]; //various floating point
	private short idleStatementDelay;
    
}
