package com.VasuIonut.aplicatiesportiva;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MyProfileActivity extends AppCompatActivity {
    private ImageView userImage;
    private TextView usernameTextView;
    private TextView descriptionTextView;
    private TextView ageTextView;
    private TextView genderTextView;
    private Button editButton;
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myprofile);

        userId = getUserId();  // Încarcă ID-ul utilizatorului o singură dată

        // Inițializează UI și setează listeneri
        setupUI();
        loadUserProfile(userId);
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Reîncarcă profilul utilizatorului pentru a reflecta orice modificări recente
        String userId = getUserId();
        if (userId != null) {
            loadUserProfile(userId);
        } else {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupUI() {
        userImage = findViewById(R.id.profile_image);
        usernameTextView = findViewById(R.id.username);
        descriptionTextView = findViewById(R.id.description);
        ageTextView = findViewById(R.id.varsta);
        genderTextView = findViewById(R.id.gender);
        editButton = findViewById(R.id.edit_profile_button);
    }
    public String getUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("userId", null); // returnează null dacă nu există valoare salvată
    }

    private void loadUserProfile(String userId) {
        String url = "http://10.0.2.2:3000/user/" + userId;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String username = response.getString("username");
                            JSONObject profileInfo = response.getJSONObject("profileInfo");
                            String description = profileInfo.getString("description");
                            int age = profileInfo.getInt("age");
                            String gender = profileInfo.getString("gender");
                            String base64Image = profileInfo.getString("photo"); // preia șirul Base64 al imaginii

                            // Set the text of your TextViews
                            usernameTextView.setText(username);
                            descriptionTextView.setText(description);
                            ageTextView.setText(String.valueOf(age));
                            genderTextView.setText(gender);

                            // Convert the Base64 string back into a Bitmap to display
                            if (!base64Image.isEmpty()) {
                                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                userImage.setImageBitmap(decodedByte);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MyProfileActivity.this, "Error parsing user data", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MyProfileActivity.this, "Error fetching user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }
}
