package battleships;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class BattleshipsApp {

    static String mode;
    static String port;
    static String address;
    static String mapPath;
    static BattleshipsMap map;

    public static void main(String[] args) throws IOException {
        readArguments(args);
        map = readMap(mapPath);
        map.display();
        if (Objects.equals(mode, "client")) {
            BattleshipsClient client = new BattleshipsClient(address, Integer.parseInt(port), map);
        } else if (Objects.equals(mode, "server")) {
            InetAddress addr = SrvUtil.findAddress();
            BattleshipsServer Server = new BattleshipsServer(addr, Integer.parseInt(port), map);
            new Thread(Server, "BattleshipsServer").start();
        }
    }

    static BattleshipsMap readMap(String path) {
        try {
            var inputPath = Paths.get(System.getProperty("user.dir") + "\\" + path);
            var map = Files.readString(inputPath);
            return new BattleshipsMap(map);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new BattleshipsMap(".........#.#..#........##..##................#...#.....#.....###...#.......###...#.........#...#....");
    }

    static void readArguments(String[] args) {
        for (int i = 0; i < args.length; i += 2) {
            switch (args[i]) {
                case ("-mode") -> mode = args[i + 1];
                case ("-port") -> port = args[i + 1];
                case ("-address") -> address = args[i + 1];
                case ("-map") -> mapPath = args[i + 1];
                default -> throw new IllegalArgumentException();
            }
        }
        if (mode == null || port == null || (address == null && mode.equals("client")) || mapPath == null)
            throw new IllegalArgumentException();
    }
}
