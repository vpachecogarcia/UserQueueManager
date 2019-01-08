package victor.pacheco.userqueuemanager;

import java.util.Date;

public class User {

    private String usr_id;
    private int waiting_time;
    private boolean state = false;
    private Date acces_time;
    private Integer usr_pos;

    User(){}
    public User(String usr_id, int waiting_time, boolean state, Date acces_time, Integer usr_pos) {
        this.usr_id = usr_id;
        this.waiting_time = waiting_time;
        this.state = state;
        this.acces_time = acces_time;
        this.usr_pos = usr_pos;

    }

    public int getWaiting_time() {
        return waiting_time;
    }

    public void setWaiting_time(int waiting_time) {
        this.waiting_time = waiting_time;
    }

    public String getUsr_id() {
        return usr_id;
    }

    public void setUsr_id(String usr_id) {
        this.usr_id = usr_id;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public boolean isState() {
        return state;
    }


    public Date getAcces_time() {
        return acces_time;
    }

    public void setAcces_time(Date acces_time) {
        this.acces_time = acces_time;
    }

    public Integer getUsr_pos() {
        return usr_pos;
    }

    public void setUsr_pos(Integer usr_pos) {
        this.usr_pos = usr_pos;
    }
}
