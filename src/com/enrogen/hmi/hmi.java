/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.enrogen.hmi;

import java.awt.Color;

public interface hmi {

    //Colours
    public static final Color BACKGROUND_MAIN = Color.BLACK;
    public static final Color BACKGROUND_TAB = new Color(38,38,38);


    public static final double VERSION = 0.1;
    public static final double REQUIRED_XML_VERSION = 0.1;

    //If no setting.xml file exists it will be created with these parameters
    //You need to delete the setting.xml to recreate if you change these values
    public static final String SETTING_XML_MAINDIRECTORY = System.getProperty("user.home");
    public static final String SETTING_XML_SUBDIRECTORY = ".enrogenhmi";
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String SETTING_XML_FILENAME = "setting.xml";
    public static final String FULL_ENROGEN_HMI_PATH = SETTING_XML_MAINDIRECTORY
            + FILE_SEPARATOR + SETTING_XML_SUBDIRECTORY;
    public static final String FULL_SETTING_XML_PATH = FULL_ENROGEN_HMI_PATH
            + FILE_SEPARATOR + SETTING_XML_FILENAME;

    //display.xml //stores display setting
    public static final String DISPLAY_XML_FILENAME = "display.xml";
    public static final String FULL_DISPLAY_XML_PATH = FULL_ENROGEN_HMI_PATH
            + FILE_SEPARATOR + DISPLAY_XML_FILENAME;

    //alarms.xml //stores the alarm settings
    public static final String ALARM_XML_FILENAME = "alarm.xml";
    public static final String FULL_ALARM_XML_PATH = FULL_ENROGEN_HMI_PATH
            + FILE_SEPARATOR + ALARM_XML_FILENAME;

    //Default Preferences
    public static final int DEFAULT_GRID_SIZE = 70;

    //GUI Settings
    public static final int EDITOR_BOX_HEIGHT = 600;
    public static final int EDITOR_BOX_WIDTH = 800;
    public static final int EDITOR_BOX_ROWHEIGHT = 30;
    public static final int EDITOR_BOX_ROWWIDTH = 550;
    
    //Tickers
    public static final int SQL_ALIVE_POLL_TICKER = 1000; //mSec
    public static final int REPAINT_POLL_TICKER = 1000; //mSec
    public static final int MAX_LOGIN_TIME = 360000; //mSec

    //SQL Settings
    public static final String MYSQL_DEFAULT_SERVER = "127.0.0.1";
    public static final String MYSQL_DEFAULT_PORT = "3306";
    public static final String MYSQL_DEFAULT_USER = "modbus2sql";
    public static final String MYSQL_DEFAULT_PASSWORD = "modicon";
    public static final String MYSQL_DEFAULT_DATABASE = "modbus2sql";

    //SMTP Server Settings
    public static final String SMTP_ISENABLED = "false";
    public static final String SMTP_SERVER = "mail.enrogen.org";
    public static final String SMTP_PORT = "25";
    public static final String SMTP_USERNAME = "enrogenhmi@enrogen.org";
    public static final String SMTP_PASSWORD = "modicon";

    //Login Passwords
    public static final String LEVEL2_PASSWORD = "003";
    public static final String LEVEL3_PASSWORD = "112";
    public static final String LEVEL4_PASSWORD = "308";

    //Logging to file
    public static final int LOG_SIZE_LIMIT =  1000000; // 1 Mb
    public static final int LOG_MAX_FILES = 3;
    public static final boolean LOG_APPEND = true;
    public static final String LOG_ENROGENHMI_FILENAME = FULL_ENROGEN_HMI_PATH +
            FILE_SEPARATOR + "EnrogenHMILog%g.log";
}
