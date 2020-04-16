package com.devilsoftware.transfelingo.Models.DictionaryResponses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Tr_ {

@SerializedName("text")
@Expose
private String text;

public String getText() {
return text;
}

public void setText(String text) {
this.text = text;
}

}