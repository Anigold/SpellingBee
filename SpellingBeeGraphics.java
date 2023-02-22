/*
 * File: SpellingBeeGraphics.java
 * ------------------------------
 * This file implements the SpellingBeeGraphics class, which manages
 * the graphical display for the SpellingBee project.
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * This class provides the graphics library framework for the SpellingBee
 * application.  The project handout describes the public methods in more
 * detail.
 */
public class SpellingBeeGraphics {
	/* Private constants */
	private static final int DEFAULT_FIELD_SIZE = 10;

	/* Private instance variables */
	private JFrame frame;
	private JPanel controlStrip;
	private SpellingBeeCanvas canvas;
	private HashMap<String,JTextField> textFields;

	public SpellingBeeGraphics() {
		frame = new JFrame("SpellingBee");						// create a JFrame object
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	// What happens when it closes?
		frame.setLayout(new BorderLayout());					// Specify frame layout manager
		
		controlStrip 	= new JPanel();							// create a container object (JPanel)
		canvas 			= new SpellingBeeCanvas();				// From custom graphics api
		textFields 		= new HashMap<String,JTextField>();		// HashMap = Python dictionary
		
		frame.add(controlStrip, BorderLayout.SOUTH);			// place container in JFrame SOUTH area		
		frame.add(canvas, BorderLayout.CENTER);					// place canvas in the CENTER area
		frame.pack();											// process and size layout
		frame.setVisible(true);									// show it
	}

/**
 * Sets the letters in the beehive on the window, placing the first letter
 * in the center hex.  The letters are converted to uppercase before
 * displaying them.
 *
 * @param letters The letters to put in the beehive
 */
	public void setBeehiveLetters(String letters) {
		canvas.setBeehiveLetters(letters);
	}

/**
 * Returns the letters in the beehive, all in uppercase.
 *
 * @return The letters in the beehive
 */
	public String getBeehiveLetters() {
		return canvas.getBeehiveLetters();
	}

/**
 * Clears the word list and erases all entries from the window.
 */
	public void clearWordList() {
		canvas.clearWordList();
	}

/**
 * Adds a word to the word list displayed on the window.
 *
 * @param word The word to be added
 */
	public void addWord(String word) {
		addWord(word, Color.BLACK);
	}

/**
 * Adds a word to the word list displayed on the window using the
 * specified color.
 *
 * @param word The word to be added
 * @param color The color of the word
 */
	public void addWord(String word, Color color) {
		canvas.addWord(word, color);
	}

/**
 * Displays a message in the message area at the bottom of the window.
 *
 * @param msg The string to display
 */
	public void showMessage(String msg) {
		showMessage(msg, Color.BLACK);
	}

/**
 * Displays a message in the message area at the bottom of the window.
 * This version of the method allows the caller to set the text color.
 *
 * @param msg The string to display
 * @param color The color of the text
 */
	public void showMessage(String msg, Color color) {
		canvas.showMessage(msg, color);
	}

/**
 * Adds a text field to the control strip at the bottom of the window,
 * where the text field is large enough to hold nchars characters.  When
 * the user hits RETURN or ENTER in the text field, the implementation
 * calls the specified listener function, passing in the value of the
 * text field.
 *
 * @param name The name of the text field, which appears as a label
 * @param listener A callback function that listens for a RETURN
 * @param nchars The number of characters in the field
 */
	public void addField(String name, SpellingBeeEventListener listener, int nchars) {
		JLabel label 		= new JLabel(name);			// each field gets a label
		JTextField field	= new JTextField(nchars);	// each field gets a text box

		field.addActionListener(new TextFieldListener(field, listener)); // specify callback function
		field.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); // apply a border
		
		controlStrip.add(label);	// add new label to the "control strip"
		controlStrip.add(field);	// add new field to the "control strip"
		textFields.put(name, field);// add new objects to map
		frame.pack();				// reorganize
	}

/**
 * Adds a text field to the control strip at the bottom of the window,
 * where the text field is defaulted to hold ten characters.
 *
 * @param name The name of the text field, which appears as a label
 * @param listener A callback function that listens for a RETURN
 */
	public void addField(String name, SpellingBeeEventListener listener) {
		addField(name, listener, DEFAULT_FIELD_SIZE);
	}

/**
 * Returns the text contained in the specified field.
 *
 * @param name The name of the field
 * @return The contents of the field
 */
	public String getField(String name) {
		JTextField field = textFields.get(name);
		if (field == null)
			throw new RuntimeException("Undefined field " + name);
		return field.getText();
	}

