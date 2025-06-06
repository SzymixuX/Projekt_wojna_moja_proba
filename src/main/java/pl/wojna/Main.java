package pl.wojna;

import pl.wojna.model.*;

import java.util.*;

public class Main
{
    public static void main(String[] args)
    {
        List<Card> fullDeck = Deck.createFullDeck();
        List<Card> deck1 = new ArrayList<>();
        List<Card> deck2 = new ArrayList<>();

        Deck.deal(fullDeck, deck1, deck2);

        Player p1 = new Player();
        Player p2 = new Player();
        p1.getDeck().addAll(deck1);
        p2.getDeck().addAll(deck2);

        Game game = new Game(p1, p2);
        int result = game.play();
        System.out.println("Wynik: " + (result == 1 ? "Gracz 1" : result == 2 ? "Gracz 2" : "Remis"));
    }
}
