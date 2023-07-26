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
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.chatapp.ProfileActivity;
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
    int REQUEST_IMAGE_CODE_USER = 1001;
    int REQUEST_IMAGE_CODE_USER_BACK = 1002;
    int REQUEST_EXTERNAL_STORAGE_PERMISSION = 1003;
    ImageView ivUser, ivUserBack;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    String stEmail, stName;
    File localFileUser;
    File localFileBack;
    TextView profileName, ivUsertext, ivUserBacktext;
    ImageButton btExit;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btExit = view.findViewById(R.id.btExit);

        // Check if the hosting activity is an instance of HostingActivity
        if (getActivity() instanceof ProfileActivity) {
            // If so, make the btExit button visible
            btExit.setVisibility(View.VISIBLE);
        } else {
            // Otherwise, hide the btExit button
            btExit.setVisibility(View.GONE);
        }
    }

    public static ProfileFragment newInstance(String email, String name) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("email", email);
        args.putString("name", name);
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //상태바를 투명하게 만들고, 이미지를 상태바 까지 적용
            Window w = getActivity().getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        btExit = root.findViewById(R.id.btExit);
        ivUser = root.findViewById(R.id.ivUser);
        ivUserBack = root.findViewById(R.id.ivUserBackground);
        profileName = root.findViewById(R.id.userName);
        ivUsertext = root.findViewById(R.id.ivUserText);
        ivUserBacktext = root.findViewById(R.id.ivUserBackgroundText);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("shared", Context.MODE_PRIVATE);
        stEmail = sharedPref.getString("email", "");
        stName = sharedPref.getString("name", "");
        Log.d(TAG, "stEmail: " + stEmail);

        profileName.setText(stName);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE_PERMISSION);
        }

        btExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity() != null){
                    getActivity().finish();
                }
            }
        });
        ivUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_CODE_USER);
            }
        });
        try {
            localFileUser = File.createTempFile("userimages", "jpg");
            StorageReference riversRef = storageRef.child("users").child(stEmail).child("profile.jpg");
            riversRef.getFile(localFileUser).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Glide.with(getContext())
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
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_CODE_USER_BACK);
            }
        });


        try {
            localFileBack = File.createTempFile("backimages", "jpg");
            StorageReference riversRef = storageRef.child("users").child(stEmail).child("profileBack.jpg");
            riversRef.getFile(localFileBack).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Glide.with(getContext())
                            .load(localFileBack )
                            .transform(new CenterCrop())
                            .into(ivUserBack);
                    ivUserBacktext.setVisibility(View.GONE);
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
                                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), image);
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
                                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), image);
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
}