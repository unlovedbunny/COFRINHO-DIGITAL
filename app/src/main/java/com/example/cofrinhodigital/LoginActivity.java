package com.example.cofrinhodigital;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Se já está logado, vai direto para o fluxo principal
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail    = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        progressBar = findViewById(R.id.progress_bar_login);

        Button btnLogin    = findViewById(R.id.btn_login);
        TextView tvRegister = findViewById(R.id.tv_go_register);

        btnLogin.setOnClickListener(v -> doLogin());

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String pass  = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Informe o e-mail");
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            etPassword.setError("Informe a senha");
            return;
        }
        if (pass.length() < 6) {
            etPassword.setError("A senha deve ter ao menos 6 caracteres");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        goToMain();
                    } else {
                        String errorCode = "";
                        if (task.getException() instanceof com.google.firebase.auth.FirebaseAuthException) {
                            errorCode = ((com.google.firebase.auth.FirebaseAuthException)
                                    task.getException()).getErrorCode();
                        }
                        String msg;
                        switch (errorCode) {
                            case "ERROR_USER_NOT_FOUND":
                            case "ERROR_WRONG_PASSWORD":
                            case "ERROR_INVALID_CREDENTIAL":
                                msg = "E-mail ou senha incorretos.";
                                break;
                            case "ERROR_INVALID_EMAIL":
                                msg = "E-mail inválido.";
                                break;
                            case "ERROR_USER_DISABLED":
                                msg = "Conta desativada.";
                                break;
                            case "ERROR_TOO_MANY_REQUESTS":
                                msg = "Muitas tentativas. Tente novamente mais tarde.";
                                break;
                            default:
                                msg = "Erro ao fazer login. Tente novamente.";
                                break;
                        }
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /** Decide para onde ir após o login bem-sucedido */
    private void goToMain() {
        SavingsManager sm = new SavingsManager(this);
        Intent intent = sm.hasGoal()
                ? new Intent(this, MainActivity.class)
                : new Intent(this, SetupActivity.class);
        startActivity(intent);
        finish();
    }
}
