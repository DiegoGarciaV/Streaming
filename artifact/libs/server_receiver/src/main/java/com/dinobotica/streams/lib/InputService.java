package com.dinobotica.streams.lib.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

import com.dinobotica.streams.dto.MessageDTO;

public class InputService{

    private ServerSocket socket;
    private static final int MAX_CONNECTIONS = 10;
    private static final int INACTIVITY_TIMEOUT = 300000;

    private MessageDTO messageDTO = new MessageDTO();

    
    
    public MessageDTO getContainer() {
        return messageDTO;
    }

    public void setContainer(MessageDTO messageDTO) {
        this.messageDTO = messageDTO;
    }

    private final Logger logger = Logger.getLogger(InputService.class.getName());
    
    public void start(int port)
    {
        messageDTO.setMessage("");
        logger.info("Iniciando servicio");
        try{
            int connections = 0;
            socket = new ServerSocket(port);
            while(connections < MAX_CONNECTIONS)
            {
                socket.setSoTimeout(INACTIVITY_TIMEOUT);
                Socket clientSocket = socket.accept();
                new Thread(new ConnectionAttender(clientSocket,messageDTO)).start();
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

    public void finalizeService()
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
        inputService.finalizeService();
    }
    
}