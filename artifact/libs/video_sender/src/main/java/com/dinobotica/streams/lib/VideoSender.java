package com.dinobotica.streams.lib.client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;

public class VideoSender extends ClientService implements Runnable{

    private final Logger logger = Logger.getLogger(VideoSender.class.getName());
    
    public VideoSender(String server, int port) throws IOException {
        super(server, port);
    }

    @Override
    public void run() {
        
        Webcam webcam = Webcam.getDefault();
        webcam.open();
        try 
        {
            BufferedImage frame = webcam.getImage();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(frame, "PNG", baos );
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close(); 
            String img = new String(imageInByte);
            sendData(img);
        } 
        catch (IOException e) {
            
            e.printStackTrace();
        }
        
    }

    public static void main(String[] args) {
        
        try
        {
            VideoSender videoSender = new VideoSender("localhost", 6666);
            videoSender.run();
            videoSender.closeConnection();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        
    }

    
}
