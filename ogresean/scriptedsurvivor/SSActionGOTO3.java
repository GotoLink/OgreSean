package ogresean.scriptedsurvivor;


/**
 * @name SSActionGOTO3
 * @description This action changes a Survivor's current action depending on how low the survivor's hp is.
 */
public class SSActionGOTO3 extends SSAction {

    /**
     * @param sAction - Action IDs to go to if required items in inventory; one of these IDs is chosen randomly upon success
     * @param fAction - Action IDs to go to if required items not in inventory; one of these IDs is chosen randomly upon failure
     * @param h       - maximum hp for action to return true
     */
    public SSActionGOTO3(int[] sActions, int[] fActions, int h) {
        succeedActions = sActions;
        failActions = fActions;
        maxHP = h;
    }

    public SSActionGOTO3() {
        succeedActions = null;
        failActions = null;
        maxHP = 0;
    }

    public void doAction(SSEntityScriptedSurvivor ess) {
    }


    public boolean isActionComplete(SSEntityScriptedSurvivor ess) {
        return true;
    }

    public void exitAction(SSEntityScriptedSurvivor ess, boolean actionCanceled) {
        if (actionCanceled) return;

        if (lowHealth(ess)) ess.currentAction = succeedActions[ess.getRNG().nextInt(succeedActions.length)] - 2;
        else ess.currentAction = failActions[ess.getRNG().nextInt(failActions.length)] - 2;
    }

    private boolean lowHealth(SSEntityScriptedSurvivor ess) {
        if (ess.getHealth() > maxHP) return false;
        return true;
    }

    /**
     * @param StringSample 5,6:3:10
     * @param Explanation  Succeed-Action 5 or 6, Fail-Action 3, max health for success is 10
     * @return GOTO3 action
     */
    public SSAction createAction(String s) {
        try {
            String codes[] = s.split(":");
            String codes1[] = codes[0].split(","); //success ints
            String codes2[] = codes[1].split(","); //failure ints
            int a[] = new int[codes1.length];
            int b[] = new int[codes2.length];
            int c = Integer.parseInt(codes[2]); //max hp
            //success actions
            for (int i = 0; i < codes1.length; i++) {
                a[i] = Integer.parseInt(codes1[i]);
            }
            //failure actions
            for (int i = 0; i < codes2.length; i++) {
                b[i] = Integer.parseInt(codes2[i]);
            }
            return new SSActionGOTO3(a, b, c);
        } catch (Exception e) {
            return null;
        }
    }

    private int[] succeedActions;
    private int[] failActions;
    private int maxHP;
}
