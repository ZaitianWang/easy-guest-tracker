import com.alibaba.fastjson.JSONArray;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class GuestAnalyzerUi extends JFrame {
    public static final int WIDTH = 1080;
    public static final int HEIGHT = 720;
    private JSONArray rfid = new JSONArray();
    private JSONArray temp = new JSONArray();
    private final OneNetDataEnquirer enquirer = new OneNetDataEnquirer();
    // top
    private final JLabel titleLabel = new JLabel("Guest Track Monitor", SwingConstants.CENTER);
    private final JPanel operationPanel = new JPanel();
    private final JLabel locationLabel = new JLabel("Location:");
    private final JTextField locationText = new JTextField("35379719");
    private final JButton connectButton = new JButton("Connect");
    private final JButton searchButton = new JButton("Search");
    private final JButton clearButton = new JButton("Clear");
    private final JButton reportButton = new JButton("Report");
    // left
    // right
    private final JPanel dataPanel = new JPanel();
    private final JScrollPane anaDataPanel = new JScrollPane();
    private final JTable anaTable = new JTable();
    private final DefaultTableModel anaTableModel = new DefaultTableModel(
            new Object[][]{
                    {"Data", null, null, null, null},
                    {null, null, null, null, null},
            },
            new String[]{
                    " ", "Max Temp", "People Count", "People Density", ""
            }
    );
    private final JScrollPane rfidDataPanel = new JScrollPane();
    private final JScrollPane tempDataPanel = new JScrollPane();
    private final JTable rfidTable = new JTable();
    private final JTable tempTable = new JTable();
    private final DefaultTableModel rfidDataTableModel = new DefaultTableModel(
            new Object[][]{
            },
            new String[]{
                    "Time", "RFID"
            });
    private final DefaultTableModel tempDataTableModel = new DefaultTableModel(
            new Object[][]{
            },
            new String[]{
                    "Time", "Temp"
            });


    public GuestAnalyzerUi() {
        initFrame();
        initComponents();
        addActionListeners();
    }

    private void initFrame() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        Point p = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getCenterPoint();
        setBounds(p.x - WIDTH / 2, p.y - HEIGHT / 2, WIDTH, HEIGHT);
        this.setLayout(null);
        setTitle("Easy Guest Tracker");
    }

    private void initComponents() {
        titleLabel.setBounds(300, 0, 480, 100);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(titleLabel);

        operationPanel.setBounds(0, 100, 1080, 50);
        operationPanel.setLayout(null);
        operationPanel.setBorder(new LineBorder(Color.BLACK));

        locationLabel.setBounds(20,10,60,30);
        locationText.setBounds(80,10,120,30);
        connectButton.setBounds(200,10,100,30);
        operationPanel.add(locationLabel);
        operationPanel.add(locationText);
        operationPanel.add(connectButton);

        searchButton.setBounds(750, 10, 80, 30);
        clearButton.setBounds(850, 10, 80, 30);
        reportButton.setBounds(950, 10, 80, 30);
        operationPanel.add(searchButton);
        operationPanel.add(clearButton);
        operationPanel.add(reportButton);

        add(operationPanel);

        dataPanel.setBounds(600, 150, 480, 500);
        dataPanel.setLayout(null);
        dataPanel.setBorder(new LineBorder(Color.BLACK));

        anaDataPanel.setBounds(10, 10, 450, 60);
        anaTable.setModel(anaTableModel);
        anaTable.setEnabled(false);
        anaTable.getTableHeader().setReorderingAllowed(false);
        anaDataPanel.setViewportView(anaTable);
        dataPanel.add(anaDataPanel);

        rfidDataPanel.setBounds(10, 80, 220, 400);
        rfidTable.setModel(rfidDataTableModel);
        rfidTable.getColumnModel().getColumn(0).setPreferredWidth(208);
        rfidTable.getTableHeader().setReorderingAllowed(false);
        rfidDataPanel.setViewportView(rfidTable);
        dataPanel.add(rfidDataPanel);

        tempDataPanel.setBounds(240, 80, 220, 400);
        tempTable.setModel(tempDataTableModel);
        tempTable.getColumnModel().getColumn(0).setPreferredWidth(208);
        tempTable.getTableHeader().setReorderingAllowed(false);
        tempDataPanel.setViewportView(tempTable);
        dataPanel.add(tempDataPanel);

        add(dataPanel);
    }

    private void addActionListeners() {
        connectButton.addActionListener(e -> connect());
        searchButton.addActionListener(e -> {
            clear();
            display();
        });

        clearButton.addActionListener(e -> clear());

        reportButton.addActionListener(e -> save());
    }

    private void connect() {
        enquirer.setDeviceId(locationText.getText());
    }
    private void display() {
        JSONArray data = enquirer.enquiry(null, null, "temp1,hex");
        JSONArray curData = enquirer.enquiry(null, null, "temp1,hex");
        rfid = data.getJSONObject(0).getJSONArray("datapoints");
        temp = data.getJSONObject(1).getJSONArray("datapoints");
        JSONArray curRfid = curData.getJSONObject(0).getJSONArray("datapoints");
        JSONArray curTemp = curData.getJSONObject(1).getJSONArray("datapoints");
        int maxTemp = -999999, peopleCount = 0;


        Pattern pattern = Pattern.compile("\\d*");
        for (int i = 0; i < rfid.size(); i++) {
            rfidDataTableModel.addRow(new String[]{rfid.getJSONObject(i).get("at").toString(), rfid.getJSONObject(i).get("value").toString()});
            peopleCount++;
        }
        for (int i = 0; i < temp.size(); i++) {
            if (!pattern.matcher( temp.getJSONObject(i).get("value").toString()).matches()) {
                continue;
            }
            tempDataTableModel.addRow(new String[]{temp.getJSONObject(i).get("at").toString(), temp.getJSONObject(i).get("value").toString()});
            int j = Integer.parseInt( temp.getJSONObject(i).get("value").toString());
            if (j > maxTemp && j <46.5)
                maxTemp = j;
        }
        anaTableModel.setValueAt(maxTemp, 0, 1);
        anaTableModel.setValueAt(peopleCount, 0, 2);
        anaTableModel.setValueAt(peopleCount/100, 0, 3);
    }

    private void clear() {
        rfidDataTableModel.setRowCount(0);
        tempDataTableModel.setRowCount(0);
        for (int i = 0; i < 2; i++) {
            for (int j = 1; j <= 4; j++) {
                anaTableModel.setValueAt(null, i, j);
            }
        }
    }

    private void save() {
        if (temp.size() == 0 && rfid.size() == 0)
            return;
        Map<String, Integer> tempMap = new LinkedHashMap<>(), rfidMap = new LinkedHashMap<>();
        Pattern pattern = Pattern.compile("\\d*");
        for (int i = 0; i < temp.size(); i++) {
            if (!pattern.matcher( temp.getJSONObject(i).get("value").toString()).matches()) {
                continue;
            }
            tempMap.put( temp.getJSONObject(i).get("at").toString(), Integer.parseInt( temp.getJSONObject(i).get("value").toString()));
        }
        for (int i = 0; i < rfid.size(); i++) {
            if (!pattern.matcher( rfid.getJSONObject(i).get("value").toString()).matches()) {
                continue;
            }
            rfidMap.put( rfid.getJSONObject(i).get("at").toString(), Integer.parseInt( rfid.getJSONObject(i).get("value").toString()));
        }
        enquirer.exportEnquiryResult(tempMap, rfidMap);
    }
}
