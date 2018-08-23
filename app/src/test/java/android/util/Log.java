package android.util;

public final class Log {
    public static int d(String tag, String msg) {
        System.out.println(msg);
        return 0;
    }

    public static int e(String tag, String msg) {
        System.out.println(msg);
        return 0;
    }

    public static int e(String tag, String msg, Throwable t) {
        System.out.println(msg);
        t.printStackTrace();
        return 0;
    }
}