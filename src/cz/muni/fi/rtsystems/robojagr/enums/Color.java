package cz.muni.fi.rtsystems.robojagr.enums;

public enum Color {
	NONE, BLACK, BLUE, GREEN, YELLOW, RED, WHITE, BROWN;
    
    public static Color getColor(int number) {
        return values()[number];
    }
}
