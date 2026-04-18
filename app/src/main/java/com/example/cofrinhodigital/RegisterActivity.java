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

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etPasswordConfirm;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        etEmail           = findViewById(R.id.et_email_reg);
        etPassword        = findViewById(R.id.et_password_reg);
        etPasswordConfirm = findViewById(R.id.et_password_confirm);
        progressBar       = findViewById(R.id.progress_bar_register);

        Button   btnRegister = findViewById(R.id.btn_register);
        TextView tvLogin     = findViewById(R.id.tv_go_login);

        btnRegister.setOnClickListener(v -> doRegister());
        tvLogin.setOnClickListener(v -> finish()); // volta para LoginActivity
    }

    private void doRegister() {
        String email   = etEmail.getText().toString().trim();
        String pass    = etPassword.getText().toString().trim();
        String confirm = etPasswordConfirm.getText().toString().trim();

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
        if (!pass.equals(confirm)) {
            etPasswordConfirm.setError("As senhas não coincidem");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();
                        // Novo usuário → sempre vai para SetupActivity criar a meta
                        startActivity(new Intent(this, SetupActivity.class));
                        finish();
                    } else {
                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Erro ao criar conta";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
