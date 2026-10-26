package com.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.framework.annotation.Json;
import com.framework.annotation.Param;
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
                throw new ServletException( "Le contexte Spring n'est pas initialise");
            }

            Object objet = spring.getBean(clazz);

            // 4. Rechercher la méthode d'action
            Method methode = trouverMethode(clazz, map.getMethode());

            // 5. Récupérer les paramètres de la méthode
            Parameter[] parameters = methode.getParameters();

            Object[] valeurs = new Object[parameters.length];

            // 6. Préparer la session si la méthode en a besoin
            Session session = null;
            HttpSession httpSession = null;

            boolean utiliseSession = false;

            for (Parameter parameter : parameters) {
                if (parameter.getType().equals(Session.class)) {
                    utiliseSession = true;
                    break;
                }
            }

            if (utiliseSession) {
                httpSession = request.getSession();

                session = recupererSession(httpSession);
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
                        throw new ServletException("Le parametre '"+ parameter.getName()+ "' doit avoir l'annotation @Param");
                    }

                    Param param = parameter.getAnnotation(Param.class);
                    String nomParam = param.value();

                    valeurs[i] = binder.resoudre(request,nomParam,parameter);
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

                throw new ServletException("Erreur dans le controleur : " + cause.getMessage(),cause);
            }

            // 9. Synchroniser Session vers HttpSession
            if (utiliseSession) {
                synchroniserSession(session, httpSession);
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

            String vue = Utilitaire.detectVue(retour,prefixe,suffixe);

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

    /**
     * Recherche une méthode d'action compatible avec les paramètres
     * pris en charge par le framework.
     *
     * Un paramètre doit être de type Session ou posséder @Param.
     */
    private Method trouverMethode(Class<?> clazz, String nomMethode) throws ServletException {

        Method methodeSansParametre = null;

        for (Method m : clazz.getDeclaredMethods()) {
            if (!m.getName().equals(nomMethode)) {
                continue;
            }

            Parameter[] parameters = m.getParameters();

            // Priorité à la méthode sans paramètre
            if (parameters.length == 0) {

                methodeSansParametre = m;
                continue;
            }

            boolean methodeValide = true;

            for (Parameter parameter : parameters) {

                // Session est injectée automatiquement
                if (parameter.getType().equals(Session.class)) {
                    continue;
                }

                // Les autres paramètres nécessitent @Param
                if (!parameter.isAnnotationPresent(Param.class)) {
                    methodeValide = false;
                    break;
                }
            }

            if (methodeValide) {
                return m;
            }
        }

        if (methodeSansParametre != null) {
            return methodeSansParametre;
        }

        throw new ServletException("Methode introuvable ou parametres invalides : "+ nomMethode);
    }

    /**
     * Récupère l'instance Session existante.
     * Si elle n'existe pas, la crée à partir des attributs HTTP.
     */
    private Session recupererSession(HttpSession httpSession) {

        Session session = (Session) httpSession.getAttribute(SESSION_ATTRIBUTE);

        if (session != null) {
            return session;
        }

        Map<String, Object> variables = new HashMap<>();

        Enumeration<String> noms = httpSession.getAttributeNames();

        while (noms.hasMoreElements()) {
            String nom = noms.nextElement();

            if (!SESSION_ATTRIBUTE.equals(nom)) {
                variables.put(nom, httpSession.getAttribute(nom));
            }
        }

        session = new Session(variables, httpSession.getId());

        httpSession.setAttribute(SESSION_ATTRIBUTE, session);

        return session;
    }

    /**
     * Synchronise les variables de Session vers HttpSession.
     * Les variables supprimées de la Map sont également supprimées
     * de HttpSession, sauf l'attribut interne du framework.
     */
    private void synchroniserSession(Session session, HttpSession httpSession) {

        Map<String, Object> variables = session.getVariables();

        // Supprimer les attributs qui ne sont plus dans la Map
        Enumeration<String> noms = httpSession.getAttributeNames();

        while (noms.hasMoreElements()) {

            String nom = noms.nextElement();

            if (SESSION_ATTRIBUTE.equals(nom)) {
                continue;
            }

            if (!variables.containsKey(nom)) {
                httpSession.removeAttribute(nom);
            }
        }

        // Ajouter ou actualiser les variables
        for (Map.Entry<String, Object> entree : variables.entrySet()) {
            httpSession.setAttribute(entree.getKey(), entree.getValue());
        }
    }
}