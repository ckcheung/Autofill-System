package Autofill;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONException;

public class Controller extends HttpServlet {
    
    // upload settings
    private static final String DIRECTORY = "form";
    private static final int MEMORY_THRESHOLD   = 1024 * 1024 * 3;  // 3MB
    private static final int MAX_FILE_SIZE      = 1024 * 1024 * 40; // 40MB
    private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 50; // 50MB
    private static final String DBURL = "jdbc:mysql://127.0.0.1:3307/Autofill";
    private static final String DBUsername = "root";
    private static final String DBPassword = "admin";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        String addr;
        HttpSession session = request.getSession(true);
        //ps -ef | grep mysql
        
        try {
            //con = DriverManager.getConnection(DBURL, DBUsername, DBPassword);
            String page = request.getParameter("page");
            if (page == null) {
                addr = "home.jsp";
            } else {
                switch (page) {
                    case "register":
                        if (register(request)) {
                            addr = "home.jsp";
                        } else {
                            addr = "register.jsp";
                        }
                        break;
                    case "login":
                        if (login(request)) {
                            addr = "home.jsp";
                        } else {
                            addr = "home.jsp";
                        }
                        break;
                    case "logout":
                        session.setAttribute("user", null);
                        addr = "home.jsp";
                        break;
                    case "record":
                        addr = "record.jsp";
                        break;
                    case "save":
                        String data = (String)session.getAttribute("data");
                        System.out.println(data);
                        addr = "record.jsp";
                        break;
                    case "fill":
                        session.setAttribute("formList", FormManager.getInstance().getFormList());
                        addr = "fill.jsp";
                        break;
                    case "process":
                        toProcessPage(request);
                        addr = "process.jsp";
                        break;
                    case "result":
                        toResultPage(request);
                        addr = "result.jsp";
                        break;
                    case "manage":
                        toManagePage(request);
                        addr = "manage.jsp";
                        break;
                    case "statistics":
                        session.setAttribute("statistics", Dictionary.getInstance().getHistory());
                        addr = "statistics.jsp";
                        break;
                    default:
                        addr = "home.jsp";
                        break;
                }            
            }
        } catch (Exception e) {
            addr = "error.jsp";
        }
        
        RequestDispatcher dispatcher = request.getRequestDispatcher(addr);
        dispatcher.forward(request, response); 
        
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    
    // User registration
    // Return true if success, otherwise return false
    private boolean register(HttpServletRequest request) throws SQLException {
        /*
        if (request.getParameter("username") != null) {
            pstmt = con.prepareStatement(
                "INSERT INTO User (username, password, role, data, fieldGroup) VALUES (?, ?, ?, ?, ?)"
            );
            pstmt.setString(1, request.getParameter("username"));
            pstmt.setString(2, request.getParameter("password"));
            pstmt.setString(3, "user");
            pstmt.setString(4, "[]");
            pstmt.setString(5, "[]");
            pstmt.executeUpdate();
            
            return true;
        } else {
            return false;
        }
        */
        AccountManager authentication = AccountManager.getInstance();
        String username = (String)request.getParameter("username");
        String password = (String)request.getParameter("password");
        boolean successful = authentication.register(username, password);
        return successful;
    }
    
    // User login
    // Return true if success, otherwise return false
    private boolean login(HttpServletRequest request) throws SQLException {
        /*
        if (request.getParameter("username") != null) {
            pstmt = con.prepareStatement(
                "SELECT role, data, fieldGroup FROM User WHERE username = ? AND password = ?"
            );
            pstmt.setString(1, request.getParameter("username"));
            pstmt.setString(2, request.getParameter("password"));
            rs = pstmt.executeQuery();

            // If success login, store account information to the session object
            if (rs.next()) {
                User user = new User();
                user.setUsername(request.getParameter("username"));
                user.setRole(rs.getString("role"));
                user.setData(rs.getString("data"));
                user.setGroup(rs.getString("fieldGroup"));
                request.getSession().setAttribute("user", user);
            }
            
        }
        
        if (request.getSession().getAttribute("user") != null) {
            return true;
        } else {
            return false;
        }
        */
        
        AccountManager authentication = AccountManager.getInstance();
        String username = (String)request.getParameter("username");
        String password = (String)request.getParameter("password");
        User user = authentication.authenticate(username, password);
        if (user != null) {
            request.getSession().setAttribute("user", user);
            return true;
        } else {
            return false;
        }

    }
    
