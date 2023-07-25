package com.example.chatapp;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
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

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    private ArrayList<ChatRoom> mDataSet;
    String currentUserEmail, currentUserName;

    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = email;
    }
    // 아이템 클릭 이벤트를 처리할 리스너 인터페이스
    public interface OnItemClickListener {
        void onItemClick(ChatRoom chatRoom);
    }

    // 리스너 객체 참조를 저장하는 변수
    private OnItemClickListener mListener = null;

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }



    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    public static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        public TextView tvUser;
        public TextView tvName;
        public ImageView ivUser;
        public TextView tvMessage;
        public TextView tvTime;

        public ChatRoomViewHolder(View view) {
            super(view);
            tvMessage = view.findViewById(R.id.tvMessage);
            tvTime = view.findViewById(R.id.tvTime);
            tvName = view.findViewById(R.id.tvName);
            ivUser = view.findViewById(R.id.ivUser);
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param myDataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public ChatRoomAdapter(ArrayList<ChatRoom> myDataSet, String stEmail, String stName) {
        mDataSet = myDataSet;
        this.currentUserEmail = stEmail;
        this.currentUserName = stName;

    }

    @Override
    public ChatRoomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.chatuser_item_view, viewGroup, false);
        return new ChatRoomViewHolder(view);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBindViewHolder(ChatRoomViewHolder viewHolder, int position) {
        ChatRoom chatRoom = mDataSet.get(position);
        viewHolder.tvName.setText(chatRoom.getPartnerName());
        viewHolder.tvMessage.setText(chatRoom.getMessage());

        String time = chatRoom.getTime();
        Log.d(TAG, "tvTime: "+time);
        String parsedTime = parseTimeFromKey(time);
        Log.d(TAG, "pasrsedTime: "+parsedTime);
        viewHolder.tvTime.setText(parsedTime);


        // Firebase Storage에서 사용자의 프로필 사진을 다운로드하여 이미지뷰에 설정합니다.
        String profileImageRef;
        if(currentUserEmail.equals(chatRoom.getPartnerEmail())) {
            profileImageRef = "users/" + chatRoom.getEmail() + "/profile.jpg";
        }else{
            profileImageRef = "users/" + chatRoom.getPartnerEmail() + "/profile.jpg";
        }
        StorageReference profileRef = FirebaseStorage.getInstance().getReference().child(profileImageRef);
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (uri != null) {
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

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    int clickedPosition = viewHolder.getAdapterPosition();
                    ChatRoom chatroom = mDataSet.get(clickedPosition);
                    Log.d(TAG, "clickEmail: "+chatroom.getEmail());
                    if (!chatroom.getPartnerEmail().equals(currentUserEmail)) {
                        mListener.onItemClick(chatroom);
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