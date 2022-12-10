import java.io.*;
import java.util.HashMap;

public class GuestInfo {
    private static final HashMap<String, String> regInfo = new HashMap<>();
    public static void update() {
        File regFile = new File("../registration/guest_registry.csv");
        try(BufferedReader br = new BufferedReader(new FileReader(regFile))) {
            String info = br.readLine();
            while (info != null) {
                String[] pair = info.split(",");
                regInfo.putIfAbsent(pair[0], pair[1]);
                info = br.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static String query(String rfid) {
        return regInfo.getOrDefault(rfid, "Name not found");
    }
}
