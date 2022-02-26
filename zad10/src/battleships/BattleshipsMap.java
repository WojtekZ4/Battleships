package battleships;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class BattleshipsMap {
    char[][] map;
    Set<Ship> ships = new HashSet<>();

    int size = 10;

    BattleshipsMap() {
        map = new char[size][size];
        for (var x : map) {
            Arrays.fill(x, '?');
        }
    }

    BattleshipsMap(String toRead) {
        this();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                reveal(new Coordinates(x, y), toRead.charAt(y * size + x));
            }
        }
    }

    void display() {
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                System.out.print(read(new Coordinates(x, y)));
            }
            System.out.print('\n');
        }
    }

    char read(Coordinates where) {
        return map[where.y()][where.x()];
    }

    void write(Coordinates where, char what) {
        map[where.y()][where.x()] = what;
    }

    Ship getShipAt(Coordinates where) {
        var out = ships.stream().filter(x -> x.allComponents.contains(where)).findAny();
        return out.orElse(null);
    }

    boolean isAnyShipAlive() {
        for (var s : ships) {
            if (s.isAlive())
                return true;
        }
        return false;
    }

    Set<Coordinates> getCloseNeighbors(Coordinates checked) {
        Set<Coordinates> out = new HashSet<>();

        if (checked.x() < 0 || checked.x() >= size || checked.y() < 0 || checked.y() >= size)
            return out;

        if (checked.y() > 0)
            out.add(checked.up());
        if (checked.y() < size - 1)
            out.add(checked.down());
        if (checked.x() > 0)
            out.add(checked.left());
        if (checked.x() < size - 1)
            out.add(checked.right());

        return out;
    }

    Set<Coordinates> getFarNeighbors(Coordinates checked) {
        Set<Coordinates> out = new HashSet<>();

        if (checked.x() < 0 || checked.x() >= size || checked.y() < 0 || checked.y() >= size)
            return out;

        out.addAll(getCloseNeighbors(checked));

        if (checked.y() > 0 && checked.x() > 0)
            out.add(checked.up().left());
        if (checked.y() < size - 1 && checked.x() > 0)
            out.add(checked.down().left());
        if (checked.y() > 0 && checked.x() < size - 1)
            out.add(checked.up().right());
        if (checked.y() < size - 1 && checked.x() < size - 1)
            out.add(checked.down().right());

        return out;
    }

    void reveal(Coordinates where, char what) {
        write(where, what);
        if (what == '#') {

            var adjacentShips = ships.stream().filter(x -> x.closeNeighbours.contains(where)).collect(Collectors.toSet());
            var temp = new Ship(this, where);
            ships.add(temp);
            for (var s : adjacentShips) {
                temp.mergeShip(s);
                ships.remove(s);
            }
        } else if (what == '@') {
            var hereShip = ships.stream().filter(x -> x.allComponents.contains(where)).findAny();
            hereShip.ifPresent(ship -> ship.damage(where));
        }
    }
}
