package com.VasuIonut.aplicatiesportiva;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MatchesUsersActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private ArrayList<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches_users);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        users = new ArrayList<>();
        userAdapter = new UserAdapter(this,users);
        recyclerView.setAdapter(userAdapter);

        ArrayList<String> userIds = getIntent().getStringArrayListExtra("matchingUserIds");
        if (userIds != null && !userIds.isEmpty()) {
            fetchUsersDetails(userIds);
        } else {
            Toast.makeText(this, "No user IDs provided", Toast.LENGTH_SHORT).show();
            Log.e("MatchesUsersActivity", "No user IDs provided");
        }
    }

    private void fetchUsersDetails(ArrayList<String> userIds) {
        RequestQueue queue = Volley.newRequestQueue(this);

        for (String userId : userIds) {
            String url = "http://10.0.2.2:3000/user/" + userId;

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                                JSONObject userJson = response;
                                User user = new User(
                                        userJson.getString("_id"),
                                        userJson.getString("username"),
                                        userJson.getString("email"),
                                        userJson.getJSONObject("profileInfo").getString("photo"),
                                        userJson.getJSONObject("profileInfo").getInt("age"),
                                        userJson.getJSONObject("profileInfo").getString("description"),
                                        userJson.getJSONObject("profileInfo").getString("gender")
                                );
                                users.add(user);
                                userAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("FetchError", "Error parsing user details: " + e.getMessage());
                            Toast.makeText(this, "Error parsing user details", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e("FetchError", "Volley error: " + error.toString());
                        Toast.makeText(this, "Error fetching user details", Toast.LENGTH_SHORT).show();
                    }
            );

            queue.add(jsonObjectRequest);
        }
    }
}
