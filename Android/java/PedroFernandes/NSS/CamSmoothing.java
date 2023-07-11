package PedroFernandes.NSS;

import java.util.Arrays;

public class CamSmoothing {

    private float x[];
    private float y[];
    private float z[];
    private int size;
    private int current;

    public CamSmoothing(int size){
        this.size = size;
        x = new float[size];
        y = new float[size];
        z = new float[size];
        current = 0;
    }

    private float average(float[] array){
        float sum = 0;
        for(float f : array){
            sum += f;
        }
        return sum / array.length;
    }

    public void newSample(float[] sample, float[] returnArray){
        x[current] = sample[0];
        y[current] = sample[1];
        z[current] = sample[2];

        returnArray[0] = average(x);
        returnArray[1] = average(y);
        returnArray[2] = average(z);

        current = (current + 1) % size;

    }
}
