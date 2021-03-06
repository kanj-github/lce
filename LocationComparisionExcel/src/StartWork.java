import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

import com.google.gson.Gson;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * Created by kanj on 15/6/16.
 */
public class StartWork {
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    //private static final String ADDRESS_SERVICE_HOST = "http://flowaddressservice-prod.us-east-1.elasticbeanstalk.com";
    //private static final String ADDRESS_SERVICE_HOST = "http://52.4.23.126:27001"; // Local PC
    private static final String ADDRESS_SERVICE_PATH = "v1/addresses/";

    private static final String CONFIG_FILE = "/home/ubuntu/kanj/lce.cfg";
    //private static final String CONFIG_FILE = "/home/kanj/Desktop/lce.cfg"; // Local PC
    private static final String QUERY = "SELECT clusterItemDeliveryDetails.id, latitude, longitude, accuracy, clusterItems.destinationAddressId, clusterItemDeliveryDetails.updatedAt, trips.vehicleId " +
            "FROM clusterItemDeliveryDetails, clusterItems, clusters, trips " +
            "WHERE (clusterItemDeliveryDetails.updatedAt BETWEEN '%1$s' AND '%2$s')" +
                " AND (clusterItems.id = clusterItemDeliveryDetails.clusterItemId)" +
                " AND (clusterItems.clusterId = clusters.id)" +
                " AND (clusters.tripId = trips.id) " +
            "ORDER BY clusterItems.clusterId, clusterItemDeliveryDetails.updatedAt;";

    public static void main(String args[]) {
        if (args.length != 2) {
            System.out.println("Provide 2 arguments- start and end date in SQL format (eg. \"2016-06-20 00:00:00\")");
            return;
        }

        // Test date arguments
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            sdf.parse(args[0]);
            sdf.parse(args[1]);
        } catch (ParseException pe) {
            pe.printStackTrace();
            return;
        }

        try {
            Scanner scanner = new Scanner(new FileInputStream(CONFIG_FILE)).useDelimiter("\n");
            String dbUrl = scanner.next();
            String dbUser = scanner.next();
            String dbPassword = scanner.next();
            String addressServiceHost = scanner.next();
            scanner.close();

            Class.forName(JDBC_DRIVER);
            Connection sqlConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            Statement statement = sqlConnection.createStatement();
            ResultSet rows = statement.executeQuery(String.format(QUERY, args[0], args[1]));

            if (rows == null) {
                System.out.println("Null rows returned from SQL");
                return;
            }

            ExcelWriter ew = new ExcelWriter();
            Gson gsonParser = new Gson();
            JerseyClient client = JerseyClientBuilder.createClient();
            int count = 0;
            while (rows.next()) {
                System.out.println("Processing row " + count++);
                try {
                    String destAddressId = rows.getString("destinationAddressId");
                    if (destAddressId != null) {
                        WebTarget target = client.target(addressServiceHost).path(ADDRESS_SERVICE_PATH + destAddressId);
                        Invocation.Builder builder = target.request();

                        Response response = builder.get();

                        String jsonString = response.readEntity(String.class);
                        GoogleLocation loc = gsonParser.fromJson(jsonString, GoogleLocation.class);

                        if (loc != null && loc.getLocation() != null && loc.getLocation().length == 2) {
                            double[] latLong = loc.getLocation();
                            double lat = rows.getDouble("latitude");
                            double lon = rows.getDouble("longitude");
                            double acc = rows.getDouble("accuracy");
                            ew.insertRow(
                                    rows.getString("id"),
                                    lat,
                                    lon,
                                    acc,
                                    latLong[1],
                                    latLong[0],
                                    distance(lat, lon, latLong[1], latLong[0]),
                                    loc.getLine1(),
                                    rows.getInt("vehicleId"),
                                    rows.getString("updatedAt")
                            );
                        } else if (loc != null) {
                            // There's no google location
                            ew.insertRowWithoutGoogleLocation(
                                    rows.getString("id"),
                                    rows.getDouble("latitude"),
                                    rows.getDouble("longitude"),
                                    rows.getFloat("accuracy"),
                                    loc.getLine1(),
                                    rows.getInt("vehicleId"),
                                    rows.getString("updatedAt")
                            );
                        } else {
                            // Address service gave null response
                            ew.insertRowWithoutGoogleLocation(
                                    rows.getString("id"),
                                    rows.getDouble("latitude"),
                                    rows.getDouble("longitude"),
                                    rows.getFloat("accuracy"),
                                    "",
                                    rows.getInt("vehicleId"),
                                    rows.getString("updatedAt")
                            );
                        }
                    } else {
                        // There is no destination address ID
                        ew.insertRowWithoutGoogleLocation(
                                rows.getString("id"),
                                rows.getDouble("latitude"),
                                rows.getDouble("longitude"),
                                rows.getFloat("accuracy"),
                                "",
                                rows.getInt("vehicleId"),
                                rows.getString("updatedAt")
                        );
                    }
                } catch (Exception e) {
                    // Error in one row will not affect processing of other rows
                    e.printStackTrace();
                }
            }

            ew.saveFile();
        } catch (FileNotFoundException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            System.out.println("Make sure that /home/ubuntu/kanj/lce.cfg has the following contents-");
            System.out.println("jdbc:mysql://<MySQL host>:<PORT>/flo_delivery");
            System.out.println("<sql user name>");
            System.out.println("<sql password>");
            System.out.println("<address service host eg. http://52.4.23.126:27001>");
        }
    }

    private static double distance (double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 111.189577;
        return dist;
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts decimal degrees to radians			:*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts radians to decimal degrees			:*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}
