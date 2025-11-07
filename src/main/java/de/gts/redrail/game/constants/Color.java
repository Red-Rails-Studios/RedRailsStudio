package de.gts.redrail.game.constants;

public interface Color {
    String NONE = "000000";  // Default color for unassigned
    String RED = "FF0000";   // Player 1
    String GREEN = "00FF00"; // Player 2
    String BLUE = "0000FF";  // Player 3
    String YELLOW = "FFFF00"; // Player 4

    static boolean isValidColor(String color) {
        if (color == null) return false;
        return color.equals(RED) || 
               color.equals(GREEN) || 
               color.equals(BLUE) || 
               color.equals(YELLOW);
    }
}
