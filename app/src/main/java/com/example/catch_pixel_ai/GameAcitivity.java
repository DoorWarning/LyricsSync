package com.example.catch_pixel_ai;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONObject;

public class GameAcitivity extends AppCompatActivity {

    private String username;
    private BroadcastReceiver serviceMessageReceiver;
    private final String tag = "GAMEACTIVITY";
    private int second;
    private boolean timerFlag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game_main);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if(savedInstanceState == null){
            serviceMessageReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(intent == null || intent.getAction() == null){
                        return;
                    }
                    String action = intent.getAction();

                    switch (action){
                        case Client.ACTTION_MESSAGE_RECEIVED:
                            String jsonMSG = intent.getStringExtra(Client.EXTRA_JSONMSG);
                            handleServerMessage(jsonMSG);
                            break;
                        default:
                            break;
                    }
                }
            };
        }else{
            try {
                //액티비가 멈춘 후 복구 되었을 때 복원 사항.
                username = savedInstanceState.get("USERNAME").toString();
            }catch (Exception e){

            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("USERNAME", username);
    }
    @Override
    protected void onStart() {
        super.onStart();
        //인텐트 필터 생성 후 LocalBroadcastManager에 등록하며 연결.
        IntentFilter filter = new IntentFilter();
        filter.addAction(Client.ACTTION_MESSAGE_RECEIVED);
        LocalBroadcastManager.getInstance(this).registerReceiver(serviceMessageReceiver, filter);
        try {
            Intent intent = getIntent();
            username = intent.getStringExtra("USERNAME");
        }catch (Exception e){

        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        //LocalBroadcastManager 해제.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceMessageReceiver);
    }

    //60초 타이머 실행
    private  void timerStart(){
//        second = 60;
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while(second >= 0 && timerFlag){
//                    try {
//                        Thread.sleep(1000);
//                    }catch (Exception e){
//
//                    }
//                    second--;
//                    //타이머를 표시하는 뷰를 가져와서 .post
//                    view.post(new Runnable(){
//                        @Override
//                        public void run() {
//                            view.setText(String.valueOf(second));
//                        }
//                    });
//                    //30초 일때
//                    if(second == 30){
//
//                    }
//                }
//            }
//        }).start();
    }

    private void timerStop(){
        timerFlag = false;
    }

    private void handleServerMessage(String jsonMessage){
        if(jsonMessage == null) return;
        try {
            JSONObject json = new JSONObject(jsonMessage);
            String type = json.optString("type", "");

            // 타입별로 다른 처리 (예시)
            String logMessage = jsonMessage; // 기본적으로는 받은 JSON 그대로 로깅

            switch (type) {
                case "connectSuccess":
                    logMessage = "[SYSTEM] 서버 연결 성공: " + json.optString("message");
                    Log.i(tag, logMessage);
                    break;
                case "error":
                    logMessage = "[ERROR] " + json.optString("message");
                    Log.i(tag,logMessage);
                    break;
                case "message":
                    logMessage = json.optString("username") + ": " + json.optString("text");
                    Log.i(tag,logMessage);
                    break;
                case "lobbyMessage":
                    logMessage = json.optString("username") + ": " + json.optString("text");
                    Log.i(tag,logMessage);
                    break;
                case "roomList":
                    logMessage = "[SYSTEM] 방 목록 업데이트됨 (구현 필요)";
                    Log.i(tag,logMessage);
                    // TODO: 실제 앱에서는 이 데이터를 파싱하여 ListView/RecyclerView 업데이트
                    break;
                case "roomInfo":
                    logMessage = "[SYSTEM] 방 정보 업데이트됨 (구현 필요)";
                    Log.i(tag,logMessage);
                    // TODO: 방 정보 파싱하여 플레이어 목록, 준비 상태 등 업데이트
                    break;
                case "gameStart":
                    logMessage = "[GAME] " + json.optString("message");
                    Log.i(tag,logMessage);
                    break;
                case "songProblem":
                    logMessage = "[문제] Round " + json.optInt("round") + ":\n" + json.optString("description");
                    Log.i(tag,logMessage);
                    break;
                case "songHint":
                    logMessage = "[힌트] " + json.optString("hint");
                    Log.i(tag,logMessage);
                    break;
                case "guessResult":
                    boolean correct = json.optBoolean("correct");
                    if (correct) {
                        logMessage = "[결과] " + json.optString("guesser") + " 정답! (+" + json.optInt("scoreEarned") + "점)";
                    } else {
                        // 오답은 UI에 표시하지 않거나, 본인 오답만 표시 (서버 로직 확인 필요)
                        if(json.optString("guesser").equals(username)){ // 임시로 사용자 이름 비교
                            logMessage = "[결과] '" + json.optString("guess") + "' (오답)";
                        } else {
                            logMessage = null; // 다른 사람 오답은 로그 안 함
                        }
                    }
                    Log.i(tag,logMessage);
                    break;
                case "roundResult":
                    logMessage = "[라운드 종료] " + json.optString("answer");
                    // TODO: 점수판 업데이트
                    Log.i(tag,logMessage);
                    break;
                case "gameOver":
                    logMessage = "[게임 종료]\n" + json.optString("message");
                    // TODO: 최종 결과 표시
                    Log.i(tag,logMessage);
                    break;
                case "playerLeft":
                    logMessage = "[SYSTEM] " + json.optString("username") + "님이 나갔습니다.";
                    Log.i(tag,logMessage);
                    break;
                // 다른 메시지 타입 처리 추가...
            }

        } catch (Exception e) {

        }
    }
}