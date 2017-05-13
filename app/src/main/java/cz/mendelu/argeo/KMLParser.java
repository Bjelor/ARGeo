package cz.mendelu.argeo;

import android.content.Context;
import android.net.Uri;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class KMLParser {

    public static final String TAG = KMLParser.class.getSimpleName();

    private static final String PLACEMARK = "Placemark";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String ALTITUDE = "altitude";
    private static final String ICON = "Icon";

    public static List<Placemark> parseFile(InputStream stream) {
        List<Placemark> placemarks = new ArrayList<>();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(stream, null);

            Placemark placemark = null;

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String name;
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase(PLACEMARK)) {
                            placemark = new Placemark();
                        } else if (placemark != null) {
                            if (name.equalsIgnoreCase(NAME)) {
                                placemark.setTitle(parser.nextText().trim());
                            } else if (name.equalsIgnoreCase(DESCRIPTION)) {
                                placemark.setDescription(parser.nextText().trim());
                            } else if (name.equalsIgnoreCase(LATITUDE)) {
                                placemark.setLatitude(Double.valueOf(parser.nextText()));
                            } else if (name.equalsIgnoreCase(LONGITUDE)) {
                                placemark.setLongitude(Double.valueOf(parser.nextText()));
                            } else if (name.equalsIgnoreCase(ALTITUDE)) {
                                placemark.setAltitude(Double.valueOf(parser.nextText()));
                            } else if (name.equalsIgnoreCase(ICON)) {
                                placemark.setIconUrl(parser.nextText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase(PLACEMARK) && placemark != null) {
                            placemarks.add(placemark);
                        }
                        break;
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException | IOException | NullPointerException e) {
            e.printStackTrace();
        }

        return placemarks;
    }
}
