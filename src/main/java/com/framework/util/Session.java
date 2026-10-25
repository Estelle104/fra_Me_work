package com.framework.util;

import java.util.Map;

public class Session {
    String id;
    Map<String, Object> variables;

    public  Session(Map<String, Object> variables, String id) {
        this.variables = variables;
        this.id = id;
    }

    public Session(String id, Map<String, Object> variables) {
        this.id = id;
        this.variables = variables;
    }

    public void ajouterVariable(String nom, Object valeur) {
        variables.put(nom, valeur);
    }

    public void supprimerVariable(String nom) {
        variables.remove(nom);
    }

    public Object getVariable(String nom) {
        return variables.get(nom);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public boolean hasVariable(String nom) {
        return variables.containsKey(nom);
    }


}
