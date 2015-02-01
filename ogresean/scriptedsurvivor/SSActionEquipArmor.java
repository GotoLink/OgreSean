package ogresean.scriptedsurvivor;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * @name SSActionEquipArmor
 * @description This action makes the survivor equip specific armor.
 */
public class SSActionEquipArmor extends SSAction {

    /**
     * @param removedItems - ItemStacks that are removed from the survivor's inventory.
     */
    public SSActionEquipArmor(Item armors[]) {
        armorIDs = armors;
    }

    public SSActionEquipArmor() {
        armorIDs = null;
    }

    public void doAction(SSEntityScriptedSurvivor ess) {
    }


    public boolean isActionComplete(SSEntityScriptedSurvivor ess) {
        return true;
    }

    public void exitAction(SSEntityScriptedSurvivor ess, boolean actionCanceled) {
        if (actionCanceled) return;

        //equip armors, if not already wearing armor.
        for (Item armorID : armorIDs) {
            if (!ess.pack.equipArmor(armorID)) break;
        }
    }

    /**
     * @param StringSample 249,266
     * @param Explanation  equip these 2 armor pieces in order.
     * @return EquipArmor action
     */
    public SSAction createAction(String s) {
        try {
            String codes3[] = s.split(","); //armors
            Item armor[] = new Item[codes3.length];
            //ints
            for (int i = 0; i < codes3.length; i++) {
                armor[i] = Item.getItemById(Integer.parseInt(codes3[i].trim()));
            }
            return new SSActionEquipArmor(armor);
        } catch (Exception e) {
            return null;
        }
    }

    private Item armorIDs[];

}
