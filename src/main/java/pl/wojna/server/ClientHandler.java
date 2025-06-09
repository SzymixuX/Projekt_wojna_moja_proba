package pl.wojna.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable
{
    private final Socket socket;
    private final int playerId;

    private BufferedReader in;
    private PrintWriter out;
    private GameRoom gameRoom;

    public ClientHandler(Socket socket, int playerId)
    {
        this.socket = socket;
        this.playerId = playerId;
    }

    @Override
    public void run()
    {
        System.out.println(" Obsluguję gracza #" + playerId);

        try
        {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            sendMessage("Witaj Graczu #" + playerId + "! Wpisz READY, gdy będziesz gotowy.");

            String message;
            while ((message = in.readLine()) != null)
            {
                System.out.println(" Gracz #" + playerId + ": " + message);


                if (message.equalsIgnoreCase("READY"))
                {
                    sendMessage("OK – czekam na drugiego gracza...");
                    GameServer.registerReadyClient(this);
                }


                else if (message.equalsIgnoreCase("PLAY")) {
                    if (gameRoom != null) {
                        gameRoom.registerPlay(this);
                    }
                }
                else if (message.equalsIgnoreCase("EXIT")) {
                    sendMessage("Rozlaczam...");
                    break;
                }


                else
                {
                    sendMessage("Echo: " + message);
                }
            }
        }
        catch (IOException e)
        {
            System.err.println(" Blad po stronie gracza #" + playerId);
            e.printStackTrace();
        }
        finally
        {
            try
            {
                socket.close();
                System.out.println(" Gracz #" + playerId + " rozlaczony.");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message)
    {
        out.println(message);
    }

    public void setGameRoom(GameRoom gameRoom)
    {
        this.gameRoom = gameRoom;
    }

    public int getPlayerId()
    {
        return playerId;
    }
}
