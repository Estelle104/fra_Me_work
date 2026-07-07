package com.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.framework.util.Mapping;
import com.framework.util.UtilMethode;
import com.framework.util.Utilitaire;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

public class FrontControllerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        UtilMethode cle = new UtilMethode();
        cle.setUrl(request.getRequestURI().replace(request.getContextPath(), ""));
        cle.setMethode(request.getMethod());

        ServletContext context = getServletContext();

        HashMap<UtilMethode, Mapping> urlMapping = (HashMap<UtilMethode, Mapping>) context.getAttribute("urlMapping");

        Mapping map = urlMapping.get(cle);
        
        if (cle.getUrl().endsWith(".jsp")) {
            request.getRequestDispatcher(cle.getUrl()).forward(request, response);
            return;
        }

        if (map == null) {
            out.println("Route introuvable");

            out.println("<br>URL = " + cle.getUrl());
            out.println("<br>METHODE = " + cle.getMethode());

            out.println("<br><br>Routes disponibles :");

            for (UtilMethode k : urlMapping.keySet()) {
                out.println("<br>" + k.getMethode() + " " + k.getUrl());
            }

            return;
        }

        try {

            Class<?> clazz = Class.forName(map.getClasse());

            Object objet = clazz.getDeclaredConstructor().newInstance();

            Method methode = clazz.getDeclaredMethod(map.getMethode());

            Object retour = methode.invoke(objet);

            out.println("Retour : " + retour);

        } catch (Exception e) {
            throw new ServletException(e);
        }

        out.println("Methode : " + request.getMethod());
        out.println("Classe: " + map.getClasse());
        out.println("Methode: " + map.getMethode());
        out.println("URL : " + cle.getUrl());
    }

    // public List<String> getListController() {
    // return listController;
    // }

    // public void setListController(List<String> listController) {
    // this.listController = listController;
    // }

}
