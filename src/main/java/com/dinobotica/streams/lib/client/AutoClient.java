package com.dinobotica.streams.lib.client;

import java.io.IOException;
import java.util.logging.Logger;

import com.dinobotica.streams.lib.server.ConnectionAttender;

public class AutoClient extends ClientService implements Runnable{
    
    private final Logger logger = Logger.getLogger(ConnectionAttender.class.getName());

    public AutoClient(String server, int port) throws IOException {
        super(server, port);
    }

    @Override
    public void run() {
        
        boolean endSendingData = false;
        long endExecutionTime = System.currentTimeMillis() + 10000L;
        long attempts = 0;
        while(!endSendingData)
        {
            sendData(Long.toString(System.currentTimeMillis()));
            logger.info(++attempts + " mensajes enviados.");

            if(System.currentTimeMillis() > endExecutionTime)
                endSendingData = true;
        }
        closeConnection();
    }

    public static void main(String[] args) {
        
        try
        {
            AutoClient autoClient = new AutoClient("localhost", 6666);
            new Thread(autoClient).start();

        }
        catch(IOException IoE)
        {
            System.err.println("Error al inicializar Cliente.");
        }
        
    }
    
}
