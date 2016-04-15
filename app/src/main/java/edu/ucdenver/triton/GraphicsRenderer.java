package edu.ucdenver.triton;


import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GraphicsRenderer implements GLSurfaceView.Renderer{

    private final int N = 8; //Number of satellites
    private final float[][] mvpMatrix  = new float[N][16]; //Vector projection matrix
    private final float[][] projMatrix = new float[N][16]; //View projection matrix
    private final float[][] vectMatrix = new float[N][16]; //Vector matrix
    private final float[][] rotMatrix  = new float[N][16]; //Rotation matrix

    private Billboard[] planetArray = new Billboard[N]; //Planet billboard array
    private float[] viewMatrix = new float[16]; //Camera matrix
    private float camX, camY, camZ; //Camera position
    private float lookX, lookY, lookZ; //Camera vector
    private float upX, upY, upZ; //Camera orthogonal vector

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //Set camera matrix
        camX = 0;
        camY = 0;
        camZ = 3;
        lookX = 0;
        lookY = 0;
        lookZ = -3;
        upX = 0;
        upY = 1;
        upZ = 0;
        Matrix.setLookAtM(viewMatrix, 0, camX, camY, camZ, lookX, lookY, lookZ, upX, upY, upZ);

        //Set background color
        GLES20.glClearColor(0.4f, 0.0f, 0.8f, 1.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Matrix.setLookAtM(viewMatrix, 0, camX, camY, camZ, lookX, lookY, lookZ, upX, upY, upZ);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //Draw Background
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
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
}
