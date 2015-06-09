package edu.hm.network;

/**
 * Created by Bernd on 23.05.2015.
 */
public class ServerConfig {

    public static final double startLongDouble = 0;
    public static final double startLatDouble = 0;
    public static final double endLongDouble = 10;
    public static final double endLatDouble = 10;
    public static final int rowAmount = 5;
    public static final int columnAmount = 5;

    // Repeat Every Week
    public static final long timePeriodRepetitionCriteria = 604_800_000;

    // Every half an hour 7*24*2
    public static final int timePeriodRepetitions = 336;
}
