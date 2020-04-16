package com.devilsoftware.transfelingo.Models.DictionaryResponses;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Ex {

@SerializedName("text")
@Expose
private String text;
@SerializedName("tr")
@Expose
private List<Tr_> tr = null;

public String getText() {
return text;
}

public void setText(String text) {
this.text = text;
}

public List<Tr_> getTr() {
return tr;
}

public void setTr(List<Tr_> tr) {
this.tr = tr;
}

}