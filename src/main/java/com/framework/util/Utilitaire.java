package com.framework.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;

import com.framework.annotation.UrlMapping;
import com.framework.util.Mapping;

import jakarta.servlet.http.HttpServletRequest;

public class Utilitaire {
    private static final Gson GSON = new Gson();

    public static List<Class<?>> getClasses(String packages) throws URISyntaxException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();

        String path = packages.replace('.', '/');

        // classloader permet de charger les classes
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        // getResource retourne une URL qui pointe vers le dossier ou se trouve les
        // classes
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

    public static void scan(String pack, File directory, List<Class<?>> classes) throws ClassNotFoundException {
        // parcourir le dossier
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                scan(pack + "." + file.getName(), file, classes);
            } else if (file.getName().endsWith(".class")) {
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
            if (c.isAnnotationPresent(com.framework.annotation.Controller.class)) {
                Method[] methode = c.getDeclaredMethods();

                for (Method m : methode) {
                    if (m.isAnnotationPresent(com.framework.annotation.UrlMapping.class)) {
                        UrlMapping url = m.getAnnotation(com.framework.annotation.UrlMapping.class);

                        Mapping map = new Mapping();
                        map.setClasse(c.getName());
                        map.setMethode(m.getName());

                        urlMapping.put(url.value(), map);
                    }
                    // else {
                    // throw new Exception("La méthode " + m.getName() + " de la classe " +
                    // c.getName() + " n'est pas annotée avec @UrlMapping");
                    // }
                }
            }
        }

        return urlMapping;
    }

    public static void getUrlAndMethod(String pack, HashMap<UtilMethode, Mapping> urlMapping) throws Exception {

        List<Class<?>> classes = getClasses(pack);

        for (Class<?> c : classes) {
            if (c.isAnnotationPresent(com.framework.annotation.Controller.class)) {
                Method[] methode = c.getDeclaredMethods();

                for (Method m : methode) {
                    if (m.isAnnotationPresent(com.framework.annotation.UrlMapping.class)) {
                        UrlMapping url = m.getAnnotation(com.framework.annotation.UrlMapping.class);

                        UtilMethode utilMethode = new UtilMethode();
                        utilMethode.setUrl(url.value());
                        utilMethode.setMethode(url.method());

                        Mapping map = new Mapping();
                        map.setClasse(c.getName());
                        map.setMethode(m.getName());

                        if (urlMapping.containsKey(utilMethode)) {
                            throw new Exception("Route deja definie : " + url.value() + " " + url.method());
                        }

                        urlMapping.put(utilMethode, map);
                    }
                }
            }
        }
    }

    public static String detectVue(Object retour, String prefixe, String suffixe) {
        if (retour == null) {
            return null;
        }
        if (retour instanceof String) {
            return prefixe + (String) retour + suffixe;
        }

        if (retour instanceof ModelView) {
            ModelView mv = (ModelView) retour;
            return prefixe + mv.getView() + suffixe;
        }

        return null;
    }

    public static void addAttribute(HttpServletRequest request, ModelView mv) {

        for (HashMap.Entry<String, Object> entry : mv.getAttribute().entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }

    }

    // dans Utilitaire

    public static String toJson(Object o) {
        return GSON.toJson(o);
    }

    // convertir un param en fonction de son type
    public static Object convertirParametre(String valeur, Class<?> type) {

        // Cas d'une valeur absente
        if (valeur == null) {

            if (type.isPrimitive()) {
                throw new IllegalArgumentException(
                        "Impossible d'affecter null au type primitif : "
                                + type.getName());
            }

            return null;
        }

        // Chaîne de caractères
        if (type == String.class) {
            return valeur;
        }

        // Types numériques
        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(valeur);
        }

        if (type == long.class || type == Long.class) {
            return Long.parseLong(valeur);
        }

        if (type == double.class || type == Double.class) {
            return Double.parseDouble(valeur);
        }

        if (type == float.class || type == Float.class) {
            return Float.parseFloat(valeur);
        }

        if (type == short.class || type == Short.class) {
            return Short.parseShort(valeur);
        }

        if (type == byte.class || type == Byte.class) {
            return Byte.parseByte(valeur);
        }

        // Type bool
        if (type == boolean.class || type == Boolean.class) {
            if (!valeur.equalsIgnoreCase("true") && !valeur.equalsIgnoreCase("false")) {
                throw new IllegalArgumentException( "Valeur bool invalide : " + valeur);
            }
            return Boolean.parseBoolean(valeur);
        }

        // caractere
        if (type == char.class || type == Character.class) {
            if (valeur.length() != 1) {
                throw new IllegalArgumentException( "Un caractère est attendu, valeur reçue : " + valeur);
            }
            return valeur.charAt(0);
        }

        // Enum
        if (type.isEnum()) {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            Object resultat = Enum.valueOf((Class<? extends Enum>) type, valeur);

            return resultat;
        }

        throw new IllegalArgumentException( "Type simple non supporté : " + type.getName());
    }

    public static boolean estTypeSimple(Class<?> type) {

        return type == String.class
                || type == int.class || type == Integer.class
                || type == long.class || type == Long.class
                || type == double.class || type == Double.class
                || type == float.class || type == Float.class
                || type == boolean.class || type == Boolean.class
                || type == short.class || type == Short.class
                || type == byte.class || type == Byte.class
                || type == char.class || type == Character.class
                || type.isEnum();
    }
}