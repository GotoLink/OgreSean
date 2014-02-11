package ogresean;

import java.lang.reflect.Field;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityCreeperSwarm extends EntityCreeper {
	public EntityCreeperSwarm leader = null; //follow and scan this creeper
	public byte direction = 8; //determines travel direction
	public byte explodeBoost = 0; //speeds up explosion
	public int xT = 0;
	public int yT = 0;
	public int zT = 0;
	float moveSpeed;
	/*
	 * Actions 0: idle 1: leading 2: following 3: leading & exploded 4: Seek out
	 * and destroy door or glass (hard difficulty only)
	 */
	public byte currentAction = 0;
	public short actionTimer = 0;
	Field sinceIgnited = EntityCreeper.class.getDeclaredFields()[1];

	//Creeper swarms move faster, have more hp, and explode quicker on harder difficulties
	public EntityCreeperSwarm(World world) {
		super(world);
		if (world.difficultySetting.ordinal() <= 1) {
			explodeBoost = 13; //ignites 2 counts sooner
		} else if (world.difficultySetting.ordinal() == 2) {
			setHealth(25);
			explodeBoost = 7; //ignites 4 counts sooner
		} else {
			setHealth(30);
			explodeBoost = 3; //ignites 9 counts sooner
		}
		if (!CreeperSwarm.enableCustomHealth)
			setHealth(20);
	}

	///sets attacked Creeper to leader, if leader is idle
	///sets leader to null if successful
	@Override
	public boolean attackEntityFrom(DamageSource d, float i) {
		//make immune to CreeperSwarm damage
		Entity entity = d.getEntity();
		if (entity instanceof EntityCreeperSwarm)
			return false;
		boolean bool = super.attackEntityFrom(d, i);
		//if survived and had either no leader or idle leader, make this creeper new leader
		if (!isDead && entity != null && currentAction == 2 && (leader == null || leader.entityToAttack == null)) {
			currentAction = 1;
			if (leader != null) {
				leader.currentAction = 2;
				leader.leader = this;
				leader = null;
			}
		}
		return bool;
	}

	public void blockSeekerUpdate() {
		actionTimer++;
		entityToAttack = null;
		//set path to target location
		this.setPathToEntity(worldObj.getEntityPathToXYZ(this, xT, yT, zT, 16F, false, true, true, true));
		//if within range of target, start exploding
		if (actionTimer < 241 && this.getDistance(xT, yT, zT) < 3D)
			actionTimer = 241;
		//if delayed for too long, just start exploding
		if (actionTimer > 240) {
			if (getTimeSinceIgnited() == 0) {
				worldObj.playSoundAtEntity(this, "random.fuse", 1.0F, 0.5F);
			}
			setCreeperState(1);
			setTimeSinceIgnited(getTimeSinceIgnited()+2);
			if (getTimeSinceIgnited() >= 30) {
				worldObj.createExplosion(this, posX, posY, posZ, 3F, true);
				setDead();
			}
			hasAttacked = true;
		}
	}

	//getBlockPathWeight
	///makes leader Creeper travel single direction (north, north-east, etc)
	@Override
	public float getBlockPathWeight(int i, int j, int k) {
		float xDif = (float) (posX - i);
		float zDif = (float) (posZ - k);
		switch (direction) {
		case 0:
			return zDif;
		case 1:
			return xDif;
		case 2:
			return -zDif;
		case 3:
			return -xDif;
		case 4:
			return ((xDif + zDif) / 2);
		case 5:
			return -((xDif + zDif) / 2);
		case 6:
			return ((xDif - zDif) / 2);
		case 7:
			return -((xDif - zDif) / 2);
		default:
			return 0.0F;
		}
	}

	@Override
	public boolean getCanSpawnHere() {
		if (super.getCanSpawnHere() == true && worldObj.difficultySetting.ordinal() != 0) {
			//number to spawn in swarm
			int swarmNumber;
			if (worldObj.difficultySetting.ordinal() <= 1) //easy = up to 5 additional creepers
				swarmNumber = CreeperSwarm.easyMax - 1;
			else if (worldObj.difficultySetting.ordinal() == 2) //normal = up to 7 additional creepers
				swarmNumber = CreeperSwarm.normalMax - 1;
			else
				//hard = up to 9 additional creepers
				swarmNumber = CreeperSwarm.hardMax - 1;
			//create additional creepers on top of original creeper
			int i = -1;
			int j = -1;
			int k = -1;
			for (int i1 = 0; i1 < swarmNumber; i1++) {
				i = MathHelper.floor_double(posX);// + (double)rand.nextInt(13)) - 6D);
				j = MathHelper.floor_double(posY);// + (double)rand.nextInt(7)) - 3D);
				k = MathHelper.floor_double(posZ);// + (double)rand.nextInt(13)) - 6D);
				EntityCreeperSwarm newCreep = new EntityCreeperSwarm(worldObj);
				newCreep.setPosition(i, j, k);
				currentAction = 1;
				newCreep.leader = this;
				newCreep.currentAction = 2;
				worldObj.spawnEntityInWorld(newCreep);
			}
			return true;
		} else
			return false;
	}

	//spawn only 1 swarm per chunk
	@Override
	public int getMaxSpawnedInChunk() {
		return 1;
	}

	//GetNewLeader Method
	///if no leader found within distance of 16, set self to leader (see NonLeaderUpdate())
	///else set leader to nearest leader
	//returns closest leading Creeper Swarm
	public EntityCreeperSwarm getNewLeader(Entity entity, double d) {
		double d1 = -1D;
		EntityCreeperSwarm entitymob2 = null;
		for (int i = 0; i < worldObj.loadedEntityList.size(); i++) {
			Entity entity1 = (Entity) worldObj.loadedEntityList.get(i);
			if (!(entity1 instanceof EntityCreeperSwarm) || entity1 == entity || entity1 == entity.ridingEntity || entity1 == entity.riddenByEntity) {
				continue;
			}
			double d2 = entity1.getDistanceSq(entity.posX, entity.posY, entity.posZ);
			if ((d < 0.0D || d2 < d * d) && (d1 == -1D || d2 < d1) && ((EntityCreeperSwarm) entity1).canEntityBeSeen(entity) && ((EntityCreeperSwarm) entity1).currentAction == 1) {
				d1 = d2;
				entitymob2 = (EntityCreeperSwarm) entity1;
			}
		}
		return entitymob2;
	}

	public void idleUpdate() {
		leader = getNewLeader(this, 16);
		//if no leader found, set self as new leader
		if (leader == null) {
			currentAction = 1; //leader
		} else
			currentAction = 2; //following
	}

	public void leaderUpdate() {
		actionTimer++;
		///change direction every 20 seconds.
		if (actionTimer == 400) {
			direction = Integer.valueOf(rand.nextInt(8)).byteValue();
			actionTimer = 0;
		}
	}

	//called by moveEntityWithHeading function; moves Entity short distance, based on moveSpeed
	//overriden to allow moveSpeeds above 1.0F
	@Override
	public void moveFlying(float f, float f1, float f2) {
		if (moveSpeed <= 1.0F)
			super.moveFlying(f, f1, f2);
		else
			super.moveFlying(f, f1, f2 * moveSpeed);
	}

	public void nonLeaderUpdate() {
		///if leader is null or is dead, obtain new leader (if not exploding)
		if (leader == null || (leader.isDead && leader.currentAction != 3)) {
			currentAction = 0;
			leader = null;
			return;
		}
		///if leader has exploded, automatically make explode
		else if (leader.currentAction == 3) {
			worldObj.createExplosion(this, posX, posY, posZ, 3F, true);
			setDead();
			return;
		}
		///if leader has leader, set this creep's leader to that leader
		else if (leader.currentAction == 2 && leader.leader != null)
			leader = leader.leader;
		//occasionally check for nearby doors & glass
		if (worldObj.difficultySetting.ordinal() == 3 && rand.nextFloat() < 0.01 && findBlockTarget())
			return;
		///if leader has lit fuse, sync up fuse with that leader
		////increase fuse if leader's is higher
		if (leader != null && leader.getTimeSinceIgnited() > getTimeSinceIgnited()) {
			//make fuse sound if time was 0
			if (getTimeSinceIgnited() == 0) {
				worldObj.playSoundAtEntity(this, "random.fuse", 1.0F, 0.5F);
			}
			setCreeperState(1);
			setTimeSinceIgnited(getTimeSinceIgnited()+2);
		}
		//decrease fuse if leader's is lower
		else if (leader != null && leader.getTimeSinceIgnited() < getTimeSinceIgnited()) {
			//func_21090_e(-1);
			//timeSinceIgnited--;
		}
		///If somehow this follower-creeper has a entityToAttack, set it to null
		if (entityToAttack != null)
			entityToAttack = null;
		///if leader has set a path, set path to that location
		if (leader != null && !leader.isDead) {
			PathEntity targetLocation;
			PathPoint targetPoint;
			int xpoint;
			int ypoint;
			int zpoint;
			try {
				targetLocation = ((PathEntity) EntityCreature.class.getDeclaredFields()[2].get(leader));
				if (targetLocation != null) {
					targetPoint = targetLocation.getFinalPathPoint();
					if (targetPoint != null) {
						xpoint = targetPoint.xCoord;
						ypoint = targetPoint.yCoord;
						zpoint = targetPoint.zCoord;
						setPathToEntity(worldObj.getEntityPathToXYZ(this, xpoint, ypoint, zpoint, 30F, false, true, true, true));
					}
				}
			} catch (Throwable e) {
				//e.printStackTrace();
			}
		}
	}

	//drop extra sulpher if leader
	@Override
	public void onDeath(DamageSource d) {
		if (currentAction == 1)
            func_145779_a(func_146068_u(), rand.nextInt(4) + 1);
		super.onDeath(d);
	}

	@Override
	public void onUpdate() {
		//non-Leader Update
		if (currentAction == 2)
			nonLeaderUpdate();
		//Leader Update
		else if (currentAction == 1)
			leaderUpdate();
		//Idle Update
		else if (currentAction == 0)
			idleUpdate();
		//Block Seeker Update
		else if (currentAction == 4)
			blockSeekerUpdate();
		super.onUpdate(); //must occur last
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		if (!CreeperSwarm.enableCustomSpeed) {
			moveSpeed = 0.7F;
		} else if (worldObj.difficultySetting.ordinal() <= 1) {
			moveSpeed = 0.85F;
		} else if (worldObj.difficultySetting.ordinal() == 2) {
			moveSpeed = 1.0F;
		} else {
			moveSpeed = 1.25F;
		}
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(moveSpeed);
	}

	/*
	 * protected void attackBlockedEntity(Entity entity, float f) { if
	 * (currentAction == 1) super.attackBlockedEntity(entity, f); }
	 */
	///AttackEntity, only occurs if leader
	@Override
	protected void attackEntity(Entity entity, float f) {
		if (currentAction == 1) {
			int i = getCreeperState();
			super.attackEntity(entity, f);
			if (isDead)
				currentAction = 3;
			//speed up explosion
			else if (CreeperSwarm.enableCustomExplosionTime && getTimeSinceIgnited() % explodeBoost == 0 && (i <= 0 && f < 3F || i > 0 && f < 7F)) {
				setCreeperState(1);
				setTimeSinceIgnited(getTimeSinceIgnited()+1);
			}
		}
	}

	//Find glass or a door to destroy
	protected boolean findBlockTarget() {
		int i = MathHelper.floor_double(posX) + rand.nextInt(13) - 6;
		int j = MathHelper.floor_double(posY) + rand.nextInt(7) - 3;
		int k = MathHelper.floor_double(posZ) + rand.nextInt(13) - 6;
		Block bid;
		for (int i0 = i - 2; i0 < i + 2; i0++)
			for (int j0 = j - 2; j0 < j + 2; j0++)
				for (int k0 = k - 2; k0 < k + 2; k0++) {
					bid = worldObj.func_147439_a(i0, j0, k0);
					if (bid == Blocks.wooden_door || bid == Blocks.iron_door || bid == Blocks.glass || bid == Blocks.glass_pane) {
						currentAction = 4;
						xT = i0;
						yT = j0;
						zT = k0;
						return true;
					}
				}
		return false;
	}

	//get PlayerToAttack
	///only checks for player to attack if leader is idle, sets self to leader if player found
	@Override
	protected Entity findPlayerToAttack() {
		if (currentAction < 3 && (currentAction == 1 || leader == null || leader.entityToAttack == null)) {
			Entity target = super.findPlayerToAttack();
			if (target != null && currentAction == 2) {
				currentAction = 1;
				if (leader != null) {
					leader.currentAction = 2;
					leader.leader = this;
					leader = null;
				}
			}
			return target;
		} else
			return null;
	}
	
	public int getTimeSinceIgnited(){
		try {
			sinceIgnited.setAccessible(true);
			return Integer.class.cast(sinceIgnited.get(this)).intValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public void setTimeSinceIgnited(int time){
		try {
			sinceIgnited.set(this, time);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
