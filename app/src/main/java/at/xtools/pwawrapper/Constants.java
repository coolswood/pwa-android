package at.xtools.pwawrapper;

public class Constants {
    public Constants(){}
    // Root page
    public static String WEBAPP_URL = "https://mind-health.ru/";
    public static String WEBAPP_HOST = "mind-health.ru"; // used for checking Intent-URLs

	// Constants
    // window transition duration in ms
    public static int SLIDE_EFFECT = 2200;
    // show your app when the page is loaded XX %.
    // lower it, if you've got server-side rendering (e.g. to 35),
    // bump it up to ~98 if you don't have SSR or a loading screen in your web app
    public static int PROGRESS_THRESHOLD = 100;
    // turn on/off mixed content (both https+http within one page) for API >= 21
    public static boolean ENABLE_MIXED_CONTENT = true;
    public static int VERSION = 31;
    public static String LANGUAGE = "ru";
}
