package com.nexora.recorder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.IBinder;
import java.io.File;
import java.io.IOException;

public class RecorderService extends Service {
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private MediaRecorder mediaRecorder;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationChannel channel = new NotificationChannel("rec", "Gravacao", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        startForeground(1, new Notification.Builder(this, "rec")
                .setContentTitle("Nexora Rec")
                .setContentText("Gravando...")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .build());

        int resultCode = intent.getIntExtra("resultCode", -1);
        Intent data = intent.getParcelableExtra("data");
        int res = intent.getIntExtra("resolution", 720);
        boolean audio = intent.getBooleanExtra("audio", false);
        
        MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        mediaProjection = mpm.getMediaProjection(resultCode, data);

        startRecording(res, audio);
        return START_STICKY;
    }

    private void startRecording(int res, boolean audio) {
        int width, height, dpi;
        if (res == 1080) { width = 1920; height = 1080; dpi = 480; }
        else if (res == 480) { width = 854; height = 480; dpi = 240; }
        else { width = 1280; height = 720; dpi = 320; }

        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "NexoraRec");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, "gravacao_" + System.currentTimeMillis() + ".mp4");

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        if (audio) {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        }
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        if (audio) {
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        }
        mediaRecorder.setVideoSize(width, height);
        mediaRecorder.setVideoFrameRate(30); 
        // Bitrate baseado na resolução para manter leve
        int bitrate = 2500000;
        if (res == 1080) bitrate = 5000000;
        if (res == 480) bitrate = 1000000;
        mediaRecorder.setVideoEncodingBitRate(bitrate);
        
        mediaRecorder.setOutputFile(file.getAbsolutePath());

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        virtualDisplay = mediaProjection.createVirtualDisplay("NexoraRec",
                width, height, dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.getSurface(), null, null);

        mediaRecorder.start();
    }

    @Override
    public void onDestroy() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
        }
        if (virtualDisplay != null) virtualDisplay.release();
        if (mediaProjection != null) mediaProjection.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
