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

    public ExcelWriter() {
        // Make a file
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        fileName = "/home/ubuntu/kanj/garbage" + sdf.format(new Date()) + ".xlsx";
        //fileName = "/home/kanj/Desktop/garbage" + sdf.format(new Date()) + ".xlsx"; // Local PC

        wb = new XSSFWorkbook();
        sh = wb.createSheet();
        Row r = sh.createRow(0);
        int cellCount = 0;
        r.createCell(cellCount++).setCellValue("id");
        r.createCell(cellCount++).setCellValue("latitude");
        r.createCell(cellCount++).setCellValue("longitude");
        r.createCell(cellCount++).setCellValue("accuracy");
        r.createCell(cellCount++).setCellValue("google lat");
        r.createCell(cellCount++).setCellValue("google long");
        r.createCell(cellCount).setCellValue("difference");
        rowCount = 1;
    }

    public void insertRow(String id, double latitude, double longitude, double accuracy, double googleLat,
                          double googleLong, double diff) {
        Row r = sh.createRow(rowCount++);

        int cellCount = 0;
        r.createCell(cellCount++).setCellValue(id);
        r.createCell(cellCount++).setCellValue(latitude);
        r.createCell(cellCount++).setCellValue(longitude);
        r.createCell(cellCount++).setCellValue(accuracy);
        r.createCell(cellCount++).setCellValue(googleLat);
        r.createCell(cellCount++).setCellValue(googleLong);
        r.createCell(cellCount).setCellValue(diff);
    }

    public void insertRowWithoutGoogleLocation(String id, double latitude, double longitude, float accuracy) {
        Row r = sh.createRow(rowCount++);

        int cellCount = 0;
        r.createCell(cellCount++).setCellValue(id);
        r.createCell(cellCount++).setCellValue(latitude);
        r.createCell(cellCount++).setCellValue(longitude);
        r.createCell(cellCount++).setCellValue(accuracy);
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
