package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Hashtable;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final String TAG = "MainActivity";
    EditText etId, etPw;
    ProgressBar progressBar; // 로딩바
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((AppCompatActivity) MainActivity.this).getSupportActionBar().hide();//플래그먼트 액션바 숨기기

        database = FirebaseDatabase.getInstance();//DB초기화
        mAuth = FirebaseAuth.getInstance();

        etId = findViewById(R.id.etId);//id 입력 et
        etPw = findViewById(R.id.etPw);//pw 입력 et

        progressBar = findViewById(R.id.Loading); // 로딩 바

        Button btLogin = findViewById(R.id.btLogin);//로그인 버튼
        btLogin.setOnClickListener(new View.OnClickListener() {//로그인 버튼을 눌렀을 때
            @Override
            public void onClick(View view) {
                String stEmail = etId.getText().toString();//etId에서 받은 문자열을 stEmial 변수에 저장
                String stPw = etPw.getText().toString();//etPw에서 받은 문자열을 stPw 변수에 저장

                if (stEmail.isEmpty()) {
                    Toast.makeText(MainActivity.this, "이메일을 입력하세요", Toast.LENGTH_LONG).show();//이메일 미입력 시 토스트 표시
                    return;//이메일 미입력 시 토스트 표시하고 버튼 클릭 수행하지않음
                }
                if (stPw.isEmpty()) {
                    Toast.makeText(MainActivity.this, "비밀번호를 입력하세요", Toast.LENGTH_LONG).show();//비밀번호 미입력 시 토스트 표시
                    return;//비밀번호 미입력 시 토스트 표시하고 버튼 클릭 수행하지않음
                }
                progressBar.setVisibility(view.VISIBLE); // 로그인 버튼을 눌렀을 때 위 예외상황이 아니라면 로그인 될 때 까지 로딩바를 활성화
                mAuth.signInWithEmailAndPassword(stEmail, stPw)//FireBase 모듈을 이용한 로그인 객체 생성
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE); // 로그인이 완료되면 로딩바 비활성화
                                if (task.isSuccessful()) {
                                    // 로그인에 성공하면
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    String stUserEmail = user.getEmail();
                                    String stUserName = user.getDisplayName();
                                    Log.d(TAG, "Name: "+stUserName.toString());

                                    SharedPreferences sharedPref = getSharedPreferences("shared", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString("email", stUserEmail);
                                    editor.putString("name", stUserName);
                                    editor.commit();

                                    Toast.makeText(MainActivity.this, "로그인 성공!!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, TabActivity.class);
                                    intent.putExtra("email", stEmail);// ChatActivity에 email값을 intent
                                    intent.putExtra("name", stUserName);
                                    startActivity(intent);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(MainActivity.this, "로그인 실패!!",
                                            Toast.LENGTH_SHORT).show();
                                    //pdateUI(null);
                                }
                            }
                        });
            }
        });

        Button btRegister = findViewById(R.id.btRegister); // 회원가입 버튼
        btRegister.setOnClickListener(new View.OnClickListener() {//회원가입 버튼 눌렀을 때
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
                                }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }

}