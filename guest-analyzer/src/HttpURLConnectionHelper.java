import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpURLConnectionHelper {

    public static String sendGetRequest(String url,Map<String, String> params) {

        HttpURLConnection con = null;

        BufferedReader buffer = null;
        StringBuffer resultBuffer = null;

        try {
            String entireUrlStr=getRqstUrl(url,params);
            URL entireUrl = new URL(entireUrlStr);
            //得到连接对象
            con = (HttpURLConnection) entireUrl.openConnection();
            //设置请求类型
            con.setRequestMethod("GET");
            //设置请求需要返回的数据类型和字符集类型
            con.setRequestProperty("Content-Type", "application/json;charset=GBK");
            con.setRequestProperty("api-key","zEslkuvPCzLnv2t7Wy=jXWZdsHY=");
            //允许写出
            con.setDoOutput(true);
            //允许读入
            con.setDoInput(true);
            //不使用缓存
            con.setUseCaches(false);
            //得到响应码
            int responseCode = con.getResponseCode();

            System.out.println(responseCode+" "+con.getResponseMessage());
            if(responseCode == HttpURLConnection.HTTP_OK){
                //得到响应流
                InputStream inputStream = con.getInputStream();
                //将响应流转换成字符串
                resultBuffer = new StringBuffer();
                String line;
                buffer = new BufferedReader(new InputStreamReader(inputStream, "GBK"));
                while ((line = buffer.readLine()) != null) {
                    resultBuffer.append(line);
                }
                return resultBuffer.toString();
            }

        }catch(Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 拼接get请求的url请求地址
     */
    public static String getRqstUrl(String url, Map<String, String> params) {
        StringBuilder builder = new StringBuilder(url);
        boolean isFirst = true;
        for (String key : params.keySet()) {
            if (key != null && params.get(key) != null) {
                if (isFirst) {
                    isFirst = false;
                    builder.append("?");
                } else {
                    builder.append("&");
                }
                builder.append(key)
                        .append("=")
                        .append(params.get(key));
            }
        }
        return builder.toString();
    }
//
//    public static void main(String[] args) {
//
//        //url中的35379643替换为自己组对应的设备ID(登录OneNet控制台查看）
//        String url ="http://api.heclouds.com/devices/35379643/datapoints";
//
//        //paras的名称和类型查看文档
//        Map<String, String> params = new HashMap<>();
//        params.put("start","2022-10-10T08:00:35");
//        params.put("end","2022-11-07T08:00:35");
//        String ret = sendGetRequest(url, params);
//        System.out.println(ret);
//    }
}

