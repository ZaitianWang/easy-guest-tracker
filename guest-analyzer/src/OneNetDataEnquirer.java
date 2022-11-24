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
    public void exportEnquiryResult(Map<String, Integer> tempMap, Map<String, Integer> humMap) {
        // generate file name
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss-SSS");
        // create files
        String path = "data/";
        File tempFile = new File(path + format.format(date) + "_temp.json");
        File humFile = new File(path + format.format(date) + "_hum.json");
        try {
            if (tempFile.createNewFile() && humFile.createNewFile()) {
                System.out.println("data saved to disk");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // write temp file
        try {
            BufferedWriter tempBw = new BufferedWriter(new FileWriter(tempFile));
            tempBw.write("{\n\t\"temperature\": [\n");
            for (Map.Entry<String, Integer> entry : tempMap.entrySet()) {
                tempBw.write("\t\t{\n\t\t\t\"time\": \"" +
                        entry.getKey() + "\",\n" +
                        "\t\t\t\"value\": "+
                        entry.getValue() + "\n\t},\n");
            }
            tempBw.write("]\n}");
            tempBw.flush();
            tempBw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // write hum file
        try {
            BufferedWriter humBw = new BufferedWriter(new FileWriter(humFile));
            humBw.write("{\n\t\"humidity\": [\n");
            for (Map.Entry<String, Integer> entry : humMap.entrySet()) {
                humBw.write("\t\t{\n\t\t\t\"time\": \"" +
                        entry.getKey() + "\",\n" +
                        "\t\t\t\"value\": "+
                        entry.getValue() + "\n\t\t},\n");
            }
            humBw.write("]\n}");
            humBw.flush();
            humBw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
