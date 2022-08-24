package com.dinobotica.streams;

import java.io.Serializable;

public class MessageDTO implements Serializable{
    
    private final long id = 1029373456L;
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    
}
