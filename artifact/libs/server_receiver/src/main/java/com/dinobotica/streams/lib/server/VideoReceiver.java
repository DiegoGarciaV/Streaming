package com.dinobotica.streams.lib.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dinobotica.streams.dto.Constants;
import com.dinobotica.streams.dto.MessageDTO;

public class VideoReceiver implements Runnable{

    private int port;
    private MessageDTO messageDTO;

    private static final int INACTIVITY_TIMEOUT = 300000;

    private static final Logger logger = Logger.getLogger(VideoReceiver.class.getName());

    public VideoReceiver(int port, MessageDTO messageDTO)
    {
        this.port = port;
        this.messageDTO = messageDTO;
    }
    
    @Override
    public void run() {
        
        try(ServerSocket socket = new ServerSocket(port))
        {
            logger.log(Level.INFO,"Iniciando servicio en puerto {0}",port);
            socket.setSoTimeout(INACTIVITY_TIMEOUT);
            Socket clientSocket = socket.accept();
            FrameReader frameReader;
            frameReader = new FrameReader(clientSocket,messageDTO);
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
        
        File framesDir = new File(Constants.FRAMES_PATH);
        
        if (!framesDir.exists() && !framesDir.mkdirs()) 
        {
            logger.info("No fue posible crear carpeta");
        }
        else if(framesDir.exists())
        {
            File[] frames = framesDir.listFiles();
            for(int j = 0; j < frames.length; j++)
            {
                if(!frames[j].delete())
                    logger.log(Level.WARNING,"No fue posible borrar el frameChunk {0}",j);
            }
        }
                
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setParams(new HashMap<>());
        for(int i = 0;i<Constants.CHUNK_RATE; i++)
            messageDTO.getParams().put("" + (i+1), new LinkedList<Integer>());
        for(int j = 0; j < Constants.FRAME_RATE; j++)
            new Thread(new VideoReceiver(Constants.START_PORT + j,messageDTO)).start();

    }
    
}
