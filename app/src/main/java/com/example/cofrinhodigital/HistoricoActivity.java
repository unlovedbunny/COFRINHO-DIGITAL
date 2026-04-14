package com.example.cofrinhodigital;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class HistoricoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        SavingsManager sm = new SavingsManager(this);
        List<String> items = sm.getHistoryList();

        ListView listView = findViewById(R.id.list_historico);
        TextView tvTotal  = findViewById(R.id.tv_total);

        tvTotal.setText(String.format("Total economizado: R$ %.2f", sm.getCurrentAmount()));

        if (items.isEmpty()) items.add("Nenhum registro ainda.");
        listView.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, items));

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}