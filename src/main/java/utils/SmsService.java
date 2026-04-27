package utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

// Envoi de messages WhatsApp via l'API Twilio
// Les identifiants sont lus depuis config.properties (ignore par Git)
public class SmsService {

    // Chargement des identifiants depuis config.properties
    private static final Properties config = new Properties();

    static {
        try {
            config.load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            System.err.println("Erreur : fichier config.properties introuvable.");
            e.printStackTrace();
        }
    }

    // Envoie un message WhatsApp au numero donne
    public static boolean envoyerWhatsApp(String numeroDestinataire, String messageTexte) {
        try {
            String accountSid = config.getProperty("twilio.account_sid");
            String authToken = config.getProperty("twilio.auth_token");
            String twilioNumber = config.getProperty("twilio.whatsapp_number");

            Twilio.init(accountSid, authToken);

            Message message = Message.creator(
                    new PhoneNumber("whatsapp:" + numeroDestinataire),
                    new PhoneNumber(twilioNumber),
                    messageTexte
            ).create();

            System.out.println("WhatsApp envoye ! SID : " + message.getSid());
            return true;

        } catch (Exception e) {
            System.err.println("Erreur envoi WhatsApp : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Envoie un WhatsApp de confirmation quand l'etudiant depose sa demande
    public static boolean envoyerConfirmationDemande(String numeroEtudiant, String prenomEtudiant, String titreBourse) {
        String message = "Bonjour " + prenomEtudiant + ",\n\n"
                + "Votre demande pour la bourse \"" + titreBourse + "\" a bien ete recue.\n"
                + "Elle est actuellement en cours de traitement.\n\n"
                + "Vous serez notifie(e) des que votre demande sera traitee.\n"
                + "- Equipe Eduverse";

        return envoyerWhatsApp(numeroEtudiant, message);
    }
}
