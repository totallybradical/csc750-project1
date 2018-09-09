package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import com.google.inject.Inject;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.db.*;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import play.mvc.*;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
@Singleton
public class HomeController extends Controller {

    @Inject Database db;
    List<Double> last5Speeds;
    double prevLat;
    double prevLong;
    double totalDist;
    double prevTotalDist;
    int delay; // milliseconds

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(views.html.index.render());
    }

    /**
     * degToRad
     *  Used to convert a degree value to radians
     * @param deg
     * @return radian equivalent of deg
     */
    public double degToRad(double deg) {
        return deg * (Math.PI/180);
    }

    /**
     * computeDist
     * @param thisLat
     * @param thisLong
     * @return
     */
    public double computeDist(double thisLat, double thisLong) {
        /**
         * Use the haversine formula to compute distance
         *  Reference: https://en.wikipedia.org/wiki/Haversine_formula
         */
        double a = Math.sin((degToRad(thisLat - prevLat))/2) * Math.sin((degToRad(thisLat - prevLat))/2) +
                Math.cos(degToRad(prevLat)) * Math.cos(degToRad(thisLat)) *
                        Math.sin(degToRad(thisLong - prevLong)) * Math.sin(degToRad(thisLong - prevLong));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = 6371e3 * c;
        return d;
    }


    /**
     * handleUpdates
     *  Method to execute when data is posted to /locationupdate
     */
    @Inject
    FormFactory formFactory;
    public Result handleupdates() {
        DynamicForm dynamicForm = formFactory.form().bindFromRequest();
        String jsonUsername = dynamicForm.get("username");
        String jsonTimestamp = dynamicForm.get("timestamp");
        String jsonLatitude = dynamicForm.get("latitude");
        String jsonLongitude = dynamicForm.get("longitude");
        String jsonNewSession = dynamicForm.get("newSession");

        // Log posted values
        Logger.debug("Received: " + jsonUsername + ", " + jsonTimestamp + ", " +
                jsonLatitude + ", " + jsonLongitude + ", " + jsonNewSession);

        // Check for nulls
        if (jsonUsername == null) {
            return badRequest("Error: Missing username");
        } else if (jsonTimestamp == null) {
            return badRequest("Error: Missing timestamp");
        } else if (jsonLatitude == null) {
            return badRequest("Error: Missing latitude");
        } else if (jsonLongitude == null) {
            return badRequest("Error: Missing longitude");
        } else {
            // Parse appropriate data types
            String username = jsonUsername;
            BigInteger timestamp = new BigInteger(jsonTimestamp);
            double latitude = Double.parseDouble(jsonLatitude);
            double longitude = Double.parseDouble(jsonLongitude);
            boolean newSession = Boolean.parseBoolean(jsonNewSession);

            // Generate SQL statements to execute:
            //  SQL statement for creating a new table
            String sqlCreateTable =
                    "CREATE TABLE IF NOT EXISTS locationData (\n"
                    + "	username TEXT NOT NULL,\n"
                    + "	timestamp INTEGER,\n"
                    + "	latitude REAL,\n"
                    + "	longitude REAL\n"
                    + ");";
            //  SQL statement to clear out any prior information about this user
            String sqlDeleteUserData =
                    "DELETE FROM locationData WHERE username='" + username + "';";
            //  SQL statement to append location data to table
            String sqlInsertData =
                    "INSERT INTO locationData VALUES (\n"
                    + "'" + username + "',\n"
                    + timestamp + ",\n"
                    + latitude + ",\n"
                    + longitude + ");";

            // Open connection to project1.db
            try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
                // Execute the create table statement
                stmt.execute(sqlCreateTable);

                // Execute the delete old user data statement if flag is true
                if (newSession) {
                    stmt.execute(sqlDeleteUserData);
                    last5Speeds = new ArrayList<>();
                    prevLat = latitude;
                    prevLong = longitude;
                    totalDist = 0.0;
                    prevTotalDist = 0.0;
                    delay = 5000;
                }

                // Compute distance traveled (previous coords vs current coords)
                totalDist = totalDist + computeDist(latitude, longitude);

                // Calculate speed (m/s)
                double currentSpeed = (totalDist - prevTotalDist) / (delay / 1000);

                // Execute the insert data statement to store data in db
                stmt.execute(sqlInsertData);

                // Maintain the most recent coordinates for next computation
                prevLat = latitude;
                prevLong = longitude;
                prevTotalDist = totalDist;

                // Store current speed in list of past 5 speeds
                last5Speeds.add(currentSpeed);
                if (last5Speeds.size() > 5) {
                    last5Speeds.remove(0);
                }

                // Calculate moving average of speed
                double sumOfSpeeds = 0.0;
                for (double speed : last5Speeds) {
                    sumOfSpeeds += speed;
                }
                double avgSpeed = sumOfSpeeds / last5Speeds.size();
                Logger.debug("Average speed: " + Double.toString(avgSpeed) + " m/s");

                if (avgSpeed <= 1.0) {
                    // If avgSpeed is less than or equal to 1 m/s, delay is 5s
                    delay = 5000;
                } else if (avgSpeed >= 20.0) {
                    // If avgSpeed is greater than or equal to 20 m/s, delay is 1s
                    delay = 1000;
                } else {
                    // Interpolate between 5s and 1s for avgSpeed between 1 m/s and 20 m/s
                    double slope = -0.210526315789473684;
                    double b = 5.210526315789473684;
                    delay = (int) (Math.round(((slope * avgSpeed) + b) * 1000));
                }
            } catch (Exception e) {
                Logger.debug(e.getMessage());
            }

            ObjectMapper mapper = new ObjectMapper();
            try {
                String jsonServerResponse = "{\"totalDist\":\"" + totalDist + "\",\"delay\":\"" + delay + "\"}";
                Logger.debug(jsonServerResponse);
                return ok(mapper.readTree(jsonServerResponse));
            } catch (Exception e) {
                return badRequest("Failed to return JSON");
            }
        }
    }
}
