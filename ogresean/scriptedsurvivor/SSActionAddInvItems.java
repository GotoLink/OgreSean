package ogresean.scriptedsurvivor;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * @name SSActionAddInvItems
 * @description This action adds items to a Survivor's inventory.
 */
public class SSActionAddInvItems extends SSAction {

    /**
     * @param addedItems - ItemStacks that are added to  the survivor's inventory.
     */
    public SSActionAddInvItems(ItemStack[] addedItems) {
        items = addedItems;
    }

    public SSActionAddInvItems() {
        items = null;
    }

    public void doAction(SSEntityScriptedSurvivor ess) {
    }


    public boolean isActionComplete(SSEntityScriptedSurvivor ess) {
        return true;
    }

    public void exitAction(SSEntityScriptedSurvivor ess, boolean actionCanceled) {
        if (actionCanceled) return;

        //add items to inventory
        for (ItemStack item : items) {
            if (!ess.pack.addItemStackToInventory(item.copy())) break;
        }
    }

    /**
     * @param StringSample 1#6#0,3#3#0
     * @param Explanation  2 itemstacks: 6 0-damage stone, 3 0-damage dirt
     * @return AddInvItems action
     */
    public SSAction createAction(String s) {
        try {
            String codes3[] = s.split(","); //itemstacks
            ItemStack items[] = new ItemStack[codes3.length];
            //itemStacks
            for (int i = 0; i < codes3.length; i++) {
                String vals[] = codes3[i].split("#");
                items[i] = new ItemStack(Item.getItemById(Integer.parseInt(vals[0])), Integer.parseInt(vals[1]), Integer.parseInt(vals[2]));
            }
            return new SSActionAddInvItems(items);
        } catch (Exception e) {
            return null;
        }
    }

    private ItemStack items[];
}
