package com.framework.util;

import java.util.Objects;

public class UtilMethode {
    String url;
    String methode;


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UtilMethode)) {
            return false;
        }

        UtilMethode um = (UtilMethode) obj;
        
        return url.equals(um.getUrl()) && methode.equals(um.getMethode()); 
    }


    @Override
    public int hashCode(){
        return Objects.hash(url, methode.toUpperCase());
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethode() {
        return methode;
    }

    public void setMethode(String methode) {
        this.methode = methode;
    }
}
