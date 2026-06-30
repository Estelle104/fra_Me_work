# FRAMEWORK
## But:
- Faire SpringMVC par moi-meme
- Travail dirige
- Fonctionnalite par sprint
- ON aura 2 espace de travail
    - le framework renvoie jar
    - le jar sera dans le classpath dans le dossier de test 
- projet par groupe 
- decomposer un probleme

## Travailler sur git
- main  
    - branch : Sprint 0
        - commits
    - pull request
        - delete branch apres chaque pull request    

## Sprint #0 (fin: Dimanche 14/06/26)
- Creer un servlet
    - FrontCOntrollerServlet
        - DoPost
        - DoGet
            - appelle processRequest
            - out.println(URL)
    - Tous les requetes doivent asser par ce servlet
- l'exporter en .jar
- l'envoyer dans le dossier de test
    - declarer dans le fichier de configuration le servlet web.xml
        - declarer le servlet
        - y mettre le chemin du servlet


**12-06-26**
- On va avoir un controller annote @Controller 
    - a un methode annotee (ex:@GetMapping)
        - 

- Il faut que je sache creer et recuperer et tester si mon annotation marche bien
    - l'annotation peut etre dans une classe, dans un attribut, ou dans une methode
    - il faut definir les variables que l'on peut declarer dans une 
    - je dois le creer
    - il ressemble a une annotation

- Creer les annotations
    - Son nom
    - Avoir des valeurs des attributs
    - Quelles sont les variables que l'on peut utiliser
    - Le tester dans le dossier test

## Sprint #1

Au niveau framework:
- mg:itu.annotation.Controller
    trouver les controllers en parcourant toutes les classes du classpath
    faire par package (car dans un fw, il y a un regle -> adapter mon fw pour que ca soit facile a utiliser)

Au niveau test
- @Controller
TestController

ON doit avoir un code qui sera lance soit :
- demarrage de l'application web (meme sans appel)
    - utiliser listener
- 1er appel du FrontServlet
    - utiliser init

Le code :
But : ON doit savoir tous les controllers

- Ajouter un attribut dans FrontServlet 
    List<String> listCOntroller
    init(){
        parcours toutes les classes de l'application
        regarde s'il y a un controller
            si oui
                ajouter dans listController
                l'afficher
        recuperer le parametre dans web.xml

    }

- on doit avoir un methode pour chercher si une annotation existe ou pas
    - entree : annotation a chercher, le package, son emplacement
    - sortie : 

- on peut mettre un variable dans web.xml
    - mettre une variable dans web.xml  pour mettre les classes dans un package ( a demander sur google )

- on aura une classe utilitaire pour mettre les fonctions utilitaire pour eviter de tout ecrire dans init


#
listener : associe  a un event -> quand cet evenement est appele -> listener est appelle

filter