/**
 * Clears the text contained in the specified field.
 * And sets focus
 * 
 * @param name The name of the field
 */
	public void clearField(String name){
		JTextField field = textFields.get(name);
		if (field == null)
			throw new RuntimeException("Undefined field " + name);
		field.setText("");
		field.requestFocus();
	}

/**
 * Sets the text contained in the specified field.
 *
 * @param name The name of the field
 * @param text The new contents of the field
 */
	public void setField(String name, String text) {
		JTextField field = textFields.get(name);
		if (field == null) {
			throw new RuntimeException("Undefined field " + name);
		}
		field.setText(text);
	}

/**
 * Adds a button to the control strip.  When the user clicks the button,
 * the implementation calls the listener function, passing in the name
 * of the button.
 *
 * @param name The name of the button
 * @param listener The listener called when the button is clicked
 */
	public void addButton(String name, SpellingBeeEventListener listener) {
		JButton button = new JButton(name);
		button.addActionListener(new ButtonListener(button, listener));
		controlStrip.add(button);
		frame.pack();
	}
}

class SpellingBeeCanvas extends JComponent {
	/* Private constants */
	private static final int CANVAS_WIDTH 		= 1000;
	private static final int CANVAS_HEIGHT 		= 300;

	private static final int BEEHIVE_X 			= 150;
	private static final int BEEHIVE_Y 			= 150;

	private static final int HEX_SIDE 			= 40;
	private static final int HEX_SEP 			= 76;

	private static final String LETTER_FONT 	= "Helvetica Neue-Bold-40";
	private static final String WORDLIST_FONT 	= "Helvetica Neue-16";
	private static final String MESSAGE_FONT 	= "Helvetica Neue-Bold-16";

	private static final Color CENTER_HEX_COLOR = Color.decode("#FFCC33");
	private static final Color OUTER_HEX_COLOR 	= Color.decode("#DDDDDD");

	private static final double ASCENT_FRACTION = 0.38;

	private static final int WORDLIST_X 		= 300;
	private static final int WORDLIST_Y 		= 20;
	private static final int WORDLIST_DX 		= 150;
	private static final int WORDLIST_DY 		= 17;
	private static final int WORDLIST_HEIGHT 	= 250;
	private static final int MESSAGE_MARGIN 	= 15;

	/* Private instance variables */
	private ArrayList<StringColorPair> wordList;
	
	private Color messageColor;
	private String letters;
	private String messageText;

	public SpellingBeeCanvas() {
		super(); 										// super class constructor call
		setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));	// set the size using constants
		letters 	= "       ";						// string to hold the letters
		messageText = "";								// string to hold a message
		wordList 	= new ArrayList<StringColorPair>();	// List of ColorPair objects
	}

/**
 * Sets the letters in the beehive on the window, placing the first letter
 * in the center hex.  The function checks to make sure that the string has
 * exactly seven characters, each of which is alphabetic.  The letters are
 * converted to uppercase before displaying them.
 *
 * @param letters The letters to put in the beehive
 */
	public void setBeehiveLetters(String letters) {
		this.letters = letters.toUpperCase();
		repaint(); // calls paint component
	}

/**
 * Returns the letters in the beehive, all in uppercase.
 *
 * @return The letters in the beehive
 */
	public String getBeehiveLetters() {
		return letters;
	}

/**
 * Clears the word list and erases all entries from the window.
 */
	public void clearWordList() {
		wordList.clear();
		repaint();
	}

/**
 * Adds a word to the word list displayed on the window.
 *
 * @param word The word to be added
 */
	public void addWord(String word) {
		addWord(word, Color.BLACK);
	}

/**
 * Adds a word to the word list displayed on the window using the specified
 * color.  Colors are passed as strings.
 *
 * @param word The word to be added
 * @param color The color of the word
 */
	public void addWord(String word, Color color) {
		wordList.add(new StringColorPair(word, color));
		repaint();
	}

/**
 * Clears the message area at the bottom of the window.
 */
	public void clearMessage() {
		messageText = "";
		repaint();
	}

/**
 * Displays a message in the message area at the bottom of the window.
 *
 * @param msg The string to display
 */
	public void showMessage(String msg) {
		showMessage(msg, Color.BLACK);
	}

