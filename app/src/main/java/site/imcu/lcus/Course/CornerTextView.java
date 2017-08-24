package site.imcu.lcus.course;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.widget.AppCompatTextView;

/**
 * Created by SHIELD_7 on 17/8/1.
 *
 */
public class CornerTextView extends AppCompatTextView{

    private int mBgColor = 0; //背景颜色
    private int mCornerSize = 0; //圆角大小

    public CornerTextView(Context context, int bgColor, int cornerSize) {
        super(context);
        mBgColor = bgColor;
        mCornerSize = cornerSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(mBgColor);
        paint.setAlpha(180);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight()), mCornerSize, mCornerSize, paint);

        super.onDraw(canvas);
    }
}
