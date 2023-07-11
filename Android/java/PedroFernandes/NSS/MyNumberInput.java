package PedroFernandes.NSS;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class MyNumberInput {

    final EditText editText;
    final TextView textView;
    private int[] isDirty;
    InputData[] inputData;
    int index;

    public MyNumberInput(EditText _editText, TextView _textView, InputData[] _inputData, int _index, int[] _isDirty) {
        this.editText = _editText;
        this.textView = _textView;
        this.inputData = _inputData;
        this.index = _index;
        this.isDirty = _isDirty;


        textView.setText(inputData[index].name);

        this.editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().length() == 0){
                    inputData[index].value = 2023;
                }else {
                    inputData[index].value = Integer.parseInt(charSequence.toString());
                }

                isDirty[0] = 1;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


}
