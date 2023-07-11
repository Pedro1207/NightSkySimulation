package PedroFernandes.NSS;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES31;
import android.opengl.GLUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShaderHelpers {

    public static int loadShader(int type, String path, Context context){

        StringBuilder shaderCode = new StringBuilder();

        try{

            BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(path)));

            String currentLine;

            while((currentLine = br.readLine()) != null){
                shaderCode.append(currentLine).append("\n");
            }

            // create a vertex shader type (GLES31.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES31.GL_FRAGMENT_SHADER)
            int shader = GLES31.glCreateShader(type);

            // add the source code to the shader and compile it
            GLES31.glShaderSource(shader, shaderCode.toString());
            GLES31.glCompileShader(shader);

            int result;
            System.out.println("-------------------Compilation Information-------------------");
            System.out.println(GLES31.glGetShaderInfoLog(shader));
            System.out.println("-----------------------------End-----------------------------");
            return shader;

        } catch (IOException e){
            e.printStackTrace();
        }

        return -1;

    }

    public static void setFloatUniform(String name, float value, int program){
        int handle = GLES31.glGetUniformLocation(program, name);
        GLES31.glUniform1f(handle, value);
    }

    public static void setIntUniform(String name, int value, int program){
        int handle = GLES31.glGetUniformLocation(program, name);
        GLES31.glUniform1i(handle, value);
    }

    public static void setVec2Uniform(String name, float[] value, int program){
        int handle = GLES31.glGetUniformLocation(program, name);
        GLES31.glUniform2f(handle, value[0], value[1]);
    }

    public static void setVec3Uniform(String name, float[] value, int program){
        int handle = GLES31.glGetUniformLocation(program, name);
        GLES31.glUniform3f(handle, value[0], value[1], value[2]);
    }

    public static void setVec4Uniform(String name, float[] value, int program){
        int handle = GLES31.glGetUniformLocation(program, name);
        GLES31.glUniform4f(handle, value[0], value[1], value[2], value[3]);
    }


    public static void crossProduct(float vect_A[], float vect_B[], float cross_P[]) {

        cross_P[0] = vect_A[1] * vect_B[2]
                - vect_A[2] * vect_B[1];
        cross_P[1] = vect_A[2] * vect_B[0]
                - vect_A[0] * vect_B[2];
        cross_P[2] = vect_A[0] * vect_B[1]
                - vect_A[1] * vect_B[0];
    }


    public static int loadTexture(final Context context, final int resourceId)
    {
        final int[] textureHandle = new int[1];

        GLES31.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_WRAP_S, GLES31.GL_REPEAT);
            GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_WRAP_T, GLES31.GL_REPEAT);
            GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_LINEAR_MIPMAP_LINEAR);
            GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MAG_FILTER, GLES31.GL_LINEAR);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES31.GL_TEXTURE_2D, 0, bitmap, 0);
            GLES31.glGenerateMipmap(GLES31.GL_TEXTURE_2D);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    public static void calcView(float[] camView, float pitch, float yaw, float[] up){

        camView[0] = (float) (-sin(pitch) * cos(yaw));
        camView[1] = (float) (-sin(yaw));
        camView[2] = (float) (-cos(pitch) * cos(yaw));

        float[] cr = new float[3];

        cr[0] = (float) -cos(pitch);
        cr[1] = (float) 0.0;
        cr[2] = (float) sin(pitch);

        ShaderHelpers.crossProduct(camView, cr, up);
    }

    public static void calcViewMotion(float[] camView, float x, float y, float z, float[] up){
        camView[0] = (float) (sin(x) * cos(z));
        camView[1] = (float) (sin(z));
        camView[2] = (float) (cos(x) * cos(z));

        float[] cr = new float[3];

        cr[0] = (float) cos(x);
        cr[1] = (float) 0.0;
        cr[2] = (float) sin(x);

        ShaderHelpers.crossProduct(camView, cr, up);
        up[0] = 0.0f;
        up[1] = 1.0f;
        up[2] = 0.0f;

    }

    public static void glGenTextureFromFramebuffer(int[] t, int[] f, int w, int h)
    {
        GLES31.glGenTextures(1, t, 0);
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, t[0]);

        GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_LINEAR);
        GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MAG_FILTER, GLES31.GL_LINEAR);
        GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_WRAP_S, GLES31.GL_CLAMP_TO_EDGE);
        GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_WRAP_T, GLES31.GL_CLAMP_TO_EDGE);

        GLES31.glTexImage2D(GLES31.GL_TEXTURE_2D, 0, GLES31.GL_RGBA, w, h, 0, GLES31.GL_RGBA, GLES31.GL_UNSIGNED_BYTE, null);

        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, 0);


// Build the framebuffer.
        GLES31.glGenFramebuffers(1, f, 0);
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, f[0]);
        GLES31.glFramebufferTexture2D(GLES31.GL_FRAMEBUFFER, GLES31.GL_COLOR_ATTACHMENT0, GLES31.GL_TEXTURE_2D, t[0], 0);

        int status = GLES31.glCheckFramebufferStatus(GLES31.GL_FRAMEBUFFER);
        if (status != GLES31.GL_FRAMEBUFFER_COMPLETE){
            System.out.println("Error building framebuffer");
        }

        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, 0);
    }

    public static void convert_AZ_EL_to_XYZ(double azimuth, double elevation, double[] values, double distance) {
	    values[0] = distance * sin(azimuth) * sin(elevation);
	    values[1] = distance * cos(elevation);
	    values[2] = distance * cos(azimuth) * sin(elevation);
    }

}
