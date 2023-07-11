package PedroFernandes.NSS;

import static android.hardware.SensorManager.AXIS_X;
import static android.hardware.SensorManager.AXIS_Y;
import static android.hardware.SensorManager.AXIS_Z;
import static android.hardware.SensorManager.remapCoordinateSystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    private OpenGLView openGLView;
    private SensorManager sensorManager;
    private SampaClass sampaClass;

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    private LowPassFilter lowPassFilter;

    int count;

    InputData[] inputDataList;
    int[] isDirty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        openGLView = (OpenGLView) findViewById(R.id.openGLView3);
        openGLView.setOrientationArray(orientationAngles);

        this.isDirty = new int[1];


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lowPassFilter = new LowPassFilter(3, 20);


        sampaClass = new SampaClass();
        sampaClass.setValues(2003, 10, 17, 12, 30, 30, -7.0, 0, 67, -105.1786, 39.742476, 1830.14, 820, 11, 30, -10, 0.5667, SpaOptions.SPA_ALL);
        sampaClass.sampa_calculate();

        inputDataList = new InputData[Enum.INTERFACE.values().length];
        inputDataList[Enum.INTERFACE.DECIMALTIME.ordinal()] = new InputData(((SeekBar) findViewById(R.id.Time)).getProgress(), "", false, "Time", 100.0f, 0.0f);
        inputDataList[Enum.INTERFACE.DAY.ordinal()] = new InputData(((SeekBar) findViewById(R.id.Day)).getProgress(), "", true, "Day", 1.0f, 1.0f);
        inputDataList[Enum.INTERFACE.MONTH.ordinal()] = new InputData(((SeekBar) findViewById(R.id.Month)).getProgress(), "", true, "Month", 1.0f, 1.0f);
        inputDataList[Enum.INTERFACE.YEAR.ordinal()] = new InputData(Integer.parseInt(((EditText) findViewById(R.id.Year)).getText().toString()), "", true, "Year", 1.0f, 0.0f);
        inputDataList[Enum.INTERFACE.CONSTELATION.ordinal()] = new InputData(0, "None", false, "Const", 0.0f, 0.0f);

        MySeekBar seekBarTime = new MySeekBar(findViewById(R.id.Time), findViewById(R.id.textTime), inputDataList, Enum.INTERFACE.DECIMALTIME.ordinal(), isDirty);
        MySeekBar seekBarDay = new MySeekBar(findViewById(R.id.Day), findViewById(R.id.textDay), inputDataList, Enum.INTERFACE.DAY.ordinal(), isDirty);
        MySeekBar seekBarMonth = new MySeekBar(findViewById(R.id.Month), findViewById(R.id.textMonth), inputDataList, Enum.INTERFACE.MONTH.ordinal(), isDirty);
        MyNumberInput numberInputYear = new MyNumberInput(findViewById(R.id.Year), findViewById(R.id.textYear), inputDataList, Enum.INTERFACE.YEAR.ordinal(), isDirty);

        Spinner spinner = (Spinner) findViewById(R.id.ConstSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.constelations, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        MyDropDown constelationInput = new MyDropDown(spinner, findViewById(R.id.ConstelationText), inputDataList, Enum.INTERFACE.CONSTELATION.ordinal(), isDirty);


        openGLView.setInputsList(inputDataList, isDirty);

        count = 0;
    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }
        updateOrientationAngles();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //TODO
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);

        // "rotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        count++;
        if(count == 60){
            System.out.println(Arrays.toString(orientationAngles));
            count = 0;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        openGLView.onResume();

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_GAME);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        openGLView.onPause();
        sensorManager.unregisterListener(this);
    }


}