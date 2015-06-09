package edu.hm.network;

import java.util.*;

/**
 * Created by Bernd on 23.05.2015.
 */
public class PositionDeterminationHelper {

    public static int determinateLocationClass(Map<String, ArrayList<Integer>> occuranceMap) throws NotEnoughDevicesException {
        // TODO: Constants threshold and k save in ServerConfig.
        Map<String, ArrayList<Integer>> bestNeighbors = doKNN(occuranceMap, 3, 3);

        // Variante 1: für jeden MapEintrag seperat den Mittelpunkt bestimmen und dann den Mittelpunkt der Mittelpunkte bestimmen
        // TODO: evtl direkt über Längen und Breitengrad?
        ArrayList<LocationHelper.LocationClassGridPosition> averagePoints = new ArrayList<>();
        for (Map.Entry<String, ArrayList<Integer>> entry : bestNeighbors.entrySet()) {
            int sumColumn = 0;
            int sumRow = 0;
            for (Integer locationClass : entry.getValue()) {
                final LocationHelper.LocationClassGridPosition pos = LocationHelper.LocationClassGridPosition.fromLocationClass(locationClass);
                sumRow += pos.row;
                sumColumn += pos.column;
            }
            averagePoints.add(new LocationHelper.LocationClassGridPosition((double) sumColumn / entry.getValue().size(), (double) sumRow / entry.getValue().size()));
        }

        int sumColumn = 0;
        int sumRow = 0;
        for (LocationHelper.LocationClassGridPosition position : averagePoints) {
            sumColumn += position.column;
            sumRow += position.row;
        }
        LocationHelper.LocationClassGridPosition resultPosition = new LocationHelper.LocationClassGridPosition(sumColumn / averagePoints.size(), sumRow / averagePoints.size());
        int resultLocationClass = resultPosition.toLocationClass();
        return resultLocationClass;
    }

    /**
     * Bestimmt anhand des Knn die k besten Einträge.
     * @param occuranceMap Map mit allen Vorkommnissen der Geräte zur vorgegebenen Zeit aus der Datenbank.
     * @param threshold Mindestanzahl an Daten pro Gerät.
     * @param k Anzahl der zurückgelieferten Einträge
     * @return leifert die k besten Einträge zurück. Oder null, falls die Anzahl der verwertbaren Geräte < k
     */
    private static Map<String, ArrayList<Integer>> doKNN(Map<String, ArrayList<Integer>> occuranceMap, int threshold, int k) throws NotEnoughDevicesException {
        /* Filter threshold. */
        for (Map.Entry<String, ArrayList<Integer>> entry : occuranceMap.entrySet()) {
            // Remove all Entries where the number of locationclasses is smaller than the threshold.
            if (entry.getValue().size() < threshold) {
                occuranceMap.remove(entry.getKey());
            }
        }

        // List with size map.size()^2 - map.size()
        DistanceRow[] distanceRows = new DistanceRow[occuranceMap.size() * occuranceMap.size() - occuranceMap.size()];
        int rowCounter = 0;
        for (Map.Entry<String, ArrayList<Integer>> outerEntry : occuranceMap.entrySet()) {
            final ArrayList<Integer> outerArrayList = outerEntry.getValue();
            for (Map.Entry<String, ArrayList<Integer>> innerEntry : occuranceMap.entrySet()) {
                if (outerEntry.getKey().equals(innerEntry.getKey())) continue;
                int distance = 0;

                final ArrayList<Integer> innerArrayList = innerEntry.getValue();
                // Compare each element of your LocationClass with the other one.
                for (Integer locationClass : outerArrayList) {
                    distance += (innerArrayList.contains(locationClass)) ? 0 : 1;
                }
                distanceRows[rowCounter++] = new DistanceRow(outerEntry.getKey(), innerEntry.getKey(), distance);
            }
        }

        // Abbruch, falls die Anzahl der verwertbaren Geräte < k
        if (distanceRows.length < k) {
            throw new NotEnoughDevicesException();
        }

        Arrays.sort(distanceRows); // Beste Ergebnisse (mit der kürzesten Distanz) stehen ganz oben


        // select the first k elements
        Map<String, ArrayList<Integer>> result = new TreeMap<>();
        for (int z = 0; z < k; z++) {
            final String key = distanceRows[z].device1;
            result.put(key, occuranceMap.get(key));
        }

        return Collections.unmodifiableMap(result);
    }

    private static class DistanceRow implements Comparable<DistanceRow> {
        final String device1;
        final String device2;
        final int distance;

        public DistanceRow(String device1, String device2, int distance) {
            this.device1 = device1;
            this.device2 = device2;
            this.distance = distance;
        }

        @Override
        public int compareTo(DistanceRow o) {
            return this.distance - o.distance;
        }
    }
}
