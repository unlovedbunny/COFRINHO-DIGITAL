package com.example.cofrinhodigital;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private SavingsManager sm;
    private TextView tvGoalName, tvProgress, tvAmount, tvDays, tvModality;
    private ProgressBar progressBar;
    private Switch switchReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sm = new SavingsManager(this);

        // Redireciona se não tem meta criada
        if (!sm.hasGoal()) {
            startActivity(new Intent(this, SetupActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        tvGoalName   = findViewById(R.id.tv_goal_name);
        tvProgress   = findViewById(R.id.tv_progress_percent);
        tvAmount     = findViewById(R.id.tv_amount);
        tvDays       = findViewById(R.id.tv_days);
        tvModality   = findViewById(R.id.tv_modality);
        progressBar  = findViewById(R.id.progress_bar);
        switchReminder = findViewById(R.id.switch_reminder);

        Button btnDeposit  = findViewById(R.id.btn_deposit);
        Button btnHistory  = findViewById(R.id.btn_history);
        Button btnReset    = findViewById(R.id.btn_reset);

        btnDeposit.setOnClickListener(v -> showDepositDialog());
        btnHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoricoActivity.class)));
        btnReset.setOnClickListener(v -> confirmReset());

        switchReminder.setChecked(sm.isReminderEnabled());
        switchReminder.setOnCheckedChangeListener((b, checked) -> {
            if (checked) showTimePickerForReminder();
            else sm.setReminder(false, 9, 0);
        });

        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sm.hasGoal()) updateUI();
    }

    private void updateUI() {
        tvGoalName.setText(sm.getGoalName());
        tvModality.setText(sm.getModality());
        tvDays.setText(sm.getDaysRemaining() + " dias restantes");
        tvAmount.setText(String.format("R$ %.2f  /  R$ %.2f",
                sm.getCurrentAmount(), sm.getGoalValue()));
        int pct = (int)(sm.getProgress() * 100);
        progressBar.setProgress(pct);
        tvProgress.setText(pct + "%");

        if (sm.getProgress() >= 1.0) {
            tvProgress.setText("Meta atingida!");
        }
    }

    private void showDepositDialog() {
        EditText input = new EditText(this);
        input.setHint("Valor em R$");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        new AlertDialog.Builder(this)
                .setTitle("Registrar economia")
                .setView(input)
                .setPositiveButton("Salvar", (d, w) -> {
                    String val = input.getText().toString().trim();
                    if (!val.isEmpty()) {
                        sm.addDeposit(Double.parseDouble(val));
                        updateUI();
                        Toast.makeText(this, "Valor registrado!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showTimePickerForReminder() {
        new TimePickerDialog(this, (view, h, m) -> {
            sm.setReminder(true, h, m);
            Toast.makeText(this, String.format("Lembrete às %02d:%02d", h, m),
                    Toast.LENGTH_SHORT).show();
        }, sm.getReminderHour(), sm.getReminderMinute(), true).show();
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle("Resetar meta")
                .setMessage("Isso apagará todo o progresso. Deseja continuar?")
                .setPositiveButton("Sim", (d, w) -> {
                    sm.resetGoal();
                    startActivity(new Intent(this, SetupActivity.class));
                    finish();
                })
                .setNegativeButton("Não", null)
                .show();
    }
}