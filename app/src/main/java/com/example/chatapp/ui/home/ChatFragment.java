package com.example.chatapp.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Chat;
import com.example.chatapp.ChatActivity;
import com.example.chatapp.ChatRoom;
import com.example.chatapp.ChatRoomAdapter;
import com.example.chatapp.ProfileActivity;
import com.example.chatapp.R;
import com.example.chatapp.User;
import com.example.chatapp.UserAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;


public class ChatFragment extends Fragment {

    private FirebaseDatabase database;
    private static final String TAG = "ChatFragment";
    private RecyclerView recyclerView;
    private ChatRoomAdapter cAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<ChatRoom> chatRoomArrayList;
    private String stEmail, stName;
    private SharedPreferences sharedPref;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private File localFile;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat, container, false);

        //((AppCompatActivity) getActivity()).getSupportActionBar().hide();//플래그먼트 액션바 숨기기



        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        sharedPref = getActivity().getSharedPreferences("shared", Context.MODE_PRIVATE);
        stEmail = sharedPref.getString("email", "");
        stName = sharedPref.getString("name", "");
        Log.d(TAG, "stEmail: " + stEmail);
        Log.d(TAG, "stName: " + stName);

        chatRoomArrayList = new ArrayList<>();

        recyclerView = root.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        cAdapter = new ChatRoomAdapter(chatRoomArrayList, stEmail, stName);
        recyclerView.setAdapter(cAdapter);

        DatabaseReference rootRef = database.getReference();
        DatabaseReference chatsRef = rootRef.child("chats");

        // chats 노드 아래의 모든 데이터를 가져옵니다.
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

        // UserAdapter의 클릭 이벤트 처리
        cAdapter.setOnItemClickListener(new ChatRoomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ChatRoom chatRoom) {
                // 해당 유저와의 ChatActivity로 이동하는 코드를 작성합니다.
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("email", chatRoom.getPartnerEmail());
                intent.putExtra("name", chatRoom.getPartnerName());
                startActivity(intent);
            }
        });

        return root;
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

    private DataSnapshot getLastMessageSnapshot(DataSnapshot chatSnapshot) {
        DataSnapshot lastMessageSnapshot = null;
        for (DataSnapshot messageSnapshot : chatSnapshot.getChildren()) {
            lastMessageSnapshot = messageSnapshot;
        }
        return lastMessageSnapshot;
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