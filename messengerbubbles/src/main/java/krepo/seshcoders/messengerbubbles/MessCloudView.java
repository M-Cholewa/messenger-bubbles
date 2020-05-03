package krepo.seshcoders.messengerbubbles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import static android.content.Context.WINDOW_SERVICE;

@SuppressWarnings({"SuspiciousNameCombination", "unused"})
public class MessCloudView extends View {

    //consts
    private static final String TAG = "MessCloudView";
    private static final float SHADOW_RADIUS = 3;
    private static final long VISIBILITY_DURATION = 15000;

    //attribute vars
    private String cloudMessage = "";
    private String messageAuthor = "";
    private int cloudColor = Color.BLUE;
    private int cloudMessageColor = Color.WHITE;
    private int arrowHeight = 15;
    private int cloudVerticalPadding = 15;
    private int cloudHorizontalPadding = 15;
    private int cloudMaxWidth = 500;
    private int bgImageResId;

    //vars
    private Context mContext;
    private BubbleCurrentWall currentWall = BubbleCurrentWall.LEFT;
    private int fullMessWidth;
    private int fullMessHeight;
    private StringBuilder messageToDisplay;
    private boolean isInAnimation = false;
    private WindowManager.LayoutParams viewParams;

    //view objects
    private Paint rectPaint, arrowPaint;
    private TextPaint messagePaint;
    private Path arrowPath, arrowShadowPath;
    private StaticLayout messageLayout;
    private Bitmap rawBgBitmap;

    public MessCloudView(Context context) {
        super(context);
        init(null, mContext);
    }

