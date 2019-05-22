package ly.rqmana.huia.java.storage;

import ly.rqmana.huia.java.util.OSValidator;
import ly.rqmana.huia.java.util.fingerprint.Finger;
import ly.rqmana.huia.java.util.fingerprint.Hand;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class DataStorage {

    public static final String WINDOWS_DATA_DIR = "C:/ProgramData/Huia/";
    public static final String UNIX_DATA_DIR = System.getProperty("user.home") + "/.Huia/";
    public static final String MAC_DATA_DIR = System.getProperty("user.home") + "/Huia/";

    public static final String DB_NAME = "Huia.db";

    public static final String FINGERPRINTS_DIR_NAME = "Fingerprints/";
    public static final String NEW_REG_DIR_NAME = "New Registrations/";
    public static final String TEMP_FINGERPRINT_IMAGES = getNewRegDir() + "Temp/";

    public static String getDataDir() {
        if (OSValidator.isWindows()) {
            return WINDOWS_DATA_DIR;
        } else if(OSValidator.isUnix()) {
            return UNIX_DATA_DIR;
        } else if (OSValidator.isMac()) {
            return MAC_DATA_DIR;
        }
        return null;
    }

    public static String getDBUrl() {
        return "jdbc:sqlite:" + getDataDir() + DB_NAME;
    }

    public static String getFingerprintsDir() {
        return getDataDir() + FINGERPRINTS_DIR_NAME;
    }

    public static String getNewRegDir() {
        return getFingerprintsDir() + NEW_REG_DIR_NAME;
    }

    public static String getNewRegFingerprintDir(String workId) {
        workId += "_";
        Path path = Paths.get(getNewRegDir());
        try {
            Files.createDirectories(path);
            List<Path> subfolder = Files.walk(path, 1)
                    .filter(Files::isDirectory)
                    .collect(Collectors.toList());
            subfolder.remove(0);

            int dirIndex = 1;
            for (Path p : subfolder) {
                StringBuilder stringPath = new StringBuilder(p.toString());
                String substring = stringPath.substring(stringPath.lastIndexOf("/") + 1);
                if (substring.startsWith(workId)) {
                    String dirInd = substring.substring(substring.indexOf('_') + 1);
                    int dirI = Integer.parseInt(dirInd);
                    if (dirI >= dirIndex) {
                        dirIndex = dirI + 1;
                    }
                }
            }
            return getNewRegDir() + workId + dirIndex;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveNewFingerprintImages(String workId, Hand rightHand, Hand leftHand) {
        String newDir = getNewRegFingerprintDir(workId);
        try {
            String path = Files.createDirectories(Paths.get(newDir)).toString();

            saveImage( path + "RightThumb", rightHand.getThumbFinger());
            saveImage( path + "RightIndex", rightHand.getIndexFinger());
            saveImage( path + "RightMiddle", rightHand.getMiddleFinger());
            saveImage( path + "RightRing", rightHand.getRingFinger());
            saveImage( path + "RightLittle", rightHand.getLittleFinger());

            saveImage( path + "LeftThumb", leftHand.getThumbFinger());
            saveImage( path + "LeftIndex", leftHand.getIndexFinger());
            saveImage( path + "LeftMiddle", leftHand.getMiddleFinger());
            saveImage( path + "LeftRing", leftHand.getRingFinger());
            saveImage( path + "LeftLittle", leftHand.getLittleFinger());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveImage(String path, Finger finger) {
        if (finger == null) return;
        for (int i = 0; i < finger.getFingerprintImages().size(); i++) {
            try {
                BufferedImage image = ImageIO.read( new ByteArrayInputStream( finger.getFingerprintImages().get(i)));
                ImageIO.write(image, "BMP", new File(path + (i+1) + ".bmp"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
