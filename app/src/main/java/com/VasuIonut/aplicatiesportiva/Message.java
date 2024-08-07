package com.VasuIonut.aplicatiesportiva;
public class Message {
    private String message;
    private String senderId;
    private String timestamp;

    public Message(String message, String senderId, String timestamp) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getTimestamp() {
        return timestamp;
    }

}
