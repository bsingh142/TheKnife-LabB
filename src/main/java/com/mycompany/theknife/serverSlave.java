package com.mycompany.theknife;

import database.GestoreDatabase;
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

            // 1. Controllo per la Registrazione
            if (oggettoRicevuto instanceof Utente) {
                gestisciRegistrazione((Utente) oggettoRicevuto);
            }
            // 2. Controllo per l'Inserimento Recensione
            else if (oggettoRicevuto instanceof Recensione) {
                gestisciAggiungiRecensione((Recensione) oggettoRicevuto);
            }
            // 3. Controllo per l'Inserimento Ristorante
            else if (oggettoRicevuto instanceof Ristorante) {
                gestisciAggiungiRistorante((Ristorante) oggettoRicevuto);
            }
            // 4. Controllo per i comandi testuali
            else if (oggettoRicevuto instanceof String[]) {
                String[] pacchetto = (String[]) oggettoRicevuto;
                String comando = pacchetto[0];

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
                    case "POSIZIONE_RISTORANTE":
                        gestisciPosizioneRistorante(pacchetto);
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
                    case "GET_RECENSIONI_UTENTE":
                        gestisciGetRecensioniUtente(pacchetto);
                        break;
                    case "MODIFICA_RECENSIONE":
                        gestisciModificaRecensione(pacchetto);
                        break;
                    // -- Funzionalità Preferiti --
                    case "AGGIUNGI_PREFERITO":
                        gestisciAggiungiPreferito(pacchetto);
                        break;
                    case "RIMUOVI_PREFERITO":
                        gestisciRimuoviPreferito(pacchetto);
                        break;
                    case "GET_PREFERITI":
                        gestisciGetPreferiti(pacchetto);
                        break;
                    // -- Funzionalità Ristoratori --
                    case "GET_UTENTE":
                        gestisciGetUtente(pacchetto);
                        break;
                    case "PROPRIETARIO":
                        gestisciRicercaProprietario(pacchetto);
                        break;
                    case "ELIMINA_RISTORANTE":
                        gestisciEliminaRistorante(pacchetto);
                        break;
                    case "RICERCA_ID":
                        gestisciRicercaId(pacchetto);
                        break;
                    case "INFO_RISTORANTE":
                        getInfoRistorante(pacchetto);
                        break;
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

    private void gestisciRegistrazione(Utente utente) throws IOException {
        System.out.println("[SLAVE " + getId() + "] Richiesta di REGISTRAZIONE per: " + utente.getUsername());
        String risposta = GestoreDatabase.registraUtente(utente, urlDB, userDB, passDB);
        output.writeObject(risposta);
        output.flush();
    }

    private void gestisciLogin(String[] pacchetto) throws IOException {
        String username = pacchetto[1];
        String password = pacchetto[2];
        System.out.println("[SLAVE " + getId() + "] Richiesta di LOGIN per: " + username);
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

    private void gestisciPosizioneRistorante(String[] pacchetto) throws IOException {
        // pacchetto conterrà: [0] comando, [1] indirizzo, [2] citta, [3] nazione
        String risp = GestoreDatabase.ricercaPosizioneRistorante(pacchetto[1], pacchetto[2], pacchetto[3]);
        output.writeObject(risp);
        output.flush();
    }

    private void gestisciTipiCucina() throws IOException {
        List<String> ris=GestoreDatabase.ricercaTipiCucina(urlDB,userDB,passDB);
        output.writeObject(ris);
        output.flush();
    }

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
        String autore = (pacchetto[2]);
        String esito = GestoreDatabase.eliminaRecensione(idRecensione, autore, urlDB, userDB, passDB);
        output.writeObject(esito);
        output.flush();
    }

    private void gestisciRiepilogo(String[] pacchetto) throws IOException {
        int idRistorante = Integer.parseInt(pacchetto[1]);
        String riepilogo = GestoreDatabase.visualizzaRiepilogo(idRistorante, urlDB, userDB, passDB);
        output.writeObject(riepilogo);
        output.flush();
    }

    private void gestisciGetRecensioniUtente(String[] pacchetto) throws IOException {
        String autore = pacchetto[1];
        List<Recensione> lista = GestoreDatabase.visualizzaRecensioniUtente(autore, urlDB, userDB, passDB);
        output.writeObject(lista);
        output.flush();
    }

    private void gestisciModificaRecensione(String[] pacchetto) throws IOException {
        int idRecensione = Integer.parseInt(pacchetto[1]);
        String autore = pacchetto[2];
        int stelle = Integer.parseInt(pacchetto[3]);
        String testo = pacchetto[4];
        String esito = GestoreDatabase.modificaRecensione(idRecensione, autore, stelle, testo, urlDB, userDB, passDB);
        output.writeObject(esito);
        output.flush();
    }

    // -- Metodi Gestione Preferiti --
    private void gestisciAggiungiPreferito(String[] pacchetto) throws IOException {
        String autore = pacchetto[1];
        int idRistorante = Integer.parseInt(pacchetto[2]);
        String esito = GestoreDatabase.aggiungiPreferito(autore, idRistorante, urlDB, userDB, passDB);
        output.writeObject(esito);
        output.flush();
    }

    private void gestisciRimuoviPreferito(String[] pacchetto) throws IOException{
        String autore = pacchetto[1];
        int idRistorante = Integer.parseInt(pacchetto[2]);
        String esito = GestoreDatabase.rimuoviPreferito(autore, idRistorante, urlDB, userDB, passDB);
        output.writeObject(esito);
        output.flush();
    }

    private void gestisciGetPreferiti(String[] pacchetto) throws IOException {
        String username = pacchetto[1];
        List<Ristorante> lista = GestoreDatabase.visualizzaPreferiti(username, urlDB, userDB, passDB);
        output.writeObject(lista);
        output.flush();
    }

    // -- Metodi Gestione Ristoratori --
    private void gestisciGetUtente(String[] pacchetto) throws IOException{
        String username = pacchetto[1];
        Utente u = GestoreDatabase.ricercaUtente(username, urlDB, userDB, passDB);
        output.writeObject(u);
        output.flush();
    }

    private void gestisciAggiungiRistorante(Ristorante pacchetto) throws IOException {
        String risposta=GestoreDatabase.aggiungiRistorante(pacchetto, urlDB, userDB, passDB);
        output.writeObject(risposta);
        output.flush();
    }

    private void gestisciRicercaProprietario(String[] pacchetto) throws IOException{
        String username = pacchetto[1];
        List<Ristorante> r= GestoreDatabase.ricercaProprietario(username, urlDB, userDB, passDB);
        output.writeObject(r);
        output.flush();
    }

    private void gestisciEliminaRistorante(String[] pacchetto) throws IOException{
        Integer i = GestoreDatabase.eliminaRistorante(pacchetto[1], Long.parseLong(pacchetto[2]), urlDB, userDB, passDB);
        output.writeObject(i);
        output.flush();
    }

    private void gestisciRicercaId(String[] pacchetto) throws IOException{
        Ristorante i = GestoreDatabase.idRistorante(Long.parseLong(pacchetto[1]), urlDB, userDB, passDB);
        output.writeObject(i);
        output.flush();
    }

    private void getInfoRistorante(String[] pacchetto) throws IOException {
        String risp=GestoreDatabase.getInfoRistorante(Integer.parseInt(pacchetto[1]),urlDB, userDB, passDB);

        output.writeObject(risp);
        output.flush();
    }
}