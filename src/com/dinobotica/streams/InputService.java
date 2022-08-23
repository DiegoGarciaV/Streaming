package com.dinobotica.streams;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

public class InputService{

    private ServerSocket socket;
    private final int maxConnections = 10;
    private final int inactivityTimeOut = 30000;
    
    private final Logger logger = Logger.getLogger(InputService.class.getName());
    
    public void start(int port)
    {
        logger.info("Iniciando servicio");
        try{
            int connections = 0;
            socket = new ServerSocket(port);
            while(connections < maxConnections)
            {
                socket.setSoTimeout(inactivityTimeOut);
                Socket clientSocket = socket.accept();
                new Thread(new ConnectionAttender(clientSocket)).start();
                connections++;
            }
        }
        catch(SocketTimeoutException SoTmE)
        {   
            logger.warning("No se han recibido peticiones durante el perido maximo de espera. El serivcio se cerrara.");
        }
        catch(IOException IoE)
        {
            IoE.printStackTrace();
        }
        
        

    }

    public void finalize()
    {
        logger.info("Cerrando servicio");
        try 
        {
            socket.close();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        
        InputService inputService = new InputService();
        inputService.start(6666);
        inputService.finalize();
    }
    
}