package com.bufalari.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for generating map links for different GPS applications.
 */
public class MapLinkGenerator {

    /**
     * Generates map links for various GPS applications based on latitude and longitude.
     *
     * @param latitude  The latitude of the location.
     * @param longitude The longitude of the location.
     * @return A map of GPS application names to their corresponding links.
     */
    public static Map<String, String> generateMapLinks(double latitude, double longitude) {
        Map<String, String> mapLinks = new LinkedHashMap<>();

        mapLinks.put("googleMaps", generateGoogleMapsLink(latitude, longitude));
        mapLinks.put("waze", generateWazeLink(latitude, longitude));
        mapLinks.put("appleMaps", generateAppleMapsLink(latitude, longitude));
        mapLinks.put("sygic", generateSygicLink(latitude, longitude));
        mapLinks.put("hereWeGo", generateHereWeGoLink(latitude, longitude));

        return mapLinks;
    }

    /**
     * Generates a Google Maps link.
     *
     * @param latitude  The latitude of the location.
     * @param longitude The longitude of the location.
     * @return The Google Maps link.
     */
    private static String generateGoogleMapsLink(double latitude, double longitude) {
        return String.format("https://www.google.com/maps/search/?api=1&query=%s,%s", latitude, longitude);
    }

    /**
     * Generates a Waze link.
     *
     * @param latitude  The latitude of the location.
     * @param longitude The longitude of the location.
     * @return The Waze link.
     */
    private static String generateWazeLink(double latitude, double longitude) {
        return String.format("https://waze.com/ul?ll=%s,%s&navigate=yes", latitude, longitude);
    }

    /**
     * Generates an Apple Maps link.
     *
     * @param latitude  The latitude of the location.
     * @param longitude The longitude of the location.
     * @return The Apple Maps link.
     */
    private static String generateAppleMapsLink(double latitude, double longitude) {
        return String.format("http://maps.apple.com/?daddr=%s,%s", latitude, longitude);
    }

    /**
     * Generates a Sygic link.
     *
     * @param latitude  The latitude of the location.
     * @param longitude The longitude of the location.
     * @return The Sygic link.
     */
    private static String generateSygicLink(double latitude, double longitude) {
        return String.format("com.sygic.aura://coordinate|%s|%s", latitude, longitude);
    }

    /**
     * Generates a HERE WeGo link.
     *
     * @param latitude  The latitude of the location.
     * @param longitude The longitude of the location.
     * @return The HERE WeGo link.
     */
    private static String generateHereWeGoLink(double latitude, double longitude) {
        return String.format("https://wego.here.com/directions/mix//%s,%s", latitude, longitude);
    }
}