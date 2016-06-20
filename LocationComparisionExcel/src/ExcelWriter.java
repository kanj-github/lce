import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kanj on 15/6/16.
 */
public class ExcelWriter {
    private XSSFWorkbook wb;
    private XSSFSheet sh;
    private int rowCount;
    private String fileName;
    private SimpleDateFormat sdf;

    public ExcelWriter() {
        // Make a file
        sdf = new SimpleDateFormat("dd-MM-yyyy");
        fileName = "/home/ubuntu/kanj/garbage" + sdf.format(new Date()) + ".xlsx";
        //fileName = "/home/kanj/Desktop/garbage" + sdf.format(new Date()) + ".xlsx"; // Local PC

        // I'll use this to format timestamp in each row
        sdf = new SimpleDateFormat("HH:mm:ss dd-MM");

        wb = new XSSFWorkbook();
        sh = wb.createSheet();
        Row r = sh.createRow(0);
        int cellCount = 0;
        r.createCell(cellCount++).setCellValue("id");
        r.createCell(cellCount++).setCellValue("Vehicle Id");
        r.createCell(cellCount++).setCellValue("Updated At");
        r.createCell(cellCount++).setCellValue("latitude");
        r.createCell(cellCount++).setCellValue("longitude");
        r.createCell(cellCount++).setCellValue("accuracy");
        r.createCell(cellCount++).setCellValue("addressLine1");
        r.createCell(cellCount++).setCellValue("google lat");
        r.createCell(cellCount++).setCellValue("google long");
        r.createCell(cellCount).setCellValue("difference");
        rowCount = 1;
    }

    public void insertRow(String id, double latitude, double longitude, double accuracy, double googleLat,
                          double googleLong, double diff, String line1, int vehId, String updatedAt) {
        Row r = sh.createRow(rowCount++);

        int cellCount = 0;
        r.createCell(cellCount++).setCellValue(id);
        r.createCell(cellCount++).setCellValue(vehId);
        r.createCell(cellCount++).setCellValue(updatedAt);
        r.createCell(cellCount++).setCellValue(latitude);
        r.createCell(cellCount++).setCellValue(longitude);
        r.createCell(cellCount++).setCellValue(accuracy);
        r.createCell(cellCount++).setCellValue(line1);
        r.createCell(cellCount++).setCellValue(googleLat);
        r.createCell(cellCount++).setCellValue(googleLong);
        r.createCell(cellCount).setCellValue(diff);
    }

    public void insertRowWithoutGoogleLocation(String id, double latitude, double longitude, float accuracy,
                                               String line1, int vehId, String updatedAt) {
        Row r = sh.createRow(rowCount++);

        int cellCount = 0;
        r.createCell(cellCount++).setCellValue(id);
        r.createCell(cellCount++).setCellValue(vehId);
        r.createCell(cellCount++).setCellValue(updatedAt);
        r.createCell(cellCount++).setCellValue(latitude);
        r.createCell(cellCount++).setCellValue(longitude);
        r.createCell(cellCount++).setCellValue(accuracy);
        r.createCell(cellCount).setCellValue(line1);
    }

    public void saveFile() {
        try {
            FileOutputStream out = new FileOutputStream(new File(fileName));
            wb.write(out);
            out.close();
            System.out.println("File saved successfully..");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
