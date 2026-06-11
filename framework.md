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