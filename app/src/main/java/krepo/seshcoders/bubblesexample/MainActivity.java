package krepo.seshcoders.bubblesexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.widget.Toast;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import krepo.seshcoders.bubblesexample.views.MessCloudView;
import krepo.seshcoders.messengerbubbles.BubbleLayout;
import krepo.seshcoders.messengerbubbles.BubblesManager;

public class MainActivity extends AppCompatActivity {

    //consts
    private static final int PERMISSIONS_REQUEST_CODE = 1231;

    private BubbleLayout bubbleView;
    private BubblesManager bubblesManager;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, PERMISSIONS_REQUEST_CODE);
        }else {
            initializeBubbles();
            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
            executor.schedule(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bubblesManager.addBubble(bubbleView, 0, 20);

                        }
                    });
                }
            }, 5, TimeUnit.SECONDS);
        }
    }

    private void initializeBubbles(){
        bubblesManager = new BubblesManager.Builder(mContext)
                .setTrashLayout(R.layout.component_bubble_trash_layout)
                .build();
        bubblesManager.initialize();

        bubbleView = (BubbleLayout) LayoutInflater
                .from(MainActivity.this).inflate(R.layout.component_bubble_layout, null);
        bubbleView.setShouldStickToWall(true);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bubblesManager.recycle();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Toast.makeText(mContext, "permissions changed", Toast.LENGTH_SHORT).show();
    }
}
