package com.taimoorhaider.www.cloudtryagain;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import android.app.Fragment;

import java.util.HashMap;
import java.util.Map;

public class Gallery extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;

    static final String FireBaseRootDirectory_Profiles = "profiles";

    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    DocumentReference docRef;
    static String completeFirebasePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Intent intent = getIntent();
       // final String userAuthEmail = intent.getStringExtra("userEmail");

        final String userAuthEmail = currentUser.getEmail();

        completeFirebasePath = "/" + FireBaseRootDirectory_Profiles + "/" + userAuthEmail;

        //        final String userAuthName = intent.getStringExtra("userName");
        //        final String userAuthPhone = intent.getStringExtra("userPhone");

        makeFireStoreUserProfile();

        setActionBarDrawer();

        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        mDrawerLayout.addDrawerListener(
                new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        // Respond when the drawer's position changes
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Respond when the drawer is opened
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        // Respond when the drawer is closed
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                        // Respond when the drawer motion state changes
                    }
                }
        );
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        switch (menuItem.getItemId()) {
                            case R.id.nav_profile: {
                                Intent intent = new Intent(getApplicationContext(), UserProfileDetails.class);
                                intent.putExtra("userEmail", userAuthEmail);
                                startActivity(intent);

//intent.putExtra("userName", userAuthName);
//intent.putExtra("userPhone", userAuthPhone);
                            }
                            break;
                            case R.id.nav_dairy: {
                                Intent intent = new Intent(getApplicationContext(), DiaryParentActivity.class);
                                startActivity(intent);

//intent.putExtra("userName", userAuthName);
//intent.putExtra("userPhone", userAuthPhone);
                            }
                            break;

                            case R.id.nav_locations: {
                                Intent intent = new Intent(getApplicationContext(), Locations.class);
                                startActivity(intent);

//intent.putExtra("userName", userAuthName);
//intent.putExtra("userPhone", userAuthPhone);
                            }
                            break;

                            default:
                                break;
                        }
                        return true;
                    }
                });

    }

    private void makeFireStoreUserProfile() {
        docRef = FirebaseFirestore.getInstance().document(completeFirebasePath);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {
                        ///     NEW user
                        Map<String, Object> newUserProfileSetup = new HashMap<String, Object>();
                        newUserProfileSetup.put("age", "");
                        newUserProfileSetup.put("city", "");
                        newUserProfileSetup.put("contactNum", "");
                        newUserProfileSetup.put("country", "");
                        newUserProfileSetup.put("name", "");
                        docRef.set(newUserProfileSetup).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Gallery.this, "ERROR: Fail To Fetch Data From Cloud", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    //         Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void setActionBarDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayShowTitleEnabled(false);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
