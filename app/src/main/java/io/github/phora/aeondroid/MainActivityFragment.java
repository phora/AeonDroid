package io.github.phora.aeondroid;

import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.Date;

import swisseph.SweDate;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends ListFragment {

    private volatile CheckPlanetaryHoursThread checking_thread = null;

    public MainActivityFragment() {
    }

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivity ma = (MainActivity)getActivity();
        if (ma != null && !(ma.isUsingGPS() && ma.isGpsAvailable())) {
            refreshFragment();
        }
    }

    public synchronized void refreshFragment() {
        MainActivity ma = (MainActivity)getActivity();
        if (ma != null) {
            Date d = new Date();
            PlanetaryHoursAdapter pha = new PlanetaryHoursAdapter(ma, ma.getEphmeris().getPlanetaryHours(d));
            setListAdapter(pha);
            if (checking_thread == null) {
                checking_thread = new CheckPlanetaryHoursThread(this, 1000);
                checking_thread.start();
            }
            else {
                Thread moribund = checking_thread;
                checking_thread = null;
                moribund.interrupt();
                checking_thread = new CheckPlanetaryHoursThread(this, 1000);
                checking_thread.start();
            }
        }
    }

    private class CheckPlanetaryHoursThread extends Thread {
        private int sleepVal;
        private Fragment fragment;
        private int lastIndex = -1;
        private boolean _autoScrolledOnce;

        public CheckPlanetaryHoursThread(Fragment f, int milliseconds) {
            this.sleepVal = milliseconds;
            this.fragment = f;
        }

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    Thread.sleep(this.sleepVal);

                    Date d = new Date();

                    final PlanetaryHoursAdapter pha = (PlanetaryHoursAdapter)getListAdapter();
                    int item_count = pha.getCount();

                    if (lastIndex == -1) {
                        for (int i=0; i<item_count;i++) {
                            PlanetaryHour ph = pha.getItem(i);
                            Date hour_d = SweDate.getDate(ph.getHourStamp());
                            Date hour_end_d = SweDate.getDate(ph.getHourStamp()+ph.getHourLength());
                            if (hour_d.compareTo(d) <= 0 && hour_end_d.compareTo(d) >= 0) {
                                lastIndex = i;
                                break;
                            }
                        }
                    }
                    else {
                        for (int i=lastIndex; i<item_count;i++) {
                            PlanetaryHour ph = pha.getItem(i);
                            Date hour_d = SweDate.getDate(ph.getHourStamp());
                            Date hour_end_d = SweDate.getDate(ph.getHourStamp()+ph.getHourLength());
                            if (hour_d.compareTo(d) <= 0 && hour_end_d.compareTo(d) >= 0) {
                                lastIndex = i;
                                break;
                            }
                        }
                    }



                    if (!_autoScrolledOnce) {
                        fragment.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Log.d("CheckPHoursThread", "Current hour is " + lastIndex);
                                getListView().setSelection(lastIndex);
                            }
                        });
                        _autoScrolledOnce = true;
                    }

                    fragment.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pha.setHourSelection(lastIndex);
                        }
                    });
                }
            } catch (InterruptedException e) {

            }
        }
    }
}