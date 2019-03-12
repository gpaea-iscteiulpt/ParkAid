package com.tese.parkaid;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Register extends AppCompatActivity {

    private EditText editUsername, editEmail, editPassword, editConfirmPassword;
    private Button buttonRegister;

    private FirebaseAuth mAuth;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editUsername = findViewById(R.id.username);
        editEmail = findViewById(R.id.email);
        editPassword = findViewById(R.id.password);
        editConfirmPassword = findViewById(R.id.confirmPassword);

        buttonRegister = findViewById(R.id.buttonRegister);

        mAuth = FirebaseAuth.getInstance();

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = editUsername.getText().toString();
                final String email = editEmail.getText().toString();
                final String password = editPassword.getText().toString();
                final String confirmPassword = editConfirmPassword.getText().toString();

                if(email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || !password.equals(confirmPassword)){
                    showMessage("Please verify all fields.");
                }else{
                    mUser = new User();
                    mUser.setEmail(email);
                    mUser.setPoints(0);
                    mUser.setUsername(username);
                    createUserAccount(username, email, password);
                }
            }
        });
    }

    private void createUserAccount(final String username, final String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    updateUserInfo(username, email, mAuth.getCurrentUser());
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    startActivity(intent);
                    showMessage("Account was created!");
                }else{
                    showMessage("Error: " + task.getException().getMessage());
                }
            }
        });
    }

    private void updateUserInfo(String username, String email, FirebaseUser currentUser){
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users");
        myRef.child(email.replace(".", "")).setValue(mUser);
        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                .setDisplayName(username).build();
        currentUser.updateProfile(profileUpdate);
    }

    private void showMessage(String str){
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }
}
