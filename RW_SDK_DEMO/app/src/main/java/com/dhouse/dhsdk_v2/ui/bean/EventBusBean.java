package com.dhouse.dhsdk_v2.ui.bean;

public class EventBusBean {
    public static class LogEvent{
        private String content;
        private int type;
        public LogEvent(String content) {
            this.content = content;
        }

        public LogEvent(String content, int type) {
            this.content = content;
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
