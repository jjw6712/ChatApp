package com.example.chatapp;

import java.util.Map;

public class ChatRoom {
    String email;
    String name;
    String message;
    String time;
    private String chatRoomKey;
    String partnerName;
    String PartnerEmail;

    public String getPartnerEmail() {
        return PartnerEmail;
    }

    public void setPartnerEmail(String email) {
        this.PartnerEmail = email;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    private Map<String, Boolean> users;

    public Map<String, Boolean> getUsers() {
        return users;
    }

    public void setUsers(Map<String, Boolean> users) {
        this.users = users;
    }

    public ChatRoom() {
        // Default constructor required for calls to DataSnapshot.getValue(ChatRoom.class)
    }
    public ChatRoom(String email, String message, String time) {
        this.email = email;
        this.message = message;
        this.time = time;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChatRoomKey() {
        return chatRoomKey;
    }

    public void setChatRoomKey(String chatRoomKey) {
        this.chatRoomKey = chatRoomKey;
    }


}
