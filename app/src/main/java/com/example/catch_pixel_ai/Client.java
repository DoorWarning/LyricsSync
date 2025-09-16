package com.example.catch_pixel_ai;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client extends Service {
    private static final String TAG = "NetworkClientService";
//    private static final String IP = "10.0.2.2"; //서버 아이피 주소 (10.0.2.2 : localhost)
    private static final String IP = ""; //서버 아이피 주소 (10.0.2.2 : localhost)
    private static final int PORT = 55555; //서버 포트

    private ExecutorService networkExecutor;
    private Socket socket; //서버 연결 소켓
    private PrintWriter out; //서버로 메세지 전송용
    private BufferedReader in; //서버로 부터 메세지 수신용
    private String username; //클라이언트 사용자 이름
    private String currentRoomName; //현재 참가 중인 방 이름
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private LocalBroadcastManager broadcastManager;
    private volatile String lastSongProblemJsonString = null;
    private volatile String lastRoomInfoJsonString = null;
    
    //Intent 식별용 ACTION 정의
    public static final String ACTTION_CONNECT = "com.example.catch_pixel_ai.ACTTION_CONNECT";
    public static final String ACTTION_DISCONNECT = "com.example.catch_pixel_ai.ACTTION_DISCONNECT";
    public static final String ACTTION_SENDJSON = "com.example.catch_pixel_ai.ACTTION_SENDJSON";
    public static final String ACTTION_MESSAGE_RECEIVED = "com.example.catch_pixel_ai.ACTTION_MESSAGE_RECEIVED";
    public static final String ACTTION_CONNECTIONSTATUS = "com.example.catch_pixel_ai.ACTTION_CONNECTIONSTATUS";
    public static final String ACTION_REQUEST_LAST_GAME_STATE = "com.example.catch_pixel_ai.ACTION_REQUEST_LAST_GAME_STATE";
    public static final String ACTION_REQUEST_LAST_ROOM_INFO = "com.example.catch_pixel_ai.ACTION_REQUEST_LAST_ROOM_INFO";

    //Intent를 통해 데이터를 주고 받기 위한 EXTRA 정의
    public static final String EXTRA_USERNAME = "com.example.catch_pixel_ai.EXTRA_USERNAME";
    public static final String EXTRA_JSONMSG = "com.example.catch_pixel_ai.EXTRA_JSONMSG";
    public static final String EXTRA_CONNECTIONSTATUS = "com.example.catch_pixel_ai.EXTRA_CONNECTIONSTATUS";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service Created!");
//        networkExecutor = Executors.newSingleThreadExecutor();
        networkExecutor = Executors.newFixedThreadPool(2); //네트워크 Thread 개수 2개 생성(서버 전송, 서버 응답)
        broadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service onStartCommand received.");
        if(intent != null && intent.getAction() != null){
            String action = intent.getAction();
            Log.d(TAG, "Action: " + action);
            switch (action){
                case ACTTION_CONNECT:
                    if(!isConnected.get()){
                        username = intent.getStringExtra(EXTRA_USERNAME);
                        connectAndListen();
                    }else{
                        Log.w(TAG, "Connect(action) ignored: Already connected.");
                    }
                    break;
                case ACTTION_DISCONNECT:
                    disconnect("Disconnect by UI");
                    break;
                case ACTTION_SENDJSON:
                    if(isConnected.get()){
                        String jsonMSG = intent.getStringExtra(EXTRA_JSONMSG);
                        if(jsonMSG != null){
                            sendMessageToServer(jsonMSG);
                        }else{
                            Log.w(TAG, "Send_JSON(action) ignored: Message is null");
                        }
                    }else{
                        Log.w(TAG, "Send_JSON(action) ignored: Not connected.");
                    }
                    break;
                case ACTION_REQUEST_LAST_GAME_STATE:
                    if(lastSongProblemJsonString != null){
                        broadcastSpecificMessage(ACTTION_MESSAGE_RECEIVED, Client.EXTRA_JSONMSG, lastSongProblemJsonString);
                    }
                    break;
                case ACTION_REQUEST_LAST_ROOM_INFO:
                    if(lastRoomInfoJsonString != null){
                        broadcastSpecificMessage(ACTTION_MESSAGE_RECEIVED, Client.EXTRA_JSONMSG, lastRoomInfoJsonString);
                    }
                    break;
                default:
                    Log.w(TAG, "Unknown action: " + action);
                    break;
            }
        }else{
            Log.w(TAG, "onStartCommand received null intent or action.");
        }
        //서비스가 강제 종료됬을 때 재시작 X
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Servic destroying!!");
        disconnect("Service destroyed");
        if(networkExecutor != null && !networkExecutor.isShutdown()){
            networkExecutor.shutdown();
        }
        Log.d(TAG, "Service destroyed.");
    }

    private void connectAndListen(){
        if(isRunning.get()){
            Log.w(TAG, "connectAndListen already running.");
            return;
        }
        networkExecutor.submit(()->{
            try {
//               closeResources();
                Log.i(TAG, "Connecting to " + IP + ":" + PORT);
                socket = new Socket(IP, PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                isRunning.set(true);
                isConnected.set(true);

                Log.i(TAG, "Connection success!!");
                broadcastConnectionStatus(true);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", "connect");
                jsonObject.put("username", username);
                sendMessageToServer(jsonObject.toString());

                String serverMsg;
                while (isRunning.get() && (serverMsg = in.readLine()) !=null){

                    try {
                        JSONObject tempJson = new JSONObject(serverMsg);
                        String type = tempJson.optString("type");
                        if(type.equals("songProblem")){
                            lastSongProblemJsonString = serverMsg;
                        }else if(type.equals("roomInfo")){
                            lastRoomInfoJsonString = serverMsg;
                        }
                    }catch (Exception e){

                    }

                    broadcastMessage(serverMsg);
                }
                Log.d(TAG, "readLine null");
            }catch (Exception e){
                Log.e(TAG, "Exception in connectAndListen. Username: " + username, e);
            }finally {
                disconnect("Listener loop finished.");
            }
        });
    }

    private void disconnect(String reason){
        if(!isRunning.compareAndSet(true, false)){
            return;
        }
        Log.i(TAG, "Disconnecting... Reason: " + reason);

        if(networkExecutor!=null && !networkExecutor.isShutdown()){
            networkExecutor.submit(this::closeResources);
        }else{
            closeResources();
        }

        if(isConnected.compareAndSet(true, false)){
            broadcastConnectionStatus(false);
        }
    }

    private void closeResources(){
        try {
            if(out!=null)
                out.close();
            if (in!=null)
                in.close();
            if(socket!=null && !socket.isClosed())
                socket.close();
            Log.d(TAG, "Network resources closed.");
        }catch (Exception e){
            Log.e(TAG, "Error closing Network resources", e);
        }finally {
            out = null;
            in = null;
            socket = null;
        }
    }
    private void sendMessageToServer(String jsonMessage){
        if(!isConnected.get() || out == null){
            Log.w(TAG, "Cannot send message: Not connected!!");
            return;
        }
        if (networkExecutor != null && !networkExecutor.isShutdown()){
            networkExecutor.submit(()-> {
                if (out != null && !out.checkError()) {
                    out.println(jsonMessage);
                    Log.i(TAG, "Message sent to server: " + jsonMessage);
                    if (out.checkError()) { // 전송 후 즉시 오류 확인
                        Log.e(TAG, "PrintWriter error after sending message.");
                        disconnect("Error sending message");
                    }
                } else {
                    Log.e(TAG, "Cannot send message: PrintWriter error.");
                    disconnect("Error sending message");
                }
            });
        }else{
            Log.e(TAG, "Cannot send message: Executor service is not running.");
        }
    }
    private void broadcastMessage(String message){
        Intent intent = new Intent(ACTTION_MESSAGE_RECEIVED);
        intent.putExtra(EXTRA_JSONMSG, message);
        broadcastManager.sendBroadcast(intent);
    }
    private void broadcastConnectionStatus(boolean connedted){
        Intent intent = new Intent(ACTTION_CONNECTIONSTATUS);
        intent.putExtra(EXTRA_CONNECTIONSTATUS, connedted);
        broadcastManager.sendBroadcast(intent);
    }
    private void broadcastSpecificMessage(String action, String extraKey, String message) {
        Intent intent = new Intent(action);
        intent.putExtra(extraKey, message);
        broadcastManager.sendBroadcast(intent);
    }
}
