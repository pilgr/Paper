package paperdb.io.paperdb;

import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Generated;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Keep;

@Entity
public class Note {
    @Id
    private long id;
    private String text;
    private Date date;

    @Keep
    public Note(String text, Date date) {
        this.text = text;
        this.date = date;
    }

    @Generated(hash = 1272611929)
    public Note() {
    }

    @Generated(hash = 1395965113)
    public Note(long id, String text, Date date) {
        this.id = id;
        this.text = text;
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public Date getDate() {
        return date;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setId(long id) {
        this.id = id;
    }
}
