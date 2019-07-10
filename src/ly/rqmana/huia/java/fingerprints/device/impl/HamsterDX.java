package ly.rqmana.huia.java.fingerprints.device.impl;

import com.nitgen.SDK.BSP.NBioBSPJNI;
import com.nitgen.SDK.BSP.NBioBSPJNI.FIR_TEXTENCODE;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.fingerprints.FingerprintCaptureResult;
import ly.rqmana.huia.java.fingerprints.FingerprintUtils;
import ly.rqmana.huia.java.fingerprints.SecurityLevel;
import ly.rqmana.huia.java.fingerprints.activity.FingerScanTimoutException;
import ly.rqmana.huia.java.fingerprints.activity.FingerprintException;
import ly.rqmana.huia.java.fingerprints.device.FingerprintDevice;
import ly.rqmana.huia.java.fingerprints.hand.Finger;
import ly.rqmana.huia.java.fingerprints.hand.FingerID;
import ly.rqmana.huia.java.fingerprints.hand.Hand;
import ly.rqmana.huia.java.fingerprints.hand.HandType;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class HamsterDX extends FingerprintDevice {

    private static final int SCAN_FORMAT = NBioBSPJNI.FIR_FORMAT.STANDARD;
    private final NBioBSPJNI BSP;
    private final NBioBSPJNI.Export exportEngine;

    public HamsterDX(long timeoutMillis, SecurityLevel securityLevel) {
        super(timeoutMillis, securityLevel);
        this.BSP = new NBioBSPJNI();
        this.exportEngine = BSP.new Export();
        initListeners();
    }

    private void initListeners(){

        Supplier<Boolean> initInfoUpdater = () -> {
            NBioBSPJNI.INIT_INFO_0 initInfo0 = BSP.new INIT_INFO_0();
            BSP.GetInitInfo(initInfo0);

            initInfo0.SecurityLevel = hamsterSecurityLevelFromHuiaSecurityLevel();
            initInfo0.DefaultTimeout = (int) getTimeoutMillis();

            BSP.SetInitInfo(initInfo0);
            return BSP.IsErrorOccured();
        };

        timeoutMillisProperty().addListener((observable, oldValue, newValue) -> {
            long newTimeout = newValue.longValue();
            if (newTimeout < 0)
                throw new IllegalArgumentException("Timeout cannot be negative, found value :" + newTimeout);
            initInfoUpdater.get();
        });

        securityLevelProperty().addListener((observable, oldValue, newValue) -> {
            initInfoUpdater.get();
        });
    }

    @Override
    public Finger captureFinger(FingerID fingerID) {

        Finger finger = null;

        NBioBSPJNI.FIR_HANDLE firDataHandler = BSP.new FIR_HANDLE();
        NBioBSPJNI.FIR_HANDLE auditDataHandler = BSP.new FIR_HANDLE();

        NBioBSPJNI.WINDOW_OPTION windowOption = BSP.new WINDOW_OPTION();
        windowOption.WindowStyle = NBioBSPJNI.WINDOW_STYLE.POPUP;

        BSP.Capture(NBioBSPJNI.FIR_PURPOSE.VERIFY, firDataHandler, (int) getTimeoutMillis(), auditDataHandler, windowOption);

        if (! BSP.IsErrorOccured()) {

            FIR_TEXTENCODE firData = BSP.new FIR_TEXTENCODE();
            BSP.GetTextFIRFromHandle(firDataHandler, firData, SCAN_FORMAT);

            NBioBSPJNI.INPUT_FIR auditDataInput = BSP.new INPUT_FIR();
            auditDataInput.SetFIRHandle(auditDataHandler);

            NBioBSPJNI.Export.AUDIT exportAudit = exportEngine.new AUDIT();
            exportEngine.ExportAudit(auditDataInput, exportAudit);

            List<BufferedImage> fingerImages = getImage(exportAudit.FingerData[0], exportAudit.ImageWidth, exportAudit.ImageHeight);

            finger = new Finger(fingerID, firData.TextFIR, fingerImages);
        }

        return finger;
    }

    private List<BufferedImage> getImage(NBioBSPJNI.Export.FINGER_DATA fingerData, int width, int height){

        int imagesCount = fingerData.Template.length;

        byte[][] imagesBinary = new byte[imagesCount][];

        for (int i = 0; i < imagesCount; i++){
            imagesBinary[i] = fingerData.Template[i].Data;
        }

        List<BufferedImage> fingerImages = new ArrayList<>();

        for (byte[] imagesDatum : imagesBinary) {
            BufferedImage image = FingerprintUtils.imageFromHamsterDX(imagesDatum, width, height);
            fingerImages.add(image);
        }

        return fingerImages;
    }

    @Override
    public FingerprintCaptureResult captureHands() {

        Hand rightHand = null;
        Hand leftHand = null;
        String fingerprintsString = "";

        NBioBSPJNI.FIR_HANDLE firDataHandler = BSP.new FIR_HANDLE();
        NBioBSPJNI.FIR_HANDLE auditDataHandler = BSP.new FIR_HANDLE();

        // TODO: the first argument in the enroll method takes
        //  a previously stored handler which can be used, i think,
        //  for fingerprint data update operations

        BSP.Enroll(null, firDataHandler,null , (int) getTimeoutMillis(), auditDataHandler, null);

        if (! BSP.IsErrorOccured()) {

            // the hand capturing goes as follows:
            // 1- show the enrollment window and let the user capture fingerprints.
            //
            // 2- iterate over the fingerprint data from firDataHandler in order to find code, using the
            // (NBioBSPJNI.Export.FINGER_DATA) exported from firDataHandler
            //
            // 3- create fingers' images using the image data (NBioBSPJNI.Export.Audit)
            // exported from auditDataHandler for each finger.

            NBioBSPJNI.INPUT_FIR firDataInput = BSP.new INPUT_FIR();
            firDataInput.SetFIRHandle(firDataHandler);

            NBioBSPJNI.Export.DATA exportFirData = exportEngine.new DATA();
            exportEngine.ExportFIR(firDataInput, exportFirData, NBioBSPJNI.EXPORT_MINCONV_TYPE.ISO);

            NBioBSPJNI.INPUT_FIR auditDataInput = BSP.new INPUT_FIR();
            auditDataInput.SetFIRHandle(auditDataHandler);

            NBioBSPJNI.Export exportEngine = BSP.new Export();
            NBioBSPJNI.Export.AUDIT audit = exportEngine.new AUDIT();
            exportEngine.ExportAudit(auditDataInput, audit);

            List<Finger> rightFingers = new ArrayList<>();
            List<Finger> leftFingers = new ArrayList<>();

            // each finger has two Templates. In this case a Template contains
            // info about the fingerprint code not the fingerprint image
            for (NBioBSPJNI.Export.FINGER_DATA fingerData: exportFirData.FingerData) {

                String fingerprintCode = null;
                for (NBioBSPJNI.Export.TEMPLATE_DATA templateData: fingerData.Template) {
                    // using the method
                    byte[] firBinary = templateData.Data;

                    NBioBSPJNI.FIR_HANDLE importHandle = BSP.new FIR_HANDLE();
                    exportEngine.ImportFIR(firBinary, firBinary.length, NBioBSPJNI.EXPORT_MINCONV_TYPE.ISO, importHandle);

                    FIR_TEXTENCODE importedFirText = BSP.new FIR_TEXTENCODE();
                    BSP.GetTextFIRFromHandle(importHandle, importedFirText);

                    //FIXME: currently each finger only stores the second image's code
                    // find a way to merge the two code into one fingerprint code.
                    // Added on: May 30th, 2019
                    fingerprintCode = importedFirText.TextFIR;
                }

                FingerID fingerID = huiaFingerIDFromHamsterFingerID(fingerData.FingerID);

                Stream<NBioBSPJNI.Export.FINGER_DATA> auditStream = Arrays.stream(audit.FingerData);

                Optional<NBioBSPJNI.Export.FINGER_DATA> result =
                        auditStream.filter(auditData -> auditData.FingerID == fingerData.FingerID)
                                   .findFirst();

                List<BufferedImage> fingerImages;

                if (result.isPresent()) {
                    NBioBSPJNI.Export.FINGER_DATA fingerprintImage = result.get();
                    fingerImages = getImage(fingerprintImage, audit.ImageWidth, audit.ImageHeight);

                }else{
                    throw new IllegalStateException("[Hamster DX] Couldn't find finger image in audit data. FinderID: " + fingerID);
                }

                Finger finger = new Finger(fingerID, fingerprintCode, fingerImages);

                if (fingerID.isRightFinger())
                    rightFingers.add(finger);
                else
                    leftFingers.add(finger);
            }

            rightHand = new Hand(HandType.RIHGT, rightFingers);
            leftHand = new Hand(HandType.LEFT, leftFingers);

            NBioBSPJNI.FIR_TEXTENCODE fingersCodeEncoder = BSP.new FIR_TEXTENCODE();
            BSP.GetTextFIRFromHandle(firDataHandler, fingersCodeEncoder, SCAN_FORMAT);
            fingerprintsString =  fingersCodeEncoder.TextFIR;
        }

        return new FingerprintCaptureResult(rightHand, leftHand, fingerprintsString);
    }


    @Override
    public Task<Boolean> matchFingerprintTemplate(String sourceFingerprint, String targetFingerprint) {

        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                FIR_TEXTENCODE sourceTextEncode = BSP.new FIR_TEXTENCODE();
                sourceTextEncode.TextFIR = sourceFingerprint;
                NBioBSPJNI.INPUT_FIR sourceInput = BSP.new INPUT_FIR();
                sourceInput.SetTextFIR(sourceTextEncode);

                FIR_TEXTENCODE targetTextEncode = BSP.new FIR_TEXTENCODE();
                targetTextEncode.TextFIR = targetFingerprint;
                NBioBSPJNI.INPUT_FIR targetInput = BSP.new INPUT_FIR();
                targetInput.SetTextFIR(targetTextEncode);

                Boolean result = Boolean.FALSE;
                BSP.VerifyMatch(sourceInput, targetInput, result, null);

                checkErrors();
                return result;
            }
        };
    }

    /* ************************************************* *
     *
     *                  UTILS
     *
     * ************************************************* */
    private void checkErrors(){
        if (BSP.IsErrorOccured()) {
            int errorCode = BSP.GetErrorCode();
            if (errorCode == NBioBSPJNI.ERROR.NBioAPIERROR_CAPTURE_TIMEOUT){
                throw new FingerScanTimoutException("[Hamster DX] Capture timed out.");
            }
            throw new FingerprintException("[Hamster DX] An error occurred while processing fingerprints NBioBSPJNI.ERROR=" + BSP.GetErrorCode());
        }
    }

    private int hamsterSecurityLevelFromHuiaSecurityLevel(){
        return getSecurityLevel().LEVEL;
    }

    private FingerID huiaFingerIDFromHamsterFingerID(byte fingerId){
        return FingerID.valueOf(fingerId);
    }

    @Override
    public void open() throws IOException {

        NBioBSPJNI.DEVICE_ENUM_INFO deviceEnum = BSP.new DEVICE_ENUM_INFO();
        BSP.EnumerateDevice(deviceEnum);

        NBioBSPJNI.INIT_INFO_0 initInfo0 = BSP.new INIT_INFO_0();
        BSP.GetInitInfo(initInfo0);

        initInfo0.SecurityLevel = hamsterSecurityLevelFromHuiaSecurityLevel();
        BSP.SetInitInfo(initInfo0);

        boolean errorOccurred = false;

        // an empty array of DeviceInfo means the
        // NBioBSP couldn't detect the device for some reason
        // eg driver failure, device not connected, ... etc.

        if (deviceEnum.DeviceInfo.length >= 1) {
            BSP.OpenDevice(deviceEnum.DeviceInfo[0].DeviceID,
                    deviceEnum.DeviceInfo[0].Instance);
        }
        else{
            errorOccurred = true;
        }

        errorOccurred = errorOccurred || BSP.IsErrorOccured();

        if (errorOccurred){
            throw new IOException("Could not open hamster dx [error code: " + BSP.GetErrorCode() +" ] ");
        }
    }

    @Override
    public boolean isOpen() {
        return BSP.GetOpenedDeviceID() != 0;
    }

    @Override
    public void close() throws IOException {
        BSP.CloseDevice();  // closes the last opened device
        if (BSP.IsErrorOccured()){
            throw new IOException("[Hamster DX] Could not close device ");
        }
        BSP.dispose();
    }

    @Override
    public boolean isTimeoutSupported() {
        return true;
    }

    @Override
    public boolean isFingerImageSupported() {
        return true;
    }

    @Override
    public boolean isHandCaptureSupported() {
        return true;
    }
}
