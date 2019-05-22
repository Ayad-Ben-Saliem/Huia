package ly.rqmana.huia.java.util;

public class OSValidator {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() {
        return (OS.startsWith("win"));
    }

    public static boolean isMac() {
        return (OS.startsWith("mac"));
    }

    public static boolean isUnix() {
        return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix") );
    }

    public static boolean isSolaris() {
        return (OS.contains("sunos"));
    }

}
