package com.taimoorhaider.www.cloudtryagain;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class new_diary_post extends AppCompatActivity {

    ArrayList<holder> files_ToBe_Uploaded_As_InpStreams;
    ArrayList<UploadTask.TaskSnapshot> Uploaded_As_URLS;
    private static final int PICK_PHOTO_FOR_GALLERY = 4584;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    // Create a storage reference from our app
    StorageReference storageRef = storage.getReference();
    String path_of_storage = "user_data/";
    int item_selected_count = 0;

    TextView selectedTV, LocationTV;
    EditText postTitle, postDescription;
    Button browseGalleryBtn, locationUpdateBtn, postApostBtn;
    ProgressBar progressBar;
    RatingBar ratings_of_this_post;
    TextView category_of_post_tv;

    LocationManager locationManager;
    private String provider;

    Location universalLoc;

    String FireBaseRootDirectory_Profiles = "profiles";
    String FireBaseRootDirectory_Posts = "all_posts";

    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    CollectionReference collectionRef;

    String categoryOfPostFromParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_diary_post);

        Intent intent = getIntent();
        categoryOfPostFromParent = intent.getStringExtra("selectedCategory");
        files_ToBe_Uploaded_As_InpStreams = new ArrayList<holder>();
        Uploaded_As_URLS = new ArrayList<UploadTask.TaskSnapshot>();

        selectedTV = findViewById(R.id.new_diary_post_selected_items_tv);
        LocationTV = findViewById(R.id.new_diary_post_current_city_tv);

        postTitle = findViewById(R.id.post_title_tv);
        postDescription = findViewById(R.id.post_discription_tv);

        browseGalleryBtn = findViewById(R.id.imageButton);
        ;
        locationUpdateBtn = findViewById(R.id.locationUpdateButton);
        postApostBtn = findViewById(R.id.post_the_post);

        progressBar = findViewById(R.id.progressBar2);

        ratings_of_this_post = findViewById(R.id.ratings_of_this_post);
        category_of_post_tv = findViewById(R.id.category_of_post);

        category_of_post_tv .setText(categoryOfPostFromParent + " Post");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
        callForLocation();
    }


    void callForLocation() {
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                if (location != null) {
                    String cityName = null;
                    Geocoder gcd = new Geocoder(getBaseContext(),
                            Locale.getDefault());
                    List<Address> addresses;
                    try {
                        universalLoc = location;
                        addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses.size() > 0)
                            System.out.println(addresses.get(0).getLocality());
                        cityName = addresses.get(0).getLocality();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    LocationTV.setText(cityName);
                } else {
                    LocationTV.setText("Failed!");
                }
            }
        };
        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(this, locationResult);
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void BrowseGallery_Clicked(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_PHOTO_FOR_GALLERY);

    }

    private void publishPostOnFireBase(final String postID) {
        String choice = null;
        if (categoryOfPostFromParent.equals("With-In City")){
            choice = "with_in_city";
        }else if (categoryOfPostFromParent.equals("Out-Of City")){
            choice = "out_of_city";
        }else if (categoryOfPostFromParent.equals("Other Country")){
            choice = "other_country";
        }
        //      c       d      c    d       c       d
        //  /profiles/email/posts/with-in/postID/post
        DocumentReference documentRef = FirebaseFirestore.getInstance().collection("profiles").
                document(currentUser.getEmail()).collection("posts").document(choice);//.document(userPath);        ////// userPath

        Map<String, Object> newUserPostSetup = new HashMap<String, Object>();
        newUserPostSetup.put(postID, "");                                  ///// profiles/taimoor.h.aslam@gmail.com/posts/postID

        documentRef.collection(postID).document(postID).set(newUserPostSetup).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Toast.makeText(getApplicationContext(), "Success! Cloud Data also Updated ", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "ERROR: Fail To Update Profile on Cloud", Toast.LENGTH_SHORT).show();
            }
        });


        DocumentReference docRef = FirebaseFirestore.getInstance().collection(FireBaseRootDirectory_Posts).document(postID);        ////// userPath
        Map<String, Object> universalPostSetup = new HashMap<String, Object>();

        universalPostSetup.put("post_id", postID);
        universalPostSetup.put("title", postTitle.getText().toString().trim());
        universalPostSetup.put("description", postDescription.getText().toString().trim());
        universalPostSetup.put("longitude", universalLoc.getLongitude());
        universalPostSetup.put("latitude", universalLoc.getLongitude());
        universalPostSetup.put("rating", Float.toString(ratings_of_this_post.getRating()));
        universalPostSetup.put("user_email", currentUser.getEmail());

        docRef.set(universalPostSetup).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(new_diary_post.this, "ERROR: Fail To Fetch Data From Cloud", Toast.LENGTH_SHORT).show();
            }
        });


