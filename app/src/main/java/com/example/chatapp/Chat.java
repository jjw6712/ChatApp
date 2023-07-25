package com.example.chatapp;

public class Chat {
    public String receivuser;

    public String getReceivuser() {
        return receivuser;
    }

    public void setReceivuser(String receivuser) {
        this.receivuser = receivuser;
    }

    String email;
    String message;
    String name;
    String time;

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
}
