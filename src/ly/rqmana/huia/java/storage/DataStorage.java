package ly.rqmana.huia.java.storage;

import com.sun.istack.internal.NotNull;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import ly.rqmana.huia.java.fingerprints.hand.Finger;
import ly.rqmana.huia.java.fingerprints.hand.FingerID;
import ly.rqmana.huia.java.models.Institute;
import ly.rqmana.huia.java.models.Subscriber;
import ly.rqmana.huia.java.util.OS;
import ly.rqmana.huia.java.util.OSValidator;
import ly.rqmana.huia.java.util.Utils;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.zeroturnaround.zip.ZipUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.prefs.Preferences;

public class DataStorage {

    private static final String HUIA_DIRECTORY_NAME = "Huia Health Care";

    public static final String WINDOWS_DATA_DIR = "C:\\ProgramData\\";
    public static final String UNIX_DATA_DIR = System.getProperty("user.home");
    public static final String MAC_DATA_DIR = System.getProperty("user.home");

    public static final String SUBSCRIBERS_DIR_NAME = "Subscribers";
    public static final String NEW_REG_DIR_NAME = "New Registrations";
    public static final String PERSONAL_PICTURES_DIR_NAME = "Personal Pictures";
    public static final String FINGERPRINT_IMAGES_DIR_NAME = "Fingerprint Images";
    public static final String FINGERPRINT_TEMPLATES_DIR_NAME = "Fingerprint Templates";

    private static final Path DATA_DIRECTORY;

    private static final String BASE_INFO_FILE_NAME = "BaseInfo.json";

    static {
        String osDataDirectory;
        if (OSValidator.isWindows()) {
            osDataDirectory = WINDOWS_DATA_DIR;
        } else if(OSValidator.isUnix()) {
            osDataDirectory = UNIX_DATA_DIR;
        } else if (OSValidator.isMac()) {
            osDataDirectory = MAC_DATA_DIR;
        }else{
            osDataDirectory = System.getProperty("user.home");
        }

        DATA_DIRECTORY = Paths.get(osDataDirectory, HUIA_DIRECTORY_NAME);
    }

    public static Path getSubscribersDir() {
        return DATA_DIRECTORY.resolve(SUBSCRIBERS_DIR_NAME);
    }

    public static Path getNewSubscribersDir() {
        return DATA_DIRECTORY.resolve(NEW_REG_DIR_NAME);
    }

    public static Path saveSubscriberData(Subscriber subscriber) throws IOException {
        Path subscriberDir = createNewSubscriberDir(subscriber);

        saveSubscriberFingerprintImages(subscriber);
        saveSubscriberPersonalPictures(subscriber);

        String subDirString = subscriberDir.toString();
        if (subDirString.endsWith(File.separator)){
            subDirString = subDirString.substring(0, subDirString.length()-1);
        }

        Path zipPath = Paths.get(subDirString + ".zip");
        ZipUtil.pack(subscriberDir.toFile(), zipPath.toFile());

        FileUtils.deleteDirectory(subscriberDir.toFile());

        return zipPath;
    }

    public static Subscriber loadSubscriberData(Subscriber subscriber) throws IOException {
        Path zipSubscriberDir = getSubscriberDir(subscriber);
        String _unzipDir = zipSubscriberDir.toString();
        if (_unzipDir.toLowerCase().endsWith(".zip"))
            _unzipDir = _unzipDir.substring(0, _unzipDir.length() - 4);
        Path unzipDir = Paths.get(_unzipDir);
        ZipUtil.unpack(zipSubscriberDir.toFile(), unzipDir.toFile());

        Path fingerprintTemplatesDir = unzipDir.resolveSibling(FINGERPRINT_TEMPLATES_DIR_NAME);
        if (Files.exists(fingerprintTemplatesDir)) {

        }

        Path fingerprintImagesDir = unzipDir.resolveSibling(FINGERPRINT_IMAGES_DIR_NAME);
        if (Files.exists(fingerprintImagesDir)) {

        }

        Path personalPicturesDir = unzipDir.resolveSibling(PERSONAL_PICTURES_DIR_NAME);
        if (Files.exists(personalPicturesDir)) {

        }

        return subscriber;
    }

    public static void saveSubscriberPersonalPictures(Subscriber subscriber) throws IOException {
        Path subscriberDir = createNewSubscriberDir(subscriber);

        Path personalPicturesDir = subscriberDir.resolve(PERSONAL_PICTURES_DIR_NAME);

        if (! Files.exists(personalPicturesDir))
            Files.createDirectories(personalPicturesDir);

        savePersonalPictures(personalPicturesDir, subscriber.getPersonalPictures());
    }

    public static void saveSubscriberFingerprintImages(Subscriber subscriber) throws IOException {

        Path subscriberDir = createNewSubscriberDir(subscriber);
        Path fingerprintImagesDir = subscriberDir.resolve(FINGERPRINT_IMAGES_DIR_NAME);

        if (! Files.exists(fingerprintImagesDir))
            Files.createDirectories(fingerprintImagesDir);

        // Save the images
        ObservableList<Finger> fingersList = FXCollections.observableArrayList();

        fingersList.addAll(subscriber.getRightHand().getFingersUnmodifiable());
        fingersList.addAll(subscriber.getLeftHand().getFingersUnmodifiable());

        for (Finger finger : fingersList) {
            saveFingerprintImages(fingerprintImagesDir, finger);
        }
    }

