package com.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.framework.annotation.AuthRequired;
import com.framework.annotation.Json;
import com.framework.annotation.Param;
import com.framework.annotation.RoleRequired;
import com.framework.util.Mapping;
import com.framework.util.ModelView;
import com.framework.util.ParamBinder;
import com.framework.util.Session;
import com.framework.util.UtilMethode;
import com.framework.util.Utilitaire;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.context.ApplicationContext;

public class FrontControllerServlet extends HttpServlet {

    private static final String SESSION_ATTRIBUTE = "frameworkSession";

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

        // 1. Identifier la requête
        UtilMethode cle = new UtilMethode();

        cle.setUrl(
                request.getRequestURI().replace(request.getContextPath(), ""));

        cle.setMethode(request.getMethod());

        ServletContext context = getServletContext();

        // String userKey = context.getInitParameter("sessionUserKey");

        @SuppressWarnings("unchecked")
        HashMap<UtilMethode, Mapping> urlMapping = (HashMap<UtilMethode, Mapping>) context.getAttribute("urlMapping");

        if (urlMapping == null) {
            throw new ServletException("urlMapping non initialise");
        }

        // 2. Rechercher le mapping correspondant à la requête
        Mapping map = urlMapping.get(cle);

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

            // 3. Récupérer le contrôleur
            Class<?> clazz = Class.forName(map.getClasse());

            ApplicationContext spring = (ApplicationContext) context.getAttribute("springContext");

            if (spring == null) {
                throw new ServletException("Le contexte Spring n'est pas initialise");
            }

            Object objet = spring.getBean(clazz);

            // 4. Rechercher la méthode d'action
            Method methode = Utilitaire.trouverMethode(clazz, map.getMethode());

            // 5. Récupérer les paramètres de la méthode
            Parameter[] parameters = methode.getParameters();

            Object[] valeurs = new Object[parameters.length];

            // 6. Préparer la session et vérifier l'authentification

            Session session = null;
            HttpSession httpSession = null;

            boolean utiliseSession = false;
            boolean authRequise = methode.isAnnotationPresent(AuthRequired.class);

            // Vérifier si la méthode utilise notre Session personnalisée
            for (Parameter parameter : parameters) {
                if (parameter.getType().equals(Session.class)) {
                    utiliseSession = true;
                    break;
                }
            }

            // Récupérer la session si la méthode en a besoin
            if (utiliseSession || authRequise) {

                // Si la méthode nécessite une authentification,
                // ne pas créer une nouvelle session HTTP vide.
                httpSession = request.getSession(!authRequise);

                if (httpSession != null) {
                    session = Utilitaire.recupererSession(
                            httpSession,
                            SESSION_ATTRIBUTE);
                }
            }

            // Vérifier l'authentification APRÈS avoir récupéré la session
            if (authRequise) {

                AuthRequired auth = methode.getAnnotation(AuthRequired.class);

                String userKey = context.getInitParameter("sessionUserKey");

                if (!Utilitaire.estConnecte(session, userKey)) {

                    response.sendRedirect(
                            request.getContextPath() + auth.loginUrl());

                    return;
                }
            }

            // Vérifier les autorisations par rôle
            if (methode.isAnnotationPresent(RoleRequired.class)) {

                RoleRequired roleRequired = methode.getAnnotation(RoleRequired.class);

                String roleKey = context.getInitParameter("sessionRoleKey");

                if (!Utilitaire.aRole(session, roleKey, roleRequired.value())) {

                    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                            "Vous n'avez pas les autorisations nécessaires.");

                    return;
                }
            }

            // 7. Préparer les arguments de la méthode
            ParamBinder binder = new ParamBinder();

            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];

                // Cas A : injection automatique de Session
                if (parameter.getType().equals(Session.class)) {
                    valeurs[i] = session;

                } else {
                    // Cas B : paramètre ordinaire avec @Param
                    if (!parameter.isAnnotationPresent(Param.class)) {
                        throw new ServletException(
                                "Le parametre '" + parameter.getName() + "' doit avoir l'annotation @Param");
                    }

                    Param param = parameter.getAnnotation(Param.class);
                    String nomParam = param.value();

                    valeurs[i] = binder.resoudre(request, nomParam, parameter);
                }
            }

            // 8. Invoquer la méthode du contrôleur
            Object retour;

            try {
                retour = methode.invoke(objet, valeurs);

            } catch (InvocationTargetException e) {
                Throwable cause = e.getTargetException();

                System.err.println("===== ERREUR DANS LE CONTROLEUR =====");

                cause.printStackTrace();

                throw new ServletException("Erreur dans le controleur : " + cause.getMessage(), cause);
            }

            // 9. Synchroniser Session vers HttpSession
            if (utiliseSession) {
                Utilitaire.synchroniserSession(session, httpSession, SESSION_ATTRIBUTE);
            }

            // 10. Traiter les méthodes JSON
            if (methode.isAnnotationPresent(Json.class)) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                String json;

                if (retour instanceof ModelView) {
                    json = Utilitaire.toJson(((ModelView) retour).getAttribute());

                } else {
                    json = Utilitaire.toJson(retour);
                }
                response.getWriter().print(json);

                return;
            }

            // 11. Déterminer la vue
            String prefixe = (String) context.getAttribute("viewPrefix");
            String suffixe = (String) context.getAttribute("viewSuffix");

            String vue = Utilitaire.detectVue(retour, prefixe, suffixe);

            // 12. Ajouter les attributs du ModelView à la requête
            if (retour instanceof ModelView) {
                ModelView mv = (ModelView) retour;

                Utilitaire.addAttribute(request, mv);
            }

            // 13. Afficher la vue
            if (vue != null) {
                request.getRequestDispatcher(vue).forward(request, response);

                return;
            }

            out.println("Retour : " + retour);

        } catch (ServletException e) {

            throw e;

        } catch (Exception e) {

            throw new ServletException(e);
        }
    }

}