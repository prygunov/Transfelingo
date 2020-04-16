package net.artux.transfelingo;

/**
 * Created by Максим on 24.10.2017.
 * Translingo 2017.
 */

public class ItemHistory {

    String textFrom;
    String textTo;
    int choice;
    String direction;
    public int id;

    public ItemHistory(String textFrom, String textTo, String direction, int choice, int id){
        this.textFrom = textFrom;
        this.textTo = textTo;
        this.direction = direction;
        this.choice = choice;
        this.id = id;
    }
}
