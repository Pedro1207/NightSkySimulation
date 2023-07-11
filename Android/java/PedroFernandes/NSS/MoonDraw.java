package PedroFernandes.NSS;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import android.content.Context;
import android.opengl.GLES31;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;

public class MoonDraw {

    private final int mProgramMoon;
    private final Context context;
    private SampaClass sampa;

    //Moon Buffers
    float[] moonVertex;
    float[] moonNormals;
    float[] moonTexture;
    char[] moonIndexes;
    private final FloatBuffer moonVertexBuffer;
    private final CharBuffer moonIndexesBuffer;
    private final FloatBuffer moonNormalBuffer;
    private final FloatBuffer moonTextureBuffer;
    int textureHandle;

    public MoonDraw(Context context, SampaClass sampa){
        this.context = context;

        this.generateSphereData(20,20, 1);

        moonVertexBuffer = ByteBuffer.allocateDirect(moonVertex.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        moonVertexBuffer.put(moonVertex).position(0);

        moonIndexesBuffer = ByteBuffer.allocateDirect(moonIndexes.length * 4).order(ByteOrder.nativeOrder()).asCharBuffer();
        moonIndexesBuffer.put(moonIndexes).position(0);

        moonTextureBuffer = ByteBuffer.allocateDirect(moonTexture.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        moonTextureBuffer.put(moonTexture).position(0);

        moonNormalBuffer = ByteBuffer.allocateDirect(moonNormals.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        moonNormalBuffer.put(moonNormals).position(0);

        int vertexShader = ShaderHelpers.loadShader(GLES31.GL_VERTEX_SHADER, "moon_draw.vert", context);
        int fragmentShader = ShaderHelpers.loadShader(GLES31.GL_FRAGMENT_SHADER, "moon_draw.frag", context);

        mProgramMoon = GLES31.glCreateProgram();
        GLES31.glAttachShader(mProgramMoon, vertexShader);
        GLES31.glAttachShader(mProgramMoon, fragmentShader);
        GLES31.glLinkProgram(mProgramMoon);

        GLES31.glUseProgram(mProgramMoon);

        textureHandle = ShaderHelpers.loadTexture(context, R.drawable.moontexture);

        this.sampa = sampa;

    }

    private float radians(float degrees){
        return (float) (degrees * sampa.PI / 180);
    }


    public void draw(float[] mvpMatrix, float[] viewMatrix, float[] normalMatrix, float[] camView){
        GLES31.glUseProgram(mProgramMoon);

        int PVMHandle = GLES31.glGetUniformLocation(mProgramMoon, "m_pvm");
        GLES31.glUniformMatrix4fv(PVMHandle, 1, false, mvpMatrix, 0);
        int ViewHandle = GLES31.glGetUniformLocation(mProgramMoon, "m_view");
        GLES31.glUniformMatrix4fv(ViewHandle, 1, false, viewMatrix, 0);

        int normalHandle = GLES31.glGetUniformLocation(mProgramMoon, "m_normal");
        GLES31.glUniformMatrix3fv(normalHandle, 1, false, normalMatrix, 0);


        int positionHandle = GLES31.glGetAttribLocation(mProgramMoon, "position");
        GLES31.glEnableVertexAttribArray(positionHandle);
        // Prepare the square coordinate data
        GLES31.glVertexAttribPointer(positionHandle, 3,
                GLES31.GL_FLOAT, false,
                0, moonVertexBuffer);
        int normalBufferHandle = GLES31.glGetAttribLocation(mProgramMoon, "normals");
        GLES31.glEnableVertexAttribArray(normalBufferHandle);
        GLES31.glVertexAttribPointer(normalBufferHandle, 3,
                GLES31.GL_FLOAT, false,
                0, moonNormalBuffer);

        int textureCoordHandle = GLES31.glGetAttribLocation(mProgramMoon, "texCoord0");
        GLES31.glEnableVertexAttribArray(textureCoordHandle);
        GLES31.glVertexAttribPointer(textureCoordHandle, 2,
                GLES31.GL_FLOAT, false,
                0, moonTextureBuffer);

        // Set the active texture unit to texture unit 0.
        GLES31.glActiveTexture(GLES31.GL_TEXTURE1);
        // Bind the texture to this unit.
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, textureHandle);
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        int samplerHandler = GLES31.glGetUniformLocation(mProgramMoon, "moonTex");
        GLES31.glUniform1i(samplerHandler, 1);

        sampa.sampa_calculate();

        float moonDistance = (float) sampa.mpa.cap_delta / 10000;
        float sunDistance = moonDistance * 389;

        float moonAzimuth = radians((float)-sampa.mpa.azimuth_astro) + radians(180);
        float moonElevation = radians((float)sampa.mpa.e) - radians(90);

        float moonX = (float) (moonDistance * sin(moonAzimuth) * sin(moonElevation));
        float moonY = (float) (moonDistance * cos(moonElevation));
        float moonZ = (float) (moonDistance * cos(moonAzimuth) * sin(moonElevation));

        //moonX = -4;
        //moonY = 0;
        //moonZ = 0;

        float sunAzimuth = radians((float)-sampa.spa.azimuth_astro) + radians(180);
        float sunElevation = radians((float)sampa.spa.e) - radians(90);

        float sunX = (float) (sunDistance * sin(sunAzimuth) * sin(sunElevation));
        float sunY = (float) (sunDistance * cos(sunElevation));
        float sunZ = (float) (sunDistance * cos(sunAzimuth) * sin(sunElevation));

        float l_dir_x = sunX - moonX;
        float l_dir_y = sunY - moonY;
        float l_dir_z = sunZ - moonZ;

        //l_dir_x = -1;
        //l_dir_y = 0;
        //l_dir_z = 0;

        ShaderHelpers.setVec3Uniform("moonPosition", new float[]{moonX, moonY, moonZ}, mProgramMoon);
        ShaderHelpers.setVec3Uniform("l_dir", new float[]{l_dir_x, l_dir_y, l_dir_z}, mProgramMoon);
        ShaderHelpers.setVec3Uniform("moonData", new float[]{moonAzimuth, (float)sampa.mpa.cap_delta, 0}, mProgramMoon);
        ShaderHelpers.setVec3Uniform("camera_view", camView, mProgramMoon);

        GLES31.glDrawElements(GLES31.GL_TRIANGLES, moonIndexes.length, GLES31.GL_UNSIGNED_SHORT, moonIndexesBuffer);

        GLES31.glDisableVertexAttribArray(positionHandle);
        GLES31.glDisableVertexAttribArray(textureCoordHandle);
        GLES31.glDisableVertexAttribArray(normalBufferHandle);
    }


    public void generateSphereData(int totalRings, int totalSectors, float radius)
    {
        moonVertex = new float[totalRings * totalSectors * 3];
        moonNormals = new float[totalRings * totalSectors * 3];
        moonTexture = new float[totalRings * totalSectors * 2];
        moonIndexes = new char[totalRings * totalSectors * 6];

        float R = 1f / (float)(totalRings-1);
        float S = 1f / (float)(totalSectors-1);
        int r, s;

        float x, y, z;
        int vertexIndex = 0, textureIndex = 0, indexIndex = 0, normalIndex = 0;

        for(r = 0; r < totalRings; r++)
        {
            for(s = 0; s < totalSectors; s++)
            {
                y = (float) sin((-Math.PI / 2f) + Math.PI * r * R );
                x = (float) cos(2f * Math.PI * s * S) * (float) sin(Math.PI * r * R );
                z = (float) sin(2f * Math.PI * s * S) * (float) sin(Math.PI * r * R );

                if (moonTexture != null)
                {
                    moonTexture[textureIndex] = s * S;
                    moonTexture[textureIndex + 1] = r * R;

                    textureIndex += 2;
                }

                moonVertex[vertexIndex] = x * radius;
                moonVertex[vertexIndex + 1] = y * radius;
                moonVertex[vertexIndex + 2] = z * radius;

                vertexIndex += 3;

                moonNormals[normalIndex] = x;
                moonNormals[normalIndex + 1] = y;
                moonNormals[normalIndex + 2] = z;

                normalIndex += 3;
            }
        }


        int r1, s1;
        for(r = 0; r < totalRings ; r++)
        {
            for(s = 0; s < totalSectors ; s++)
            {
                r1 = (r + 1 == totalRings) ? 0 : r + 1;
                s1 = (s + 1 == totalSectors) ? 0 : s + 1;

                moonIndexes[indexIndex] = (char)(r * totalSectors + s);
                moonIndexes[indexIndex + 2] = (char)(r * totalSectors + (s1));
                moonIndexes[indexIndex + 1] = (char)((r1) * totalSectors + (s1));

                moonIndexes[indexIndex + 3] = (char)((r1) * totalSectors + s);
                moonIndexes[indexIndex + 4] = (char)((r1) * totalSectors + (s1));
                moonIndexes[indexIndex + 5] = (char)(r * totalSectors + s);
                indexIndex += 6;
            }
        }
    }

}
