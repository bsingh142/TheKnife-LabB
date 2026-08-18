package com.mycompany.theknife;

import database.GestoreDatabase; // Assicurati che il package sia corretto!
import modelli.Recensione;
import modelli.Ristorante;
import modelli.Utente;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * Gestore multithread per la singola connessione client.
 * Riceve i dati, capisce che tipo di operazione è richiesta tramite uno switch,
 * e delega il lavoro al GestoreDatabase.
 */
public class serverSlave extends Thread {

    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    // Variabili per il database
    private String urlDB;
    private String userDB;
    private String passDB;

    /**
     * Costruttore dello Slave.
     */
    public serverSlave(Socket s, String url, String user, String pass) {
        this.socket = s;
        this.urlDB = url;
        this.userDB = user;
        this.passDB = pass;

        try {
            this.input = new ObjectInputStream(socket.getInputStream());
            this.output = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            System.err.println("Errore nella creazione dei flussi: " + ex.getMessage());
        }
    }

    @Override
    public void run() {
        System.out.println("[SLAVE " + getId() + "] Gestione client in corso...");

        try {
            // Legge il pacchetto in arrivo dal Client
            Object oggettoRicevuto = input.readObject();

            // 1. Controllo per la Registrazione (che invia un oggetto Utente)
            if (oggettoRicevuto instanceof Utente) {
                gestisciRegistrazione((Utente) oggettoRicevuto);
            }
            // 2. Controllo per l'Inserimento Recensione (invia un oggetto Recensione)
            else if (oggettoRicevuto instanceof Recensione) {
                gestisciAggiungiRecensione((Recensione) oggettoRicevuto);}
            // 2. Controllo per il Login e comandi futuri (che inviano un Array di Stringhe)
            else if (oggettoRicevuto instanceof String[]) {
                String[] pacchetto = (String[]) oggettoRicevuto;
                String comando = pacchetto[0]; // La prima parola è il comando


                // Il tuo SWITCH per gestire le varie funzionalità dell'app
                switch (comando) {
                    case "LOGIN":
                        gestisciLogin(pacchetto);
                        break;
                    case "RISTORANTI":
                        if(pacchetto.length==2){
                            gestisciRistoranti(pacchetto[1],null);
                        }else{
                            gestisciRistoranti(pacchetto[1],pacchetto[2]);
                        }
                        break;
                    case "POSIZIONE":
                        gestisciPosizione(pacchetto);
                        break;
                    case "TIPI_CUCINA":
                        gestisciTipiCucina();
                        break;
                    case "GET_RECENSIONI":
                        gestisciGetRecensioni(pacchetto);
                        break;
                    case "RISPONDI_RECENSIONE":
                        gestisciRispondiRecensione(pacchetto);
                        break;
                    case "ELIMINA_RECENSIONE":
                        gestisciEliminaRecensione(pacchetto);
                        break;
                    case "RIEPILOGO":
                        gestisciRiepilogo(pacchetto);
                        break;

                    // Qui in futuro potrai aggiungere case "PRENOTA" ecc.

                    default:
                        System.out.println("[SLAVE " + getId() + "] Comando sconosciuto: " + comando);
                        output.writeObject("ERRORE: Comando non riconosciuto dal Server.");
                        output.flush();
                        break;
                }
            } else {
                System.out.println("[SLAVE " + getId() + "] Formato dati non supportato.");
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[SLAVE " + getId() + "] Errore di comunicazione: " + e.getMessage());
        } finally {
            chiudiRisorse();
        }
    }

    /**
     * Gestisce la logica di Registrazione.
     */
    private void gestisciRegistrazione(Utente utente) throws IOException {
        System.out.println("[SLAVE " + getId() + "] Richiesta di REGISTRAZIONE per: " + utente.getUsername());

        // Il GestoreDatabase ora ci restituisce direttamente la frase da inviare al Client
        String risposta = GestoreDatabase.registraUtente(utente, urlDB, userDB, passDB);

        output.writeObject(risposta);
        output.flush();
    }

    /**
     * Gestisce la logica di Login.
     */
    private void gestisciLogin(String[] pacchetto) throws IOException {
        String username = pacchetto[1];
        String password = pacchetto[2];

        System.out.println("[SLAVE " + getId() + "] Richiesta di LOGIN per: " + username);

        // Il GestoreDatabase valuta l'errore specifico (utente inesistente o password errata)
        String risposta = GestoreDatabase.verificaLogin(username, password, urlDB, userDB, passDB);

        output.writeObject(risposta);
        output.flush();
    }

    private void gestisciRistoranti(String richiesta,String posU) throws IOException {
        List<Ristorante> risultati=GestoreDatabase.ricercaRistoranti(richiesta,posU, urlDB,userDB,passDB);

        output.writeObject(risultati);
        output.flush();
    }

    private void gestisciPosizione(String[] pacchetto) throws IOException {
        String risp=GestoreDatabase.ricercaPosizione(pacchetto[1],pacchetto[2]);

        output.writeObject(risp);
        output.flush();
    }

    private void gestisciTipiCucina() throws IOException {
        List<String> ris=GestoreDatabase.ricercaTipiCucina(urlDB,userDB,passDB);

        output.writeObject(ris);
        output.flush();
    }

    /**
     * Chiude i flussi e il socket in modo sicuro.
     */
    private void chiudiRisorse() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("[SLAVE " + getId() + "] Connessione chiusa.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void gestisciAggiungiRecensione(Recensione recensione) throws IOException {
        System.out.println("[SLAVE " + getId() + "] Nuova RECENSIONE per ristorante ID: " + recensione.getRistoranteId());
        String risposta = GestoreDatabase.aggiungiRecensione(recensione, urlDB, userDB, passDB);
        output.writeObject(risposta);
        output.flush();
    }

    private void gestisciGetRecensioni(String[] pacchetto) throws IOException {
        int idRistorante = Integer.parseInt(pacchetto[1]);
        List<Recensione> lista = GestoreDatabase.visualizzaRecensioni(idRistorante, urlDB, userDB, passDB);
        output.writeObject(lista);
        output.flush();
    }

    private void gestisciRispondiRecensione(String[] pacchetto) throws IOException {
        int idRecensione = Integer.parseInt(pacchetto[1]);
        String testoRisposta = pacchetto[2];
        String esito = GestoreDatabase.rispondiARecensione(idRecensione, testoRisposta, urlDB, userDB, passDB);
        output.writeObject(esito);
        output.flush();
    }

    private void gestisciEliminaRecensione(String[] pacchetto) throws IOException {
        int idRecensione = Integer.parseInt(pacchetto[1]);
        int idUtente = Integer.parseInt(pacchetto[2]);
        String esito = GestoreDatabase.eliminaRecensione(idRecensione, idUtente, urlDB, userDB, passDB);
        output.writeObject(esito);
        output.flush();
    }

    private void gestisciRiepilogo(String[] pacchetto) throws IOException {
        int idRistorante = Integer.parseInt(pacchetto[1]);
        String riepilogo = GestoreDatabase.visualizzaRiepilogo(idRistorante, urlDB, userDB, passDB);
        output.writeObject(riepilogo);
        output.flush();
    }



}