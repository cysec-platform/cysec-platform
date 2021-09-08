package eu.smesec.totalcross;

import eu.smesec.totalcross.main.CySecApp;
import totalcross.TotalCrossApplication;


public class App{
    public static void main(String[] args) {
        TotalCrossApplication.run(CySecApp.class,
                "/r", "ACTIVATION_KEY","/scr","iphone", "/fingertouch");
    }
}

