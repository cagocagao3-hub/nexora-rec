package com.nexora.recorder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
        
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0A0A0F"));
        root.setGravity(Gravity.CENTER);
        root.setPadding(60, 60, 60, 60);

        TextView title = new TextView(this);
        title.setText("NEXORA REC");
        title.setTextColor(Color.parseColor("#00FF88"));
        title.setTextSize(36f);
        title.setGravity(Gravity.CENTER);
        title.setShadowLayer(10f, 0f, 0f, Color.parseColor("#00FF88"));
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("GRAVADOR 720p @ 30FPS");
        sub.setTextColor(Color.parseColor("#8888AA"));
        sub.setTextSize(12f);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 60);
        root.addView(sub);

        Button btnStart = createButton("INICIAR GRAVAÇÃO", "#00FF88", "#003322");
        btnStart.setOnClickListener(v -> startActivityForResult(mpm.createScreenCaptureIntent(), REQ_CODE));

        Button btnStop = createButton("PARAR E SALVAR", "#FF0055", "#330011");
        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, RecorderService.class));
            Toast.makeText(this, "Vídeo salvo em Movies/NexoraRec", Toast.LENGTH_LONG).show();
        });

        root.addView(btnStart);
        root.addView(btnStop);
        setContentView(root);
        mpm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
    }

    private Button createButton(String text, String textColor, String bgColor) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.parseColor(textColor));
        btn.setTextSize(16f);
        btn.setAllCaps(false);
        
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Color.parseColor(bgColor));
        shape.setCornerRadius(30f);
        shape.setStroke(2, Color.parseColor(textColor));
        btn.setBackground(shape);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 150);
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
