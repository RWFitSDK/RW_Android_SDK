package com.dhouse.dhsdk_v2.ui.set.bean;

public class AlarmBean {
    private int hour;
    private int min;
    private boolean isSelected;
    private int[] repeat;
    private String tag;

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int[] getRepeat() {
        return repeat;
    }

    public void setRepeat(int[] repeat) {
        this.repeat = repeat;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
