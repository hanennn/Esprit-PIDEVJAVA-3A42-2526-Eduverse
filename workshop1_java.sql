-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 02, 2026 at 04:33 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.1.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `workshop1_java`
--

-- --------------------------------------------------------

--
-- Table structure for table `certification`
--

CREATE TABLE `certification` (
  `id` int(11) NOT NULL,
  `score_obtenu` float NOT NULL,
  `statut` varchar(255) NOT NULL,
  `badge` varchar(255) NOT NULL,
  `date_attribution` datetime NOT NULL,
  `user_id` int(11) NOT NULL,
  `quiz_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `certification`
--

INSERT INTO `certification` (`id`, `score_obtenu`, `statut`, `badge`, `date_attribution`, `user_id`, `quiz_id`) VALUES
(4, 100, 'Réussi', '🥇 Or', '2026-04-11 15:24:21', 1, 18),
(19, 100, 'Réussi', 'Or', '2026-04-16 20:01:22', 1, 23),
(20, 100, 'Réussi', 'Or', '2026-04-22 19:44:43', 1, 18),
(21, 100, 'Réussi', 'Or', '2026-04-25 13:48:52', 1, 18),
(22, 76.9231, 'Réussi', 'Argent', '2026-04-25 14:04:17', 1, 18),
(23, 61.5385, 'Échoué', 'Bronze', '2026-04-25 14:05:23', 1, 18),
(24, 84.6154, 'Réussi', 'Argent', '2026-04-28 08:14:16', 1, 18),
(25, 100, 'Réussi', 'Or', '2026-04-28 09:48:54', 1, 18),
(26, 100, 'Réussi', 'Or', '2026-04-30 18:17:06', 1, 27),
(27, 0, 'Échoué', '', '2026-05-01 12:26:02', 1, 27),
(28, 50, 'Réussi', 'Bronze', '2026-05-02 01:30:38', 1, 27),
(29, 100, 'Réussi', 'Or', '2026-05-02 01:33:50', 16, 27),
(30, 0, 'Échoué', '', '2026-05-02 03:07:49', 16, 27),
(31, 50, 'Réussi', 'Bronze', '2026-05-02 03:09:58', 16, 27),
(32, 50, 'Réussi', 'Bronze', '2026-05-02 15:16:52', 16, 27),
(33, 100, 'Réussi', 'Or', '2026-05-02 15:25:04', 16, 27);

-- --------------------------------------------------------

--
-- Table structure for table `certification_finale`
--

CREATE TABLE `certification_finale` (
  `id` int(11) NOT NULL,
  `date_emission` datetime NOT NULL,
  `badge` varchar(50) NOT NULL,
  `user_id` int(11) NOT NULL,
  `quiz_id` int(11) NOT NULL,
  `tentative_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `certification_finale`
--

INSERT INTO `certification_finale` (`id`, `date_emission`, `badge`, `user_id`, `quiz_id`, `tentative_id`) VALUES
(30, '2026-04-28 09:46:25', '🥇 Or', 1, 18, 4),
(31, '2026-04-30 18:26:57', 'Or', 1, 18, 25),
(32, '2026-05-02 01:45:42', 'Or', 16, 27, 29);

-- --------------------------------------------------------

--
-- Table structure for table `chapitres`
--

CREATE TABLE `chapitres` (
  `id` int(11) NOT NULL,
  `titre_chap` varchar(255) NOT NULL,
  `desc_chap` longtext NOT NULL,
  `ordre_chap` int(11) NOT NULL,
  `duree_chap` varchar(255) NOT NULL,
  `statut_chap` varchar(20) NOT NULL,
  `contenu_chap` varchar(255) NOT NULL,
  `type_contenu` varchar(255) NOT NULL,
  `cours_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `chapitres`
--

INSERT INTO `chapitres` (`id`, `titre_chap`, `desc_chap`, `ordre_chap`, `duree_chap`, `statut_chap`, `contenu_chap`, `type_contenu`, `cours_id`) VALUES
(19, 'Interpolation Polynomiale : Méthode de Lagrange', 'Ce chapitre couvre lesl notions  de l\'interpolation polynomiale', 1, '1h', 'OUVERT', 'C:\\Users\\hanen\\Downloads\\1-Interpolation de Lagrange.pdf', 'pdf', 23),
(26, 'Interpolation polynomiale : Newton', 'ce chapitre couvre les notions de l\'interpolation polynomiale : Méthode de Newton', 2, '1h', 'OUVERT', 'C:\\Users\\hanen\\Downloads\\2-Interpolation de Newton.pdf', 'pdf', 23);

-- --------------------------------------------------------

--
-- Table structure for table `cours`
--

CREATE TABLE `cours` (
  `id` int(11) NOT NULL,
  `titre_cours` varchar(255) NOT NULL,
  `niv_cours` varchar(100) NOT NULL,
  `matiere_cours` varchar(100) NOT NULL,
  `langue_cours` varchar(100) NOT NULL,
  `description` longtext DEFAULT NULL,
  `createur_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `cours`
--

INSERT INTO `cours` (`id`, `titre_cours`, `niv_cours`, `matiere_cours`, `langue_cours`, `description`, `createur_id`) VALUES
(15, 'Php', '2éme année prepa', 'programmation', 'français', 'ce cours est une initiation à PHP', 8),
(20, 'Java', '1ere cycle ing', 'programmation', 'français', 'ce cours couvre les notions du java', NULL),
(23, 'Analyse Numérique', '1ere cycle ing', 'Mathématiques', 'Français', 'Ce cours couvre les notions de AN', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `inscription`
--

CREATE TABLE `inscription` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `cours_id` int(11) NOT NULL,
  `date_inscription` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `inscription`
--

INSERT INTO `inscription` (`id`, `user_id`, `cours_id`, `date_inscription`) VALUES
(1, 16, 20, '2026-05-02 01:13:40'),
(2, 16, 23, '2026-05-02 01:38:33');

-- --------------------------------------------------------

--
-- Table structure for table `question`
--

CREATE TABLE `question` (
  `id` int(11) NOT NULL,
  `quiz_id` int(11) NOT NULL,
  `question` varchar(500) NOT NULL,
  `points` int(11) NOT NULL,
  `reponses` longtext NOT NULL CHECK (json_valid(`reponses`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `question`
--

INSERT INTO `question` (`id`, `quiz_id`, `question`, `points`, `reponses`) VALUES
(3, 18, 'c\'est quoi stream?', 40, '[{\"texte\":\"Flux de données\",\"correct\":true},{\"texte\":\"methode\",\"correct\":false},{\"texte\":\"framework\",\"correct\":false},{\"texte\":\"code\",\"correct\":false}]'),
(5, 23, 'c\'est quoi une balise ?', 75, '[{\"texte\":\"rep1\",\"correct\":false},{\"texte\":\"rep2\",\"correct\":false},{\"texte\":\"rep3\",\"correct\":false},{\"texte\":\"rep4\",\"correct\":true}]'),
(12, 18, 'Quel est l\'avantage principal de l\'utilisation de Java pour le développement d\'applications Web ?', 15, '[{\"texte\":\"La rapidité d\'exécution\",\"correct\":false},{\"texte\":\"La compatibilité avec de nombreux systèmes d\'exploitation\",\"correct\":true},{\"texte\":\"La simplicité de la syntaxe\",\"correct\":false},{\"texte\":\"La gratuité du langage\",\"correct\":false}]'),
(13, 18, 'Quel est le nom de la méthode utilisée pour créer une instance d\'une classe en Java ?', 10, '[{\"texte\":\"new()\",\"correct\":true},{\"texte\":\"create()\",\"correct\":false},{\"texte\":\"getInstance()\",\"correct\":false},{\"texte\":\"init()\",\"correct\":false}]'),
(16, 23, 'Quel est l\'objet principal de la classe String dans Java ?', 15, '[{\"texte\":\"Représenter des nombres entiers\",\"correct\":false},{\"texte\":\"Représenter des chaînes de caractères\",\"correct\":true},{\"texte\":\"Gérer des fichiers\",\"correct\":false},{\"texte\":\"Effectuer des opérations mathématiques\",\"correct\":false}]'),
(17, 23, 'Quelle est la différence principale entre les interfaces et les classes abstraites en Java ?', 20, '[{\"texte\":\"Les classes abstraites ne peuvent pas être instanciées, tandis que les interfaces peuvent l\'être\",\"correct\":false},{\"texte\":\"Les interfaces ne peuvent pas être étendues, tandis que les classes abstraites peuvent l\'être\",\"correct\":false},{\"texte\":\"Les interfaces ne peuvent contenir que des méthodes abstraites, tandis que les classes abstraites peuvent contenir des méthodes concrètes\",\"correct\":true},{\"texte\":\"Les classes abstraites ne peuvent contenir que des méthodes concrètes, tandis que les interfaces peuvent contenir des méthodes abstraites\",\"correct\":false}]'),
(19, 25, 'Quelle est la fonction principale d\'une fonction en Java ?', 50, '[{\"texte\":\"Déclarer une variable\",\"correct\":false},{\"texte\":\"Créer un objet\",\"correct\":false},{\"texte\":\"Exécuter un bloc de code réutilisable\",\"correct\":true},{\"texte\":\"Lire un fichier\",\"correct\":false}]'),
(20, 25, 'quel est le mot utilisé pour déclarer une variable finale', 5, '[{\"texte\":\"static\",\"correct\":false},{\"texte\":\"abstract\",\"correct\":false},{\"texte\":\"final\",\"correct\":true},{\"texte\":\"Aucune Réponse\",\"correct\":false}]'),
(21, 27, 'Qu\'est-ce que la méthode d\'interpolation de Lagrange ?', 10, '[{\"texte\":\"Résoudre une équation différentielle\",\"correct\":false},{\"texte\":\"Calculer une dérivée\",\"correct\":false},{\"texte\":\"Trouver un polynôme passant par des points donnés\",\"correct\":true},{\"texte\":\"Aucune réponse\",\"correct\":false}]'),
(22, 27, 'Le polynôme d\'interpolation de Lagrange est :', 10, '[{\"texte\":\"Unique\",\"correct\":true},{\"texte\":\"infini\",\"correct\":false},{\"texte\":\". Toujours linéaire\",\"correct\":false},{\"texte\":\"aucune réponse\",\"correct\":false}]');

-- --------------------------------------------------------

--
-- Table structure for table `quiz`
--

CREATE TABLE `quiz` (
  `id` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `type_quiz` varchar(255) NOT NULL,
  `duree` int(11) NOT NULL,
  `score_minimum` float NOT NULL,
  `cours_associe_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `quiz`
--

INSERT INTO `quiz` (`id`, `titre`, `type_quiz`, `duree`, `score_minimum`, `cours_associe_id`) VALUES
(18, 'java quizz', 'Final', 60, 70, 1),
(23, 'web', 'Intermédiaire', 60, 75, 2),
(24, 'mathématique', 'Final', 35, 10, 1),
(25, 'web', 'Intermédiaire', 60, 75, 20),
(27, 'interpolation polynomiale', 'Intermédiaire', 1, 50, 23);

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `username` varchar(180) DEFAULT NULL,
  `roles` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`roles`)),
  `google_id` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `nom` varchar(100) DEFAULT NULL,
  `prenom` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `date_inscription` datetime DEFAULT NULL,
  `date_derniere_connexion` datetime DEFAULT NULL,
  `specialite` varchar(255) DEFAULT NULL,
  `experience` longtext DEFAULT NULL,
  `last_known_ip` varchar(45) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`id`, `username`, `roles`, `google_id`, `password`, `nom`, `prenom`, `email`, `is_active`, `date_inscription`, `date_derniere_connexion`, `specialite`, `experience`, `last_known_ip`) VALUES
(4, 'Student', '[\"ROLE_STUDENT\"]', NULL, '000', 'Student', 'Student', 'Student@gmail.com', 0, '2026-04-06 23:21:00', '2026-04-20 23:52:22', NULL, NULL, NULL),
(5, 'testUpdate2', '[\"ROLE_ADMIN\"]', NULL, '0000', 'admin2', 'admin2', 'admin2@gmail.com', 1, '2026-04-12 23:59:36', NULL, NULL, NULL, '165.50.247.222'),
(9, 'Aouadi1', '[\"ROLE_STUDENT\"]', NULL, 'Azerty@2000', 'yassine', 'Aouadi', 'aouadiyassine3@gmail.com', 1, '2026-04-14 10:18:51', '2026-04-14 10:20:57', NULL, NULL, NULL),
(11, 'TestGoogle', '[\"ROLE_PROFESSOR\"]', '', '0000', 'aouadi', 'Mohammed Yassine', 'aouadi.mohammedyassine@gmail.com', 1, '2026-04-26 16:37:12', '2026-05-01 22:18:01', NULL, NULL, '165.51.163.68'),
(15, 'testGoogle1', '[\"ROLE_STUDENT\"]', '114215327080500843815', '', 'aouadi1', 'yassine1', 'mohamedyassine.aouadi@esprit.tn', 1, '2026-04-28 10:19:36', '2026-04-28 10:19:58', NULL, NULL, NULL),
(16, 'aouadi10', '[\"ROLE_STUDENT\"]', NULL, '#MoustiStop2004', 'aouadi', 'yassine', 'aouadiyassine12@gmail.com', 1, '2026-04-28 10:21:07', '2026-05-02 01:44:45', NULL, NULL, '165.50.247.222');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `certification`
--
ALTER TABLE `certification`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `certification_finale`
--
ALTER TABLE `certification_finale`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `tentative_id` (`tentative_id`);

--
-- Indexes for table `chapitres`
--
ALTER TABLE `chapitres`
  ADD PRIMARY KEY (`id`),
  ADD KEY `chapitres_ibfk_1` (`cours_id`);

--
-- Indexes for table `cours`
--
ALTER TABLE `cours`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `inscription`
--
ALTER TABLE `inscription`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `cours_id` (`cours_id`);

--
-- Indexes for table `question`
--
ALTER TABLE `question`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `quiz`
--
ALTER TABLE `quiz`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `certification`
--
ALTER TABLE `certification`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=34;

--
-- AUTO_INCREMENT for table `certification_finale`
--
ALTER TABLE `certification_finale`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=33;

--
-- AUTO_INCREMENT for table `chapitres`
--
ALTER TABLE `chapitres`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=27;

--
-- AUTO_INCREMENT for table `cours`
--
ALTER TABLE `cours`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT for table `inscription`
--
ALTER TABLE `inscription`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `question`
--
ALTER TABLE `question`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- AUTO_INCREMENT for table `quiz`
--
ALTER TABLE `quiz`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

--
-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `chapitres`
--
ALTER TABLE `chapitres`
  ADD CONSTRAINT `chapitres_ibfk_1` FOREIGN KEY (`cours_id`) REFERENCES `cours` (`id`);

--
-- Constraints for table `inscription`
--
ALTER TABLE `inscription`
  ADD CONSTRAINT `inscription_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `inscription_ibfk_2` FOREIGN KEY (`cours_id`) REFERENCES `cours` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
