package com.dinobotica.streams.lib.client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.logging.Logger;

import com.dinobotica.streams.dto.Constants;
import com.dinobotica.streams.dto.MessageDTO;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

public class ParallelVideoSender{
    
    private String host;
    private int port;
    private BufferedImage image;
    private static final int CHANNELS = 1;
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
        this.messageDTO.setMessage("0:0");
        try
        {
            ClientService clientService = new ClientService(host,port);
            for(int i=0;i<frames;i++)
            {
                new Thread(new ChunkSender(webcam,i,messageDTO,clientService)).start();
            }
            while(Double.parseDouble(messageDTO.getMessage().split(":")[0])<(frames-1)/2);
            clientService.sendData("_END_OF_MSG_".getBytes());
            clientService.closeConnection();
        }
        catch(IOException e){}
        

        // try
        // {
        //     for(int i=0;i<frames;i++)
        //     {
        //         new Thread(new ChunkSender(webcam,i,messageDTO,null)).start();
        //     }
        //     try{
        //         Thread.sleep(8000);
        //     }
        //     catch(Exception e){}
        // }
        // catch(IOException e){}
        
        // logger.info("Transmitiendo");
        // try
        // {
        //     int i = 0;
        //     ClientService clientService = new ClientService(host,port);
        //     for(i=0;i<frames;i++)
        //     {
        //         byte[] frame = messageDTO.getBuffer()[i];
        //         System.out.println(i);
        //         clientService.sendData(frame);
                
        //     }
        //     clientService.sendData("_END_OF_MSG_".getBytes());
        //     try{
        //         Thread.sleep(3000);
        //     }
        //     catch(Exception e){}
        //     clientService.closeConnection();
        // }
        // catch(IOException e){}
        
        webcam.close();
    }

    public static void main(String[] args) {
        
        ParallelVideoSender parallelVideoSender = new ParallelVideoSender("localhost", 6666);
        parallelVideoSender.takePicture();
    }

}
