package ly.rqmana.huia.java.models;

import java.time.LocalDateTime;

public class SubscriberIdentification {

    private long id;
    private LocalDateTime dateTime;
    private boolean isIdentified;

    private User user;
    private Subscriber subscriber;

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

    public long getSubscriberId() {
        return this.subscriber.getId();
    }

    public String getSubscriberWorkId(){
        return this.subscriber.getWorkId();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean getStatus(){
        return isIdentified;
    }
}
