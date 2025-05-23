package com.example.catch_pixel_ai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private String USERNAME;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onClickLogin(View view){
        EditText editText = findViewById(R.id.editUsername);

        if(!editText.getText().toString().isEmpty()){
            //username을 반환하기 위한 Intent 생성
            Intent intent = new Intent();
            USERNAME = editText.getText().toString();
            intent.putExtra("USERNAME", USERNAME);
            setResult(RESULT_OK, intent);

            //server와 연결하는 Intent 생성
            Intent serviceIntent = new Intent(this, Client.class);
            serviceIntent.setAction(Client.ACTTION_CONNECT);
            serviceIntent.putExtra(Client.EXTRA_USERNAME, USERNAME);
            startService(serviceIntent);

            finish();
        }
    }

//    private void handleServerMessage(String jsonMessage){
//        if(jsonMessage == null) return;
//        try {
//            JSONObject json = new JSONObject(jsonMessage);
//            String type = json.optString("type", "");
//
//            // 타입별로 다른 처리 (예시)
//            String logMessage = jsonMessage; // 기본적으로는 받은 JSON 그대로 로깅
//
//            switch (type) {
//                case "connectSuccess":
//                    logMessage = "[SYSTEM] 서버 연결 성공: " + json.optString("message");
//                    Log.i(tag, logMessage);
//                    break;
//                case "error":
//                    logMessage = "[ERROR] " + json.optString("message");
//                    Log.i(tag,logMessage);
//                    break;
//                case "message":
//                    logMessage = json.optString("username") + ": " + json.optString("text");
//                    Log.i(tag,logMessage);
//                    break;
//                case "lobbyMessage":
//                    logMessage = json.optString("username") + ": " + json.optString("text");
//                    Log.i(tag,logMessage);
//                    break;
//                case "roomList":
//                    logMessage = "[SYSTEM] 방 목록 업데이트됨 (구현 필요)";
//                    Log.i(tag,logMessage);
//                    // TODO: 실제 앱에서는 이 데이터를 파싱하여 ListView/RecyclerView 업데이트
//                    break;
//                case "roomInfo":
//                    logMessage = "[SYSTEM] 방 정보 업데이트됨 (구현 필요)";
//                    Log.i(tag,logMessage);
//                    // TODO: 방 정보 파싱하여 플레이어 목록, 준비 상태 등 업데이트
//                    break;
//                case "gameStart":
//                    logMessage = "[GAME] " + json.optString("message");
//                    Log.i(tag,logMessage);
//                    break;
//                case "songProblem":
//                    logMessage = "[문제] Round " + json.optInt("round") + ":\n" + json.optString("description");
//                    Log.i(tag,logMessage);
//                    break;
//                case "songHint":
//                    logMessage = "[힌트] " + json.optString("hint");
//                    Log.i(tag,logMessage);
//                    break;
//                case "guessResult":
//                    boolean correct = json.optBoolean("correct");
//                    if (correct) {
//                        logMessage = "[결과] " + json.optString("guesser") + " 정답! (+" + json.optInt("scoreEarned") + "점)";
//                    } else {
//                        // 오답은 UI에 표시하지 않거나, 본인 오답만 표시 (서버 로직 확인 필요)
//                        if(json.optString("guesser").equals(username)){ // 임시로 사용자 이름 비교
//                            logMessage = "[결과] '" + json.optString("guess") + "' (오답)";
//                        } else {
//                            logMessage = null; // 다른 사람 오답은 로그 안 함
//                        }
//                    }
//                    Log.i(tag,logMessage);
//                    break;
//                case "roundResult":
//                    logMessage = "[라운드 종료] " + json.optString("answer");
//                    // TODO: 점수판 업데이트
//                    Log.i(tag,logMessage);
//                    break;
//                case "gameOver":
//                    logMessage = "[게임 종료]\n" + json.optString("message");
//                    // TODO: 최종 결과 표시
//                    Log.i(tag,logMessage);
//                    break;
//                case "playerLeft":
//                    logMessage = "[SYSTEM] " + json.optString("username") + "님이 나갔습니다.";
//                    Log.i(tag,logMessage);
//                    break;
//                // 다른 메시지 타입 처리 추가...
//            }
//
//        } catch (Exception e) {
//
//        }
//    }

}
