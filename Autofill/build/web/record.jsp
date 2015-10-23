<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>
        <title>FYP</title>
        <script>
            var data = JSON.parse('${user.data}');  // Decode JSON
        </script>
        <script src='js/record.js'></script>
        <script src='http://code.jquery.com/jquery-latest.js'></script>
        <script src="http://code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
        <link rel="stylesheet" type="text/css" href="css/general.css">
        <link rel="stylesheet" href="http://code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
    </head>
    <body onload='pageLoad()'>
        <%@include file="header.jsp" %>
        <div id='main'>
            <div class='container'>
                    <table id='dataTable'>
                        <tr>
                            <th>Data Type</th>
                            <th>Field Name</th>
                            <th>Value</th>
                            <th>Action</th>
                        </tr>
                        <tr id='inputRow'>
                            <td>
                                <select id='dataTypeList' onchange='listChanged()'>
                                    <option value='Text' selected>Text</option>
                                    <option value='Date'>Date</option>
                                </select>
                            </td>
                            <td><input type='text' id='fieldName' /></td>
                            <td><input type='text' id='fieldValue' /></td>
                            <td><button onclick='insertRecord()'>Insert</button></td>
                        </tr>
                    </table>
                <button onclick='sendAjax()'>Save</button>
            </div>
        </div>
    </body>
</html>
