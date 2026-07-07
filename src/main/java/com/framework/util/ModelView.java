package com.framework.util;

import java.util.HashMap;

public class ModelView {
    String view;
    HashMap<String, Object> attribute = new HashMap<>();

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
