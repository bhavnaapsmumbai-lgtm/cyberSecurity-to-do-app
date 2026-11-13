package com.example.to_do_list;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText emailInput, passwordInput;
    Button loginButton, registerButton, forgotPasswordButton;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        // If already logged in, skip straight to the to-do list
        if (auth.getCurrentUser() != null) {
            openTodoList();
            return;
        }

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);

        loginButton.setOnClickListener(v -> doLogin());

        // Pressing Enter / Done in the password box also logs in
        passwordInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean enterPressed = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            if (actionId == EditorInfo.IME_ACTION_DONE || enterPressed) {
                doLogin();
                return true;
            }
            return false;
        });

        registerButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            if (!isValid(email, password)) return;

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(result -> openTodoList())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Could not create account", Toast.LENGTH_LONG).show());
        });

        forgotPasswordButton.setOnClickListener(v -> sendResetEmail());
    }

    private void doLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (!isValid(email, password)) return;

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> openTodoList())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Login failed", Toast.LENGTH_LONG).show());
    }

    private void sendResetEmail() {
        String email = emailInput.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter your email first");
            return;
        }

        // The same message is shown whether or not the email is registered,
        // so the app does not reveal which emails have accounts
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task ->
                        Toast.makeText(this,
                                "If an account exists for that email, a reset link has been sent",
                                Toast.LENGTH_LONG).show());
    }

    private boolean isValid(String email, String password) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email");
            return false;
        }
        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            return false;
        }
        return true;
    }

    private void openTodoList() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}