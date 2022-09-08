package com.dinobotica.streams.dto;

import java.io.Serializable;

public class FrameDTO implements Serializable{

    private long chunkId;
    private long time;
    private String b64Image;
    private int frameIndex;
    private int frameChunkIndex;

    
    public FrameDTO(long chunkId, long time, String b64Image, int frameIndex) {
        this.chunkId = chunkId;
        this.time = time;
        this.b64Image = b64Image;
        this.frameIndex = frameIndex;
    }
    
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
    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }

    public int getFrameChunkIndex() {
        return frameChunkIndex;
    }

    public void setFrameChunkIndex(int frameChunkIndex) {
        this.frameChunkIndex = frameChunkIndex;
    }

    @Override
    public String toString() {
        StringBuilder toStringClass = new StringBuilder("{ \"chunkId\": ").append(chunkId).append(",");
        toStringClass.append("\"time\": ").append(time).append(",");
        toStringClass.append("\"frameIndex\": ").append(frameIndex).append(",");
        toStringClass.append("\"image\": \"").append(b64Image).append("\"}");
        return toStringClass.toString();
    }



}
