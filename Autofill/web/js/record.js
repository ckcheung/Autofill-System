var currentRow;
var originDataType;
var originFieldName;
var originFieldValue;

pageLoad = function() {
	var dataTable = document.getElementById("dataTable");
	var inputRow = document.getElementById("inputRow");

	// Output data
	if (data.length > 0) {
		var tr;
		var td;	
		var button;

		for (var i=0; i<data.length; i++) {
			tr = document.createElement("tr");
			td = document.createElement("td");
			td.innerHTML = data[i].dataType;
			tr.appendChild(td);
			td = document.createElement("td");
			td.innerHTML = data[i].name;
			tr.appendChild(td);
			td = document.createElement("td");
			td.innerHTML = data[i].value;
			tr.appendChild(td);
			td = document.createElement("td");
			button = createEditButton();
			td.appendChild(button);
			button = createDeleteButton();
			td.appendChild(button);
			tr.appendChild(td);
			dataTable.appendChild(tr);
		}
		dataTable.appendChild(inputRow);
	}
}

// Change input box to match the data type
listChanged = function() {
	var list = document.getElementById("dataTypeList");
	var fieldValue = document.getElementById("fieldValue");

	// Reset to empty field
	fieldValue.value = "";
	
	switch (list.value) {
		case "Text":
			$("#fieldValue").datepicker("destroy");
			break;
		case "Date":
			$("#fieldValue").datepicker({dateFormat: "dd/mm/yy"});
			break;
	}
}

// Insert new record
insertRecord = function() {
	var dataType = document.getElementById('dataTypeList').value;
	var name = document.getElementById('fieldName').value;
	var value = document.getElementById('fieldValue').value;

	var field = {"dataType" : dataType, "name" : name, "value" : value};
	data.push(field);
        
        var dataTable = document.getElementById("dataTable");
	var inputRow = document.getElementById("inputRow");
        var tr;
        var td;	
        var button;

        tr = document.createElement("tr");
        td = document.createElement("td");
        td.innerHTML = dataType;
        tr.appendChild(td);
        td = document.createElement("td");
        td.innerHTML = name;
        tr.appendChild(td);
        td = document.createElement("td");
        td.innerHTML = value;
        tr.appendChild(td);
        td = document.createElement("td");
        button = createEditButton();
        td.appendChild(button);
        button = createDeleteButton();
        td.appendChild(button);
        tr.appendChild(td);
        dataTable.appendChild(tr);
		
        dataTable.appendChild(inputRow);
        
        document.getElementById('dataTypeList').value = "Text";
	document.getElementById('fieldName').value = "";
	document.getElementById('fieldValue').value = "";
        
}

// Create edit record button
createEditButton = function() {
	var button = document.createElement("button");
	button.innerHTML = "Edit";
	button.onclick = editRecord;
	return button;
}

