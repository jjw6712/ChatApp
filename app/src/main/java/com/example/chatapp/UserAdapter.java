package com.example.chatapp;

import android.annotation.SuppressLint;
import android.net.Uri;
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

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private ArrayList<User> mDataSet;
    String stMyEmail, stMyNAme;
    String currentUserEmail, currentUserName;
    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = stMyEmail;
    }

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvUser;
        public TextView tvName;
        public ImageView ivUser;
        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            tvUser = (TextView) view.findViewById(R.id.tvUser);//리사이클러 뷰의 각 요소의 tv
            tvName = view.findViewById(R.id.tvName);
            ivUser = view.findViewById(R.id.ivUser);
        }

        public TextView getTextView() {
            return tvUser;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param myDataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public UserAdapter(ArrayList<User> myDataSet, String stEmail, String stName) { //ChatActivity에서 메세지 배열과 현재 로그인 된 유저의 Email을 받아온다
        mDataSet = myDataSet;
        this.currentUserEmail = stEmail;
        this.currentUserName = stName;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.user_item_view, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
        User user = mDataSet.get(position);

        // 현재 로그인한 사용자와 유저의 이메일이 일치하지 않을 때만 표시
        if (!user.getEmail().equals(currentUserEmail)) {
            viewHolder.tvUser.setText(user.getEmail());
            viewHolder.tvName.setText(user.getName());

            // Firebase Storage에서 사용자의 프로필 사진을 다운로드하여 이미지뷰에 설정합니다.
            String profileImageRef = "users/" + user.getEmail() + "/profile.jpg";
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

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    User user = mDataSet.get(position);
                    if (!user.getEmail().equals(currentUserEmail)) {
                        listener.onItemClick(user);
                    }
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
