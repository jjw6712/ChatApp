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

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    MyAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    EditText etSendMessage; //채팅 매세지 et
    Button btSend; //전송 버튼
    String stEmail;
    FirebaseDatabase database;
    ArrayList<Chat> chatArrayList; //Chat 클래스 모델을 저장하는 배열
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatArrayList = new ArrayList<>(); //Chat 클래스 모델을 저장하는 배열객체 생성

        database = FirebaseDatabase.getInstance();//Firebase db 객체 생성

        stEmail = getIntent().getStringExtra("email"); // 매인 액티비티에서 email intent를 받아옴

        btSend = findViewById(R.id.btSend); //전송 버튼 연결
        etSendMessage = findViewById(R.id.etSendMessage); // 채팅 메세지 et 연결

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this); // 하나의 행을 사용할 때 리니어 레이아웃을 사용
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new MyAdapter(chatArrayList, stEmail);
        recyclerView.setAdapter(mAdapter);

        ChildEventListener childEventListener = new ChildEventListener() { // 하위 이벤트 수신 대기 https://firebase.google.com/docs/database/android/lists-of-data?hl=ko
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new comment has been added, add it to the displayed list
                Chat chat = dataSnapshot.getValue(Chat.class);
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

        btSend.setOnClickListener(new View.OnClickListener() { // 전송 버튼을 눌렀을 때
            @Override
            public void onClick(View view) {
                String stSendMessage = etSendMessage.getText().toString(); //stSendMessage 변수에 채팅 메세지et에 적은 문자열을 저장

                Calendar c = Calendar.getInstance(); //날짜를 받아오기 위한 캘린더 객채 생성
                SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss");
                String datetime = dateformat.format(c.getTime());
                System.out.println(datetime);

                DatabaseReference myRef = database.getReference("message").child(datetime); //Firebase db에서 message 참조항목을 생성하고, 날짜를 자식 참조하여 myRef 변수에 저장


                Hashtable<String, String> message //여러 데이터를 같이 전송하는 해쉬 테이블 자료형
                        = new Hashtable<String, String>();
                message.put("email", stEmail);
                message.put("message", stSendMessage);

                myRef.setValue(message);
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
}