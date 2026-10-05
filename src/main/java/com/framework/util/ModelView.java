package com.framework.util;

import java.util.HashMap;

public class ModelView {
    String view; // Nom de la vue (fichier JSP) à afficher
    HashMap<String, Object> attribute = new HashMap<>(); // Attributs à passer à la vue (fichier JSP)

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public HashMap<String, Object> getAttribute() {
        return attribute;
    }

    public void setAttribute(String key, Object value) {
        this.attribute.put(key, value);
    }
    
}
