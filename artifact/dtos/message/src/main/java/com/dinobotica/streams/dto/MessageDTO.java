package com.dinobotica.streams.dto;

import java.io.Serializable;

public class MessageDTO implements Serializable{
    
    private final long id = 1029373456L;
    private String message;
    private byte buffer[][] = new byte[Constants.FRAME_RATE][];

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

    

    
}
