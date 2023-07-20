package com.example.chatapp;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    String stEmail, stName;
    FirebaseDatabase database;
    ArrayList<Chat> chatArrayList; //Chat 클래스 모델을 저장하는 배열
    String chatKey;
    ChildEventListener childEventListener;
    Chat chat;
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

            childEventListener = new ChildEventListener() { // 하위 이벤트 수신 대기 https://firebase.google.com/docs/database/android/lists-of-data?hl=ko
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new comment has been added, add it to the displayed list
                    chat = dataSnapshot.getValue(Chat.class);
                    String commendKey = dataSnapshot.getKey();
                    String stEmail =  chat.getEmail();
                    String stSandMessage = chat.getMessage();
                    Log.d(TAG, "stEmail: "+stEmail);
                    Log.d(TAG, "stSandMessage: "+stSandMessage);
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

        btSend.setOnClickListener(new View.OnClickListener() { // 전송 버튼을 클릭했을 때의 동작 정의
            @Override
            public void onClick(View view) {
                String stSendMessage = etSendMessage.getText().toString(); // 채팅 메세지et에 적은 문자열을 저장

                Calendar c = Calendar.getInstance();// 날짜를 받아오기 위한 캘린더 객체 생성
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss");
                String datetime = dateFormat.format(c.getTime());// 현재 시간을 날짜 포맷에 맞게 저장

                DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatKey);//chats 노드를 생성하고 하위에 채팅방 식별키(chatKey)를 생성
                DatabaseReference messageRef = chatRef.child(datetime); // chats노드밑에 식별키 밑에 날짜를 하위로 두고 메세지 데이터를 저장하는 messageRef에 저장

                Hashtable<String, String> message = new Hashtable<>();
                message.put("email", stEmail); // 사용자 이메일
                message.put("message", stSendMessage); // 채팅 메시지

                messageRef.setValue(message); // 채팅 메시지 데이터를 해당 채팅방의 날짜 하위에 저장
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

}