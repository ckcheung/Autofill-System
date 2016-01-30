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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FormFiller {

    private ArrayList<AcroFormField> fieldList;
    private String sourceFilePath;
    private HashMap groupMap;
    
    public FormFiller(String filepath) {
        fieldList = new ArrayList<>();
        sourceFilePath = filepath;
        groupMap = new HashMap();
    }
    
//    public ArrayList<AcroFormField> fillPdf(String dest, String data, Connection con, PreparedStatement pstmt, ResultSet rs) throws IOException, DocumentException, JSONException, SQLException {
    public ArrayList<AcroFormField> fillPdf(String dest, String data) throws IOException, DocumentException, JSONException, SQLException {
        Dictionary dictionary = Dictionary.getInstance();
        PdfReader reader = new PdfReader(sourceFilePath); 
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        AcroFields form = stamper.getAcroFields();        
        ArrayList<String> fields = new ArrayList<>(form.getFields().keySet());
        Collections.sort(fields);
        
        AcroFormField temp;
        
        HashMap objectMap = new HashMap();
        HashMap fieldMap = new HashMap();
        
        for (String key : fields) {
            temp = new AcroFormField();
            temp.setFieldName(key);
            temp.setPage(form.getFieldPositions(key).get(0).page);
            temp.setPosition(form.getFieldPositions(key).get(0).position);
            fieldList.add(temp);
            objectMap.put(key, temp);
            //fieldMap.put(key.toLowerCase(), key);
            ArrayList<String> tempKeys;
            if (fieldMap.get(key.toLowerCase()) != null) {
                tempKeys = (ArrayList<String>)fieldMap.get(key.toLowerCase());
                tempKeys.add(key);
            } else {
                tempKeys = new ArrayList<>();
                tempKeys.add(key);
                fieldMap.put(key.toLowerCase(), tempKeys);
            }

            //System.out.println(key);
            //ArrayList<String> samePrefixWords = getSamePrefixWords(key, con);
            ArrayList<String> samePrefixWords = dictionary.getSamePrefixWords(key);
            for (String samePrefixWord: samePrefixWords) {
                if (fieldMap.get(samePrefixWord) != null) {
                    tempKeys = (ArrayList<String>)fieldMap.get(samePrefixWord);
                    tempKeys.add(key);
                } else {
                    tempKeys = new ArrayList<>();
                    tempKeys.add(key);
                    fieldMap.put(samePrefixWord, tempKeys);
                }
            }
        }

        // Get field label and group
        RenderFilter filter;
        TextExtractionStrategy strategy;
        Rectangle targetArea;
        Collections.sort(fieldList);
        AcroFormField currentField = null;
        AcroFormField previousField = null;
        String group = null;
        String preGroup = null;
        int groupCount = 1;
        for (int i=0; i<fieldList.size(); i++) {
            currentField = fieldList.get(i);
            if (form.getFieldType(currentField.getFieldName()) == AcroFields.FIELD_TYPE_TEXT) {
                if (i>0 && isSameLinePreviousField(currentField.getPosition(), previousField.getPosition())) {
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
            }   

            if (i>0 && (isNewSection(currentField.getPosition(), previousField.getPosition()) || currentField.getPage() != previousField.getPage())) {
                targetArea = new Rectangle(
                    0,                                          // lower left x
                    currentField.getPosition().getTop(),        // Lower left y
                    currentField.getPosition().getRight(),      // Upper right x
                    currentField.getPosition().getTop()+currentField.getPosition().getHeight()      // Upper right y
                );
                filter = new RegionTextRenderFilter(targetArea);
                strategy = new FilteredTextRenderListener(new LocationTextExtractionStrategy(), filter);
                String tempGroup = PdfTextExtractor.getTextFromPage(reader, currentField.getPage(), strategy);
                
                if (!tempGroup.equals("")) {
                    group = tempGroup.split("\\(")[0].trim();
                    if (group.split(" ").length > 4) {
                        group = "Other";
                        preGroup = group;
                    } else if (!group.equals(preGroup)) {
                        // New group name, reset counter
                        groupCount = 1;
                        preGroup = group;
                    } else {
                        // Group name same as previous group, adjust group name and increase counter
                        groupCount++;
                        group = group + " " + groupCount;
                    }
                }
            }
            currentField.setGroup(group);
            previousField = fieldList.get(i);
        }
        
        
        JSONArray dataArray = new JSONArray(data);
        for (int i=0; i<dataArray.length(); i++) {
            boolean matchFound = false;
            JSONObject dataObject = dataArray.getJSONObject(i);
            System.out.println(dataArray.get(i));
            
            if (fieldMap.get(dataObject.getString("name")) != null) {
                // Exact match
                ArrayList<String> keyList = (ArrayList<String>)fieldMap.get(dataObject.getString("name"));
                for (String key : keyList) {
                    AcroFormField field = (AcroFormField)objectMap.get(key);
                    // Fill and process next if group match
                    if (isGroupMatch(field, dataObject.getString("group"))) {
                        form.setField(key, dataObject.getString("value"));
                        field.setPersonalFieldName(dataObject.getString("name"));
                        matchFound = true;
                        break;
                    }   
                }
            }
            
            if (fieldMap.get(dataObject.getString("name")) == null && !matchFound) {    // Search dictionary
                // Get standard field name for synonym
                //String standard = getStandardWord(dataObject.getString("name"), con);
                String standard = dictionary.getStandardWord(dataObject.getString("name"));
                if (fieldMap.get(standard) != null) {
                    // standard is the key
                    ArrayList<String> keyList = (ArrayList<String>)fieldMap.get(standard);
                    for (String key : keyList) {
                        AcroFormField field = (AcroFormField)objectMap.get(key);
                        // Fill and process next if group match
                        if (isGroupMatch(field, dataObject.getString("group"))) {
                            form.setField(key, dataObject.getString("value"));
                            field.setPersonalFieldName(dataObject.getString("name"));
                            break;
                        } 
                    }
                } else {
                    // standard is not the key
                    ArrayList<String> synonyms = dictionary.getSynonyms(dataObject.getString("name"));
                    for (String fieldName: synonyms) {
                        if (fieldMap.get(fieldName) != null) {
                            ArrayList<String> keyList = (ArrayList<String>)fieldMap.get(fieldName);
                            for (String key : keyList) {
                                AcroFormField field = (AcroFormField)objectMap.get(key);
                                // Fill and process next if group match
                                //System.out.println(field.getFieldLabel() + " " + field.getGroup());
                                if (isGroupMatch(field, dataObject.getString("group"))) {
                                    form.setField(key, dataObject.getString("value"));
                                    field.setPersonalFieldName(dataObject.getString("name"));
                                    //System.out.println(fieldName);
                                    matchFound = true;
                                    break;
                                }
                            }
                            // Process next if filled
                            if (matchFound) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        for (int i=0; i<fieldList.size(); i++) {
            currentField = fieldList.get(i);
            if (form.getFieldType(currentField.getFieldName()) == AcroFields.FIELD_TYPE_TEXT) {
                currentField.setFieldValue(form.getField(currentField.getFieldName()));
            }   
        }
        
        stamper.close();
        reader.close();

        return fieldList;
    }
    
    
//    private boolean isGroupMatch(Connection con, AcroFormField field, String group) throws SQLException {
    private boolean isGroupMatch(AcroFormField field, String group) throws SQLException {
        Dictionary dictionary = Dictionary.getInstance();
        String fieldGroup;
        
        if (field.getGroup() == null) {
            fieldGroup = "personal information";   
        } else {
            fieldGroup = field.getGroup().toLowerCase();
            fieldGroup = decodeGroupName(fieldGroup);
        }
        
        String dataGroup = group.toLowerCase();
        dataGroup = decodeGroupName(dataGroup);
        
        //ArrayList<String> groupSynonyms = getSynonyms(fieldGroup, con);
        ArrayList<String> groupSynonyms = dictionary.getSynonyms(fieldGroup);

        // Default group is Perosnal Information
        if (dataGroup.equals("")) {
            dataGroup = "personal information";
        }
        
        if (groupMap.get(group) != null) {
            // Group matching already exist, decide according to the record
            String matchingGroup = (String)groupMap.get(group);
            if (matchingGroup.equals(field.getGroup())) {
                return true;
            }
        } else if (fieldGroup.equals(dataGroup)) {  // Group matching not existing, try to create new matching
            // Exact match of Group name 
            groupMap.put(group, field.getGroup());
            return true;
        } else {
            for (String groupSynonym : groupSynonyms) {
                if (fieldGroup.equals(groupSynonym)) {
                    // Match of synonym of the group name
                    groupMap.put(group, field.getGroup());
                    return true;
                }
            }
        }

        return false;
    }
    
    private boolean isSameLinePreviousField(Rectangle curFieldPosition, Rectangle preFieldPosition) {
        return isOnRight(curFieldPosition, preFieldPosition) && isOnSameLine(curFieldPosition, preFieldPosition);
    }
    
    private boolean isOnRight(Rectangle curFieldPosition, Rectangle preFieldPosition) {
        return curFieldPosition.getLeft() > preFieldPosition.getRight();
    }
    
    private boolean isOnSameLine(Rectangle curFieldPosition, Rectangle preFieldPosition) {
        double dy = curFieldPosition.getTop()-preFieldPosition.getTop();
        return  dy < curFieldPosition.getHeight();
    }
    
    private boolean isNewSection(Rectangle curFieldPosition, Rectangle preFieldPosition) {
        return !isOnRight(curFieldPosition, preFieldPosition) && isSeperated(curFieldPosition, preFieldPosition);         
    }
    
    private boolean isSeperated(Rectangle curFieldPosition, Rectangle preFieldPosition) {
        double dy = preFieldPosition.getBottom()-curFieldPosition.getTop();
        return dy > curFieldPosition.getHeight();
    }
    
    private String decodeGroupName(String originGroupName) {
        String[] tempGroupName = originGroupName.split(" ");
        try {
            Integer.parseInt(tempGroupName[tempGroupName.length-1]);
            String decodedName = "";
            for (int i=0; i<tempGroupName.length-2; i++) {
                decodedName = decodedName + tempGroupName[i];
            }
            return decodedName;
        } catch (NumberFormatException e) {
            return originGroupName;
        }
    }
    /*
    private String getStandardWord(String word, Connection dictionaryCon) throws SQLException {
        String standard = "";
        PreparedStatement pstmt;
        ResultSet rs;
        
        pstmt = dictionaryCon.prepareStatement(
            "SELECT standard FROM Dictionary WHERE synonym = ? ORDER BY probability"
        );
        pstmt.setString(1, word);
        rs = pstmt.executeQuery();
        if (rs.next()) {
            standard = rs.getString("standard");
        }
        return standard;
    }
    
    private ArrayList<String> getSynonyms(String word, Connection dictionaryCon) throws SQLException {
        ArrayList<String> synonyms = new ArrayList<>();
        PreparedStatement pstmt;
        ResultSet rs;
        
        String standard = getStandardWord(word, dictionaryCon);
        pstmt = dictionaryCon.prepareStatement(
            "SELECT synonym FROM Dictionary WHERE standard = ? OR standard = ? ORDER BY probability"
        );
        pstmt.setString(1, word);
        pstmt.setString(2, standard); 
        rs = pstmt.executeQuery();
        while (rs.next()) {
            synonyms.add(rs.getString("synonym"));
        }
        
        return synonyms;
    }
    
    private ArrayList<String> getSamePrefixWords(String word, Connection dictionaryCon) throws SQLException {
        ArrayList<String> samePrefixWords = new ArrayList<>();
        PreparedStatement pstmt;
        ResultSet rs;
        
        // Create search key
        StringBuilder strBuilder = new StringBuilder(word);
        strBuilder.insert(word.length(), "%");
        for (int i=word.length()-1; i>0; i--) {
            if (Character.isUpperCase(word.charAt(i))) {
                strBuilder.insert(i, " %");
            }
        }
        String searchKey = strBuilder.toString().toLowerCase();

        pstmt = dictionaryCon.prepareStatement(
            "SELECT standard FROM Dictionary WHERE standard LIKE ?"
        );
        pstmt.setString(1, searchKey);
        rs = pstmt.executeQuery();
        while (rs.next()) {
            samePrefixWords.add(rs.getString("standard"));
        }

        pstmt = dictionaryCon.prepareStatement(
            "SELECT synonym FROM Dictionary WHERE synonym LIKE ?"
        );
        pstmt.setString(1, searchKey);
        rs = pstmt.executeQuery();
        while (rs.next()) {
            samePrefixWords.add(rs.getString("synonym"));
        }
        
        return samePrefixWords;
    }
    */
}