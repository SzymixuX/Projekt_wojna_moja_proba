package pl.wojna.model;

import java.util.List;

public class Game
{
    private final Player player1;
    private final Player player2;

    public Game(Player p1, Player p2)
    {
        this.player1 = p1;
        this.player2 = p2;
    }

    public int play()
    {
        int round = 0;
        while (player1.hasCards() && player2.hasCards() && round < 100)
        {
            round++;
            System.out.println("Runda " + round + ": P1: " + player1.totalCards() + " P2: " + player2.totalCards());
            fight();
            //player1.reloadIfNeeded();
            //player2.reloadIfNeeded();
        }

        if (player1.totalCards() > player2.totalCards()) return 1;
        if (player2.totalCards() > player1.totalCards()) return 2;
        return 3; // remis
    }

    private void fight()
    {
        if (player1.getDeck().isEmpty() || player2.getDeck().isEmpty()) return;

        Card c1 = player1.getDeck().removeLast();
        Card c2 = player2.getDeck().removeLast();

        int v1 = c1.getStrength();
        int v2 = c2.getStrength();

        if (v1 > v2)
        {
            player1.getDeck().add(c1);
            player1.getDeck().add(c2);
        }
        else if (v2 > v1)
        {
            player2.getDeck().add(c1);
            player2.getDeck().add(c2);
        }
        else
        {
            war(c1, c2, 0);
        }
    }

    private void war(Card base1, Card base2, int depth)
    {
        System.out.println("WOJNA!");
        List<Card> pile1 = new java.util.ArrayList<>();
        List<Card> pile2 = new java.util.ArrayList<>();

        pile1.add(base1);
        pile2.add(base2);

        int needed = 4 + depth;
        if (player1.getDeck().size() < needed || player2.getDeck().size() < needed)
        {
            return;
        }

        for (int i = 0; i < needed; i++)
        {
            pile1.add(player1.getDeck().removeLast());
            pile2.add(player2.getDeck().removeLast());
        }

        Card war1 = pile1.get(pile1.size() - 1);
        Card war2 = pile2.get(pile2.size() - 1);

        int v1 = war1.getStrength();
        int v2 = war2.getStrength();

        if (v1 > v2)
        {
            player1.getDeck().addAll(pile1);
            player1.getDeck().addAll(pile2);
        }
        else if (v2 > v1)
        {
            player2.getDeck().addAll(pile1);
            player2.getDeck().addAll(pile2);
        }
        else
        {
            war(war1, war2, depth + 4);
        }
    }
}
