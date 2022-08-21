package ht.treechop.common.settings;

public class EntityChopSettings extends ChopSettings {

    private boolean isSynced = false;

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced() {
        this.isSynced = true;
    }

}
