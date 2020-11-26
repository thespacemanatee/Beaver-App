package com.example.beever.feature;

public class Events {
    public static final int TEXT_TYPE = 0;
    public static final int IMAGE_TYPE = 1;
    public static final int AUDIO_TYPE = 2;

    public int type;
    public int data;
    public String text;

    public Events(int type, int data, String text){
        this.type = type;
        this.data = data;
        this.text = text;
    }
}
