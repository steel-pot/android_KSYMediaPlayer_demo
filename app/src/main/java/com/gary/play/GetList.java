package com.gary.play;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetList {
    public interface Event{
        void onGetList(JSONArray arr);
    }
    public void getList(final Event event)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONArray arr=getData();
                event.onGetList(arr);
            }
        }).start();


    }
    private JSONArray getData()
    {
        String urlStr=" ";
        try {
            URL url = new URL(urlStr); //URL对象
            HttpURLConnection conn = (HttpURLConnection)url.openConnection(); //使用URL打开一个链接,下面设置这个连接
            conn.setRequestMethod("GET"); //使用get请求

            if(conn.getResponseCode()==200){//返回200表示连接成功
                InputStream is = conn.getInputStream(); //获取输入流
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader bufferReader = new BufferedReader(isr);
                String inputLine  = "";
                String resultData="";
                while((inputLine = bufferReader.readLine()) != null){
                    resultData += inputLine ;
                }
                JSONArray jsonArray=new JSONArray(resultData);
                return jsonArray;
            }

        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
