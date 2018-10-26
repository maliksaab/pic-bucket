package com.taimoorhaider.www.cloudtryagain;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserProfileDetails extends AppCompatActivity {

    EditText editText_Name;
    EditText editText_Phone;
    EditText editText_Age;
    EditText editText_City;
    EditText editText_Country;
    private String userAuthPhone;
    private String userAuthName;
    private String userAuthEmail;


    DocumentReference docRef;
    private String completeFirebasePath;
    private Map<String, Object> userProfileDataFromCloud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_details);

        Intent intent = getIntent();
        userAuthEmail = intent.getStringExtra("userEmail");
//        userAuthName = intent.getStringExtra("userName");
//        userAuthPhone = intent.getStringExtra("userPhone");


        getEditTextsHandles();

        completeFirebasePath = "/" + Gallery.FireBaseRootDirectory_Profiles + "/" + userAuthEmail;
        docRef = FirebaseFirestore.getInstance().document(completeFirebasePath);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ///     OLD user
                        userProfileDataFromCloud = document.getData();
                        setEditTexts();
                    } else {
                        ///     NEW user
                        Toast.makeText(UserProfileDetails.this, "ERROR: Fail To Fetch Data From Cloud", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    //         Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


    }

    private void setEditTexts() {
        if (!(
                userProfileDataFromCloud.get("age") == "" &&
                        userProfileDataFromCloud.get("city") == "" &&
                        userProfileDataFromCloud.get("contactNum") == "" &&
                        userProfileDataFromCloud.get("country") == "" &&
                        userProfileDataFromCloud.get("name") == ""
        )) {
            editText_Name.setText(userProfileDataFromCloud.get("name").toString());
            editText_Phone.setText(userProfileDataFromCloud.get("contactNum").toString());
            editText_Age.setText(userProfileDataFromCloud.get("age").toString());
            editText_City.setText(userProfileDataFromCloud.get("city").toString());
            editText_Country.setText(userProfileDataFromCloud.get("country").toString());
        }
        editText_Name.setEnabled(false);
        editText_Phone.setEnabled(false);
        editText_Age.setEnabled(false);
        editText_City.setEnabled(false);
        editText_Country.setEnabled(false);

    }

    public void EnableEdit(View view) {
        editText_Name.setEnabled(true);
        editText_Phone.setEnabled(true);
        editText_Age.setEnabled(true);
        editText_City.setEnabled(true);
        editText_Country.setEnabled(true);
        Toast.makeText(this,"Editing Enabled, Press Update after making Changes",Toast.LENGTH_LONG).show();
    }
    public void SaveEdits(View view) {
        editText_Name.setEnabled(false);
        editText_Phone.setEnabled(false);
        editText_Age.setEnabled(false);
        editText_City.setEnabled(false);
        editText_Country.setEnabled(false);

        Map<String, Object> newUserProfileSetup = new HashMap<String, Object>();
        newUserProfileSetup.put("age", editText_Age.getText().toString());
        newUserProfileSetup.put("city", editText_City.getText().toString());
        newUserProfileSetup.put("contactNum", editText_Phone.getText().toString());
        newUserProfileSetup.put("country" , editText_Country.getText().toString());
        newUserProfileSetup.put("name", editText_Name.getText().toString());
        docRef.set(newUserProfileSetup).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Success! Cloud Data also Updated ", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "ERROR: Fail To Update Profile on Cloud", Toast.LENGTH_SHORT).show();
            }
        });
    }


    void getEditTextsHandles() {
        editText_Name = findViewById(R.id.user_name_profile);
        editText_Phone = findViewById(R.id.user_number_profile);
        editText_Age = findViewById(R.id.user_age_profile);
        editText_City = findViewById(R.id.user_city_profile);
        editText_Country = findViewById(R.id.user_country_profile);
    }
}
