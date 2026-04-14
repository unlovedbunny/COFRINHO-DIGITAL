package com.example.cofrinhodigital;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class SetupActivity extends AppCompatActivity {

    private String selectedModality = "Economia Diária";
    private MaterialCardView cardDiaria, cardSemanal, cardCorte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        EditText etName  = findViewById(R.id.et_goal_name);
        EditText etValue = findViewById(R.id.et_goal_value);
        EditText etDays  = findViewById(R.id.et_days);
        Button   btnSave = findViewById(R.id.btn_save_goal);

        cardDiaria  = findViewById(R.id.card_diaria);
        cardSemanal = findViewById(R.id.card_semanal);
        cardCorte   = findViewById(R.id.card_corte);

        highlightCard(cardDiaria);

        cardDiaria.setOnClickListener(v  -> { selectedModality = "Economia Diária";    highlightCard(cardDiaria); });
        cardSemanal.setOnClickListener(v -> { selectedModality = "Desafio Semanal";    highlightCard(cardSemanal); });
        cardCorte.setOnClickListener(v   -> { selectedModality = "Corte de Gastos";    highlightCard(cardCorte); });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String valStr = etValue.getText().toString().trim();
            String daysStr = etDays.getText().toString().trim();

            if (name.isEmpty() || valStr.isEmpty() || daysStr.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            double value = Double.parseDouble(valStr);
            int days = Integer.parseInt(daysStr);

            if (value <= 0 || days <= 0) {
                Toast.makeText(this, "Valores inválidos!", Toast.LENGTH_SHORT).show();
                return;
            }

            new SavingsManager(this).saveGoal(name, value, days, selectedModality);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void highlightCard(MaterialCardView selected) {
        for (MaterialCardView c : new MaterialCardView[]{cardDiaria, cardSemanal, cardCorte}) {
            c.setStrokeWidth(c == selected ? 6 : 2);
        }
    }
}