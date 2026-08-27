package modelli;

import java.io.Serializable;

/**
 * Questa classe rappresenta un Ristorante nel sistema TheKnife.
 * Mappa tutti i campi della tabella 'ristorantitheknife' del database.
 */
public class Ristorante implements Serializable {

    // Identificativo univoco del ristorante nel database (bigint)
    private long id;

    private String nome;
    private String indirizzo;
    private String citta;
    private String nazione;

    // Coordinate geografiche
    private double latitudine;
    private double longitudine;

    private String fasciaPrezzo; // Nel database è una stringa (es. "Minore di 30€")
    private boolean delivery;
    private boolean prenotazioneOnline;
    private String tipoCucina;

    // Contiene l'username dell'Utente ristoratore che ha creato il ristorante
    private String proprietario;

    /**
     * Costruttore completo: inizializza tutte le variabili del ristorante.
     */
    public Ristorante(long id, String nome, String indirizzo, String citta, String nazione, double latitudine, double longitudine, String fasciaPrezzo,
                      boolean delivery, boolean prenotazioneOnline, String tipoCucina, String proprietario) {
        this.id = id;
        this.nome = nome;
        this.indirizzo = indirizzo;
        this.citta = citta;
        this.nazione = nazione;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.fasciaPrezzo = fasciaPrezzo;
        this.delivery = delivery;
        this.prenotazioneOnline = prenotazioneOnline;
        this.tipoCucina = tipoCucina;
        this.proprietario = proprietario;
    }

    public Ristorante(String nome, String indirizzo, String citta, String nazione, String latitudine, String longitudine, String fasciaPrezzo,
                      boolean delivery, boolean prenotazioneOnline, String tipoCucina, String proprietario) {
        this.nome = nome;
        this.indirizzo = indirizzo;
        this.citta = citta;
        this.nazione = nazione;
        this.latitudine = Double.parseDouble(latitudine.trim());
        this.longitudine = Double.parseDouble(longitudine.trim());
        this.fasciaPrezzo = fasciaPrezzo;
        this.delivery = delivery;
        this.prenotazioneOnline = prenotazioneOnline;
        this.tipoCucina = tipoCucina;
        this.proprietario = proprietario;
    }

    // Costruttore vuoto
    public Ristorante() {}

    // Metodi Getter
    public long getId() { return id; }
    public String getNome() { return nome; }
    public String getIndirizzo() { return indirizzo; }
    public String getCitta() { return citta; }
    public String getNazione() { return nazione; }
    public double getLatitudine() { return latitudine; }
    public double getLongitudine() { return longitudine; }
    public String getFasciaPrezzo() { return fasciaPrezzo; }
    public boolean isDelivery() { return delivery; }
    public boolean isPrenotazioneOnline() { return prenotazioneOnline; }
    public String getTipoCucina() { return tipoCucina; }
    public String getProprietario() { return proprietario; }


    // Metodi Setter
    public void setId(long id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setIndirizzo(String indirizzo) { this.indirizzo = indirizzo; }
    public void setCitta(String citta) { this.citta = citta; }
    public void setNazione(String nazione) { this.nazione = nazione; }
    public void setLatitudine(double latitudine) { this.latitudine = latitudine; }
    public void setLongitudine(double longitudine) { this.longitudine = longitudine; }
    public void setFasciaPrezzo(String fasciaPrezzo) { this.fasciaPrezzo = fasciaPrezzo; }
    public void setDelivery(boolean delivery) { this.delivery = delivery; }
    public void setPrenotazioneOnline(boolean prenotazioneOnline) { this.prenotazioneOnline = prenotazioneOnline; }
    public void setTipoCucina(String tipoCucina) { this.tipoCucina = tipoCucina; }
    public void setProprietario(String proprietario) { this.proprietario = proprietario; }
}
