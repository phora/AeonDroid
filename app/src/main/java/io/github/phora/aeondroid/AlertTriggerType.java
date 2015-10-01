package io.github.phora.aeondroid;

/**
 * Created by phora on 9/25/15.
 */
public enum AlertTriggerType {
    ATRIGGER_GROUP, //stored in the subtriggers table,            ?, ?
    DAY_TYPE,       //fields:                day type,            ?, only from sunrise
    MOON_PHASE,     //fields:                   phase,            ?, ?
    PLANET_SIGN,    //fields:                  planet,     position, sloppy (just in sign)/exact
    PLANETARY_HOUR, //fields:     planetary hour type,            ?, ?
    DATETIME,       //fields:         the... datetime,            ?, and whether it's the date/time/both?
    ASPECT;         //fields:        aspecting planet, natal planet, override orb?

    public int atriggerTypeToInt() {
        switch (this) {
            case ATRIGGER_GROUP:
                return 0;
            case DAY_TYPE:
                return 1;
            case MOON_PHASE:
                return 2;
            case PLANET_SIGN:
                return 3;
            case PLANETARY_HOUR:
                return 4;
            case DATETIME:
                return 5;
            case ASPECT:
                return 6;
            default:
                return -1;
        }
    }

    public static AlertTriggerType intToAtrigger(int i) {
        switch (i) {
            case 0:
                return ATRIGGER_GROUP;
            case 1:
                return DAY_TYPE;
            case 2:
                return MOON_PHASE;
            case 3:
                return PLANET_SIGN;
            case 4:
                return PLANETARY_HOUR;
            case 5:
                return DATETIME;
            case 6:
                return ASPECT;
            default:
                return null;
        }
    }
}
