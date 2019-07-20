package com.myans.mandirihackathon.model;

public class ChatModel {
    public static final int USER_INPUT = 1;
    public static final int SYSTEM_INPUT = 2;
    public static final int SYSTEM_TYPING_INPUT = 3;

    private int from;
    private String message;
    private String sendTime;

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public ChatModel(int from, String message, String sendTime) {
        this.from = from;
        this.message = message;
        this.sendTime = sendTime;
    }


    public ChatModel() {
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
