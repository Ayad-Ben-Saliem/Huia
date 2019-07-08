package ly.rqmana.huia.java.models;

import java.time.LocalDateTime;

public class IdentificationRecord {

    private long id;
    private LocalDateTime datetime;
    private boolean isIdentified;

    private User user;
    private Subscriber subscriber;

    private String notes;

    public long getId() {
        return id;
    }

    public String getStringId() {
        return Long.toHexString(getId()).toUpperCase();
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getDatetime() {
        return datetime;
    }

    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
    }

    public boolean isIdentified() {
        return isIdentified;
    }

    public void setIdentified(boolean identified) {
        isIdentified = identified;
    }

    // required for table view
    // ***** don't remove this method ****
    public boolean getStatus(){
        return isIdentified;
    }


    public void setSubscriber(Subscriber subscriber){
        this.subscriber = subscriber;
    }

    public Subscriber getSubscriber(){
        return this.subscriber;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSubscriberName(){
        if (subscriber != null)
            return this.subscriber.getFullName();
        else
            return "";
    }

    public String getSubscriberWorkId(){
        if (subscriber != null)
            return this.subscriber.getWorkId();
        else
            return "";
    }

    public String getProvidingUserName(){
        if (user != null)
            return this.user.getFullName();
        else
            return "";
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void addNote(String note) {
        if (getNotes() != null)
            note = getNotes().concat("\n").concat(note);
        setNotes(note);
    }
}
