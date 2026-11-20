package com.nexora.recorder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class MainActivity extends Activity {
    private MediaProjectionManager mpm;
    private static final int REQ_CODE = 100;
    
    private LinearLayout contentArea;
    private LinearLayout mainLayout, galleryLayout, settingsLayout;
    private TextView title;
    
    public static int selectedResolution = 720; // Padrão
    public static boolean audioEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {Color.parseColor("#0A0A0F"), Color.parseColor("#12121A")});
        root.setBackground(bg);
        root.setPadding(40, 40, 40, 40);

        // Header (Título e Menu de 3 traços)
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        
        title = new TextView(this);
        title.setText("NEXORA REC");
        title.setTextColor(Color.parseColor("#00FF88"));
        title.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        title.setTextSize(24f);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        header.addView(title, titleParams);
        
        Button menuBtn = new Button(this);
        menuBtn.setText("≡");
        menuBtn.setTextSize(24f);
        menuBtn.setTextColor(Color.parseColor("#00FF88"));
        menuBtn.setBackgroundColor(Color.TRANSPARENT);
        menuBtn.setOnClickListener(v -> showSettings());
        header.addView(menuBtn);
        
        root.addView(header);

        // Area de Conteúdo (onde as abas vão mudar)
        contentArea = new LinearLayout(this);
        contentArea.setOrientation(LinearLayout.VERTICAL);
        contentArea.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        root.addView(contentArea, contentParams);

        // Menu Inferior (Abas Gravar e Galeria)
        LinearLayout bottomNav = new LinearLayout(this);
        bottomNav.setOrientation(LinearLayout.HORIZONTAL);
        bottomNav.setGravity(Gravity.CENTER);
        
        Button navRec = createNavButton("Gravar");
        navRec.setOnClickListener(v -> showMain());
        
        Button navGallery = createNavButton("Galeria");
        navGallery.setOnClickListener(v -> showGallery());
        
        bottomNav.addView(navRec);
        bottomNav.addView(navGallery);
        root.addView(bottomNav);

        setContentView(root);
        mpm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        
        showMain(); // Abre na tela principal por padrão
    }

    private Button createNavButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundColor(Color.TRANSPARENT);
        btn.setTextSize(14f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        btn.setLayoutParams(params);
        return btn;
    }

    private Button createButton(String text, String textColor, String bgColor) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.parseColor(textColor));
        btn.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        btn.setTextSize(14f);
        
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Color.parseColor(bgColor));
        shape.setCornerRadius(25f);
        shape.setStroke(3, Color.parseColor(textColor));
        btn.setBackground(shape);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 140);
        params.setMargins(0, 15, 0, 15);
        btn.setLayoutParams(params);
        return btn;
    }

    private void showMain() {
        contentArea.removeAllViews();
        title.setText("NEXORA REC");

        TextView sub = new TextView(this);
        sub.setText("Resolução: " + selectedResolution + "p | Som: " + (audioEnabled ? "ON" : "OFF"));
        sub.setTextColor(Color.parseColor("#A0A0B0"));
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 30);
        contentArea.addView(sub);

        Button btnStart = createButton("INICIAR GRAVAÇÃO", "#00FF88", "#003322");
        btnStart.setOnClickListener(v -> startActivityForResult(mpm.createScreenCaptureIntent(), REQ_CODE));

        Button btnStop = createButton("PARAR", "#FF0055", "#330011");
        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, RecorderService.class));
            Toast.makeText(this, "Salvo em Galeria!", Toast.LENGTH_SHORT).show();
        });

        contentArea.addView(btnStart);
        contentArea.addView(btnStop);
    }

    private void showGallery() {
        contentArea.removeAllViews();
        title.setText("GALERIA");

        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        
        File dir = new File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_MOVIES), "NexoraRec");
        if (dir.exists() && dir.listFiles() != null) {
            File[] files = dir.listFiles();
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.compare(f2.lastModified(), f1.lastModified());
                }
            });
            
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
                    list.addView(videoBtn);
                }
            }
            contentArea.addView(list);
        } else {
            TextView empty = new TextView(this);
            empty.setText("Nenhum vídeo encontrado.");
            empty.setTextColor(Color.GRAY);
            contentArea.addView(empty);
        }
    }

    private void showSettings() {
        contentArea.removeAllViews();
        title.setText("CONFIGURAÇÕES");

        Button res720 = createButton("Qualidade: 720p (Rápido)", "#00FF88", "#003322");
        res720.setOnClickListener(v -> { selectedResolution = 720; Toast.makeText(this, "Resolução 720p", Toast.LENGTH_SHORT).show(); showMain(); });

        Button res1080 = createButton("Qualidade: 1080p (Nítido)", "#00FF88", "#003322");
        res1080.setOnClickListener(v -> { selectedResolution = 1080; Toast.makeText(this, "Resolução 1080p", Toast.LENGTH_SHORT).show(); showMain(); });

        Button res480 = createButton("Qualidade: 480p (Super Leve)", "#00FF88", "#003322");
        res480.setOnClickListener(v -> { selectedResolution = 480; Toast.makeText(this, "Resolução 480p", Toast.LENGTH_SHORT).show(); showMain(); });
        
        Button audioBtn = createButton("Áudio: " + (audioEnabled ? "LIGADO" : "DESLIGADO"), "#FFAA00", "#332200");
        audioBtn.setOnClickListener(v -> { 
            audioEnabled = !audioEnabled; 
            Toast.makeText(this, "Áudio: " + (audioEnabled ? "ON" : "OFF"), Toast.LENGTH_SHORT).show(); 
            showMain(); 
        });

        contentArea.addView(res720);
        contentArea.addView(res1080);
        contentArea.addView(res480);
        contentArea.addView(audioBtn);
        
        Button back = createButton("Voltar", "#FFFFFF", "#222222");
        back.setOnClickListener(v -> showMain());
        contentArea.addView(back);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {
            Intent serviceIntent = new Intent(this, RecorderService.class);
            serviceIntent.putExtra("resultCode", resultCode);
            serviceIntent.putExtra("data", data);
            serviceIntent.putExtra("resolution", selectedResolution);
            serviceIntent.putExtra("audio", audioEnabled);
            startForegroundService(serviceIntent);
            Toast.makeText(this, "Gravando...", Toast.LENGTH_SHORT).show();
        }
    }
}
