package battleships;

public record Coordinates(int x, int y) {
    Coordinates(String toRead) {
        this(toRead.charAt(0) - 'A', Integer.parseInt(toRead.substring(1)) - 1);
    }

    String WriteCoordinates() {
        return "" + ((char) (x + 'A')) + ((y + 1));
    }

    Coordinates up() {
        return new Coordinates(x, y - 1);
    }

    Coordinates down() {
        return new Coordinates(x, y + 1);
    }

    Coordinates left() {
        return new Coordinates(x - 1, y);
    }

    Coordinates right() {
        return new Coordinates(x + 1, y);
    }
}
