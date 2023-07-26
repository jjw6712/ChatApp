package com.example.chatapp;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * 프로필 탭바 클릭시 호출되는 클래스
 * 갤러리에서 프로필 사진을 가져오는 기능 구현
 */
public class UserProfileActivity extends AppCompatActivity {
    int REQUEST_IMAGE_CODE_USER = 1001;
    int REQUEST_IMAGE_CODE_USER_BACK = 1002;
    int REQUEST_EXTERNAL_STORAGE_PERMISSION = 1003;
    ImageView ivUser, ivUserBack;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    String stEmail;
    String stName;
    File localFileUser;
    File localFileBack;
    TextView profileName, ivUsertext, ivUserBacktext, btChat;
    ImageButton btExit;
    SharedPreferences sharedPref;
    FirebaseDatabase database;
    ChatRoomAdapter cAdapter;
    ArrayList<ChatRoom> chatRoomArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        sharedPref = UserProfileActivity.this.getSharedPreferences("shared", Context.MODE_PRIVATE);
        String stMyEmail = sharedPref.getString("email", "");
        String stMyName = sharedPref.getString("name", "");
        Log.d(TAG, "stEmail: " + stMyEmail);
        Log.d(TAG, "stName: " + stMyName);

        Intent intent = getIntent();
        stEmail = intent.getStringExtra("email");
        stName = intent.getStringExtra("name");
        Log.d(TAG, "profile Email and Name: "+stEmail+stName);

        cAdapter = new ChatRoomAdapter(chatRoomArrayList, stMyEmail, stMyName);
        chatRoomArrayList = new ArrayList<>();
        database = FirebaseDatabase.getInstance();

        DatabaseReference rootRef = database.getReference();
        DatabaseReference chatsRef = rootRef.child("chats");

        btChat = findViewById(R.id.chatButton);
        btExit = findViewById(R.id.btExit);
        ivUser = findViewById(R.id.ivUser);
        ivUserBack = findViewById(R.id.ivUserBackground);
        profileName = findViewById(R.id.userName);
        ivUsertext = findViewById(R.id.ivUserText);
        ivUserBacktext = findViewById(R.id.ivUserBackgroundText);


        profileName.setText(stName);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();


        ((AppCompatActivity) UserProfileActivity.this).getSupportActionBar().hide();//플래그먼트 액션바 숨기기

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }


        btExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE_PERMISSION);
        }

        ivUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //startActivityForResult(intent, REQUEST_IMAGE_CODE_USER);
            }
        });
        try {
            localFileUser = File.createTempFile("userimages", "jpg");
            StorageReference riversRef = storageRef.child("users").child(stEmail).child("profile.jpg");
            riversRef.getFile(localFileUser).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Glide.with(UserProfileActivity.this)
                            .load(localFileUser)
                            .transform(new CenterCrop(), new RoundedCorners(200))
                            .into(ivUser);
                    ivUsertext.setVisibility(View.GONE);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ivUserBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //startActivityForResult(intent, REQUEST_IMAGE_CODE_USER_BACK);
            }
        });

        try {
            localFileBack = File.createTempFile("backimages", "jpg");
            StorageReference riversRef = storageRef.child("users").child(stEmail).child("profileBack.jpg");
            riversRef.getFile(localFileBack).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Glide.with(UserProfileActivity.this)
                            .load(localFileBack )
                            .transform(new CenterCrop())
                            .into(ivUserBack);
                    ivUserBacktext.setVisibility(View.GONE);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatRoomArrayList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                        // 각 채팅방의 키가 현재 사용자의 이메일을 포함하고 있는지 확인합니다.
                        String chatRoomKey = chatSnapshot.getKey();
                        if (chatRoomKey.contains(encodeEmail(stEmail))) {
                            ChatRoom chatRoom = new ChatRoom();
                            chatRoom.setChatRoomKey(chatRoomKey);

                            String partnerEmail = "";
                            String currentUserEmail = encodeEmail(stEmail); // 현재 로그인한 사용자의 이메일
                            // 현재 사용자 이메일이 chatRoomKey에서 처음으로 나타나는 위치를 찾습니다.
                            int index = chatRoomKey.indexOf(currentUserEmail);

                            if (index == 0) {
                                // 현재 사용자 이메일이 chatRoomKey의 시작 부분에 있으면,
                                // chatRoomKey에서 currentUserEmail의 길이 + 1("_" 문자) 만큼 잘라내고 남은 부분을 partnerEmail로 설정합니다.
                                partnerEmail = chatRoomKey.substring(currentUserEmail.length() + 1);
                            } else if (index > 0) {
                                // 현재 사용자 이메일이 chatRoomKey의 중간 또는 끝 부분에 있으면,
                                // chatRoomKey의 처음부터 currentUserEmail 직전까지의 부분을 partnerEmail로 설정합니다.
                                partnerEmail = chatRoomKey.substring(0, index - 1);
                            }

// 마지막으로 partnerEmail에서 "_" 문자를 "." 문자로 바꿉니다.
                            partnerEmail = partnerEmail.replace("_", ".");
                            chatRoom.setPartnerEmail(partnerEmail);
                            Log.d(TAG, "partnerEmail: "+partnerEmail);

                            // Firebase Realtime Database에서 상대방의 이름을 찾아 설정합니다.
                            setPartnerName(chatRoom, partnerEmail);

                            // 채팅방의 마지막 메시지를 가져옵니다.
                            DataSnapshot lastMessageSnapshot = getLastMessageSnapshot(chatSnapshot);
                            if (lastMessageSnapshot != null) {
                                String stMessage = lastMessageSnapshot.child("message").getValue(String.class);
                                chatRoom.setMessage(stMessage);

                                String stTime = lastMessageSnapshot.getKey();
                                chatRoom.setTime(stTime);
                            }

                            chatRoomArrayList.add(chatRoom);
                        }
                    }
                }
                cAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Error handling if needed
            }
        });

        btChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 해당 유저와의 ChatActivity로 이동하는 코드를 작성합니다.
                Intent intent = new Intent(UserProfileActivity.this, ChatActivity.class);
                intent.putExtra("email", stEmail);
                intent.putExtra("name", stName);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri image = data.getData();
            try {
                if (requestCode == REQUEST_IMAGE_CODE_USER) {
                    StorageReference riversRef = storageRef.child("users").child(stEmail).child("profile.jpg");
                    riversRef.putFile(image)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    try {
                                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), image);
                                        ivUser.setImageBitmap(bitmap);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                } else if (requestCode == REQUEST_IMAGE_CODE_USER_BACK) {
                    StorageReference riversRef = storageRef.child("users").child(stEmail).child("profileBack.jpg");
                    riversRef.putFile(image)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    try {
                                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), image);
                                        ivUserBack.setImageBitmap(bitmap);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private DataSnapshot getLastMessageSnapshot(DataSnapshot chatSnapshot) {
        DataSnapshot lastMessageSnapshot = null;
        for (DataSnapshot messageSnapshot : chatSnapshot.getChildren()) {
            lastMessageSnapshot = messageSnapshot;
        }
        return lastMessageSnapshot;
    }
    private void setPartnerName(ChatRoom chatRoom, String partnerEmail) { // 상대방 이메일 찾기
        DatabaseReference usersRef = database.getReference().child("users");
        Query query = usersRef.orderByChild("email").equalTo(partnerEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String partnerName = (String) userSnapshot.child("name").getValue();
                    chatRoom.setPartnerName(partnerName);
                }
                cAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Error handling if needed
            }
        });
    }
    private String encodeEmail(String email) {
        return email.replace(".", "_");
    }
}