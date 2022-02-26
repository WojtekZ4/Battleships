package battleships;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class BattleshipsServer implements Runnable {

    private final ServerSocket serverSocket;
    private final BattleshipsMap map;
    boolean killing = false;

    BattleshipsServer(InetAddress address, int port, BattleshipsMap map) throws IOException {

        this.map = map;
        serverSocket = new ServerSocket(port, 10000, address);
        System.out.println("Running BattleshipsServer at address: " + address + ", port: " + port);
    }

    @Override
    public void run() {
        int sessionId = 0;
        while (true) {
            try {

                Socket socket = serverSocket.accept();
                if (!killing) {
                    new Thread(SrvUtil::doKill, "[KILLER]").start();
                    killing = true;
                }
                System.out.println("Got request from " + socket.getRemoteSocketAddress() + ", starting session " + sessionId);
                BattleshipsSession session = new BattleshipsSession(socket, BattleshipsProtocol.SERVER, map);
                new Thread(session, "BattleshipsServer Session-" + sessionId).start();
                sessionId++;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
