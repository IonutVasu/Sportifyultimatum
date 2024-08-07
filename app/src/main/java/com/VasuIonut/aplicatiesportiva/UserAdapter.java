package com.VasuIonut.aplicatiesportiva;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private ArrayList<User> users;
    private Context context;

    public UserAdapter(Context context, ArrayList<User> users) {
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.nameTextView.setText(user.getUsername());
        holder.ageTextView.setText(String.valueOf(user.getAge()));
        holder.descriptionTextView.setText(user.getDescription());
        holder.genderTextView.setText(user.getGender());
        if (!user.getProfileImage().isEmpty()) {
            byte[] decodedString = Base64.decode(user.getProfileImage(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.profileImageView.setImageBitmap(decodedByte);
        }
        holder.itemView.setOnClickListener(v -> {
            String senderId = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).getString("userId", "");
            String receiverId = user.getId();
            String chatId = generateChatId(senderId, receiverId);

            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chatId", chatId);
            intent.putExtra("senderId", senderId);
            intent.putExtra("receiverId", receiverId);
            intent.putExtra("username", user.getUsername());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, ageTextView, descriptionTextView, genderTextView;
        ImageView profileImageView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            ageTextView = itemView.findViewById(R.id.ageTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            genderTextView = itemView.findViewById(R.id.genderTextView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
        }
    }
    private String generateChatId(String userId1, String userId2) {
        return userId1.compareTo(userId2) < 0 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }
}
