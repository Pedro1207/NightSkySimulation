package PedroFernandes.NSS;

import android.content.Context;
import android.opengl.GLES31;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class MoonS {

    private int mProgramMoonS;
    private Context context;

    //Sky buffers
    private FloatBuffer skyVertexBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float[] squareCoords = {
            -1.0f,  1.0f, 0.0f,   // top left
            -1.0f, -1.0f, 0.0f,   // bottom left
            1.0f, -1.0f, 0.0f,    // bottom right
            1.0f,  1.0f, 0.0f,    // top right
            -1.0f,  1.0f, 0.0f,
            1.0f, -1.0f, 0.0f, };

    private final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private float fov;
    private float ratio;

    public MoonS(Context context) {

        this.context = context;

        //------------------------------Compile Sky Shader--------------------------------------

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        skyVertexBuffer = bb.asFloatBuffer();
        skyVertexBuffer.put(squareCoords);
        skyVertexBuffer.position(0);

        int vertexShader = ShaderHelpers.loadShader(GLES31.GL_VERTEX_SHADER,
                "square.vert", context);
        int fragmentShader = ShaderHelpers.loadShader(GLES31.GL_FRAGMENT_SHADER,
                "moonS.frag", context);

        // create empty OpenGL ES Program
        mProgramMoonS = GLES31.glCreateProgram();


        // add the vertex shader to program
        GLES31.glAttachShader(mProgramMoonS, vertexShader);

        // add the fragment shader to program
        GLES31.glAttachShader(mProgramMoonS, fragmentShader);

        // creates OpenGL ES program executables
        GLES31.glLinkProgram(mProgramMoonS);

    }

    public void setFOV(float fov, float ratio){
        this.fov = fov;
        this.ratio = ratio;
    }


    public void draw(float[] camView, float[] up, int Texture){
        //Log.d("Pitch, yaw, roll:", pitch + " - " + yaw + " - " + roll);

        // Add program to OpenGL ES environment
        GLES31.glUseProgram(mProgramMoonS);

        // get handle to shape's transformation matrix
        int vPMatrixHandle = GLES31.glGetUniformLocation(mProgramMoonS, "uMVPMatrix");

        float[] identity = new float[16];
        Matrix.setIdentityM(identity, 0);
        GLES31.glUniformMatrix4fv(vPMatrixHandle, 1, false, identity, 0);

        // get handle to vertex shader's vPosition member
        int positionHandle = GLES31.glGetAttribLocation(mProgramMoonS, "vPosition");

        // Enable a handle to the triangle vertices
        GLES31.glEnableVertexAttribArray(positionHandle);

        // Prepare the square coordinate data
        GLES31.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES31.GL_FLOAT, false,
                vertexStride, skyVertexBuffer);

        ShaderHelpers.setVec3Uniform("camUp", up, mProgramMoonS);
        ShaderHelpers.setVec3Uniform("camView", camView, mProgramMoonS);
        ShaderHelpers.setFloatUniform("fov", fov, mProgramMoonS);
        ShaderHelpers.setFloatUniform("ratio", ratio, mProgramMoonS);
        ShaderHelpers.setIntUniform("divisions", 8, mProgramMoonS);
        ShaderHelpers.setIntUniform("divisionsLightRay", 8, mProgramMoonS);
        ShaderHelpers.setFloatUniform("exposure", 1.4f, mProgramMoonS);
        ShaderHelpers.setIntUniform("cameraMode", 0, mProgramMoonS);
        ShaderHelpers.setIntUniform("sampling", 0, mProgramMoonS);

        float pressure = 101325;
        float temperature = 0;
        float indexOfRefraction = 1.0003f;
        float[] wavelenghts = {700, 530, 470};
        float[] betaR = new float[3];

        float n = pressure / (1.38e-23f * (temperature + 273));
        float k = (float) (2.0 * Math.pow(Math.PI, 2) * Math.pow(Math.pow(indexOfRefraction, 2) - 1, 2) / (3 * n));
        for(int i = 0; i < 3; i++){
            betaR[i] = (float) (k / Math.pow(wavelenghts[i] * 10e-10, 4));
        }


        ShaderHelpers.setVec3Uniform("betaR", betaR, mProgramMoonS);
        ShaderHelpers.setFloatUniform("betaMf", 5.76e-7f, mProgramMoonS);
        ShaderHelpers.setFloatUniform("Hr", 7994.0f, mProgramMoonS);
        ShaderHelpers.setFloatUniform("Hm", 1200.0f, mProgramMoonS);
        ShaderHelpers.setFloatUniform("g", 0.999f, mProgramMoonS);
        ShaderHelpers.setVec2Uniform("sunAngles", new float[]{175.0f, 32.0f}, mProgramMoonS);


        GLES31.glActiveTexture(GLES31.GL_TEXTURE2);
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, Texture);
        int samplerHandler = GLES31.glGetUniformLocation(mProgramMoonS, "temp_rt_texture");
        GLES31.glUniform1i(samplerHandler, 2);

        // Draw the square
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array
        GLES31.glDisableVertexAttribArray(positionHandle);

    }



}
