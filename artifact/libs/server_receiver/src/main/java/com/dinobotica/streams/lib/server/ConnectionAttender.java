package com.dinobotica.streams.lib.server;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import com.dinobotica.streams.dto.Constants;
import com.dinobotica.streams.dto.MessageDTO;


public class ConnectionAttender implements Runnable{

    protected BufferedInputStream dataIn;
    protected BufferedOutputStream dataOut;
    private Socket clientSocket;
    private boolean endConnection = false;
    private MessageDTO messageDTO;

    private String currentChunk = "-1";

    private static final String END_MESSAJE = "_END_OF_MSG_";
    private static final String SET_DATA = "_SET_";
    private static final String GET_DATA = "_GET_";
    private static final String SEPARATOR = "_M_";

    private Map<String,Integer> chunksCounter = new HashMap<String,Integer>();

    int contador = 0;

    private final Logger logger = Logger.getLogger(ConnectionAttender.class.getName());

    public ConnectionAttender(Socket clienSocket,MessageDTO messageDTO) throws IOException
    {
        this.clientSocket = clienSocket;
        dataOut = new BufferedOutputStream(clienSocket.getOutputStream());
        dataIn = new BufferedInputStream(clienSocket.getInputStream(),Constants.BUFFER_SIZE);
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
            byte[] lectura = new byte[Constants.BUFFER_SIZE];
            int readSize = dataIn.read(lectura);
            byte[] datareaded = Arrays.copyOf(lectura, readSize);
            String stringDataReaded = new String(datareaded);
            getString(stringDataReaded);
            String chunkId = "-2";
            String formatedChunkId = "-2";
            boolean chunkToRead = false;
            File framesDir = new File(Constants.FRAMES_PATH);
            if (!framesDir.exists() && !framesDir.mkdirs()) {
                logger.info("Error al crear directorio de frames");
            }
            if(!endConnection)
                chunkToRead = true;
            else
            {
                BufferedOutputStream lastFrameWriter = new BufferedOutputStream(new FileOutputStream(Constants.FRAMES_PATH + "FramesChunk_" + currentChunk + ".json",true),Constants.BUFFER_SIZE);
                lastFrameWriter.write("]".getBytes());
                lastFrameWriter.close();
            }

            
            if(readSize > 0 && chunkToRead)
            {

                if(stringDataReaded.contains("chunkId"))
                {
                    chunkId = stringDataReaded.split(":")[1].replace(",\"time\"", "").replace(" ", "");
                    formatedChunkId = String.format("%04d", Integer.parseInt(chunkId));
                }
                else
                {
                    formatedChunkId = currentChunk;
                }
                if(currentChunk.equals("-1"))
                {
                    BufferedOutputStream frameWriter = new BufferedOutputStream(new FileOutputStream(Constants.FRAMES_PATH + "FramesChunk_" + formatedChunkId + ".json"),Constants.BUFFER_SIZE);
                    ByteArrayOutputStream concatBytes = new ByteArrayOutputStream();
                    concatBytes.write("[".getBytes());
                    concatBytes.flush();
                    concatBytes.write(datareaded);
                    concatBytes.flush();
                    frameWriter.write(concatBytes.toByteArray());
                    frameWriter.close();
                }
                else if(currentChunk.equals(formatedChunkId))
                {
                    BufferedOutputStream frameWriter = new BufferedOutputStream(new FileOutputStream(Constants.FRAMES_PATH + "FramesChunk_" + formatedChunkId + ".json",true),Constants.BUFFER_SIZE);
                    ByteArrayOutputStream concatBytes = new ByteArrayOutputStream();
                    concatBytes.write(",".getBytes());
                    concatBytes.flush();
                    concatBytes.write(datareaded);
                    concatBytes.flush();
                    frameWriter.write(concatBytes.toByteArray());
                    frameWriter.close();
                }
                else
                {
                    BufferedOutputStream lastFrameWriter = new BufferedOutputStream(new FileOutputStream(Constants.FRAMES_PATH + "FramesChunk_" + currentChunk + ".json",true),Constants.BUFFER_SIZE);
                    BufferedOutputStream frameWriter = new BufferedOutputStream(new FileOutputStream(Constants.FRAMES_PATH + "FramesChunk_" + formatedChunkId + ".json"),Constants.BUFFER_SIZE);
                    lastFrameWriter.write("]".getBytes());
                    lastFrameWriter.close();
                    ByteArrayOutputStream concatBytes = new ByteArrayOutputStream();
                    concatBytes.write("[".getBytes());
                    concatBytes.flush();
                    concatBytes.write(datareaded);
                    concatBytes.flush();
                    frameWriter.write(concatBytes.toByteArray());
                    frameWriter.close();
                }
                currentChunk = formatedChunkId;
                
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
        catch(IllegalArgumentException e)
        {
            e.printStackTrace();
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
            else if(readedData.contains(SET_DATA))
            {
                String newData = readedData.split(SEPARATOR)[1];
                messageDTO.setMessage(newData);
                dataOut.write("Mensaje recibido y almacenado\n".getBytes());
            }
            else if(readedData.contains(GET_DATA))
            {
                dataOut.write(messageDTO.getMessage().getBytes());
            }
            else
            {
                int longitud = readedData.length();
                dataOut.write(("R.- " + longitud).getBytes());
                dataOut.flush();
            }
        }
    }


    
}
