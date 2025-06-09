package pl.wojna.server;

import java.io.IOException;
import java.io.ObjectInputFilter;
import java.net.ServerSocket;
import java.net.Socket;

import pl.wojna.config.ConfigLoader;
import pl.wojna.model.Card;
import pl.wojna.model.Deck;
import pl.wojna.model.Player;

import java.util.*;


public class GameServer
{

    //public static final int PORT = 5000;

    private static final ClientHandler[] clients = new ClientHandler[2];


    private static volatile int readyCount = 0;

    private static List<Card> player1Deck = new ArrayList<>();
    private static List<Card> player2Deck = new ArrayList<>();

    private static final Queue<ClientHandler> waitingClients = new LinkedList<>();

    private static Player player1 = new Player();
    private static Player player2 = new Player();


    public static void main(String[] args)
    {
        ConfigLoader.load();
        int PORT = ConfigLoader.getServerPort();
        System.out.println(" Serwer gry uruchomiony na porcie " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT))
        {
            int id = 1;
            while(true)
            {
                Socket clientSocket = serverSocket.accept();
                System.out.println(" Połączono gracza #" + (id));

                ClientHandler handler = new ClientHandler(clientSocket, id++);
                new Thread(handler).start();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public static synchronized void playerReady()
    {
        readyCount++;
        if (readyCount == 2)
        {
            System.out.println(" Obaj gracze gotowi – zaczynamy grę");
            startGame();
        }
    }



    private static void startGame()
    {
        for (ClientHandler ch : clients)
        {
            ch.sendMessage("START");
        }

        dealCards();


        if (player1.getDeck().isEmpty() || player2.getDeck().isEmpty())
        {
            clients[0].sendMessage("Koniec gry");
            clients[1].sendMessage("Koniec gry");
            return;
        }

         //wyciągamy ostatnią kartę
        Card card1 = player1.getDeck().removeLast();
        Card card2 = player2.getDeck().removeLast();

        clients[0].sendMessage("CARD:" + card1.toString());
        clients[1].sendMessage("CARD:" + card2.toString());

        int strength1 = card1.getStrength();
        int strength2 = card2.getStrength();

        if (strength1 > strength2)
        {
            player1.getDeck().add(card1);
            player1.getDeck().add(card2);
            clients[0].sendMessage("RESULT:1\nYOU_WIN");
            clients[1].sendMessage("RESULT:2\nYOU_LOSE");
        }
        else if (strength2 > strength1)
        {
            player2.getDeck().add(card1);
            player2.getDeck().add(card2);
            clients[0].sendMessage("RESULT:2\nYOU_LOSE");
            clients[1].sendMessage("RESULT:1\nYOU_WIN");
        }
        else
        {

            player1.getDeck().addFirst(card1);
            player2.getDeck().addFirst(card2);
            clients[0].sendMessage("RESULT:0\nDRAW");
            clients[1].sendMessage("RESULT:0\nDRAW");
        }

        for (ClientHandler ch : clients)
        {
            ch.sendMessage("END");
        }
    }

    private static int convertToStrength(String card)
    {
        String value = card.length() == 3 ? card.substring(0, 2) : card.substring(0, 1);
        String[] order = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        for (int i = 0; i < order.length; i++)
        {
            if (order[i].equals(value)) return i;
        }
        return -1;
    }

    private static void dealCards()
    {
        List<Card> fullDeck = Deck.createFullDeck();
        List<Card> deck1 = new ArrayList<>();
        List<Card> deck2 = new ArrayList<>();
        Deck.deal(fullDeck, deck1, deck2);
        player1.getDeck().addAll(deck1);
        player2.getDeck().addAll(deck2);
    }

    public static synchronized void registerReadyClient(ClientHandler handler)
    {
        waitingClients.add(handler);
        System.out.println(" Gracz #" + handler.getPlayerId() + " gotowy. Liczba oczekujących: " + waitingClients.size());

        if (waitingClients.size() >= 2)
        {
            ClientHandler player1 = waitingClients.poll();
            ClientHandler player2 = waitingClients.poll();

            System.out.println(" Tworzenie nowego pokoju: " + player1.getPlayerId() + " vs " + player2.getPlayerId());

            GameRoom room = new GameRoom(player1, player2, 52);
            new Thread(room).start();
        }
    }
}
