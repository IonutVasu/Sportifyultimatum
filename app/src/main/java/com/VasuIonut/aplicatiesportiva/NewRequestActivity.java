package com.VasuIonut.aplicatiesportiva;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewRequestActivity extends AppCompatActivity {

    private Spinner sportSpinner, locationSpinner;
    private EditText numPlayersEditText, descriptionEditText;
    private RadioGroup difficultyRadioGroup;
    private Button submitRequestButton;
    private String[] sports;
    private String selectedDate;
    private CalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newrequest);

        sportSpinner = findViewById(R.id.sportSpinner);
        locationSpinner = findViewById(R.id.locationSpinner);
        numPlayersEditText = findViewById(R.id.numPlayersEditText);
        difficultyRadioGroup = findViewById(R.id.difficultyRadioGroup);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        calendarView = findViewById(R.id.calendarView);
        submitRequestButton = findViewById(R.id.submitRequestButton);

        setupSportSpinner();  // Populează sportSpinner cu datele de la server

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            }
        });

        submitRequestButton.setOnClickListener(v -> sendSportPreferences());
    }

    private void setupSportSpinner() {
        String url = "http://10.0.2.2:3000/sports/availableSports"; // Asumând că acesta este endpointul pe server

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Preia array-ul de sporturi din obiectul JSON sub cheia "availableSports"
                        JSONArray sportsArray = response.getJSONArray("availableSports");
                        sports = new String[sportsArray.length()];
                        for (int i = 0; i < sportsArray.length(); i++) {
                            sports[i] = sportsArray.getString(i);
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(NewRequestActivity.this, android.R.layout.simple_spinner_dropdown_item, sports);
                        sportSpinner.setAdapter(adapter);
                    } catch (JSONException e) {
                        Toast.makeText(NewRequestActivity.this, "Error parsing sports data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }, error -> Toast.makeText(NewRequestActivity.this, "Failed to fetch sports: " + error.getMessage(), Toast.LENGTH_SHORT).show());

        queue.add(jsonObjectRequest);

        // Setează listenerul pentru când este selectat un sport
        sportSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < sports.length) { // Verifică să fie o poziție validă
                    updateLocationSpinner(sports[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateLocationSpinner(String sport) {
        String[] locations;
        switch (sport) {
            case "Fotbal":
                locations = new String[]{"Tineretului", "Herastrau", "Stadionul Național"};
                break;
            case "Volei":
                locations = new String[]{"Sala Polivalenta", "Parc Tineretului"};
                break;
            default:
                locations = new String[]{"General"};
                break;
        }
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, locations);
        locationSpinner.setAdapter(locationAdapter);
    }

    private String getUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("userId", null);
    }

    private void sendSportPreferences() {
        String userId = getUserId(); // Presupunem că aceasta este metoda care obține ID-ul utilizatorului
        if (userId == null) {
            Toast.makeText(this, "User ID not available", Toast.LENGTH_SHORT).show();
            return;
        }

        String sport = sportSpinner.getSelectedItem().toString();
        String location = locationSpinner.getSelectedItem().toString();
        String numPlayers = numPlayersEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        int selectedDifficultyId = difficultyRadioGroup.getCheckedRadioButtonId();
        if (selectedDifficultyId == -1) {
            Toast.makeText(this, "Please select a difficulty level", Toast.LENGTH_SHORT).show();
            return;
        }

        String difficulty = ((RadioButton) findViewById(selectedDifficultyId)).getText().toString();

        String url = "http://10.0.2.2:3000/sports/newSportRequest";
        RequestQueue queue = Volley.newRequestQueue(this);
        Map<String, String> params = new HashMap<>();
        params.put("sport", sport);
        params.put("location", location);
        params.put("playersNeeded", numPlayers);
        params.put("description", description);
        params.put("difficultyLevel", difficulty);
        params.put("date", selectedDate); // Adăugăm data în parametri
        params.put("user", userId); // Adăugăm ID-ul utilizatorului în parametri

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                response -> {
                    Toast.makeText(NewRequestActivity.this, "Request submitted successfully!", Toast.LENGTH_SHORT).show();
                    fetchMatchingUsers(); // Fetch matching users
                },
                error -> Toast.makeText(NewRequestActivity.this, "Failed to submit request: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    private void fetchMatchingUsers() {
        String url = "http://10.0.2.2:3000/match/matchingUsers";
        RequestQueue queue = Volley.newRequestQueue(this);
        String userId = getUserId();

        Map<String, String> params = new HashMap<>();
        params.put("sport", sportSpinner.getSelectedItem().toString());
        params.put("location", locationSpinner.getSelectedItem().toString());
        params.put("playersNeeded", numPlayersEditText.getText().toString());
        params.put("difficultyLevel", ((RadioButton) findViewById(difficultyRadioGroup.getCheckedRadioButtonId())).getText().toString().trim());
        params.put("date", selectedDate); // Adăugăm data în parametri
        params.put("userId", userId);

        Log.d("FetchMatchingUsers", "Sending request with parameters: " + params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                response -> {
                    Log.d("FetchMatchingUsers", "Received response: " + response.toString());
                    try {
                        if (response.has("matchingUsers")) {
                            JSONArray usersArray = response.getJSONArray("matchingUsers");
                            ArrayList<String> matchingUserIds = new ArrayList<>();
                            for (int i = 0; i < usersArray.length(); i++) {
                                matchingUserIds.add(usersArray.getJSONObject(i).getString("_id"));
                            }
                            launchUserProfilesActivity(matchingUserIds);
                        } else {
                            Log.e("FetchMatchingUsers", "No matchingUsers key in the response");
                            Toast.makeText(this, "No matching users found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("FetchMatchingUsers", "Error parsing matching users: " + e.getMessage(), e);
                        Toast.makeText(this, "Error parsing matching users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            Log.e("FetchMatchingUsers", "Volley error: " + error.toString());
            Toast.makeText(this, "Error fetching matching users", Toast.LENGTH_SHORT).show();
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


    private void launchUserProfilesActivity(ArrayList<String> matchingUserIds) {
        Intent intent = new Intent(this, MatchesUsersActivity.class);
        intent.putStringArrayListExtra("matchingUserIds", matchingUserIds);
        startActivity(intent);
    }
}
