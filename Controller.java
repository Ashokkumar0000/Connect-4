package com.internshala.connectfour;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {
	private static final int COLOMNS = 7;
	private static final int ROWS = 6;
	private static final int CIRCLE_DIAMETER = 80;
	private static final String discColor1 = "#24303E";
	private static final String discColor2 = "#4CAA88";
	private static String PLAYER_ONE = "Player One";
	private static String PLAYER_TWO = "Player Two";
	private static boolean isPlayerOneTurn = true;
	private Disc[][] insertDiscArray = new Disc[ROWS][COLOMNS]; // for structural changes for developer

	@FXML
	public GridPane rootGridPane;
	@FXML
	public Pane insertedDiscsPane;
	@FXML
	public Label playerNameLable;
	@FXML
	public TextField playerOneTextField, playerTwoTextField;
	@FXML
	public Button setNamesButton;

	private boolean isAllowedToInsert = true;

	public void createPlayGround(){
		Platform.runLater(() -> setNamesButton.requestFocus());
		Shape rectangleWithHoles = createGameStructralGrid();
		rootGridPane.add(rectangleWithHoles,0,1);
		List<Rectangle> rectangleList= clickableColumns();
		for (Rectangle rectangle:rectangleList) {
			rootGridPane.add(rectangle, 0, 1);
		}

		setNamesButton.setOnAction(event -> {
			PLAYER_ONE = playerOneTextField.getText();
			PLAYER_TWO = playerTwoTextField.getText();
			playerNameLable.setText(isPlayerOneTurn? PLAYER_ONE : PLAYER_TWO);
		});

	}
	private Shape createGameStructralGrid(){
		Shape rectangleWithHoles = new Rectangle((COLOMNS + 1) * CIRCLE_DIAMETER, (ROWS +1) * CIRCLE_DIAMETER);
		for(int row = 0; row < ROWS; row++){
			for(int column= 0; column < COLOMNS; column++){
				Circle circle = new Circle();
				circle.setRadius(CIRCLE_DIAMETER / 2);
				circle.setCenterX(CIRCLE_DIAMETER / 2);
				circle.setCenterY(CIRCLE_DIAMETER / 2);
				circle.setSmooth(true);
				circle.setTranslateX(column * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);
				circle.setTranslateY(row * (CIRCLE_DIAMETER +5 ) + CIRCLE_DIAMETER / 4);

				rectangleWithHoles = Shape.subtract(rectangleWithHoles, circle);
			}
		}
		rectangleWithHoles.setFill(Color.ORANGERED);
		return rectangleWithHoles;

	}
	private List<Rectangle> clickableColumns(){
		List<Rectangle> rectangleList = new ArrayList<>();

		for (int col = 0; col < COLOMNS; col++) {
			Rectangle rectangle = new Rectangle(CIRCLE_DIAMETER,(ROWS +1) * CIRCLE_DIAMETER);
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX(col *(CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);
			rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
			rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));
			final int column = col;
			rectangle.setOnMouseClicked(event -> {
				if (isAllowedToInsert){
					isAllowedToInsert = false;
					insertDisc(new Disc(isPlayerOneTurn), column);
				}
			});
			rectangleList.add(rectangle);

		}

		return rectangleList;
	}
	private void insertDisc(Disc disc, int column){
		int row = ROWS - 1;
		while (row >= 0){
			if (getDiscIfPresent(row, column) == null)
				break;
			row--;
		}
		if (row < 0)// if it is full we cannot insert any disc
			return;



		insertDiscArray[row][column] = disc;
		insertedDiscsPane.getChildren().add(disc);
		disc.setTranslateX(column * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);
		int currentRow = row;
		TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5), disc);
		translateTransition.setToY(row * (CIRCLE_DIAMETER +5 ) + CIRCLE_DIAMETER / 4);
		translateTransition.setOnFinished(event -> {
			isAllowedToInsert = true;
			if (gameEnded(currentRow, column)) {
				gameOver();
				return;

			}
			isPlayerOneTurn = !isPlayerOneTurn;
			playerNameLable.setText(isPlayerOneTurn ? PLAYER_ONE : PLAYER_TWO);
		});
		translateTransition.play();
	}

	private boolean gameEnded(int row, int column){
		// vertical Ponts, A small example: player has inserted his last disc at row=2, column = 3


		List<Point2D> verticalPoints = IntStream.rangeClosed(row - 3, row + 3) //range of row values = 0,1,2,3,4,5
				.mapToObj(r -> new Point2D(r, column)) //index of each element present in column [row][column]:0,3 1,3 2,3 3,3 4,3 5,3
				.collect(Collectors.toList());

		List<Point2D> horizontalPoints = IntStream.rangeClosed(column - 3, column + 3)
				.mapToObj(col -> new Point2D(row, col))
				.collect(Collectors.toList());

		Point2D startPoint1 = new Point2D(row - 3, column + 3);

		List<Point2D> diagonal1Points = IntStream.rangeClosed(0,6)
										.mapToObj(i -> startPoint1.add(i, - i))
										.collect(Collectors.toList());

		Point2D startPoint2 = new Point2D(row - 3, column - 3);

		List<Point2D> diagonal2Points = IntStream.rangeClosed(0,6)
				.mapToObj(i -> startPoint2.add(i, i))
				.collect(Collectors.toList());



		boolean isEnded = checkCombination(verticalPoints) || checkCombination(horizontalPoints)
							|| checkCombination(diagonal1Points) || checkCombination(diagonal2Points);

		return isEnded;

	}

	private boolean checkCombination(List<Point2D> points) {
		int chain = 0;
		for (Point2D point : points) {

			int rowIndexForArray = (int) point.getX();
			int columnIndexForArray = (int) point.getY();
			Disc disc = getDiscIfPresent(rowIndexForArray, columnIndexForArray);
			if (disc != null && disc.isPlayerOneMove == isPlayerOneTurn){
				chain++;
				if (chain == 4){
					return true;
				}
			}else {
				chain = 0;
			}
		}
		return false;
	}
	private Disc getDiscIfPresent(int row, int column){
		if (row >= ROWS || row < 0 || column >= COLOMNS || column < 0)
			return null;
		return insertDiscArray[row][column];
	}

	private void gameOver(){
		String winner = isPlayerOneTurn ? PLAYER_ONE : PLAYER_TWO;
		System.out.println("Winner is: " + winner);
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Connect Four");
		alert.setHeaderText("The Winner is " + winner);
		alert.setContentText("Want to play again?");

		ButtonType yesBtn = new ButtonType("Yes");
		ButtonType noBtn = new ButtonType("No, Exit");
		alert.getButtonTypes().setAll(yesBtn,noBtn);


		Platform.runLater(() ->{
			Optional<ButtonType> btnClicked= alert.showAndWait();
			if (btnClicked.isPresent() && btnClicked.get() == yesBtn) {
				resetGame();
			}else {
				Platform.exit();
				System.exit(0);
			}

		});

	}

	public void resetGame() {
		insertedDiscsPane.getChildren().clear();
		for (int row = 0; row < insertDiscArray.length; row++){
			for (int col = 0; col<insertDiscArray[row].length; col++){
				insertDiscArray[row][col] = null;
			}
		}
		isPlayerOneTurn = true;
		playerNameLable.setText(PLAYER_ONE);
		createPlayGround();


	}

	private static class Disc extends Circle{
		private final boolean isPlayerOneMove;
		public Disc(boolean isPlayerOneMove){
			this.isPlayerOneMove = isPlayerOneMove;
			setRadius(CIRCLE_DIAMETER/2.0);
			setFill(isPlayerOneMove? Color.valueOf(discColor1):Color.valueOf(discColor2));
			setCenterX(CIRCLE_DIAMETER/2.0);
			setCenterY(CIRCLE_DIAMETER/2.0);

		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
}
