package com.inertia.phyzmo.datadisplay.models;

public class TableRowModel {

    private String header1;
    private String header2;
    private String header3;
    private String header4;

    public TableRowModel(String header1, String header2, String header3,
                         String header4){

        this.header1 = header1;
        this.header2 = header2;
        this.header3 = header3;
        this.header4 = header4;

    }

    public String getHeader1() {
        return header1;
    }

    public void setHeader1(String header1) {
        this.header1 = header1;
    }

    public String getHeader2() {
        return header2;
    }

    public void setHeader2(String header2) {
        this.header2 = header2;
    }

    public String getHeader3() {
        return header3;
    }

    public void setHeader3(String header3) {
        this.header3 = header3;
    }

    public String getHeader4() {
        return header4;
    }

    public void setHeader4(String header4) {
        this.header4 = header4;
    }
}