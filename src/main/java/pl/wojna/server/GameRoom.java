package pl.wojna.server;

import pl.wojna.database.BazaDanych;
import pl.wojna.model.Card;
import pl.wojna.model.Deck;
import pl.wojna.model.Player;

import java.util.ArrayList;
import java.util.List;

public class GameRoom implements Runnable
{
    private final ClientHandler player1Handler;
    private final ClientHandler player2Handler;

    private boolean player1Ready = false;
    private boolean player2Ready = false;


    private final Player player1 = new Player();
    private final Player player2 = new Player();


    private boolean isWar = false;
    private final List<Card> battlefield = new ArrayList<>();
    private int warStep = 0; // 0 - normalna gra, 1 - rewersy, 2 - rozstrzygająca karta

    public final int iloscKart;

    public GameRoom(ClientHandler player1Handler, ClientHandler player2Handler, int iloscKart)
    {
        this.player1Handler = player1Handler;
        this.player2Handler = player2Handler;
        this.iloscKart = iloscKart;

        player1Handler.setGameRoom(this);
        player2Handler.setGameRoom(this);
    }

    @Override
    public void run()
    {
        startGame();

        player1Handler.sendMessage("START");
        player2Handler.sendMessage("START");
    }


    private void startGame()
    {
        //dealCards(); WERSJA DO NORMALNEJ GRY!!
        symulujTestowyDeck();
        player1Handler.sendMessage("START");
        player2Handler.sendMessage("START");
    }
    public void playRound()
    {
        if (!isWar)
        {
            //  Normalna runda
            Card card1 = player1.getDeck().pollFirst();
            Card card2 = player2.getDeck().pollFirst();

            if (card1 == null || card2 == null)
            {
                broadcast("GAME_OVER:Brak kart u któregoś z graczy.");
                return;
            }

            battlefield.add(card1);
            battlefield.add(card2);

            sendCards(card1, card2);

            int s1 = card1.getStrength();
            int s2 = card2.getStrength();

            if (s1 > s2)
            {
                giveBattlefieldTo(player1);
                sendResult(1);
            }
            else if (s2 > s1)
            {
                giveBattlefieldTo(player2);
                sendResult(2);
            }
            else
            {
                isWar = true;
                warStep = 1;
                broadcast("RESULT:0\nDRAW");
            }

            broadcast("END");

        }
        else
        {
            //  wojna albo 1 bok albo 2 rozstrzygajaca
            if (warStep == 1)
            {
                // zakryta
                Card reverse1 = player1.getDeck().pollFirst();
                Card reverse2 = player2.getDeck().pollFirst();

                if (reverse1 == null || reverse2 == null)
                {
                    broadcast("GAME_OVER:Koniec kart podczas wojny.");
                    return;
                }

                battlefield.add(reverse1);
                battlefield.add(reverse2);

                broadcast("WAR_STEP:REVERSE");
                warStep = 2;

            }
            else if (warStep == 2)
            {
                // Gracze dokładają kartę rozstrzygającą
                Card card1 = player1.getDeck().pollFirst();
                Card card2 = player2.getDeck().pollFirst();

                if (card1 == null || card2 == null)
                {
                    broadcast("GAME_OVER:Koniec kart podczas wojny.");
                    return;
                }

                battlefield.add(card1);
                battlefield.add(card2);

                sendCards(card1, card2);

                int s1 = card1.getStrength();
                int s2 = card2.getStrength();

                if (s1 > s2)
                {
                    giveBattlefieldTo(player1);
                    sendResult(1);
                    isWar = false;
                    warStep = 0;
                }
                else if (s2 > s1)
                {
                    giveBattlefieldTo(player2);
                    sendResult(2);
                    isWar = false;
                    warStep = 0;
                }
                else
                {
                    // remis -> kontynuujemy wojnę etapami
                    warStep = 1;
                    isWar = true;

                    battlefield.add(card1);
                    battlefield.add(card2);

                    broadcast("RESULT:0\nDRAW");
                }
                broadcast("END");
            }
        }

        System.out.println("Player 1 start deck: " + player1.getDeck());
        System.out.println("Player 2 start deck: " + player2.getDeck());
    }



