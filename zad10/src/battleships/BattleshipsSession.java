package battleships;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class BattleshipsSession implements Runnable {

    static public BattleshipsMap myMap;
    static public BattleshipsMap enemyMap;
    private final Socket socket;
    private final BattleshipsProtocol mode;
    private final BufferedWriter out;
    private final BufferedReader in;
    Random rnd = new Random();
    Coordinates lastShot;
    String responseToSend = "start";
    boolean gameInProgress = true;
    String gameOutcome = "";
    int maxErrors = 3;
    int errorCount = maxErrors;
    String lastFullMessage;
    private int counter;

    public BattleshipsSession(Socket socket, BattleshipsProtocol mode, BattleshipsMap map) throws IOException {

        this.socket = socket;
        this.mode = mode;
        myMap = map;
        enemyMap = new BattleshipsMap();
        out = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8));
        in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
    }

    String generateMessage() {
        var response = generateResponse();
        if (response.equals("ostatni zatopiony")) {
            gameInProgress = false;
            gameOutcome = "Przegrana\n";
            return response;
        } else {
            return response + ';' + generateShot();
        }
    }

    String generateResponse() {
        return responseToSend;
    }

    String generateShot() {
        Coordinates shot = null;

        var foundShip = enemyMap.ships.stream().filter(Ship::isAlive).findAny();
        if (foundShip.isPresent()) {
            var potentialShot = foundShip.get().closeNeighbours.stream().filter(x -> enemyMap.read(x) == '?').findAny();
            if (potentialShot.isPresent()) {
                shot = potentialShot.get();
            }
        }

        if (shot == null) {
            do {
                shot = new Coordinates(rnd.nextInt(enemyMap.size), rnd.nextInt(enemyMap.size));
            } while (enemyMap.read(shot) != '?');
        }
        lastShot = shot;
        return shot.WriteCoordinates();
    }

    void readMessage(String message) {
        if (message.equals("ostatni zatopiony")) {
            readResponse(message);
            gameInProgress = false;
            gameOutcome = "Wygrana\n";
        } else {
            var divider = message.indexOf(';');
            readResponse(message.substring(0, divider));
            readShot(new Coordinates(message.substring(divider + 1)));
        }
    }

    void readResponse(String response) {
        switch (response) {
            case "start":
                break;
            case "pud\u0142o":
                enemyMap.reveal(lastShot, '.');
                break;
            case "trafiony":
                enemyMap.reveal(lastShot, '#');
                break;
            case "trafiony zatopiony":
            case "ostatni zatopiony":
                enemyMap.reveal(lastShot, '#');
                var shotShip = enemyMap.getShipAt(lastShot);
                if (shotShip != null) {
                    for (var component : shotShip.allComponents) {
                        shotShip.damage(component);
                    }
                    for (var n : shotShip.farNeighbours) {
                        enemyMap.reveal(n, '.');
                    }
                }
                break;

        }
    }

    boolean readShot(Coordinates shot) {
        if (shot.x() >= enemyMap.size || shot.y() >= enemyMap.size || shot.x() < 0 || shot.y() < 0)
            return false;
        switch (myMap.read(shot)) {
            case '.':
                myMap.reveal(shot, '~');
            case '~':
                responseToSend = "pud\u0142o";
                break;
            case '#':
                myMap.reveal(shot, '@');
            case '@':
                var hitShip = myMap.getShipAt(shot);

                if (hitShip.isAlive()) {
                    responseToSend = "trafiony";
                } else {
                    if (myMap.isAnyShipAlive())
                        responseToSend = "trafiony zatopiony";
                    else {
                        responseToSend = "ostatni zatopiony";
                    }

                }
                break;
        }
        return true;
    }

    boolean valideMeassage(String message) {
        if (message.equals("ostatni zatopiony")) {
            return true;
        } else {
            var sucess = true;
            var divider = message.indexOf(';');
            if (divider == -1)
                return false;
            var firstPart = message.substring(0, divider);
            return firstPart.equals("start") || firstPart.equals("pud\u0142o") || firstPart.equals("trafiony") || firstPart.equals("trafiony zatopiony");
        }
    }

    public void run() {
        try {
            if (mode == BattleshipsProtocol.CLIENT) {
                send(generateMessage());
            }
            while (gameInProgress) {

                read();

                if (gameInProgress || gameOutcome.equals("Przegrana\n"))
                    send(generateMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.print(gameOutcome);
        enemyMap.display();
        System.out.print('\n');
        myMap.display();
    }

    private void read() throws IOException {
        counter++;
        boolean messageRecived = false;
        do {
            try {
                String inputLine = in.readLine();
                System.out.println("[" + Thread.currentThread().getName() + "] [" + counter + "] received " + inputLine);
                messageRecived = valideMeassage(inputLine);
                //System.out.println(messageRecived);
                if (messageRecived)
                    readMessage(inputLine);
                else
                    registerError();
            } catch (java.net.SocketTimeoutException e) {
                registerError();
            }
        } while (!messageRecived);

    }

    void registerError() throws IOException {
        errorCount++;
        if (errorCount >= maxErrors) {
            System.out.println("Błąd komunikacji");
            System.exit(-1);
        } else {
            send(lastFullMessage);
        }
    }


    private void send(String toSend) throws IOException {
        counter++;
        System.out.println("[" + Thread.currentThread().getName() + "] [" + counter + "] sending " + toSend);
        out.write(toSend);
        out.newLine();
        out.flush();
        lastFullMessage = toSend;
    }
}