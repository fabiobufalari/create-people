package com.bufalari.createpeople.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Classe utilitária para gerar links de mapas em diferentes aplicativos de GPS.
 * Utility class for generating map links for different GPS applications.
 */
public class MapLinkGenerator {

    /**
     * Gera links de mapa para vários aplicativos de GPS a partir de latitude e longitude.
     * Generates map links for various GPS applications based on latitude and longitude.
     *
     * @param latitude  Latitude do local.
     *                  Location latitude.
     * @param longitude Longitude do local.
     *                  Location longitude.
     * @return Mapa de nome de aplicativo para URL correspondente.
     *         Map of application name to its corresponding URL.
     */
    public static Map<String, String> generateMapLinks(double latitude, double longitude) {
        Map<String, String> mapLinks = new LinkedHashMap<>();
        mapLinks.put("googleMaps", generateGoogleMapsLink(latitude, longitude));
        mapLinks.put("waze", generateWazeLink(latitude, longitude));
        mapLinks.put("appleMaps", generateAppleMapsLink(latitude, longitude));
        mapLinks.put("bingMaps", generateBingMapsLink(latitude, longitude));
        return mapLinks;
    }

    private static String generateGoogleMapsLink(double latitude, double longitude) {
        return String.format("https://www.google.com/maps/search/?api=1&query=%s,%s", latitude, longitude);
    }

    private static String generateWazeLink(double latitude, double longitude) {
        return String.format("https://waze.com/ul?ll=%s,%s&navigate=yes", latitude, longitude);
    }

    private static String generateAppleMapsLink(double latitude, double longitude) {
        return String.format("http://maps.apple.com/?daddr=%s,%s", latitude, longitude);
    }

    private static String generateBingMapsLink(double latitude, double longitude) {
        return String.format("https://www.bing.com/maps/?cp=%s~%s", latitude, longitude);
    }
}
