import com.alibaba.fastjson.JSONArray;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class GuestAnalyzerUi extends JFrame {
    public static final int WIDTH = 1080;
    public static final int HEIGHT = 720;
    private final HashMap<String, Integer> map = new HashMap<>(); // rfid-temp map
    private final ArrayList<String> list = new ArrayList<>(); // cur rfid list
    private final OneNetDataEnquirer enquirer = new OneNetDataEnquirer();
    private final JLabel titleLabel = new JLabel("Guest Track Monitor", SwingConstants.CENTER);
    private final JPanel operationPanel = new JPanel();
    private final JLabel locationLabel = new JLabel("Location:");
    private final JTextField locationText = new JTextField("35379719");
    private final JButton connectButton = new JButton("Connect");
    private final JButton refreshButton = new JButton("Refresh");
    private final JButton clearButton = new JButton("Clear");
    private final JButton reportButton = new JButton("Report");
    private final JPanel dataPanel = new JPanel();
    private final JScrollPane anaDataPanel = new JScrollPane();
    private final JTable anaTable = new JTable();
    private final JScrollPane visitorDataPanel = new JScrollPane();
    private final JTable visitorTable = new JTable();
    private final DefaultTableModel anaTableModel = new DefaultTableModel(
            new Object[][]{
                    {null, null, null},
            },
            new String[]{
                    "Max Temperature", "Visitor Count", "Traffic Density"
            }
    );
    private final DefaultTableModel visitorDataTableModel = new DefaultTableModel(
            new Object[][]{
            },
            new String[]{
                    "Visitor Name", "Registration Code", "Temperature"
            }
    );
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

        locationLabel.setBounds(120,10,60,30);
        locationText.setBounds(180,10,120,30);
        connectButton.setBounds(300,10,100,30);
        operationPanel.add(locationLabel);
        operationPanel.add(locationText);
        operationPanel.add(connectButton);

        refreshButton.setBounds(650, 10, 80, 30);
        clearButton.setBounds(750, 10, 80, 30);
        reportButton.setBounds(850, 10, 80, 30);
        operationPanel.add(refreshButton);
        operationPanel.add(clearButton);
        operationPanel.add(reportButton);

        add(operationPanel);

        dataPanel.setBounds(40, 150, 1000, 500);
        dataPanel.setLayout(null);
        dataPanel.setBorder(new LineBorder(Color.BLACK));

        anaDataPanel.setBounds(140, 10, 720, 60);
        anaTable.setModel(anaTableModel);
        anaTable.setEnabled(false);
        anaTable.getTableHeader().setReorderingAllowed(false);
        anaDataPanel.setViewportView(anaTable);
        dataPanel.add(anaDataPanel);

        visitorDataPanel.setBounds(140, 80, 720, 400);
        visitorTable.setModel(visitorDataTableModel);
        visitorTable.setEnabled(false);
        visitorTable.getTableHeader().setReorderingAllowed(false);
        visitorTable.setAlignmentX(CENTER_ALIGNMENT);
        visitorTable.setAlignmentY(CENTER_ALIGNMENT);
        visitorDataPanel.setViewportView(visitorTable);
        dataPanel.add(visitorDataPanel);

        add(dataPanel);
    }

    private void addActionListeners() {
        connectButton.addActionListener(e -> connect());
        refreshButton.addActionListener(e -> {
            clear();
            display();
        });

        clearButton.addActionListener(e -> clear());

        reportButton.addActionListener(e -> report());
    }

    private void connect() {
        enquirer.setDeviceId(locationText.getText());
    }
    private void display() {
        // get raw data
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        JSONArray data = enquirer.enquiry(dateFormat.format(now)+"T00:00:00",
                dateFormat.format(now)+"T23:59:59", "temp1,hex");
        JSONArray rfid = data.getJSONObject(0).getJSONArray("datapoints");
        JSONArray temp = data.getJSONObject(1).getJSONArray("datapoints");
        JSONArray curData = enquirer.enquiry(
                dateFormat.format(new Date(now.getTime()-300000))
                        +"T"+timeFormat.format(new Date(now.getTime()-300000)),
                dateFormat.format(now)+"T"+timeFormat.format(now),
                "temp1,hex"); // cur = 5 min earlier to now
        JSONArray curVisitor = new JSONArray();
        if (curData.size() > 0) {
            curVisitor = curData.getJSONObject(0).getJSONArray("datapoints");
        }
        // prepare data to be displayed
        int maxTemp = -273, peopleCountTotal = 0, peopleCountCurrent = 0;
        // assume one temp for each rfid
        for (int i = 0; i < rfid.size(); i++) {
            if (!Pattern.compile("\\d*").matcher( temp.getJSONObject(i).get("value").toString()).matches()) {
                // non-numeric value
                continue;
            }
            String r = rfid.getJSONObject(i).getString("value");
            int t = temp.getJSONObject(i).getInteger("value");
            if (!map.containsKey(r)) {
                // rfid first occurrence
                if (t > 30) {
                    peopleCountTotal++;
                    map.put(r, t);
                }
            } else {
                if (map.get(r) < t && t < 46.5) {
                    // a higher but valid temp
                    map.replace(r, t);
                }
            }
            if (t > maxTemp) {
                maxTemp = t;
            }
        }
        for (int i = 0; i < curVisitor.size(); i++) {
            String r = curVisitor.getJSONObject(i).getString("value");
            if (!list.contains(r)){
                peopleCountCurrent++;
                list.add(r);
            }
        }
        // put data to panel
        anaTableModel.setValueAt(maxTemp, 0, 0);
        anaTableModel.setValueAt(peopleCountTotal, 0, 1);
        anaTableModel.setValueAt(peopleCountCurrent, 0, 2);
        GuestInfo.update();
        map.forEach((r, t) -> visitorDataTableModel.addRow(new String[]{GuestInfo.query(r), r, t.toString()}));
    }

    private void clear() {
        visitorDataTableModel.setRowCount(0);
        for (int i = 0; i < anaTableModel.getRowCount(); i++) {
            for (int j = 0; j < anaTableModel.getColumnCount(); j++) {
                anaTableModel.setValueAt(null, i, j);
            }
        }
    }

    private void report() {
        enquirer.exportEnquiryResult(map);
    }
}
