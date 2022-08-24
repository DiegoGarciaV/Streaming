package com.dinobotica.streams.lib.client;

import java.io.File;
import java.io.IOException;
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
        String currentData = this.sendData("_GET_");
        try 
        {
            ImageIO.write(webcam.getImage(), "PNG", new File("hello-world.png"));
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
