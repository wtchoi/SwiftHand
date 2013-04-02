package edu.berkeley.wtchoi.logger;

/**
 * Created with IntelliJ IDEA.
 * User: cusgadmin
 * Date: 4/13/12
 * Time: 12:08 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Logger {
    private static LoggerImp logger;

    public static void init(LoggerImp l){
        logger = l;
    }

    public static void log(String s){
        logger.log(s);
    }
}
