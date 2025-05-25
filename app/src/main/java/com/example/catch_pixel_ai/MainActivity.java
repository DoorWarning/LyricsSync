package com.example.catch_pixel_ai;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.KeyCycle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver serviceMessageReceiver;
    private String username;
    ActivityResultLauncher<Intent> loginResult;
    private final String tag = "MAINACTIVITY";
    private ConstraintLayout lobyLayout;
    private ConstraintLayout roomLayout ;
    private ConstraintLayout roomPopupLayout;
    private LinearLayout exitLayout;
    private ArrayAdapter<String> chatsadapter;
    private ListView lobbymsgLayout;
    private ListView roommsgLayout;
    ArrayList<String> roomNames;
    ArrayList<Integer> roomCurrents;
    ArrayList<Integer> roomCapacities;
    ArrayList<Boolean> roomStarteds;
    ArrayList<Integer> roomRounds;
    ArrayList<String> roomplayers;
    ArrayList<Boolean> playerReady;
    private ListView roomListLayout;
    private ListView playerListLayout;
    private RoomList roomadapter;
    private PlayerList playeradapter;
    private int currentRoomsNow;
    private int currentRoomsTotal;
    private int totalRound = 0;

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
        roomLayout = findViewById(R.id.panel_after_join);;
        roomPopupLayout = findViewById(R.id.panel_creat_room);
        lobbymsgLayout = findViewById(R.id.chat_list_befor_join);
        roommsgLayout = findViewById(R.id.chat_list_after_join);
        roomListLayout = findViewById(R.id.index_room);
        playerListLayout = findViewById(R.id.index_player);
        exitLayout = findViewById(R.id.panel_exit_question);

        roomNames = new ArrayList<String>();
        roomCurrents = new ArrayList<Integer>();
        roomCapacities = new ArrayList<Integer>();
        roomRounds = new ArrayList<Integer>();
        roomStarteds = new ArrayList<Boolean>();
        roomplayers = new ArrayList<String>();
        playerReady = new ArrayList<Boolean>();

        lobyLayout.setVisibility(View.VISIBLE);
        roomLayout.setVisibility(View.INVISIBLE);
        roomPopupLayout.setVisibility(View.INVISIBLE);
        exitLayout.setVisibility(View.INVISIBLE);

        chatsadapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        roomadapter = new RoomList(this);
        playeradapter = new PlayerList(this);

        roomListLayout.setAdapter(roomadapter);
        lobbymsgLayout.setAdapter(chatsadapter);
        roommsgLayout.setAdapter(chatsadapter);
        playerListLayout.setAdapter(playeradapter);

        roomListLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(roomStarteds.get(position) == true){
                    Toast.makeText(getBaseContext(), "이미 게임이 시작 된 방입니다.", Toast.LENGTH_SHORT).show();
                }else{
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("type", "joinRoom");
                        jsonObject.put("roomName", roomNames.get(position));
                        Intent intent = new Intent(getBaseContext(), Client.class);
                        intent.setAction(Client.ACTTION_SENDJSON);
                        intent.putExtra(Client.EXTRA_JSONMSG,jsonObject.toString());
                        startService(intent);
                    }catch (Exception e){

                    }
                }
            }
        });

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
                        }else{
                            finish();
                        }
                    });

            //LoginActivity 실행
            Log.i(tag, "startLoginAcitivity");
            startLoginActivity();

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
        intent.putExtra("totalRounds", totalRound);
        chatsadapter.clear();
        playeradapter.clear();
        startActivity(intent);
    }

    public void onClickShowCreateRoom(View view){
        if(roomPopupLayout.getVisibility() == View.INVISIBLE){
            roomPopupLayout.setVisibility(View.VISIBLE);
            ((Button)findViewById(R.id.creat_room_btn)).setText("생성 창 닫기");
        }
        else{
            roomPopupLayout.setVisibility(View.INVISIBLE);
            ((Button)findViewById(R.id.creat_room_btn)).setText("방 생성");
        }

    }

    public void onClickReady(View view){
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "ready");
            jsonObject.put("status", true);
            Intent intent = new Intent(this, Client.class);
            intent.setAction(Client.ACTTION_SENDJSON);
            intent.putExtra(Client.EXTRA_JSONMSG,jsonObject.toString());
            startService(intent);
        }catch (Exception exception){

        }
    }

    public void onClickRefreshRoomList(View view){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "getRoomList");
            Intent intent = new Intent(this, Client.class);
            intent.setAction(Client.ACTTION_SENDJSON);
            intent.putExtra(Client.EXTRA_JSONMSG,jsonObject.toString());
            startService(intent);
        }catch (Exception e){

        }
    }

    public void onClickCreateRoom(View view){

        String roomName = ((EditText)findViewById(R.id.creat_room_title)).getText().toString();
        String roomRound = ((EditText)findViewById(R.id.creat_round_count)).getText().toString();
        String roomPlayer = ((EditText)findViewById(R.id.creat_player_count)).getText().toString();

        if(roomName.isEmpty()){
            Toast.makeText(this, "방 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
        } else if (roomRound.isEmpty()) {
            Toast.makeText(this, "라운드 수를 입력해주세요.", Toast.LENGTH_SHORT).show();
        } else if (roomPlayer.isEmpty()) {
            Toast.makeText(this, "총 인원을 입력해주세요.", Toast.LENGTH_SHORT).show();
        } else if (!roomName.matches("^[a-zA-Z0-9가-힣]*$")) {
            Toast.makeText(this, "방 이름은 영문, 숫자, 한글만 사용 가능합니다.", Toast.LENGTH_SHORT).show();
        } else if (Integer.valueOf(roomPlayer) >9 || Integer.valueOf(roomPlayer) < 2) {
            Toast.makeText(this, "잘못된 수용 인원입니다. 2에서 8 사이여야 합니다.", Toast.LENGTH_SHORT).show();
        } else if (Integer.valueOf(roomRound) < 1) {
            Toast.makeText(this, "잘못된 라운드 수 입니다. 1 이상여야 합니다.", Toast.LENGTH_SHORT).show();
        } else{
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", "createRoom");
                jsonObject.put("roomName", roomName);
                jsonObject.put("capacity", roomPlayer);
                jsonObject.put("totalRounds", roomRound);
                Intent intent = new Intent(this, Client.class);
                intent.setAction(Client.ACTTION_SENDJSON);
                intent.putExtra(Client.EXTRA_JSONMSG, jsonObject.toString());
                startService(intent);
                ((EditText)findViewById(R.id.creat_round_count)).setText("");
                ((EditText)findViewById(R.id.creat_room_title)).setText("");
                ((EditText)findViewById(R.id.creat_player_count)).setText("");
            }catch (Exception e){

            }
            roomPopupLayout.setVisibility(View.INVISIBLE);
            roomLayout.setVisibility(View.VISIBLE);
            lobyLayout.setVisibility(View.INVISIBLE);
            chatsadapter.clear();
        }
    }

    public void onClickExit(View view){
        exitLayout.setVisibility(View.VISIBLE);
    }

    public void onClickExitYes(View view){
        finish();
    }

    public void onClickExitNo(View view){
        exitLayout.setVisibility(View.INVISIBLE);
    }

    public void onClickLeaveRoom(View view){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "leaveRoom");
            Intent intent = new Intent(this, Client.class);
            intent.setAction(Client.ACTTION_SENDJSON);
            intent.putExtra(Client.EXTRA_JSONMSG,jsonObject.toString());
            startService(intent);
            chatsadapter.clear();
            roomLayout.setVisibility(View.INVISIBLE);
            lobyLayout.setVisibility(View.VISIBLE);
        }catch (Exception e){

        }
    }

    public void onClickLobbyMsg(View view){
        EditText editText = findViewById(R.id.chat_befor_join);
        String msg = editText.getText().toString();
        if(!msg.isEmpty()){
            try{
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", "message");
                jsonObject.put("username", username);
                jsonObject.put("text", msg);
                Intent intent = new Intent(this, Client.class);
                intent.setAction(Client.ACTTION_SENDJSON);
                intent.putExtra(Client.EXTRA_JSONMSG,jsonObject.toString());
                startService(intent);
                editText.setText("");
            }catch (Exception exception){

            }
        }
    }

    public void onClickRoomMsg(View view){
        EditText editText = findViewById(R.id.chat_after_join);
        String msg = editText.getText().toString();
        if(!msg.isEmpty()){
            try{
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", "message");
                jsonObject.put("username", username);
                jsonObject.put("text", msg);
                Intent intent = new Intent(this, Client.class);
                intent.setAction(Client.ACTTION_SENDJSON);
                intent.putExtra(Client.EXTRA_JSONMSG,jsonObject.toString());
                startService(intent);
                editText.setText("");
            }catch (Exception exception){

            }
        }
    }

    private void handleRoomMSG(String message){
        chatsadapter.add(message);
    }

    private void handleLobbyMSG(String message){
        chatsadapter.add(message);
    }

    private void handleRoomInfo(JSONArray players){
        roomPopupLayout.setVisibility(View.INVISIBLE);
        roomLayout.setVisibility(View.VISIBLE);
        lobyLayout.setVisibility(View.INVISIBLE);


        chatsadapter.clear();
        roomplayers.clear();
        playerReady.clear();

        if (players != null) {
            for (int i = 0; i < players.length(); i++) {
                JSONObject player = players.optJSONObject(i);
                if (player != null) {
                    roomplayers.add(player.optString("username"));
                    playerReady.add(player.optBoolean("ready"));
                }
            }
        }

        runOnUiThread(() -> playeradapter.notifyDataSetChanged());
    }

    private void handleRoomList(JSONArray rooms){
        roomNames.clear();
        roomCurrents.clear();
        roomCapacities.clear();
        roomRounds.clear();
        roomStarteds.clear();

        if(rooms!= null && rooms.length()>0){
            for(int i = 0; i < rooms.length(); i++){
                JSONObject room = rooms.optJSONObject(i);
                if(room != null && room.optInt("current")!=0){
                    roomNames.add(room.optString("name"));
                    roomCurrents.add(room.optInt("current"));
                    roomCapacities.add(room.optInt("capacity"));
                    roomRounds.add(room.optInt("rounds"));
                    roomStarteds.add(room.optBoolean("gameStarted"));
                }
            }
        }
        runOnUiThread(() -> roomadapter.notifyDataSetChanged());
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
                    //게임 로비 메세지
                    logMessage = json.optString("username") + ": " + json.optString("text");
                    handleRoomMSG(logMessage);
                    Log.i(tag,logMessage);
                    break;
                case "lobbyMessage":
                    //메인 로비 메세지
                    logMessage = json.optString("username") + ": " + json.optString("text");
                    handleLobbyMSG(logMessage);
                    Log.i(tag,logMessage);
                    break;
                case "roomList":
                    Log.i(tag,logMessage);
                    handleRoomList(json.optJSONArray("rooms"));
                    break;
                case "roomInfo":
                    currentRoomsNow = json.optInt("currentGameSize");
                    currentRoomsTotal = json.optInt("capacity");
                    totalRound = json.optInt("totalRounds");
                    handleRoomInfo(json.optJSONArray("players"));
                    Log.i(tag,logMessage);
                    // TODO: 방 정보 파싱하여 플레이어 목록, 준비 상태 등 업데이트
                    break;
                case "gameStart":
                    logMessage = "[GAME] " + json.optString("message");
                    Log.i(tag,logMessage);
                    startGameActivity();
                    break;
                case "playerLeft":
                    logMessage = "[SYSTEM] " + json.optString("username") + "님이 나갔습니다.";
                    Log.i(tag,logMessage);
                    break;
                case "error":
                    logMessage = "[ERROR] " + json.optString("message");
                    Toast.makeText(this, logMessage, Toast.LENGTH_SHORT).show();
                    Log.i(tag, logMessage);
                case "songProblem":
                    logMessage = "[문제] Round " + json.optInt("round") + ":\n" + json.optString("description");
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

    @Override
    protected void onResume() {
        super.onResume();
        chatsadapter.clear();
        playeradapter.clear();
        Intent requestIntent = new Intent(this, Client.class);
        requestIntent.setAction(Client.ACTION_REQUEST_LAST_ROOM_INFO);
        startService(requestIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(tag, "MainActivity onDestroy called.");

        if (isFinishing()) {
            Log.d(tag, "MainActivity is finishing. Sending disconnect to service.");
            Intent serviceIntent = new Intent(this, Client.class);
            serviceIntent.setAction(Client.ACTTION_DISCONNECT);
            startService(serviceIntent); // 서비스에 연결 해제 명령
        }
    }

    public class RoomList extends ArrayAdapter<String> {
        private final Activity context;

        public RoomList(Activity context){
            super(context, R.layout.sample_room_view);
            this.context = context;

        }

        @Override
        public int getCount() {
            return roomNames.size();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.sample_room_view, null, true);

            TextView player = (TextView) rowView.findViewById(R.id.room_count_player);
            TextView round = (TextView) rowView.findViewById(R.id.room_count_round);
            TextView name = (TextView) rowView.findViewById(R.id.room_player_name);

            name.setText(roomNames.get(position));
            round.setText(roomRounds.get(position).toString());
            player.setText(roomCurrents.get(position).toString() + "/" + roomCapacities.get(position).toString());

            return rowView;
        }
    }

    public class PlayerList extends ArrayAdapter<String>{
        private final Activity context;

        public PlayerList(Activity context){
            super(context, R.layout.sample_sample_player_view);
            this.context = context;
        }

        @Override
        public int getCount() {
            return roomplayers.size();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.sample_sample_player_view, null, true);

            TextView player = (TextView) rowView.findViewById(R.id.room_player_name1);
            ImageView ready = (ImageView) rowView.findViewById(R.id.imageViewReady);
            ((TextView)findViewById(R.id.player_count)).setText(Integer.toString(currentRoomsNow)+ "/" + Integer.toString(currentRoomsTotal));
            player.setText(roomplayers.get(position));
            if(playerReady.get(position)){
                //레디했을때이미지
                ready.setVisibility(View.VISIBLE);
            }else{
                //레디 안했을때
                ready.setVisibility(View.INVISIBLE);
            }

            return rowView;
        }
    }
    @Override
    public void onBackPressed() {

    }
}

