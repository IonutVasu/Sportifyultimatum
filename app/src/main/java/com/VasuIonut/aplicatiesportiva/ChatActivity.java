package com.VasuIonut.aplicatiesportiva;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private Button buttonSend;
    private ImageButton buttonHistory;
    private ImageView profileImageView;
    private TextView usernameTextView;
    private MessageAdapter messageAdapter;
    private ArrayList<Message> messages;
    private RequestQueue requestQueue;
    private String chatId;
    private String senderId;
    private String receiverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        buttonHistory = findViewById(R.id.buttonHistory);
        profileImageView = findViewById(R.id.profile_image);
        usernameTextView = findViewById(R.id.username);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chatId = getIntent().getStringExtra("chatId");
        senderId = getIntent().getStringExtra("senderId");
        receiverId = getIntent().getStringExtra("receiverId");

        Log.d("ChatActivity", "onCreate: chatId=" + chatId + ", senderId=" + senderId + ", receiverId=" + receiverId);

        if (chatId == null || senderId == null || receiverId == null) {
            Log.e("ChatActivity", "Chat ID, Sender ID or Receiver ID is null");
            Toast.makeText(this, "Missing chat information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String username = getIntent().getStringExtra("username");
        String profileImage = getIntent().getStringExtra("profileImage");

        if (username != null) {
            usernameTextView.setText(username);
        }
        if (profileImage != null && !profileImage.isEmpty()) {
            Log.d("ChatActivity", "Profile image received: " + profileImage);
            try {
                byte[] decodedString = Base64.decode(profileImage, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                profileImageView.setImageBitmap(decodedByte);
                Log.d("ChatActivity", "Profile image set in ImageView");
            } catch (Exception e) {
                Log.e("ChatActivity", "Error decoding profile image: " + e.getMessage());
            }
        } else {
            Log.d("ChatActivity", "No profile image received");
        }

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages, senderId);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messageAdapter);

        requestQueue = Volley.newRequestQueue(this);

        loadMessages();

        buttonSend.setOnClickListener(v -> sendMessage());
        buttonHistory.setOnClickListener(v -> openHistoryActivity());
    }

    private void loadMessages() {
        String url = "http://10.0.2.2:3000/chat/getMessages/" + chatId;
        Log.d("ChatActivity", "Loading messages from: " + url);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("ChatActivity", "Messages loaded: " + response.toString());
                    try {
                        messages.clear();  // Clear existing messages to avoid duplicates
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject messageJson = response.getJSONObject(i);
                            String messageText = messageJson.getString("message");
                            String senderId = messageJson.getString("senderId");
                            String timestamp = messageJson.getString("timestamp");

                            SimpleDateFormat sdf = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            }
                            String formattedTime = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                formattedTime = sdf.format(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(timestamp));
                            }

                            Message message = new Message(messageText, senderId, formattedTime);
                            messages.add(message);
                        }
                        messageAdapter.notifyDataSetChanged();
                        recyclerViewMessages.scrollToPosition(messages.size() - 1);
                        Log.d("ChatActivity", "Messages displayed in RecyclerView");
                    } catch (JSONException e) {
                        Log.e("ChatActivity", "Error parsing messages: " + e.getMessage());
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing messages", Toast.LENGTH_SHORT).show();
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    Log.e("ChatActivity", "Error loading messages: " + error.getMessage());
                    Toast.makeText(this, "Error loading messages", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(jsonArrayRequest);
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {
            String url = "http://10.0.2.2:3000/chat/sendMessage";
            Log.d("ChatActivity", "Sending message to: " + url);
            JSONObject params = new JSONObject();
            try {
                params.put("chatId", chatId);
                params.put("senderId", senderId);
                params.put("receiverId", receiverId);
                params.put("message", messageText);

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                        response -> {
                            Log.d("ChatActivity", "Message sent: " + response.toString());
                            try {
                                String messageTextResponse = response.getString("message");
                                String senderIdResponse = response.getString("senderId");
                                String timestampResponse = response.getString("timestamp");

                                SimpleDateFormat sdf = null;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                    sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                }
                                String formattedTime = null;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                    formattedTime = sdf.format(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(timestampResponse));
                                }

                                Message message = new Message(messageTextResponse, senderIdResponse, formattedTime);
                                messages.add(message);
                                messageAdapter.notifyDataSetChanged();
                                recyclerViewMessages.scrollToPosition(messages.size() - 1);
                                editTextMessage.setText("");

                                Log.d("ChatActivity", "Message displayed in RecyclerView");
                            } catch (JSONException e) {
                                Log.e("ChatActivity", "Error parsing sent message: " + e.getMessage());
                                e.printStackTrace();
                                Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show();
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        error -> {
                            Log.e("ChatActivity", "Error sending message: " + error.getMessage());
                            Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show();
                        });

                requestQueue.add(jsonObjectRequest);
            } catch (JSONException e) {
                Log.e("ChatActivity", "Error creating JSON object for message: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void openHistoryActivity() {
        sendDummyMessageForHistory(() -> {
            // Redirecționare către HistoryActivity după actualizarea listei de prieteni
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
        });
    }

    private void sendDummyMessageForHistory(Runnable callback) {
        String url = "http://10.0.2.2:3000/chat/sendMessage";
        JSONObject params = new JSONObject();
        try {
            params.put("chatId", chatId);
            params.put("senderId", senderId);
            params.put("receiverId", receiverId);
            params.put("message", ""); // Mesaj fictiv

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                    response -> {
                        Log.d("ChatActivity", "Dummy message sent: " + response.toString());
                        callback.run(); // Apelarea callback-ului după actualizare
                    },
                    error -> {
                        Log.e("ChatActivity", "Error sending dummy message: " + error.getMessage());
                        Toast.makeText(this, "Error sending dummy message", Toast.LENGTH_SHORT).show();
                        callback.run(); // Apelarea callback-ului chiar dacă apare o eroare
                    });

            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            Log.e("ChatActivity", "Error creating JSON object for dummy message: " + e.getMessage());
            e.printStackTrace();
            callback.run(); // Apelarea callback-ului chiar dacă apare o eroare
        }
    }
}
