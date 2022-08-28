package com.jovistar.caltxt.activity;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;

import com.jovistar.caltxt.R;

public class EditTextCaltxt extends androidx.appcompat.widget.AppCompatEditText {

    public EditTextCaltxt(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSingleLine(false);
        checkBounds();
    }

    public EditTextCaltxt(Context context) {
        super(context);
        checkBounds();
        setPadding(10, 10, 10, 10);
    }

    private void checkBounds() {
        getBackground().mutate().setColorFilter(getResources().getColor(R.color.white), android.graphics.PorterDuff.Mode.SRC_ATOP);

        int maxLength = Integer.parseInt(getResources().getString(R.string.preference_value_caltxt_length_max));
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(maxLength);
        setFilters(FilterArray);
        setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
        addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString();
                if (str.contains("^")) {
                    setText(str.replace("^", ""));
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
    }
}
