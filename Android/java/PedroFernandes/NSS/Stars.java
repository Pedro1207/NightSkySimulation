package PedroFernandes.NSS;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES31;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Stars {

    private final int mProgramStars;
    private Context context;

    //Sky buffers
    private FloatBuffer starsVertexBuffer;
    private FloatBuffer starsTexBuffer;
    int[] instanceVBO;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float[] squareCoords = {
            -1.0f,  1.0f, 0.0f,   // top left
            -1.0f, -1.0f, 0.0f,   // bottom left
            1.0f, -1.0f, 0.0f,    // bottom right
            1.0f,  1.0f, 0.0f,    // top right
            -1.0f,  1.0f, 0.0f,
            1.0f, -1.0f, 0.0f, };

    static float[] squareTexCoords = {
            0.0f, 1.0f,   // top left
            0.0f, 0.0f,   // bottom left
            1.0f, 0.0f,   // bottom right
            1.0f, 1.0f,   // top right
            0.0f, 1.0f,
            1.0f, 0.0f    };

    private final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private float fov;
    private float ratio;
    private int textureHandle;

    public Stars(Context context, int size, FloatBuffer instances) {

        this.context = context;

        //------------------------------Compile Stars Shader--------------------------------------

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        starsVertexBuffer = bb.asFloatBuffer();
        starsVertexBuffer.put(squareCoords);
        starsVertexBuffer.position(0);

        ByteBuffer bbt = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                squareTexCoords.length * 4);
        bbt.order(ByteOrder.nativeOrder());
        starsTexBuffer = bbt.asFloatBuffer();
        starsTexBuffer.put(squareTexCoords);
        starsTexBuffer.position(0);

        int vertexShader = ShaderHelpers.loadShader(GLES31.GL_VERTEX_SHADER,
                "stars.vert", context);
        int fragmentShader = ShaderHelpers.loadShader(GLES31.GL_FRAGMENT_SHADER,
                "stars.frag", context);

        // create empty OpenGL ES Program
        mProgramStars = GLES31.glCreateProgram();

        // add the vertex shader to program
        GLES31.glAttachShader(mProgramStars, vertexShader);

        // add the fragment shader to program
        GLES31.glAttachShader(mProgramStars, fragmentShader);

        // creates OpenGL ES program executables
        GLES31.glLinkProgram(mProgramStars);

        textureHandle = ShaderHelpers.loadTexture(context, R.drawable.star);

        instanceVBO = new int[1];
        GLES31.glGenBuffers(1, instanceVBO, 0);
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, instanceVBO[0]);
        GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER, 4 * 4 * size, instances, GLES31.GL_STATIC_DRAW);
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, 0);

    }

    public void setFOV(float fov, float ratio){
        this.fov = fov;
        this.ratio = ratio;
    }


    public void draw(float[] mvpmatrix, float[] viewMatrix, float[] projMatrix, FloatBuffer instances, int size, float maxStarBright, float minStarBright){

        // Add program to OpenGL ES environment
        GLES31.glUseProgram(mProgramStars);


        // get handle to shape's transformation matrix
        int vPMatrixHandle = GLES31.glGetUniformLocation(mProgramStars, "m_pvm");
        GLES31.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpmatrix, 0);

        int viewMatrixHandle = GLES31.glGetUniformLocation(mProgramStars, "m_v");
        GLES31.glUniformMatrix4fv(viewMatrixHandle, 1, false, viewMatrix, 0);

        int projMatrixHandle = GLES31.glGetUniformLocation(mProgramStars, "m_p");
        GLES31.glUniformMatrix4fv(projMatrixHandle, 1, false, projMatrix, 0);


        ShaderHelpers.setFloatUniform("maxStarBright", maxStarBright, mProgramStars);

        ShaderHelpers.setFloatUniform("minStarBright", minStarBright, mProgramStars);


        // Enable a handle to the triangle vertices
        GLES31.glEnableVertexAttribArray(0);

        // Prepare the square coordinate data
        GLES31.glVertexAttribPointer(0, COORDS_PER_VERTEX,
                GLES31.GL_FLOAT, false,
                vertexStride, starsVertexBuffer);


        // Enable a handle to the triangle vertices
        GLES31.glEnableVertexAttribArray(2);

        // Prepare the square coordinate data
        GLES31.glVertexAttribPointer(2, 2,
                GLES31.GL_FLOAT, false,
                0, starsTexBuffer);



        GLES31.glEnableVertexAttribArray(1);
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, instanceVBO[0]);
        GLES31.glVertexAttribPointer(1, 4, GLES31.GL_FLOAT, false, 0, 0);
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, 0);
        GLES31.glVertexAttribDivisor(1, 1);


        GLES31.glActiveTexture(GLES31.GL_TEXTURE0);
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, textureHandle);

        GLES31.glUniform1i(GLES31.glGetUniformLocation(mProgramStars, "star_texture"), 0);

        // Draw the square
        GLES31.glDrawArraysInstanced(GLES31.GL_TRIANGLES, 0, vertexCount, size);


    }


    public void updateBuffer(FloatBuffer fb, int size) {
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, instanceVBO[0]);
        GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER, 4 * 4 * size, fb, GLES31.GL_STATIC_DRAW);
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, 0);
    }
}

