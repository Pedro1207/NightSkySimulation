package PedroFernandes.NSS;

import android.widget.SeekBar;
import android.widget.TextView;

import java.io.InputStream;

public class MySeekBar {

    final SeekBar seekBar;
    final TextView textView;
    private int[] isDirty;
    InputData[] inputData;
    int index;

    public MySeekBar(SeekBar _seekBar, TextView _textView, InputData[] _inputData, int _index, int[] _isDirty) {
        this.seekBar = _seekBar;
        this.textView = _textView;
        this.inputData = _inputData;
        this.index = _index;
        this.isDirty = _isDirty;

        String t;
        if(inputData[index].integer){
            t = inputData[index].name + " : " + (int) (seekBar.getProgress() / inputData[index].division);
        } else{
            t = inputData[index].name + " : " + (seekBar.getProgress() / inputData[index].division);
        }

        textView.setText(t);

        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String t;
                int curValue = seekBar.getProgress();
                if(inputData[index].integer){
                    t = inputData[index].name + " : " + (int) (curValue / inputData[index].division + inputData[index].offset);
                } else{
                    t = inputData[index].name + " : " + (curValue / inputData[index].division + inputData[index].offset);
                }
                textView.setText(t);
                inputData[index].value = curValue + inputData[index].offset;
                isDirty[0] = 1;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


}
