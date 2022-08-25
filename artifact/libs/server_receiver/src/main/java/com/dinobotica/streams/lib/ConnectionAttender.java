package com.dinobotica.streams.lib.server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

import javax.print.DocFlavor.BYTE_ARRAY;

import com.dinobotica.streams.dto.MessageDTO;

public class ConnectionAttender implements Runnable{

    protected BufferedInputStream dataIn;
    protected ObjectOutputStream dataOut;
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
        dataOut = new ObjectOutputStream(clienSocket.getOutputStream());
        dataIn = new BufferedInputStream(clienSocket.getInputStream(),1024<<9);
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
            logger.info("Finaliza comunicación en socket " + clientSocket.getInetAddress());
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
                
                byte[] streamReceived = new byte[1024<<9];
                Object receivedData = dataIn.read(streamReceived);
                
                //String readedData = (String)receivedData;
                String readedData = new String(streamReceived);
                if(readedData.contains(END_MESSAJE))
                {
                    dataOut.writeObject("Fin de la conexion\n");
                    dataOut.flush();
                    
                    endConnection = true;
                }
                else if(readedData.contains(SET_DATA))
                {
                    String newData = readedData.split(SEPARATOR)[1];
                    messageDTO.setMessage(newData);
                    dataOut.writeObject("Mensaje recibido y almacenado\n");
                    dataOut.flush();
                }
                else if(readedData.contains(GET_DATA))
                {
                    dataOut.writeObject(messageDTO.getMessage());
                    dataOut.flush();
                }
                else
                {
                    int longitud = readedData.length();
                    // logger.info("Mensaje recibido: " + longitud);
                    dataOut.writeObject("Longitud del mensaje: " + longitud + "\n");
                    
                }
                
                
                    
            }
        } 
        catch(EOFException e)
        {
            logger.info("No hay datos por leer en socket " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getLocalPort());
            //e.printStackTrace();
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
