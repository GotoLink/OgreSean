package ogresean.scriptedsurvivor;

import java.util.HashMap;

//NOTE: grid search refers to complete search in cube-like grid, like 3x3x3 grid
/**
 ** Actions planned:
 * -0%- Place Block (block id) : if block in inventory, place it nearby (saved in coords)
 * -0%- Craft Item (item id, maximum number to craft, boolean workBench?) : if possible use items in inventory to craft the item. If workbench required, check for workbench at coords and use that, or fail if not there
 * -0%- Heal Self(item[] ids of healing items to use) : attempt to use healing items in inventory to heal self up to maximum health if possible.
 * -0%- Place Item(s) in Chest(item[] ids to place in container, max[] of each item to place into container) : Placed at container at coords, else 4x4x4 grid search for container nearby;
*/
public abstract class SSAction {
	public abstract void doAction(SSEntityScriptedSurvivor ess);
	public abstract boolean isActionComplete(SSEntityScriptedSurvivor ess);
	public abstract void exitAction(SSEntityScriptedSurvivor ess, boolean actionCanceled);
	public SSAction createAction(String s){
		return null;
	}
	
	/**
	 * @note Remember to lowercase before searching for key.
	 * @note This hashmap is used for the createAction method in each SSAction
	 */
	public static final HashMap<String, SSAction> actionTypes;
	
	static{
		actionTypes = new HashMap<String, SSAction>();
		actionTypes.put("wander", new SSActionWander());
		actionTypes.put("findblock", new SSActionFindBlock());
		actionTypes.put("findtarget", new SSActionFindTarget());
		actionTypes.put("goto1", new SSActionGOTO1());
		actionTypes.put("saymessage", new SSActionSayMessage());
		actionTypes.put("addinvitem", new SSActionAddInvItems());
		actionTypes.put("reminvitem", new SSActionRemInvItems());
		actionTypes.put("equiparmor", new SSActionEquipArmor());
		actionTypes.put("mineblock", new SSActionMineBlock());
		actionTypes.put("goto2", new SSActionGOTO2());
		actionTypes.put("goto3", new SSActionGOTO3());
	}
}
