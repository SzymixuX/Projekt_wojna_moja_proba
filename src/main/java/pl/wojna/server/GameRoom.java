package pl.wojna.server;

import pl.wojna.database.BazaDanych;
import pl.wojna.model.Card;
import pl.wojna.model.Deck;
import pl.wojna.model.Player;

import java.util.ArrayList;
import java.util.List;

public class GameRoom implements Runnable {
    private final ClientHandler player1Handler;
    private final ClientHandler player2Handler;

    private boolean player1Ready = false;
    private boolean player2Ready = false;


    // DODANO: gracze (logika talii)
    private final Player player1 = new Player();
    private final Player player2 = new Player();

    public final int iloscKart;

    public GameRoom(ClientHandler player1Handler, ClientHandler player2Handler, int iloscKart) {
        this.player1Handler = player1Handler;
        this.player2Handler = player2Handler;
        this.iloscKart = iloscKart;

        // 🟨 DODANO: powiązanie handlera z pokojem
        player1Handler.setGameRoom(this);
        player2Handler.setGameRoom(this);
    }

    @Override
    public void run() {
        startGame();

        player1Handler.sendMessage("START");
        player2Handler.sendMessage("START");
    }

    // DODANO: główna logika gry (jedna runda)
    private void startGame()
    {
        dealCards();
        player1Handler.sendMessage("START");
        player2Handler.sendMessage("START");
    }
    public void playRound()
    {

        if (!player1.hasCards() || !player2.hasCards()) {
            broadcast("GAME_OVER DRAW");
            return;
        }

        Card card1 = player1.getDeck().removeFirst();
        Card card2 = player2.getDeck().removeFirst();
        System.out.println(card1 + " vs " + card2 + " = " + card1.getStrength() + " vs " + card2.getStrength());

        // Wyślij każdemu jego kartę i kartę przeciwnika
        player1Handler.sendMessage("CARD:" + card1);
        player1Handler.sendMessage("OPPONENT_CARD:" + card2);

        player2Handler.sendMessage("CARD:" + card2);
        player2Handler.sendMessage("OPPONENT_CARD:" + card1);

        int strength1 = card1.getStrength();
        int strength2 = card2.getStrength();

        if (strength1 > strength2) {
            player1.getDeck().add(card1);
            player1.getDeck().add(card2);
            player1Handler.sendMessage("RESULT:1\nYOU_WIN");
            player2Handler.sendMessage("RESULT:2\nYOU_LOSE");
        } else if (strength2 > strength1) {
            player2.getDeck().add(card1);
            player2.getDeck().add(card2);
            player1Handler.sendMessage("RESULT:2\nYOU_LOSE");
            player2Handler.sendMessage("RESULT:1\nYOU_WIN");
        } else {
            player1.getDeck().addLast(card1);
            player2.getDeck().addLast(card2);
            broadcast("RESULT:0\nDRAW");
        }
        player1Handler.sendMessage("DECK_COUNT:" + player1.getDeck().size());
        player2Handler.sendMessage("DECK_COUNT:" + player2.getDeck().size());

        broadcast("END");

        if (!player1.hasCards())
        {
            player1Handler.sendMessage("YOU_LOSE_GAME");
            player2Handler.sendMessage("YOU_WIN_GAME");
        }
        else if (!player2.hasCards())
        {
            player1Handler.sendMessage("YOU_WIN_GAME");
            player2Handler.sendMessage("YOU_LOSE_GAME");
        }
    }


    //  DODANO: rozdawanie talii
    private void dealCards() {
        List<Card> fullDeck = Deck.createFullDeck(); // potasowany zestaw 52 kart

    /*
        for (int i = 0; i < iloscKart/2; i++) {
            player1.getDeck().add(fullDeck.remove(0));
            player2.getDeck().add(fullDeck.remove(0));
        }

        */

        BazaDanych.initialize();
        for (int i = 0; i < iloscKart/2; i++) {
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


    //  DODANO: wysyłanie do obu graczy
    private void broadcast(String msg) {
        player1Handler.sendMessage(msg);
        player2Handler.sendMessage(msg);
    }

    public synchronized void registerPlay(ClientHandler handler) {
        if (handler == player1Handler) {
            player1Ready = true;
        } else if (handler == player2Handler) {
            player2Ready = true;
        }

        if (player1Ready && player2Ready) {
            player1Ready = false;
            player2Ready = false;
            playRound();
        }
    }
}
