package com.dinobotica.streams.lib.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;
import java.awt.Dimension;

import com.dinobotica.streams.dto.Constants;
import com.dinobotica.streams.dto.MessageDTO;


import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

public class ParallelVideoSender{
    
    private String host;
    private int port;
    private MessageDTO messageDTO = new MessageDTO();

    public static final String FRAMES_COUNT = "framesCount";
    public static final String CHUNK_COUNT = "chunkCount";

    private final Logger logger = Logger.getLogger(ParallelVideoSender.class.getName());

    public ParallelVideoSender(String host, int port)
    {
        this.host = host;
        this.port = port;
        messageDTO.setMessage("");
        messageDTO.setParams(new HashMap<>());
    }

    
    public void takePicture()
    {
        Webcam webcam = Webcam.getDefault();    
        Dimension fullHD = WebcamResolution.WVGA.getSize();
        webcam.setCustomViewSizes(fullHD);
        webcam.setViewSize(fullHD);
        while(!webcam.open());
        

        long frames = Constants.FRAME_RATE;
        logger.info("Capturando");
        messageDTO.getParams().put(FRAMES_COUNT, 0);
        messageDTO.getParams().put(CHUNK_COUNT, 1L);
        
        try
        {
            ClientService clientService = new ClientService(host,port);
            for(int j = 0;j<15;j++)
            {
                for(int i=0;i<frames;i++)
                {
                    new Thread(new ChunkSender(webcam,i,messageDTO,clientService)).start();
                    // ChunkSender chunkSender = new ChunkSender(webcam,i,messageDTO,clientService);
                    // chunkSender.run();
                }
                while((Integer)messageDTO.getParams().get(FRAMES_COUNT)<(frames));
                
                messageDTO.getParams().replace(CHUNK_COUNT, (long)messageDTO.getParams().get(CHUNK_COUNT) + 1);
                messageDTO.getParams().replace(FRAMES_COUNT, 0);
            }
            clientService.sendData("_END_OF_MSG_".getBytes());
            clientService.closeConnection();
                
        }
        catch(IOException e){
            e.printStackTrace();
        }
        
        webcam.close();
    }

    public static void main(String[] args) {
        
        String servidor = args[0];
        int port = Integer.parseInt(args[1]);
        ParallelVideoSender parallelVideoSender = new ParallelVideoSender(servidor, port);
        parallelVideoSender.takePicture();
    }

}
