package pl.wojna.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

public class GameClientGUI extends Application
{
    private Label statusLabel;
    private Label deckCountLabel;
    //private ImageView playerCardView;
    //private ImageView opponentCardView;
    private Button playButton;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private int deckCount = 5;
    private int cardLayerCounter = 0;

    private boolean czyTrwaWojna = false;

    private StackPane playerCardPane;
    private StackPane opponentCardPane;

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        showConnectionScreen(primaryStage);


    }

    private void connectToServer(String ip, int port)
    {
        new Thread(() ->
        {
            try
            {
                socket = new Socket(ip, port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("READY");

                String line;
                while ((line = in.readLine()) != null)
                {
                    String finalLine = line;
                    Platform.runLater(() -> handleServerMessage(finalLine));
                }
            }
            catch (IOException e)
            {
                Platform.runLater(() -> statusLabel.setText("Blad polaczenia z serwerem."));
            }
        }).start();
    }


    private void handleServerMessage(String message)
    {
        System.out.println("[ODBIERAM]: " + message);

        if (message.startsWith("START")) {
            statusLabel.setText("Gra rozpoczeta!");
            playButton.setDisable(false);
        }
        else if (message.startsWith("CARD:"))
        {
            if (!czyTrwaWojna) {
                clearCardPanes();
                cardLayerCounter = 0;
            }
            String card = message.substring(5).trim();
            Image img = loadCardImage(card);
            addCardToPane(playerCardPane, img, -cardLayerCounter * 25, 0, 0); // przesuwaj w prawo
        }

        else if (message.startsWith("OPPONENT_CARD:"))
        {
            String card = message.substring(14).trim();
            Image img = loadCardImage(card);
            addCardToPane(opponentCardPane, img, cardLayerCounter * 25, 0, 0);
            cardLayerCounter++;
        }
        else if (message.startsWith("WAR_STEP:REVERSE"))
        {

            int horizontalOffset = cardLayerCounter * 25;
            int verticalAdjust = 40;

            // dodajemy kartę "zakrytą" (tył) z przesunięciem
            Image back = loadCardImage("BACK");
            addCardToPane(playerCardPane, back, -horizontalOffset + 10, verticalAdjust, 90);
            addCardToPane(opponentCardPane, back, horizontalOffset - 10, verticalAdjust, 90);
            cardLayerCounter++;
            czyTrwaWojna = true;
        }
        else if (message.startsWith("RESULT:"))
        {
            String result = message.substring(7, 8);
            if ("1".equals(result))
            {
                statusLabel.setText("WYGRAŁEŚ rundę!");
                czyTrwaWojna = false;
            }
            else if ("2".equals(result))
            {
                statusLabel.setText("PRZEGRAŁEŚ rundę.");
                czyTrwaWojna = false;
            }
            else
            {
                statusLabel.setText("REMIS!");
                czyTrwaWojna = true;
            }
        }
        else if (message.startsWith("GAME_OVER"))
        {
            statusLabel.setText("Gra zakończona: " + message);
            playButton.setDisable(true);
        }
        else if (message.startsWith("DECK_COUNT:"))
        {
            try
            {
                deckCount = Integer.parseInt(message.substring(11).trim());
                deckCountLabel.setText("Twoje karty: " + deckCount);
            }
            catch (NumberFormatException e)
            {
                System.out.println("Błędna wartość DECK_COUNT: " + message);
            }
        }

        else if (message.startsWith("END"))
        {}
    }

    private Image loadCardImage(String cardCode)
    {
        try
        {
            System.out.println("Odbieram karte: " + cardCode);

            String value = cardCode.substring(0, cardCode.length() - 1);
            String suit = cardCode.substring(cardCode.length() - 1);

            if (cardCode.equals("BACK"))
            {
                return new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cards/back001.gif")));
            }

            String filename = switch (suit)
            {
                case "H" -> value + "h";
                case "S" -> value + "s";
                case "D" -> value + "d";
                case "C" -> value + "c";
                default -> "back";
            };

            if ("JQKA".contains(value))
            {
                value = switch (value)
                {
                    case "J" -> "11";
                    case "Q" -> "12";
                    case "K" -> "13";
                    case "A" -> "01";
                    default -> value;
                };
            }

            else if (value.length() == 1)
            {
                value = "0" + value;
            }

            filename = value + filename.charAt(filename.length() - 1) + ".gif";

            System.out.println(" laduje plik: /cards/" + filename);

            var stream = getClass().getResourceAsStream("/cards/" + filename);
            if (stream == null)
            {
                System.out.println(" Brak pliku: " + filename);
                return null;
            }

            return new Image(stream);

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private void showConnectionScreen(Stage primaryStage)
    {
        TextField ipField = new TextField("localhost");
        TextField portField = new TextField("5000");
        Button connectBtn = new Button("Połącz");

        Label info = new Label("Podaj IP i port serwera:");

        VBox vbox = new VBox(10, info, ipField, portField, connectBtn);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Scene connectScene = new Scene(vbox, 300, 200);
        primaryStage.setScene(connectScene);
        primaryStage.show();

        connectBtn.setOnAction(e ->
        {
            String ip = ipField.getText();
            int port = Integer.parseInt(portField.getText());
            showGameUI(primaryStage);   // uruchamiamy interfejs gry
            connectToServer(ip, port);  // przekazujemy IP i port

        });
    }


    private void showGameUI(Stage primaryStage)
    {

        deckCountLabel = new Label("Twoje karty: " + 26);
        statusLabel = new Label("Czekam na rozpoczęcie gry...");


        playerCardPane = new StackPane();
        opponentCardPane = new StackPane();
        playerCardPane.setPrefSize(100, 150);
        opponentCardPane.setPrefSize(100, 150);

        playButton = new Button("ZAGRAJ KARTĘ");
        playButton.setDisable(true);
        playButton.setOnAction(e -> {
            out.println("PLAY");
        });

        VBox topBox = new VBox(10, deckCountLabel, statusLabel);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(10));

        HBox cardBox = new HBox(40, playerCardPane, opponentCardPane);
        cardBox.setAlignment(Pos.CENTER);
        cardBox.setPrefHeight(200);
        VBox bottomBox = new VBox(playButton);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setCenter(cardBox);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Gra w Wojnę – Klient");
        primaryStage.show();

    }


    private void addCardToPane(StackPane pane, Image image, double offsetX, double offsetY, double rotation)
    {
        if (pane == null)
        {
            System.out.println(" Pane is null — nie dodano karty.");
            return;
        }
        if (image == null)
        {
            System.out.println(" Obrazek null — nie dodano.");
            return;
        }

        ImageView view = new ImageView(image);
        view.setFitWidth(100);
        view.setFitHeight(150);
        view.setPreserveRatio(false);
        view.setTranslateX(offsetX);
        view.setTranslateY(offsetY);
        view.setRotate(rotation);

        pane.getChildren().add(view);
        System.out.println(" Dodano karte, stack: " + pane.getChildren().size());
    }




    private void clearCardPanes()
    {
        playerCardPane.getChildren().clear();
        opponentCardPane.getChildren().clear();
    }
}