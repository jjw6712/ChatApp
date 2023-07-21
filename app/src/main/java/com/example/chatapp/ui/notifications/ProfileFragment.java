package com.example.chatapp.ui.notifications;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
//import com.example.chatapp.databinding.FragmentNotificationsBinding;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * 프로필 탭바 클릭시 호출되는 클래스
 * 갤러리에서 프로필 사진을 가져오는 기능 구현
 */
public class ProfileFragment extends Fragment {
    int REQUEST_IMAGE_CODE = 1001;
    int REQUEST_EXTERNAL_STORAGE_PERMISSION = 1002;
    ImageView ivUser;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    String stEmail, stName;
    File localFile;
    TextView profileName, ivUsertext;


    // 정적 메서드를 이용해 새로운 ProfileFragment 인스턴스를 생성하고 인자를 전달합니다.
    public static ProfileFragment newInstance(String email, String name) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("email", email);
        args.putString("name", name);
        fragment.setArguments(args);
        return fragment;
    }
    //private FragmentNotificationsBinding binding;

    @SuppressLint("MissingInflatedId")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
       // NotificationsViewModel notificationsViewModel =
       //         new ViewModelProvider(this).get(NotificationsViewModel.class);

        //binding = FragmentNotificationsBinding.inflate(inflater, container, false);

        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        ivUser = root.findViewById(R.id.ivUser);
        profileName = root.findViewById(R.id.profileName);
        ivUsertext = root.findViewById(R.id.ivUserText);
        // 이미지를 선택하지 않았을 때만 텍스트가 보이도록 설정


        SharedPreferences sharedPref = getActivity().getSharedPreferences("shared", Context.MODE_PRIVATE);
        stEmail = sharedPref.getString("email", "");
        stName = sharedPref.getString("name", "");
        Log.d(TAG, "stEmail: " + stEmail);

        profileName.setText(stName+"님의 프로필");


        storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        storageRef = storage.getReference();



        //final TextView textView = binding.textNotifications;
        //notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없는 경우, 권한 요청을 해야 합니다.
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE_PERMISSION);
        }
        ivUser = root.findViewById(R.id.ivUser);
        ivUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_CODE);

            }
        });


        try {
            localFile = File.createTempFile("images", "jpg");
            StorageReference riversRef = storageRef.child("users").child(stEmail).child("profile.jpg");
            riversRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                    // Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    // ivUser.setImageBitmap(bitmap);

                    // Glide로 이미지를 로드하고 둥근 모서리 처리를 적용합니다.
                    Glide.with(getContext())
                            .load(localFile)
                            .transform(new CenterCrop(), new RoundedCorners(200)) // 둥근 모서리 처리 (반지름 값을 조정하여 모서리의 둥글기를 조절)
                            .into(ivUser);
                    ivUsertext.setVisibility(View.GONE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return root;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri image = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), image);
                ivUser.setImageBitmap(bitmap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //Uri file = Uri.fromFile(new File("path/to/images/rivers.jpg"));
            StorageReference riversRef = storageRef.child("users").child(stEmail).child("profile.jpg");
            riversRef.putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, taskSnapshot.toString());

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}