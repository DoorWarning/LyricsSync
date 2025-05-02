package com.example.catch_pixel_ai;

import android.content.BroadcastReceiver;
import android.os.Bundle;

import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver serviceMessageReceiver;
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
    }

    public void onClickUsername(View view){

    }
    public void onClickLobbyMsg(View view){

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
}