package fr.alkanife.dbtohtml;

public class Tour {

    private String author;
    private String date;
    private String message;

    public Tour(String author, String date, String message) {
        this.author = author;
        this.date = date;
        this.message = message;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
