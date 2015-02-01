package ogresean.scriptedsurvivor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;

import java.util.List;
import java.util.Locale;

/**
 * @name SSActionFindTarget
 * @description This action makes a Survivor look for certain entities, and possibly attack them
 */
public class SSActionFindTarget extends SSAction {
    /**
     * @param targets  - String IDs of entities to follow/attack
     * @param maxTicks - maximum time, in ticks, that Survivor will follow/attack Target
     * @param weapons  - IDs of items to be used for attacking; weapons[0] == -1, then target only followed
     */
    public SSActionFindTarget(String[] targets, int maxTicks, Item[] weapons) {
        targetedEntities = targets;
        maxTicksToLook = maxTicks;
        weaponIDs = weapons;
    }

    public SSActionFindTarget() {
        targetedEntities = null;
        maxTicksToLook = 0;
        weaponIDs = null;
    }

    public void doAction(SSEntityScriptedSurvivor ess) {
        if (ess.getEntityToAttack() == null && (ess.actionTimer == -1 || ess.path == null || ess.getRNG().nextFloat() < 0.001F))
            setNewWayPoint(ess);

        if ((ess.getEntityToAttack() == null || !ess.canEntityBeSeen(ess.getEntityToAttack()) || ess.getDistanceSqToEntity(ess.getEntityToAttack()) > 256D) && ess.actionTimer % 10 == 0)
            checkForTargets(ess);
        else if (ess.getEntityToAttack() != null && weaponIDs != null && weaponIDs[0] != null)
            attackTarget(ess);

        if (ess.swingProgress > 0)
            ess.swingProgress = ess.swingProgress >= 0.875F ? 0.0F : ess.swingProgress + 0.125F;
        if (ess.getEntityToAttack() != null && !ess.getEntityToAttack().isEntityAlive())
            ess.setEntityToAttack(null);
        ess.actionTimer++;
    }


    public boolean isActionComplete(SSEntityScriptedSurvivor ess) {
        if (ess.getEntityToAttack() != null && weaponIDs != null && weaponIDs[0] != null)
            return false;
        return ess.actionTimer >= maxTicksToLook;
    }

    public void exitAction(SSEntityScriptedSurvivor ess, boolean actionCanceled) {
        ess.actionTimer = -1;
        ess.setEntityToAttack(null);
        if (actionCanceled) {
            ess.coords = null;
            ess.setPathToEntity(null);
        }
    }


    private void setNewWayPoint(SSEntityScriptedSurvivor ess) {
        boolean flag = false;
        int j = -1;
        int k = -1;
        int l = -1;
        float f2 = -99999F;
        for (int i1 = 0; i1 < 10; i1++) {
            int j1 = MathHelper.floor_double((ess.posX + (double) ess.getRNG().nextInt(21)) - 10D);
            int k1 = MathHelper.floor_double((ess.posY + (double) ess.getRNG().nextInt(7)) - 3D);
            int l1 = MathHelper.floor_double((ess.posZ + (double) ess.getRNG().nextInt(21)) - 10D);
            float f3 = (float) ess.getDistance(j1, k1, l1);
            if (f3 > f2) {
                f2 = f3;
                j = j1;
                k = k1;
                l = l1;
                flag = true;
            }
        }

        if (flag) {
            ess.setPathToEntity(ess.worldObj.getEntityPathToXYZ(ess, j, k, l, 18F));
        }
    }

    private void checkForTargets(SSEntityScriptedSurvivor ess) {
        double d1 = 256D;
        Entity target = null;
        List list = ess.worldObj.getEntitiesWithinAABBExcludingEntity(ess, ess.boundingBox.expand(16D, 16D, 16D));
        for (Object object : list) {
            Entity entity1 = (Entity) object;
            if (!(isEntityTarget(entity1)) || entity1 == ess.ridingEntity || entity1 == ess.riddenByEntity) {
                continue;
            }
            double d2 = entity1.getDistanceSqToEntity(ess);
            if ((d2 < d1) && ess.canEntityBeSeen(entity1)) {
                d1 = d2;
                target = entity1;
            }
        }

        if (target == null)
            return;

        ess.setEntityToAttack(target);
        setWeapon(ess);
    }