    // Return the name of the uplaoded file if success, otherwise return false
    private String fileUpload(HttpServletRequest request) throws IOException, FileUploadException, Exception {
        // checks if the request actually contains upload file
        if (ServletFileUpload.isMultipartContent(request)) {
            // configures upload settings
            DiskFileItemFactory factory = new DiskFileItemFactory();
            // sets memory threshold - beyond which files are stored in disk
            factory.setSizeThreshold(MEMORY_THRESHOLD);
            // sets temporary location to store files
            factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

            ServletFileUpload upload = new ServletFileUpload(factory);

            // sets maximum size of upload file
            upload.setFileSizeMax(MAX_FILE_SIZE);

            // sets maximum size of request (include file + form data)
            upload.setSizeMax(MAX_REQUEST_SIZE);

            // creates the directory if it does not exist
            File uploadDir = new File(getServletContext().getRealPath(DIRECTORY));
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }

            // parses the request's content to extract file data
            //@SuppressWarnings("unchecked")
            List<FileItem> formItems = upload.parseRequest(request);

            String fileName = null;
            String filepath = null;
            if (formItems != null && formItems.size() > 0) {
                for (FileItem item : formItems) {
                    // processes only fields that are not form fields
                    if (!item.isFormField()) {
                        fileName = new File(item.getName()).getName();
                        filepath = getServletContext().getRealPath(DIRECTORY + File.separator + fileName);
                        File storeFile = new File(filepath);
                        item.write(storeFile);
                    }
                }
                FormManager formManager = FormManager.getInstance();
                formManager.addForm(fileName);
                return fileName;
            }


            /*
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                con = DriverManager.getConnection(DBURL, DBUsername, DBPassword);
                //Store file information to database
                pstmt = con.prepareStatement(
                    "INSERT INTO Form VALUES (?, ?)"
                );
                pstmt.setString(1, fileName);
                pstmt.setString(2, fileName.substring(0, fileName.lastIndexOf('.')));
                pstmt.executeUpdate();
            } catch (Exception e) {
                } finally {
                try {
                    if (con != null) {con.close();}
                    if (pstmt != null) {con.close();}
                    if (rs != null) {con.close();}
                } catch (Exception e) {

                }
            }

            return fileName;
            */
        }
        return null;
    }
    
    /*
    private ArrayList<PDFForm> createFormList() {
        ArrayList<PDFForm> formList = new ArrayList();
        
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            con = DriverManager.getConnection(DBURL, DBUsername, DBPassword);
            pstmt = con.prepareStatement(
                "SELECT * FROM Form ORDER BY name"
            );
            rs = pstmt.executeQuery();
            while (rs.next()) {
                PDFForm form = new PDFForm();
                form.setName(rs.getString("name"));
                form.setDescription(rs.getString("description"));
                formList.add(form);
            }
        } catch (Exception e) {
            
        } finally {
            try {
                if (con != null) {con.close();}
                if (pstmt != null) {con.close();}
                if (rs != null) {con.close();}
            } catch (Exception e) {
                
            }
        }
        
        return formList;
    }
    */
    private void toProcessPage(HttpServletRequest request) throws IOException, FileUploadException, DocumentException, JSONException, Exception {
        String filepath = request.getParameter("formPath");
        System.out.println(filepath);
        if (filepath == null) {
            filepath = fileUpload(request);
        } 
        User user = ((User)request.getSession().getAttribute("user"));
        FormFiller filler = new FormFiller(getServletContext().getRealPath(DIRECTORY + File.separator + filepath));
        /*
        ArrayList<AcroFormField> fields = filler.fillPdf(
            getServletContext().getRealPath(DIRECTORY + File.separator + "temp.pdf"), 
            user.getData(),
            con,
            pstmt,
            rs
        );
        */
        ArrayList<AcroFormField> fields = filler.fillPdf(
            getServletContext().getRealPath(DIRECTORY + File.separator + "temp.pdf"), 
            user.getData()
        );
        request.getSession().setAttribute("fields", fields);

    }
    
    private void toResultPage(HttpServletRequest request) throws IOException, DocumentException, SQLException {
        Dictionary dictionary = Dictionary.getInstance();
        HttpSession session = request.getSession();
        ArrayList<AcroFormField> fields = (ArrayList<AcroFormField>)session.getAttribute("fields");

        PdfReader reader = new PdfReader(getServletContext().getRealPath(DIRECTORY + File.separator + "temp.pdf")); 
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(getServletContext().getRealPath(DIRECTORY + File.separator + "result.pdf")));
        AcroFields form = stamper.getAcroFields();

        for (AcroFormField field : fields) {
            if (field.getFieldLabel() != null && field.getFieldLabel().length() != 0) {
                if (!request.getParameter(field.getFieldName() + "_option").equals("")) {
                    if (form.getField(field.getFieldName()).equals("")) {
                        // Add synonym to dictionary
                        /*
                        String personalFieldName = request.getParameter(field.getFieldName() + "_personalFieldName");
                        pstmt = con.prepareStatement(
                            "INSERT INTO Dictionary VALUES (?, ?, ?, ?)"
                        );
                        pstmt.setString(1, personalFieldName);
                        pstmt.setString(2, field.getFieldName().toLowerCase());
                        pstmt.setFloat(3, 0.5f);
                        pstmt.setString(4, "[{\"word1\": \"" + personalFieldName + "\", \"word2\": \"" + field.getFieldName().toLowerCase() + "\", \"probability\": \"0.5\"");
                        pstmt.executeUpdate();
                        */
                        String personalFieldName = request.getParameter(field.getFieldName() + "_personalFieldName");
                        dictionary.addSynonym(personalFieldName, field.getFieldName().toLowerCase());
                    } else if (!form.getField(field.getFieldName()).equals(request.getParameter(field.getFieldName()))) {
                        // Reduce synonym's probability in dictionary
                        /*
                        pstmt = con.prepareStatement(
                            "SELECT probability, history FROM Dictionary WHERE standard = ? AND synonym = ?"
                        );
                        pstmt.setString(1, field.getPersonalFieldName());
                        pstmt.setString(2, field.getFieldName().toLowerCase());
                        System.out.println("TEST " + pstmt);
                        rs = pstmt.executeQuery();
                        if (rs.next()) {
                            Float probability = rs.getFloat("probability");
                            String history = rs.getString("history");
                            probability = probability - 0.1f;
                            if (probability < 0) {
                                // Remove entry
                                pstmt = con.prepareStatement(
                                    "DELETE FROM Dictionary WHERE standard = ? AND synonym = ?"
                                );
                                pstmt.setString(1, field.getPersonalFieldName());
                                pstmt.setString(2, field.getFieldName().toLowerCase());
                                pstmt.executeUpdate();
                            } else {
                                // Update probability
                                String newHistory = addHistory(history, formatHistoryRecord(field.getPersonalFieldName(), field.getFieldName().toLowerCase(), probability));
                                pstmt = con.prepareStatement(
                                    "UPDATE Dictionary SET probability = ?, history = ? WHERE standard = ? AND synonym = ?"
                                );
                                pstmt.setFloat(1, probability);
                                pstmt.setString(2, newHistory);
                                pstmt.setString(3, field.getPersonalFieldName());
                                pstmt.setString(4, field.getFieldName().toLowerCase());
                                pstmt.executeUpdate();                                        
                                //System.out.println("History : " + addHistory(history, formatHistoryRecord(field.getPersonalFieldName(), field.getFieldName().toLowerCase(), probability)));
                            }
                            System.out.println("TEST2 " + pstmt);
                        } else {
                            // Synonym to synonym matching
                            String personalFieldName = request.getParameter(field.getFieldName() + "_personalFieldName");
                            pstmt = con.prepareStatement(
                                "SELECT D1.standard As standard, D1.probability As probability, D1.history As history FROM Dictionary D1, Dictionary D2 WHERE D1.standard = D2.standard AND D1.synonym = ? AND D2.synonym = ?"
                            );
                            pstmt.setString(1, field.getFieldName().toLowerCase());
                            pstmt.setString(2, field.getPersonalFieldName());
                            System.out.println("TEST7 - " + pstmt);
                            rs = pstmt.executeQuery();
                            String standard;
                            String history;
                            Float probability;
                            if (rs.next()) {
                                standard = rs.getString("standard");
                                history = rs.getString("history");
                                probability = rs.getFloat("probability");
                                probability = (float)Math.round((probability - 0.1f)*10)/10;
                            } else {
                                continue;
                            }

                            if (probability == 0) {
                                //remove
                            } else {
                                String newHistory = addHistory(history, formatHistoryRecord(field.getPersonalFieldName(), field.getFieldName().toLowerCase(), probability));
                                pstmt = con.prepareStatement(
                                    "UPDATE Dictionary SET probability = ROUND(LEAST(1, probability - 0.1), 1), history = ? WHERE standard = ? AND (synonym = ? OR synonym = ?)"
                                ); 
                                pstmt.setString(1, newHistory);
                                pstmt.setString(2, standard);
                                pstmt.setString(3, field.getFieldName().toLowerCase());
                                pstmt.setString(4, field.getPersonalFieldName());
                                System.out.println("TEST6 - " + pstmt);
                                pstmt.executeUpdate();
                                //System.out.println("History : " + addHistory(history, formatHistoryRecord(field.getPersonalFieldName(), field.getFieldName().toLowerCase(), probability)));
                            }
                        }
                        */
                        dictionary.reduceProbability(field.getPersonalFieldName(), field.getFieldName().toLowerCase());
                    }

                    if (!form.getField(field.getFieldName()).equals(request.getParameter(field.getFieldName()))) {
                        // Refill that field if it is changed by user
                        form.setField(
                            field.getFieldName(), 
                            request.getParameter(field.getFieldName())
                        );
                    }
                }

                // Matching correct or user perform matching
                if ((request.getParameter(field.getFieldName() + "_option").equals("") && !request.getParameter(field.getFieldName()).equals("")) ||
                    (!request.getParameter(field.getFieldName() + "_option").equals("") && !form.getField(field.getFieldName()).equals("") && !form.getField(field.getFieldName()).equals(request.getParameter(field.getFieldName())))) {
                    // Increase probability
                    String personalFieldName = request.getParameter(field.getFieldName() + "_personalFieldName");
                    
                    /*
                    // Standard to synonym matching
                    pstmt = con.prepareStatement(
                        "SELECT probability, history FROM Dictionary WHERE standard = ? AND synonym = ?"
                    );                      
                    pstmt.setString(1, personalFieldName);
                    pstmt.setString(2, field.getFieldName().toLowerCase());
                    System.out.println("TEST3 - " + pstmt);
                    rs = pstmt.executeQuery();
                    if (rs.next()) {
                        float probability = rs.getFloat("probability");
                        String history = rs.getString("history");
                        probability = Math.min(1, Math.round((probability + 0.1f)*10)/10);
                        String newHistory = addHistory(history, formatHistoryRecord(personalFieldName, field.getFieldName().toLowerCase(), probability));
                        pstmt = con.prepareStatement(
                            "UPDATE Dictionary SET probability = ?, history = ? WHERE standard = ? AND synonym = ?"    
                        );
                        pstmt.setFloat(1, probability);
                        pstmt.setString(2, newHistory);
                        pstmt.setString(3, personalFieldName);
                        pstmt.setString(4, field.getFieldName().toLowerCase());
                        pstmt.executeUpdate();
                        //System.out.println("History : " + addHistory(history, formatHistoryRecord(personalFieldName, field.getFieldName().toLowerCase(), probability)));
                        continue;
                    }
                        
                    // Synonym to standard matching
                    pstmt = con.prepareStatement(
                        "SELECT probability, history FROM Dictionary WHERE standard = ? AND synonym = ?"
                    ); 
                    pstmt.setString(1, field.getFieldName().toLowerCase());
                    pstmt.setString(2, personalFieldName);
                    System.out.println("TEST4 - " + pstmt);
                    rs = pstmt.executeQuery();
                    if (rs.next()) {
                        System.out.println("TEST4");
                        float probability = rs.getFloat("probability");
                        String history = rs.getString("history");
                        probability = Math.min(1, Math.round((probability + 0.1f)*10)/10);
                        String newHistory = addHistory(history, formatHistoryRecord(field.getPersonalFieldName(), field.getFieldName().toLowerCase(), probability));
                        pstmt = con.prepareStatement(
                            "UPDATE Dictionary SET probability = ? WHERE standard = ? AND synonym = ?"    
                        );
                        pstmt.setFloat(1, probability);
                        pstmt.setString(2, addHistory(history, newHistory));
                        pstmt.setString(3, field.getFieldName().toLowerCase());
                        pstmt.setString(4, personalFieldName);
                        pstmt.executeUpdate();
                        //System.out.println("History : " + addHistory(history, formatHistoryRecord(field.getPersonalFieldName(), field.getFieldName().toLowerCase(), probability)));
                        continue;
                    }
                    
                    // Synonym to synonym matching
                    pstmt = con.prepareStatement(
                        "SELECT D1.standard As standard, D1.probability As probability, D1.history As history FROM Dictionary D1, Dictionary D2 WHERE D1.standard = D2.standard AND D1.synonym = ? AND D2.synonym = ?"
                    );
                    pstmt.setString(1, field.getFieldName().toLowerCase());
                    pstmt.setString(2, personalFieldName);
                    rs = pstmt.executeQuery();
                    String standard;
                    String history;
                    Float probability;
                    if (rs.next()) {
                        standard = rs.getString("standard");
                        history = rs.getString("history");
                        probability = rs.getFloat("probability");
                        probability = Math.min(1, probability + 0.1f);
                    } else {
                        continue;
                    }String newHistory = addHistory(history, formatHistoryRecord(personalFieldName, field.getFieldName().toLowerCase(), probability));
                    pstmt = con.prepareStatement(
                        "UPDATE Dictionary SET probability = ROUND(LEAST(1, probability + 0.1), 1), history = ? WHERE standard = ? AND (synonym = ? OR synonym = ?)"
                    ); 
                    pstmt.setString(1, newHistory);
                    pstmt.setString(2, standard);
                    pstmt.setString(3, field.getFieldName().toLowerCase());
                    pstmt.setString(4, personalFieldName);
                    System.out.println("TEST5 - " + pstmt);
                    //System.out.println("History : " + addHistory(history, formatHistoryRecord(personalFieldName, field.getFieldName().toLowerCase(), probability)));
                    pstmt.executeUpdate();
                    
                    */
                    dictionary.addProbability(personalFieldName, field.getFieldName().toLowerCase());
                }
            }
        }
        stamper.close();
        reader.close();

    }
    
    /*
    private void getHistory(HttpServletRequest request) throws SQLException {
        ArrayList<Statistics> statistics = new ArrayList<>();
        pstmt = con.prepareStatement(
            "SELECT standard, synonym, history FROM Dictionary ORDER BY standard"
        );
        rs = pstmt.executeQuery();
        StringBuilder str = new StringBuilder();
        while (rs.next()) {
            Statistics s = new Statistics();
            s.setStandard(rs.getString("standard"));
            s.setSynonym(rs.getString("synonym"));
            if (rs.getString("history") != null) {
                s.setHistory(rs.getString("history"));
            } else {
                s.setHistory("[]");
            }
            statistics.add(s);
        }
        
        request.getSession().setAttribute("statistics", statistics);
    }
    */
    
    /*
    private String formatHistoryRecord(float probability) {
        StringBuilder str = new StringBuilder();
        str.append("{\"probability\": \"");
        str.append(probability);
        str.append("\"}");
        return new String(str);
    }
    */
    
    /*
    private String formatHistoryRecord(String word1, String word2, float probability) {
        StringBuilder str = new StringBuilder();
        str.append("{");
        str.append("\"word1\": ");
        str.append("\"");
        str.append(word1);
        str.append("\"");
        str.append(", ");
        str.append("\"word2\": ");
        str.append("\"");
        str.append(word2);
        str.append("\"");
        str.append(", ");
        str.append("\"probability\": ");
        str.append("\"");
        str.append(probability);
        str.append("\"");
        str.append("}");
        return new String(str);
    }
        
    private String addHistory(String oldHistory, String newHistory) {
        StringBuilder str;
        if (oldHistory.length() > 2) {
            // Have record
            str = new StringBuilder(oldHistory);
            str.replace(str.length()-1, str.length(), ", " + newHistory);
            str.append("]");
        } else {
            // No record
            str = new StringBuilder();
            str.append("[");
            str.append(newHistory);
            str.append("]");
        }
        return new String(str);
    }
    */
    
    private void toManagePage(HttpServletRequest request) throws SQLException {
        AccountManager accountManager = AccountManager.getInstance();
        FormManager formManager = FormManager.getInstance();
        
        String action = request.getParameter("action");
        if (action != null) {
            if (action.equals("deleteAccount")) {
                accountManager.deleteAccount(request.getParameter("userName"));
            } else if (action.equals("deleteForm")) {
                formManager.deleteForm(request.getParameter("formName"));
            }
        }
        
        request.getSession().setAttribute("userList", accountManager.getUserList());
        request.getSession().setAttribute("formList", formManager.getFormList());
    }
}
