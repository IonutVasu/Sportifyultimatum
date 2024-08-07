package com.VasuIonut.aplicatiesportiva;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private ImageView editProfileImageView;
    private EditText editUsernameEditText, editDescriptionEditText, editAgeEditText;
    private Button editProfileImageButton, saveChangesButton;
    private String base64Image;
    private RadioGroup genderRadioGroup;
    private String userId; // ID-ul utilizatorului

    private ActivityResultLauncher<Intent> mStartForResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);

        editProfileImageView = findViewById(R.id.editProfileImageView);
        editUsernameEditText = findViewById(R.id.editUsernameEditText);
        editDescriptionEditText = findViewById(R.id.editDescriptionEditText);
        editAgeEditText = findViewById(R.id.editAgeEditText);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        editProfileImageButton = findViewById(R.id.editProfileImageButton);
        saveChangesButton = findViewById(R.id.saveChangesButton);

        // Preia ID-ul utilizatorului transmis prin Intent
        userId = getIntent().getStringExtra("USER_ID");

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID not provided", Toast.LENGTH_SHORT).show();
            finish(); // Încheie activitatea dacă nu există un ID valid
        } else {
            loadUserProfile(); // Încarcă profilul utilizatorului
        }


        editProfileImageButton.setOnClickListener(v -> dispatchTakePictureIntent());

        saveChangesButton.setOnClickListener(v -> saveUserProfile());

        mStartForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        editProfileImageView.setImageBitmap(imageBitmap);

                        // Convert the Bitmap to a Base64 string
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
                    }
                }
        );
    }

    private void loadUserProfile() {
        String url = "http://10.0.2.2:3000/user/" + userId;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String username = response.getString("username");
                        JSONObject profileInfo = response.getJSONObject("profileInfo");

                        String description = profileInfo.optString("description", "");
                        int age = profileInfo.optInt("age", 0);
                        String gender = profileInfo.optString("gender", "");
                        String photo = profileInfo.getString("photo");

                        editUsernameEditText.setText(username);
                        editDescriptionEditText.setText(description);
                        editAgeEditText.setText(String.valueOf(age));
                        // Setează genul curent dacă este disponibil
                        if (gender != null) {
                            if (gender.equals("Bărbat")) {
                                genderRadioGroup.check(R.id.radioMale);
                            } else if (gender.equals("Femeie")) {
                                genderRadioGroup.check(R.id.radioFemale);
                            }
                        }

                        if (!photo.isEmpty()) {
                            byte[] decodedString = Base64.decode(photo, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            editProfileImageView.setImageBitmap(decodedByte);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing user data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error fetching user data: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        queue.add(jsonObjectRequest);
    }

    private void saveUserProfile() {
        String url = "http://10.0.2.2:3000/updateUser/" + userId;

        RequestQueue queue = Volley.newRequestQueue(this);

        Map<String, String> params = new HashMap<>();
        params.put("username", editUsernameEditText.getText().toString().trim());
        params.put("description", editDescriptionEditText.getText().toString().trim());
        params.put("age", editAgeEditText.getText().toString().trim());
        params.put("photo", base64Image);
        // Extrage genul selectat din RadioGroup
        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
        if (selectedGenderId != -1) {
            RadioButton selectedGenderRadioButton = findViewById(selectedGenderId);
            String selectedGender = selectedGenderRadioButton.getText().toString();
            params.put("gender", selectedGender);
        }
        JSONObject jsonParams = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonParams,
                response -> {
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Acest apel finalizează EditProfileActivity și ar trebui să te întoarcă la MyProfileActivity
                }, error -> {
            Toast.makeText(EditProfileActivity.this, "Error updating profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            mStartForResult.launch(takePictureIntent);
        }
    }
}