    private void attackTarget(SSEntityScriptedSurvivor ess) {
        if (!ess.canEntityBeSeen(ess.getEntityToAttack()) || ess.getEntityToAttack() instanceof EntityItem)
            return;
        if (ess.getHeldItem() != null && ess.getHeldItem().getItem() == Items.bow && ess.getDistanceSqToEntity(ess.getEntityToAttack()) < 200D) {
            double d = ess.getEntityToAttack().posX - ess.posX;
            double d1 = ess.getEntityToAttack().posZ - ess.posZ;
            if (ess.attackTime == 0 && ess.pack.consumeInventoryItem(Items.arrow)) {
                EntityArrow entityarrow = new EntityArrow(ess.worldObj, ess, 1.0F);
                entityarrow.posY += 0.8999999761581421D;
                double d2 = ess.getEntityToAttack().posY - 0.20000000298023224D - entityarrow.posY;
                float f1 = MathHelper.sqrt_double(d * d + d1 * d1) * 0.2F;
                ess.worldObj.playSoundAtEntity(ess, "random.bow", 1.0F, 1.0F / (ess.getRNG().nextFloat() * 0.4F + 0.8F));
                ess.worldObj.spawnEntityInWorld(entityarrow);
                entityarrow.setThrowableHeading(d, (d2 + (double) f1), d1, 1.4F, 1.5F);
                ess.attackTime = 10 + ess.getRNG().nextInt(20); //added random attacktime
            }
            ess.rotationYaw = (float) ((Math.atan2(d1, d) * 180D) / 3.1415927410125732D) - 90F;
            ess.hasAttacked = true;
        } else if (ess.getDistanceSqToEntity(ess.getEntityToAttack()) < 10D) {
            ess.attackTime = 24 - ess.getRNG().nextInt(14);
            ess.prevSwingProgress = ess.swingProgress;
            ess.swingProgress = 0.0001F;
            ItemStack itemstack = ess.pack.getCurrentItem();
            int damage = itemstack == null || itemstack.getItem() instanceof ItemBlock ? 2 : itemstack.getItem().getDamageVsEntity(ess.getEntityToAttack());
            ess.getEntityToAttack().attackEntityFrom(DamageSource.causeMobDamage(ess), damage);
            if (itemstack != null && itemstack.itemID > 255 && ess.getEntityToAttack() instanceof EntityLiving) //damage item
            {
                itemstack.getItem().hitEntity(itemstack, (EntityLiving) ess.getEntityToAttack(), ess);
                if (itemstack.stackSize == 0) {
                    ess.pack.setInventorySlotContents(ess.pack.currentItem, null);
                    setWeapon(ess);
                }
            }
        }
    }

    private void setWeapon(SSEntityScriptedSurvivor ess) {
        for (Item weaponID : weaponIDs) {
            if (ess.pack.setCurrentItem(weaponID)) return;
        }
        ess.pack.currentItem = -1;
    }

    private boolean isEntityTarget(Entity e) {
        if (e == null) return false;

        for (String targetedEntity : targetedEntities) {
            if ((targetedEntity.equalsIgnoreCase("player") && e instanceof EntityPlayer)
                    || targetedEntity.equalsIgnoreCase(e.getCommandSenderName())
                    || targetedEntity.equalsIgnoreCase("all")) return true;
        }
        return false;
    }

    /**
     * @param StringSample pig,cow:400:267
     * @param Explanation  Hunt for pigs and cows for 400 ticks, striking them with an iron sword
     * @return FindTarget action
     */
    public SSAction createAction(String s) {
        try {
            String codes[] = s.split(":");
            String targets[] = codes[0].split(","); //target ids
            String weps[] = codes[2].split(","); //wep ids
            Item weapons[] = new Item[weps.length];
            int b = Integer.valueOf(codes[1]).intValue(); //ticks to look
            //target ids
            for (int i = 0; i < targets.length; i++) {
                targets[i] = targets[i].toLowerCase(Locale.ENGLISH).trim();
            }
            //weapon ids
            for (int i = 0; i < weps.length; i++) {
                weapons[i] = Item.getItemById(Integer.parseInt(weps[i].trim()));
            }

            return new SSActionFindTarget(targets, b, weapons);
        } catch (Exception e) {
            return null;
        }
    }

    private String[] targetedEntities;
    private int maxTicksToLook;
    private Item[] weaponIDs;
}
