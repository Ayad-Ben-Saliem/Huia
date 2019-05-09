package ly.rqmana.huia.java.controllers;

import javafx.event.ActionEvent;
import ly.rqmana.huia.java.controls.ContactField;
import ly.rqmana.huia.java.util.OnSelectListener;
import ly.rqmana.huia.java.util.Selectable;

public class AddContactMethodDialogController implements Selectable<ContactField.Type> {

    public void onPhoneBtnClicked(ActionEvent actionEvent) {
        for (OnSelectListener onSelectListener : onSelectListeners) {
            onSelectListener.select(ContactField.Type.PHONE);
        }
    }

    public void onMailboxBtnClicked(ActionEvent actionEvent) {
        for (OnSelectListener onSelectListener : onSelectListeners) {
            onSelectListener.select(ContactField.Type.MAILBOX);
        }
    }

    public void onEmailBtnClicked(ActionEvent actionEvent) {
        for (OnSelectListener onSelectListener : onSelectListeners) {
            onSelectListener.select(ContactField.Type.EMAIL);
        }
    }

    public void onFacebookBtnClicked(ActionEvent actionEvent) {
        for (OnSelectListener onSelectListener : onSelectListeners) {
            onSelectListener.select(ContactField.Type.FACEBOOK);
        }
    }

    public void onInstagramBtnClicked(ActionEvent actionEvent) {
        for (OnSelectListener onSelectListener : onSelectListeners) {
            onSelectListener.select(ContactField.Type.INSTAGRAM);
        }
    }

    public void onTwitterBtnClicked(ActionEvent actionEvent) {
        for (OnSelectListener onSelectListener : onSelectListeners) {
            onSelectListener.select(ContactField.Type.TWITTER);
        }
    }

    public void onViberBtnClicked(ActionEvent actionEvent) {
        for (OnSelectListener onSelectListener : onSelectListeners) {
            onSelectListener.select(ContactField.Type.VIBER);
        }
    }

    public void onWhatsAppBtnClicked(ActionEvent actionEvent) {
        for (OnSelectListener onSelectListener : onSelectListeners) {
            onSelectListener.select(ContactField.Type.WHATSAPP);
        }
    }

    public void onOtherContactsBtnClicked(ActionEvent actionEvent) {
        for (OnSelectListener onSelectListener : onSelectListeners) {
            onSelectListener.select(ContactField.Type.OTHER);
        }
    }
}
