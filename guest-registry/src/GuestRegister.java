import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.sun.xml.internal.ws.api.ha.StickyFeature;
import uhf.linkage.Linkage;
import uhf.structures.RwData;
import uhf.utils.StringUtils;

public class GuestRegister {
    public void launch() {
        new RegistryUi().setVisible(true);
    }
    private class RegistryUi extends JFrame {
        JLabel title = new JLabel("Guest Registry", SwingConstants.CENTER);
        JLabel nameLabel = new JLabel("Guest Name: ");
        JTextField nameTextField = new JTextField("");
        JButton registerButton = new JButton("Register");
        JTextArea guestInfoTextArea = new JTextArea("");
        JScrollPane guestInfoPane = new JScrollPane();
        public RegistryUi() {
            setBounds(200, 100, 540, 480);
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setLayout(null);
            title.setBounds(20, 20, 500, 40);
            title.setFont(new Font("Arial", Font.PLAIN, 20));
            add(title);
            nameLabel.setBounds(40, 80, 100, 40);
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            nameTextField.setBounds(140, 80, 200, 40);
            nameTextField.setFont(new Font("Arial", Font.PLAIN, 16));
            registerButton.setBounds(380, 80, 100, 40);
            registerButton.setFont(new Font("Arial", Font.PLAIN, 16));
            add(nameLabel);
            add(nameTextField);
            add(registerButton);
            guestInfoTextArea.setFont(new Font("Consolas", Font.PLAIN, 16));
            guestInfoTextArea.setEditable(false);
            guestInfoPane.setBounds(40, 160, 450, 200);
            guestInfoPane.setViewportView(guestInfoTextArea);
            add(guestInfoPane);
            registerButton.addActionListener(e -> {
                String user = (Integer.toHexString(65536 + new Random().nextInt(65535)).substring(1,5)
                        + Integer.toHexString(65536 + new Random().nextInt(65535)).substring(1,5)).toUpperCase();
                Map<String, String> registration = new HashMap<>();
                registration.put(user, nameTextField.getText());
                guestInfoTextArea.append(user + ", " + nameTextField.getText() + "\n");
                nameTextField.setText("");
                register(registration);
            });
        }
    }
    @SuppressWarnings("static-access")
    private void register(Map<String, String> registration) {
        // create files
        File dir = new File("registration");
        if (!dir.exists() && dir.mkdir()) {
            System.out.println("guest registry directory created");
        }
        File regFile = new File("registration/guest_registry.csv");
        try {
            if (!regFile.exists() && regFile.createNewFile()) {
                System.out.println("guest registry file created");
                BufferedWriter bw = new BufferedWriter(new FileWriter(regFile));
                bw.write("RFID,Name\n");
                bw.flush();
                bw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // write file
        try {
            FileOutputStream outputStream = new FileOutputStream(regFile, true);
            for (Map.Entry<String, String> entry : registration.entrySet()) {
                outputStream.write((entry.getKey() + "," + entry.getValue() + "\n").getBytes());
                // write rfid
                int i;
                do {
                    i = Linkage.getInstance().initial("COM3");
                } while (i!=0);
                userWriteSync(entry.getKey());
            }
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void userWriteSync(String newUser) {
        userWriteSync(0, 2,newUser);
    }
    private static void userWriteSync(int start, int length, String newUser) {
        byte[] password = StringUtils.stringToByte("00000000");
        byte[] writeData = StringUtils.stringToByte(newUser);
        RwData rwData = new RwData();
        int status;
        do {
            status = Linkage.getInstance().writeTagSync(password, 3, start, length, writeData, 500, rwData);
        } while (status != 0);
        if (rwData.status == 0) {
            System.out.println("new user: " + newUser);
            System.out.println("user write success\n");
        } else {
            System.out.println("user write failed");
        }
    }
    public static void main(String[] args) {
        new GuestRegister().launch();
    }
}
