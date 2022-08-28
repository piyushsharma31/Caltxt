package com.jovistar.commons.ui;

import com.jovistar.commons.bo.XRes;
import com.jovistar.commons.exception.CCMException;

public interface IDisplayObject {

	//to be called with display data
    void callback(XRes obj) throws CCMException;
	void busy();
	void idle();
    //overidden to provide set and reset function
//    void setTitle(String title);//setTitle() by default implemented in List, Form in lcdui
}
