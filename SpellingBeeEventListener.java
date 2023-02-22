/*
 * File: SpellingBeeEventListener.java
 * -----------------------------------
 * This file defines an interface that is used to specify event
 * callbacks in the SpellingBee project.
 */

/**
 * This interface defines the signature for callback functions in the
 * SpellingBee assignments.  The easiest way to use this interface is to
 * use arrow functions, but it is also possible to import this interface
 * and then define an explicit listener method that implements it.
 */

public interface SpellingBeeEventListener {

/*
 * This function defines SpellingBeeEventListener as a functional
 * interface, suitable for use with arrow functions.
 */

    public void eventAction(String s);

}
