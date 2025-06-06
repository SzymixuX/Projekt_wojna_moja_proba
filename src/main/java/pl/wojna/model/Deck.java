package pl.wojna.model;

import java.util.*;

public class Deck
{
    public static final List<String> ORDER = Arrays.asList("2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A");

    public static List<Card> createFullDeck()
    {
        String[] suits = {"H", "S", "D", "C"};
        String[] values = ORDER.toArray(new String[0]);

        List<Card> deck = new ArrayList<>();
        for (String suit : suits)
        {
            for (String value : values)
            {
                deck.add(new Card(value, suit));
            }
        }

        Collections.shuffle(deck);
        return deck;
    }

    public static void deal(List<Card> deck, List<Card> deck1, List<Card> deck2)
    {
        Iterator<Card> iterator = deck.iterator();
        while (iterator.hasNext())
        {
            deck1.add(iterator.next());
            iterator.remove();
            if (iterator.hasNext())
            {
                deck2.add(iterator.next());
                iterator.remove();
            }
        }
    }
}
