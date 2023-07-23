package com.example.chatapp.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        // 로그인한 사용자의 이메일을 기반으로 상대방과의 채팅방 데이터만 가져옵니다.
        String encodedEmail = encodeEmail(stEmail);
        Query query = chatsRef.child(encodedEmail);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatRoomArrayList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                        ChatRoom chatRoom = chatSnapshot.getValue(ChatRoom.class);
                        if (chatRoom != null) {
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

        return root;
    }

    private String encodeEmail(String email) {
        return Base64.encodeToString(email.replace(".", "_").replace("@", "_").getBytes(), Base64.NO_WRAP);
    }
}