// Change the row structure for updating record
editRecord = function(event) {
	var source = event.target || event.srcElement;
	var parent = source.parentElement;

	if (currentRow != null) {
		cancel();
	}

	currentRow = parent.parentElement;

	// Change Action column
	while (parent.firstChild) {
		parent.removeChild(parent.firstChild);
	}
	var button;
	button = document.createElement("button");
	button.innerHTML = "Update";
	button.onclick = updateRecord;
	parent.appendChild(button);
	button = document.createElement("button");
	button.innerHTML = "Cancel";
	button.onclick = cancelEdit;
	parent.appendChild(button);

	// Save origin data
	originDataType = currentRow.childNodes[0].innerHTML;
	originFieldName = currentRow.childNodes[1].innerHTML;
	originFieldValue = currentRow.childNodes[2].innerHTML;

	// Change Data Type, Field Name and Value column
	currentRow.childNodes[0].removeChild(currentRow.childNodes[0].firstChild);
	var dataTypeList = document.createElement("select");
	var option;
	option = document.createElement("option");
	option.setAttribute("value", "Text");
	option.innerHTML = "Text";
	dataTypeList.appendChild(option);
	option = document.createElement("option");
	option.setAttribute("value", "Date");
	option.innerHTML = "Date";
	dataTypeList.appendChild(option);
	dataTypeList.value = originDataType;
	dataTypeList.onchange = function(event) {
		var source = event.target || event.srcElement;
		var row = source.parentElement.parentElement;
		var list = row.childNodes[0].childNodes[0];
		var fieldValue = row.childNodes[2].childNodes[0];

		// Reset to empty field
		fieldValue.value = "";

		var id = fieldValue.getAttribute("id");
		fieldValue.setAttribute("id", "temp");
		switch (list.value) {
			case "Text":
				$("#temp").datepicker("destroy");
				break;
			case "Date":
				$("#temp").datepicker({dateFormat: "dd/mm/yy"});
				break;
		}
		fieldValue.setAttribute("id", id);
	};
	currentRow.childNodes[0].appendChild(dataTypeList);

	currentRow.childNodes[1].removeChild(currentRow.childNodes[1].firstChild);
	var fieldNameBox = document.createElement("input");
	fieldNameBox.setAttribute("type", "text");
	fieldNameBox.value = originFieldName;
	currentRow.childNodes[1].appendChild(fieldNameBox);

	currentRow.childNodes[2].removeChild(currentRow.childNodes[2].firstChild);
	var fieldValueBox = document.createElement("input");
	fieldValueBox.setAttribute("type", originDataType);
	fieldValueBox.value = originFieldValue;
	currentRow.childNodes[2].appendChild(fieldValueBox);
	if (dataTypeList.value == "Date") {
		fieldValueBox.setAttribute("id", "fieldValue");
		$("fieldValue").datepicker({dateFormat: "dd/mm/yy"});
	}
}

// Create delete button
createDeleteButton = function() {
	var button = document.createElement("button");
	button.innerHTML = "Delete";
	button.onclick = deleteRecord;
	return button;
}

// Delete record from data
deleteRecord = function() {
	var source = event.target || event.srcElement;
	var row = source.parentElement.parentElement;

	// Retrieve the index of the record
	var i = -2;
	while ((row = row.previousSibling) != null) {
		i++;
	}

	data.splice(i, 1);
        
        var dataTable = document.getElementById("dataTable");
        dataTable.removeChild(dataTable.childNodes[i+2]);
}

// Cancel edit mode of a row
cancelEdit = function() {
	while (currentRow.lastChild.firstChild) {
            currentRow.lastChild.removeChild(currentRow.lastChild.firstChild);
	}	
	currentRow.childNodes[0].innerHTML = originDataType;
	currentRow.childNodes[1].innerHTML = originFieldName;
	currentRow.childNodes[2].innerHTML = originFieldValue;
	currentRow.childNodes[3].appendChild(createEditButton());
	currentRow.childNodes[3].appendChild(createDeleteButton());

	// Reset global variable
	currentRow = null;
	originDataType = null;
	originFieldName = null;
	originFieldValue = null;
}

// Update record according to the edited value
updateRecord = function(event) {
	var source = event.target || event.srcElement;
	var row = source.parentElement.parentElement;
	var tempRow = row;

	// Retrieve the index of the record
	var i = -2;
	while ((tempRow = tempRow.previousSibling) != null) {
		i++;
	}

        // Update record
	data[i].dataType = row.childNodes[0].firstChild.value;
	data[i].name = row.childNodes[1].firstChild.value;
	data[i].value = row.childNodes[2].firstChild.value;
	
        row.childNodes[0].removeChild(row.childNodes[0].firstChild);
        row.childNodes[0].innerHTML = data[i].dataType;
        
        row.childNodes[1].removeChild(row.childNodes[1].firstChild);
        row.childNodes[1].innerHTML = data[i].name;
        
        row.childNodes[2].removeChild(row.childNodes[2].firstChild);
        row.childNodes[2].innerHTML = data[i].value;
        
        while (row.lastChild.firstChild) {
            row.lastChild.removeChild(row.lastChild.firstChild);
	}	
	currentRow.childNodes[3].appendChild(createEditButton());
	currentRow.childNodes[3].appendChild(createDeleteButton());
}

// Save data to database
sendAjax = function() {
    $.ajax({
        url: "JsonSaver",
        type: 'POST',
        data: JSON.stringify(data),
        
        success: function(data) {},
        
        error:function(data, status, error) {
            alert("error: "+data+" status: "+status+" error:"+error);
        }
    });
}

