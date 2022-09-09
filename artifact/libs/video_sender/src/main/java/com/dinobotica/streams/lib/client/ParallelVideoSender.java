package com.dinobotica.streams.lib.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.awt.Dimension;

import com.dinobotica.streams.dto.Constants;
import com.dinobotica.streams.dto.MessageDTO;


import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

public class ParallelVideoSender{
    
    private String host;
    private MessageDTO messageDTO = new MessageDTO();

    public static final String FRAMES_COUNT = "framesCount";
    public static final String CHUNK_COUNT = "chunkCount";

    private final Logger logger = Logger.getLogger(ParallelVideoSender.class.getName());

    public ParallelVideoSender(String host)
    {
        this.host = host;
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
        
        messageDTO.getParams().put(FRAMES_COUNT, 0);
        messageDTO.getParams().put(CHUNK_COUNT, 1L);
        
        try
        {
            ClientService[] clientService = new ClientService[Constants.FRAME_RATE];
            for(int j = 0;j<Constants.FRAME_RATE;j++)
                clientService[j] = new ClientService(host,Constants.START_PORT + j);

            logger.info("Capturando");
            for(int j = 0;j<Constants.CHUNK_RATE;j++)
            {
                for(int i=0;i<Constants.FRAME_RATE;i++)
                    new Thread(new ChunkSender(webcam,i,j,messageDTO,clientService[i])).start();
            }

            while((Integer)messageDTO.getParams().get(FRAMES_COUNT)<(Constants.FRAME_RATE*Constants.CHUNK_RATE));

            logger.info("Fin de la transmision, cerrando conexiones");
            for(int j = 0;j<Constants.FRAME_RATE;j++)
            {
                clientService[j].sendData("_END_OF_MSG_".getBytes());
                logger.log(Level.INFO,"Conexion {0} cerrada",j);
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
        ParallelVideoSender parallelVideoSender = new ParallelVideoSender(servidor);
        parallelVideoSender.takePicture();
    }

}
