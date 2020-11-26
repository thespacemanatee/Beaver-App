package com.example.beever.database;

import android.util.Log;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class ChatEntry {
    private String sender = null, message = null, attachment = null;
    private Timestamp time = null;

    public ChatEntry(){}

    public ChatEntry(Object o){
        if (!(o instanceof Map)) {
            Log.d("ChatEntry creation","Passed object is not a Map");
            return;
        }
        Map<String,Object> map = (Map<String,Object>) o;
        setSender((String) map.get("sender"));
        setMessage((String) map.get("message"));
        setAttachment((String) map.get("attachment"));
        setTime((Timestamp) map.get("time"));
    }

    public ChatEntry(String sender, String message, String attachment, Timestamp time){
        setSender(sender);
        setMessage(message);
        setAttachment(attachment);
        setTime(time);
    }

    private void setSender(String name){
        this.sender = sender;
    }

    private void setMessage(String message){
        this.message = message;
    }

    private void setAttachment(String attachment){
        this.attachment = attachment;
    }

    private void setTime(Timestamp time){
        this.time = time;
    }

    public String getSender(){
        return sender;
    }

    public String getMessage(){
        return message;
    }

    public String getAttachment(){
        return attachment;
    }

    public Timestamp getTime(){
        return time;
    }

    public Map<String, Object> getRepresentation(){
        HashMap<String, Object> ret = new HashMap<String, Object>();
        ret.put("sender", sender);
        ret.put("message", message);
        ret.put("attachment", attachment);
        ret.put("time", time);
        return ret;
    }

    public boolean equals(Object o){
        if (o==this) return true;
        if (!(o instanceof ChatEntry)){return false;}
        ChatEntry other = (ChatEntry) o;
        return sender.equals(other.getSender()) && message.equals(other.getMessage()) && attachment.equals(other.getAttachment()) && time.equals(other.getTime());
    }

    // Print to string, mostly for debugging
    public String toString(){
        return "EventEntry({sender=" + sender + ",\n"
                + "message=" + message + ",\n"
                + "attachment=" + attachment + ",\n"
                + "time=" + time.toString() + "})";
    }

}