/**
 * Displays a message in the message area at the bottom of the window.
 * This version of the method allows the caller to set the text color.
 *
 * @param msg The string to display
 * @param color The color of the text
 */
	public void showMessage(String msg, Color color) {
		messageText 	= msg;
		messageColor 	= color;
		repaint();
	}
 
 // paintComponent is called by the run time (via repaint also)
	@Override
	public void paintComponent(Graphics g) {
		// draw a white rectangle the size of the JComponent
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight()); // height and width of the JComponent
		g.setColor(Color.BLACK);
		
		Color bg = CENTER_HEX_COLOR;
		
		int x0	= BEEHIVE_X;
		int y0	= BEEHIVE_Y;
		
		// draw center hexagon
		drawBeehiveHexagon(g, x0, y0, letters.substring(0, 1), bg);
		
		bg = OUTER_HEX_COLOR;
		
		// draw surrounding hexagons
		for (int k = 0; k < 6; k++) {
			double theta = Math.toRadians(30 + 60 * k);
			int x = x0 + (int) Math.round(HEX_SEP * Math.cos(theta));
			int y = y0 - (int) Math.round(HEX_SEP * Math.sin(theta));
			drawBeehiveHexagon(g, x, y, letters.substring(k + 1, k + 2), bg);
		}

		if (messageText.length() > 0) {
			g.setColor(messageColor);
			g.setFont(Font.decode(MESSAGE_FONT));
			int y = CANVAS_HEIGHT - MESSAGE_MARGIN;
			g.drawString(messageText, WORDLIST_X, y);
		}
		
		drawWordList(g);
	}

/* Private methods */
	private void drawBeehiveHexagon(Graphics g, int x0, int y0, String letter, Color color) {
		Polygon hexagon = new Polygon();

		// draw six sided polygon
		for (int i = 0; i < 6; i++) {
			double theta = Math.toRadians(180 - i * 60);				// specify angle in radians
			int x = x0 + (int) Math.round(HEX_SIDE * Math.cos(theta));
			int y = y0 - (int) Math.round(HEX_SIDE * Math.sin(theta));
			hexagon.addPoint(x, y);
		}
		
		g.setColor(color);
		g.fillPolygon(hexagon);
		g.setColor(Color.BLACK);
		g.drawPolygon(hexagon);
		g.setFont(Font.decode(LETTER_FONT));
		
		FontMetrics fm = g.getFontMetrics();
		
		int x = x0 - fm.stringWidth(letter) / 2;
		int y = y0 + (int) Math.round(fm.getAscent() * ASCENT_FRACTION);
		
		g.drawString(letter, x, y);
	}

	private void drawWordList(Graphics g) {
		int x = WORDLIST_X;
		int y = WORDLIST_Y;
		
		g.setFont(Font.decode(WORDLIST_FONT));
		
		for (StringColorPair pair : wordList) {
			g.setColor(pair.getColor());
			g.drawString(pair.getString(), x, y);
			y += WORDLIST_DY;
			if (y > WORDLIST_Y + WORDLIST_HEIGHT) {
				x += WORDLIST_DX;
				y = WORDLIST_Y;
			}
		}
	}
}

/**
 * This class implements a pair of a string and a color.
 */
class StringColorPair {
	/* Private instance variables */
	private String str;
	private Color color;

	public StringColorPair(String str, Color color) {
		this.str	= str;
		this.color	= color;
	}

	public String getString() {
		return str;
	}

	public Color getColor() {
		return color;
	}
}

/*
 * Implementation notes: TextFieldListener
 * ---------------------------------------
 * This class converts the event generated by the text field into a
 * callback to the SpellingBeeEventListener.
 */
class TextFieldListener implements ActionListener {

	/* Private instance variables */
	private JTextField field;
	private SpellingBeeEventListener listener;

	public TextFieldListener(JTextField field, SpellingBeeEventListener listener) {
	  	this.field		= field;
	   	this.listener	= listener;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		listener.eventAction(this.field.getText());
	}
}

/*
 * Implementation notes: ButtonListener
 * ------------------------------------
 * This class converts the event generated by the JButton into a
 * callback to the SpellingBeeEventListener.
 */
class ButtonListener implements ActionListener {

	/* Private instance variables */

	private JButton button;
	private SpellingBeeEventListener listener;

	public ButtonListener(JButton button, SpellingBeeEventListener listener) {
		this.button 	= button;
		this.listener 	= listener;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		listener.eventAction(this.button.getText());
	}
}
