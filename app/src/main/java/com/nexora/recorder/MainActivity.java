package com.nexora.recorder;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends Activity {
    private MediaProjectionManager mpm;
    private static final int REQ_CODE = 100;
    
    private LinearLayout contentArea;
    private LinearLayout menuDrawer;
    private boolean isMenuOpen = false;
    
    public static int resolution = 720;
    public static boolean recordAudio = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#0A0A0F"));

        LinearLayout mainContent = new LinearLayout(this);
        mainContent.setOrientation(LinearLayout.VERTICAL);
        mainContent.setPadding(40, 40, 40, 40);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        
        Button menuBtn = new Button(this);
        menuBtn.setText("≡");
        menuBtn.setTextSize(24f);
        menuBtn.setTextColor(Color.parseColor("#00FF88"));
        menuBtn.setBackgroundColor(Color.TRANSPARENT);
        menuBtn.setOnClickListener(v -> toggleMenu());
        header.addView(menuBtn);
        
        TextView title = new TextView(this);
        title.setText("NEXORA REC");
        title.setTextColor(Color.parseColor("#00FF88"));
        title.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        title.setTextSize(20f);
        title.setPadding(20, 0, 0, 0);
        header.addView(title);
        
        mainContent.addView(header);

        contentArea = new LinearLayout(this);
        contentArea.setOrientation(LinearLayout.VERTICAL);
        contentArea.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        mainContent.addView(contentArea, params);

        root.addView(mainContent);

        // Menu Lateral (Drawer)
        menuDrawer = new LinearLayout(this);
        menuDrawer.setOrientation(LinearLayout.VERTICAL);
        menuDrawer.setBackgroundColor(Color.parseColor("#12121A"));
        menuDrawer.setPadding(30, 60, 30, 30);
        menuDrawer.setVisibility(View.GONE);
        
        FrameLayout.LayoutParams drawerParams = new FrameLayout.LayoutParams(500, FrameLayout.LayoutParams.MATCH_PARENT);
        root.addView(menuDrawer, drawerParams);

        Button navInicio = createMenuBtn("Início");
        navInicio.setOnClickListener(v -> { showInicio(); toggleMenu(); });
        
        Button navGaleria = createMenuBtn("Galeria");
        navGaleria.setOnClickListener(v -> { showGaleria(); toggleMenu(); });
        
        Button navConfig = createMenuBtn("Configuração");
        navConfig.setOnClickListener(v -> { showConfig(); toggleMenu(); });

        menuDrawer.addView(navInicio);
        menuDrawer.addView(navGaleria);
        menuDrawer.addView(navConfig);

        setContentView(root);
        mpm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        showInicio();
    }

    private void toggleMenu() {
        isMenuOpen = !isMenuOpen;
        menuDrawer.setVisibility(isMenuOpen ? View.VISIBLE : View.GONE);
    }

    private Button createMenuBtn(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundColor(Color.TRANSPARENT);
        btn.setTextSize(16f);
        btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        btn.setPadding(20, 30, 20, 30);
        return btn;
    }

    private Button createActionButton(String text, String color) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.parseColor(color));
        btn.setTextSize(16f);
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Color.parseColor("#003322"));
        shape.setCornerRadius(20f);
        shape.setStroke(3, Color.parseColor(color));
        btn.setBackground(shape);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 140);
        params.setMargins(0, 20, 0, 20);
        btn.setLayoutParams(params);
        return btn;
    }

    private void showInicio() {
        contentArea.removeAllViews();
        
        TextView sub = new TextView(this);
        sub.setText("REC " + resolution + "p • " + (recordAudio ? "Áudio ON" : "Áudio OFF"));
        sub.setTextColor(Color.parseColor("#A0A0B0"));
        sub.setGravity(Gravity.CENTER);
        contentArea.addView(sub);

        Button btnStart = createActionButton("INICIAR GRAVAÇÃO", "#00FF88");
        btnStart.setOnClickListener(v -> startActivityForResult(mpm.createScreenCaptureIntent(), REQ_CODE));

        Button btnStop = createActionButton("PARAR", "#FF0055");
        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, RecorderService.class));
            Toast.makeText(this, "Vídeo salvo na Galeria!", Toast.LENGTH_SHORT).show();
        });

        contentArea.addView(btnStart);
        contentArea.addView(btnStop);
    }

    private void showGaleria() {
        contentArea.removeAllViews();
        
        TextView title = new TextView(this);
        title.setText("VÍDEOS SALVOS");
        title.setTextColor(Color.parseColor("#00FF88"));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);
        contentArea.addView(title);
        
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "NexoraRec");
        if (dir.exists() && dir.listFiles() != null) {
            File[] files = dir.listFiles();
            Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            
            if (files.length == 0) {
                TextView empty = new TextView(this);
                empty.setText("Nenhum vídeo encontrado.");
                empty.setTextColor(Color.GRAY);
                contentArea.addView(empty);
                return;
            }

            for (File f : files) {
                if (f.getName().endsWith(".mp4")) {
                    Button videoBtn = new Button(this);
                    videoBtn.setText("▶ " + f.getName());
                    videoBtn.setTextColor(Color.WHITE);
                    videoBtn.setBackgroundColor(Color.parseColor("#1A1A2A"));
                    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
                    p.setMargins(0, 5, 0, 5);
                    videoBtn.setLayoutParams(p);
                    videoBtn.setOnClickListener(v -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(f), "video/mp4");
                        startActivity(intent);
                    });
                    contentArea.addView(videoBtn);
                }
            }
        } else {
            TextView empty = new TextView(this);
            empty.setText("Nenhum vídeo encontrado.");
            empty.setTextColor(Color.GRAY);
            contentArea.addView(empty);
        }
    }

    private void showConfig() {
        contentArea.removeAllViews();
        
        Button r720 = createActionButton("720p (Leve)", "#00FF88");
        r720.setOnClickListener(v -> { resolution = 720; showConfig(); });

        Button r1080 = createActionButton("1080p (Nítido)", "#00FF88");
        r1080.setOnClickListener(v -> { resolution = 1080; showConfig(); });

        Button r480 = createActionButton("480p (Super Leve)", "#00FF88");
        r480.setOnClickListener(v -> { resolution = 480; showConfig(); });
        
        Button audioBtn = createActionButton("Áudio: " + (recordAudio ? "LIGADO" : "DESLIGADO"), "#FFAA00");
        audioBtn.setOnClickListener(v -> { recordAudio = !recordAudio; showConfig(); });

        contentArea.addView(r720);
        contentArea.addView(r1080);
        contentArea.addView(r480);
        contentArea.addView(audioBtn);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {
            Intent serviceIntent = new Intent(this, RecorderService.class);
            serviceIntent.putExtra("resultCode", resultCode);
            serviceIntent.putExtra("data", data);
            serviceIntent.putExtra("resolution", resolution);
            serviceIntent.putExtra("audio", recordAudio);
            startForegroundService(serviceIntent);
            Toast.makeText(this, "Gravando em " + resolution + "p...", Toast.LENGTH_SHORT).show();
        }
    }
}
