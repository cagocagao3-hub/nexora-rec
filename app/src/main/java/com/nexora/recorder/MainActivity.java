package com.nexora.recorder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private MediaProjectionManager mpm;
    private static final int REQ_CODE = 100;
    private LinearLayout contentArea;
    private TextView title;

    public static int resolution = 720; // Padrão
    public static boolean recordAudio = false;

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

        // Header
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

        contentArea = new LinearLayout(this);
        contentArea.setOrientation(LinearLayout.VERTICAL);
        contentArea.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        root.addView(contentArea, contentParams);

        setContentView(root);
        mpm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        
        showMain();
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
        sub.setText("Resolução: " + resolution + "p | Som: " + (recordAudio ? "ON" : "OFF"));
        sub.setTextColor(Color.parseColor("#A0A0B0"));
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 30);
        contentArea.addView(sub);

        Button btnStart = createButton("INICIAR GRAVAÇÃO", "#00FF88", "#003322");
        btnStart.setOnClickListener(v -> startActivityForResult(mpm.createScreenCaptureIntent(), REQ_CODE));

        Button btnStop = createButton("PARAR", "#FF0055", "#330011");
        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, RecorderService.class));
            Toast.makeText(this, "Processando vídeo...", Toast.LENGTH_SHORT).show();
            finish();
        });

        contentArea.addView(btnStart);
        contentArea.addView(btnStop);
    }

    private void showSettings() {
        contentArea.removeAllViews();
        title.setText("CONFIGURAÇÕES");

        Button r720 = createButton("720p (Leve)", "#00FF88", "#003322");
        r720.setOnClickListener(v -> { resolution = 720; Toast.makeText(this, "720p", Toast.LENGTH_SHORT).show(); showMain(); });

        Button r1080 = createButton("1080p (Nítido)", "#00FF88", "#003322");
        r1080.setOnClickListener(v -> { resolution = 1080; Toast.makeText(this, "1080p", Toast.LENGTH_SHORT).show(); showMain(); });

        Button r480 = createButton("480p (Super Leve)", "#00FF88", "#003322");
        r480.setOnClickListener(v -> { resolution = 480; Toast.makeText(this, "480p", Toast.LENGTH_SHORT).show(); showMain(); });
        
        Button audioBtn = createButton("Áudio: " + (recordAudio ? "LIGADO" : "DESLIGADO"), "#FFAA00", "#332200");
        audioBtn.setOnClickListener(v -> { 
            recordAudio = !recordAudio; 
            Toast.makeText(this, "Áudio: " + (recordAudio ? "ON" : "OFF"), Toast.LENGTH_SHORT).show(); 
            showMain(); 
        });

        contentArea.addView(r720);
        contentArea.addView(r1080);
        contentArea.addView(r480);
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
            serviceIntent.putExtra("resolution", resolution);
            serviceIntent.putExtra("audio", recordAudio);
            startForegroundService(serviceIntent);
            Toast.makeText(this, "Gravando em " + resolution + "p...", Toast.LENGTH_SHORT).show();
        }
    }
}
