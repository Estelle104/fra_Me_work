package com.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.framework.util.Mapping;
import com.framework.util.Utilitaire;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

public class FrontControllerServlet extends HttpServlet {
    private List<String> listController = new ArrayList<>();
    private HashMap<String, Mapping> urlMapping = new HashMap<>();

    @Override
    public void init() throws ServletException {

        try {

            ServletContext context = getServletContext();

            String pack = context.getInitParameter("controller");

            Utilitaire.addInController(pack, listController);

            System.out.println("Controllers trouvés : " + listController.size());

            this.urlMapping = Utilitaire.getUrlMapping(pack);

        } catch (Exception e) {
            throw new ServletException(e);
        }

    }

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

        String url = request.getRequestURI();

        out.println(url);

        for (String string : listController) {
            out.println(string);
        }

        for (String urlKey : urlMapping.keySet()) {

            Mapping map = urlMapping.get(urlKey);

            out.println("URL: " + urlKey + " | Classe: "+ map.getClasse() + " | methode: " + map.getMethode());

        }
    }

    public List<String> getListController() {
        return listController;
    }

    public void setListController(List<String> listController) {
        this.listController = listController;
    }

}
