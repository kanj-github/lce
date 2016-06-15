import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * Created by kanj on 15/6/16.
 */
public class StartWork {
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    //private static final String ADDRESS_SERVICE_HOST = "http://flowaddressservice-prod.us-east-1.elasticbeanstalk.com";
    private static final String ADDRESS_SERVICE_HOST = "http://52.4.23.126:27001"; // Local PC
    private static final String ADDRESS_SERVICE_PATH = "v1/addresses/";

    private static final String CONFIG_FILE = "/home/ubuntu/kanj/lce.cfg";
    //private static final String CONFIG_FILE = "/home/kanj/Desktop/lce.cfg"; // Local PC
    private static final String QUERY = "SELECT clusterItemDeliveryDetails.id, latitude, longitude, accuracy, clusterItems.destinationAddressId " +
            "FROM clusterItemDeliveryDetails, clusterItems " +
            "WHERE clusterItems.id = clusterItemDeliveryDetails.clusterItemId " +
            "ORDER BY clusterItems.clusterId, clusterItemDeliveryDetails.updatedAt;";

    public static void main(String args[]) {
        try {
            Scanner scanner = new Scanner(new FileInputStream(CONFIG_FILE)).useDelimiter("\n");
            String dbUrl = scanner.next();
            String dbUser = scanner.next();
            String dbPassword = scanner.next();
            scanner.close();

            Class.forName(JDBC_DRIVER);
            Connection sqlConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            Statement statement = sqlConnection.createStatement();
            ResultSet rows = statement.executeQuery(QUERY);

            if (rows == null) {
                return;
            }

            ExcelWriter ew = new ExcelWriter();
            Gson gsonParser = new Gson();
            JerseyClient client = JerseyClientBuilder.createClient();
            WebTarget target = client.target(ADDRESS_SERVICE_HOST);
            while (rows.next()) {
                try {
                    String destAddressId = rows.getString("destinationAddressId");
                    if (destAddressId != null) {
                        target.path(ADDRESS_SERVICE_PATH + destAddressId);
                        Invocation.Builder builder = target.request();

                        Response response = builder.get();

                        String jsonString = response.readEntity(String.class);
                        System.out.println("response= "+jsonString);
                        GoogleLocation loc = gsonParser.fromJson(jsonString, GoogleLocation.class);

                        if (loc != null && loc.getLocation() != null) {
                            double[] latLong = loc.getLocation();
                            double lat = rows.getDouble("latitude");
                            double lon = rows.getDouble("longitude");
                            ew.insertRow(
                                    rows.getString("id"),
                                    lat,
                                    lon,
                                    rows.getFloat("accuracy"),
                                    latLong[1],
                                    latLong[0],
                                    distance(lat, lon, latLong[1], latLong[0])
                            );
                        } else {
                            // There's no google location
                            ew.insertRowWithoutGoogleLocation(
                                    rows.getString("id"),
                                    rows.getDouble("latitude"),
                                    rows.getDouble("longitude"),
                                    rows.getFloat("accuracy")
                            );
                        }
                    } else {
                        // There is no destination address ID
                        ew.insertRowWithoutGoogleLocation(
                                rows.getString("id"),
                                rows.getDouble("latitude"),
                                rows.getDouble("longitude"),
                                rows.getFloat("accuracy")
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
        }
        /*Gson gsonParser = new Gson();
        JerseyClient client = JerseyClientBuilder.createClient();
        WebTarget target = client.target(ADDRESS_SERVICE_HOST).path(ADDRESS_SERVICE_PATH + "5759a493605b745d7ee345e6");
        Invocation.Builder builder = target.request();

        Response response = builder.get();

        String jsonString = response.readEntity(String.class);
        System.out.println("response= "+jsonString);
        GoogleLocation loc = gsonParser.fromJson(jsonString, GoogleLocation.class);
        if (loc != null) {
            double[] latLong = loc.getLocation();
            if (latLong != null && latLong.length == 2) {
                System.out.println("Latitude= "+latLong[1]);
                System.out.println("Longitude= "+latLong[0]);
            }
        }*/
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
