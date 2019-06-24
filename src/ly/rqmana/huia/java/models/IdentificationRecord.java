package ly.rqmana.huia.java.models;

import java.time.LocalDateTime;

public class IdentificationRecord {

    private long id;
    private LocalDateTime dateTime;
    private boolean isIdentified;

    private User user;
    private Subscriber subscriber;

    private String notes;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
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
