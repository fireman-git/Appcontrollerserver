package com.fireman.appcontrollerserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(getApplication(), MainService.class);

        // 通知作成
        PendingIntent sendPendingIntent = PendingIntent.getBroadcast(this, 0, sendIntent, 0);
        int importance = NotificationManager.IMPORTANCE_HIGH; // デフォルトの重要度
        NotificationChannel channel = new NotificationChannel(channelId, name, importance);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.channel_id))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_txt))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.ic_launcher_foreground, getString(R.string.notification_btn), );

        // ボタン追加

        // サービス開始
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}