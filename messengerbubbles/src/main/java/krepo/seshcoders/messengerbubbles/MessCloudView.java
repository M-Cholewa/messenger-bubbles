package krepo.seshcoders.messengerbubbles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"SuspiciousNameCombination", "unused"})
public class MessCloudView extends View {

    //consts
    private static final String TAG = "MessCloudView";
    private static final long VISIBILITY_DURATION = 5000;

    //attribute vars
    private String cloudMessage = "";
    private int cloudColor = Color.BLUE;
    private int cloudMessageColor = Color.WHITE;
    private int arrowHeight = 15;
    private int cloudVerticalPadding = 15;
    private int cloudHorizontalPadding = 15;


    //vars
    private Context mContext;
    private BubbleCurrentWall currentWall = BubbleCurrentWall.LEFT;
    private int fullMessWidth;
    private int fullMessHeight;
    private StringBuilder messageToDisplay;
    private boolean isInAnimation = false;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    //view objects
    private Paint rectPaint;
    private Paint messagePaint;
    private Path arrowPath;
    private Rect textRect;

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
        rectPaint = new Paint();
        messagePaint = new Paint();
        arrowPath = new Path();
        textRect = new Rect();
        this.mContext = mContext;
        messageToDisplay = new StringBuilder();

        if (set == null) return;
        TypedArray typedArray = getContext().obtainStyledAttributes(set, R.styleable.MessCloudView);

        cloudMessage = typedArray.getString(R.styleable.MessCloudView_cloudMessage);
        cloudColor = typedArray.getColor(R.styleable.MessCloudView_cloudColor, Color.BLUE);
        cloudMessageColor = typedArray.getColor(R.styleable.MessCloudView_cloudMessageColor, Color.WHITE);
        arrowHeight = typedArray.getDimensionPixelSize(R.styleable.MessCloudView_arrowSize, 15);
        cloudVerticalPadding = typedArray.getDimensionPixelSize(R.styleable.MessCloudView_cloudVerticalPadding, 15);
        cloudHorizontalPadding = typedArray.getDimensionPixelSize(R.styleable.MessCloudView_cloudHorizontalPadding, 15);

        typedArray.recycle();

        messagePaint.setAntiAlias(true);
        messagePaint.setColor(cloudMessageColor);
        messagePaint.setTextAlign(Paint.Align.LEFT);
        messagePaint.setTextSize(40);

        //calculate the message body with message itself
        calculateMessageBody();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // TODO: 05.02.2020 move this block upside, cuz of onDraw constant refreshing
        rectPaint.setColor(cloudColor);
        rectPaint.setStyle(Paint.Style.FILL);
        rectPaint.setAntiAlias(true);

        //body of cloud
        drawRoundRect(canvas);

        //little arrow on the border of cloud
        drawArrow(canvas);

