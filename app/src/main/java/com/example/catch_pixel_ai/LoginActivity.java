package com.example.catch_pixel_ai;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

}
