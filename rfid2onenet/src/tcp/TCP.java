package tcp;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

public class TCP {
    public static void main(String[] args) {
        new TCP().send(
                (Integer.toHexString(65536 + new Random().nextInt(65535)).substring(1,5)
                        + Integer.toHexString(65536 + new Random().nextInt(65535)).substring(1,5)).toUpperCase());
    }
    public TCP() {
    }
    public void send(String message) {
        try {
            try (Socket socket = new Socket(InetAddress.getByName("183.230.40.40"), 1811)) {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.write("*153149#06#hex*".getBytes());
                out.writeBytes(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
