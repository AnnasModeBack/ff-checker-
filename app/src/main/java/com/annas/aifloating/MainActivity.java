package com.annas.aifloating;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final String[] MODES = new String[]{
            "Auto Tugas & Jawaban Lengkap",
            "Pilihan Ganda Cepat (A/B/C/D/E + Alasan)",
            "Esai & Penjelasan Terstruktur",
            "Matematika & Langkah Hitung",
            "Coding & Debugging"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationHelper.ensureChannel(this);

        final ImageView heroIcon = findViewById(R.id.ivHero3D);
        LinearLayout mainCard = findViewById(R.id.mainCardContainer);
        final TextView tvServiceStatus = findViewById(R.id.tvServiceStatus);
        final EditText etApiKey = findViewById(R.id.etApiKey);
        final Spinner spinnerMode = findViewById(R.id.spinnerMode);
        final Button btnSave = findViewById(R.id.btnSave);
        final Button btnTestApi = findViewById(R.id.btnTestApi);
        final Button btnRequestNotif = findViewById(R.id.btnRequestNotif);
        final Button btnStart = findViewById(R.id.btnStartOverlay);
        final Button btnStop = findViewById(R.id.btnStopOverlay);

        Animation3DHelper.apply3DTiltEntrance(mainCard, 0L);
        Animation3DHelper.start3DFloatingIconLoop(heroIcon);

        heroIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation3DHelper.pop3DClick(heroIcon);
            }
        });

        final SharedPreferences prefs = getSharedPreferences("annas_ai_3d_java_prefs", Context.MODE_PRIVATE);
        etApiKey.setText(prefs.getString("api_key", ""));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                MODES
        );
        spinnerMode.setAdapter(adapter);
        spinnerMode.setSelection(prefs.getInt("mode_index", 0));

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation3DHelper.pop3DClick(btnSave);
                String key = etApiKey.getText().toString().trim();
                int idx = spinnerMode.getSelectedItemPosition();
                prefs.edit()
                        .putString("api_key", key)
                        .putInt("mode_index", idx)
                        .putString("mode_name", MODES[idx])
                        .apply();
                Toast.makeText(MainActivity.this, "Pengaturan Google AI v12 disimpan!", Toast.LENGTH_SHORT).show();
                NotificationHelper.showAnswerNotification(
                        MainActivity.this,
                        "Annas AI 3D Ultra v12",
                        "API Key & mode pengerjaan tugas berhasil disimpan."
                );
            }
        });

        btnTestApi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation3DHelper.pop3DClick(btnTestApi);
                String key = etApiKey.getText().toString().trim();
                if (key.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Isi API Key terlebih dahulu!", Toast.LENGTH_SHORT).show();
                    return;
                }
                btnTestApi.setEnabled(false);
                btnTestApi.setText("MENGUJI...");
                GeminiClient.solveTaskAsync(key, "Auto Tugas", "Jawab singkat satu kata: SIAP", new GeminiClient.AnswerCallback() {
                    @Override
                    public void onResult(final String answer) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnTestApi.setEnabled(true);
                                btnTestApi.setText("TES API");
                                if (answer != null && !answer.startsWith("Error") && !answer.startsWith("Gagal") && !answer.startsWith("Terjadi")) {
                                    tvServiceStatus.setText("API TERHUBUNG: " + answer.trim());
                                    Toast.makeText(MainActivity.this, "Koneksi Google AI Berhasil!", Toast.LENGTH_SHORT).show();
                                } else {
                                    tvServiceStatus.setText("API PERLU CEK ULANG");
                                    Toast.makeText(MainActivity.this, answer, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                });
            }
        });

        btnRequestNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation3DHelper.pop3DClick(btnRequestNotif);
                requestNotificationPermissionIfNeeded();
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation3DHelper.pop3DClick(btnStart);
                requestNotificationPermissionIfNeeded();
                if (!Settings.canDrawOverlays(MainActivity.this)) {
                    Toast.makeText(MainActivity.this, "Aktifkan izin tampil di atas aplikasi lain untuk bubble 3D", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName())
                    );
                    startActivity(intent);
                    return;
                }
                Intent serviceIntent = new Intent(MainActivity.this, FloatingAIService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
                tvServiceStatus.setText("STATUS: FLOATING AI 3D AKTIF");
                Toast.makeText(MainActivity.this, "Annas AI 3D Ultra v12 Floating aktif!", Toast.LENGTH_SHORT).show();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation3DHelper.pop3DClick(btnStop);
                stopService(new Intent(MainActivity.this, FloatingAIService.class));
                tvServiceStatus.setText("STATUS: NONAKTIF");
                Toast.makeText(MainActivity.this, "Annas AI 3D Floating dimatikan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            } else {
                Toast.makeText(this, "Izin pemberitahuan sudah aktif", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Pemberitahuan aktif di versi Android ini", Toast.LENGTH_SHORT).show();
        }
    }
}
