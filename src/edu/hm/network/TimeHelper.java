package edu.hm.network;

/**
 * Created by Bernd on 23.05.2015.
 */
public class TimeHelper {

    public static void main(String... args) {
        System.out.printf("TimeZone: %d", getTime());
    }

    public static int getTime() {
        final long currentMillis = System.currentTimeMillis();

        final long timeStepSize = ServerConfig.timePeriodRepetitionCriteria / ServerConfig.timePeriodRepetitions;
        long repetitionOffset = currentMillis % ServerConfig.timePeriodRepetitionCriteria;
        long timePeriod = repetitionOffset / timeStepSize;
        if (timePeriod > ServerConfig.timePeriodRepetitions) {
            throw new RuntimeException("Irgendwas stimmt hier nicht.");
        }
        return (int) timePeriod;
    }

}
