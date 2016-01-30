package Autofill;


import com.itextpdf.text.Rectangle;

public class AcroFormField implements Comparable<AcroFormField>{

    private String fieldLabel;
    private String fieldName;
    private String fieldValue;
    private String group;
    private Rectangle position;
    private int page;
    private String personalFieldName;
    
    public AcroFormField() {
        personalFieldName = "";
    }
    
    public String getFieldLabel() {
        return fieldLabel;
    }

    public void setFieldLabel(String fieldLabel) {
        this.fieldLabel = fieldLabel;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public String getGroup() {
        return this.group;
    }
    
    public void setGroup(String group) {
        this.group = group;
    }
    
    public Rectangle getPosition() {
        return position;
    }

    public void setPosition(Rectangle position) {
        this.position = position;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getPersonalFieldName() {
        return personalFieldName;
    }

    public void setPersonalFieldName(String personalFieldName) {
        this.personalFieldName = personalFieldName;
    }
    
    @Override
    public int compareTo(AcroFormField another) {
        if (this.page > another.getPage()) {
            return 1;
        } else if (this.page < another.getPage()) {
            return -1;
        } else {    // In same page
            if (Math.round(this.position.getTop()) < Math.round(another.getPosition().getTop()) &&
                    Math.round(this.position.getBottom()) < Math.round(another.getPosition().getBottom()) &&
                    another.getPosition().getTop() - this.position.getTop() > this.position.getHeight()) {
                return 1;
            } else if (Math.round(this.position.getTop()) > Math.round(another.getPosition().getTop()) &&
                    Math.round(this.position.getBottom()) > Math.round(another.getPosition().getBottom()) &&
                    this.position.getTop() - another.getPosition().getTop() > this.position.getHeight()) {
                return -1;
            } else {    // In same row
                if (this.position.getLeft() > another.getPosition().getLeft()) {
                    return 1;
                } else if (this.position.getLeft() < another.getPosition().getLeft()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }
    
}