    private void dealCards() {
        List<Card> fullDeck = Deck.createFullDeck(); // potasowany zestaw 52 kart

        BazaDanych.initialize();
        for (int i = 0; i < iloscKart/2; i++)
        {
            Card card1 = fullDeck.remove(0);
            Card card2 = fullDeck.remove(0);
            player1.getDeck().add(card1);
            player2.getDeck().add(card2);
            BazaDanych.saveDeck(1, card1.toString());
            BazaDanych.saveDeck(2, card2.toString());
        }



        // logowanie do testów
        System.out.println("Player 1 start deck: " + player1.getDeck());
        System.out.println("Player 2 start deck: " + player2.getDeck());
    }


    // wysyłanie do obu graczy
    private void broadcast(String msg)
    {
        player1Handler.sendMessage(msg);
        player2Handler.sendMessage(msg);
    }

    public synchronized void registerPlay(ClientHandler handler)
    {
        if (handler == player1Handler)
        {
            player1Ready = true;
        }
        else if (handler == player2Handler)

        {
            player2Ready = true;
        }

        if (player1Ready && player2Ready)
        {
            player1Ready = false;
            player2Ready = false;
            playRound();
        }
    }

    private void sendCards(Card c1, Card c2)
    {
        player1Handler.sendMessage("CARD:" + c1);
        player1Handler.sendMessage("OPPONENT_CARD:" + c2);

        player2Handler.sendMessage("CARD:" + c2);
        player2Handler.sendMessage("OPPONENT_CARD:" + c1);
    }

    private void sendResult(int winner)
    {
        if (winner == 1)
        {
            player1Handler.sendMessage("RESULT:1\nYOU_WIN");
            player2Handler.sendMessage("RESULT:2\nYOU_LOSE");
        }
        else
        {
            player1Handler.sendMessage("RESULT:2\nYOU_LOSE");
            player2Handler.sendMessage("RESULT:1\nYOU_WIN");
        }
    }

    private void giveBattlefieldTo(Player player)
    {
        player.getDeck().addAll(battlefield);
        battlefield.clear();
    }


    private void sleep(int ms)
    {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }




    private void symulujTestowyDeck()
    {
        player1.getDeck().clear();
        player2.getDeck().clear();

        player1.getDeck().add(new Card("3", "H"));  // 3H
        player1.getDeck().add(new Card("7", "C"));  // 7C
        player1.getDeck().add(new Card("A", "S"));  // AS
        player1.getDeck().add(new Card("6", "H"));  // 6H
        player1.getDeck().add(new Card("2", "C"));  // 2C
        player1.getDeck().add(new Card("9", "H"));  // 9H
        player1.getDeck().add(new Card("A", "C"));  // AC
        player1.getDeck().add(new Card("10", "C"));  // 10C


        player2.getDeck().add(new Card("5", "S"));  // 5S
        player2.getDeck().add(new Card("7", "D"));  // 7D
        player2.getDeck().add(new Card("Q", "H"));  // QH
        player2.getDeck().add(new Card("6", "S"));  // 6S
        player2.getDeck().add(new Card("8", "C"));  // 8C
        player2.getDeck().add(new Card("9", "C"));  // 9C
        player2.getDeck().add(new Card("4", "C"));  // 4C
        player2.getDeck().add(new Card("8", "C"));  // 8C

        System.out.println("Załadowano testowe decki do debugowania.");

        System.out.println("Player 1 start deck: " + player1.getDeck());
        System.out.println("Player 2 start deck: " + player2.getDeck());

        int iloscKart = 16;


        BazaDanych.initialize();
        for (int i = 0; i < iloscKart/2; i++)
        {
            Card card1 = player1.getDeck().remove(0);
            Card card2 = player2.getDeck().remove(0);
            player1.getDeck().add(card1);
            player2.getDeck().add(card2);
            BazaDanych.saveDeck(1, card1.toString());
            BazaDanych.saveDeck(2, card2.toString());
        }


    }

}
