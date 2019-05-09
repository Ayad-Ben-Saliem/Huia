package ly.rqmana.huia.java.util;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.base.IFXValidatableControl;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.scene.Node;
import javafx.scene.control.ComboBoxBase;

public class RequiredFieldValidator extends ValidatorBase {
    {
        setMessage(Utils.getBundle().getString("REQUIRED_FIELD"));
        setIcon(Utils.getErrorIcon());
    }
    @Override
    protected void eval() {
        Node srcNode = srcControl.get();
        if (srcNode instanceof JFXTextField) {
            JFXTextField textField = (JFXTextField) srcNode;
            textField.resetValidation();
            if (textField.getText().isEmpty()) {
                hasErrors.set(true);
            } else {
                hasErrors.set(false);
            }
        }

        if (srcNode instanceof ComboBoxBase){
            // includes time & date picker
            ComboBoxBase comboBoxBase = (ComboBoxBase) srcNode;
            if (comboBoxBase instanceof IFXValidatableControl) {
                ((IFXValidatableControl) comboBoxBase).resetValidation();

                if (comboBoxBase.getValue() == null) {
                    hasErrors.set(true);
                } else {
                    hasErrors.set(false);
                }
            }
        }
    }
}
