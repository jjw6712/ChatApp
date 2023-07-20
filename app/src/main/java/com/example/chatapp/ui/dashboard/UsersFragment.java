package com.example.chatapp.ui.dashboard;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.DialogFragmentNavigatorDestinationBuilder;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.ChatActivity;
import com.example.chatapp.MyAdapter;
import com.example.chatapp.R;
import com.example.chatapp.User;
import com.example.chatapp.UserAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 친구 탭바를 누르면 친구 목록 플래그먼트를 호출
 * Firebase DB에 users에 저장된 UID와 Email을 Json형태로 불러와  {eqAHRabl9eUMmxfMM3maNJqikmn1={email=jinwo@naver.com}}
 * 리사이클러 뷰로 불러온 친구 목록을 보여줌
 */

public class UsersFragment extends Fragment {

    //private FragmentDashboardBinding binding;
    FirebaseDatabase database;
    private static final String TAG = "UsersFragment";
    private RecyclerView recyclerView;
    UserAdapter uAdapter;
    private RecyclerView.LayoutManager layoutManager;
    ArrayList<User> userArrayList;
    private String stEmail, stName;
    SharedPreferences sharedPref;
    FirebaseStorage storage;
    StorageReference storageRef;
    public File localFile;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        database = FirebaseDatabase.getInstance();//DB초기화
        storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        storageRef = storage.getReference();

        //binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = inflater.inflate(R.layout.users_dashboard, container, false);


        sharedPref = getActivity().getSharedPreferences("shared", Context.MODE_PRIVATE);
        stEmail = sharedPref.getString("email", "");
        stName = sharedPref.getString("name", "");
        Log.d(TAG, "stEmail: " + stEmail);
        Log.d(TAG, "stName: "+stName);

        userArrayList = new ArrayList<>();

        recyclerView = root.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext()); // 하나의 행을 사용할 때 리니어 레이아웃을 사용
        recyclerView.setLayoutManager(layoutManager);

        uAdapter = new UserAdapter(userArrayList, stEmail, stName);
        recyclerView.setAdapter(uAdapter);

        DatabaseReference ref = database.getReference("users"); //Firebase db에서 users 참조항목을 생성
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: "+snapshot.getValue().toString());
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    Log.d(TAG, "datasnapshot1: "+snapshot1.getValue().toString());
                    User user = snapshot1.getValue(User.class);
                    userArrayList.add(user);
                }
                uAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // UserAdapter의 클릭 이벤트 처리
        uAdapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User user) {
                // 해당 유저와의 ChatActivity로 이동하는 코드를 작성합니다.
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("email", user.getEmail()); // 또는 필요한 데이터를 인텐트에 추가할 수 있습니다.
                Log.d(TAG, "유저 목록 클릭시 상대 이메일: "+user.getEmail());
                intent.putExtra("name", user.getName());
                Log.d(TAG, "유저 목록 클릭시 상대 이메일: "+user.getName());
                startActivity(intent);
            }
        });

        //final TextView textView = binding.textDashboard;
        //dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

}