//        Map<String, Object> URLSPostSetup = new HashMap<String, Object>();
//        for (UploadTask.TaskSnapshot ts : Uploaded_As_URLS) {
//            URLSPostSetup.put(ts.getDownloadUrl().toString(), "");
//        }
//        docRef.collection("mediaURLS").document(postID).set(URLSPostSetup).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(new_diary_post.this, "ERROR: Fail To Fetch Data From Cloud", Toast.LENGTH_SHORT).show();
//            }
//        });

    }

    public void MakeAPost_Clicked(View view) {
        if (postTitle.getText().toString().trim().equals("") || postDescription.getText().toString().trim().equals("")) {
            Toast.makeText(new_diary_post.this, "Text Fields are Empty!", Toast.LENGTH_SHORT).show();
            return;
        } else if (selectedTV.getText().toString().trim().equals("0 selected")) {
            Toast.makeText(new_diary_post.this, "Attach some media!", Toast.LENGTH_SHORT).show();
            return;
        } else if (LocationTV.getText().toString().trim().equals("No Location")) {
            Toast.makeText(new_diary_post.this, "Post Location Unknown!", Toast.LENGTH_SHORT).show();
            return;
        }

        browseGalleryBtn.setEnabled(false);
        locationUpdateBtn.setEnabled(false);
        postApostBtn.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        final String postID = UUID.randomUUID().toString();

        for (holder eachStream : files_ToBe_Uploaded_As_InpStreams) {
            StorageReference ImagesRef = storageRef.child(path_of_storage + UUID.randomUUID() + eachStream.uri.getLastPathSegment());
            UploadTask uploadTask = ImagesRef.putStream(eachStream.is);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(), "Failed Upload!", Toast.LENGTH_SHORT);
                }
            }).addOnSuccessListener(new_diary_post.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getApplicationContext(), "Upload Success!", Toast.LENGTH_SHORT);
                    Uploaded_As_URLS.add(taskSnapshot); //contains file metadata such as size, content-type, etc.
                    progressBar.setVisibility(View.GONE);

                    DocumentReference docRef = FirebaseFirestore.getInstance().collection(FireBaseRootDirectory_Posts).document(postID);        ////// userPath
                    Map<String, Object> URLSPostSetup = new HashMap<String, Object>();
                    for (UploadTask.TaskSnapshot ts : Uploaded_As_URLS) {
                        URLSPostSetup.put(ts.getDownloadUrl().toString(), "");
                    }
                    docRef.collection("mediaURLS").document(postID).set(URLSPostSetup).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(new_diary_post.this, "ERROR: Fail To Fetch Data From Cloud", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Toast.makeText(getApplicationContext(), "Success! Cloud Data also Updated ", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }

        publishPostOnFireBase(postID);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FOR_GALLERY && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(this, "Error opening Gallery", Toast.LENGTH_SHORT);
                return;
            }
            item_selected_count++;
            selectedTV.setText(item_selected_count + " selected");
            InputStream data_as_InputStream = null;
            Uri selectedImage = data.getData();

            try {
                data_as_InputStream = getApplicationContext().getContentResolver().openInputStream(selectedImage);
                holder holder_obj = new holder(data_as_InputStream, selectedImage);
                files_ToBe_Uploaded_As_InpStreams.add(holder_obj);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void RefreshLocation_Clicked(View view) {
        callForLocation();
    }
}

class holder {
    InputStream is;
    Uri uri;

    holder(InputStream _is, Uri _uri) {
        this.is = _is;
        this.uri = _uri;
    }
}

class MyLocation {
    Timer timer1;
    LocationManager lm;
    LocationResult locationResult;
    boolean gps_enabled = false;
    boolean network_enabled = false;

    @SuppressLint("MissingPermission")
    public boolean getLocation(Context context, LocationResult result) {
        //I use LocationResult callback class to pass location value from MyLocation to user code.
        locationResult = result;
        if (lm == null)
            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        //don't start listeners if no provider is enabled
        if (!gps_enabled && !network_enabled)
            return false;

        if (gps_enabled)
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
        if (network_enabled)
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
        timer1 = new Timer();
        timer1.schedule(new GetLastLocation(), 10000);
        return true;
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer1.cancel();
            locationResult.gotLocation(location);
            lm.removeUpdates(this);
            lm.removeUpdates(locationListenerNetwork);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer1.cancel();
            locationResult.gotLocation(location);
            lm.removeUpdates(this);
            lm.removeUpdates(locationListenerGps);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    class GetLastLocation extends TimerTask {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            lm.removeUpdates(locationListenerGps);
            lm.removeUpdates(locationListenerNetwork);

            Location net_loc = null, gps_loc = null;
            if (gps_enabled)
                gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (network_enabled)
                net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            //if there are both values use the latest one
            if (gps_loc != null && net_loc != null) {
                if (gps_loc.getTime() > net_loc.getTime())
                    locationResult.gotLocation(gps_loc);
                else
                    locationResult.gotLocation(net_loc);
                return;
            }
            if (gps_loc != null) {
                locationResult.gotLocation(gps_loc);
                return;
            }
            if (net_loc != null) {
                locationResult.gotLocation(net_loc);
                return;
            }
            locationResult.gotLocation(null);
        }
    }

    public static abstract class LocationResult {
        public abstract void gotLocation(Location location);
    }
}