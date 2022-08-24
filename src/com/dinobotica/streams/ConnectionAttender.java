package com.dinobotica.streams;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

public class ConnectionAttender implements Runnable{

    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private Socket clientSocket;
    private boolean endConnection = false;
    private MessageDTO messageDTO;

    private static final String END_MESSAJE = "_END_OF_MSG_";
    private static final String SET_DATA = "_SET_";
    private static final String GET_DATA = "_GET_";
    private static final String SEPARATOR = "_M_";

    private final Logger logger = Logger.getLogger(ConnectionAttender.class.getName());

    public ConnectionAttender(Socket clienSocket,MessageDTO messageDTO) throws IOException
    {
        this.clientSocket = clienSocket;
        dataIn = new DataInputStream(clienSocket.getInputStream());
        dataOut = new DataOutputStream(clienSocket.getOutputStream());
        this.messageDTO = messageDTO;
        
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void setData(MessageDTO messageDTO) {
        this.messageDTO = messageDTO;
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
                if(readedData.contains(END_MESSAJE))
                {
                    dataOut.writeUTF("Fin de la conexion\n");
                    endConnection = true;
                }
                else if(readedData.contains(SET_DATA))
                {
                    String newData = readedData.split(SEPARATOR)[1];
                    messageDTO.setMessage(newData);
                    dataOut.writeUTF("Mensaje recibido y almacenado\n");
                }
                else if(readedData.contains(GET_DATA))
                {
                    dataOut.writeUTF(messageDTO.getMessage());
                }
                else
                {
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
        catch(SocketException e)
        {
            logger.info("El cliente cerro la conexión de forma inesperada " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getLocalPort());
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
