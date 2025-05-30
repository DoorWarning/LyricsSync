package com.example.catch_pixel_ai;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GameAcitivity extends AppCompatActivity {

    private String username;
    private BroadcastReceiver serviceMessageReceiver;
    private final String tag = "GAMEACTIVITY";
    private int second;
    private boolean timerFlag = true;
    private ConstraintLayout gamePanel;
    private ConstraintLayout answerPanel;
    private ConstraintLayout gradePanel;
    private ListView chattingLayout;
    private ListView problemLayout;
    private ListView answerLayout;
    private ListView rankingLayout;
    private ArrayAdapter<String> chatAdapter;
    private AnswerView answerAdapter;
    private ProblemView problemAdapter;
    private RankingView rankingAdapter;
    ArrayList<String> problems;
    ArrayList<String> answers;
    ArrayList<String> rankings;
    private int currentRound = 0;
    private int totalRound = 0;
    private CountDownTimer countDownTimer;
    private static final int TIMEOUT_SECONDS = 60; // 라운드 시간 제한(초)
    private TextView timeText;
    private CountDownTimer animationTimer;
    private final int ANIMATION_SECONDS = 300;

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

        gamePanel = findViewById(R.id.game_panel);
        answerPanel = findViewById(R.id.panel_check_anser);
        gradePanel = findViewById(R.id.game_grade_panel);
        chattingLayout = findViewById(R.id.chat_list_in_game);
        problemLayout = findViewById(R.id.question_list);
        timeText = findViewById(R.id.timer);
        answerLayout = findViewById(R.id.answer_list);
        rankingLayout = findViewById(R.id.grade_list);


        gamePanel.setVisibility(View.VISIBLE);
        answerPanel.setVisibility(View.INVISIBLE);
        gradePanel.setVisibility(View.INVISIBLE);


        chatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        chattingLayout.setAdapter(chatAdapter);

        problems = new ArrayList<String>();
        problemAdapter = new ProblemView(this);
        problemLayout.setAdapter(problemAdapter);

        answers = new ArrayList<String>();
        answerAdapter = new AnswerView(this);
        answerLayout.setAdapter(answerAdapter);

        rankings = new ArrayList<String>();
        rankingAdapter = new RankingView(this);
        rankingLayout.setAdapter(rankingAdapter);



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

        //
        Intent requestIntent = new Intent(this, Client.class);
        requestIntent.setAction(Client.ACTION_REQUEST_LAST_GAME_STATE);
        startService(requestIntent);
        //

        chatAdapter.clear();
        problems.clear();
        answers.clear();
        rankings.clear();

        gamePanel.setVisibility(View.VISIBLE);
        answerPanel.setVisibility(View.INVISIBLE);
        gradePanel.setVisibility(View.INVISIBLE);

        try {
            Intent intent = getIntent();
            username = intent.getStringExtra("USERNAME");
            totalRound = intent.getIntExtra("totalRounds", 0);
        }catch (Exception e){

        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        //LocalBroadcastManager 해제.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceMessageReceiver);
    }

    // 타이머 실행
    private void StartTimers(){
        cancelTimers();

        countDownTimer = new CountDownTimer(TIMEOUT_SECONDS * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeText.setText(String.valueOf(millisUntilFinished/1000));
            }

            @Override
            public void onFinish() {
                timeText.setText("0");
            }
        };
        countDownTimer.start();

    }
    // 타이머 취소
    private void cancelTimers() {
        if(countDownTimer != null){
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    public void onClickGuess(View view){
        Animation.btnAnimation(view);
        EditText editText = findViewById(R.id.chat_game);
        Animation.chattingAnimation(editText);
        String msg = editText.getText().toString();
        if(!msg.isEmpty()){
            try{
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", "guess");
                jsonObject.put("guess", msg);
                Intent intent = new Intent(this, Client.class);
                intent.setAction(Client.ACTTION_SENDJSON);
                intent.putExtra(Client.EXTRA_JSONMSG,jsonObject.toString());
                startService(intent);
                editText.setText("");
            }catch (Exception exception){

            }
        }
    }

    public void onClickCloseRank(View view){
        Animation.btnAnimation(view);
        // 키보드 숨기기
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocus = getCurrentFocus();
        if (imm != null && currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            currentFocus.clearFocus();  // 포커스도 제거
        }

        Intent intent = new Intent(GameAcitivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        animationTimer = new CountDownTimer(ANIMATION_SECONDS, 100) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                finish();
            }
        };
    }

    private void handleChat(String msg){
        chatAdapter.add(msg);
        int positon = chatAdapter.getCount()-1;
        if(positon >= 0){
            chattingLayout.smoothScrollToPosition(positon);
            View newView = chattingLayout.getChildAt(positon);
            if(newView != null){
                ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
                animator.setDuration(5000);
                animator.setInterpolator(new LinearInterpolator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        newView.setAlpha(value);
                    }
                });
                animator.start();
            }
        }
    }

    private void handleSongProblem(JSONObject jsonObject){
        answerPanel.setVisibility(View.INVISIBLE);
        gamePanel.setVisibility(View.VISIBLE);

        problems.clear();
        chatAdapter.clear();

        String singer = jsonObject.optString("singer");
        String title = "가사";
        String desciption = jsonObject.optString("description");
        problems.add(title+":"+desciption);
        currentRound = jsonObject.optInt("round");
        ((TextView)findViewById(R.id.round_count)).setText(currentRound+"/"+totalRound);
        ((TextView)findViewById(R.id.singer_name)).setText(singer);

        timeText.setText("");
        StartTimers();

        runOnUiThread(()-> problemAdapter.notifyDataSetChanged());
    }
    private void handleHintProblem(JSONObject jsonObject){

        String title = "힌트";
        String hint = jsonObject.optString("hint");
        problems.add(title+":"+hint);

        runOnUiThread(()-> {
            problemAdapter.notifyDataSetChanged();
            int position = problemAdapter.getCount() - 1;
            problemLayout.smoothScrollToPosition(position);
        });
    }

    private void handleAnswer(JSONObject jsonObject){
        cancelTimers();
        answers.clear();

        String answer = jsonObject.optString("answer");
        String original = jsonObject.optString("originalLyrics");
        String problem = jsonObject.optString("translatedLyrics");

        ((TextView)findViewById(R.id.answer)).setText(answer);

        answers.add("문제:"+problem);
        answers.add("가사:"+original);


        gamePanel.setVisibility(View.INVISIBLE);
        answerPanel.setVisibility(View.VISIBLE);

        runOnUiThread(()-> answerAdapter.notifyDataSetChanged());
    }

    private void handleGameOver(JSONObject jsonObject){
        rankings.clear();

        try {
            JSONArray ranking = jsonObject.getJSONArray("rankings");
            if(ranking!=null && ranking.length()>0){
                for(int i=0; i<ranking.length(); i++){
                    JSONObject rankInfo = ranking.optJSONObject(i);
                    if(rankInfo != null){
                       String name = rankInfo.optString("username");
                       int score = rankInfo.optInt("score");
                       int rank = rankInfo.optInt("rank");

                       rankings.add(name+":"+String.valueOf(score)+":"+String.valueOf(rank));
                    }
                }
            }
        }catch (Exception e){

        }

        gamePanel.setVisibility(View.INVISIBLE);
        answerPanel.setVisibility(View.INVISIBLE);
        gradePanel.setVisibility(View.VISIBLE);

        runOnUiThread(()-> rankingAdapter.notifyDataSetChanged());
    }
    private void handleServerMessage(String jsonMessage){
        if(jsonMessage == null) return;
        try {
            JSONObject json = new JSONObject(jsonMessage);
            String type = json.optString("type", "");

            // 타입별로 다른 처리 (예시)
            String logMessage = jsonMessage; // 기본적으로는 받은 JSON 그대로 로깅

            switch (type) {
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
                case "songProblem":
                    logMessage = "[문제] Round " + json.optInt("round") + ":\n" + json.optString("description");
                    Log.i(tag,logMessage);
                    handleSongProblem(json);
                    break;
                case "songHint":
                    logMessage = "[힌트] " + json.optString("hint");
                    Log.i(tag,logMessage);
                    handleHintProblem(json);
                    break;
                case "guessResult":
                    boolean correct = json.optBoolean("correct");
                    if (correct) {
                        logMessage = "[결과] " + json.optString("guesser") + " 정답! (+" + json.optInt("scoreEarned") + "점)";
                    } else {
                        logMessage = json.optString("username") + ": " + json.optString("message");
                        Log.i(tag, logMessage);
                        handleChat(logMessage);
                    }
                    Log.i(tag,logMessage);
                    break;
                case "roundResult":
                    logMessage = "[라운드 종료] " + json.optString("answer");
                    handleAnswer(json);
                    // TODO: 점수판 업데이트
                    Log.i(tag,logMessage);
                    break;
                case "gameOver":
                    logMessage = "[게임 종료]\n" + json.optString("message");
                    // TODO: 최종 결과 표시
                    Log.i(tag,logMessage);
                    handleGameOver(json);
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
    public void onBackPressed() {

    }

    public class ProblemView extends ArrayAdapter<String>{
        private final Activity context;

        public ProblemView(Activity context){
            super(context, R.layout.problem_view);
            this.context = context;
        }

        @Override
        public int getCount() {
            return problems.size();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.problem_view, null, true);

            TextView title = (TextView) rowView.findViewById(R.id.titleProblem);
            TextView description = (TextView) rowView.findViewById(R.id.descriptionProblem);

            String[] tokens = problems.get(position).split(":");
            title.setText(tokens[0]);
            description.setText(tokens[1]);

            return rowView;
        }
    }

    public class AnswerView extends ArrayAdapter<String>{
        private final Activity context;

        public AnswerView(Activity context){
            super(context, R.layout.problem_view);
            this.context = context;
        }

        @Override
        public int getCount() {
            return answers.size();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.problem_view, null, true);

            TextView title = (TextView) rowView.findViewById(R.id.titleProblem);
            TextView description = (TextView) rowView.findViewById(R.id.descriptionProblem);

            String[] tokens = answers.get(position).split(":");
            title.setText(tokens[0]);
            description.setText(tokens[1]);

            return rowView;
        }
    }

    public class RankingView extends ArrayAdapter<String>{
        private final Activity context;

        public RankingView(Activity context){
            super(context, R.layout.ranking_view);
            this.context = context;
        }

        @Override
        public int getCount() {
            return rankings.size();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.ranking_view, null, true);

            TextView username = (TextView) rowView.findViewById(R.id.textName);
            TextView score = (TextView) rowView.findViewById(R.id.textScore);
            TextView rank = (TextView) rowView.findViewById(R.id.textRank);

            String[] tokens = rankings.get(position).split(":");
            username.setText(tokens[0]);
            score.setText(tokens[1]);
            rank.setText(tokens[2]);

            return rowView;
        }
    }
}