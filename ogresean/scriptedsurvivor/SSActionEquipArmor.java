package ogresean.scriptedsurvivor;

/**
 * 
 * @name SSActionEquipArmor
 * @description This action makes the survivor equip specific armor.
 */
public class SSActionEquipArmor extends SSAction{

	/**
	 * 
	 * @param removedItems - ItemStacks that are removed from the survivor's inventory.
	 * 
	 */
	public SSActionEquipArmor(int armors[]){
		armorIDs = armors;
	}
	
	public SSActionEquipArmor(){
		armorIDs = null;
	}
	
	public void doAction(SSEntityScriptedSurvivor ess){
	}
	
	
	public boolean isActionComplete(SSEntityScriptedSurvivor ess){
		return true;
	}
	
	public void exitAction(SSEntityScriptedSurvivor ess, boolean actionCanceled){
		if(actionCanceled) return;
		
		//equip armors, if not already wearing armor.
		for(int i = 0; i < armorIDs.length; i++){
			if(!ess.pack.equipArmor(armorIDs[i])) break;
		}
	}
	
	/**
	 * 
	 * @param StringSample 249,266
	 * @param Explanation equip these 2 armor pieces in order.
	 * @return EquipArmor action
	 */
	public SSAction createAction(String s){
		try{
			String codes3[] = s.split(","); //armors
			int armor[] = new int[codes3.length];
			//ints
			for(int i = 0; i < codes3.length; i++){
				armor[i] = Integer.valueOf(codes3[i].trim());
			}
			return new SSActionEquipArmor(armor);
		}catch(Exception e){
			return null;
		}
	}
	
	private int armorIDs[];

}
