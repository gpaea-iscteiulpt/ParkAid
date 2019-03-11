package com.tese.parkaid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView txtRegister = (TextView) findViewById(R.id.txtRegister);
        String str = "Register new account <b> here </b>";
        txtRegister.setText(Html.fromHtml(str));
    }

    public void loginUser(View view){
        mAuth = FirebaseAuth.getInstance();


        Constants.setUsername("");
        Constants.setUserPoints(0);
        Intent intent = new Intent(this, Menu.class);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
    }

    public void registerUser(){
        Intent intent = new Intent(this, Menu.class);
        startActivity(intent);
    }

}
