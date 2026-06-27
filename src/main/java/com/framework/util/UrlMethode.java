package com.framework.util;

public class UrlMethode {
    String url;
    String methode;


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UrlMethode)) {
            return false;
        }

        UrlMethode um = (UrlMethode) obj;
        
        return url.equals(um.getUrl()) && methode.equals(um.getMethode()); 
    }


    @Override
    public int hashCode(){
        

        return 0;
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
