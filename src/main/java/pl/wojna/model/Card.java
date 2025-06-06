package pl.wojna.model;

import java.util.List;

public class Card {
    private final String value;
    private final String suit;

    private static final List<String> ORDER = Deck.ORDER;

    public Card(String value, String suit) {
        this.value = value;
        this.suit = suit;
    }

    public String getValue() {
        return value;
    }

    public String getSuit() {
        return suit;
    }

    public int getStrength() {
        return ORDER.indexOf(value); // 🟨 Kluczowy element – porównujemy po wartości
    }

    @Override
    public String toString() {
        return value + suit;
    }
}
