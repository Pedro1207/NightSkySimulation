package PedroFernandes.NSS;

public class LowPassFilter {

    private float[][] values;
    private int current;
    private final int batchSize;
    private final int arraySize;



    LowPassFilter(int arraysize, int batchsize){
        this.values = new float[arraysize][batchsize];
        this.arraySize = arraysize;
        this.batchSize = batchsize;
        this.current = 0;
    }

    public void addSample(float[] sample, float[] dest){
        for(int i = 0; i < arraySize; i++){
            values[i][current] = sample[i];
        }
        current++;

        if (current == batchSize) current = 0;

        float[] returnValues = new float[3];
        for(int i = 0; i < batchSize; i++){
            for(int j = 0; j < arraySize; j++){
                returnValues[j] += this.values[j][i];
            }
        }

        for(int i = 0; i < arraySize; i++){
            dest[i] = returnValues[i] / batchSize;
        }

    }

}
