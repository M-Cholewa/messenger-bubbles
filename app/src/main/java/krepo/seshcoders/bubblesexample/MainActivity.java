package krepo.seshcoders.bubblesexample;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import krepo.seshcoders.messengerbubbles.BubbleLayout;
import krepo.seshcoders.messengerbubbles.BubbleStack;
import krepo.seshcoders.messengerbubbles.BubblesManager;
import krepo.seshcoders.messengerbubbles.MessCloudView;

import static krepo.seshcoders.messengerbubbles.MessCloudView.BubbleCurrentWall.LEFT;

@SuppressLint("InflateParams")
public class MainActivity extends AppCompatActivity implements BubbleLayout.OnBubbleStickToWallListener {
    // TODO: 02.01.2020 foreground service instead of this one
    // TODO: 22.04.2020 after some time cloud doesnt want to dissapear, doesnt react to anything, doesnt change visibility

    //consts
    private static final int PERMISSIONS_REQUEST_CODE = 1231;
    private static final String TAG = "MainActivity";
//    BubbleLayout bubbleView2;

    //views, android objects
    private BubblesManager bubblesManager;
    private BubbleStack stack = new BubbleStack();
    private Context mContext;
    private BubbleLayout bubbleView3;
    private TextView msgBadge;


    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    public static final String CHANNEL_ID = "1332";

    private ImageView interceptedNotificationImageView;
    private ImageChangeBroadcastReceiver imageChangeBroadcastReceiver;
    private AlertDialog enableNotificationListenerAlertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();

       final MessCloudView messCloudView = LayoutInflater
                .from(MainActivity.this)
                .inflate(R.layout.component_bubble_cloud, null, false)
                .findViewById(R.id.cloudView);


