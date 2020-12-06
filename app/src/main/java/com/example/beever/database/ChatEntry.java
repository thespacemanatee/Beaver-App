/*
Copyright (c) 2020 Beever

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package com.example.beever.database;

import android.util.Log;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to represent chat entries in the Firestore database.
 * This class is partly for auto-generation via UserEntry.class and GroupEntry.class.
 *
 * Chat entries follow this contract, the ChatEntry contract, in Firestore:
 * - [Event number in array]: Map<Object>
 *    - sender: String
 *    - message: String (nullable)
 *    - attachment: String (nullable)
 *    - end_time: Timestamp
 *
 * To create an instance of this class manually, use only the 3rd constructor.
 **/
public class ChatEntry implements MapEntry {
    private String sender = null, message = null, attachment = null;
    private Timestamp time = null;

    /**
     * No-arg constructor for debugging
     */
    public ChatEntry(){}

    /**
     * Constructor to create an ChatEntry from a Map<String,Object>, usually to extract
     * ChatEntry objects from existing UserEntry/GroupEntry objects. If passed Object is not
     * a Map, behaviour is mostly the same as the no-arg constructor.
     * @param o object to convert to EventEntry, must be a Map
     */
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

    /**
     * Constructor for manually generating ChatEntry
     * @param sender sender's user ID
     * @param message message if any, or null
     * @param attachment attachment URL if any, or null
     * @param time time at which message was sent
     */
    public ChatEntry(String sender, String message, String attachment, Timestamp time){
        setSender(sender);
        setMessage(message);
        setAttachment(attachment);
        setTime(time);
    }

    // Setters

    public void setSender(String sender){
        this.sender = sender;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public void setAttachment(String attachment){
        this.attachment = attachment;
    }

    public void setTime(Timestamp time){
        this.time = time;
    }

    // Getters

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

    /**
     * Get equivalent Map object representation which obeys ChatEntry contract,
     * for addition to UserEntry/GroupEntry
     * @return Map object representation
     */
    public Map<String, Object> retrieveRepresentation(){
        HashMap<String, Object> ret = new HashMap<String, Object>();
        ret.put("sender", sender);
        ret.put("message", message);
        ret.put("attachment", attachment);
        ret.put("time", time);
        return ret;
    }

    /**
     * Check if this ChatEntry object equals another object
     * @param o object to check equality with
     * @return boolean for whether this object equals the other object
     */
    public boolean equals(Object o){
        if (o==this) return true;
        if (!(o instanceof ChatEntry)){return false;}
        ChatEntry other = (ChatEntry) o;
        return retrieveRepresentation().equals(other.retrieveRepresentation());
    }

    /**
     * Get string representation of this ChatEntry
     * @return string representation
     */
    public String toString(){
        return "EventEntry({sender=" + sender + ",\n"
                + "message=" + message + ",\n"
                + "attachment=" + attachment + ",\n"
                + "time=" + time.toString() + "})";
    }

}
