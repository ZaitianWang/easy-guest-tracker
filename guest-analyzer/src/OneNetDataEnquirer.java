import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OneNetDataEnquirer {

    private String deviceId = "";

    private final Map<String, String> params = new HashMap<>();
    public OneNetDataEnquirer() {
        int limit = 6000;
        params.put("limit", String.valueOf(limit));
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    public String getDeviceId() {
        return deviceId;
    }
    private String getUrl() {
        return "https://api.heclouds.com/devices/" + getDeviceId() + "/datapoints";
    }
    public JSONArray enquiry(String start, String end, String streamId) {
        System.out.println(getUrl());
        // create temp param map
        Map<String, String> tmpParams = new HashMap<>(params);
        if (start != null && !start.isEmpty())
            tmpParams.put("start", start);
        if (end != null && !end.isEmpty())
            tmpParams.put("end", end);
        if (streamId != null && !streamId.isEmpty())
            tmpParams.put("datastream_id", streamId);
        else
            tmpParams.put("datastream_id", "temp1,hum1");

        String ret = HttpURLConnectionHelper.sendGetRequest(getUrl(), tmpParams);

        // print original response body
        System.out.println(ret);

        JSONObject data = JSONObject.parseObject(ret).getJSONObject("data");
        // get data number
        int count = data.getIntValue("count");
        System.out.println("total count: " + count);
        // get stream array
        JSONArray datastreams = data.getJSONArray("datastreams");
        System.out.println("stream count: " + datastreams.size());
        return datastreams;
    }
    public void exportEnquiryResult(HashMap<String, Integer> map) {
        // create file
        File dir = new File("../report");
        if (!dir.exists() && dir.mkdir()) {
            System.out.println("visitor report directory created");
        }
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss-SSS");
        File reportFile = new File("../report/visitor_report_"+format.format(date)+".csv");
        try {
            if (!reportFile.exists() && reportFile.createNewFile()) {
                System.out.println("visitor report file created");
                BufferedWriter bw = new BufferedWriter(new FileWriter(reportFile));

                bw.flush();
                bw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // write report file
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(reportFile));
            bw.write("Name,RFID,Temperature\n");
            map.forEach((r, t) -> {
                try {
                    bw.write(GuestInfo.query(r)+","+r+","+t.toString()+"\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            bw.flush();
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
