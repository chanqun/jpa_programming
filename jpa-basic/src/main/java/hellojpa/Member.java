package hellojpa;

import javax.persistence.*;
import java.util.Date;

//@Entity
public class Member {

    public Member() {

    }

    @Id
    private String id;

    @Column(name = "name")
    private String userName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
