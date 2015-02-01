package ogresean.scriptedsurvivor;

import net.minecraft.util.ChatComponentText;

public class SSActionSayMessage extends SSAction {

    /**
     * @param s       - message that Survivor will say
     * @param maxDist - distance that player must be within to hear message; -1 for infinite range
     */
    public SSActionSayMessage(String s[], int maxDist) {
        messages = s;
        range = maxDist;
    }

    public SSActionSayMessage() {
        messages = null;
        range = 0;
    }

    public void doAction(SSEntityScriptedSurvivor ess) {
    }


    public boolean isActionComplete(SSEntityScriptedSurvivor ess) {
        return true;
    }

    public void exitAction(SSEntityScriptedSurvivor ess, boolean actionCanceled) {
        if (actionCanceled) return;

        if (range > 0 && ess.getDistanceToEntity(ess.worldObj.getClosestPlayerToEntity(ess, range)) > range) return;
        //say message
        String s = mod_ScriptedSurvivors.scripts.get(ess.type).name.concat(": ").concat(messages[ess.getRNG().nextInt(messages.length)]);
        ess.worldObj.getClosestPlayerToEntity(ess, range).addChatMessage(new ChatComponentText(s));
    }

    /**
     * @param StringSample 10:Hi there buddy!
     * @param Explanation  range = 10; message = obvious
     * @return SayMessage action
     */
    public SSAction createAction(String s) {
        try {
            String codes[] = s.split(":", 2);
            int a = Integer.parseInt(codes[0]);
            String phrases[] = codes[1].split(":");
            return new SSActionSayMessage(phrases, a);
        } catch (Exception e) {
            return null;
        }
    }

    private String messages[];
    private int range;
}
