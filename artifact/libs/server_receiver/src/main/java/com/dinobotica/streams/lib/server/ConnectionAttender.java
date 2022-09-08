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

    private static final String END_MESSAJE = "_END_OF_MSG_";
    private static final String SET_DATA = "_SET_";
    private static final String GET_DATA = "_GET_";
    private static final String SEPARATOR = "_M_";

    private Map<String,Float> chunksCounter = new HashMap<String,Float>();

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
            String chunkId;
            boolean chunkToRead = false;
            File framesDir = new File(Constants.FRAMES_PATH);
            if (!framesDir.exists() && !framesDir.mkdirs()) {
                logger.info("Error al crear directorio de frames");
            }
            if(!endConnection)
                chunkToRead = true;
            
            if(readSize > 0 && chunkToRead)
            {
                stringDataReaded = stringDataReaded.replaceAll("}\\{", "\\},\\{");
                datareaded = stringDataReaded.getBytes();
                //Verificamos que haya solo un cuadro, en caso contrario, se separan
                String[] frames;
                if(stringDataReaded.contains("},{"))
                {   
                    frames = stringDataReaded.split("\\},\\{");
                    System.out.println("Existen " + frames.length + " },{");
                    for(int i = 0; i < frames.length; i++)
                    {
                        String frame = frames[i];
                        if(i==0)
                            frame = frame + "}";
                        else if(i < (frames.length-1))
                            frame = "{" + frame + "}";
                        else
                            frame = "{" + frame;
                        writeReadedChunk(frame.getBytes(),frame);
                    }
                }
                else
                {
                    writeReadedChunk(datareaded,stringDataReaded);
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

    private void writeReadedChunk(byte[] datareaded,String stringDataReaded) throws IOException
    {
        String chunkId;
        boolean completeFrame = (stringDataReaded.contains("{") && stringDataReaded.contains("}"));
        if(stringDataReaded.contains("chunkId"))
        {
            chunkId = stringDataReaded.split(":")[1].replace(",\"time\"", "").replace(" ", "");
            if(chunksCounter.containsKey(chunkId))
                chunksCounter.replace(chunkId, chunksCounter.get(chunkId) + (completeFrame ? 1.0f : 0.5f));
            else
                chunksCounter.put(chunkId, 1.0f);

        }
        else
        {
            int chunkIdInt = Constants.CHUNK_RATE;
            chunkId = "" + chunkIdInt;
            for(Map.Entry<String,Float> tupla : chunksCounter.entrySet())
            {
                if(tupla.getValue() < 1.0f && Integer.parseInt(tupla.getKey()) < chunkIdInt)
                {
                    chunkId = tupla.getKey();
                    chunkIdInt = Integer.parseInt(chunkId);
                }
            }
        }

        String formatedChunkId = String.format("%04d", Integer.parseInt(chunkId));
        System.out.println(chunkId + " " + completeFrame + " " + stringDataReaded.length());
        if(chunksCounter.get(chunkId) == 1.0f)
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
        else if(chunksCounter.get(chunkId) <= Constants.FRAME_RATE)
        {
            BufferedOutputStream frameWriter = new BufferedOutputStream(new FileOutputStream(Constants.FRAMES_PATH + "FramesChunk_" + formatedChunkId + ".json",true),Constants.BUFFER_SIZE);
            ByteArrayOutputStream concatBytes = new ByteArrayOutputStream();
            concatBytes.write(",".getBytes());
            concatBytes.flush();
            concatBytes.write(datareaded);
            concatBytes.flush();
            if(!(chunksCounter.get(chunkId) < Constants.FRAME_RATE))
            {
                concatBytes.write("]".getBytes());
                concatBytes.flush();
            }
            frameWriter.write(concatBytes.toByteArray());
            frameWriter.close();
        }
    }


    
}
