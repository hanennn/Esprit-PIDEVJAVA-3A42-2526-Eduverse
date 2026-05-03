EduVerse — Plateforme de Gestion Pédagogique

EduVerse est une plateforme éducative desktop développée dans le cadre d'un projet académique à Esprit School of Engineering – Tunisie, durant l'année universitaire 2025–2026.
Elle vise à centraliser et digitaliser la gestion pédagogique en proposant plusieurs modules destinés aux étudiants, formateurs et administrateurs, tout en offrant une expérience interactive et structurée.

Fonctionnalités:

 Gestion des utilisateurs (étudiants, formateurs, administrateurs)
 Gestion des cours et des chapitres 
 Gestion des quiz et certifications
 Gestion des événements éducatifs
 Gestion des bourses et opportunités académiques
 Forums de discussion et échanges entre utilisateurs
 
Technologies utilisées:

  Java 17+
  
  Architecture MVC
  
  JDBC — connexion et persistance MySQL

Frontend / Interface:

  JavaFX 21 — interface graphique desktop
  
  FXML — définition des vues

Outils & Bibliothèques:

  Maven — gestion des dépendances
  
  MySQL 8.0.33 — base de données relationnelle

Architecture:

L'application est conçue selon l'architecture MVC (Model – View – Controller) :
  Model : entités Java (cours, chapitres, Quiz, Certification...) et services JDBC
  View : interfaces utilisateur définies en FXML (listeCours.fxml, CoursDetailEtudiant.fxml...)
  Controller : logique métier et navigation entre vues (ListeCoursController, AdminQuizController...)

Structure du projet:

  src/
  ├── main/
  
  │   ├── java/org/example/
  │   │   ├── controllers/    
  │   │   ├── entities/        
  │   │   ├── services/      
  │   │   └── utils/ 
  
  │   └── resources/
  │       ├── *.fxml           
  
Installation et exécution:

  Prérequis:
  
  Java 17+
  MySQL 8.0+
  JavaFX SDK 21

Equipe du projet:

  Hanen Ben Naceur
  
  Asma Trabelsi
  
  Mohammed Yassine Aouadi
  
  Fatma Klibi
  
  Mohammed Amine Jaibi
  
  Alaa Boujenoui

 Contexte académique:
 
   Établissement : Esprit School of Engineering – Tunisie
   
   Cadre : Projet Intégré (PI / PIDEV)
   
   Année universitaire : 2025–2026