    public MessCloudView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, mContext);
    }

    public MessCloudView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, mContext);
    }

    public MessCloudView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, mContext);
    }

    private void init(@Nullable AttributeSet set, Context mContext) {
        this.mContext = mContext;
        rectPaint = new Paint();
        messagePaint = new TextPaint();
        arrowPath = new Path();
        arrowPaint = new Paint();
        arrowShadowPath = new Path();
        messageToDisplay = new StringBuilder();

        if (set == null) return;
        TypedArray typedArray = getContext().obtainStyledAttributes(set, R.styleable.MessCloudView);

        cloudMessage = typedArray.getString(R.styleable.MessCloudView_cloudMessage);
        messageAuthor = typedArray.getString(R.styleable.MessCloudView_messageAuthor);
        cloudColor = typedArray.getColor(R.styleable.MessCloudView_cloudColor, Color.BLUE);
        cloudMessageColor = typedArray.getColor(R.styleable.MessCloudView_cloudMessageColor, Color.WHITE);
        arrowHeight = typedArray.getDimensionPixelSize(R.styleable.MessCloudView_arrowSize, 15);
        cloudVerticalPadding = typedArray.getDimensionPixelSize(R.styleable.MessCloudView_cloudVerticalPadding, 15);
        cloudHorizontalPadding = typedArray.getDimensionPixelSize(R.styleable.MessCloudView_cloudHorizontalPadding, 15);
        cloudMaxWidth = typedArray.getDimensionPixelSize(R.styleable.MessCloudView_cloudMaxWidth, 500);
        if (typedArray.hasValue(R.styleable.MessCloudView_backgroundImage)){
            bgImageResId = typedArray.getResourceId(R.styleable.MessCloudView_backgroundImage, 0);
            rawBgBitmap = BitmapFactory.decodeResource(getResources(), bgImageResId);
        }

        typedArray.recycle();

        initDrawObjects();

        //calculate the message body with message itself
        if (messageAuthor != null && cloudMessage != null)
            cloudMessage = !messageAuthor.equals("") ? messageAuthor + ": " + cloudMessage : cloudMessage;
        calculateMessageBody();

    }

    private void initDrawObjects() {
        messagePaint.setTextSize(32);
        messagePaint.setAntiAlias(true);
        messagePaint.setColor(cloudMessageColor);

        rectPaint.setColor(cloudColor);
        rectPaint.setStyle(Paint.Style.FILL);
        rectPaint.setAntiAlias(true);
        rectPaint.setShadowLayer(SHADOW_RADIUS, 0, 1, Color.DKGRAY);
        setLayerType(LAYER_TYPE_SOFTWARE, rectPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        //body of cloud
        drawRoundRect(canvas);

        //background image
        drawBackgroundBitmap(canvas);

        //little arrow on the border of cloud
        drawArrow(canvas);

        //message inside cloud
        drawMessage(canvas);
    }

    //draw object methods
    private void drawRoundRect(Canvas canvas) {

        if (currentWall == BubbleCurrentWall.LEFT) {
            //left sided cloud
            canvas.drawRoundRect(arrowHeight,
                    0,
                    getFullWidth() - SHADOW_RADIUS,
                    getBoxHeight() - SHADOW_RADIUS * 1.2f,
                    7, 7, rectPaint);
        } else {
            //right sided cloud
            canvas.drawRoundRect(SHADOW_RADIUS,
                    0,
                    getBoxWidth(),
                    getBoxHeight() - SHADOW_RADIUS * 1.2f,
                    7, 7, rectPaint);
        }
    }

    private void drawBackgroundBitmap(Canvas canvas) {
        if (rawBgBitmap==null) return;

        RoundedBitmapDrawable backgroundImage =
                RoundedBitmapDrawableFactory.create(
                        getResources(),
                        Bitmap.createScaledBitmap(rawBgBitmap,
                                getBoxWidth() - (int) SHADOW_RADIUS,
                                getBoxHeight() - (int) (SHADOW_RADIUS * 1.2f),
                                true)
                );

        if (currentWall == BubbleCurrentWall.LEFT) {
            backgroundImage.setBounds(
                    new Rect(
                            arrowHeight,
                            0,
                            getFullWidth() - (int) SHADOW_RADIUS,
                            getBoxHeight() - (int) (SHADOW_RADIUS * 1.2f)
                    )
            );
        } else {
            backgroundImage.setBounds(
                    new Rect(
                            (int) SHADOW_RADIUS,
                            0,
                            getBoxWidth(),
                            getBoxHeight() - (int) (SHADOW_RADIUS * 1.2f)
                    )
            );
        }
        backgroundImage.setCornerRadius(7f);
        backgroundImage.setAntiAlias(true);
        backgroundImage.setAlpha(70);

        backgroundImage.draw(canvas);
    }

    private void drawArrow(Canvas canvas) {
        //draw shadow first, so we can overdraw it when it reaches cloud...
        drawArrowShadow(canvas);
        arrowPaint.setColor(cloudColor);
        arrowPath.reset();
        if (currentWall == BubbleCurrentWall.LEFT) {
            //left sided cloud
            arrowPath.moveTo(arrowHeight * 1.2f, getBoxHeight() / 2f - arrowHeight);
            arrowPath.lineTo(SHADOW_RADIUS, getBoxHeight() / 2);
            arrowPath.lineTo(arrowHeight * 1.2f, getBoxHeight() / 2f + arrowHeight);
        } else {
            //right sided cloud
            arrowPath.moveTo(getBoxWidth() - (arrowHeight * 0.2f), getBoxHeight() / 2f - arrowHeight);
            arrowPath.lineTo(getBoxWidth() + arrowHeight - SHADOW_RADIUS, getBoxHeight() / 2);
            arrowPath.lineTo(getBoxWidth() - (arrowHeight * 0.2f), getBoxHeight() / 2f + arrowHeight);
        }
        arrowPath.close();
        canvas.drawPath(arrowPath, arrowPaint);
    }

    private void drawArrowShadow(Canvas canvas) {
        arrowShadowPath.reset();
        if (currentWall == BubbleCurrentWall.LEFT) {
            //left sided cloud
            arrowShadowPath.moveTo(arrowHeight, getBoxHeight() / 2f - arrowHeight / 1.2f);
            arrowShadowPath.lineTo(SHADOW_RADIUS, getBoxHeight() / 2);
            arrowShadowPath.lineTo(arrowHeight, getBoxHeight() / 2f + arrowHeight / 1.2f);
        } else {
            //right sided cloud
            arrowShadowPath.moveTo(getBoxWidth(), getBoxHeight() / 2f - arrowHeight / 1.2f);
            arrowShadowPath.lineTo(getBoxWidth() + arrowHeight - SHADOW_RADIUS, getBoxHeight() / 2);
            arrowShadowPath.lineTo(getBoxWidth(), getBoxHeight() / 2f + arrowHeight / 1.2f);
        }
        arrowShadowPath.close();
        canvas.drawPath(arrowShadowPath, rectPaint);
    }

    private void drawMessage(Canvas canvas) {
        int paddingY = cloudVerticalPadding;
        int paddingX = currentWall == BubbleCurrentWall.LEFT
                ? cloudHorizontalPadding + arrowHeight
                : cloudHorizontalPadding;

        canvas.save();
        canvas.translate(paddingX, paddingY);
        messageLayout.draw(canvas);
        canvas.restore();
    }

    private void calculateMessageBody() {
        this.setWillNotDraw(true);
        //single line with max 24 letters
        int totalLetterNum = 0;
        messageToDisplay.setLength(0);
        fullMessHeight = 0;
        fullMessWidth = 0;
        if (cloudMessage == null) return;

        for (String s : cloudMessage.split("\\s")) {
            int wordLength = s.codePointCount(0,s.length());
//            int length = s.length();
            if (wordLength > 24 && totalLetterNum < 24) {
                //the word is too long for the first line
                messageToDisplay.append(s.substring(0, 24));
                totalLetterNum += 24;
                // TODO: 28.04.2020 create a separate function of this block...
                //remove whitespaces at the end of line
                char lastChar = messageToDisplay.charAt(messageToDisplay.length() - 1);
                while (Character.toString(lastChar).equals(" ")){
                    messageToDisplay.deleteCharAt(messageToDisplay.length() - 1);
                    totalLetterNum--;
                    lastChar = messageToDisplay.charAt(messageToDisplay.length() - 1);
                }
                messageToDisplay.append("\n");
                if (wordLength > 48) {
                    //the word is too long for both lines...
                    messageToDisplay.append(s.substring(24, 46));
                    messageToDisplay.append("...");
                    break;
                } else {
                    //finish in next line
                    messageToDisplay.append(s.substring(24));
                    messageToDisplay.append(" ");
                    totalLetterNum += s.substring(24).length() + 1;
                    continue;
                }
            }

            if (totalLetterNum == 24 || (totalLetterNum < 24 && totalLetterNum + wordLength >= 24)) {
                //go to new line, first one is full
                totalLetterNum = 25;
                // TODO: 28.04.2020 create a separate function of this block...
                //remove whitespaces at the end of line
                char lastChar = messageToDisplay.charAt(messageToDisplay.length() - 1);
                while (Character.toString(lastChar).equals(" ")){
                    messageToDisplay.deleteCharAt(messageToDisplay.length() - 1);
                    totalLetterNum--;
                    lastChar = messageToDisplay.charAt(messageToDisplay.length() - 1);
                }
                messageToDisplay.append("\n");

            }

            if (totalLetterNum == 48) {
                //both lines full, the last word ended, there might be more text
                break;
            }

            if (totalLetterNum + wordLength > 48) {
                //both lines are full, but there is still text in message
                int lastWordLength = 46 - totalLetterNum;
                if (lastWordLength > 0) {
                    messageToDisplay.append(s.substring(0, lastWordLength));
                    messageToDisplay.append("...");
                }else if (lastWordLength==0){
                    messageToDisplay.append("...");
                }
                break;
            }
            messageToDisplay.append(s);
            messageToDisplay.append(" ");
            totalLetterNum += wordLength + 1;
        }

        Spannable wordToSpan = new SpannableString(messageToDisplay.toString());
//        wordToSpan.setSpan(new ForegroundColorSpan(Color.BLUE), 0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Log.d(TAG, "calculateMessageBody: AFTER:"+messageToDisplay.toString().replace(" ","$"));
        if (messageAuthor != null)
            wordToSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, messageAuthor.length()+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        wordToSpan.setSpan(new BackgroundColorSpan(Color.LTGRAY), 0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        messageLayout = new StaticLayout(wordToSpan, messagePaint, cloudMaxWidth, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);

        for (int i = 0; i < messageLayout.getLineCount(); i++)
            if (messageLayout.getLineWidth(i) > fullMessWidth) {
                fullMessWidth = (int) messageLayout.getLineWidth(i);
            }
        fullMessHeight = messageLayout.getHeight();
        this.setWillNotDraw(false);
    }


    //overrides
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //always force wrap_content behavior
        setMeasuredDimension(getBoxWidth() + arrowHeight, getBoxHeight());
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getVisibility() == GONE) return true;
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            this.setVisibility(GONE);
        }
        return true;
    }

    //animations
    private void playAnimationHide() {
        int resAnim = currentWall == BubbleCurrentWall.LEFT
                ? R.anim.mess_cloud_hide_left_anim
                : R.anim.mess_cloud_hide_right_anim;
        Animation hideAnim = AnimationUtils.loadAnimation(getContext(), resAnim);
        hideAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                MessCloudView.super.setVisibility(GONE);
                isInAnimation = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        this.startAnimation(hideAnim);

    }

    private void playAnimationShow() {
        int resAnim = currentWall == BubbleCurrentWall.LEFT
                ? R.anim.mess_cloud_shown_left_anim
                : R.anim.mess_cloud_shown_right_anim;

        Animation shownAnim = AnimationUtils.loadAnimation(getContext(), resAnim);
        shownAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
//                MessCloudView.super.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
//                executorService.schedule(new Runnable() {
//                    @Override
//                    public void run() {
//                        MessCloudView.super.setVisibility(GONE);
//                    }
//                }, VISIBILITY_DURATION, TimeUnit.MILLISECONDS);
                isInAnimation = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        this.startAnimation(shownAnim);
    }


    //setters
    public void setCloudColor(int cloudColor) {
        this.cloudColor = cloudColor;
    }

    public void move(int x, int y) {
        FrameLayout parent = (FrameLayout) this.getParent();
        WindowManager.LayoutParams parentParams = (WindowManager.LayoutParams) parent.getLayoutParams();
        parentParams.x = x;
        parentParams.y = y;
        getWindowManager().updateViewLayout(parent, parentParams);
    }

    public void setCloudMessage(String cloudMessage, String messageAuthor) {
        if (messageAuthor != null) {
            cloudMessage = !messageAuthor.equals("") ? messageAuthor + ": " + cloudMessage : cloudMessage;
        }
        this.messageAuthor = messageAuthor;
        if (!cloudMessage.equals(this.cloudMessage)) {
            this.cloudMessage = cloudMessage;
            calculateMessageBody();
            Log.d(TAG, "setCloudMessage: INVALIDATED");
            postInvalidate();
        }
    }

    public void setCurrentWall(BubbleCurrentWall currentWall) {
        if (this.currentWall != currentWall) {
            this.currentWall = currentWall;
            postInvalidate();
        }
    }

    public void setViewParams(WindowManager.LayoutParams viewParams) {
        this.viewParams = viewParams;
    }

    public void forceHide() {
        super.setVisibility(GONE);
    }


    @Override
    public void setVisibility(int visibility) {
        if (visibility != getVisibility()) {
            switch (visibility) {
                case GONE:
                    if (!isInAnimation) {
                        isInAnimation = true;
                        Log.d(TAG, "setVisibility: ANIMATION GONE");
                        playAnimationHide();
                        return;
                    }
                    break;
                case VISIBLE:
                    Log.d(TAG, "setVisibility: ANIMATION SHOW");
                    super.setVisibility(VISIBLE);
                    playAnimationShow();
                    break;
                default:
                    super.setVisibility(visibility);
                    break;
            }
        }
    }


    //getters
    private int getColorRes(int color) {
        return getResources().getColor(color);
    }

    public int getFullWidth() {
        return getBoxWidth() + arrowHeight;
    }

    public int getFullHeight() {
        return getBoxHeight();
    }

    private int getBoxWidth() {
        return fullMessWidth + (cloudHorizontalPadding * 2);
    }

    private int getBoxHeight() {
        return fullMessHeight + (cloudVerticalPadding * 2);
    }

    private WindowManager getWindowManager() {
        return (WindowManager) getContext().getSystemService(WINDOW_SERVICE);
    }

    public WindowManager.LayoutParams getViewParams() {
        return viewParams;
    }

    //utility
    private int dpToPx(int dp) {
        float density = getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }

    public enum BubbleCurrentWall {
        LEFT,
        RIGHT
    }

}
