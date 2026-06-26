package com.dhouse.dhsdk_v2.ui.set.bean;

public class SwitchBean {
    private int type;
    private String name;
    private boolean isOpen;

    public SwitchBean(int type, String name, boolean isOpen) {
        this.type = type;
        this.name = name;
        this.isOpen = isOpen;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }
}