        //message inside cloud
        drawMessage(canvas);
    }

    //draw object methods
    private void drawRoundRect(Canvas canvas) {
        if(currentWall == BubbleCurrentWall.LEFT){
            //left sided cloud
            canvas.drawRoundRect(arrowHeight, 0, getBoxWidth() + arrowHeight, getBoxHeight(), 10, 10, rectPaint);
        }else {
            //right sided cloud
            canvas.drawRoundRect(0, 0, getBoxWidth(), getBoxHeight(), 10, 10, rectPaint);
        }

    }

    private void drawArrow(Canvas canvas) {
        if (currentWall == BubbleCurrentWall.LEFT){
            //left sided cloud
            arrowPath.moveTo(arrowHeight, getBoxHeight() / 2f - arrowHeight / 1.2f);
            arrowPath.lineTo(0, getBoxHeight() / 2);
            arrowPath.lineTo(arrowHeight, getBoxHeight() / 2f + arrowHeight / 1.2f);
            arrowPath.close();
        } else{
            //right sided cloud
            arrowPath.moveTo(getBoxWidth(), getBoxHeight() / 2f - arrowHeight / 1.2f);
            arrowPath.lineTo(getBoxWidth()+arrowHeight, getBoxHeight() / 2);
            arrowPath.lineTo(getBoxWidth(), getBoxHeight() / 2f + arrowHeight / 1.2f);
            arrowPath.close();
        }


        canvas.drawPath(arrowPath, rectPaint);
    }

    private void drawMessage(Canvas canvas) {

        int paddingY = cloudVerticalPadding;
        int paddingX = currentWall == BubbleCurrentWall.LEFT
                ? cloudHorizontalPadding + arrowHeight
                : cloudHorizontalPadding;

        for (String line : messageToDisplay.toString().split("\n")) {
            messagePaint.getTextBounds(line, 0, line.length(), textRect);
            canvas.drawText(line, paddingX - textRect.left, paddingY + textRect.height(), messagePaint);
            paddingY += messagePaint.descent() - messagePaint.ascent();
        }

    }

    private void calculateMessageBody() {
        this.setWillNotDraw(true);
        //single line with max 24 letters
        int totalLetterNum = 0;
        messageToDisplay.setLength(0);
        fullMessHeight = 0;
        fullMessWidth = 0;
        if (cloudMessage == null) return;

        for (String s : cloudMessage.split("\\s+")) {

            if (s.length() > 24 && totalLetterNum < 24) {
                //the word is too long for the first line
                messageToDisplay.append(s.substring(0, 24));
                totalLetterNum += 24;
                messageToDisplay.append("\n");
                if (s.length() > 48) {
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

            if (totalLetterNum == 24 || (totalLetterNum < 24 && totalLetterNum + s.length() > 24)) {
                //go to new line, first one is full
                totalLetterNum = 25;
                messageToDisplay.append("\n");
            }

            if (totalLetterNum == 48) {
                //both lines full, the last word ended, there might be more text
                break;
            }

            if (totalLetterNum + s.length() > 48) {
                //both lines are full, but there is still text in message
                int lastWordLength = 46 - totalLetterNum;
                if (lastWordLength > 0) {
                    messageToDisplay.append(s.substring(0, lastWordLength));
                    messageToDisplay.append("...");
                }
                break;
            }
            messageToDisplay.append(s);
            messageToDisplay.append(" ");
            totalLetterNum += s.length() + 1;
        }

        int iterator = 0;
        for (String line : messageToDisplay.toString().split("\n")) {
            messagePaint.getTextBounds(line, 0, line.length(), textRect);
            fullMessHeight += messagePaint.descent() - messagePaint.ascent();
            if (textRect.width() - textRect.left > fullMessWidth) {
                fullMessWidth = textRect.width() - textRect.left;
            }
            iterator++;
        }
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
        Animation hideAnim = AnimationUtils.loadAnimation(getContext(), R.anim.mess_cloud_hide_anim);
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
        Animation shownAnim = AnimationUtils.loadAnimation(getContext(), R.anim.mess_cloud_shown_anim);
        shownAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                MessCloudView.super.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                executorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        MessCloudView.this.setVisibility(GONE);
                    }
                }, VISIBILITY_DURATION, TimeUnit.MILLISECONDS);
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

    public void setCloudMessage(String cloudMessage) {
        if (!cloudMessage.equals(this.cloudMessage)) {
            this.cloudMessage = cloudMessage;
            calculateMessageBody();
            Log.d(TAG, "setCloudMessage: INVALIDATED");
            postInvalidate();
        }
    }

    public void setCurrentWall(BubbleCurrentWall currentWall) {
        if(this.currentWall != currentWall){
            this.currentWall = currentWall;
            postInvalidate();
        }
    }


    @SuppressLint("SwitchIntDef")
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
                    playAnimationShow();
                    break;
            }
        }
    }


    //getters
    private int getColorRes(int color) {
        return getResources().getColor(color);
    }

    private int getBoxWidth() {
        return fullMessWidth + (cloudHorizontalPadding * 2);
    }

    private int getBoxHeight() {
        Log.d(TAG, "fullMessHeight: " + fullMessHeight + " cloudVerticalPadding " + (cloudVerticalPadding * 2));
        return fullMessHeight + (cloudVerticalPadding * 2);
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
