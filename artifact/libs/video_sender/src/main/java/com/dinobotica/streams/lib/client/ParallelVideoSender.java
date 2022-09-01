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

    private final Logger logger = Logger.getLogger(ParallelVideoSender.class.getName());

    public ParallelVideoSender(String host, int port)
    {
        this.host = host;
        this.port = port;
        messageDTO.setMessage("");
        messageDTO.setParams(new HashMap<String,Object>());
    }

    
    public void takePicture()
    {
        Webcam webcam = Webcam.getDefault();    
        Dimension FullHD = WebcamResolution.WVGA.getSize();
        webcam.setCustomViewSizes(FullHD);
        webcam.setViewSize(FullHD);
        // webcam.setViewSize(WebcamResolution.VGA.getSize());
        while(!webcam.open());
        

        long frames = Constants.FRAME_RATE;
        logger.info("Capturando");
        messageDTO.getParams().put("framesCount", 0);
        messageDTO.getParams().put("ChunkCount", 1L);
        
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
                while((Integer)messageDTO.getParams().get("framesCount")<(frames));
                
                messageDTO.getParams().replace("ChunkCount", (long)messageDTO.getParams().get("ChunkCount") + 1);
                messageDTO.getParams().replace("framesCount", 0);
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
        
        ParallelVideoSender parallelVideoSender = new ParallelVideoSender("localhost", 6666);
        parallelVideoSender.takePicture();
    }

}
