package PedroFernandes.NSS;

import android.content.Context;
import android.opengl.GLES31;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;


public class OpenGLRenderer implements GLSurfaceView.Renderer {

    private Sky mSky;
    private MoonDraw moonDraw;
    private Stars stars;
    private MoonS moonS;
    private Context context;
    FPSCounter fpsCounter;
    private int width, height;
    private SampaClass sampa;

    private final float[] vPMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private float[] normalMatrix = new float[9];

    private float[] orientationAngles;
    private float[] sunAngles;

    private float yaw, pitch, roll;
    private float[] camView;
    private float[] oldCamView;
    private float[] up;

    private int[] frameBuffer1;
    private int[] frameBuffer2;
    private int[] texture1;
    private int[] texture2;
    private float[] starBuffer;

    int starNumber;
    FloatBuffer fb;
    private InputData[] inputDataList;
    private int[] isDirty;

    float julian_day;
    int count;

    CamSmoothing camSmoothing;

    ArrayList<StarData> starDataList = new ArrayList<>();

    public void updateViewTouch(float dx, float dy) {
        pitch += dy;
        yaw += dx;
    }



    public void setContext(Context context){
        this.context = context;
    }

    private void calculateStars(String constelation){
        String[] constNames = {"None", "And", "Ant", "Aps", "Aql", "Aqr", "Ara", "Ari", "Aur", "Boo", "Cae", "Cam", "Cap", "Car", "Cas", "Cen", "Cep", "Cet", "Cha", "Cir", "CMa", "CMi", "Cnc", "Col", "Com", "CrA", "CrB", "Crt", "Cru", "Crv", "CVn", "Cyg", "Del", "Dor", "Dra", "Equ", "Eri", "For", "gem", "Gru", "Her", "Hor", "Hya", "Hyi", "Lnd", "Lac", "Leo", "Lep", "Lib", "LMi", "Lup", "Lyn", "Lyr", "Men", "Mic", "Mon", "Mus", "Nor", "Oct", "Oph", "Ori", "Pav", "Peg", "Per", "Phe", "Pic", "PsA", "Psc", "PuP", "Pyx", "Ret", "Scl", "Sco", "Sct", "Ser", "Sex", "Sge", "Sgr", "Tau", "Tel", "TrA", "Tri", "Tuc", "UMa", "UMi", "Vel", "Vir", "Vol", "Vul"};


        starBuffer = new float[starDataList.size() * 4];
        starNumber = 0;

        double azimuth, elevation;
        double[] xyz = new double[3];

        for(int i = 0; i < starDataList.size(); i++){
            if(constelation.equals("None") || constelation.equals(starDataList.get(i).con)){
                starNumber++;
                sampa.spa.alpha = starDataList.get(i).ra * (360.0f / 24);
                sampa.spa.delta = starDataList.get(i).dec;
                sampa.convertToAzDec();

                azimuth = (sampa.spa.azimuth_astro / 180 * Math.PI);
                elevation = ((sampa.spa.e + 90) / 180 * Math.PI);

                ShaderHelpers.convert_AZ_EL_to_XYZ(azimuth, elevation, xyz, 200);
                starBuffer[i * 4] = (float) xyz[0];
                starBuffer[i * 4 + 1] = (float) xyz[1];
                starBuffer[i * 4 + 2] = (float) xyz[2];
                starBuffer[i * 4 + 3] = (float) starDataList.get(i).mag;

                if(i % 100 == 0){
                    starBuffer[i * 4 + 3] = -26;
                }
            }
        }

        fb = FloatBuffer.wrap(starBuffer);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // enable face culling feature
        GLES31.glEnable(GLES31.GL_CULL_FACE);
        // specify which faces to not draw
        GLES31.glCullFace(GLES31.GL_BACK);

        GLES31.glDisable(GLES31.GL_DEPTH_TEST);

        GLES31.glClearColor(0, 0, 0, 1f);

        sampa = new SampaClass();
        sampa.setValues(2021, 11, 5, 16, 20, 0, 0, 0, 66.4, -8.4, 41.5, 0, 1000, 11, 0, 0, 0.5667, SpaOptions.SPA_ALL);
        sampa.sampa_calculate();

        sunAngles = new float[2];
        sunAngles[0] = (float) sampa.spa.azimuth;
        sunAngles[1] = (float) sampa.spa.e;

        mSky = new Sky(context);
        moonDraw = new MoonDraw(context, sampa);
        moonS = new MoonS(context);

        fpsCounter = new FPSCounter();

        camView = new float[3];
        oldCamView = new float[3];
        up = new float[3];

        pitch = (float) (Math.PI/2);
        yaw = 0;


        frameBuffer1 = new int[1];
        frameBuffer2 = new int[1];
        texture1 = new int[1];
        texture2 = new int[1];

        count = 0;

        camSmoothing = new CamSmoothing(30);

        InputStream is = context.getResources().openRawResource(R.raw.hyg);
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String line = "";

        try{
            br.readLine();
            while((line = br.readLine()) != null){
                try{
                    String[] tokens = line.split(";");

                    StarData sd = new StarData();
                    sd.id = Integer.parseInt(tokens[0]);
                    sd.proper = tokens[1];
                    sd.ra = Float.parseFloat(tokens[2]);
                    sd.dec = Float.parseFloat(tokens[3]);
                    sd.mag = Float.parseFloat(tokens[4]);
                    sd.absmag = Float.parseFloat(tokens[5]);
                    sd.con = tokens.length == 7 ? tokens[6] : "";
                    starDataList.add(sd);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        } catch (IOException ioe){
            ioe.printStackTrace();
        }

        System.out.println(starDataList.size());

        this.calculateStars("None");

        stars = new Stars(context, starNumber, fb);

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES31.glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;


        ShaderHelpers.glGenTextureFromFramebuffer(texture1, frameBuffer1, width, height);
        ShaderHelpers.glGenTextureFromFramebuffer(texture2, frameBuffer2, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        int top = 1;
        int bottom = -1;
        int near = 1;

        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, bottom, top, near, 1000);
        float fov = (float) ((2 * Math.atan((top - bottom) * 0.5 / near)) * 180 / Math.PI);
        mSky.setFOV(40, ratio);
        moonS.setFOV(40, ratio);
    }

    float fract(float n){
        return n - (int) n;
    }

    float compute_day(int year, int month, int day, float hour) {
        int id = 367 * year - 7 * (year + (month + 9) / 12) / 4 - 3 * ((year + (month - 9) / 7) / 100 + 1) / 4 + 275 * month / 9 + day - 730515;
        float d = id + hour / 24;
        return d;
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        ShaderHelpers.calcView(camView, pitch, yaw, up);


        float[] tempCamView = new float[3];
        ShaderHelpers.calcViewMotion(tempCamView, -orientationAngles[0], orientationAngles[1], (float) (orientationAngles[2] - 90 * Math.PI /180), up);
        camSmoothing.newSample(tempCamView, camView);
        float len = (float) Math.sqrt(camView[0] * camView[0] + camView[1] * camView[1] + camView[2] * camView[2]);
        for(int i = 0; i < 3; i++){
            camView[i] /= len;
        }



        if(isDirty[0] != 0){
            isDirty[0] = 0;

            float decimalTime = inputDataList[Enum.INTERFACE.DECIMALTIME.ordinal()].getValue();

            System.out.println(decimalTime + " - " + inputDataList[Enum.INTERFACE.YEAR.ordinal()].getValue() + " - " + (int) inputDataList[Enum.INTERFACE.MONTH.ordinal()].getValue()  + " - " +  (int) inputDataList[Enum.INTERFACE.DAY.ordinal()].getValue());
            int spa_hour = (int) decimalTime;
            int spa_minute = (int) (fract(decimalTime) * 60);
            float spa_second = fract(fract(decimalTime) * 60) * 60;

            sampa.setValues((int) inputDataList[Enum.INTERFACE.YEAR.ordinal()].getValue(), (int) inputDataList[Enum.INTERFACE.MONTH.ordinal()].getValue(), (int) inputDataList[Enum.INTERFACE.DAY.ordinal()].getValue(), spa_hour, spa_minute, spa_second, 0, 0, 66.4, -8.4, 41.5, 0, 1000, 11, 0, 0, 0.5667, SpaOptions.SPA_ALL);
            sampa.sampa_calculate();
            sunAngles[0] = (float) sampa.spa.azimuth;
            sunAngles[1] = (float) sampa.spa.e;

            this.calculateStars(inputDataList[Enum.INTERFACE.CONSTELATION.ordinal()].getStrValue());
            stars.updateBuffer(fb, starNumber);

        }


        Matrix.setLookAtM(viewMatrix, 0, 0.0f, 0.0f, 0.0f, camView[0], camView[1], camView[2], 0f, 1.0f, 0.0f);


        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        normalMatrix = new float[] {1, 0, 0, 0, 1, 0, 0, 0, 1};

        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, frameBuffer1[0]);

        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT);

        stars.draw(vPMatrix, viewMatrix, projectionMatrix, fb, starNumber, -26.7f, 7.0f);

        moonDraw.draw(vPMatrix, viewMatrix, normalMatrix, camView);


        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, frameBuffer2[0]);
        moonS.draw(camView, up, texture1[0]);

        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, 0);

        mSky.draw(camView, up, texture2[0], sunAngles);


    }

    public void updateView(float dx, float dy) {
        pitch += dy;
        yaw += dx;
    }

    public void setOrientationAngles(float[] orientationAngles){
        this.orientationAngles = orientationAngles;
    }


    public void setInputsList(InputData[] inputDataList, int[] isDirty) {
        this.inputDataList = inputDataList;
        this.isDirty = isDirty;
    }
}
