package com.dinobotica.streams.dto;

import java.io.Serializable;

public class Frame implements Serializable{

    private long chunkId;
    private String b64Image;
    private int frameIndex;

    
    public long getChunkId() {
        return chunkId;
    }
    public void setChunkId(long chunkId) {
        this.chunkId = chunkId;
    }
    public String getB64Image() {
        return b64Image;
    }
    public void setB64Image(String b64Image) {
        this.b64Image = b64Image;
    }
    public int getFrameIndex() {
        return frameIndex;
    }
    public void setFrameIndex(int frameIndex) {
        this.frameIndex = frameIndex;
    }

    


}
