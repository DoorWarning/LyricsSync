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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver serviceMessageReceiver;
    private String username;
    ActivityResultLauncher<Intent> loginResult;
    private final String tag = "MAINACTIVITY";
    private ConstraintLayout lobyLayout;
    private ConstraintLayout roomLayout;

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

        lobyLayout = findViewById(R.id.panel_befor_join);
        roomLayout = findViewById(R.id.panel_after_join);

        lobyLayout.setVisibility(View.VISIBLE);
        roomLayout.setVisibility(View.INVISIBLE);

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
    private void startGameActivity(){
        //Game_main.Activity 실행
        Intent intent = new Intent(MainActivity.this, GameAcitivity.class);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
    }

    public void onClickCreateRoom(View view){

    }
    public void onClickEnterRoom(View view){

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

    private void handleCreateRoom(){

    }

    private void handleMessage(){

    }

    private void handleServerMessage(String jsonMessage){
        if(jsonMessage == null) return;
        try {
            JSONObject json = new JSONObject(jsonMessage);
            String type = json.optString("type", "");

            // 타입별로 다른 처리 (예시)
            String logMessage = jsonMessage; // 기본적으로는 받은 JSON 그대로 로깅

            switch (type) {
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
                case "playerLeft":
                    logMessage = "[SYSTEM] " + json.optString("username") + "님이 나갔습니다.";
                    Log.i(tag,logMessage);
                    break;
                default:
                    logMessage = "[ERROR]" + "Unkown type: " + type;
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
        outState.putString("USERNAME", username);
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