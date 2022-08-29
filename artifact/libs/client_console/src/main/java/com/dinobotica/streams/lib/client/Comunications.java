package com.dinobotica.streams.lib.client;

import java.io.IOException;
import java.util.logging.Logger;


public class Comunications {

    private ClientService clientService;
    private final String END_MESSAJE = "_END_OF_MSG_";

    private final Logger logger = Logger.getLogger(Comunications.class.getName());


    public ClientService getClientService() {
        return clientService;
    }

    public void startClient()
    {
        String server = System.console().readLine("Servidor: ");
        int port = Integer.parseInt(System.console().readLine("Puerto: "));

        try
        {
            server = (server.equals("") ? "localhost" : server) ;   
            port = (port == 0 ? 6666 : port);
            clientService = new ClientService(server, port);
        }
        catch(IOException IoE)
        {
            IoE.printStackTrace();
        }
        
    }

    public static void main(String[] args) {
        
        Comunications comunications = new Comunications();
        comunications.startClient();
        boolean connStatus = true;
        String hostResponse = "";
        while(connStatus && comunications.getClientService().isConnected())
        {
            String cmd = System.console().readLine("Escriba un mensaje: ");
            if(cmd.equals("END"))
            {
                hostResponse = comunications.getClientService().sendData(comunications.END_MESSAJE.getBytes());
                connStatus = false;
            }  
            else
            {
                hostResponse = comunications.getClientService().sendData(cmd.getBytes());
                System.out.println(hostResponse);
            }
        }
        
        if(hostResponse.equals("Fin de la conexion\n"))
            System.out.println("Se ha finalizado correctamente la comunicación");
        comunications.getClientService().closeConnection();
    }

    
    
}
