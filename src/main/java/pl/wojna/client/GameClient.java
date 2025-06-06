package pl.wojna.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class GameClient
{
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args)
    {

        try
                (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                Scanner scanner = new Scanner(System.in)
        )
        {
            System.out.println("Połączono z serwerem!");

            // Wątek do odbierania wiadomości z serwera
            Thread readerThread = new Thread(() ->
            {
                try
                {
                    String line;
                    while ((line = in.readLine()) != null)
                    {
                        System.out.println("Serwer: " + line);
                    }
                }
                catch (IOException e)
                {
                    System.out.println("Rozłączono z serwerem.");
                }
            });
            readerThread.start();

            // Główna pętla klienta: czytaj z konsoli i wysyłaj do serwera
            while (true)
            {
                String userInput = scanner.nextLine();
                out.println(userInput);

                if (userInput.equalsIgnoreCase("exit"))
                {
                    System.out.println("Zamykam połączenie...");
                    break;
                }
            }
            socket.close();
        }
        catch (IOException e)
        {
            System.err.println("Błąd połączenia z serwerem.");
            e.printStackTrace();
        }
    }
}
