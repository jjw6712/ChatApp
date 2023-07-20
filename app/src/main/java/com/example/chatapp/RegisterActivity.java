package com.example.chatapp;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Hashtable;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    EditText etId, etPw, etName;
    ProgressBar progressBar; // 로딩바
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        database = FirebaseDatabase.getInstance();//DB초기화
        mAuth = FirebaseAuth.getInstance();

        etId = findViewById(R.id.etId);//id 입력 et
        etPw = findViewById(R.id.etPw);//pw 입력 et
        etName = findViewById(R.id.etName);//name입력 et

        progressBar = findViewById(R.id.Loading); // 로딩 바

        Button btRegister = findViewById(R.id.btRegister); // 회원가입 버튼
        btRegister.setOnClickListener(new View.OnClickListener() {//회원가입 버튼 눌렀을 때
            @Override
            public void onClick(View view) {
                String stEmail = etId.getText().toString();//etId에서 받은 문자열을 stEmial 변수에 저장
                String stPw = etPw.getText().toString();//etPw에서 받은 문자열을 stPw 변수에 저장
                String stName = etName.getText().toString();//etName에서 받은 문자열을 stName 변수에 저장

                if (stName.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "이름을 입력하세요", Toast.LENGTH_SHORT).show();//이메일 미입력 시 토스트 표시
                    return;//이메일 미입력 시 토스트 표시하고 버튼 클릭 수행하지않음
                }
                else if (stEmail.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "이메일을 입력하세요", Toast.LENGTH_SHORT).show();//이메일 미입력 시 토스트 표시
                    return;//이메일 미입력 시 토스트 표시하고 버튼 클릭 수행하지않음
                }
                else if (stPw.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();//비밀번호 미입력 시 토스트 표시
                    return;//비밀번호 미입력 시 토스트 표시하고 버튼 클릭 수행하지않음
                }
                progressBar.setVisibility(View.VISIBLE); // 회원가입이 완료되면 로딩바 비활성화
                mAuth.createUserWithEmailAndPassword(stEmail, stPw) //FireBase 모듈을 이용한 회원가입 객체 생성
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE); // 회원가입이 완료되면 로딩바 비활성화
                                if (task.isSuccessful()) { // 회원가입 을 성공하면
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    Toast.makeText(RegisterActivity.this, "회원가입 성공!!", Toast.LENGTH_SHORT).show();
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    // 사용자 이름을 Firebase 프로필에 설정
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(stName)
                                            .build();

                                    user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User profile updated.");
                                            } else {
                                                Log.w(TAG, "Failed to update user profile.", task.getException());
                                            }
                                        }
                                    });
                                    //updateUI(user);
                                    DatabaseReference myRef = database.getReference("users").child(user.getUid()); //Firebase db에서 users 참조항목을 생성하고, 회원가입 할 때 생긴 유니크아이디를 자식으로 참조

                                    Hashtable<String, String> users //여러 데이터를 같이 전송하는 해쉬 테이블 자료형
                                            = new Hashtable<String, String>();
                                    users.put("email", user.getEmail());
                                    users.put("name", stName);

                                    myRef.setValue(users);
                                    finish();//회원가입 완료 후 액티비티 종료
                                } else { //회원가입 실패하면
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(RegisterActivity.this, "회원가입 실패",
                                            Toast.LENGTH_SHORT).show();
                                    //updateUI(null);
                                }
                            }
                        });

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
