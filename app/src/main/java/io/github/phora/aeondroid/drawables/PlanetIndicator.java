package io.github.phora.aeondroid.drawables;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import io.github.phora.aeondroid.R;

/**
 * Created by phora on 9/16/15.
 */
public class PlanetIndicator {

    private Context mContext;
    private static PlanetIndicator sInstance;

    private static Drawable BASE_CHAKRA = null;
    private static Drawable SACRAL_CHAKRA = null;
    private static Drawable SOLAR_CHAKRA = null;
    private static Drawable HEART_CHAKRA = null;
    private static Drawable THROAT_CHAKRA = null;
    private static Drawable SIXTH_CHAKRA = null;
    private static Drawable CROWN_CHAKRA = null;

    public static synchronized PlanetIndicator getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new PlanetIndicator(context.getApplicationContext());
        }
        return sInstance;
    }

    private PlanetIndicator(Context context) {
        mContext = context;
    }

    public int getChakraNoti(int i) {
        switch (i) {
            case 0:
                return R.drawable.solar_chakra;
            case 1:
                return R.drawable.heart_chakra;
            case 2:
                return R.drawable.throat_chakra;
            case 3:
                return R.drawable.sixth_chakra;
            case 4:
                return R.drawable.base_chakra;
            case 5:
                return R.drawable.crown_chakra;
            case 6:
                return R.drawable.sacral_chakra;
            default:
                return 0;
        }
    }

    public int getPlanetNoti(int i) {
        switch (i) {
            case 0:
                return R.drawable.sun_dark;
            case 1:
                return R.drawable.venus_dark;
            case 2:
                return R.drawable.mercury_dark;
            case 3:
                return R.drawable.moon_dark;
            case 4:
                return R.drawable.saturn_dark;
            case 5:
                return R.drawable.jupiter_dark;
            case 6:
                return R.drawable.mars_dark;
            default:
                return 0;
        }
    }

    public int getPlanetSymbol(int i) {
        TypedValue tv = new TypedValue();
        switch (i) {
            case 0:
                mContext.getTheme().resolveAttribute(R.attr.PlanetaryHours_Sun, tv, false);
                return tv.data;
            case 1:
                mContext.getTheme().resolveAttribute(R.attr.PlanetaryHours_Venus, tv, false);
                return tv.data;
            case 2:
                mContext.getTheme().resolveAttribute(R.attr.PlanetaryHours_Mercury, tv, false);
                return tv.data;
            case 3:
                mContext.getTheme().resolveAttribute(R.attr.PlanetaryHours_Moon, tv, false);
                return tv.data;
            case 4:
                mContext.getTheme().resolveAttribute(R.attr.PlanetaryHours_Saturn, tv, false);
                return tv.data;
            case 5:
                mContext.getTheme().resolveAttribute(R.attr.PlanetaryHours_Jupiter, tv, false);
                return tv.data;
            case 6:
                mContext.getTheme().resolveAttribute(R.attr.PlanetaryHours_Mars, tv, false);
                return tv.data;
            default:
                return 0;
        }
    }

    public int getPlanetChartSymbol(int i) {
        TypedValue tv = new TypedValue();
        switch (i) {
            case 0:
                mContext.getTheme().resolveAttribute(R.attr.PlanetaryHours_Sun, tv, false);
                return tv.data;
            case 1:
                mContext.getTheme().resolveAttribute(R.attr.PlanetaryHours_Moon, tv, false);
                return tv.data;
            case 2:
                mContext.getTheme().resolveAttribute(R.attr.PlanetaryHours_Mercury, tv, false);
                return tv.data;
            case 3:
                mContext.getTheme().resolveAttribute(R.attr.PlanetaryHours_Venus, tv, false);
                return tv.data;
            case 4:
                mContext.getTheme().resolveAttribute(R.attr.PlanetaryHours_Mars, tv, false);
                return tv.data;
            case 5:
                mContext.getTheme().resolveAttribute(R.attr.PlanetaryHours_Jupiter, tv, false);
                return tv.data;
            case 6:
                mContext.getTheme().resolveAttribute(R.attr.PlanetaryHours_Saturn, tv, false);
                return tv.data;
            case 7:
                mContext.getTheme().resolveAttribute(R.attr.RightNow_Uranus, tv, false);
                return tv.data;
            case 8:
                mContext.getTheme().resolveAttribute(R.attr.RightNow_Neptune, tv, false);
                return tv.data;
            case 9:
                mContext.getTheme().resolveAttribute(R.attr.RightNow_Pluto, tv, false);
                return tv.data;
            default:
                return 0;
        }
    }

    public int getAspectSymbol(int i) {
        TypedValue tv = new TypedValue();
        switch (i) {
            case 0:
                mContext.getTheme().resolveAttribute(R.attr.Aspects_Conjunction, tv, false);
                return tv.data;
            case 1:
                mContext.getTheme().resolveAttribute(R.attr.Aspects_SemiSextile, tv, false);
                return tv.data;
            case 2:
                mContext.getTheme().resolveAttribute(R.attr.Aspects_SemiSquare, tv, false);
                return tv.data;
            case 3:
                mContext.getTheme().resolveAttribute(R.attr.Aspects_Sextile, tv, false);
                return tv.data;
            case 4:
                mContext.getTheme().resolveAttribute(R.attr.Aspects_Quintile, tv, false);
                return tv.data;
            case 5:
                mContext.getTheme().resolveAttribute(R.attr.Aspects_Square, tv, false);
                return tv.data;
            case 6:
                mContext.getTheme().resolveAttribute(R.attr.Aspects_Trine, tv, false);
                return tv.data;
            case 7:
                mContext.getTheme().resolveAttribute(R.attr.Aspects_Sesquisquare, tv, false);
                return tv.data;
            case 8:
                mContext.getTheme().resolveAttribute(R.attr.Aspects_Biquintile, tv, false);
                return tv.data;
            case 9:
                mContext.getTheme().resolveAttribute(R.attr.Aspects_Quincunx, tv, false);
                return tv.data;
            case 10:
                mContext.getTheme().resolveAttribute(R.attr.Aspects_Opposition, tv, false);
                return tv.data;
            default:
                return 0;
        }
    }

    public Drawable getChakraDrawable(int i) {
        Drawable d = null;
        switch(i) {
            case 0:
                if (SOLAR_CHAKRA == null) {
                    SOLAR_CHAKRA = new EquilateralTriangle(Color.argb(255, 255, 200, 0),
                            EquilateralTriangle.Direction.SOUTH);
                }
                d = SOLAR_CHAKRA;
                break;
            case 1:
                if (HEART_CHAKRA == null) {
                    HEART_CHAKRA = new Rhombus(Color.GREEN, Rhombus.Direction.VERTICAL);
                }
                d = HEART_CHAKRA;
                break;
            case 2:
                if (THROAT_CHAKRA == null) {
                    THROAT_CHAKRA = new EquilateralTriangle(Color.argb(255, 0, 178, 255),
                            EquilateralTriangle.Direction.SOUTH);
                }
                d = THROAT_CHAKRA;
                break;
            case 3:
                if (SIXTH_CHAKRA == null) {
                    SIXTH_CHAKRA =  new EquilateralTriangle(Color.BLUE,
                            EquilateralTriangle.Direction.SOUTH);
                }
                d = SIXTH_CHAKRA;
                break;
            case 4:
                if (BASE_CHAKRA == null) {
                    BASE_CHAKRA = new EquilateralTriangle(Color.RED,
                            EquilateralTriangle.Direction.NORTH);
                }
                d = BASE_CHAKRA;
                break;
            case 5:
                if (CROWN_CHAKRA == null) {
                    CROWN_CHAKRA =  new EquilateralTriangle(Color.argb(255, 121, 0, 255),
                            EquilateralTriangle.Direction.SOUTH);
                }
                d = CROWN_CHAKRA;
                break;
            case 6:
                if (SACRAL_CHAKRA == null) {
                    SACRAL_CHAKRA = new EquilateralTriangle(Color.argb(255, 255, 116, 0),
                            EquilateralTriangle.Direction.NORTH);
                }
                d = SACRAL_CHAKRA;
                break;
        }
        return d;
    }

}
