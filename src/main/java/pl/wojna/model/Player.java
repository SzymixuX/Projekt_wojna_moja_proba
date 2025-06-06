package pl.wojna.model;

import java.util.*;

public class Player
{
    private final LinkedList<Card> deck = new LinkedList<>();
    //private final List<Card> holder = new ArrayList<>();

    public LinkedList<Card> getDeck()
    {
        return deck;
    }

    /*
    public List<Card> getHolder()
    {
        return holder;
    }
     */

    /*
    public void reloadIfNeeded()
    {
        if (deck.isEmpty() && !holder.isEmpty())
        {
            deck.addAll(holder);
            holder.clear();
        }
    }
*/

    public boolean hasCards()
    {
        //return !deck.isEmpty() || !holder.isEmpty();
        return !deck.isEmpty();
    }

    public int totalCards()
    {
        //return deck.size() + holder.size();
        return deck.size();
    }
}
