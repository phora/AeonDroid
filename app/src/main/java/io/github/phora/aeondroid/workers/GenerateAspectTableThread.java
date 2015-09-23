package io.github.phora.aeondroid.workers;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import io.github.phora.aeondroid.Events;
import io.github.phora.aeondroid.calculations.EphemerisUtils;

/**
 * Created by phora on 9/23/15.
 */
class GenerateAspectTableThread extends Thread {
    private AeonDroidService aeonDroidService;
    AspectHandler aspectHandler;

    public GenerateAspectTableThread(AeonDroidService aeonDroidService) {
        this.aeonDroidService = aeonDroidService;
    }

    @Override
    public void run() {
        Looper.prepare();
        aspectHandler = new AspectHandler(aeonDroidService);
        Looper.loop();
    }

    static class AspectHandler extends Handler {
        private AeonDroidService aeonDroidService;
        public AspectHandler(AeonDroidService aeonDroidService) {
            this.aeonDroidService = aeonDroidService;
        }

        @Override
        public void handleMessage(Message msg) {
            if (aeonDroidService.natalChart == null) {
                return;
            }
            /*else {
                double[] natalChart = aeonDroidService.natalChart;
                double[] currentChart = (double[])msg.obj;


                double[] outputs = new double[natalChart.length * currentChart.length];

                Intent intent = new Intent();
                intent.setAction(Events.ASPECT_TABLE_GENERATED);
                for (int i = 0; i< natalChart.length; i++) {
                    for (int j = 0; j < currentChart.length; i++) {
                        outputs[i*10+j] = Math.abs(natalChart[i]-currentChart[j]);
                    }
                }
                intent.putExtra(Events.EXTRA_CHART_DATA, outputs);
                aeonDroidService.localBroadcastManager.sendBroadcast(intent);
            }*/
        }
    }
}
