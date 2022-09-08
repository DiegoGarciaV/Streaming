package com.dinobotica.streams.lib.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

import com.dinobotica.streams.dto.Constants;

public class VideoReceiver implements Runnable{

    private int port;

    private static final int INACTIVITY_TIMEOUT = 300000;

    public VideoReceiver(int port)
    {
        this.port = port;
    }
    
    private final Logger logger = Logger.getLogger(VideoReceiver.class.getName());
    
    
    @Override
    public void run() {
        
        try(ServerSocket socket = new ServerSocket(port))
        {
            socket.setSoTimeout(INACTIVITY_TIMEOUT);
            Socket clientSocket = socket.accept();
            FrameReader frameReader;
            frameReader = new FrameReader(clientSocket);
            frameReader.run();
        }
        catch(SocketTimeoutException soTmE)
        {   
            logger.warning("No se han recibido peticiones durante el perido maximo de espera. El serivcio se cerrara.");
        }
        catch(IOException ioE)
        {
            ioE.printStackTrace();
        }
        
    }

    public static void main(String[] args) {
        
        for(int j = 0; j < Constants.FRAME_RATE; j++)
            new Thread(new VideoReceiver(Constants.START_PORT + j));
            
    }
    
}
