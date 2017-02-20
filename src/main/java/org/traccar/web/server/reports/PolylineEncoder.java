package org.traccar.web.server.reports;

import org.traccar.web.shared.model.Position;

import java.util.List;

/**
 * Methods to encode and decode a polyline with Google polyline encoding/decoding scheme.
 * See https://developers.google.com/maps/documentation/utilities/polylinealgorithm
 */
public class PolylineEncoder {

    private PolylineEncoder() {
    }

    private static StringBuilder encodeSignedNumber(int num) {
        int sgn_num = num << 1;
        if (num < 0) {
            sgn_num = ~(sgn_num);
        }
        return encodeNumber(sgn_num);
    }

    private static StringBuilder encodeNumber(int num) {
        StringBuilder encodeString = new StringBuilder();
        while (num >= 0x20) {
            int nextValue = (0x20 | (num & 0x1f)) + 63;
            if (nextValue == 92) {
                encodeString.append((char)(nextValue));
            }
            encodeString.append((char)(nextValue));
            num >>= 5;
        }

        num += 63;
        if (num == 92) {
            encodeString.append((char)(num));
        }

        encodeString.append((char)(num));

        return encodeString;
    }

    /**
     * Encode a polyline with Google polyline encoding method
     * @param polyline the polyline
     * @return the encoded polyline, as a String
     */
    public static String encode(List<Position> polyline) {
        StringBuilder encodedPoints = new StringBuilder();
        int prev_lat = 0, prev_lng = 0;
        for (Position trackpoint : polyline) {
            int lat = (int) Math.round(trackpoint.getLatitude() * 1e5);
            int lng = (int) Math.round(trackpoint.getLongitude() * 1e5);
            encodedPoints.append(encodeSignedNumber(lat - prev_lat));
            encodedPoints.append(encodeSignedNumber(lng - prev_lng));
            prev_lat = lat;
            prev_lng = lng;
        }
        return encodedPoints.toString();
    }
}
