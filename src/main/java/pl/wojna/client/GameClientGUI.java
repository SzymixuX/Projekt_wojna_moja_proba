package pl.wojna.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

public class GameClientGUI extends Application {
    private Label statusLabel;
    private Label deckCountLabel;
    private ImageView playerCardView;
    private ImageView opponentCardView;
    private Button playButton;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private int deckCount = 5;

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        connectToServer();

        deckCountLabel = new Label("Twoje karty: " + deckCount);
        statusLabel = new Label("Czekam na rozpoczęcie gry...");

        playerCardView = new ImageView();
        opponentCardView = new ImageView();
        playerCardView.setFitWidth(100);
        playerCardView.setFitHeight(150);
        opponentCardView.setFitWidth(100);
        opponentCardView.setFitHeight(150);

        playButton = new Button("ZAGRAJ KARTĘ");
        playButton.setDisable(true);
        playButton.setOnAction(e -> out.println("PLAY"));

        VBox topBox = new VBox(10, deckCountLabel, statusLabel);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(10));

        HBox cardBox = new HBox(40, playerCardView, opponentCardView);
        cardBox.setAlignment(Pos.CENTER);

        VBox bottomBox = new VBox(playButton);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setCenter(cardBox);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Gra w Wojnę - Klient");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket("localhost", 5000);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("READY");

                String line;
                while ((line = in.readLine()) != null) {
                    String finalLine = line;
                    Platform.runLater(() -> handleServerMessage(finalLine));
                }
            } catch (IOException e) {
                Platform.runLater(() -> statusLabel.setText("Błąd połączenia z serwerem."));
                e.printStackTrace();
            }
        }).start();
    }

    private void handleServerMessage(String message) {
        if (message.startsWith("START")) {
            statusLabel.setText("Gra rozpoczęta!");
            playButton.setDisable(false);
        } else if (message.startsWith("CARD:")) {
            String card = message.substring(5).trim();
            playerCardView.setImage(loadCardImage(card));
        } else if (message.startsWith("OPPONENT_CARD:")) {
            String card = message.substring(14).trim();
            opponentCardView.setImage(loadCardImage(card));
        } else if (message.startsWith("RESULT:")) {
            String result = message.substring(7).trim();
            if ("1".equals(result)) {
                statusLabel.setText("WYGRAŁEŚ rundę!");
            } else if ("2".equals(result)) {
                statusLabel.setText("PRZEGRAŁEŚ rundę.");
            } else {
                statusLabel.setText("REMIS!");
            }
        } else if (message.startsWith("GAME_OVER")) {
            statusLabel.setText("Gra zakończona: " + message);
            playButton.setDisable(true);
        }
        else if (message.startsWith("DECK_COUNT:")) {
            try {
                deckCount = Integer.parseInt(message.substring(11).trim());
                deckCountLabel.setText("Twoje karty: " + deckCount);
            } catch (NumberFormatException e) {
                System.out.println("Błędna wartość DECK_COUNT: " + message);
            }
        }
    }

    private Image loadCardImage(String cardCode) {
        try {
            System.out.println("Odbieram kartę: " + cardCode);

            String value = cardCode.substring(0, cardCode.length() - 1);
            String suit = cardCode.substring(cardCode.length() - 1);

            String filename = switch (suit) {
                case "H" -> value + "h";
                case "S" -> value + "s";
                case "D" -> value + "d";
                case "C" -> value + "c";
                default -> "back";
            };

            if ("JQKA".contains(value)) {
                value = switch (value) {
                    case "J" -> "11";
                    case "Q" -> "12";
                    case "K" -> "13";
                    case "A" -> "01";
                    default -> value;
                };
            } else if (value.length() == 1) {
                value = "0" + value;
            }

            filename = value + filename.charAt(filename.length() - 1) + ".gif";

            System.out.println(" Ładuję plik: /cards/" + filename);

            var stream = getClass().getResourceAsStream("/cards/" + filename);
            if (stream == null) {
                System.out.println(" Brak pliku: " + filename);
                return null;
            }

            return new Image(stream);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
