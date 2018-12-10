package com.taimoorhaider.www.cloudtryagain;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Locations extends AppCompatActivity {

    FirebaseStorage storage = FirebaseStorage.getInstance();
    // Create a storage reference from our app
    StorageReference storageRef = storage.getReference();

    String FireBaseRootDirectory_Posts = "all_posts";
    String completeFirebasePath;
    ArrayList< HashMap<String,Object> > allpostsInfos = new ArrayList<>();
    HashMap<String,Object> userPostsDataFromCloud = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);

//        completeFirebasePath = "/" + FireBaseRootDirectory_Posts + "/";
//        DocumentReference docRef = FirebaseFirestore.getInstance().document(completeFirebasePath);
//        docRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        ///     OLD user
//                        userProfileDataFromCloud = document.getData();
//
//                    } else {
//                        ///     NEW user
//                        Toast.makeText(Locations.this, "ERROR: Fail To Fetch Data From Cloud", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    //         Log.d(TAG, "get failed with ", task.getException());
//                }
//            }
//        });
//
        completeFirebasePath = "/" + FireBaseRootDirectory_Posts ;
        CollectionReference collectionRef = FirebaseFirestore.getInstance().collection(completeFirebasePath);
        collectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                userPostsDataFromCloud  = (HashMap<String, Object>) document.getData();

                                allpostsInfos.add(userPostsDataFromCloud );
                            }
                        } else {
                            //Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                            ;

        });
    }
}
