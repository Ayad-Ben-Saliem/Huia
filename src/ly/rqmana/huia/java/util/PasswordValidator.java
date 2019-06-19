package ly.rqmana.huia.java.util;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.validation.base.ValidatorBase;

public class PasswordValidator extends ValidatorBase {
    {
        setMessage(Utils.getBundle().getString("WEEK_PASSWORD"));
        setIcon(Utils.getErrorIcon());
    }
    @Override
    protected void eval() {
        JFXPasswordField srcNode = (JFXPasswordField) srcControl.get();

        String password = srcNode.getText();
        hasErrors.set(!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&.+=])(?=\\S+$).{8,}$"));
    }
}
