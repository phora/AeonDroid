package io.github.phora.aeondroid.model.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.util.LongSparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import io.github.phora.aeondroid.BackedOrderWatcher;
import io.github.phora.aeondroid.DBHelper;
import io.github.phora.aeondroid.R;

/**
 * Created by phora on 10/19/15.
 */
public class StepReorderAdapter extends CursorAdapter {
    private LongSparseArray<int[]> pendingStepChanges;

    public StepReorderAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        pendingStepChanges = new LongSparseArray<>();
    }

    public LongSparseArray<int[]> getPendingStepChanges() {
        return pendingStepChanges;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return View.inflate(context, R.layout.step_reorder_item, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView stepImage = (ImageView) view.findViewById(R.id.StepItem_Image);
        View colorPreview = view.findViewById(R.id.StepItem_Color);
        EditText editOrder = (EditText)view.findViewById(R.id.StepItem_Order);
        TextView stepUri = (TextView) view.findViewById(R.id.StepItem_Uri);
        TextView descPreview = (TextView) view.findViewById(R.id.StepItem_Desc);

        long id = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID));
        int step = cursor.getInt(cursor.getColumnIndex(DBHelper.LINKED_STEP_ORDER));
        Uri imageUri = Uri.parse(cursor.getString(cursor.getColumnIndex(DBHelper.STEP_IMAGE)));
        String otherUri = cursor.getString(cursor.getColumnIndex(DBHelper.STEP_LINK));
        String desc = cursor.getString(cursor.getColumnIndex(DBHelper.STEP_DESCRIPTION));
        int color = cursor.getInt(cursor.getColumnIndex(DBHelper.STEP_COLOR));

        int[] pendingStore = pendingStepChanges.get(id);

        if (pendingStore == null) {
            pendingStepChanges.put(id, new int[]{step, step});
        }
        else {
            pendingStore[0] = step;
        }

        stepImage.setImageURI(imageUri);
        stepUri.setText(otherUri);
        colorPreview.setBackgroundColor(color);

        int descLength = desc.length();
        if (descLength > 50) {
            String genPreview = String.format("%s ... %s", desc.substring(0, 25),
                    desc.substring(descLength-25));
            descPreview.setText(genPreview);
        }
        else {
            descPreview.setText(desc.substring(0, Math.min(50, descLength)));
        }
        editOrder.setTag(id);
        editOrder.addTextChangedListener(new BackedOrderWatcher(editOrder, pendingStepChanges));
        editOrder.setText(String.valueOf(step));
    }
}
