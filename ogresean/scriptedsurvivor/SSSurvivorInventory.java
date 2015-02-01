package ogresean.scriptedsurvivor;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class SSSurvivorInventory {

    public SSSurvivorInventory(SSEntityScriptedSurvivor ess) {
        mainInventory = new ItemStack[mod_ScriptedSurvivors.scripts.get(ess.type).inventorySize];
        armorInventory = new ItemStack[4];
        currentItem = 0;
        inventoryChanged = false;
        survivor = ess;
    }

    public ItemStack getCurrentItem() {
        if (currentItem < mainInventory.length && currentItem >= 0) {
            return mainInventory[currentItem];
        } else {
            return null;
        }
    }

    private int getInventorySlotContainItem(Item i) {
        for (int j = 0; j < mainInventory.length; j++) {
            if (mainInventory[j] != null && mainInventory[j].getItem() == i) {
                return j;
            }
        }

        return -1;
    }

    public boolean isItemStackInInventory(ItemStack item) {
        for (int j = 0; j < mainInventory.length; j++) {
            if (mainInventory[j] != null && mainInventory[j].getItem() == item.getItem() && (!item.getHasSubtypes() || item.getItemDamage() == -1 || mainInventory[j].getItemDamage() == item.getItemDamage()) && mainInventory[j].stackSize >= item.stackSize) {
                return true;
            }
        }
        return false;
    }

    public ItemStack getItemStackInInventory(ItemStack item) {
        for (int j = 0; j < mainInventory.length; j++) {
            if (mainInventory[j] != null && mainInventory[j].getItem() == item.getItem() && (!item.getHasSubtypes() || mainInventory[j].getItemDamage() == item.getItemDamage()) && mainInventory[j].stackSize >= item.stackSize) {
                return mainInventory[j];
            }
        }
        return null;
    }

    public int getItemStackInInventory2(ItemStack item) {
        for (int j = 0; j < mainInventory.length; j++) {
            if (mainInventory[j] != null && mainInventory[j].getItem() == item.getItem() && (!item.getHasSubtypes() || item.getItemDamage() == -1 || mainInventory[j].getItemDamage() == item.getItemDamage()) && mainInventory[j].stackSize >= item.stackSize) {
                return j;
            }
        }
        return -1;
    }

    private int storeItemStack(ItemStack itemstack) {
        for (int i = 0; i < mainInventory.length; i++) {
            if (mainInventory[i] != null && mainInventory[i].getItem() == itemstack.getItem() && mainInventory[i].isStackable() && mainInventory[i].stackSize < mainInventory[i].getMaxStackSize() && mainInventory[i].stackSize < getInventoryStackLimit() && (!mainInventory[i].getHasSubtypes() || mainInventory[i].getItemDamage() == itemstack.getItemDamage())) {
                return i;
            }
        }

        return -1;
    }

    private int getFirstEmptyStack() {
        for (int i = 0; i < mainInventory.length; i++) {
            if (mainInventory[i] == null) {
                return i;
            }
        }

        return -1;
    }

    public boolean setCurrentItem(Item i) {
        int j = getInventorySlotContainItem(i);
        if (j >= 0 && j < mainInventory.length) {
            currentItem = j;
            return true;
        } else {
            return false;
        }
    }

    public void changeCurrentItem(int i) {
        if (i > 0) {
            i = 1;
        }
        if (i < 0) {
            i = -1;
        }
        for (currentItem -= i; currentItem < 0; currentItem += mainInventory.length) {
        }
        for (; currentItem >= mainInventory.length; currentItem -= mainInventory.length) {
        }
    }

    private int storePartialItemStack(ItemStack itemstack) {
        Item i = itemstack.getItem();
        int j = itemstack.stackSize;
        int k = storeItemStack(itemstack);
        if (k < 0) {
            k = getFirstEmptyStack();
        }
        if (k < 0) {
            return j;
        }
        if (mainInventory[k] == null) {
            mainInventory[k] = new ItemStack(i, 0, itemstack.getItemDamage());
        }
        int l = j;
        if (l > mainInventory[k].getMaxStackSize() - mainInventory[k].stackSize) {
            l = mainInventory[k].getMaxStackSize() - mainInventory[k].stackSize;
        }
        if (l > getInventoryStackLimit() - mainInventory[k].stackSize) {
            l = getInventoryStackLimit() - mainInventory[k].stackSize;
        }
        if (l == 0) {
            return j;
        } else {
            j -= l;
            mainInventory[k].stackSize += l;
            mainInventory[k].animationsToGo = 5;
            return j;
        }
    }

    public void decrementAnimations() {
        for (int i = 0; i < mainInventory.length; i++) {
            if (mainInventory[i] != null) {
                mainInventory[i].updateAnimation(survivor.worldObj, survivor, i, currentItem == i);
            }
        }

    }

    public boolean consumeInventoryItem(Item i) {
        int j = getInventorySlotContainItem(i);
        if (j < 0) {
            return false;
        }
        if (--mainInventory[j].stackSize <= 0) {
            mainInventory[j] = null;
        }
        return true;
    }

    public boolean equipArmor(Item i) {
        int k = this.getItemStackInInventory2(new ItemStack(i, 1, 0));
        if (k == -1) return false;

        for (int j = 0; j < 4; j++) {
            if (this.armorInventory[j] == null) {
                armorInventory[j] = mainInventory[k].copy();
                mainInventory[k] = null;
                return true;
            }
        }
        return false;
    }

    public boolean removeItemStackFromInventory(ItemStack itemstack) {
        int j = this.getItemStackInInventory2(itemstack);
        if (j == -1) return false;

        mainInventory[j].stackSize -= itemstack.stackSize;
        if (mainInventory[j].stackSize <= 0) mainInventory[j] = null;

        return true;
    }

    public boolean addItemStackToInventory(ItemStack itemstack) {
        if (!itemstack.isItemDamaged()) {
            int i;
            do {
                i = itemstack.stackSize;
                itemstack.stackSize = storePartialItemStack(itemstack);
            } while (itemstack.stackSize > 0 && itemstack.stackSize < i);
            return itemstack.stackSize < i;
        }
        int j = getFirstEmptyStack();
        if (j >= 0) {
            mainInventory[j] = ItemStack.copyItemStack(itemstack);
            mainInventory[j].animationsToGo = 5;
            itemstack.stackSize = 0;
            return true;
        } else {
            return false;
        }
    }

    public ItemStack decrStackSize(int i, int j) {
        ItemStack aitemstack[] = mainInventory;
        if (i >= mainInventory.length) {
            aitemstack = armorInventory;
            i -= mainInventory.length;
        }
        if (aitemstack[i] != null) {
            if (aitemstack[i].stackSize <= j) {
                ItemStack itemstack = aitemstack[i];
                aitemstack[i] = null;
                return itemstack;
            }
            ItemStack itemstack1 = aitemstack[i].splitStack(j);
            if (aitemstack[i].stackSize == 0) {
                aitemstack[i] = null;
            }
            return itemstack1;
        } else {
            return null;
        }
    }

    public void setInventorySlotContents(int i, ItemStack itemstack) {
        ItemStack aitemstack[] = mainInventory;
        if (i >= aitemstack.length) {
            i -= aitemstack.length;
            aitemstack = armorInventory;
        }
        aitemstack[i] = itemstack;
    }

    public float getStrVsBlock(Block block) {
        float f = 1.0F;
        if (currentItem >= 0 && mainInventory[currentItem] != null) {
            f *= mainInventory[currentItem].getStrVsBlock(block);
        }
        return f;
    }

    public NBTTagList writeToNBT(NBTTagList nbttaglist) {
        for (int i = 0; i < mainInventory.length; i++) {
            if (mainInventory[i] != null) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                mainInventory[i].writeToNBT(nbttagcompound);
                nbttaglist.appendTag(nbttagcompound);
            }
        }

        for (int j = 0; j < armorInventory.length; j++) {
            if (armorInventory[j] != null) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte) (j + 100));
                armorInventory[j].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        return nbttaglist;
    }

    public void readFromNBT(NBTTagList nbttaglist) {
        mainInventory = new ItemStack[mod_ScriptedSurvivors.scripts.get(survivor.type).inventorySize];
        armorInventory = new ItemStack[4];
        for (int i = 0; i < nbttaglist.tagCount(); i++) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 0xff;
            ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound);
            if (itemstack.getItem() == null) {
                continue;
            }
            if (j >= 0 && j < mainInventory.length) {
                mainInventory[j] = itemstack;
            }
            if (j >= 100 && j < armorInventory.length + 100) {
                armorInventory[j - 100] = itemstack;
            }
        }

    }

    public int getSizeInventory() {
        return mainInventory.length + armorInventory.length;
    }

    public ItemStack getStackInSlot(int i) {
        if (i < 0) return null;
        ItemStack aitemstack[] = mainInventory;
        if (i >= aitemstack.length) {
            i -= aitemstack.length;
            aitemstack = armorInventory;
        }
        return aitemstack[i];
    }

    public String getInvName() {
        return "SurvivorInventory";
    }

    public int getInventoryStackLimit() {
        return 64;
    }

    public int getDamageVsEntity(Entity entity) {
        ItemStack itemstack = getStackInSlot(currentItem);
        if (itemstack != null) {
            return itemstack.getDamageVsEntity(entity);
        } else {
            return 1;
        }
    }

    public boolean canHarvestBlock(Block block) {
        if (block.getMaterial() != Material.rock && block.getMaterial() != Material.iron && block.getMaterial() != Material.craftedSnow && block.getMaterial() != Material.snow) {
            return true;
        }
        ItemStack itemstack = getStackInSlot(currentItem);
        if (itemstack != null) {
            return itemstack.canHarvestBlock(block);
        } else {
            return false;
        }
    }

    public ItemStack armorItemInSlot(int i) {
        return armorInventory[i];
    }

    public int getTotalArmorValue() {
        int i = 0;
        int j = 0;
        int k = 0;
        for (int l = 0; l < armorInventory.length; l++) {
            if (armorInventory[l] != null && (armorInventory[l].getItem() instanceof ItemArmor)) {
                int i1 = armorInventory[l].getMaxDamage();
                int j1 = armorInventory[l].getItemDamageForDisplay();
                int k1 = i1 - j1;
                j += k1;
                k += i1;
                int l1 = ((ItemArmor) armorInventory[l].getItem()).damageReduceAmount;
                i += l1;
            }
        }

        if (k == 0) {
            return 0;
        } else {
            return ((i - 1) * j) / k + 1;
        }
    }

    public void damageArmor(int i) {
        for (int j = 0; j < armorInventory.length; j++) {
            if (armorInventory[j] == null || !(armorInventory[j].getItem() instanceof ItemArmor)) {
                continue;
            }
            armorInventory[j].damageItem(i, survivor);
            if (armorInventory[j].stackSize == 0) {
                armorInventory[j] = null;
            }
        }

    }

    public void dropAllItems() {
        for (int i = 0; i < mainInventory.length; i++) {
            if (mainInventory[i] != null) {
                survivor.dropItemWithRandomChoice(mainInventory[i], true);
                mainInventory[i] = null;
            }
        }

        for (int j = 0; j < armorInventory.length; j++) {
            if (armorInventory[j] != null) {
                survivor.dropItemWithRandomChoice(armorInventory[j], true);
                armorInventory[j] = null;
            }
        }

    }

    public void onInventoryChanged() {
        inventoryChanged = true;
    }

    public void setItemStack(ItemStack itemstack) {
        itemStack = itemstack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public boolean canInteractWith(EntityPlayer entityplayer) {
        return !survivor.isDead && entityplayer.getDistanceSqToEntity(survivor) <= 64D;
    }

    public boolean func_28018_c(ItemStack itemstack) {
        for (ItemStack armor : armorInventory) {
            if (armor != null && armor.isStackEqual(itemstack)) {
                return true;
            }
        }

        for (ItemStack main : mainInventory) {
            if (main != null && main.isStackEqual(itemstack)) {
                return true;
            }
        }

        return false;
    }

    public ItemStack mainInventory[];
    public ItemStack armorInventory[];
    public int currentItem;
    public SSEntityScriptedSurvivor survivor;
    private ItemStack itemStack;
    public boolean inventoryChanged;
}