        bubblesManager = new BubblesManager.Builder(mContext)
                .setTrashLayout(R.layout.component_bubble_trash_layout)
                .setMessageCloud(messCloudView)
                .build();
        bubblesManager.initialize();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, PERMISSIONS_REQUEST_CODE);
        } else {
            initializeBubbles();
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    bubbleView3.displayMessage("nocojestbyniuuu","\uD83D\uDC9C\uD83D\uDC9C sugar daddy    2️⃣ 0️⃣ 0️⃣ 6️⃣ ✖️✖️\n");
                }
            }, 3000);
        }

        // Here we get a reference to the image we will modify when a notification is received
        interceptedNotificationImageView
                = this.findViewById(R.id.intercepted_notification_logo);

        // If the user did not turn the notification listener service on we prompt him to do so
        if(!isNotificationServiceEnabled()){
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }

        // Finally we register a receiver to tell the MainActivity when a notification has been received
        imageChangeBroadcastReceiver = new ImageChangeBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("krepo.seshcoders.notificationlistenerexample");
        registerReceiver(imageChangeBroadcastReceiver,intentFilter);

    }

    private void initializeBubbles() {
        BubbleLayout bubbleView = (BubbleLayout) LayoutInflater
                .from(MainActivity.this).inflate(R.layout.component_bubble_layout, null, false);

        BubbleLayout bubbleView2 = (BubbleLayout) LayoutInflater
                .from(MainActivity.this).inflate(R.layout.component_bubble_layout, null, false);

        bubbleView3 = (BubbleLayout) LayoutInflater
                .from(MainActivity.this).inflate(R.layout.component_bubble_layout, null, false);

//        bubbleView.setShouldStickToWall(true);
//        bubbleView2.setShouldStickToWall(true);
//        bubbleView3.setShouldStickToWall(true);

        bubbleView.setOnBubbleStickToWallListener(this);
        bubbleView2.setOnBubbleStickToWallListener(this);
        bubbleView3.setOnBubbleStickToWallListener(this);

        bubbleView.setElevation(100);
        bubbleView2.setElevation(100);
        bubbleView3.setElevation(100);

//        ((AvatarView) bubbleView2.findViewById(R.id.avatar)).setAvatar(R.drawable.profile_blank100);
//        ((AvatarView) bubbleView3.findViewById(R.id.avatar)).setAvatar(R.drawable.profile_memowa100);


        bubbleView.findViewById(R.id.badge).setVisibility(View.GONE);
        bubbleView2.findViewById(R.id.badge).setVisibility(View.GONE);

        msgBadge = bubbleView3.findViewById(R.id.badge);
        stack.addBubble(
//                bubbleView,
//                bubbleView2,
                bubbleView3
        );

        bubblesManager.addBubbleStack(stack, getScreenWidth(), getScreenHeight() / 2- 350);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Toast.makeText(mContext, "permissions changed", Toast.LENGTH_SHORT).show();
    }

    private int getScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    private int getScreenHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    @Override
    public void onBubbleStickToWall(MessCloudView.BubbleCurrentWall wall, BubbleLayout bubble) {
        TextView messageBadge = bubble.findViewById(R.id.badge);
        RelativeLayout bubbleContainer = bubble.findViewById(R.id.bubbleContainer);

        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) messageBadge.getLayoutParams();
        final RelativeLayout.LayoutParams containerParams = (RelativeLayout.LayoutParams) bubbleContainer.getLayoutParams();

        if (wall == LEFT) {
            //left wall, messageBadge layout grevity right
            containerParams.setMargins(-1 * (bubble.getStackPosition() + 1) * 15,
                    0,
                    0,
                    0);
            bubble.setX(bubble.getStackPosition() * 7);
            bubbleContainer.setLayoutParams(containerParams);

            params.removeRule(RelativeLayout.ALIGN_START);
            params.addRule(RelativeLayout.ALIGN_END, R.id.avatar);
            messageBadge.setLayoutParams(params);

        } else {
            //right wall, messageBadge layout grevity left
            containerParams.setMargins(0,
                    0,
                    -1 * (bubble.getStackPosition() + 1) * 15,
                    0);
            bubbleContainer.setLayoutParams(containerParams);
            bubble.setX(bubble.getStackPosition() * 7);

            params.removeRule(RelativeLayout.ALIGN_END);
            params.addRule(RelativeLayout.ALIGN_START, R.id.avatar);
            messageBadge.setLayoutParams(params);
        }
    }

    public void setMessage(View v){
        String author = ((EditText)findViewById(R.id.author)).getText().toString();
        String message = ((EditText)findViewById(R.id.message)).getText().toString();
        String msgCount = ((EditText)findViewById(R.id.msgCount)).getText().toString();
        msgBadge.setText(msgCount);
        bubbleView3.displayMessage(message,author);
        Log.d(TAG, "setMessage: BEFORE"+(author+": "+message).replace(" ","$"));
    }

    public void showNotification(View v){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.mess_logo)
                .setContentTitle("notification title")
                .setContentText("notification content")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());

    }


    @Override
    protected void onDestroy() {
        bubblesManager.recycle();
        unregisterReceiver(imageChangeBroadcastReceiver);
        super.onDestroy();
    }


    private void changeInterceptedNotificationImage(int notificationCode){
        switch(notificationCode){
            case NotificationListenerExampleService.InterceptedNotificationCode.FACEBOOK_CODE:
                interceptedNotificationImageView.setImageResource(R.drawable.facebook_logo);
                break;
            case NotificationListenerExampleService.InterceptedNotificationCode.INSTAGRAM_CODE:
                interceptedNotificationImageView.setImageResource(R.drawable.instagram_logo);
                break;
            case NotificationListenerExampleService.InterceptedNotificationCode.WHATSAPP_CODE:
                interceptedNotificationImageView.setImageResource(R.drawable.whatsapp_logo);
                break;
            case NotificationListenerExampleService.InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE:
                interceptedNotificationImageView.setImageResource(R.drawable.other_notification_logo);
                break;
        }
    }


    public class ImageChangeBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int receivedNotificationCode = intent.getIntExtra("Notification Code",-1);
            changeInterceptedNotificationImage(receivedNotificationCode);
        }
    }

    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If you choose to not enable the notification listener
                        // the app. will not work as expected
                    }
                });
        return(alertDialogBuilder.create());
    }

}
