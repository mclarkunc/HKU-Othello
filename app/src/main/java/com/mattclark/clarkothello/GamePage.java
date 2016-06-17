package com.mattclark.clarkothello;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;


import java.util.ArrayList;

public class GamePage extends AppCompatActivity {


    ImageButton board[][] = new ImageButton[8][8]; //2-D array of ImageButtons to simulate the 64 squares of the board
    int boardColors[][] = new int[8][8];  //Hold the colors of the pieces, 0 is empty square, 1 is white, 2 is black
    ArrayList<Integer> nextValidMove = new ArrayList<Integer>();    //holds the ImageButton id values of next valid moves
    ArrayList<Integer> squaresToFlip = new ArrayList<Integer>();  //holds the squares needing to be flipped
    Integer blackScore = 0; //Scores are declared Integer because need to use toString to display in ImageView
    Integer whiteScore = 0;
    boolean currentTurnIsBlack = true; //Initialize currentTurn as black
    boolean displayHints = false;    //Initialize displayHints

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_page);

        //Initialize board setup
        initializeBoard();

        //Hints Toggle Button
        ToggleButton toggleHints = (ToggleButton) findViewById(R.id.toggleHints);
        toggleHints.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Turn Hints On
                    displayHints = true;
                    drawHints();
                } else {
                    // Turn Hints Off
                    displayHints = false;
                    eraseHints();
                }
            }
        });

        //New Game Button
        Button startNewGame = (Button) findViewById(R.id.buttonNewGame);
        startNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeBoard();
            }
        });

    }



    public void initializeBoard() {

        //Initalize array of ImageButtons (board[][]) and the array of boardColors
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {

                String sqr = "square" + i + j;
                int rID = getResources().getIdentifier(sqr, "id", getPackageName());
                board[i][j] = (ImageButton) findViewById(rID);
                board[i][j].setImageResource(R.drawable.transparent); //Reset all squares to transparent
                boardColors[i][j] = 0; //set all squares to 'empty'

            }
        }

        //Initialize center pieces of the ImageButtons, 2 black, 2 white
        board[3][3].setImageResource(R.drawable.white_chess);
        board[3][4].setImageResource(R.drawable.black_chess);
        board[4][4].setImageResource(R.drawable.white_chess);
        board[4][3].setImageResource(R.drawable.black_chess);

        //Initialize the colors in the pieces holder
        boardColors[3][3] = 1;  //white
        boardColors[3][4] = 2;  //black
        boardColors[4][4] = 1;  //white
        boardColors[4][3] = 2;  //black

        //Initialize black and white scores
        blackScore = 2;
        whiteScore = 2;
        updateScores();

        //Initialize nextValidMove (at start of the game, black moves first, and has 4 valid moves
        if (nextValidMove.size() > 0) nextValidMove.clear();
        nextValidMove.add(board[2][3].getId());
        nextValidMove.add(board[3][2].getId());
        nextValidMove.add(board[4][5].getId());
        nextValidMove.add(board[5][4].getId());

        //Draw hints if toggled
        if (displayHints) drawHints();

        //Initialize currentTurn
        currentTurnIsBlack = true;
        setImageViewTurn();

    }




    //Method to handle clicks on the 64 ImageButtons of the board
    public void clickSquare(View v){
        ImageButton square = (ImageButton) findViewById(v.getId());

        //If button clicked is a valid move
        if (isValid(square)) {

            String tempSqrName = getResources().getResourceName(v.getId());
            tempSqrName = tempSqrName.substring(tempSqrName.length() - 2);
            //System.out.println("SqrName: " + tempSqrName);
            int row = Integer.parseInt(tempSqrName.substring(0, 1));
            int col = Integer.parseInt(tempSqrName.substring(1, 2));
            //System.out.println("row = " + String.valueOf(row) + " col = " + String.valueOf(col));

            //Place the piece, add score
            if (currentTurnIsBlack) {
                board[row][col].setImageResource(R.drawable.black_chess);
                boardColors[row][col] = 2;
                blackScore++;

            } else {
                board[row][col].setImageResource(R.drawable.white_chess);
                boardColors[row][col] = 1;
                whiteScore++;
            }

            //Flip squares that are bounded
            flipSquares(row, col);

            //Update Scores
            updateScores();

            //Remove square from valid moves Array
            nextValidMove.remove((Integer)square.getId());

            //change turn
            currentTurnIsBlack = !(currentTurnIsBlack);

            //Remove translucent pieces (if hints are turned on)
            if (displayHints) eraseHints();

            //Fill the nextValidMove array with the next possible moves
            fillNextValidMove();

            //check to see if any valid moves are available, if not, change turn
            if (nextValidMove.size() == 0) {

                //System.out.println("****Changed turn without move");
                currentTurnIsBlack = !(currentTurnIsBlack); //change turn
                fillNextValidMove(); //recall function

                if (nextValidMove.size() == 0){
                    //If neither color can move, game is over
                    gameOver();

                }
            }

            //Re-draw new valid moves (if hints are turned on)
            if (displayHints) drawHints();


            //Change Turn graphic
            setImageViewTurn();

        }
    }


    //Check to see if move is in nextValidMove array
    public boolean isValid(ImageButton sqr){
        //System.out.println("Is square in ArrayList?" + nextValidMove.contains(sqr.getId()));
        return nextValidMove.contains(sqr.getId());
    }

    //Update the score TextViews
    public void updateScores(){
        TextView textBlackScore = (TextView) findViewById(R.id.textViewBlackScore);
        TextView textWhiteScore = (TextView) findViewById(R.id.textViewWhiteScore);
        textBlackScore.setText(blackScore.toString());
        textWhiteScore.setText(whiteScore.toString());
    }

    //Update the black/white Turn button
    public void setImageViewTurn() {
        ImageView turn = (ImageView) findViewById(R.id.imageViewTurn);
        if (currentTurnIsBlack) {
            turn.setImageResource(R.drawable.black_chess);

        } else {
            turn.setImageResource(R.drawable.white_chess);
        }
    }


    //Fill nextValidMove ArrayList
    public void fillNextValidMove(){

        //Clear nextValidMove ArrayList
        nextValidMove.clear();

        //Cycle through all squares on board to determine if valid
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                //Check if square is empty
                if (boardColors[i][j] == 0 && ( checkN(i,j,false) || checkS(i,j,false) || checkE(i,j,false) || checkW(i,j,false) ||
                            checkNE(i,j,false) || checkNW(i,j,false) || checkSE(i,j,false) || checkSW(i,j,false) )) {
                    //at least one direction is bounded correctly = valid move. Add to ArrayList
                    nextValidMove.add(board[i][j].getId());

                    //System.out.println("Valid for square: row: "+ i + " col: " + j);
                }

            }
        }

    }

    //Remove the translucent pieces, replace with transparent.png
    public void eraseHints(){
        for (int i = 0; i < nextValidMove.size(); i++){
            ImageButton tempImgButton = (ImageButton) findViewById(nextValidMove.get(i));
            tempImgButton.setImageResource(R.drawable.transparent);
        }
    }

    //Draw the translucent pieces, if hints are turned on
    public void drawHints(){
        for (int i = 0; i < nextValidMove.size(); i++){
            ImageButton tempImgButton = (ImageButton) findViewById(nextValidMove.get(i));
            if (currentTurnIsBlack) {
                tempImgButton.setImageResource(R.drawable.black_chess_t);
            } else {
                tempImgButton.setImageResource(R.drawable.white_chess_t);

            }
        }
    }

    //Display Game Over message with winner (or draw)
    public void gameOver(){

        //Code borrowed and modified from http://www.mkyong.com/android/android-custom-dialog-example/
        final Dialog winnerMsg = new Dialog(this);
        winnerMsg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        winnerMsg.setContentView(R.layout.winner);

        ImageView imageWinner = (ImageView) winnerMsg.findViewById(R.id.imageWinner);
        TextView textWinner = (TextView) winnerMsg.findViewById(R.id.textWinner);

        //If scores are equal, game is a draw
        if (blackScore == whiteScore) {
            imageWinner.setImageResource(R.drawable.transparent);
            imageWinner.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
            textWinner.setText("IT'S A DRAW!");
        } else {
            //Black wins if blackScore > whiteScore; vice versa
            if (blackScore > whiteScore) imageWinner.setImageResource(R.drawable.black_chess);
            else imageWinner.setImageResource(R.drawable.white_chess);
        }

        Button winnerButton = (Button) winnerMsg.findViewById(R.id.winnerOk);
        //If button is clicked, close the dialog box
        winnerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                winnerMsg.dismiss();
            }
        });
        winnerMsg.show();

    }

    //Flip squares bounded by current color
    public void flipSquares(int myRow, int myCol){
        //Clear squaresToFlip
        squaresToFlip.clear();

        //Update squaresToFlip
        boolean isN = checkN(myRow, myCol, true);
        boolean isS = checkS(myRow, myCol, true);
        boolean isE = checkE(myRow, myCol, true);
        boolean isW = checkW(myRow, myCol, true);
        boolean isNE = checkNE(myRow, myCol, true);
        boolean isNW = checkNW(myRow, myCol, true);
        boolean isSE = checkSE(myRow, myCol, true);
        boolean isSW = checkSW(myRow, myCol, true);

        //System.out.println("N?: " + isN + " S?: " + isS + " E?: " + isE + " W?:" + isW +
        //            " NE?: " + isNE + " NW?: " + isNW + " SE?: " + isSE + " SW?: " + isSW);

        //Cycle through squaresToFlip and update color of pieces
        ImageButton tempSquare;
        for (int i = 0; i < squaresToFlip.size(); i++) {
            tempSquare = (ImageButton) findViewById(squaresToFlip.get(i));
            String tempSqrName = getResources().getResourceName(tempSquare.getId());
            tempSqrName = tempSqrName.substring(tempSqrName.length() - 2);
            int row = Integer.parseInt(tempSqrName.substring(0, 1));
            int col = Integer.parseInt(tempSqrName.substring(1, 2));

            if (currentTurnIsBlack) {
                board[row][col].setImageResource(R.drawable.black_chess);
                boardColors[row][col] = 2;  //Set color to black
                blackScore++;   //add 1 to black score
                whiteScore--;   //subtract 1 from black score
                //System.out.println("Flipping. Color: black. row: " + row + " col: " + col);
            }
            else {
                board[row][col].setImageResource(R.drawable.white_chess);
                boardColors[row][col] = 1;  //Set color to white
                whiteScore++; //add 1 to white score
                blackScore--; //subtract 1 from black score
                //System.out.println("Flipping. Color: white. row: " + row + " col: " + col);
            }
        }

    }


    //Directions to check if move is valid, or what squares to flip
    //8 Directions: N, S, E, W, NE, NW, SE, SW
    public boolean checkN (int myRow, int myCol, boolean addToFlip){
        int tempRow = myRow;
        if (tempRow > 1) {
            tempRow--;
            if ((currentTurnIsBlack && boardColors[tempRow][myCol] != 1) || (!(currentTurnIsBlack) && boardColors[tempRow][myCol] != 2)){
                //Piece above current square is the same color as the current turn, or it's empty = not a valid move
                return false;
            }
            else {
                //Piece above current square is different color from current turn. Keep checking up until find current color.
                ArrayList<Integer> tempFlipHolder = new ArrayList<Integer>();
                do {
                    //Add square to temp flip list just in case it's a valid move
                    if (addToFlip) tempFlipHolder.add(board[tempRow][myCol].getId());

                    tempRow--;
                    //If temp square is empty, return false
                    if (boardColors[tempRow][myCol] == 0) return false;
                    //If temp square is same color as turn, then it's a valid move. Else, continue loop
                    if ((currentTurnIsBlack && boardColors[tempRow][myCol] == 2) || (!(currentTurnIsBlack) && boardColors[tempRow][myCol] == 1)) {
                        //If checking which squares to flip, add bounded squares to running list
                        if (addToFlip) squaresToFlip.addAll(tempFlipHolder);

                        //System.out.println("Valid move, N");
                        return true;
                    }
                }while (tempRow > 0);
            }
        }

        return false; //if loop ends without finding valid move

    }

    public boolean checkS (int myRow, int myCol, boolean addToFlip){
        int tempRow = myRow;
        if (tempRow < 6) {
            tempRow++;
            if ((currentTurnIsBlack && boardColors[tempRow][myCol] != 1) || (!(currentTurnIsBlack) && boardColors[tempRow][myCol] != 2)){
                //Piece below current square is the same color as the current turn, or it's empty = not a valid move
                return false;
            }
            else {
                //Piece below current square is different color from current turn. Keep checking down until find current color.
                ArrayList<Integer> tempFlipHolder = new ArrayList<Integer>();
                do {
                    //Add square to temp flip list just in case it's a valid move
                    if (addToFlip) tempFlipHolder.add(board[tempRow][myCol].getId());

                    tempRow++;
                    //If temp square is empty, return false
                    if (boardColors[tempRow][myCol] == 0) return false;

                    //If temp square is same color as turn, then it's a valid move. Else, continue loop
                    if ((currentTurnIsBlack && boardColors[tempRow][myCol] == 2) || (!(currentTurnIsBlack) && boardColors[tempRow][myCol] == 1)){
                        //If checking which squares to flip, add bounded squares to running list
                        if (addToFlip) squaresToFlip.addAll(tempFlipHolder);

                        //System.out.println("Valid move, S");
                        return true;
                    }
                } while (tempRow < 7);
            }

        }

        return false; //if loop ends without finding valid move

    }

    public boolean checkE (int myRow, int myCol, boolean addToFlip){
        int tempCol = myCol;
        if (tempCol < 6) {
            tempCol++;
            if ((currentTurnIsBlack && boardColors[myRow][tempCol] != 1) || (!(currentTurnIsBlack) && boardColors[myRow][tempCol] != 2)){
                //Piece to the right (east) of current square is the same color as the current turn, or it's empty = not a valid move
                return false;
            }
            else {
                //Piece to the right of current square is different color from current turn. Keep checking down until find current color.
                ArrayList<Integer> tempFlipHolder = new ArrayList<Integer>();
                do {
                    //Add square to temp flip list just in case it's a valid move
                    if (addToFlip) tempFlipHolder.add(board[myRow][tempCol].getId());

                    tempCol++;
                    //If temp square is empty, return false
                    if (boardColors[myRow][tempCol] == 0) return false;

                    //If temp square is same color as turn, then it's a valid move. Else, continue loop
                    if ((currentTurnIsBlack && boardColors[myRow][tempCol] == 2) || (!(currentTurnIsBlack) && boardColors[myRow][tempCol] == 1)){
                        //If checking which squares to flip, add bounded squares to running list
                        if (addToFlip) squaresToFlip.addAll(tempFlipHolder);

                        //System.out.println("Valid move, E");
                        return true;
                    }
                }while (tempCol < 7);
            }
        }

        return false; //if loop ends without finding valid move

    }

    public boolean checkW (int myRow, int myCol, boolean addToFlip){
        int tempCol = myCol;
        if (tempCol > 1) {
            tempCol--;
            if ((currentTurnIsBlack && boardColors[myRow][tempCol] != 1) || (!(currentTurnIsBlack) && boardColors[myRow][tempCol] != 2)){
                //Piece to the left (west) of current square is the same color as the current turn, or it's empty = not a valid move
                return false;
            }
            else {
                //Piece to the left of current square is different color from current turn. Keep checking down until find current color.
                ArrayList<Integer> tempFlipHolder = new ArrayList<Integer>();
                do {
                    //Add square to temp flip list just in case it's a valid move
                    if (addToFlip) tempFlipHolder.add(board[myRow][tempCol].getId());

                    tempCol--;
                    //If temp square is empty, return false
                    if (boardColors[myRow][tempCol] == 0) return false;

                    //If temp square is same color as turn, then it's a valid move. Else, continue loop
                    if ((currentTurnIsBlack && boardColors[myRow][tempCol] == 2) || (!(currentTurnIsBlack) && boardColors[myRow][tempCol] == 1)){
                        //If checking which squares to flip, add bounded squares to running list
                        if (addToFlip) squaresToFlip.addAll(tempFlipHolder);

                        //System.out.println("Valid move, W");
                        return true;
                    }
                }while (tempCol > 0);
            }

        }

        return false; //if loop ends without finding valid move

    }

    public boolean checkNE (int myRow, int myCol, boolean addToFlip){
        int tempRow = myRow;
        int tempCol = myCol;
        if (tempRow > 1 && tempCol < 6) {
            tempRow--;
            tempCol++;
            if ((currentTurnIsBlack && boardColors[tempRow][tempCol] != 1) || (!(currentTurnIsBlack) && boardColors[tempRow][tempCol] != 2)){
                //Piece to the northeast of current square is the same color as the current turn, or it's empty = not a valid move
                return false;
            }
            else {
                //Piece to the northeast of current square is different color from current turn. Keep checking NE until find current color.
                ArrayList<Integer> tempFlipHolder = new ArrayList<Integer>();
                do {
                    //Add square to temp flip list just in case it's a valid move
                    if (addToFlip) tempFlipHolder.add(board[tempRow][tempCol].getId());

                    tempRow--;
                    tempCol++;
                    //If temp square is empty, return false
                    if (boardColors[tempRow][tempCol] == 0) return false;

                    //If temp square is same color as turn, then it's a valid move. Else, continue loop
                    if ((currentTurnIsBlack && boardColors[tempRow][tempCol] == 2) || (!(currentTurnIsBlack) && boardColors[tempRow][tempCol] == 1)){
                        //If checking which squares to flip, add bounded squares to running list
                        if (addToFlip) squaresToFlip.addAll(tempFlipHolder);

                        //System.out.println("Valid move, NE");
                        return true;
                    }
                } while (tempRow > 0 && tempCol < 7);
            }
        }

        return false; //if loop ends without finding valid move

    }

    public boolean checkNW (int myRow, int myCol, boolean addToFlip){
        int tempRow = myRow;
        int tempCol = myCol;
        if (tempRow > 1 && tempCol > 1) {
            tempRow--;
            tempCol--;
            if ((currentTurnIsBlack && boardColors[tempRow][tempCol] != 1) || (!(currentTurnIsBlack) && boardColors[tempRow][tempCol] != 2)){
                //Piece to the northwest of current square is the same color as the current turn, or it's empty = not a valid move
                return false;
            }
            else {
                //Piece to the northwest of current square is different color from current turn. Keep checking NW until find current color.
                ArrayList<Integer> tempFlipHolder = new ArrayList<Integer>();

                do {
                    //Add square to temp flip list just in case it's a valid move
                    if (addToFlip) tempFlipHolder.add(board[tempRow][tempCol].getId());

                    tempRow--;
                    tempCol--;
                    //If temp square is empty, return false
                    if (boardColors[tempRow][tempCol] == 0) return false;

                    //If temp square is same color as turn, then it's a valid move. Else, continue loop
                    if ((currentTurnIsBlack && boardColors[tempRow][tempCol] == 2) || (!(currentTurnIsBlack) && boardColors[tempRow][tempCol] == 1)){
                        //If checking which squares to flip, add bounded squares to running list
                        if (addToFlip) squaresToFlip.addAll(tempFlipHolder);

                        //System.out.println("Valid move, NW");
                        return true;
                    }
                } while (tempRow > 0 && tempCol > 0);
            }
        }

        return false; //if loop ends without finding valid move

    }

    public boolean checkSE (int myRow, int myCol, boolean addToFlip){
        int tempRow = myRow;
        int tempCol = myCol;
            if (tempRow < 6 && tempCol < 6) {
            tempRow++;
            tempCol++;
            if ((currentTurnIsBlack && boardColors[tempRow][tempCol] != 1) || (!(currentTurnIsBlack) && boardColors[tempRow][tempCol] != 2)){
                //Piece to the southeast of current square is the same color as the current turn, or it's empty = not a valid move
                return false;
            }
            else {
                //Piece to the southeast of current square is different color from current turn. Keep checking SE until find current color.
                ArrayList<Integer> tempFlipHolder = new ArrayList<Integer>();
                do {
                    //Add square to temp flip list just in case it's a valid move
                    if (addToFlip) tempFlipHolder.add(board[tempRow][tempCol].getId());

                    tempRow++;
                    tempCol++;
                    //If temp square is empty, return false
                    if (boardColors[tempRow][tempCol] == 0) return false;

                    //If temp square is same color as turn, then it's a valid move. Else, continue loop
                    if ((currentTurnIsBlack && boardColors[tempRow][tempCol] == 2) || (!(currentTurnIsBlack) && boardColors[tempRow][tempCol] == 1)){
                        //If checking which squares to flip, add bounded squares to running list
                        if (addToFlip) squaresToFlip.addAll(tempFlipHolder);

                        //System.out.println("Valid move, SE");
                        return true;
                    }
                } while (tempRow < 7 && tempCol < 7);
            }
        }

        return false; //if loop ends without finding valid move

    }

    public boolean checkSW (int myRow, int myCol, boolean addToFlip){
        int tempRow = myRow;
        int tempCol = myCol;
        if (tempRow < 6 && tempCol > 1) {
            tempRow++;
            tempCol--;
            if ((currentTurnIsBlack && boardColors[tempRow][tempCol] != 1) || (!(currentTurnIsBlack) && boardColors[tempRow][tempCol] != 2)){
                //Piece to the southwest of current square is the same color as the current turn, or it's empty = not a valid move
                return false;
            }
            else {
                //Piece to the southwest of current square is different color from current turn. Keep checking SW until find current color.

                ArrayList<Integer> tempFlipHolder = new ArrayList<Integer>();
                do {
                    //Add square to temp flip list just in case it's a valid move
                    if (addToFlip) tempFlipHolder.add(board[tempRow][tempCol].getId());

                    tempRow++;
                    tempCol--;
                    //If temp square is empty, return false
                    if (boardColors[tempRow][tempCol] == 0) return false;

                    //If temp square is same color as turn, then it's a valid move. Else, continue loop
                    if ((currentTurnIsBlack && boardColors[tempRow][tempCol] == 2) || (!(currentTurnIsBlack) && boardColors[tempRow][tempCol] == 1)){
                        //If checking which squares to flip, add bounded squares to running list
                        if (addToFlip) squaresToFlip.addAll(tempFlipHolder);

                        //System.out.println("Valid move, SW");
                        return true;
                    }
                } while (tempRow < 7 && tempCol > 0);
            }
        }

        return false; //if loop ends without finding valid move

    }



}
