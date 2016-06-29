package edu.iit.bluetoothbeacon.models;

public class Translation {

    private String title;
    private String content;

    public Translation(String title, String content){
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }
    public String getContent() {
        return content;
    }
}