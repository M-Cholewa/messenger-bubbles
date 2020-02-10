package krepo.seshcoders.bubblesexample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import krepo.seshcoders.messengerbubbles.BubbleLayout;
import krepo.seshcoders.messengerbubbles.BubbleStack;
import krepo.seshcoders.messengerbubbles.BubblesManager;
import krepo.seshcoders.messengerbubbles.MessCloudView;

import static krepo.seshcoders.messengerbubbles.MessCloudView.BubbleCurrentWall.LEFT;

@SuppressLint("InflateParams")
public class MainActivity extends AppCompatActivity implements BubbleLayout.OnBubbleStickToWallListener {
    // TODO: 02.01.2020 stick to wall with cloud view bubble stays in the middle
    // TODO: 02.01.2020 margin when out of screen boundaries
    // TODO: 02.01.2020 foreground service instead of this one


    //consts
    private static final int PERMISSIONS_REQUEST_CODE = 1231;
//    BubbleLayout bubbleView2;

    //views, android objects
    private BubblesManager bubblesManager;
    private BubbleStack stack = new BubbleStack();
    private Context mContext;
    private BubbleLayout bubbleView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();

//        final MessCloudView mcv = findViewById(R.id.cloudView);

       final MessCloudView messCloudView = LayoutInflater
                .from(MainActivity.this)
                .inflate(R.layout.component_bubble_cloud, null, false)
                .findViewById(R.id.cloudView);
//        if (messCloudView.getParent()!=null){
//            ViewGroup viewGroup = (ViewGroup)messCloudView.getParent();
//            viewGroup.removeView(messCloudView);
//        }
//        this.addContentView(messCloudView, messCloudView.getLayoutParams());
//        messCloudView.setCurrentWall(LEFT);
//        messCloudView.setCloudMessage("witaaaaaaaaaaaaaaaaaaa mikola nie istnieje",50 ,150);

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
//                    bubbleView3.displayMessage("białorycerz: witaaaaaa on mi kota orzygal mikola nie istnieje");
                    bubbleView3.displayMessage("białorycerz: ciosaj mongoła kocmołuchu");
//                    bubbleView3.displayMessage("białorycerz: witaaaaaa");
//                    bubbleView3.displayMessage("Mufinka Michał: Wyślij pocztą walentynkową");

                }
            }, 3000);

//            new ScheduledThreadPoolExecutor(1).schedule(new Runnable() {
//                @Override
//                public void run() {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
////                            Handler h = Looper()
////                            bubbleView.displayMessage("witaaaaaaaaaaaaaaaaaaa mikola nie istnieje");
////                            mcv.setVisibility(View.VISIBLE);
//
//                        }
//                    });
//                }
//            }, 3, TimeUnit.SECONDS);
        }
    }

    private void initializeBubbles() {
        BubbleLayout bubbleView = (BubbleLayout) LayoutInflater
                .from(MainActivity.this).inflate(R.layout.component_bubble_layout, null, false);

        BubbleLayout bubbleView2 = (BubbleLayout) LayoutInflater
                .from(MainActivity.this).inflate(R.layout.component_bubble_layout, null, false);

        bubbleView3 = (BubbleLayout) LayoutInflater
                .from(MainActivity.this).inflate(R.layout.component_bubble_layout, null, false);

        bubbleView.setShouldStickToWall(true);
        bubbleView2.setShouldStickToWall(true);
        bubbleView3.setShouldStickToWall(true);

        bubbleView.setOnBubbleStickToWallListener(this);
        bubbleView2.setOnBubbleStickToWallListener(this);
        bubbleView3.setOnBubbleStickToWallListener(this);

        bubbleView.setElevation(100);
        bubbleView2.setElevation(100);
        bubbleView3.setElevation(100);

        ((ImageView) bubbleView2.findViewById(R.id.avatar)).setImageResource(R.drawable.profile_blank);
        ((ImageView) bubbleView3.findViewById(R.id.avatar)).setImageResource(R.drawable.profile_roxi);

        bubbleView.findViewById(R.id.badge).setVisibility(View.GONE);
        bubbleView2.findViewById(R.id.badge).setVisibility(View.GONE);

        stack.addBubble(bubbleView, bubbleView2, bubbleView3);

        bubblesManager.addBubbleStack(stack, getScreenWidth(), getScreenHeight() / 2);

//        bubblesManager.addBubble(bubbleView, 0, getScreenHeight());
//        bubblesManager.addBubble(bubbleView2, 0, getScreenHeight()/2);
//        bubblesManager.addBubble(bubbleView3, 0, getScreenHeight()/2);
//        bubblesManager.addBubbleStack(stack, 0,getScreenHeight()/2 );
//        stack.addBubble(bubbleView);

//        bubbleView.displayMessage("SKRRR SKRRRRR COS SKRRRRp");
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
}
