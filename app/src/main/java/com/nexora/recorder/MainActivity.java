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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Fundo do App (Gradiente Escuro Premium)
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {Color.parseColor("#0A0A0F"), Color.parseColor("#12121A")});
        root.setBackground(bg);
        root.setGravity(Gravity.CENTER);
        root.setPadding(60, 60, 60, 60);

        // Logo/Ícone (Texto grande)
        TextView logo = new TextView(this);
        logo.setText("🎬");
        logo.setTextSize(60f);
        logo.setGravity(Gravity.CENTER);
        root.addView(logo);

        // Título Neon
        TextView title = new TextView(this);
        title.setText("NEXORA REC");
        title.setTextColor(Color.parseColor("#00FF88"));
        title.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        title.setTextSize(32f);
        title.setGravity(Gravity.CENTER);
        title.setShadowLayer(15f, 0f, 0f, Color.parseColor("#00FF88")); 
        root.addView(title);

        // Subtítulo
        TextView sub = new TextView(this);
        sub.setText("REC 720p • 30FPS • ULTRA LEVE");
        sub.setTextColor(Color.parseColor("#A0A0B0"));
        sub.setTextSize(12f);
        sub.setLetterSpacing(0.1f);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 10, 0, 80);
        root.addView(sub);

        // Botão Iniciar (Verde Neon)
        Button btnStart = createButton("INICIAR GRAVAÇÃO", "#00FF88", "#003322", "#00FF88");
        btnStart.setOnClickListener(v -> startActivityForResult(mpm.createScreenCaptureIntent(), REQ_CODE));

        // Botão Parar (Vermelho)
        Button btnStop = createButton("PARAR E SALVAR", "#FF0055", "#330011", "#FF0055");
        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, RecorderService.class));
            Toast.makeText(this, "Vídeo salvo em Movies/NexoraRec", Toast.LENGTH_LONG).show();
        });

        root.addView(btnStart);
        root.addView(btnStop);

        setContentView(root);
        mpm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
    }

    private Button createButton(String text, String textColor, String bgColor, String strokeColor) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.parseColor(textColor));
        btn.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        btn.setTextSize(14f);
        btn.setAllCaps(false);
        
        // Efeito de Vidro (Glassmorphism) com Borda Neon
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Color.parseColor(bgColor));
        shape.setCornerRadius(25f);
        shape.setStroke(3, Color.parseColor(strokeColor));
        btn.setBackground(shape);
        btn.setElevation(10f);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 140);
        params.setMargins(0, 15, 0, 15);
        btn.setLayoutParams(params);
        
        return btn;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {
            Intent serviceIntent = new Intent(this, RecorderService.class);
            serviceIntent.putExtra("resultCode", resultCode);
            serviceIntent.putExtra("data", data);
            startForegroundService(serviceIntent);
            Toast.makeText(this, "Gravando em 720p...", Toast.LENGTH_SHORT).show();
        }
    }
}
