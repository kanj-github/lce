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
        rowCount = 0;
    }

    public void insertRow(String str) {
        Row r = sh.createRow(rowCount++);
        r.createCell(0).setCellValue(str);
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
