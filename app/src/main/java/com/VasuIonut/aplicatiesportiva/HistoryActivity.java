// HistoryActivity.java
package com.VasuIonut.aplicatiesportiva;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ConversationAdapter adapter;
    private ArrayList<Conversation> conversationList;
    private RequestQueue requestQueue;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Log.d("HistoryActivity", "onCreate: Activity started");

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        conversationList = new ArrayList<>();
        adapter = new ConversationAdapter(this, conversationList);
        recyclerView.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(this);
        currentUserId = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("userId", "");

        Log.d("HistoryActivity", "onCreate: Current user ID: " + currentUserId);

        fetchConversations();
    }

    private void fetchConversations() {
        String url = "http://10.0.2.2:3000/getFriends/" + currentUserId; // URL-ul endpoint-ului pentru lista de prieteni
        Log.d("HistoryActivity", "fetchConversations: Fetching conversations from: " + url);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("HistoryActivity", "fetchConversations: Response received");
                    try {
                        conversationList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject conversationJson = response.getJSONObject(i);
                            Log.d("HistoryActivity", "fetchConversations: Parsing conversation: " + conversationJson.toString());

                            Conversation conversation = new Conversation(
                                    conversationJson.getString("_id"),
                                    conversationJson.getString("username"),
                                    conversationJson.getJSONObject("profileInfo").getString("description"),
                                    conversationJson.getJSONObject("profileInfo").getString("photo")
                            );
                            conversationList.add(conversation);
                        }
                        adapter.notifyDataSetChanged();
                        Log.d("HistoryActivity", "fetchConversations: Conversations loaded");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("FetchError", "Error parsing conversation details: " + e.getMessage());
                        Toast.makeText(this, "Error parsing conversation details", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("FetchError", "Volley error: " + error.toString());
                    Toast.makeText(this, "Error fetching conversations", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(jsonArrayRequest);
    }
}
