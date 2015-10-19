package io.github.phora.aeondroid;

import android.support.v4.util.LongSparseArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Created by phora on 10/19/15.
 */
public class BackedOrderWatcher implements TextWatcher {
    private final EditText editText;
    private final LongSparseArray<int[]> backingStore;

    public BackedOrderWatcher(EditText editText, LongSparseArray<int[]> backingStore)
    {
        this.editText = editText;
        this.backingStore = backingStore;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        Long id = (Long) editText.getTag();
        if (id > 0) {
            Integer parsedInt;
            try {
                parsedInt = Integer.valueOf(editText.getText().toString());
                backingStore.get(id)[1] = parsedInt;
            }
            catch (NumberFormatException e) {
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
