package com.example.catch_pixel_ai;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver serviceMessageReceiver;
    private String username;
    ActivityResultLauncher<Intent> loginResult;
    private final String tag = "MAINLOBBY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if(savedInstanceState == null){
            //LoginActivity 실행 후 username을 반환받기 위한 Launcher 등록 ** 무조건 onCreate에서 정의해야 오류X
            loginResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if(result.getResultCode() == Activity.RESULT_OK){
                            Intent data  = result.getData();
                            try {
                                username = data.getStringExtra("USERNAME");
                            }catch (Exception e) {

                            }
                        }
                    });

            //LoginActivity 실행
            startLoginActivity();

            //service로 부터 메세지를 받는 리시버 등록
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

    private void startLoginActivity(){
        //로그인 액티비티 실행
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        loginResult.launch(intent);
    }
    public void onClickStartGame(View view){
        //Game_main.Activity 실행
        Intent intent = new Intent(MainActivity.this, GameAcitivity.class);
        startActivity(intent);
    }
    public void onClickRoomMsg(View view){

    }
    public void onClickGuess(View view){

    }
    public void onClickCreateRoom(View view){

    }
    public void OnClickExitProgram(View view){
        //프로그램 종료
    }
    public void OnClickReady(View view){
        //체크 표시 상태 & 서버에 레디 상태 변동 전송
    }
    public void OnClickSelectRoom(View view){
        //게임 로비 패널 visible & 메인 로비 패널 hide
    }
    public void OnClickExitRoom(View view){
        //게임 로비 패널 hide & 메인 로비 패널 visible
    }
    public void OnClickCloseRanking(View view){

    }
    public void onClickLobbyMsg(View view){
//        EditText editText = findViewById(R.id.inputText);
//        String msg = editText.getText().toString();
//        if(!msg.isEmpty()){
//            try{
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("type", "message");
//                jsonObject.put("username", username);
//                jsonObject.put("text", msg);
//                Intent intent = new Intent(this, Client.class);
//                intent.setAction(Client.ACTTION_SENDJSON);
//                intent.putExtra(Client.EXTRA_JSONMSG,jsonObject.toString());
//                startService(intent);
//                editText.setText("");
//            }catch (Exception exception){
//
//            }
//        }
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("USERNMAE", username);
    }
    @Override
    protected void onStart() {
        super.onStart();
        //인텐트 필터 생성 후 LocalBroadcastManager에 등록하며 연결.
        IntentFilter filter = new IntentFilter();
        filter.addAction(Client.ACTTION_MESSAGE_RECEIVED);
        LocalBroadcastManager.getInstance(this).registerReceiver(serviceMessageReceiver, filter);
    }
    @Override
    protected void onStop() {
        super.onStop();
        //LocalBroadcastManager 해제.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceMessageReceiver);
    }
}