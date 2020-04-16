package net.artux.transfelingo.Models;

/**
 * Created by Максим on 07.11.2017.
 * Transfelingo 2017.
 */

public class ModelAnswer {

    public String textFrom;
    public String textTo;
    public String saveTextTo;
    public String way;
    public int choice; // 1 = true; 0 = false;

    public ModelAnswer(String textFrom, String textTo, String way, int choice) {
        this.textFrom = textFrom;
        this.textTo = textTo;
        this.way = way;
        this.choice = choice;
        saveTextTo = textTo;
    }

    public ModelAnswer(String textFrom, String textTo,String saveTextTo, String way, int choice) {
        this.textFrom = textFrom;
        this.textTo = textTo;
        this.way = way;
        this.choice = choice;
        this.saveTextTo = saveTextTo;
    }

}
