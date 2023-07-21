package com.example.chatapp;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Comment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.concurrent.BlockingDeque;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    MyAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    EditText etSendMessage; //채팅 매세지 et
    Button btSend; //전송 버튼
    String stEmail, stName, stTime;
    FirebaseDatabase database;
    ArrayList<Chat> chatArrayList; //Chat 클래스 모델을 저장하는 배열
    String chatKey;
    ChildEventListener childEventListener;
    Chat chat;
    TextView tvChatName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        stEmail = getIntent().getStringExtra("email"); // 매인 액티비티에서 email intent를 받아옴
        stName = getIntent().getStringExtra("name");
        Log.d(TAG, "채팅방 이름: "+stName);

        // 로그인된 사용자의 이메일 주소를 얻어옴.
        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail().toString();
        Log.d(TAG, "currentUSer: "+currentUser);



        // 둘만의 채팅방을 구분하기 위한 키를 생성
        if (currentUser != null) {
            String stMyEmail = currentUser;
            if (stMyEmail != null && stEmail != null) {
                if (stMyEmail.compareTo(stEmail) < 0) {
                    chatKey = stMyEmail.replace(".", "_") + "_" + stEmail.replace(".", "_");
                } else {
                    chatKey = stEmail.replace(".", "_") + "_" + stMyEmail.replace(".", "_");
                }
            } else {
                // stMyEmail 또는 stEmail이 null인 경우 처리
            }
        } else {
            // currentUser가 null인 경우 처리 (사용자가 로그인되지 않은 상태)
        }
        Log.d(TAG, "chtkey: "+chatKey);

        tvChatName = findViewById(R.id.tvChatName);
        tvChatName.setText(stName);
        chatArrayList = new ArrayList<>(); //Chat 클래스 모델을 저장하는 배열객체 생성

        database = FirebaseDatabase.getInstance();//Firebase db 객체 생성

        btSend = findViewById(R.id.btSend); //전송 버튼 연결
        etSendMessage = findViewById(R.id.etSendMessage); // 채팅 메세지 et 연결

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this); // 하나의 행을 사용할 때 리니어 레이아웃을 사용
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new MyAdapter(chatArrayList, stEmail, stName);
        recyclerView.setAdapter(mAdapter);

        // 스크롤 자동 이동
        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int itemCount = mAdapter.getItemCount();
                            if (itemCount > 0) {
                                recyclerView.smoothScrollToPosition(itemCount - 1);
                            }
                        }
                    }, 100);
                }
            }
        });

            childEventListener = new ChildEventListener() { // 하위 이벤트 수신 대기 https://firebase.google.com/docs/database/android/lists-of-data?hl=ko
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new comment has been added, add it to the displayed list
                    chat = dataSnapshot.getValue(Chat.class);
                    String commendKey = dataSnapshot.getKey();
                    String stEmail =  chat.getEmail();
                    String stSandMessage = chat.getMessage();
                    String stTime = parseTimeFromKey(dataSnapshot.getKey()); // 시간 정보만 추출하여 stTime에 저장
                    Log.d(TAG, "stEmail: "+stEmail);
                    Log.d(TAG, "stSandMessage: "+stSandMessage);
                    Log.d(TAG, "stTime: "+stTime);
                    chat.setTime(stTime);
                    chatArrayList.add(chat);//Chat 클래스 모델의 객채를 chatArrayList에 추가
                    mAdapter.notifyDataSetChanged(); // 어댑터에 변경사항을 알리고, 해당 아이템의 뷰를 업데이트
                    // ...
                }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so displayed the changed comment.

                // ...
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so remove it.
                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A comment has changed position, use the key to determine if we are
                // displaying this comment and if so move it.
                //Comment movedComment = dataSnapshot.getValue(Comment.class);
                //String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(ChatActivity.this, "Failed to load comments.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        DatabaseReference ref = database.getReference("message"); //Firebase db에서 message 참조항목을 생성하고, 날짜를 자식 참조하여 myRef 변수에 저장
        ref.addChildEventListener(childEventListener);

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stSendMessage = etSendMessage.getText().toString(); // 채팅 메세지et에 적은 문자열을 저장

                Calendar c = Calendar.getInstance();// 날짜를 받아오기 위한 캘린더 객체 생성
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss");
                stTime = dateFormat.format(c.getTime());// 현재 시간을 날짜 포맷에 맞게 저장

                DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatKey);//chats 노드를 생성하고 하위에 채팅방 식별키(chatKey)를 생성
                DatabaseReference messageRef = chatRef.child(stTime); // chats노드밑에 식별키 밑에 날짜를 하위로 두고 메세지 데이터를 저장하는 messageRef에 저장

                Hashtable<String, String> message = new Hashtable<>();
                message.put("email", stEmail); // 사용자 이메일
                message.put("message", stSendMessage); // 채팅 메시지

                messageRef.setValue(message); // 채팅 메시지 데이터를 해당 채팅방의 날짜 하위에 저장

                // EditText 내용 비우기
                etSendMessage.setText("");
                if (!stSendMessage.isEmpty()) {
                    // ... (기존 코드 생략)

                    // 메시지를 보낸 후 스크롤 자동 이동
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollToBottom();
                        }
                    }, 100);
                }
            }
        });

        Button btFinish = findViewById(R.id.btFinish);
        btFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
    private void scrollToBottom() {
        int itemCount = mAdapter.getItemCount();
        if (itemCount > 0) {
            recyclerView.smoothScrollToPosition(itemCount - 1);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        // 채팅방의 데이터를 Firebase Realtime Database에서 불러오기 위해 해당 채팅방에 대한 DatabaseReference를 생성합니다.
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatKey);
        chatRef.addChildEventListener(childEventListener); // 채팅방에 대한 ChildEventListener를 등록하여 채팅 메시지가 추가될 때마다 데이터를 받아옵니다.
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 액티비티가 화면에서 사라지면 Firebase Realtime Database에서의 데이터 수신을 중지합니다.
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatKey);
        // 등록한 ChildEventListener를 해제합니다.
        chatRef.removeEventListener(childEventListener);
    }
    private String parseTimeFromKey(String key) {
        // 예시로 key가 "2023-7월-20 06:06:29"와 같은 형태라고 가정합니다.
        // 먼저 "-"와 " "로 문자열을 분리합니다.
        String[] parts = key.split("[- ]");
        if (parts.length >= 4) {
            // 시간 정보가 "06:06:29"와 같이 뒤에 위치하므로, ":"으로 문자열을 분리하고 첫 번째 요소를 반환합니다.
            String[] timeParts = parts[parts.length - 1].split(":");
            if (timeParts.length >= 2) {
                return timeParts[0] + ":" + timeParts[1];
            }
        }
        return key; // 기본적으로 key 값을 반환합니다.
    }

}