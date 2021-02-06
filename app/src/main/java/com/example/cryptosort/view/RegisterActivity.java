package com.example.cryptosort.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cryptosort.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import database.User;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;

    private ProgressBar progressBar;
    private EditText editTextEmail, editTextPassword, editTextRePassword;
    private Button registerButton, registerGoback;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        registerGoback = (Button) findViewById(R.id.registerGoback);
        registerGoback.setOnClickListener(this);

        registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(this);

        editTextEmail = (EditText) findViewById(R.id.registerEmail);
        editTextPassword = (EditText) findViewById(R.id.registerPassword);
        editTextRePassword = (EditText) findViewById(R.id.registerRePassword);

        progressBar = (ProgressBar) findViewById(R.id.registerProgressbar);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.registerGoback:
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case R.id.registerButton:
                registerUser();
                break;
        }
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String repassword = editTextRePassword.getText().toString().trim();

        if (email.isEmpty()){
            editTextEmail.setError("Email is Required!");
            editTextEmail.requestFocus();
            return;
        }
        if (password.isEmpty()){
            editTextPassword.setError("Password is Required!");
            editTextPassword.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Invalid Email!");
            editTextEmail.requestFocus();
            return;
        }
        if (!password.matches("") && repassword.isEmpty()){
            editTextRePassword.setError("Repeat Password is Required!");
            editTextRePassword.requestFocus();
            return;
        }
        if (password.length() < 6){
            editTextPassword.setError("Password Should Be Min 6 Characters.");
            editTextPassword.requestFocus();
            return;
        }
        if (!password.matches(repassword)){
            editTextPassword.setError("Passwords Doesn't Match!");
            editTextRePassword.setError("Passwords Doesn't Match!");
            editTextRePassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            User user = new User(email);

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(RegisterActivity.this, "You Have Successfully Registered!", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                    }else{
                                        Toast.makeText(RegisterActivity.this, "Failed To Register! Please Try Again.", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(RegisterActivity.this, "Failed To Register! Please Try Again.", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }
}