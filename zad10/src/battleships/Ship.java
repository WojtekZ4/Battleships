package battleships;

import java.util.HashSet;
import java.util.Set;

public class Ship {
    Set<Coordinates> allComponents = new HashSet<>();
    Set<Coordinates> undamagedComponents = new HashSet<>();

    Set<Coordinates> closeNeighbours = new HashSet<>();
    Set<Coordinates> farNeighbours = new HashSet<>();

    BattleshipsMap containingMap;

    Ship(BattleshipsMap containingMap, Coordinates init) {
        this.containingMap = containingMap;
        allComponents.add(init);
        undamagedComponents.add(init);

        updateNeighbors();
    }

    void updateNeighbors() {
        closeNeighbours = new HashSet<>();
        farNeighbours = new HashSet<>();
        for (var c : allComponents) {
            closeNeighbours.addAll(containingMap.getCloseNeighbors(c));
            farNeighbours.addAll(containingMap.getFarNeighbors(c));
        }
        closeNeighbours.removeAll(allComponents);
        farNeighbours.removeAll(allComponents);
    }

    Boolean isAlive() {
        return !undamagedComponents.isEmpty();
    }

    void damage(Coordinates toDamage) {
        undamagedComponents.remove(toDamage);
    }

    void addComponent(Coordinates toAdd, boolean damaged) {
        allComponents.add(toAdd);
        if (!damaged)
            undamagedComponents.add(toAdd);

        updateNeighbors();
    }

    void mergeShip(Ship toMerge) {
        allComponents.addAll(toMerge.allComponents);
        undamagedComponents.addAll(toMerge.undamagedComponents);

        updateNeighbors();
    }
}
