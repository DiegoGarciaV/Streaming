package com.dinobotica.streams.lib.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.logging.Logger;

import com.dinobotica.streams.dto.Constants;


public class ClientService {

    protected BufferedInputStream dataIn;
    protected BufferedOutputStream dataOut;
    private Socket clientSocket;
    protected boolean connected;    

    private final Logger logger = Logger.getLogger(ClientService.class.getName());

    public boolean isConnected() {
        return connected;
    }

    public ClientService(String server, int port) throws IOException
    {
        clientSocket = new Socket(server, port);
        dataOut = new BufferedOutputStream(clientSocket.getOutputStream());
        dataIn = new BufferedInputStream(clientSocket.getInputStream(),Constants.BUFFER_SIZE);
        connected = true;

    }

    public String sendData(byte[] message)
    {
        String response = "None response\n";
        if(message==null)
            return response;
        try
        {
            dataOut.write(message);
            dataOut.flush();
            byte[] lectura = new byte[Constants.BUFFER_SIZE];
            int readSize = dataIn.read(lectura);     
            if(readSize>0)
            {
                byte[] datareaded = Arrays.copyOf(lectura, readSize);
                response = new String(datareaded);
                connected = !response.equals("Fin de la conexion\n");
            }
            

        }
        catch(SocketException SoE)
        {
            logger.severe("Ha ocurrido un problema con la conexion.");
            if(clientSocket.isConnected())
            {
                logger.severe("El servidor ha cerrado la conexión");
                connected = false;

            }
        }
        catch(IOException IoE)
        {
            logger.severe("Ha ocurrido un problema durante la transmisión del mensaje.");
            IoE.printStackTrace();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return response;
    }

    public void sendDataNonResponse(byte[] message)
    {
        try
        {
            if(message!=null)
            {
                dataOut.write(message);
                dataOut.flush();
            }

        }
        catch(SocketException SoE)
        {
            logger.severe("Ha ocurrido un problema con la conexion.");
            if(clientSocket.isConnected())
            {
                logger.severe("El servidor ha cerrado la conexión");
                connected = false;

            }
        }
        catch(IOException IoE)
        {
            logger.severe("Ha ocurrido un problema durante la transmisión del mensaje.");
            IoE.printStackTrace();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
    }

    public void closeConnection()
    {
        try
        {
            logger.info("Cerrando conexión del socket " + clientSocket.getLocalPort());
            clientSocket.close();
            connected = false;
        }
        catch(IOException ioException)
        {
            logger.severe("Ha ocurrido un problema al finalizar la comunicación");
            ioException.printStackTrace();
        }
    }

}
