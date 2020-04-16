package net.artux.transfelingo.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by maxim on 01.03.18.
 */
public class Lang {

    @SerializedName("dirs")
    @Expose
    private List<String> dirs = null;
    @SerializedName("langs")
    @Expose
    private Langs langs;

    public List<String> getDirs() {
        return dirs;
    }

    public void setDirs(List<String> dirs) {
        this.dirs = dirs;
    }

    public Langs getLangs() {
        return langs;
    }

    public void setLangs(Langs langs) {
        this.langs = langs;
    }

}
