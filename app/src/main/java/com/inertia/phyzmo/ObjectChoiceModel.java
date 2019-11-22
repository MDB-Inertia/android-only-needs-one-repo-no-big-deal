package com.inertia.phyzmo;

public class ObjectChoiceModel {

    private String mName;
    private boolean mEnabled;

    public ObjectChoiceModel() {
        this.mName = "";
        this.mEnabled = false;
    }

    public ObjectChoiceModel(String name, boolean enabled) {
        this.mName = name;
        this.mEnabled = enabled;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean mEnabled) {
        this.mEnabled = mEnabled;
    }
}
