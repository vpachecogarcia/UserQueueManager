package victor.pacheco.userqueuemanager;

public class Queue {


    private String id;
    private String queue_name;
    private String current_user;
    private Integer slot_time, hour, min, numuser;

    Queue() {}

    public Queue(String queue_name, Integer slot_time, Integer hour, Integer min, Integer numuser, String current_user) {
        this.queue_name = queue_name;
        this.slot_time = slot_time;
        this.hour = hour;
        this.min = min;
        this.numuser=numuser;
        this.current_user = current_user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQueue_name() {
        return queue_name;
    }

    public void setQueue_name(String queue_name) {
        this.queue_name = queue_name;
    }

    public Integer getSlot_time() {
        return slot_time;
    }

    public void setSlot_time(Integer slot_time) {
        this.slot_time = slot_time;
    }

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getNumuser() {
        return numuser;
    }

    public void setNumuser(Integer numuser) {
        this.numuser = numuser;
    }

    public String getCurrent_user() {
        return current_user;
    }

    public void setCurrent_user(String current_user) {
        this.current_user = current_user;
    }

}

