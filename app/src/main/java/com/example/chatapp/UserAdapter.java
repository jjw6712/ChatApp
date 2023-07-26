package com.example.chatapp;

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

public class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_PROFILE = 0;
    private static final int VIEW_TYPE_USER = 1;

    private ArrayList<User> mDataSet;
    String currentUserEmail, currentUserName;
    private RecyclerView.ViewHolder profileViewHolder;

    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = email;
    }

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private OnItemClickListener profileClickListener;

    public void setProfileClickListener(OnItemClickListener listener) {
        profileClickListener = listener;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        public TextView tvProfileEmail;
        public TextView tvProfileName;
        public TextView tvuserCount;
        public ImageView ivProfileUser;

        public ProfileViewHolder(View view) {
            super(view);
            tvuserCount = view.findViewById(R.id.userCount);
            tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
            tvProfileName = view.findViewById(R.id.tvProfileName);
            ivProfileUser = view.findViewById(R.id.ivProfileUser);
        }
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public TextView tvUser;
        public TextView tvName;
        public ImageView ivUser;

        public UserViewHolder(View view) {
            super(view);
            tvUser = view.findViewById(R.id.tvUser);
            tvName = view.findViewById(R.id.tvName);
            ivUser = view.findViewById(R.id.ivUser);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TYPE_PROFILE : VIEW_TYPE_USER;
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param myDataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public UserAdapter(ArrayList<User> myDataSet, String stEmail, String stName) {
        mDataSet = myDataSet;
        this.currentUserEmail = stEmail;
        this.currentUserName = stName;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_PROFILE) {
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.profile_item_view, viewGroup, false);
            return new ProfileViewHolder(view);
        } else {
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.user_item_view, viewGroup, false);
            return new UserViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);

        if (viewType == VIEW_TYPE_PROFILE) {
            ProfileViewHolder profileViewHolder = (ProfileViewHolder) viewHolder;
            User currentUserProfile = mDataSet.get(0);
            profileViewHolder.tvProfileEmail.setText(currentUserProfile.getEmail());
            profileViewHolder.tvProfileName.setText(currentUserProfile.getName());
            profileViewHolder.tvuserCount.setText("친구 "+(getItemCount() - 1)); // 친구 명수 표시

            // Firebase Storage에서 사용자의 프로필 사진을 다운로드하여 이미지뷰에 설정합니다.
            String profileImageRef = "users/" + currentUserProfile.getEmail() + "/profile.jpg";
            StorageReference profileRef = FirebaseStorage.getInstance().getReference().child(profileImageRef);
            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    if (uri != null) {
                        Glide.with(profileViewHolder.itemView.getContext())
                                .load(uri)
                                .placeholder(R.drawable.freinds) // You can add a placeholder image if desired.
                                .error(R.mipmap.ic_launcher) // You can add an error image if there's a failure.
                                .transform(new CenterCrop(), new RoundedCorners(85)) // 둥근 모서리 처리 (반지름 값을 조정하여 모서리의 둥글기를 조절)
                                .into(profileViewHolder.ivProfileUser);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });

            // Set the click listener for the profile view
            profileViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (profileClickListener != null) {
                        int clickedPosition = viewHolder.getAdapterPosition();
                        User user = mDataSet.get(clickedPosition);
                        if (user.getEmail().equals(currentUserEmail)) {
                            profileClickListener.onItemClick(user);
                        }
                    }
                }
            });

        } else {
            UserViewHolder userViewHolder = (UserViewHolder) viewHolder;
            User user = mDataSet.get(position);
            userViewHolder.tvUser.setText(user.getEmail());
            userViewHolder.tvName.setText(user.getName());

            // Firebase Storage에서 사용자의 프로필 사진을 다운로드하여 이미지뷰에 설정합니다.
            String profileImageRef = "users/" + user.getEmail() + "/profile.jpg";
            StorageReference profileRef = FirebaseStorage.getInstance().getReference().child(profileImageRef);
            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    if (uri != null) {
                        Glide.with(userViewHolder.itemView.getContext())
                                .load(uri)
                                .placeholder(R.drawable.freinds) // You can add a placeholder image if desired.
                                .error(R.mipmap.ic_launcher) // You can add an error image if there's a failure.
                                .transform(new CenterCrop(), new RoundedCorners(85)) // 둥근 모서리 처리 (반지름 값을 조정하여 모서리의 둥글기를 조절)
                                .into(userViewHolder.ivUser);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });

            // Set the click listener for the user view
            userViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int clickedPosition = viewHolder.getAdapterPosition();
                        User user = mDataSet.get(clickedPosition);
                        if (!user.getEmail().equals(currentUserEmail)) {
                            listener.onItemClick(user);
                        }
                    }
                }
            });
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}