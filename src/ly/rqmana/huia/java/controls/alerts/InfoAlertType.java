package ly.rqmana.huia.java.controls.alerts;


import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public enum InfoAlertType {

    NONE(Color.TRANSPARENT, FontAwesomeIcon.CIRCLE, 32),
    INFO(Color.DEEPSKYBLUE, FontAwesomeIcon.INFO_CIRCLE, 32),
    CONFIRM(Color.DEEPSKYBLUE, FontAwesomeIcon.QUESTION_CIRCLE, 32),
    WARNING(Color.DARKORANGE, FontAwesomeIcon.EXCLAMATION_TRIANGLE, 32),
    ERROR(Color.RED, FontAwesomeIcon.EXCLAMATION_CIRCLE, 32);


    public final Paint fill;
    public final FontAwesomeIcon graphic;
    public final Number glyphSize;

    InfoAlertType(Paint fill, FontAwesomeIcon graphic, Number glyphSize) {
        this.fill = fill;
        this.graphic = graphic;
        this.glyphSize = glyphSize;
    }



}
