package com.dinobotica.streams.lib.client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.dinobotica.streams.dto.MessageDTO;
import com.dinobotica.streams.dto.Constants;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

public class ChunkSender implements Runnable{
    
    private Webcam webcam;
    private int frameIndex;
    private MessageDTO messageDTO;
    private ClientService clientService;
    
    private final Logger logger = Logger.getLogger(ChunkSender.class.getName());

    public ChunkSender(Webcam webcam, int frameIndex, MessageDTO messageDTO, ClientService clientService) throws IOException{
        this.webcam = webcam;
        this.frameIndex = frameIndex;
        this.messageDTO = messageDTO;
        this.clientService = clientService;
    }

    @Override
    public void run() {
        
        
        if(webcam!= null)
        {
            BufferedImage frame = webcam.getImage();
            if(frame!=null)
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream(Constants.BUFFER_SIZE);
                try
                {
                    ImageIO.write(frame, "JPG", baos );
                    double mean = Double.parseDouble(messageDTO.getMessage().split(":")[0]);
                    int num = Integer.parseInt(messageDTO.getMessage().split(":")[1]);

                    mean = num*mean + frameIndex;
                    mean = mean/(num+1);
                    num++;
                    messageDTO.setMessage(mean+":"+num);
                    messageDTO.getBuffer()[frameIndex] = baos.toByteArray();
                    baos.close(); 
                    if(clientService!=null)
                        clientService.sendData(baos.toByteArray());
                    ImageIO.write(frame, "JPG", new FileOutputStream("foto.jpg"));
                    
                }
                catch(Exception e){ e.printStackTrace();}
            }
            
        }
        
    }

    
}
