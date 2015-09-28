package io.github.phora.aeondroid.widgets;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import io.github.phora.aeondroid.DBHelper;
import io.github.phora.aeondroid.Events;
import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.calculations.EphemerisUtils;
import io.github.phora.aeondroid.model.adapters.AspectAdapter;
import io.github.phora.aeondroid.model.AspectConfig;
import io.github.phora.aeondroid.model.AspectEntry;
import io.github.phora.aeondroid.workers.AeonDroidService;


public class AspectsView extends LinearLayout implements BroadcastReceivable, AeonDroidServiceable {
    private Date timeStamp = null;

    private GridView mAspects;
    private AeonDroidService mAeonDroidService;

    private List<ReceiverFilterPair> backingStore;

    public AspectsView(Context context, AeonDroidService aeonDroidService, Date date) {
        // Required empty public constructor
        super(context);
        View.inflate(context, R.layout.fragment_aspects, this);
        onFinishInflate();

        backingStore = new LinkedList<>();
        IntentFilter intentFilter = new IntentFilter(Events.PLANET_POS);
        AspectReceiver aspectReceiver = new AspectReceiver();

        backingStore.add(new ReceiverFilterPair(aspectReceiver, intentFilter));
        mAeonDroidService = aeonDroidService;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAspects = (GridView)findViewById(R.id.AspectsGrid_Table);
        mAspects.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AspectEntry ae = (AspectEntry)mAspects.getItemAtPosition(i);
                if (ae.isHeader()) {
                    return;
                }
                Context context = getContext();
                String[] planetNames = getResources().getStringArray(R.array.PlanetChartNames);
                String fromPlanet = planetNames[ae.getFromPlanetType()];
                String toPlanet = planetNames[ae.getToPlanetType()];
                String fromString = EphemerisUtils.degreesToSignString(context, ae.getFromPlanetPos());
                String toString = EphemerisUtils.degreesToSignString(context, ae.getToPlanetPos());
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(getContext().getString(R.string.Aspects_Details, fromPlanet, fromString, toPlanet, toString));
                builder.setNegativeButton(R.string.OK, null);
                builder.create().show();
            }
        });
    }

    @Override
    public boolean hasReceivers() {
        return true;
    }

    @Override
    public List<ReceiverFilterPair> getReceivers() {
        return backingStore;
    }

    @Override
    public void setServiceReference(AeonDroidService aeonDroidService) {
        mAeonDroidService = aeonDroidService;
    }

    private class AspectReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Intent peekIntent = new Intent(context, AeonDroidService.class);
            //AeonDroidService.AeonDroidBinder adb = (AeonDroidService.AeonDroidBinder)peekService(context, peekIntent);

            if (mAeonDroidService == null || mAspects == null) {
                return;
            }

            double[] rawChart = intent.getDoubleArrayExtra(Events.EXTRA_PLANET_POS);
            double[] natalChart = mAeonDroidService.getNatalChart();
            if (rawChart != null && natalChart != null) {
                new TransformRawtoAspects(natalChart).execute(rawChart);
            }
        }
    }

    private class TransformRawtoAspects extends AsyncTask<double[], Void, ArrayList<AspectEntry>> {
        private double[] chartAgainst;

        public TransformRawtoAspects(double[] chartAgainst) {
            this.chartAgainst = chartAgainst;
        }

        @Override
        protected ArrayList<AspectEntry> doInBackground(double[]... doubles) {
            if (doubles.length == 0) {
                return null;
            }
            ArrayList<AspectEntry> aspects = new ArrayList<>(121);

            // blank filler spot
            aspects.add(new AspectEntry(-1, -1, 0, -1, -1, -1));

            int chartAgainstCount = chartAgainst.length;
            int chartToCount = doubles[0].length;

            double[] chartTo = doubles[0];
            boolean gen_headers = true;
            for (int i = 0; i < chartAgainstCount; i++) {
                if (gen_headers) {
                    gen_headers = false;
                    for (int j = 0; j < chartToCount; j++) {
                        AspectEntry header_ae = new AspectEntry(i, j, 0, -1, -1, -1);
                        aspects.add(header_ae);
                    }
                }
                AspectEntry start_ae = new AspectEntry(i, -1, 0, -1, -1, -1);
                aspects.add(start_ae);
                for (int j = 0; j < chartToCount; j++) {
                    double pos1 = chartAgainst[i];
                    double pos2 = chartTo[j];

                    AspectEntry ae = new AspectEntry(i, j, 0, -1, pos1, pos2);
                    aspects.add(ae);
                }
            }

            return aspects;
        }

        @Override
        protected void onPostExecute(ArrayList<AspectEntry> aspectEntries) {
            SparseArray<AspectConfig> orbConfig = DBHelper.getInstance(getContext()).getOrbs();
            mAspects.setAdapter(new AspectAdapter(getContext(), orbConfig, aspectEntries));
        }
    }
}
