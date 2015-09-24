package io.github.phora.aeondroid.activities;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.github.phora.aeondroid.DBHelper;
import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.model.AspectConfig;
import io.github.phora.aeondroid.model.adapters.OrbsAdapter;

public class OrbsActivity extends ListActivity {

    Context context;
    private CompoundButton.OnCheckedChangeListener mCheckChange;
    private AdapterView.OnItemClickListener mItemEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orbs);
        context = this;
        mCheckChange = new CheckboxListener();
        mItemEdit = new EditAspectListener();
        getListView().setOnItemClickListener(mItemEdit);
        new LoadOrbsTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_orbs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_reset) {
            new ResetOrbsTask().execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class LoadOrbsTask extends AsyncTask<Void, Void, SparseArray<AspectConfig>> {
        ProgressDialog pd;
        @Override
        protected void onPostExecute(SparseArray<AspectConfig> orbConfig) {
            pd.dismiss();
            setListAdapter(new OrbsAdapter(context, orbConfig, mCheckChange));
        }

        @Override
        protected SparseArray<AspectConfig> doInBackground(Void... voids) {
            return DBHelper.getInstance(context).getOrbs();
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(context);
            pd.setIndeterminate(true);
            pd.setMessage(getString(R.string.OrbsActivity_LoadingOrbs));
            pd.show();
        }
    }

    private class CheckboxListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            Integer pos = (Integer)compoundButton.getTag();
            Log.d("OrbsActivity", "Aspect " + pos + " is now " + b);
            OrbsAdapter oa =  (OrbsAdapter)getListAdapter();
            SparseArray<AspectConfig> orbs = oa.getOrbConfig();
            AspectConfig ac = orbs.valueAt(pos);
            ac.setShown(b);
            int degree = orbs.keyAt(pos);
            DBHelper.getInstance(context).updateOrb(degree, ac.getOrb(), ac.isShown());
        }
    }

    private class EditAspectListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final OrbsAdapter oa =  (OrbsAdapter)getListAdapter();
            final AspectConfig ac = oa.getOrbConfig().valueAt(pos);
            final int position = pos;

            View inputWidgets = LayoutInflater.from(context).inflate(R.layout.orb_dialog, null);

            TextView tv = (TextView) inputWidgets.findViewById(R.id.OrbDialog_DefaultDisplay);
            final EditText et = (EditText) inputWidgets.findViewById(R.id.OrbDialog_OrbValue);
            tv.setText(getString(R.string.OrbDialog_DefaultDisplayFmt, AspectConfig.DEFAULT_ORBS[pos]));
            et.setText(String.valueOf(ac.getOrb()));

            builder.setTitle(getString(R.string.OrbDialog_TitleFmt, getString(AspectConfig.ASPECT_NAMES[pos])));
            builder.setView(inputWidgets);

            builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        ac.setOrb(Double.valueOf(et.getText().toString()));
                        int degree = oa.getOrbConfig().keyAt(position);
                        DBHelper.getInstance(context).updateOrb(degree, ac.getOrb(), ac.isShown());
                        oa.notifyDataSetChanged();
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton(R.string.Cancel, null);

            builder.create().show();
        }
    }

    private class ResetOrbsTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog pd;
        @Override
        protected Void doInBackground(Void... voids) {
            OrbsAdapter oa = (OrbsAdapter) getListAdapter();
            SparseArray<AspectConfig> orbs = oa.getOrbConfig();

            for (int i = 0; i < orbs.size(); i++) {
                AspectConfig ac = orbs.valueAt(i);
                ac.setShown(AspectConfig.DEFAULT_VISIBILITY[i]);
                ac.setOrb(AspectConfig.DEFAULT_ORBS[i]);
            }
            DBHelper.getInstance(context).batchUpdateOrbs(orbs);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            pd.dismiss();
            OrbsAdapter oa = (OrbsAdapter) getListAdapter();
            oa.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(context);
            pd.setIndeterminate(true);
            pd.setMessage(getString(R.string.OrbsActivity_ResettingOrbs));
            pd.show();
        }
    }
}
