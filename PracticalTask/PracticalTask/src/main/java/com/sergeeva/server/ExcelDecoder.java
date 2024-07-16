package com.sergeeva.server;

import com.netconomy.encryption.suite.Encryptor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ExcelDecoder implements FileTypeDecoder {

    @Override
    public byte[] decodeFile(byte[] encodedContent, String key) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(encodedContent);  // Create an InputStream out of the decoded byte array
            Workbook workbook = new XSSFWorkbook(inputStream); // Feed the InputStream into XSSFWorkbook constructor

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                for (Cell cell : row) {
                    String encryptedText = cell.getStringCellValue();
                    String decryptedText = Encryptor.decrypt(key, new String(encryptedText)); // Decrypt the content first
                    cell.setCellValue(decryptedText);
                }
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            inputStream.close();

            return bos.toByteArray();

        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return encodedContent;
    }
}
