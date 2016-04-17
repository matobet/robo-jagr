package cz.muni.fi.rtsystems.robojagr.enums;

public enum Color {
    WHITE, NOTWHITE;
    
    public static Color getColor(int number) {
        if (number == -1) {
            return Color.WHITE;
        }
        return NOTWHITE;
    }
}
