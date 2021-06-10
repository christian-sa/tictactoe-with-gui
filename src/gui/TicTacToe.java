package gui;

import static game.Mark.*;
import game.Mark;
import game.AI;
import game.Field;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Menu;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

public class TicTacToe extends Application {

    public static Field field;
    private static GridPane gameBoard;
    private static BorderPane frame;
    private AnimationTimer timer;
    private static StatusDisplay status;

    private static final int WINDOW_WIDTH = 650;
    private static final int WINDOW_HEIGHT = 750;
    private final static int BOARD_SIZE = 3;
    private static boolean pvp = false;
    private static int difficulty = 3; // Default difficulty is 'Hard'.
    private static boolean aiTurn = true;
    private static Mark markAI = X;

    /**
     * Additional flag needed to make sure the method gameOver() which displays
     * the game over alert is only executed once. Otherwise AnimationTimers
     * handle() method was executing it multiple times.
     */
    private static boolean gameOverAlertShown = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        frame = new BorderPane();
        status = new StatusDisplay();
        Scene scene = new Scene(frame);

        frame.setTop(initialiseFullTopMenu());
        frame.setCenter(createBoard());
        frame.setBottom(status);

        scene.setFill(Paint.valueOf("#f5f5f5"));
        scene.getStylesheets().add(getClass().getResource("stylesheet.css").toExternalForm());
        primaryStage.setTitle("Tic-Tac-Toe");
        primaryStage.getIcons().add(new Image(this.getClass().getResource("/resources/img/user-icon.png").toExternalForm()));
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.setWidth(WINDOW_WIDTH);
        primaryStage.setHeight(WINDOW_HEIGHT);
        System.out.println("\n--STARTING NEW GAME: PvE--");
        field.print();

