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
    List<String> listController = new ArrayList<>();

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

    @Override
    public void init() throws ServletException {
        try {
            ServletContext context = getServletContext();
            String pack = context.getInitParameter("controller");
            
            System.out.println("===============");
            System.out.println("package: " + pack);

            CheckController.getControllers(pack);
        } catch (Exception e) {
            throw new ServletException("Error initializing FrontControllerServlet", e);
        }
    }

}
