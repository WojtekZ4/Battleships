package battleships;

import java.io.IOException;
import java.net.Socket;

public class BattleshipsClient {

    BattleshipsClient(String host, int port, BattleshipsMap map) throws IOException {
        Socket s = new Socket(host, port);
        s.setSoTimeout(1 * 1000);
        BattleshipsSession session = new BattleshipsSession(s, BattleshipsProtocol.CLIENT, map);
        new Thread(session, "BattleshipsClient").start();
    }

}
