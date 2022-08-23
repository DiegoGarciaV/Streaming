package com.dinobotica.streams;

import java.io.IOException;

public class Comunications {

    private ClientService clientService;
    private final String END_MESSAJE = "_END_OF_MSG_";

    public ClientService getClientService() {
        return clientService;
    }

    public void startClient()
    {
        String server = System.console().readLine("Servidor: ");
        int port = Integer.parseInt(System.console().readLine("Puerto: "));

        try
        {
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
        while(connStatus)
        {
            String cmd = System.console().readLine("Escriba un mensaje: ");
            if(cmd.equals("END"))
                connStatus = false;
            else
            {
                String hostResponse = comunications.getClientService().sendData(cmd);
                System.out.println(hostResponse);
            }
        }
        String hostResponse = comunications.getClientService().sendData(comunications.END_MESSAJE);
        if(hostResponse.equals("Fin de la conexion\n"))
            System.out.println("Se ha finalizado correctamente la comunicación");
        comunications.getClientService().closeConnection();
    }

    
    
}
