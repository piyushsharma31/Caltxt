package com.jovistar.commons.exception;

public class CCMException extends Exception {

 //   String codestr;
    int code=0;
    //error, status codes
    public static final int SUCCCESS = 1;
    public static final int ERROR_GENERAL = 0;

    public CCMException() {
        super();
    }

    public CCMException(String e) {
        super(e);
    }

    public CCMException(int cd, String e) {
        super(e);
        code = cd;
    }

    /*public String getShortMessage() {
        return codestr;
    }
*/
    public int getCode() {
        return code;
    }
}
