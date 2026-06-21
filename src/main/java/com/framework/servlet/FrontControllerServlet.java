package com.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.framework.annotation.CheckController;
import com.framework.util.Utilitaire;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

public class FrontControllerServlet extends HttpServlet {
    private List<String> listController = new ArrayList<>();

    @Override
    public void init() throws ServletException {

        try {

            System.out.println("=== FRAMEWORK INIT ===");
            System.out.println("############################");
            System.out.println("VERSION FRAMEWORK 999999");
            System.out.println("############################");

            ServletContext context = getServletContext();

            String pack = context.getInitParameter("controller");

            System.out.println("Package = " + pack);

            List<Class<?>> classes = Utilitaire.getClasses(pack);

            System.out.println("Nombre classes = "+ classes.size());

            for (Class<?> c : classes) {

                System.out.println("Classe trouvée = "+ c.getName());

                System.out.println("Controller ? "+ c.isAnnotationPresent(com.framework.annotation.Controller.class));

                if (c.isAnnotationPresent(com.framework.annotation.Controller.class)) {
                    listController.add(c.getName());
                }
            }

            for (Class<?> c : classes) {
                if (c.isAnnotationPresent(com.framework.annotation.Controller.class)) {
                    listController.add(c.getName());
                }
            }

            System.out.println("Controllers trouvés : " + listController.size());

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

        out.println("wertyuiop");
        String url = request.getRequestURI();

        out.println(url);

        for (String string : listController) {
            out.println(string);
        }
    }

    public List<String> getListController() {
        return listController;
    }

    public void setListController(List<String> listController) {
        this.listController = listController;
    }

}
