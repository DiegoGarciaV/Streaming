package com.dinobotica.streams.lib.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dinobotica.streams.dto.Constants;
import com.dinobotica.streams.dto.MessageDTO;

@SuppressWarnings("unchecked")
public class FrameReader implements Runnable{

    protected BufferedInputStream dataIn;
    protected BufferedOutputStream dataOut;
    
    ByteArrayOutputStream[] concatBytes;
    private Socket clientSocket;
    private boolean endConnection = false;
    private String chunkId = "1";
    private MessageDTO messageDTO;

    private static final String END_MESSAJE = "_END_OF_MSG_";

    int contador = 0;

    private final Logger logger = Logger.getLogger(FrameReader.class.getName());

    public FrameReader(Socket clienSocket, MessageDTO messageDTO, ByteArrayOutputStream[] concatBytes) throws IOException
    {
        this.clientSocket = clienSocket;
        this.messageDTO = messageDTO;
        this.concatBytes = concatBytes;
        dataOut = new BufferedOutputStream(clienSocket.getOutputStream());
        dataIn = new BufferedInputStream(clienSocket.getInputStream(),Constants.BUFFER_SIZE);
        
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
            logger.info("Finaliza comunicación en socket " + clientSocket.getInetAddress());
            clientSocket.close();
        }
        catch(Exception e){ e.printStackTrace();}
        
    }


    private void socketCommunication()
    {
        try 
        {
            ByteArrayOutputStream readedBytes = new ByteArrayOutputStream();
            String stringDataReaded = "";
            int fullReadSize = 0;
            do
            {
                byte[] lectura = new byte[Constants.BUFFER_SIZE];
                int readSize = dataIn.read(lectura);
                if(readSize < 0)
                    break;
                fullReadSize = fullReadSize + readSize;
                byte[] datareaded = Arrays.copyOf(lectura, readSize);
                readedBytes.write(datareaded);
                stringDataReaded = new String(readedBytes.toByteArray());
            }
            while(!(stringDataReaded.contains("{") && stringDataReaded.contains("}")) && !stringDataReaded.contains(END_MESSAJE));
            byte[] fullDataReaded = readedBytes.toByteArray();
            getString(stringDataReaded);
            if(!endConnection && fullReadSize > 0)
            {
                writeReadedChunk(fullDataReaded,stringDataReaded);
            }
            else if(endConnection && ((LinkedList<Integer>)messageDTO.getParams().get(chunkId)).size() < Constants.FRAME_RATE)
                writeOnFile("]".getBytes(), true);
                      
        } 
        catch (IOException e) {
            e.printStackTrace();
            endConnection = true;
        }
        catch(Exception e)
        {
            logger.severe("Excepcion no controlada");
            e.printStackTrace();

            endConnection = true;
        }
        
    }


    public void getString(String readedData) throws IOException
    {
        
        if(clientSocket.isConnected())
        {
            if(readedData.contains(END_MESSAJE))
            {
                dataOut.write("Fin de la conexion\n".getBytes());
                endConnection = true;
            }
            else
            {
                int longitud = readedData.length();

                dataOut.write(("R.- " + longitud).getBytes());
                dataOut.flush();
            }
        }
    }

    private void writeReadedChunk(byte[] datareaded,String stringDataReaded) throws IOException
    {
        if(stringDataReaded.contains("chunkId"))
            chunkId = stringDataReaded.split(":")[1].replace(",\"time\"", "").replace(" ", "");
        int msgLong = datareaded.length;
        logger.log(Level.INFO,"{0}\n",msgLong);
        int chunkIdNum = Integer.parseInt(chunkId) -1;
        int currentInsertedFrames = ((LinkedList<Integer>)messageDTO.getParams().get(chunkId)).size();
        boolean initalFrame = (currentInsertedFrames == 0);
        
        ((LinkedList<Integer>)messageDTO.getParams().get(chunkId)).add(currentInsertedFrames+1);
        
        boolean finalFrame = (currentInsertedFrames == (Constants.FRAME_RATE - 1));
        String initialChar = (initalFrame ? "[" : ",");

        concatBytes[chunkIdNum].write((initialChar + stringDataReaded).getBytes());
        if(finalFrame)
        {
            concatBytes[chunkIdNum].write("]".getBytes());
            writeOnFile(concatBytes[chunkIdNum].toByteArray(),false);
            concatBytes[chunkIdNum].reset();
            ((LinkedList<Integer>)messageDTO.getParams().get(chunkId)).clear();
        }
        
        
    }

    private void writeOnFile(byte[] bytesToWrite, boolean append)
    {
        String formatedChunkId = String.format("%04d", Integer.parseInt(chunkId));
        String finalPath = Constants.FRAMES_PATH + "FramesChunk_" + formatedChunkId + ".json";
        try(BufferedOutputStream frameWriter = new BufferedOutputStream(new FileOutputStream(finalPath,append),Constants.BUFFER_SIZE))
        {
            frameWriter.write(bytesToWrite);
            frameWriter.flush();
        }
        catch (Exception e) {
            logger.warning(e.toString());
        }
    }

    public boolean isEndConnection() {
        return endConnection;
    }
    
}
