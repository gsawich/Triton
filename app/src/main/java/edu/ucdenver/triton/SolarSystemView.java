package edu.ucdenver.triton;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class SolarSystemView extends SurfaceView implements SurfaceHolder.Callback {

    int sWidth;
    int sHeight;
    private Context ctx;
    private final int PLANETNUM = 8;
    private ExecutorService pool;
    private Paint planet;
    private PointF center;
    private static final double rMin[] = {0.307, 0.718, 0.983, 1.382, 4.951, 9.024, 18.33, 29.81, 29.656};   //AU
    private static final double rMax[] = {0.467, 0.728, 1.017, 1.666, 5.455, 10.086, 20.11, 30.33, 49.319};   //AU
    private static final String planetName[] = {"Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune"};
    private static final double eccentricityArr[] = {0.206, 0.007, 0.017, 0.093, 0.048, 0.056, 0.047, 0.009, 0.248, 0.617, 0.529, 0.967};   //Dimensonless
    private static final double period[] = {0.241, 0.615, 1.0, 1.881, 11.86, 29.46, 84.01,164.8, 248.5, 3.61, 2.76, 75.32}; //Years
    private Planet planetArray [] = new Planet[PLANETNUM];

    TextView dateDisplay;
    Calendar cal;

    PlanetPosRun[] posRunArr;
    PlanetInitThread[] pInitArr;

    //Vector<float[]> floatVec;
    //float[][] floatHolder;
    //need to change to a long time storage container.
    Stack floatArrHolder;

    /*Bitmaps for all 9 planets*/
    Bitmap sol;
    int solW;
    int solH;
    Bitmap[] bArr;
    int[] w_hHolder;

    //Scale tools
    private ScaleGestureDetector scaleDetector;
    private float scaleFactor = 1.0f;
    PointF sun;
    //Animation
    static final double MIN_FPS = 30;
    static final double MED_FPS = 90;
    static final double MAX_FPS = 360;
    public int speed;
    double FRAME_TIME_SECONDS, FRAME_TIME_MILLISECONDS;
    double tPrevFrame, tEOF, deltaT;
    private double currentDay;
    DrawThread thread;
    //1 = forward one day. -1 = backward one day.
    int FORWARD_BACKWARD_ONE_DAY = 1;

    //****************************************************************************************************************************************************
//Constructors
    public SolarSystemView(Context context) {
        super(context);
        init(context);
    }

    public SolarSystemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public SolarSystemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

//***********************************************************************************************************************************************
//Nested Classes

    private class UpdateDate extends AsyncTask<Calendar, Void, String> {

        private SimpleDateFormat formatDate = new SimpleDateFormat("yyyy:MM:dd");

        @Override
        protected String doInBackground(Calendar... params) {
            String era;
            if (cal.get(GregorianCalendar.ERA) == GregorianCalendar.AD) {
                era = "AD";
            } else {
                era = "BC";
            }
            formatDate.setCalendar(cal);

            /*formatDate = String.valueOf(cal.get(GregorianCalendar.MONTH) + 1)
                    + "-" + String.valueOf(cal.get(GregorianCalendar.DAY_OF_MONTH))
                    + "-" + String.valueOf(cal.get(GregorianCalendar.YEAR) + "-" + era);*/
            return formatDate.format(cal.getTime());
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dateDisplay.setText(s);
        }
    }

    private class DrawThread extends Thread {

        private SurfaceHolder surfaceHolder;
        private SolarSystemView ssv;
        private Object lock;   //Dummy lock object
        private boolean paused, finished;

        public DrawThread(SurfaceHolder surfaceHolder, SolarSystemView gview) {
            this.surfaceHolder = surfaceHolder;
            this.ssv = gview;
            lock = new Object();
            paused = false;
            finished = false;
        }

        public SurfaceHolder getSurfaceHolder() {
            return surfaceHolder;
        }

        public void onPause() {
            synchronized (lock) {
                paused = true;
            }
        }

        public void onResume() {
            synchronized (lock) {
                paused = false;
                lock.notifyAll();
            }
        }
        @Override
        public void run() {
            Canvas c;
            while(!finished) {
                c = null;
                pool = Executors.newFixedThreadPool(PLANETNUM);
                for (int i = 0; i < PLANETNUM; ++i) {
                    pool.execute(posRunArr[i]);
                }
                pool.shutdown();
                try {
                    pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                new UpdateDate().execute(cal);
                tPrevFrame = System.currentTimeMillis();
                try {
                    c = surfaceHolder.lockCanvas(null);
                    synchronized (surfaceHolder) {
                        //ToDo: Postinvalidate or invalidate?
                        ssv.draw(c);
                    }
                } finally {
                    if (c != null) {
                        surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
                cal.add(GregorianCalendar.DAY_OF_MONTH, FORWARD_BACKWARD_ONE_DAY*speed);
                currentDay = getJulianCalDay();
                tEOF = System.currentTimeMillis();
                deltaT = tEOF - tPrevFrame;
                if (deltaT < FRAME_TIME_MILLISECONDS) {
                    deltaT = FRAME_TIME_MILLISECONDS - deltaT;
                    try {
                        Thread.sleep((long) deltaT);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                synchronized (lock) {
                    while (paused) {
                        try {
                            lock.wait();
                        }
                        catch(InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    private class PlanetPosRun implements Runnable {

        int planetID;
        public PlanetPosRun(int id){
            planetID = id;
        }
        @Override
        public void run() {
            planetArray[planetID].calculatePosition(currentDay);
        }
    }

    private class PlanetInitThread implements Runnable {
        int planetID;

        public PlanetInitThread(int id) {
            planetID = id;
        }

        @Override
        public void run() {
            planetArray[planetID] = new Planet(planetID, sun, 1.0f, solW);            //*Note might need to make sun obj threadsafe.
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        float span;
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector){
            span = detector.getCurrentSpan();
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            //scaleFactor = detector.getScaleFactor();
            scaleFactor *= 1+((1-(span/detector.getCurrentSpan() ))/10);
            if (scaleFactor < 0.01) { scaleFactor = 0.01f; }
            if (scaleFactor > 2) { scaleFactor = 2; }
            Log.i("ScaleFactor", "Scale Factor: " + scaleFactor );
            for (Planet p: planetArray) p.setScale(scaleFactor);
            return true;
        }
    }

//************************************************************************************************************************************************************************************


    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        return true;
    }
    /*
    public void incZoom() {
        scaleFac += 0.1f;
        Planet.setZoomFac(scaleFac);
    }
    public void decZoom() {
        if (scaleFac - 0.1f > 0) {
            scaleFac -= 0.1f;
            Planet.setZoomFac(scaleFac);
        }
    }
    */
    public void setTextView(TextView tview) {
        dateDisplay = tview;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new DrawThread(getHolder(), this);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        if(thread.paused) {
            thread.finished = true;
        }
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void setForwardBackward(int i) {
        FORWARD_BACKWARD_ONE_DAY = i;
    }

    public synchronized void pause(){
        thread.onPause();
        Log.i("DrawThread", "Draw Thread State: " + thread.getState() );
    }

    public synchronized void resume() {
        thread.onResume();
        Log.i("DrawThread", "Draw Thread State: " + thread.getState() );
    }

    public void init(Context context) {

        ctx = context;
        cal = GregorianCalendar.getInstance();
        sol = BitmapFactory.decodeResource(getResources(),R.drawable.sol);
        solW = sol.getWidth();
        solH = sol.getHeight();
        speed = 1; //One month per second
        /*bArr = new Bitmap[9];
        w_hHolder = new int[18];

        int k = 0;

        for(int i = 0;i < 9; ++i) {

            switch (i) {

                case 0:
                    bArr[i] = BitmapFactory.decodeResource(getResources(),R.drawable.sol);
                    w_hHolder[k] = bArr[i].getWidth();
                    ++k;
                    w_hHolder[k] = bArr[i].getHeight();
                    ++k;
                    break;
                case 1:
                    bArr[i] = BitmapFactory.decodeResource(getResources(),R.drawable.rsz_mercury);
                    w_hHolder[k] = bArr[i].getWidth();
                    ++k;
                    w_hHolder[k] = bArr[i].getHeight();
                    ++k;
                    break;
                case 2:
                    bArr[i] = BitmapFactory.decodeResource(getResources(),R.drawable.rsz_mercury);
                    w_hHolder[k] = bArr[i].getWidth();
                    ++k;
                    w_hHolder[k] = bArr[i].getHeight();
                    ++k;
                    break;
                case 3:
                    bArr[i] = BitmapFactory.decodeResource(getResources(),R.drawable.rsz_mercury);
                    w_hHolder[k] = bArr[i].getWidth();
                    ++k;
                    w_hHolder[k] = bArr[i].getHeight();
                    ++k;
                    break;
                case 4:
                    bArr[i] = BitmapFactory.decodeResource(getResources(),R.drawable.rsz_mercury);
                    w_hHolder[k] = bArr[i].getWidth();
                    ++k;
                    w_hHolder[k] = bArr[i].getHeight();
                    ++k;
                    break;
                case 5:
                    bArr[i] = BitmapFactory.decodeResource(getResources(),R.drawable.rsz_mercury);
                    w_hHolder[k] = bArr[i].getWidth();
                    ++k;
                    w_hHolder[k] = bArr[i].getHeight();
                    ++k;
                    break;
                case 6:
                    bArr[i] = BitmapFactory.decodeResource(getResources(),R.drawable.rsz_mercury);
                    w_hHolder[k] = bArr[i].getWidth();
                    ++k;
                    w_hHolder[k] = bArr[i].getHeight();
                    ++k;
                    break;
                case 7:
                    bArr[i] = BitmapFactory.decodeResource(getResources(),R.drawable.rsz_mercury);
                    w_hHolder[k] = bArr[i].getWidth();
                    ++k;
                    w_hHolder[k] = bArr[i].getHeight();
                    ++k;
                    break;
                case 8:
                    bArr[i] = BitmapFactory.decodeResource(getResources(),R.drawable.rsz_mercury);
                    w_hHolder[k] = bArr[i].getWidth();
                    ++k;
                    w_hHolder[k] = bArr[i].getHeight();
                    ++k;
                    break;
                default:

                    break;

            }

        }*/

        currentDay = getJulianCalDay();

        //floatArrHolder = new Stack();

        //pool = Executors.newFixedThreadPool(PLANETNUM);

        scaleDetector = new ScaleGestureDetector(ctx, new ScaleListener());
        posRunArr = new PlanetPosRun[PLANETNUM];
        for(int i = 0; i < PLANETNUM; ++i){
            posRunArr[i] = new PlanetPosRun(i);
        }

        pInitArr = new PlanetInitThread[PLANETNUM];
        for(int i = 0; i < PLANETNUM; ++i){
            pInitArr[i] = new PlanetInitThread(i);
        }
        //floatVec = new Vector<>();
        //floatHolder = new float[][];
        getHolder().addCallback(this);
        planet = new Paint(Paint.ANTI_ALIAS_FLAG);
        planet.setStyle(Paint.Style.FILL_AND_STROKE);
        planet.setStrokeWidth(3);
    }

    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        sWidth = w;
        sHeight = h;
        center = new PointF();
        sun = new PointF();
        center.x = w / 2;
        center.y = h / 2;
        //Arbitrary sun position
        sun.x = w / 2;
        sun.y = h / 2;
        //Scaling needs revision
        //scaleFactor = Math.min(center.x, center.y) / 2;
        pool = Executors.newFixedThreadPool(PLANETNUM);
        scaleFactor = Math.max(center.x, center.y)/8;
        pool = Executors.newFixedThreadPool(PLANETNUM);
        for (int i = 0; i < PLANETNUM; ++i) {
            pool.execute(pInitArr[i]);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //ToDo: Make thread safe for changing speed
    public void setSpeed(int s) {
        speed = s;
        /*switch (choice) {
            //Increment by one month
            case 0:
                FRAME_TIME_SECONDS = 1/MIN_FPS;
                FRAME_TIME_MILLISECONDS = FRAME_TIME_SECONDS * 1000;
                break;
            //Increment by three months
            case 1:
                FRAME_TIME_SECONDS = 1/MED_FPS;
                FRAME_TIME_MILLISECONDS = FRAME_TIME_SECONDS * 1000;
                break;
            //Increment by one year
            case 2:
                FRAME_TIME_SECONDS = 1/MAX_FPS;
                FRAME_TIME_MILLISECONDS = FRAME_TIME_SECONDS * 1000;
                break;
        }*/
    }

    @Override
    public void draw(Canvas c) {
        super.draw(c);
        c.save();
        c.drawColor(Color.BLACK);
        //Draw Sun
        /*planet.setStyle(Paint.Style.FILL_AND_STROKE);
        planet.setColor(Color.YELLOW);
        c.drawCircle(sun.x, sun.y, 4, planet);*/
        //c.drawBitmap(bArr[0],sun.x-(w_hHolder[0]/2),sun.y-(w_hHolder[1]/2),null);
        c.drawBitmap(sol, sun.x -(solW/2), sun.y - (solH/2), null);

        //Draw Planets
        planet.setColor(Color.GREEN);
        planet.setStyle(Paint.Style.FILL_AND_STROKE);
        //int k = 2;
        for (int i = 0; i < PLANETNUM; ++i) {
            float x = planetArray[i].currentLocation.x;
            float y = planetArray[i].currentLocation.y;
            if(x < 0.0f) {
                x = 3.0f;
            }
            else if (x > sWidth) {
                x = sWidth-3.0f;
            }
            if(y < 0.0f) {
                y = 3.0f;
            }
            else if (y > sHeight) {
                y = sHeight-3.0f;
            }
            c.drawCircle(x, y, 3, planet);
            /*c.drawBitmap(bArr[i+1],(planetArray[i].currentLocation.x)-(w_hHolder[k]/2),(planetArray[i].currentLocation.y)-(w_hHolder[k+1]/2),null);
            k += 2;*/
        }
        c.restore();
    }

    private double getJulianCalDay() {
        return 367*cal.get(GregorianCalendar.YEAR) - (7*(cal.get(GregorianCalendar.YEAR)+(cal.get(GregorianCalendar.MONTH)+10)/12) / 4) + 275*(cal.get(GregorianCalendar.MONTH)+1)/9 + cal.get(GregorianCalendar.DAY_OF_MONTH) - 730530;
    }
    public synchronized void pushArr(float[] mArr) {
        floatArrHolder.push(mArr);
    }
}