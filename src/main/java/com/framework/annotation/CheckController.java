package com.framework.annotation;

import java.util.ArrayList;
import java.util.List;

import com.framework.util.Utilitaire;


public class CheckController {
    public static void getControllers(String pack) throws Exception {
        List<Class<?>> classes = Utilitaire.getClasses(pack);
        for (Class<?> c : classes) {
            if (c.isAnnotationPresent(Controller.class)) {
                System.out.println(c.getName());
            }
        }
    }

    public List<Class<?>> getListController(String pack) throws Exception {
        List<Class<?>> controllers = new ArrayList<>();
        List<Class<?>> classes = Utilitaire.getClasses(pack);
        for (Class<?> c : classes) {
            if (c.isAnnotationPresent(Controller.class)) {
                controllers.add(c);
            }
        }
        return controllers;
    }

    public List<Class<?>> getListController() throws Exception {
        return getListController("controller");
    }
} 