package krepo.seshcoders.bubblesexample.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import krepo.seshcoders.bubblesexample.R;

@SuppressWarnings({"SuspiciousNameCombination", "unused"})
public class MessCloudView extends View {

    private static final String TAG = "MessCloudView";

    //attribute vars
    private String cloudMessage;
    private int cloudColor;
    private int arrowHeight = 15;
    private int cloudVerticalPadding = 15;
    private int cloudHorizontalPadding = 15;


    //vars
    private Context mContext;
    private BubbleCurrentWall currentWall;
    private int fullMessWidth;
    private int fullMessHeight;
    private StringBuilder messageToDisplay;

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

        messagePaint.setAntiAlias(true);
        messagePaint.setColor(getColorRes(android.R.color.black));
        messagePaint.setTextAlign(Paint.Align.LEFT);
        messagePaint.setTextSize(40);

        if (set == null) return;
        TypedArray typedArray = getContext().obtainStyledAttributes(set, R.styleable.MessCloudView);

        cloudMessage = typedArray.getString(R.styleable.MessCloudView_cloudMessage);
        cloudColor = typedArray.getColor(R.styleable.MessCloudView_cloudColor, Color.GREEN);
        arrowHeight = typedArray.getDimensionPixelSize(R.styleable.MessCloudView_arrowSize, 15);
        cloudVerticalPadding = typedArray.getDimensionPixelSize(R.styleable.MessCloudView_cloudVerticalPadding, 15);
        cloudHorizontalPadding = typedArray.getDimensionPixelSize(R.styleable.MessCloudView_cloudHorizontalPadding, 15);

        typedArray.recycle();

        //calculate the message body with message itself
        calculateMessageBody();
    }

    @Override
    protected void onDraw(Canvas canvas) {
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

        canvas.drawRoundRect(arrowHeight, 0, getBoxWidth()+arrowHeight, getBoxHeight(), 10, 10, rectPaint);
    }

    private void drawArrow(Canvas canvas) {
        arrowPath.moveTo(arrowHeight, getBoxHeight() /2f - arrowHeight / 1.2f);
        arrowPath.lineTo(0, getBoxHeight() / 2);
        arrowPath.lineTo(arrowHeight, getBoxHeight() /2f + arrowHeight / 1.2f);
        arrowPath.close();

        canvas.drawPath(arrowPath, rectPaint);
    }

    private void drawMessage(Canvas canvas) {

        int paddingY = cloudVerticalPadding;
        int paddingX = cloudHorizontalPadding + arrowHeight;

        for (String line : messageToDisplay.toString().split("\n")) {
            messagePaint.getTextBounds(line, 0, line.length(), textRect);
            canvas.drawText(line, paddingX-textRect.left, paddingY+textRect.height(), messagePaint);
            paddingY += messagePaint.descent() - messagePaint.ascent();
        }

    }

    private void calculateMessageBody() {
        //single line with max 24 letters
        int totalLetterNum = 0;

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


        for (String line : messageToDisplay.toString().split("\n")) {
            messagePaint.getTextBounds(line, 0, line.length(), textRect);
            fullMessHeight += messagePaint.descent() - messagePaint.ascent();
//            fullMessHeight += textRect.height();
            if(textRect.width() - textRect.left>fullMessWidth){
                fullMessWidth = textRect.width() - textRect.left;
            }
        }
    }

    //edit wrap_content behavior
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    //setters
    public void setCloudColor(int cloudColor) {
        this.cloudColor = cloudColor;
    }

    public void setCloudMessage(String cloudMessage) {
        this.cloudMessage = cloudMessage;
    }

    public void setCurrentWall(BubbleCurrentWall currentWall) {
        this.currentWall = currentWall;
    }


    //getters
    private int getColorRes(int color) {
        return getResources().getColor(color);
    }

    private int dpToPx(int dp) {
        float density = getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }

    private int getBoxWidth() {
        return fullMessWidth + (cloudHorizontalPadding * 2);
    }

    private int getBoxHeight() {
        Log.d(TAG, "fullMessHeight: "+fullMessHeight + " cloudVerticalPadding " +(cloudVerticalPadding * 2));
        return fullMessHeight + (cloudVerticalPadding * 2);
    }


    public enum BubbleCurrentWall {
        LEFT,
        RIGHT
    }


}
