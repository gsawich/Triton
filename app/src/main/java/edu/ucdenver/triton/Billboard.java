package edu.ucdenver.triton;


import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Billboard {
    private final int COORDS_PER_VERTEX = 3;
    private final int VERT_SIZE = COORDS_PER_VERTEX * 4;
    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            // the matrix must be included as a modifier of gl_Position
            "uniform mat4 uMVPMatrix;" + "attribute vec4 vPosition;" + "void main() {" +
                    "  gl_Position = vPosition * uMVPMatrix;" + "}";

    private final String fragmentShaderCode = "precision mediump float;"
            + "uniform vec4 vColor;" + "void main() {"
            + "  gl_FragColor = vColor;" + "}";

    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private float[] position = new float[3];
    private float[] color = new float[4];
    private float[] coordinates = {
            -0.5f,  0.5f,  0.0f,
            -0.5f, -0.5f,  0.0f,
             0.5f, -0.5f,  0.0f,
             0.5f,  0.5f,  0.0f};
    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 };
    private int positionHandler, colorHandler, matrixHandler;
    private int vertexCount;
    private int glProgram;
    private boolean canDraw;

    public Billboard(float x, float y, float z){
        position[0] = x;
        position[1] = y;
        position[2] = z;

        color[0] = 100;
        color[1] = 0;
        color[2] = 200;
        color[3] = 1;

        for (int i = 0; i < coordinates.length; i ++){
            switch (i%COORDS_PER_VERTEX){
                case (0):
                    coordinates[i]+=position[0];
                    break;
                case (1):
                    coordinates[i]+=position[1];
                    break;
                case (2):
                    coordinates[i]+=position[2];
                    break;
                default:
                    break;
            }
        }

        vertexCount = coordinates.length / COORDS_PER_VERTEX;
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                coordinates.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(coordinates);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);


        // prepare shaders and OpenGL program
        int vertexShader = GraphicsRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = GraphicsRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        glProgram = GLES20.glCreateProgram(); // create empty OpenGL Program
        GLES20.glAttachShader(glProgram, vertexShader); // add the vertex shader
        // to program
        GLES20.glAttachShader(glProgram, fragmentShader); // add the fragment
        // shader to program
        GLES20.glLinkProgram(glProgram); // create OpenGL program executables


        canDraw = true;
    }

    public void draw(float[] mvpMatrix) {

        if (canDraw) {
            // Add program to OpenGL environment
            GLES20.glUseProgram(glProgram);

            // get handle to vertex shader's vPosition member
            positionHandler = GLES20.glGetAttribLocation(glProgram, "vPosition");

            // Enable a handle to the triangle vertices
            GLES20.glEnableVertexAttribArray(positionHandler);

            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(positionHandler, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false, VERT_SIZE, vertexBuffer);

            // get handle to fragment shader's vColor member
            colorHandler = GLES20.glGetUniformLocation(glProgram, "vColor");

            // Set color for drawing the triangle
            GLES20.glUniform4fv(colorHandler, 1, color, 0);

            // get handle to shape's transformation matrix
            matrixHandler = GLES20.glGetUniformLocation(glProgram,
                    "uMVPMatrix");

            // Apply the projection and view transformation
            GLES20.glUniformMatrix4fv(matrixHandler, 1, false, mvpMatrix, 0);

            // Draw the triangle
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        }

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandler);
    }

    public float getX() {
        return position[0];
    }

    public float getY() {
        return position[1];
    }

    public float getZ() {
        return position[2];
    }
}
