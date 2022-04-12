package config;

import java.text.SimpleDateFormat;
import java.util.Date;

/*
* Log with timestamp and log level
 */
public class Log {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss");

    private static void printWithLevel(String msg, String level, Object ...args) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(level).append(dateFormat.format(new Date())).append(" - ");
        strBuilder.append(String.format(msg, args));
        System.out.println(strBuilder.toString());
    }

    public static void Info(String msg, Object ...args) {
        printWithLevel(msg, "[Info]", args);
    }

    public static void Operation(String msg, Object ...args) {printWithLevel(msg, "[Operation]", args);}

    public static void Debug(String msg, Object ...args) {
        printWithLevel(msg, "[Debug]", args);
    }

    public static void Error(String msg, Object ...args) {
        printWithLevel(msg, "[Error]", args);
    }

    public static void Warn(String msg, Object ...args) {
        printWithLevel(msg, "[Warn]", args);
    }
}