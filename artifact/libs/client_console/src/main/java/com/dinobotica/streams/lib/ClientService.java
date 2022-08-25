package com.dinobotica.streams.lib.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;


public class ClientService {

    protected ObjectInputStream dataIn;
    protected ObjectOutputStream dataOut;
    private Socket clientSocket;
    protected boolean connected;    

    private final Logger logger = Logger.getLogger(ClientService.class.getName());

    public boolean isConnected() {
        return connected;
    }

    public ClientService(String server, int port) throws IOException
    {
        clientSocket = new Socket(server, port);
        dataOut = new ObjectOutputStream(clientSocket.getOutputStream());
        dataIn = new ObjectInputStream(clientSocket.getInputStream());
        connected = true;

    }

    public String sendData(Object message)
    {
        String response = "None response\n";
        long initTime = System.currentTimeMillis();
        try
        {
            dataOut.writeObject(message);
            long endTime = System.currentTimeMillis();
            logger.info("Tiempo de ejecucion: " + (endTime - initTime) + "ms");
            response = (String)dataIn.readObject();
            logger.info(response);
            connected = !response.equals("Fin de la conexion\n");

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
