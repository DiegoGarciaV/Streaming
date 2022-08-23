package com.dinobotica.streams;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

public class ConnectionAttender implements Runnable{

    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private Socket clientSocket;
    private boolean endConnection = false;

    private final String END_MESSAJE = "_END_OF_MSG_";

    private final Logger logger = Logger.getLogger(ConnectionAttender.class.getName());

    public ConnectionAttender(Socket clienSocket) throws IOException
    {
        this.clientSocket = clienSocket;
        dataIn = new DataInputStream(clienSocket.getInputStream());
        dataOut = new DataOutputStream(clienSocket.getOutputStream());
        
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }



    @Override
    public void run() {
        logger.info("Comenzando comunicación en socket " + clientSocket.getInetAddress());
        while(!endConnection)
            socketCommunication();

        try{
            clientSocket.close();
        }
        catch(Exception e){ e.printStackTrace();}
        
    }

    private void socketCommunication()
    {
        try 
        {
            if(clientSocket.isConnected())
            {
                String readedData = dataIn.readUTF();
                switch(readedData.replace("\n", "").replace("\r", ""))
                {
                    case END_MESSAJE:

                        dataOut.writeUTF("Fin de la conexion\n");
                        endConnection = true;
                        break;

                    default:

                        int longitud = readedData.length();
                        dataOut.writeUTF("Longitud del mensaje: " + longitud + "\n");
                    
                }
            }
        } 
        catch(EOFException e)
        {
            logger.info("No hay datos por leer en socket " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getLocalPort());
            endConnection = true;
        }
        catch (IOException e) {
            e.printStackTrace();
            endConnection = true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            endConnection = true;
        }
    }


    
}
