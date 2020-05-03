package krepo.seshcoders.bubblesexample;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;

import static android.content.ContentValues.TAG;


public class AvatarView extends View {
    //consts
    private static final float SHADOW_RADIUS = 3.25f;
    private static final float SHADOW_Y_OFFSET = 1.25f;

    //attribute vars
    private int imageResId;

    //view objects
    private Paint bitmapPaint;
    private Paint shadowPaint;
    private Bitmap avatar;

    public AvatarView(Context context) {
        super(context);
        init(null, context);
    }

    public AvatarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, context);
    }

    public AvatarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, context);
    }

    private void init(@Nullable final AttributeSet attrs, Context context) {
        Log.d(TAG, "init: getMeasuredHeight"+getMeasuredHeight());
        if (attrs == null) return;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AvatarView);
        imageResId = typedArray.getResourceId(R.styleable.AvatarView_avatarImage, R.drawable.profile_blank100);
        typedArray.recycle();

        this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //init value after getting measurements
                avatar = Bitmap.createScaledBitmap(
                        BitmapFactory.decodeResource(getResources(), imageResId),
                        AvatarView.this.getWidth()-(int)SHADOW_RADIUS*2,
                        AvatarView.this.getHeight()-(int)SHADOW_RADIUS*2-(int) SHADOW_Y_OFFSET*2,
                        true);
            }
        });

        bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setShadowLayer(SHADOW_RADIUS, 0, SHADOW_Y_OFFSET, Color.BLACK);
        setLayerType(LAYER_TYPE_SOFTWARE, shadowPaint);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawAvatar(canvas);
    }

    private void drawAvatar(Canvas canvas){
        drawAvatarShadow(canvas);
        canvas.drawBitmap(avatar, SHADOW_RADIUS, SHADOW_RADIUS, bitmapPaint);
    }

    private void drawAvatarShadow(Canvas canvas){
        canvas.drawCircle(
                avatar.getWidth()/2f+SHADOW_RADIUS,
                avatar.getHeight()/2f+SHADOW_RADIUS,
                avatar.getWidth()/2f,
                shadowPaint);

    }

    public void setAvatar(int imageResId){
        if (imageResId!=this.imageResId){
            this.imageResId = imageResId;
            avatar = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(getResources(), imageResId),
                    AvatarView.this.getWidth()-(int)SHADOW_RADIUS*2,
                    AvatarView.this.getHeight()-(int)SHADOW_RADIUS*2,
                    true);
            this.invalidate();
        }
    }

    //overrides
}
