import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.Scanner;

/**
 * Created by kanj on 15/6/16.
 */
public class StartWork {
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
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
            while (rows.next()) {
                ew.insertRow(rows.getString("destinationAddressId"));
            }

            ew.saveFile();
        } catch (FileNotFoundException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}