        runGameLoop();
        primaryStage.show();
    }

    public static final class Tile extends Button {

        private static final int TILE_SIZE = 200;
        private Mark mark;
        private final AudioClip turn = new AudioClip(getClass().getResource("/resources/sfx/turn.wav").toExternalForm());

        public Tile(int row, int col) {
            this.mark = field.getMark(row, col);
            this.setMinSize(TILE_SIZE, TILE_SIZE);
            this.getStyleClass().add("tile");
            this.setText("" + this.mark);
            frame.setCenter(gameBoard);

            this.setOnMouseClicked(e -> {
                this.mark = field.isXsTurn() ? X : O;
                if (field.available(row, col)) {
                    turn.play();
                    setTileAndField(row, col, this.mark, this);
                    setAITurn(true);
                }
            });
        }

        public void setTileAndField(int row, int col, Mark mark, Tile tile) {
            field.setMark(row, col, mark);
            tile.setText("" + mark);
            field.print();
        }
    }

    public static class StatusDisplay extends VBox {

        private static Label statusText = new Label();

        public StatusDisplay() {
            getStyleClass().add("statusBox");
            setAlignment(Pos.CENTER);
            setPrefSize(650, 40);
            getChildren().add(statusText);
            statusText.getStyleClass().add("status");
            if (pvp) { statusText.setText("X TURN"); }
        }

        /**
         * Updates the Status message.
         * Executed by the AnimationTimer in runGameLoop().
         */
        public static void updateStatus() {
            // Checking for game over in PvP or PvE mode to display
            // appropriate message.
            if (pvp && field.getGameState().equals("Draw")) {
                statusText.setText("DRAW");
                return;
            } else if (pvp && field.isGameOver()) {
                statusText.setText("PLAYER " + field.getGameState().toUpperCase());
                return;
            }
            if (!pvp & field.isGameOver()) {
                if (field.getGameState().equals("Draw")) {
                    statusText.setText("YOU MANAGED TO DRAW AGAINST THE AI.");
                    return;
                }
                Mark markPlayer = markAI == O ? X : O;
                boolean playerWonGame = field.getGameState().contains(markPlayer.toString() + " wins");
                statusText.setText(playerWonGame ? "CONGRATS! YOU DEFEATED THE AI." : "OH NO, THE AI DEFEATED YOU!");
                return;
            }

            // Displaying the current turn is only enabled in PvP mode because
            // the AI computes its turn and places mark almost instantaneously.
            // In PvE mode the status instead displays the difficulty of the AI
            // you are playing against.
            if (pvp) {
                statusText.setText(field.isXsTurn() ? "PLAYER X TURN" : "PLAYER O TURN");
            } else {
                String pveStatus = difficulty == 1 ? "PLAYING AGAINST AI DIFFICULTY EASY " :
                        difficulty == 3 ? "PLAYING AGAINST AI DIFFICULTY HARD " : "PLAYING AGAINST AI DIFFICULTY MEDIUM ";
                statusText.setText(pveStatus + "(" + markAI + ")");
            }
        }
    }

    public static GridPane createBoard() {
        field = new Field();
        gameBoard = new GridPane();
        gameBoard.setAlignment(Pos.CENTER);
        gameBoard.setPadding(new Insets(10, 0, 0, 0));

        for(int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Tile tile = new Tile(row, col);
                // Need to switch row & col so it matches field array.
                GridPane.setConstraints(tile, col, row);
                gameBoard.getChildren().add(tile);
            }
        }
        return gameBoard;
    }

    private void setGameMode(String mode) {
        pvp = mode.equals("pvp");
    }

    public static void setAITurn(boolean isAITurn) {
        aiTurn = isAITurn;
    }

    private void setDifficulty(int difficulty) {
        TicTacToe.difficulty = difficulty;
        System.out.print("\n--DIFFICULTY SET TO: ");
        System.out.println(difficulty == 1 ? "EASY--" : difficulty == 3 ? "HARD--" : "MEDIUM--");
        restartGame();
    }

    public static void playAIMove() {
        int[] move = AI.getMoveByDifficulty(field, markAI, difficulty);
        int row = move[0];
        int col = move[1];
        System.out.print("Making move level: ");
        System.out.println(difficulty == 1 ? "\"Easy\"" : difficulty == 3 ? "\"Hard\"" : "\"Medium.\"");

        for (Node child : gameBoard.getChildren()) {
            if (GridPane.getRowIndex(child) == row
                    && GridPane.getColumnIndex(child) == col) {
                Tile ai = (Tile) child;
                ai.setTileAndField(row, col, markAI, ai);
                frame.setCenter(gameBoard);
            }
        }
    }

    private void runGameLoop() {
        createBoard();

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                boolean gameInProgress = !field.isGameOver();
                StatusDisplay.updateStatus();
                if (!gameInProgress && !gameOverAlertShown) {
                    this.stop();
                    System.out.println(field.getGameState().toUpperCase() + "!\n");
                    gameOver();
                } else if (!pvp && aiTurn) {
                    playAIMove();
                    aiTurn = false;
                }
            }
        };
        timer.start();
    }

    public void restartGame() {
        frame.setCenter(createBoard());
        gameOverAlertShown = false;
        StatusDisplay.updateStatus();
        // Resets AI turn tracker depending on mark. X always goes first.
        setAITurn(markAI == X);
        field.print();
        runGameLoop();
    }

    /**
     * Handles the display of the alert which informs about the outcome of the game.
     * Flag gameOverShown is set to true right away to ensure method execution is limited
     * to once per game.
     */
    private void gameOver() {
        gameOverAlertShown = true;
        ButtonType newGame = new ButtonType("New Game");

        Alert gameOverAlert = new Alert(Alert.AlertType.NONE, "", newGame);
        gameOverAlert.setTitle("Game Over");
        gameOverAlert.setHeaderText(null);
        gameOverAlert.setContentText(field.getGameState() + "!");

        Stage alert = (Stage) gameOverAlert.getDialogPane().getScene().getWindow();
        alert.getIcons().add(new Image(this.getClass().getResource("/resources/img/user-icon.png").toExternalForm()));
        gameOverAlert.setGraphic((new ImageView(this.getClass().getResource("/resources/img/iconSmall.png").toExternalForm())));
        gameOverAlert.getDialogPane().getStylesheets().add(getClass().getResource("stylesheet.css").toExternalForm());
        gameOverAlert.getDialogPane().getStyleClass().add("gameOverAlertBox");

        gameOverAlert.setOnHidden(e -> {
            System.out.println("--NEW GAME: PREVIOUS SETTINGS--");
            restartGame();
        });

        gameOverAlert.show();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setPrefWidth(WINDOW_WIDTH);

        // Mode Menu
        Menu modeMenu = new Menu("Mode");
        RadioMenuItem pvpMode = new RadioMenuItem("PvP");
        Menu pveMode = new Menu("PvE");
        RadioMenuItem playerFirst = new RadioMenuItem("Player First");
        RadioMenuItem aiFirst = new RadioMenuItem("AI First");

        ToggleGroup toggleMode = new ToggleGroup();
        toggleMode.getToggles().add(pvpMode);
        toggleMode.getToggles().add(playerFirst);
        toggleMode.getToggles().add(aiFirst);

        modeMenu.getItems().add(pvpMode);
        modeMenu.getItems().add(pveMode);
        pveMode.getItems().add(playerFirst);
        pveMode.getItems().add(aiFirst);

        // Difficulty Menu
        Menu difficultyMenu = new Menu("Difficulty");
        RadioMenuItem easyDifficulty = new RadioMenuItem("Easy");
        RadioMenuItem mediumDifficulty = new RadioMenuItem("Medium");
        RadioMenuItem hardDifficulty = new RadioMenuItem("Hard");

        ToggleGroup toggleDifficulty = new ToggleGroup();
        toggleDifficulty.getToggles().add(easyDifficulty);
        toggleDifficulty.getToggles().add(mediumDifficulty);
        toggleDifficulty.getToggles().add(hardDifficulty);
        hardDifficulty.setSelected(true);

        difficultyMenu.getItems().add(easyDifficulty);
        difficultyMenu.getItems().add(mediumDifficulty);
        difficultyMenu.getItems().add(hardDifficulty);

        // Menu Bar
        menuBar.getMenus().add(modeMenu);
        menuBar.getMenus().add(difficultyMenu);

        // setOnAction methods
        pvpMode.setOnAction(e -> {
            setGameMode("pvp");
            difficultyMenu.setDisable(true);
            System.out.println("\n--NEW GAME: PvP--");
            restartGame();
        });
        pveMode.setOnAction(e -> {
            setGameMode("pve");
            difficultyMenu.setDisable(false);
            System.out.println("\n--NEW GAME: PvE--");
            restartGame();
        });
        aiFirst.setOnAction(e -> {
            markAI = X;
            aiTurn = true;
        });
        playerFirst.setOnAction(e -> {
            markAI = O;
            aiTurn = false;
        });
        difficultyMenu.setOnAction(e -> restartGame());
        easyDifficulty.setOnAction(e -> setDifficulty(1));
        mediumDifficulty.setOnAction(e -> setDifficulty(2));
        hardDifficulty.setOnAction(e -> setDifficulty(3));

        return menuBar;
    }

    public Pane initialiseFullTopMenu() {
        Pane topMenu = new Pane();
        Button restartButton = new Button("Restart");
        restartButton.getStyleClass().add("restartButton");
        restartButton.setLayoutX(190);
        restartButton.setLayoutY(5.5);

        restartButton.setOnAction(e -> {
            if (markAI == X) { setAITurn(true); }
            System.out.println("\n--RESTART--\n");
            restartGame();
        });

        topMenu.getChildren().add(createMenuBar());
        topMenu.getChildren().add(restartButton);
        return topMenu;
    }

}
