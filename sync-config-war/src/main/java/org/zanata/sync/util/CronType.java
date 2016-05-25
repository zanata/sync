package org.zanata.sync.util;

import lombok.Getter;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Getter
public enum CronType {
    MANUAL("Only run manually by user", ""),
    ONE_HOUR("Hourly", "0 0 0/1 * * ?"),
    TWO_HOUR("2 hourly", "0 0 0/2 * * ?"),
    SIX_HOUR("6 hourly (6:00am,12:00am,6pm,12pm)", "0 0 0/6 * * ?"),
    TWELVE_HOUR("12 hour (12:00am/pm)", "0 0 0,12 * * ?"),
    ONE_DAY("24 hour(12:00am)", "0 0 0 * * ?"),
    //this is for testing purposes
    THIRTY_SECONDS("30 seconds", "0/30 * * * * ?");

    private final String display;
    private final String expression;

    CronType(String display, String expression) {
        this.display = display;
        this.expression = expression;
    }

    public static CronType getTypeFromExpression(String expression) {
        for (CronType cronType : values()) {
            if (cronType.getExpression().equals(expression)) {
                return cronType;
            }
        }
        throw new IllegalArgumentException(expression);
    }

    public static CronType getTypeFromDisplay(String display) {
        for (CronType cronType : values()) {
            if (cronType.getDisplay().equals(display)) {
                return cronType;
            }
        }
        throw new IllegalArgumentException(display);
    }
}
