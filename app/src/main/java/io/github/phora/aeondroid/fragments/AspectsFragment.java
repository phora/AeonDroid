package io.github.phora.aeondroid.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AspectsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AspectsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AspectsFragment extends Fragment implements BroadcastReceivable {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private GridView mAspects;

    private List<ReceiverFilterPair> backingStore;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AspectsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AspectsFragment newInstance(String param1, String param2) {
        AspectsFragment fragment = new AspectsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public AspectsFragment() {
        // Required empty public constructor
        backingStore = new LinkedList<>();
        IntentFilter intentFilter = new IntentFilter(Events.PLANET_POS);
        AspectReceiver aspectReceiver = new AspectReceiver();

        backingStore.add(new ReceiverFilterPair(aspectReceiver, intentFilter));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_aspects, container, false);
        mAspects = (GridView)view.findViewById(R.id.AspectsGrid_Table);
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
                builder.setMessage(getString(R.string.Aspects_Details, fromPlanet, fromString, toPlanet, toString));
                builder.setNegativeButton(R.string.OK, null);
                builder.create().show();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /*String[] fromHere = {"0", "1", "2", "3", "4", "5", "6", "7",
                "8", "9", "A", "B", "C", "D", "E", "F"};
        String[] testItems = new String[11*11];*/

        /*List<AspectEntry> aspects = new ArrayList<>(121);

        int count = 121;
        for (int i = 0; i < count; i++) {
            double pos1 = 0;
            double pos2 = 0;

            if (i / 11 == 0 || i % 11 == 0) {
                pos1 = pos2 = -1;
            }

            AspectEntry ae = new AspectEntry(i / 11 -1, i % 11 -1, 0, -1, pos1, pos2);
            aspects.add(ae);
        }
        mAspects.setAdapter(new AspectAdapter(getActivity(), aspects));*/
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            //mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean hasReceivers() {
        return true;
    }

    @Override
    public List<ReceiverFilterPair> getReceivers() {
        return backingStore;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private class AspectReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent peekIntent = new Intent(context, AeonDroidService.class);
            AeonDroidService.AeonDroidBinder adb = (AeonDroidService.AeonDroidBinder)peekService(context, peekIntent);

            if (adb == null || getActivity() == null) {
                return;
            }

            double[] rawChart = intent.getDoubleArrayExtra(Events.EXTRA_PLANET_POS);
            if (rawChart != null) {
                new TransformRawtoAspects(adb.getService().getNatalChart()).execute(rawChart);
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
            mAspects.setAdapter(new AspectAdapter(getActivity(), orbConfig, aspectEntries));
        }
    }
}
