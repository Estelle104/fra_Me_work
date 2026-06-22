package com.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

public class FrontControllerServlet extends HttpServlet {
<<<<<<< Updated upstream
=======
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

        } catch (Exception e) {
            throw new ServletException(e);
        }

    }

>>>>>>> Stashed changes
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        captureUrl(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        captureUrl(request, response);
    }

    private void captureUrl(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        String url = request.getRequestURI();

        out.println(url);
    }

}