    private static Path createNewSubscriberDir(Subscriber subscriber) throws IOException {

        Path subscriberDir = getNewSubscriberDir(subscriber);

        // the first time the sub is being added.
        if (! Files.exists(subscriberDir)){
            // try to create it
            Files.createDirectories(subscriberDir);
        }

        return subscriberDir;
    }

    private static Path getSubscriberDir(Subscriber subscriber) throws IOException {
        // Storage hierarchy is as follows:
        //
        // SUBSCRIBERS_DIR
        //  - {INSTITUTE_ID}
        //      - {SUBSCRIBER_ID}
        //              Subscribers data files.
        //              - ...
        //              - ...

        Institute institute = subscriber.getInstitute();
        String subscriberId = String.valueOf(subscriber.getId());

        if (institute == null )
            throw new RuntimeException("Invalid institute");

        return getSubscribersDir().resolve(String.valueOf(institute.getId())).resolve(subscriberId);
    }

    private static Path getNewSubscriberDir(Subscriber subscriber) {
        // Storage hierarchy is as follows:
        //
        // NEW_REG_DIR
        //  - {INSTITUTE_ID}
        //      - {SUBSCRIBER_ID}
        //              Subscribers data files.
        //              - ...
        //              - ...

        Institute institute = subscriber.getInstitute();
        if (institute == null )
            throw new RuntimeException("Invalid institute");

        String subscriberId = String.valueOf(subscriber.getId());

        return getNewSubscribersDir().resolve(String.valueOf(institute.getId())).resolve(subscriberId);
    }

    private static void saveFingerprintImages(Path imageDir, Finger finger) throws IOException {
        // Save fingerprintImages in format
        //
        // ...
        //    - {SUBSCRIBER_ID}
        //          - FINGER_{FINGER_INDEX}_{IMAGE_ID}.jpg
        //          - ...
        //

        if (finger == null)
            return;

        String fileExtension = ".jpg";
        ObservableList<BufferedImage> fingerprintImages = finger.getFingerprintImages();

        for (int imageIndex = 0; imageIndex < fingerprintImages.size(); imageIndex++) {

            BufferedImage fingerprintImage = fingerprintImages.get(imageIndex);
            FingerID fingerId = finger.getId();

            String fileName = String.format("finger_%d_%d%s", fingerId.index(), imageIndex, fileExtension);
            File imageFile = imageDir.resolve(fileName).toFile();

            ImageIO.write(fingerprintImage, "JPEG", imageFile);
        }
    }

    private static void savePersonalPictures(Path imageDir, Map<Integer, Image> personalPictures) throws IOException {
        // Save fingerprintImages in format
        //
        // ...
        //    - {SUBSCRIBER_ID}
        //          - rightPicture.jpg
        //          - frontPicture.jpg
        //          - leftPicture.jpg
        //          - picture3.jpg
        //          - picture4.jpg
        //          - picture5.jpg
        //          - ...
        //

        if (personalPictures == null)
            return;

        String fileExtension = ".jpg";

        for (Map.Entry<Integer, Image> entry : personalPictures.entrySet()) {
            Integer imageIndex = entry.getKey();
            Image image = entry.getValue();

            String imageFileName = String.format("picture_%s%s", imageIndex, fileExtension);

            BufferedImage personalPicture = SwingFXUtils.fromFXImage(image, null);

            File imageFile = imageDir.resolve(imageFileName).toFile();


            ImageIO.write(personalPicture, "JPEG", imageFile);
        }
    }














































    @NotNull
    public static OS getOSArch() {
        if (System.getProperty("os.name").contains("Windows")) {
            if (System.getenv("ProgramFiles(x86)") != null)
                return OS.WINDOWS7_x64;
            else
                return OS.WINDOWS7_x86;
        } else {
            if (System.getProperty("os.arch").contains("64"))
                return OS.Mac_x64;
            else
                return OS.Mac_x86;
        }
    }

    public static Path getInstallationPath() {
        switch (getOSArch()) {
            case Mac_x64:
            case Mac_x86:
                return Paths.get("/Applications/" + Utils.APP_NAME);
            case Linux_x64:
            case Linux_x86:
                return Paths.get("/opt/" + Utils.APP_NAME);
            case WINDOWS7_x86:
            case WINDOWS7_x64:
            case WINDOWS8_x86:
            case WINDOWS8_x64:
            case WINDOWS8_1_x86:
            case WINDOWS8_1_x64:
            case WINDOWS10_x86:
            case WINDOWS10_x64:
            default:
                return Paths.get("C:\\Program Files\\" + Utils.APP_NAME);
        }
    }

    public static Path getDataPath(){
        return DATA_DIRECTORY;
    }

    private static Path getHomePath() {
        return Paths.get(System.getProperty("user.home"));
    }

    private static String getUsername() {
        return System.getProperty("user.name");
    }

    public static Path desktop() {
        return Paths.get(getHomePath() + "/Desktop");
    }

    public static void updateBaseInfo(String key, Object value) {
        // TODO
    }
}
