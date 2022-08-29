package com.dinobotica.streams.lib.client;

import java.io.IOException;
import java.util.logging.Logger;

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
    }

    public void takePicture()
    {
        Webcam webcam = Webcam.getDefault();
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        while(!webcam.open());
        

        long frames = Constants.FRAME_RATE;
        logger.info("Capturando");
        this.messageDTO.setMessage("0");
        try
        {
            ClientService clientService = new ClientService(host,port);
            for(int i=0;i<frames;i++)
            {
                new Thread(new ChunkSender(webcam,i,messageDTO,clientService)).start();
            }
            while(Integer.parseInt(messageDTO.getMessage())<(frames));
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
