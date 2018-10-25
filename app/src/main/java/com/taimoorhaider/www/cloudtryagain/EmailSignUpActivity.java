package com.taimoorhaider.www.cloudtryagain;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailSignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_sign_up);
        mAuth = FirebaseAuth.getInstance();

    }

    public void SignMeUp(View view){
        TextView email = (TextView)findViewById(R.id.EmailSignUp_email_field);
        TextView password = (TextView )findViewById(R.id.EmailSignUp_password_field);

        Toast.makeText(this,"Email se Login Ho",Toast.LENGTH_LONG).show();
        String em = email.getText().toString().trim();
        String ps = password.getText().toString();

        mAuth.createUserWithEmailAndPassword(em, ps)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(getApplication(), "Authentication Success.", Toast.LENGTH_SHORT).show();
                            if(user != null){    //user already signed in

                            }
                        } else {
                            // If sign in fails, display a message to the user.
//                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getApplication(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

}
