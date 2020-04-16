package com.devilsoftware.transfelingo.Models.DictionaryResponses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Syn {

@SerializedName("text")
@Expose
private String text;
@SerializedName("pos")
@Expose
private String pos;
@SerializedName("gen")
@Expose
private String gen;

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

public String getGen() {
return gen;
}

public void setGen(String gen) {
this.gen = gen;
}

}