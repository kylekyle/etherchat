package edu.usma.etherchat;

class Message {

    private final String user;
    private final String text;

    Message(String user, String text) {
        this.user = user;
        this.text = text;
    }

    public String getUser() {
        return user;
    }

    public String getText() {
        return text;
    }
}
