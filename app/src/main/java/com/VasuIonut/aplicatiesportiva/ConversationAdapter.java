// ConversationAdapter.java
package com.VasuIonut.aplicatiesportiva;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {
    private ArrayList<Conversation> conversations;
    private Context context;

    public ConversationAdapter(Context context, ArrayList<Conversation> conversations) {
        this.conversations = conversations;
        this.context = context;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        holder.nameTextView.setText(conversation.getName());
        holder.descriptionTextView.setText(conversation.getDescription());
        if (!conversation.getProfileImageUrl().isEmpty()) {
            byte[] decodedString = Base64.decode(conversation.getProfileImageUrl(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.profileImageView.setImageBitmap(decodedByte);
        }
        holder.itemView.setOnClickListener(v -> {
            Log.d("ConversationAdapter", "onBindViewHolder: Clicking on conversation: " + conversation.getName());
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chatId", conversation.getId());
            intent.putExtra("senderId", context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).getString("userId", ""));
            intent.putExtra("receiverId", conversation.getId());
            intent.putExtra("username", conversation.getName());
            intent.putExtra("profileImage", conversation.getProfileImageUrl());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public static class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, descriptionTextView;
        ImageView profileImageView;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.conversation_name);
            descriptionTextView = itemView.findViewById(R.id.conversation_description);
            profileImageView = itemView.findViewById(R.id.profile_image);
        }
    }
}
