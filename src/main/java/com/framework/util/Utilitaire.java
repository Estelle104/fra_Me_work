package com.framework.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.framework.annotation.UrlMapping;

public class Utilitaire {
    public static List<Class<?>> getClasses(String packages) throws URISyntaxException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();

        String path = packages.replace('.','/');

        // classloader permet de charger les classes
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        // getResource retourne une URL qui pointe vers le dossier ou se trouve les classes
        URL ressource = loader.getResource(path);

        // 
        if (ressource == null) {
            return classes;
        }

        // URL -> URI -> FIle
        File directory = new File(ressource.toURI());

        scan(packages, directory, classes);

        return classes;

    }

    public static void scan (String pack, File directory, List<Class<?>> classes) throws ClassNotFoundException {
        // parcourir le dossier
        for (File file : directory.listFiles()){
            if(file.isDirectory()) {
                scan(pack + "." + file.getName(), file, classes);
            }
            else if (file.getName().endsWith(".class")) {
                String className = pack + "." + file.getName().replace(".class", "");

                classes.add(Class.forName(className));
            }
        }   
    }

    public static void addInController(String pack, List<String> controllers) throws Exception {
        List<Class<?>> classes = getClasses(pack);
        
        for (Class<?> c : classes) {
            if (c.isAnnotationPresent(com.framework.annotation.Controller.class)) {
                controllers.add(c.getName());
            }
        }
    }

    public static HashMap<String, Mapping> getUrlMapping(String pack) throws Exception {
        List<Class<?>> classes = getClasses(pack);
        HashMap<String, Mapping> urlMapping = new HashMap<>();

        for (Class<?> c : classes) {
            if(c.isAnnotationPresent(com.framework.annotation.Controller.class)) {
                Method[] methode = c.getDeclaredMethods();

                for (Method m : methode){
                    if(m.isAnnotationPresent(com.framework.annotation.UrlMapping.class)){
                        UrlMapping url = m.getAnnotation(com.framework.annotation.UrlMapping.class);
                        
                        Mapping map = new Mapping();
                        map.setClasse(c.getName());
                        map.setMethode(m.getName());

                        urlMapping.put(url.value(), map);
                    }
                    else {
                        throw new Exception("La méthode " + m.getName() + " de la classe " + c.getName() + " n'est pas annotée avec @UrlMapping");
                    }
                }
            }
        }

        return urlMapping;
    }
}
