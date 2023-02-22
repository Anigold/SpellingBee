import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.HashMap;


public class SpellingBee {
	/**
	 * Intermediary run-time class to couple SpellingBeeGraphics to Puzzle logic.
	 * 
	 * All methods are callback functions propagating from SpellingBeeGraphics
	 * and invoking Puzzle class logic. All exceptions from the Puzzle class are
	 * passed to the SpellingBeeGraphics message handler to be displayed.
	 * 
	 */

	/**
	 * Initializes the game board and field callbacks.
	 */
	public void run() {

		sbg = new SpellingBeeGraphics();
		sbg.addButton("Shuffle", (s) -> shuffleAction());
		sbg.addField("Puzzle", (s) -> puzzleAction(s));
		sbg.addButton("Random", (s) -> randomizeAction());
		sbg.addField("Word", (s) -> guessAction(s));
		sbg.addButton("Solve", (s) -> solveAction());

	};

	/**
	 * Callback function for "Puzzle" input field.
	 * 
	 * Determines validity of puzzle input. If valid, generates
	 * the game board and loads the accepted words list to memory.
	 * 
	 * @param puzzleString The puzzle letters to be verified
	 */
	private void puzzleAction(String puzzleString) {
		sbg.showMessage(" ");
		sbg.clearWordList();

		try {

			puzzle = new Puzzle(puzzleString);
			sbg.setBeehiveLetters(puzzleString);
			puzzle.loadWordListFromFile(ENGLISH_DICTIONARY);

		} catch (InvalidPuzzleException e) {
			sbg.showMessage(e.getMessage(), Color.RED);
		} catch (FileNotFoundException fnf) {

		}
	}

	/**
	 * Callback function for "Solve" button.
	 * 
	 * Loads the word list to the game board and displays
	 * scoring.
	 */
	private void solveAction() {

		sbg.clearWordList();
		int totalScore = 0;
		for (String word : puzzle.currentWordList.keySet()) {
			HashMap<String, Integer> wordInfo = puzzle.currentWordList.get(word);

			int score = wordInfo.get("score");
			int pangram = wordInfo.get("pangram");

			totalScore += score;

			String outputString = String.format("%s (%d)", word, score);
			Color outputColor = (pangram == 1) ? Color.BLUE : Color.BLACK;

			sbg.addWord(outputString, outputColor);
			wordInfo.replace("found", 1);
		}

		String totalsOutput = String.format("%d words; %d points", puzzle.currentWordList.size(), totalScore);
		sbg.showMessage(totalsOutput);
	}

	/**
	 * Callback function for "Word" input field.
	 * 
	 * Checks the supplied value against the word list
	 * for the current puzzle. Updates the game board with
	 * scoring information if the word is found.
	 * 
	 * Will update the word list to reflect "found" status.
	 * 
	 * @param word The word to be checked
	 */
	private void guessAction(String word) {

		try {

			String validGuess = puzzle.isValidGuess(word);

			// Get the word info from the current words list.
			HashMap<String, Integer> wordInfo = puzzle.currentWordList.get(validGuess);

			if (wordInfo.get("found") == 1) {
				return;
			}
			;

			int score = wordInfo.get("score");
			int pangram = wordInfo.get("pangram");

			String outputString = String.format("%s (%d)", validGuess, score);
			Color outputColor = (pangram == 1) ? Color.BLUE : Color.BLACK;

			sbg.addWord(outputString, outputColor);
			wordInfo.replace("found", 1);

			int currentScore = puzzle.getCurrentScore();
			int currentWordCount = puzzle.getCurrentWordCount();
			String totalsString = String.format("%d words; %d points", currentWordCount, currentScore);
			sbg.showMessage(totalsString);

			sbg.clearField("Word");

		} catch (InvalidGuessException e) {
			sbg.showMessage(e.getMessage(), Color.RED);
		}
	};

	/**
	 * Callback function to shuffle game board.
	 * 
	 * Displays a new string for the game board; keeps
	 * the middle letter the same.
	 * 
	 */
	private void shuffleAction() {

		// Get current puzzle.
		StringBuilder currentWord = new StringBuilder();
		currentWord.append(sbg.getBeehiveLetters());

		// Set aside middle letter.
		char middleLetter = currentWord.charAt(0);
		currentWord.deleteCharAt(0);

		// Shuffle word.
		List<String> currentWordAsList = Arrays.asList(currentWord.toString().split(""));
		Collections.shuffle(currentWordAsList);

		// Place middle letter back.
		StringBuilder shuffledWord = new StringBuilder();
		shuffledWord.append(middleLetter);

		for (String letter : currentWordAsList) {
			shuffledWord.append(letter);
		}

		// Display shuffled word.
		sbg.setBeehiveLetters(shuffledWord.toString());
	}

	/**Functional but incomplete...
	 * 
	 * Wanted to generate an exhaustive list of puzzles that had between
	 * 21 and 81 words as well as a pangram. Did the math, there will be
	 * 26 choose 7 = 657,800 different combinations.
	 * 
	 * Wrote an inefficient nChoosek algorithm, after an hour of running
	 * it handed out ~300 solutions. This randomizer works functionally, 
	 * the rest of the list just needs to be generated.
	 * 
	 * @throws InvalidPuzzleException
	 * @throws FileNotFoundException
	 * 
	 * 
	 */
	private void randomizeAction() {

		
		try {
			Puzzle randomPuzzle = Puzzle.generateRandomPuzzle();
			puzzleAction(randomPuzzle.letters);
		} catch (FileNotFoundException fnf) {
			
		} catch (InvalidPuzzleException ip) {

		}
	}
	
    /* Constants */

	private static final String ENGLISH_DICTIONARY = "EnglishWords.txt";

	/* Private instance variables */

	private SpellingBeeGraphics sbg;
	private Puzzle puzzle;

	/* Startup code */
	public static void main(String[] args) {
		new SpellingBee().run();
	}

}

class InvalidPuzzleException extends Exception {
	public InvalidPuzzleException(String errorMessage) {
		super(errorMessage);
	}
}

class InvalidGuessException extends Exception {
	public InvalidGuessException(String errorMessage) {
		super(errorMessage);
	}
}
