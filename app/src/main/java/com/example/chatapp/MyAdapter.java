package com.example.chatapp;

import static android.content.ContentValues.TAG;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private ArrayList<Chat> mDataSet;
    String stMyEmail;
    String stMyName;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        public TextView tvName;
        public ImageView ivUser;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textView = (TextView) view.findViewById(R.id.tvChat);//리사이클러 뷰의 각 요소의 tv
            ivUser = (ImageView) view.findViewById(R.id.ivUser);
            tvName = (TextView) view.findViewById(R.id.tvName);

        }

        public TextView getTextView() {
            return textView;
        }
    }

    @Override
    public int getItemViewType(int position) {
        //return super.getItemViewType(position);
        if(mDataSet.get(position).email.equals(stMyEmail)){ // 로그인 된 이메일과 송신자의 이메일이 같다면
            return 1;
        }else return 2;
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param myDataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public MyAdapter(ArrayList<Chat> myDataSet, String stEmail, String stName) { //ChatActivity에서 메세지 배열과 현재 로그인 된 유저의 Email을 받아온다
        mDataSet = myDataSet;
        this.stMyEmail = stEmail;
        this.stMyName = stName;

    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;
        if (viewType == 1) { // 오른쪽으로 정렬하는 뷰 홀더
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.right_text_row_item, viewGroup, false);
        } else { // 왼쪽으로 정렬하는 뷰 홀더
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.text_row_item, viewGroup, false);
        }
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Chat chat = mDataSet.get(position);

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getTextView().setText(mDataSet.get(position).getMessage());
        viewHolder.tvName.setText(stMyName);

        // Firebase Storage에서 사용자의 프로필 사진을 다운로드하여 이미지뷰에 설정합니다.
        String profileImageRef = "users/" + stMyEmail + "/profile.jpg";
        Log.d(TAG, "챗 어댑터 이메일: "+chat.getEmail());
        Log.d(TAG, "챗 어댑터 이름: "+chat.getName());
        StorageReference profileRef = FirebaseStorage.getInstance().getReference().child(profileImageRef);
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (uri != null) {
                    // uri로부터 이미지를 다운로드하여 이미지뷰에 설정합니다.
                    Glide.with(viewHolder.itemView.getContext())
                            .load(uri)
                            .placeholder(R.drawable.freinds) // You can add a placeholder image if desired.
                            .error(R.mipmap.ic_launcher) // You can add an error image if there's a failure.
                            .transform(new CenterCrop(), new RoundedCorners(85)) // 둥근 모서리 처리 (반지름 값을 조정하여 모서리의 둥글기를 조절)
                            .into(viewHolder.ivUser);
                } else {
                    // uri가 null인 경우 기본 이미지를 설정하거나 오류 처리를 수행할 수 있습니다.
                    // 여기서는 기본 이미지를 설정하지 않고 그냥 넘어갑니다.
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // 다운로드에 실패한 경우 기본 이미지 또는 오류 처리를 수행할 수 있습니다.
                // 여기서는 기본 이미지를 설정하지 않고 그냥 넘어갑니다.
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
