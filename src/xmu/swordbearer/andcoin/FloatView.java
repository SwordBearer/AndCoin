package xmu.swordbearer.andcoin;

import java.text.DecimalFormat;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class FloatView extends SurfaceView implements SurfaceHolder.Callback {

	SurfaceHolder holder = this.getHolder();
	private static final int lineHeight = 30;

	private Paint drawPaint;
	

	public FloatView(Context context) {
		super(context);
		holder = this.getHolder();
		holder.addCallback(this);
		holder.setFormat(PixelFormat.TRANSLUCENT);
		drawPaint = new Paint();
		drawPaint.setColor(Color.WHITE);
	}

	public void displayData(long totalMem, long availMem,
			List<String> processData, int color, int fontSize) {
		drawPaint.setColor(color);
		drawPaint.setTextSize(fontSize);

		Canvas canvas = holder.lockCanvas();
		if (null == canvas) {
			return;
		}
		canvas.drawColor(0, PorterDuff.Mode.CLEAR);
		canvas.drawColor(Color.argb(150, 100, 100, 100));
		// canvas.drawText("CPU " + cpu, 10, 10, p);
		float memPercent = ((float) (totalMem - availMem) / totalMem) * 100;
		DecimalFormat dFormat = new DecimalFormat("0.0");
		canvas.drawText(
				"Memory " + availMem + "    " + dFormat.format(memPercent)
						+ "%", 10, 10 + lineHeight, drawPaint);
		int count = processData.size();
		for (int i = 0; i < count; i++) {
			canvas.drawText(processData.get(i), 10, 10 + (2 + i) * lineHeight,
					drawPaint);
		}
		holder.unlockCanvasAndPost(canvas);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		System.out.println("这个被调用了吗 surface Created");
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		System.out.println("这个被调用了吗 surface Changed");
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
	}

}
