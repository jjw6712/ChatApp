package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.ui.notifications.ProfileFragment;

public class ProfileActivity extends AppCompatActivity {

    private String stEmail, stName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Intent로 전달된 데이터를 가져옵니다.
        Intent intent = getIntent();
        stEmail = intent.getStringExtra("email");
        stName = intent.getStringExtra("name");

        // 프로필 프래그먼트를 추가합니다.
        ProfileFragment profileFragment = ProfileFragment.newInstance(stEmail, stName);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .commit();
    }
}