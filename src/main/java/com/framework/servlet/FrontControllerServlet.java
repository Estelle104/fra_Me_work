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


            ServletContext context = getServletContext();

            String pack = context.getInitParameter("controller");

            List<Class<?>> classes = Utilitaire.getClasses(pack);

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
