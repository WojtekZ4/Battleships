package battleships;

import java.net.*;

public class SrvUtil {
    static InetAddress findAddress() throws SocketException, UnknownHostException {
        var en0 = NetworkInterface.getByName("wlan0");
        return en0.inetAddresses()
                .filter(a -> a instanceof Inet4Address)
                .findFirst()
                .orElse(InetAddress.getLocalHost());
    }

    static void doKill() {
        for (int i = 0; i < 10; i++) {
            System.out.println("COUNTING TO DIE: " + i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }
}
