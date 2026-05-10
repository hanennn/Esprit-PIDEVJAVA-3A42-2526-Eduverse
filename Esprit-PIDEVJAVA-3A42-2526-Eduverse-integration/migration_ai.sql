-- =====================================================
-- Migration Eduverse – Ajout note (INT) et commentaire
-- =====================================================

-- 1) Modifier la colonne note : passer de FLOAT à INT (1-5)
ALTER TABLE event_inscription
    MODIFY COLUMN note INT DEFAULT NULL;

-- 2) Ajouter la colonne commentaire si elle n'existe pas encore
ALTER TABLE event_inscription
    ADD COLUMN commentaire VARCHAR(500) DEFAULT NULL;
