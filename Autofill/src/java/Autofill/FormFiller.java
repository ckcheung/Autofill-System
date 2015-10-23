package Autofill;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.FilteredTextRenderListener;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.RegionTextRenderFilter;
import com.itextpdf.text.pdf.parser.RenderFilter;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FormFiller {

    public static ArrayList<AcroFormField> fillPdf(String filepath, String dest, String data, Connection con, PreparedStatement pstmt, ResultSet rs) throws IOException, DocumentException, JSONException, SQLException {
        
        PdfReader reader = new PdfReader(filepath); 
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        AcroFields form = stamper.getAcroFields();
        Set<String> fields = form.getFields().keySet();
        
        //test
        ArrayList<AcroFormField> fieldList = new ArrayList();
        AcroFormField temp;
        //end test
        HashMap objectMap = new HashMap();
        
        HashMap fieldMap = new HashMap();
        for (String key : fields) {
            //test
            temp = new AcroFormField();
            temp.setFieldName(key);
            temp.setPage(form.getFieldPositions(key).get(0).page);
            temp.setPosition(form.getFieldPositions(key).get(0).position);
            fieldList.add(temp);
            objectMap.put(key, temp);
            //end test
            
            fieldMap.put(key.toLowerCase(), key);
            System.out.println(key.toLowerCase());
      
            // Prefix
            StringBuilder strBuilder = new StringBuilder(key);
            strBuilder.insert(key.length(), "%");
            for(int i=key.length()-1; i>0; i--) {
                if(Character.isUpperCase(key.charAt(i))) {
                    strBuilder.insert(i, " %");
                }
            }
            String searchKey = strBuilder.toString().toLowerCase();

            pstmt = con.prepareStatement(
                "SELECT standard FROM Dictionary WHERE standard LIKE ?"
            );
            pstmt.setString(1, searchKey);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                fieldMap.put(rs.getString("standard"), key);
            }

            pstmt = con.prepareStatement(
                "SELECT synonym FROM Dictionary WHERE synonym LIKE ?"
            );
            pstmt.setString(1, searchKey);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                fieldMap.put(rs.getString("synonym"), key);
            }   
        }

        JSONArray dataArray = new JSONArray(data);
        for (int i=0; i<dataArray.length(); i++) {
            JSONObject dataObject = dataArray.getJSONObject(i);
            System.out.println(dataArray.get(i));
            if (fieldMap.get(dataObject.getString("name")) != null) {
                form.setField((String)fieldMap.get(dataObject.getString("name")), dataObject.getString("value"));
                ((AcroFormField)objectMap.get((String)fieldMap.get(dataObject.getString("name")))).setPersonalFieldName(dataObject.getString("name"));
            } else {
                // Search dictionary

                // Get standard field name for synonym
                String standard = "";
                pstmt = con.prepareStatement(
                    "SELECT standard FROM Dictionary WHERE synonym = ? ORDER BY probability"
                );
                pstmt.setString(1, dataObject.getString("name"));
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    standard = rs.getString("standard");
                }

                if (fieldMap.get(standard) != null) {
                    // standard is the key
                    form.setField((String)fieldMap.get(standard), dataObject.getString("value"));
                    ((AcroFormField)objectMap.get((String)fieldMap.get(standard))).setPersonalFieldName(dataObject.getString("name"));
                } else {
                    // standard is not the key
                    pstmt = con.prepareStatement(
                        "SELECT synonym FROM Dictionary WHERE standard = ? OR standard = ? ORDER BY probability"
                    );
                    pstmt.setString(1, dataObject.getString("name"));
                    pstmt.setString(2, standard); 
                    rs = pstmt.executeQuery();
                    while (rs.next()) {
                        String fieldName = rs.getString(1);
                        if (fieldMap.get(fieldName) != null) {
                            form.setField((String)fieldMap.get(fieldName), dataObject.getString("value"));
                            ((AcroFormField)objectMap.get((String)fieldMap.get(fieldName))).setPersonalFieldName(dataObject.getString("name"));
                            System.out.println(fieldName);
                            break;
                        }
                    }
                }
            }
        }


        RenderFilter filter;
        TextExtractionStrategy strategy;
        Rectangle targetArea;
/*
        for (String key : fields) {
            if (form.getFieldType(key) == AcroFields.FIELD_TYPE_TEXT) {
                page = form.getFieldPositions(key).get(0).page;
                fieldArea = form.getFieldPositions(key).get(0).position;
                targetArea = new Rectangle(
                    fieldArea.getLeft() - fieldArea.getWidth()/2,  // lower left x
                    fieldArea.getBottom(),      // lower left y
                    fieldArea.getLeft(),        // upper right x
                    fieldArea.getTop()          // upper right y
                ); 
                filter = new RegionTextRenderFilter(targetArea);
                strategy = new FilteredTextRenderListener(new LocationTextExtractionStrategy(), filter);
                System.out.print(key + " ");
                System.out.println(PdfTextExtractor.getTextFromPage(reader, page, strategy));
            }
        }
        */
        
        Collections.sort(fieldList);
        AcroFormField currentField = null;
        AcroFormField previousField = null;
        for (int i=0; i<fieldList.size(); i++) {
            currentField = fieldList.get(i);
            if (form.getFieldType(currentField.getFieldName()) == AcroFields.FIELD_TYPE_TEXT) {
                if (i>0 && currentField.getPosition().getLeft() > previousField.getPosition().getRight()
                        && currentField.getPosition().getTop()-previousField.getPosition().getTop() < currentField.getPosition().getHeight()) {
                    targetArea = new Rectangle(
                        previousField.getPosition().getRight(), // Lower left x
                        currentField.getPosition().getBottom(), // Lower left y
                        currentField.getPosition().getLeft(),   // Upper right x
                        currentField.getPosition().getTop()     // Upper right y
                    );
                } else {
                    targetArea = new Rectangle(
                        0,                                      // lower left x
                        currentField.getPosition().getBottom(), // Lower left y
                        currentField.getPosition().getLeft(),   // Upper right x
                        currentField.getPosition().getTop()     // Upper right y
                    );                    
                }
                filter = new RegionTextRenderFilter(targetArea);
                strategy = new FilteredTextRenderListener(new LocationTextExtractionStrategy(), filter);
                currentField.setFieldValue(form.getField(currentField.getFieldName()));
                currentField.setFieldLabel(PdfTextExtractor.getTextFromPage(reader, currentField.getPage(), strategy));
                //System.out.print(currentField.getFieldName() + " ::: ");
                //System.out.println(PdfTextExtractor.getTextFromPage(reader, currentField.getPage(), strategy));
            }
            previousField = fieldList.get(i);
            //System.out.println(f.getPage() + " " + f.getFieldName() + " " + f.getPosition().getTop());
        }
        
        stamper.close();
        reader.close();
        
        return fieldList;
        /* Show all text
        reader = new PdfReader(filepath);
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        TextExtractionStrategy strategy1;
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            strategy1 = parser.processContent(i, new SimpleTextExtractionStrategy());
            System.out.println(strategy1.getResultantText());
        }
        reader.close();
        */
    }
}