package ly.rqmana.huia.java.fingerprints;

import java.awt.image.BufferedImage;

public class FingerprintUtils {

    private FingerprintUtils(){

    }

    public static BufferedImage imageFromHamsterDX(byte[] data, int width, int height){

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        image.getRaster().setDataElements(0, 0, width, height, data);
        return image;
    }
}
