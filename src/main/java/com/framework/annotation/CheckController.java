package com.framework.annotation;

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
} 