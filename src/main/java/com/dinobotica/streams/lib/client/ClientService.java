package com.dinobotica.streams.lib.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

import com.dinobotica.streams.lib.server.ConnectionAttender;


public class ClientService {

    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private Socket clientSocket;
    private boolean connected;    

    private final Logger logger = Logger.getLogger(ConnectionAttender.class.getName());

    public boolean isConnected() {
        return connected;
    }

    public ClientService(String server, int port) throws IOException
    {
        logger.info("Iniciando servicio cliente.");
        clientSocket = new Socket(server, port);
        dataIn = new DataInputStream(clientSocket.getInputStream());
        dataOut = new DataOutputStream(clientSocket.getOutputStream());
        connected = true;

    }

    public String sendData(String message)
    {
        String response = "None response\n";
        try
        {
            dataOut.writeUTF(message + "\n");
            response = dataIn.readUTF();
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
            logger.severe(" :: MENSAJE: " + message);
            IoE.printStackTrace();
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
