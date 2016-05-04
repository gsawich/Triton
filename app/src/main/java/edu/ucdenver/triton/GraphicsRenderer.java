package edu.ucdenver.triton;


import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GraphicsRenderer implements GLSurfaceView.Renderer{

    private final int N = 8; //Number of satellites
    private final float[][] mvpMatrix  = new float[N+1][16]; //Vector projection matrix
    private final float[][] projMatrix = new float[N+1][16]; //View projection matrix
    private final float[][] vectMatrix = new float[N+1][16]; //Vector matrix
    private final float[][] rotMatrix  = new float[N+1][16]; //Rotation matrix

    public PointF sun;
    public float scaleFactor;
    public double currentDate;
    public Calendar cal;
    public int speed;
    public boolean isPaused;

    private Planet[] planetArray = new Planet[N];
    private Billboard[] planetBillboardArray = new Billboard[N]; //Planet billboard array
    private Billboard sunBillboard;
    private float[] viewMatrix = new float[16]; //Camera matrix
    private float camX, camY, camZ; //Camera position
    private float camXd, camYd, camZd; //Camera motion
    private float lookX, lookY, lookZ; //Camera vector
    private float upX, upY, upZ; //Camera orthogonal vector
    private Context thisContext;

    public GraphicsRenderer(Context context){
        this.thisContext = context;
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //Set camera matrix
        camX = 0;
        camY = 1000;
        camZ = 300;
        camXd = 0f;
        camYd = 0f;
        camZd = 0f;
        lookX = 0;
        lookY = 0;
        lookZ = 0;
        upX = 0;
        upY = 0;
        upZ = 1;
        Matrix.setLookAtM(viewMatrix, 0, camX, camY, camZ, lookX, lookY, lookZ, upX, upY, upZ);

        cal = GregorianCalendar.getInstance();
        sun = new PointF();
        scaleFactor = 1;
        currentDate = getJulianCalDay();
        speed = 10;

        for (int i = 0; i < N; i++) {
            planetArray[i] = new Planet(i, sun, scaleFactor);
            planetBillboardArray[i] = new Billboard(1, 0, 0, 1+i*i);
        }

        sunBillboard = new Billboard(1,0,0, 5.0f);

        //Set background color
        GLES20.glClearColor(0.001f, 0.0f, 0.01f, 1.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Matrix.setLookAtM(viewMatrix, 0, camX, camY, camZ, lookX, lookY, lookZ, upX, upY, upZ);

        // this projection matrix is applied to object coordinates
        float ratio = (float) width / height;
        for (int i = 0; i < N+1; i++) {
            Matrix.frustumM(projMatrix[i], 0, -ratio, ratio, -1, 1, 1, 10);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //Draw Background
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if (!isPaused) {
            camX += camXd;
            camY += camYd;
            camZ += camZd;
            cal.add(Calendar.DAY_OF_YEAR, speed);
            currentDate = getJulianCalDay();
        }

        Matrix.setLookAtM(viewMatrix, 0, camX, camY, camZ, lookX, lookY, lookZ, upX, upY, upZ);

        for (int i = 0; i < N; i++) {
            planetArray[i].calculatePosition(currentDate);
            Billboard curBB = planetBillboardArray[i];
            curBB.setCoords(planetArray[i]);
            // Set the billboard to look at the camera
            Matrix.setLookAtM(vectMatrix[i], 0, curBB.getX(), curBB.getY(), curBB.getZ(),
                    camX, camY, camZ, upX, upY, upZ);
            //Matrix.translateM(mVMatrix[i], 0, coords[i][0], coords[i][1], coords[i][2]);

            // Calculate the projection and view transformation
            Matrix.multiplyMM(mvpMatrix[i], 0, projMatrix[i], 0, viewMatrix, 0);
            Matrix.setRotateM(rotMatrix[i], 0, 0, curBB.getX(), curBB.getY(), curBB.getZ());

            // Combine the rotation matrix with the projection and camera view
            Matrix.multiplyMM(mvpMatrix[i], 0, rotMatrix[i], 0, mvpMatrix[i], 0);
            Matrix.rotateM(mvpMatrix[i], 0, 90, 1.0f, 0.0f, 0.0f);
            // Draw billboard
            planetBillboardArray[i].draw(mvpMatrix[i]);
        }

        //Draw Sun
        // Set the billboard to look at the camera
        Matrix.setLookAtM(vectMatrix[N], 0, sun.x, sun.y, 0,
                camX, camY, camZ, upX, upY, upZ);
        //Matrix.translateM(mVMatrix[i], 0, coords[i][0], coords[i][1], coords[i][2]);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mvpMatrix[N], 0, projMatrix[N], 0, viewMatrix, 0);
        Matrix.setRotateM(rotMatrix[N], 0, 0, sun.x, sun.y, 0);

        // Combine the rotation matrix with the projection and camera view
        Matrix.multiplyMM(mvpMatrix[N], 0, rotMatrix[N], 0, mvpMatrix[N], 0);
        Matrix.rotateM(mvpMatrix[N], 0, 90, 1.0f, 0.0f, 0.0f);

        // Draw billboard
        sunBillboard.draw(mvpMatrix[N]);
    }

    public static int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    private double getJulianCalDay() {
        return 367*cal.get(GregorianCalendar.YEAR) - (7*(cal.get(GregorianCalendar.YEAR)+(cal.get(GregorianCalendar.MONTH)+10)/12) / 4) + 275*(cal.get(GregorianCalendar.MONTH)+1)/9 + cal.get(GregorianCalendar.DAY_OF_MONTH) - 730530;
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int s) {
        speed = s;
    }
}
