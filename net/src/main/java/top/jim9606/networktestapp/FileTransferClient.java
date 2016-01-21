package top.jim9606.networktestapp;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by jim on 2015/12/3 0003.
 */

public class FileTransferClient {
    public static final String encoding = "UTF-8";
    public static final String LogTag = "FileTransferClient";

    public static final int progressMsg_update = 1;
    public static final int progressMsg_download = 2;
    public static final int finishMsg_update = 3;
    public static final int finishMsg_download = 4;
    public static final int finishMsg_register = 5;
    public static final int errorMsg = 6;

    private static final String httpMethod = "POST";
    private static final String webAppLocation = "http://jim9606a.sinaapp.com/android_memo";
    private int bufferSize = 256;
    private int chunkSize = 8;
    private int sleepInt = 25;
    private static final String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";

    private String user,password;
    private Handler handler;
    public FileTransferClient(CharSequence user,CharSequence password,Handler handler) {
        this.user = user.toString();
        this.password = password.toString();
        this.handler = handler;
    }

    public int register() {
        HttpURLConnection conn;
        int code = 0;
        try {
            conn = initConnection(webAppLocation + "/register.php");
            code = conn.getResponseCode();
            Message msg = new Message();
            msg.arg1 = conn.getResponseCode();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
                msg.what = finishMsg_register;
            else
                msg.what = errorMsg;
            msg.obj = conn.getResponseMessage();
            handler.sendMessage(msg);
            conn.disconnect();
        }
        catch (Exception e) {
            e.printStackTrace();
            Message msg = new Message();
            msg.what = errorMsg;
            msg.obj = e;
            handler.sendMessage(msg);
        }
        finally {
            return code;
        }
    }

    public int update(URI filePath) {
        HttpURLConnection conn;
        int code = 0;
        try {
            int check = checkAuth();
            if (check != HttpURLConnection.HTTP_OK) {
                Message msg = new Message();
                msg.what = errorMsg;
                msg.arg1 = check;
                handler.sendMessage(msg);
                return check;
            }

            conn = initConnection(webAppLocation + "/update.php");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            OutputStream outputStream = new DataOutputStream(conn.getOutputStream());

            StringBuilder sb = new StringBuilder();
            File file = new File(filePath);
            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition:form-data;name=data;filename=\"" + file.getName() + "\"\r\n");
            sb.append("Content-Type:application/octet-stream\r\n\r\n");
            outputStream.write(sb.toString().getBytes());

            FileInputStream fileInputStream = new FileInputStream(file);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            int ptr = 0,size = 0,total = fileInputStream.available();
            byte[] buffer = new byte[bufferSize];
            while ((ptr = dataInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, ptr);
                size += ptr;
                Message msg = new Message();
                msg.what = progressMsg_update;
                msg.arg1 = size;
                msg.arg2 = total;
                handler.sendMessage(msg);
                Thread.sleep(sleepInt);
            }
            //outputStream.write("\r\n".getBytes());
            dataInputStream.close();

            outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes());
            outputStream.flush();
            outputStream.close();

            Message msg = new Message();
            code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK)
                msg.what = finishMsg_update;
            else
                msg.what = errorMsg;
            msg.arg1 = conn.getResponseCode();
            msg.obj = conn.getResponseMessage();
            handler.sendMessage(msg);
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
                Log.i(LogTag, receiveText(conn).toString());
            conn.disconnect();
        }
        catch (Exception e) {
            e.printStackTrace();
            Message msg = new Message();
            msg.what = errorMsg;
            msg.obj = e;
            handler.sendMessage(msg);
        }
        finally {
            return code;
        }
    }

    public int download(URI destPath) {
        HttpURLConnection conn;
        int code = 0;
        try {
            conn = initConnection(webAppLocation + "/download.php");
            conn.setDoOutput(true);

            code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                Message msg = new Message();
                msg.what = errorMsg;
                msg.arg1 = conn.getResponseCode();
                msg.obj = conn.getResponseMessage();
                handler.sendMessage(msg);
                return code;
            }
            InputStream inputStream = new BufferedInputStream(conn.getInputStream());
            FileOutputStream fileOutputStream = new FileOutputStream(destPath.getPath());

            File file = new File(destPath);
            if(!file.exists()) {
                if(!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }

            byte[] buffer = new byte[bufferSize];
            int ptr = 0 , size = 0 , total = Integer.parseInt(conn.getHeaderField("Original-Content-Length"));
            Log.i(LogTag, "Original-Content-Length:" + conn.getHeaderField("Original-Content-Length"));
            while ((ptr = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, ptr);
                size += ptr;
                Message msg = new Message();
                msg.what = progressMsg_download;
                msg.arg1 = size;
                msg.arg2 = total;
                handler.sendMessage(msg);
                Thread.sleep(sleepInt);
            }
            fileOutputStream.close();
            inputStream.close();

            Message msg = new Message();
            msg.what = finishMsg_download;
            msg.arg1 = conn.getResponseCode();
            msg.obj = (String)conn.getResponseMessage();
            handler.sendMessage(msg);

            conn.disconnect();
        }
        catch (Exception e) {
            e.printStackTrace();
            Message msg = new Message();
            msg.what = errorMsg;
            msg.obj = e;
            handler.sendMessage(msg);
        }
        finally {
            return code;
        }
    }

    public int checkAuth() {
        try {
            HttpURLConnection conn = initConnection(webAppLocation + "/auth.php");
            int code = conn.getResponseCode();
            conn.disconnect();
            return code;
        }
        catch (IOException e) {
            e.printStackTrace();
            return HttpURLConnection.HTTP_NOT_FOUND;
        }
    }

    private CharSequence receiveText(HttpURLConnection conn) throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = null;
        StringBuilder response = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response;
    }

    private HttpURLConnection initConnection(CharSequence Location) throws IOException{
        URL httpUrl = new URL(Location.toString());
        HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
        conn.setUseCaches(false);
        conn.setRequestMethod(httpMethod);
        conn.setRequestProperty("Charset", encoding);conn.setChunkedStreamingMode(chunkSize);
        conn.setRequestProperty("Authorization", "Basic " + Base64.encodeToString((user + ":" + password).getBytes(), Base64.DEFAULT));
        return conn;
    }

}