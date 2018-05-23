package ru.track.prefork;

import java.io.Serializable;

public class Message implements Serializable {
    private long ts;
    private String data;

    public Message(long ts, String data) {
        this.ts = ts;
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void PutData(String data){
        this.data = data;
    }

    public long GetTs(){
        return ts;
    }
}
//documentation about ts will be uploaded to ApiReadme
