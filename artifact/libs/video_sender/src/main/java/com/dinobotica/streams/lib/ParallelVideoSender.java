package com.dinobotica.streams.lib.client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.logging.Logger;

import com.dinobotica.streams.dto.MessageDTO;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

public class ParallelVideoSender{
    
    private String host;
    private int port;
    private BufferedImage image;
    private static final int CHANNELS = 8;
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
        webcam.open();
        long initTime = System.currentTimeMillis();
        try{
            ClientService clientService = new ClientService(host,port);
            for(int i=0;i<72;i++)
                {
                    BufferedImage frame = webcam.getImage();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try{
                        ImageIO.write(frame, "PNG", baos );
                        baos.flush();
                        byte[] imageInByte = baos.toByteArray();
                        baos.close(); 
                        int imageInByteSize = imageInByte.length;
                        //logger.info("Tamanio de la transmision: " + imageInByteSize + ", Cuadro " + i);
                        // for(int j=0; j<CHANNELS-1; j++)
                        // {
                        //     byte[] imageInByteChunked = new byte[imageInByteSize/CHANNELS];
                        //     System.arraycopy(imageInByte, ((imageInByteSize/CHANNELS)*j), imageInByteChunked, 0, imageInByteChunked.length);
                        //     new Thread(new ChunkSender(host,port,imageInByteChunked)).start();
                        // }
                        byte[] imageInByteChunked = new byte[imageInByteSize/CHANNELS + imageInByteSize%CHANNELS];
                        System.arraycopy(imageInByte, ((imageInByteSize/CHANNELS)*(CHANNELS-1)), imageInByteChunked, 0, imageInByteChunked.length);
                        // new Thread(new ChunkSender(host,port,imageInByteChunked)).start();
                        //new Thread(new ChunkSender(host,port,imageInByte)).start();
                        clientService.sendData(imageInByte);
                    }
                    catch(IOException e){}
                    
                    // String img = new String(imageInByte);
                    // String respouesta = sendData(img);
                    //logger.info(respouesta); 
                }
                clientService.closeConnection();
            }
            catch(IOException e){}

        webcam.close();
    }

    public static void main(String[] args) {
        
        ParallelVideoSender parallelVideoSender = new ParallelVideoSender("localhost", 6666);
        parallelVideoSender.takePicture();
    }

}
