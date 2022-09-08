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
            ClientService[] clientService = new ClientService[Constants.CHUNK_RATE];
            for(int j = 0;j<Constants.CHUNK_RATE;j++)
            {
                clientService[j] = new ClientService(host,port);
                for(int i=0;i<frames;i++)
                {
                    new Thread(new ChunkSender(webcam,i,j,messageDTO,clientService[j])).start();
                    // ChunkSender chunkSender = new ChunkSender(webcam,i,messageDTO,clientService);
                    // chunkSender.run();
                }
                //messageDTO.getParams().replace(FRAMES_COUNT, 0);
            }
            while((Integer)messageDTO.getParams().get(FRAMES_COUNT)<(frames*15));
            for(int j = 0;j<Constants.CHUNK_RATE;j++)
            {
                clientService[j].sendData("_END_OF_MSG_".getBytes());
                clientService[j].closeConnection();
            }
                
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
