package net.artux.transfelingo.Models.DictionaryResponses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Def {

@SerializedName("text")
@Expose
private String text;
@SerializedName("pos")
@Expose
private String pos;
@SerializedName("ts")
@Expose
private String ts;
@SerializedName("tr")
@Expose
private List<Tr> tr = null;

public String getText() {
return text;
}

public void setText(String text) {
this.text = text;
}

public String getPos() {
return pos;
}

public void setPos(String pos) {
this.pos = pos;
}

public String getTs() {
return ts;
}

public void setTs(String ts) {
this.ts = ts;
}

public List<Tr> getTr() {
return tr;
}

public void setTr(List<Tr> tr) {
this.tr = tr;
}

}