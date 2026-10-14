package com.framework.util;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import jakarta.servlet.http.HttpServletRequest;

public class ParamBinder {

    /**
     * Point d'entrée du binder.
     *
     * Exemple :
     * @Param("etudiant") Etudiant etudiant
     *
     * prefixe = "etudiant"
     * parametre = le paramètre Java de la méthode du Controller
     */
    public Object resoudre( HttpServletRequest request,String prefixe,java.lang.reflect.Parameter parametre) throws Exception {

        return construireValeur(request,prefixe,parametre.getType(),parametre.getParameterizedType()
        );
    }

    /**
     * Construit une valeur en fonction du type attendu.
     */
    private Object construireValeur(HttpServletRequest request,String prefixe,Class<?> type, Type typeGenerique) throws Exception {

        // Type simple 
        if (Utilitaire.estTypeSimple(type)) {
            String valeur = request.getParameter(prefixe);

            return Utilitaire.convertirParametre(valeur, type);
        }

        // Tableau : non pris en charge pour le moment.
        if (type.isArray() || typeGenerique instanceof GenericArrayType) {
            throw new IllegalArgumentException(
                    "Les tableaux ne sont pas encore pris en charge : " + type.getName()
            );
        }

        // Liste : List<String>, List<Etudiant>, etc.
        if (List.class.isAssignableFrom(type)) {
            if (!(typeGenerique instanceof ParameterizedType)) {
                throw new IllegalArgumentException(
                        "Le type de la liste doit être précisé : " + prefixe + ". Utilise par exemple List<Etudiant>."
                );
            }

            ParameterizedType paramType = (ParameterizedType) typeGenerique;

            Type typeElement = paramType.getActualTypeArguments()[0];

            return construireListe(request,prefixe, typeElement);
        }

        // Objet Java : Etudiant, Personne, Adresse...
        return construireObjet(request,prefixe,type);
    }

    //  Construit une liste à partir des indices reçus.
    private List<Object> construireListe(HttpServletRequest request,String prefixe, Type typeElement) throws Exception {

        Class<?> classeElement = obtenirClasse(typeElement);

        TreeSet<Integer> indices = new TreeSet<>();

        Map<String, String[]> parametres = request.getParameterMap();

        String debut = prefixe + "[";

        // recherche des indice dans les param de la req
        for (String nom : parametres.keySet()) {
            if (!nom.startsWith(debut)) {
                continue;
            }

            int fin = nom.indexOf(']', debut.length());

            if (fin == -1) {
                continue;
            }

            String indiceTexte =
                    nom.substring(debut.length(), fin);

            try {

                int indice = Integer.parseInt(indiceTexte);

                if (indice < 0) {
                    throw new IllegalArgumentException( "Indice de liste négatif : " + nom);
                }

                indices.add(indice);

            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Indice de liste invalide : " + nom,e);
            }
        }

        List<Object> resultat = new ArrayList<>();

        if (indices.isEmpty()) {
            return resultat;
        }

        // doit etre continu (0,1,2,...)
        int attendu = 0;

        for (int indice : indices) {
            if (indice != attendu) {
                throw new IllegalArgumentException("Indices de liste discontinus pour "+ prefixe+ ". Indice attendu : "+ attendu);
            }
            attendu++;
        }

        for (int indice : indices) {
            String prefixeElement =prefixe + "[" + indice + "]";

            Object element = construireValeur(request,prefixeElement,classeElement,typeElement);

            resultat.add(element);
        }

        return resultat;
    }

    
    //Det la classe d'un element de liste
    private Class<?> obtenirClasse(Type type) {

        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;

            Type typeBrut = paramType.getRawType();

            if (typeBrut instanceof Class<?>) {
                return (Class<?>) typeBrut;
            }
        }

        throw new IllegalArgumentException(
                "Type générique non pris en charge : " + type.getTypeName()
        );
    }

    
    //Construit un objet et remplit ses proprietes.
    private Object construireObjet(HttpServletRequest request,String prefixe,Class<?> type) throws Exception {

        if (!contientDonnees(request, prefixe)) {
            return null;
        }

        verifierTypeObjet(type);

        Object objet = type.getDeclaredConstructor().newInstance();

        for (Field champ : obtenirChamps(type)) {

            // Ignore les champs statiques et techniques.
            if (Modifier.isStatic(champ.getModifiers()) || champ.isSynthetic()) {
                continue;
            }

            String nomChamp = champ.getName();

            String prefixeChamp =prefixe + "." + nomChamp;

            Class<?> typeChamp = champ.getType();

            Type typeGeneriqueChamp = champ.getGenericType();

            // Ignore les propriétés sans données correspondantes.
            if (!contientDonnees(request, prefixeChamp)) {
                continue;
            }

            Object valeur = construireValeur(request,prefixeChamp,typeChamp,typeGeneriqueChamp);

            // N'affecte pas null à un champ primitif.
            if (valeur == null && typeChamp.isPrimitive()) {
                continue;
            }

            affecterPropriete(objet,champ,valeur);
        }

        return objet;
    }

    
    //Récupère les champs déclarés dans la classe et dans ses classes parentes.
    
    private List<Field> obtenirChamps(Class<?> type) {

        List<Field> champs = new ArrayList<>();

        Class<?> courant = type;

        while (courant != null && courant != Object.class) {
            for (Field champ : courant.getDeclaredFields()) {
                champs.add(champ);
            }
            courant = courant.getSuperclass();
        }

        return champs;
    }

    
    // Vérifie qu'une classe peut être instanciée.
    private void verifierTypeObjet(Class<?> type) {

        if (type.isInterface() || Modifier.isAbstract(type.getModifiers()) || type.isEnum()) {
            throw new IllegalArgumentException(
                    "Impossible de construire automatiquement : " + type.getName()
            );
        }

        if (type.getName().startsWith("java.")) {
            throw new IllegalArgumentException(
                    "Cette classe Java ne peut pas être construite "+ "comme objet de formulaire : " + type.getName()
            );
        }
    }

    // Vérifie si une donnée existe pour un préfixe.
    private boolean contientDonnees(HttpServletRequest request,String prefixe) {

        Map<String, String[]> parametres =request.getParameterMap();

        if (parametres.containsKey(prefixe)) {
            return true;
        }

        String debutObjet = prefixe + ".";
        String debutListe = prefixe + "[";

        for (String nom : parametres.keySet()) {
            if (nom.startsWith(debutObjet) || nom.startsWith(debutListe)) {
                return true;
            }
        }

        return false;
    }

    // Affecte une propriété à l'aide de son setter.
    private void affecterPropriete(Object objet,Field champ,Object valeur) throws Exception {
        String nomChamp = champ.getName();

        String nomSetter ="set"+ Character.toUpperCase(nomChamp.charAt(0))+ nomChamp.substring(1);

        Method setter = null;

        // Recherche du setter correspondant au type exact.
        try {
            setter = objet.getClass().getMethod(nomSetter,champ.getType());

        } catch (NoSuchMethodException e) {

            // Recherche d'un setter compatible avec le type du champ
            throw new IllegalArgumentException(
                    "Setter public introuvable : "+ objet.getClass().getName()+ "."+ nomSetter+ "("+ champ.getType().getSimpleName() + ")", e
            );
        }

        try {

            setter.invoke(objet, valeur);

        } catch (InvocationTargetException e) {

            Throwable cause = e.getCause();

            throw new IllegalArgumentException("Erreur pendant l'affectation de la propriété "+ champ.getName(), cause);
        }
    }
}