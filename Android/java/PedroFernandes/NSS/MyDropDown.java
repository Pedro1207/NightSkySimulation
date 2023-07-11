package PedroFernandes.NSS;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MyDropDown {

    final Spinner spinner;
    final TextView textView;
    private int[] isDirty;
    InputData[] inputData;
    int index;

    public MyDropDown(Spinner _spinner, TextView _textView, InputData[] _inputData, int _index, int[] _isDirty) {
        this.spinner = _spinner;
        this.textView = _textView;
        this.inputData = _inputData;
        this.index = _index;
        this.isDirty = _isDirty;

        textView.setText(inputData[index].name);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                inputData[index].strValue = adapterView.getSelectedItem().toString();
                isDirty[0] = 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }


}
