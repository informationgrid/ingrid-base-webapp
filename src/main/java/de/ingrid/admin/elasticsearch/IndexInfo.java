package de.ingrid.admin.elasticsearch;

public class IndexInfo {
    private String toIndex;
    private String toType;
    private String docIdField;
    
    public String getToIndex() {
        return toIndex;
    }
    public void setToIndex(String toIndex) {
        this.toIndex = toIndex;
    }
    public String getToType() {
        return toType;
    }
    public void setToType(String toType) {
        this.toType = toType;
    }
    public String getDocIdField() {
        return docIdField;
    }
    public void setDocIdField(String docIdField) {
        this.docIdField = docIdField;
    }
    
}
