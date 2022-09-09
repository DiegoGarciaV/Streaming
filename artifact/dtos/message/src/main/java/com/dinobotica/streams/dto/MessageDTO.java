package com.dinobotica.streams.dto;

import java.io.Serializable;
import java.util.Map;

public class MessageDTO implements Serializable{
    
    private String message;
    private byte[][] buffer = new byte[Constants.FRAME_RATE][];
    private Map<String,Object> params;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public byte[][] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[][] buffer) {
        this.buffer = buffer;
    }

    public Map<String,Object> getParams() {
        return params;
    }
    
    public void setParams(Map<String,Object> params) {
        this.params = params;
    }

    
}
