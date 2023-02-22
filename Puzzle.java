import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Time;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Puzzle {

    /**
     * Game class that maintains state and logic of the SpellingBee game.
     * 
     * Players create their owns puzzles according to the following rules:
     * 
     * 1. The puzzle must contain exactly 7 characters.
     * 2. Only letters from the English alphabet may be used.
     * 3. A letter may be only used once.
     * 
     * The player can then guess words based upon the available letters and
     * according to the following rules:
     * 
     * 1. All words must be at least 4 letters.
     * 2. Letters can be repeated as many times as needed.
     * 3. Only letters found within the game board are valid.
     * 
     * Upon entry of a valid word, the word will be scored based upon the
     * following rules:
     * 
     * 1. A word using 4 letters is worth 1 point, all other words
     * are worth their length (e.g. "chant" is worth 5 points).
     * 2. An additional 7 points are awarded if the player uses all
     * 7 of the letters.
     * 
     * When the player is finished, they may press "Solve" and the remaining
     * words will be added to the word list.
     * 
     */

    String letters;
    String pattern;
    boolean hasPangram = false;

    LinkedHashMap<String, HashMap<String, Integer>> currentWordList = new LinkedHashMap<String, HashMap<String, Integer>>();
    /*
     * wordList <word, wordInfo> = {
     * 
     * "word": {
     * "score": 1,
     * "pangram": false,
     * "found": false,
     * },
     * };
     */

    public Puzzle(String puzzleString) throws InvalidPuzzleException {

        try {
            this.letters = _isValidPuzzle(puzzleString);
            this.pattern = _generatePuzzlePattern(puzzleString);
        } catch (InvalidPuzzleException ipe) {
            throw new InvalidPuzzleException(ipe.getMessage());
        }

    };

    /**
     * Helper function to produce the regular expression for the puzzle
     * 
     * Assumes the supplied puzzle is valid. Inserts letter checks from
     * the puzzle into the regular expression.
     * 
     * @param puzzleString The puzzle to model the regular expression from
     * @return String representation of the regular expression
     */
    private String _generatePuzzlePattern(String puzzleString) {
        return String.format("^(?=[%s]{4,})(?=.*%s.*)(?!.*[^%s])[a-zA-Z]*$",
                puzzleString.toLowerCase(),
                puzzleString.toLowerCase().charAt(0),
                puzzleString.toLowerCase());

    };

    /**
     * Helper function to determine validity of a puzzle string.
     * 
     * Uses regular expressions to determine whether the puzzle string
     * falls within the following rules:
     * 
     * 1. The puzzle must be exactly 7 characters.
     * 2. Contains only letters from the English alphabet.
     * 3. All letters are unique, no letter repeats.
     * 
     * @param puzzleString The puzzle to be checked
     * @return puzzleString if valid, otherwise throws exception
     * @throws InvalidPuzzleException Throws message detailing why the check failed
     */
    private String _isValidPuzzle(String puzzleString) throws InvalidPuzzleException {

        String pattern = "^(?!.*(.).*\\1)[a-zA-Z]{7}$";

        if (!puzzleString.matches(pattern)) {

            if (puzzleString.matches("^.*[^a-zA-Z].*$")) {
                throw new InvalidPuzzleException("Input string contains non-letter symbols.");
            } else if (puzzleString.matches("^.*(.).*\\1.*$")) {
                throw new InvalidPuzzleException("Input string contains repeated letters.");
            } else if (!puzzleString.matches("^.{7}$")) {
                throw new InvalidPuzzleException("Input string does not contain exactly 7 characters.");
            } else {
                throw new InvalidPuzzleException("Input string does not match the expected pattern.");
            }
        }
        return puzzleString;
    };

    /**
     * Helper function to determine validity of a guess.
     * 
     * A guess must fall within the following rules:
     *
     * 1. The word must be at least 4 letters long.
     * 2. The word can only use the letters provided by the game board.
     * 3. The word must use the center letter.
     * 4. The word must be found in the dictionary.
     * 5. The word cannot have already been found.
     * 
     * @param guessString Player's guess to be checked
     * @return
     * @throws InvalidGuessException
     */
    public String isValidGuess(String guessString) throws InvalidGuessException {

        boolean inDict = this.currentWordList.containsKey(guessString);
        boolean usesLetters = guessString.matches(String.format("^[%s]*$", this.letters.toLowerCase()));
        boolean isSize = guessString.length() >= 4;
        boolean usesCenter = guessString
                .matches(String.format("^(?=.*%s.*)[a-zA-Z]*$", this.letters.toLowerCase().charAt(0)));
        boolean isFound = inDict && (this.currentWordList.get(guessString).get("found") != 1);

        if (!isSize) {
            throw new InvalidGuessException("Not enough letters.");
        } else if (!usesLetters) {
            throw new InvalidGuessException("Word uses invalid letter.");
        } else if (!usesCenter) {
            throw new InvalidGuessException("Word does not use center letter.");
        } else if (!inDict) {
            throw new InvalidGuessException("Word was not found in dictionary.");
        } else if (!isFound) {
            throw new InvalidGuessException("Already found.");
        }

        return guessString;
    }

    /**
     * Helper function to load word list from file.
     * 
     * Scans file for words that fit the pattern. Loads
     * matching words to the accepted words list.
     * 
     * @param path    The string representation of the path to the file
     * @param pattern The Regular Expression pattern to be tested against
     * @throws FileNotFoundException
     */
    public void loadWordListFromFile(String path) throws FileNotFoundException {
        this.currentWordList.clear();

        try {
            Scanner fileScanner = new Scanner(new File(path));

            while (fileScanner.hasNext()) {
                String word = fileScanner.next();
                if (!word.matches(this.pattern)) {
                    continue;
                }

                // This could be a good time to use Prototype Pattern
                HashMap<String, Integer> wordInfo = new HashMap<String, Integer>();

                int wordScore = _scoreWord(word);
                int pangram = _isPangram(word) ? 1 : 0;

                if (!hasPangram && pangram == 1) {
                    hasPangram = true;
                }
                wordInfo.put("score", wordScore);
                wordInfo.put("pangram", pangram);
                wordInfo.put("found", 0);

                this.currentWordList.put(word, wordInfo);
            }
            ;

            fileScanner.close();

        } catch (FileNotFoundException fnf) {
            throw new FileNotFoundException(String.format("File not found at path: %s", path));
        } catch (NoSuchElementException nse) {
            throw new NoSuchElementException();
        }
    };

    /**
     * Functional but incomplete...
     * 
     * Wanted to generate an exhaustive list of puzzles that had between
     * 21 and 81 words as well as a pangram. Did the math, there will be
     * 26 choose 7 = 657,800 different combinations.
     * 
     * *UPDATE: The above math is wrong. This doesn't work due to not forcing
     * each letter to be in the starting position (i.e. be the middle letter).
     * 
     * The equation should instead be (26 choose 1) * (25 choose 6).
     * We can choose each of the 26 letters to be in the starting position,
     * and then choose 6 from the remaining 25 letters. 
     * 
     * We get 4,604,600 different permutations. 
     * 
     * RIP my CPU.
     * 
     * Wrote an inefficient nChoosek algorithm, after an hour of running
     * it handed out ~300 solutions. This randomizer works functionally,
     * the rest of the list just needs to be generated.
     * 
     * A consequence of this is that all random puzzles have a center letter with A.
     * 
     */
    public static Puzzle generateRandomPuzzle() throws FileNotFoundException, InvalidPuzzleException {

        try {
            Scanner fileScanner = new Scanner(new File("ExhaustiveList.txt"));
            fileScanner.useDelimiter(Pattern.compile("(\\n)|;"));
            Random rand = new Random();
            rand.setSeed(System.currentTimeMillis());

            String puzzle = "acegiop";
            int upperbound = 300;

            int randomInt = rand.nextInt(upperbound);

            int lineCount = 0;
            while (fileScanner.hasNext()) {
                if (lineCount == randomInt) {

                    puzzle = fileScanner.next();
                    break;
                }
                fileScanner.next();
                ++lineCount;
            }

            return new Puzzle(puzzle.split(" ")[0]);

        } catch (FileNotFoundException fnf) {
            throw new FileNotFoundException(String.format("No file found at path: ./ExhaustiveList.txt"));
        } catch (InvalidPuzzleException ip) {
            throw new InvalidPuzzleException(ip.getMessage());
        }

    }

    /**
     * Helper function to determine if a word is a pangram.
     * 
     * Checks the word to see if it uses 7 distinct letters.
     * It assumes the word is valid within the puzzle.
     * 
     * @param word The word to be checked
     * @return boolean
     */
    private boolean _isPangram(String word) {
        return word.chars().distinct().count() == 7;
    };

    /**
     * Helper function to determine score of a given word.
     * 
     * Scored based on the following rules:
     * 1. A word using 4 letters is worth 1 point, all other words
     * are worth their length (e.g. "chant" is worth 5 points).
     * 2. An additional 7 points are awarded if the player uses all
     * 7 of the letters (i.e. the word is a pangram).
     * 
     * @param word The word to be checked
     * @return int value of the word score
     */
    private int _scoreWord(String word) {
        int baseScore = word.length() == 4 ? 1 : word.length();
        int pangramScore = (_isPangram(word)) ? 7 : 0;

        return baseScore + pangramScore;
    }

    /**
     * Getter function to calculate current score.
     * 
     * Iterates through current accepted words list, tallying
     * the score when a word is flagged as found.
     * 
     * @return int value of the total score
     */
    public int getCurrentScore() {
        int totalScore = 0;

        for (String word : this.currentWordList.keySet()) {
            HashMap<String, Integer> wordInfo = this.currentWordList.get(word);
            if (wordInfo.get("found") == 1) {
                int wordScore = wordInfo.get("score");
                totalScore += wordScore;
            }
        }
        return totalScore;
    }

    /**
     * Getter function to tally the number of found words.
     * 
     * Iterates through current accepted words list, tallying
     * the count when a word is flagged as found.
     * 
     * @return int value of the total word count
     */
    public int getCurrentWordCount() {
        int totalWords = 0;

        for (String word : this.currentWordList.keySet()) {
            HashMap<String, Integer> wordInfo = this.currentWordList.get(word);
            if (wordInfo.get("found") == 1) {
                ++totalWords;
            }
        }
        return totalWords;

    }

}
