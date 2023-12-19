package com.example.qrdolgozat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RequestHandler {
        private RequestHandler(){}
        public static Response get(String url)
                throws IOException {
            HttpURLConnection connection =setupConnction(url);
            return getResponse(connection);
        }
        public static Response post(String url,String data)
                throws IOException{
            HttpURLConnection connection =setupConnction(url);
            connection.setRequestMethod("POST");
            addRequestBody(connection,data);
            return getResponse(connection);
        }

        public static Response put(String url,String data)
                throws IOException{
            HttpURLConnection connection =setupConnction(url);
            connection.setRequestMethod("PUT");
            addRequestBody(connection,data);
            return getResponse(connection);
        }


        private static void addRequestBody(HttpURLConnection connection,String data)
                throws IOException{
            connection.setRequestProperty("Content-Type","application/json");
            connection.setDoOutput(true);
            OutputStream outputStream=connection.getOutputStream();
            BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            writer.write(data);
            writer.flush();
            writer.close();
            outputStream.close();
        }

        private static HttpURLConnection setupConnction(String url)
                throws IOException{
            URL urlobj=new URL(url);
            HttpURLConnection connection=(HttpURLConnection) urlobj.openConnection();
            connection.setRequestProperty("Accept","application/json");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            return connection;
        }

        private static Response getResponse(HttpURLConnection connection)
                throws IOException{
            int responsecode=connection.getResponseCode();
            InputStream inputStream;
            if (responsecode<400) {
                inputStream=connection.getInputStream();
            }else {
                inputStream=connection.getErrorStream();
            }
            StringBuilder builder=new StringBuilder();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            String sor=bufferedReader.readLine();
            while (sor!=null){
                builder.append(sor);
                sor=bufferedReader.readLine();
            }
            bufferedReader.close();
            inputStream.close();
            return new Response(responsecode,builder.toString());

        }

}
