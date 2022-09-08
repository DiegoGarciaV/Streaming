package com.dinobotica.streams.lib.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Logger;

import com.dinobotica.streams.dto.Constants;

public class FrameReader implements Runnable{

    protected BufferedInputStream dataIn;
    protected BufferedOutputStream dataOut;
    private Socket clientSocket;
    private boolean endConnection = false;
    private String chunkId = "1";
    private int frameIndex;

    private static final String END_MESSAJE = "_END_OF_MSG_";

    int contador = 0;

    private final Logger logger = Logger.getLogger(FrameReader.class.getName());

    public FrameReader(Socket clienSocket) throws IOException
    {
        this.clientSocket = clienSocket;
        this.frameIndex = this.clientSocket.getPort() - Constants.START_PORT;
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
            ByteArrayOutputStream concatBytes = new ByteArrayOutputStream();
            String stringDataReaded = "";
            int fullReadSize = 0;
            do
            {
                byte[] lectura = new byte[Constants.BUFFER_SIZE];
                int readSize = dataIn.read(lectura);
                if(readSize < 0)
                    break;
                fullReadSize=+ readSize;
                byte[] datareaded = Arrays.copyOf(lectura, readSize);
                concatBytes.write(datareaded);
                stringDataReaded = new String(concatBytes.toByteArray());
            }
            while(!(stringDataReaded.contains("{") && stringDataReaded.contains("}")));

            byte[] fullDataReaded = concatBytes.toByteArray();
            getString(stringDataReaded);

            if(!endConnection && fullReadSize > 0)
                writeReadedChunk(fullDataReaded,stringDataReaded);
                      
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
        
        ByteArrayOutputStream concatBytes = new ByteArrayOutputStream();
        if(stringDataReaded.contains("chunkId"))
            chunkId = stringDataReaded.split(":")[1].replace(",\"time\"", "").replace(" ", "");

        if(stringDataReaded.contains("frameIndex"))
            frameIndex = Integer.parseInt(stringDataReaded.split(":")[3].replace(",\"image\"", "").replace(" ", ""));

        String formatedChunkId = String.format("%04d", Integer.parseInt(chunkId));
        boolean initalFrame = (frameIndex == 0);
        String finalPath = Constants.FRAMES_PATH + "FramesChunk_" + formatedChunkId + ".json";
        byte[] initialChar = (initalFrame ? "[".getBytes() : ",".getBytes());
        try(BufferedOutputStream frameWriter = new BufferedOutputStream(new FileOutputStream(finalPath,!initalFrame),Constants.BUFFER_SIZE))
        {
            concatBytes.write(initialChar);
            concatBytes.write(datareaded);
            if((frameIndex + 1) == Constants.FRAME_RATE)
            {
                concatBytes.write("]".getBytes());
            }
            frameWriter.write(concatBytes.toByteArray());
        }
        catch (Exception e) {
            logger.warning(e.toString());
        }
    }

    public boolean isEndConnection() {
        return endConnection;
    }
    
}
