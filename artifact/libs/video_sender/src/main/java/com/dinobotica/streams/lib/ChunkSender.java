package com.dinobotica.streams.lib.client;

import java.io.IOException;
import java.util.logging.Logger;

public class ChunkSender extends ClientService implements Runnable{
    
    private byte[] bytesChunk;

    private final Logger logger = Logger.getLogger(ChunkSender.class.getName());

    public ChunkSender(String server, int port, byte[] bytesChunk) throws IOException{
        super(server, port);
        this.bytesChunk = bytesChunk;
    }

    @Override
    public void run() {
        
        logger.info(sendData(bytesChunk));
        
    }

    
}
