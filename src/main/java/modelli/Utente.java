package modelli;

import java.io.Serializable;

/**
 * Questa classe rappresenta un Utente (sia Cliente che Ristoratore) nel sistema TheKnife.
 * Implementa l'interfaccia Serializable per permettere l'invio dell'oggetto tramite Socket.
 */
public class Utente implements Serializable {

    private static final long serialVersionUID = 1L;
    // Variabili private per incapsulare i dati, corrispondenti alle colonne del database
    private String nome;
    private String cognome;
    private String username;
    private String pwd; // Password
    private String dob; // Data di nascita (Date of birth)
    private Double latitudine;
    private Double longitudine;
    private String ruolo; // Deve essere "Cliente" o "Ristoratore"

    /**
     * Costruttore completo: utilizzato per creare un nuovo oggetto Utente
     * fornendo tutti i dati insieme.
     */
    public Utente(String nome, String cognome, String username, String pwd, String dob, Double lat,Double lon, String ruolo) {
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.pwd = pwd;
        this.dob = dob;
        this.latitudine=lat;
        this.longitudine=lon;
        this.ruolo = ruolo;
    }

    /**
     * Costruttore vuoto di default.
     * È una buona pratica includerlo in Java quando si lavora con database e serializzazione.
     */
    public Utente() {}

    /**
     * Costruttore per utenti ottenuti dal db
     * Manca la pwd perchè serve solo per avere le informazioni utente durante l'uso della applicazione
     */
    public Utente(String nome, String cognome, String username, String dob, Double lat,Double lon, String ruolo) {
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.dob = dob;
        this.latitudine=lat;
        this.longitudine=lon;
        this.ruolo = ruolo;
    }

    // Metodi Getter (permettono di leggere i dati dall'esterno)
    public String getNome() { return nome; }
    public String getCognome() { return cognome; }
    public String getUsername() { return username; }
    public String getPwd() { return pwd; }
    public String getDob() { return dob; }
    public Double getLatitudine() {return latitudine;}
    public Double getLongitudine() {return longitudine;}
    public String getRuolo() { return ruolo; }

    // Metodi Setter (permettono di modificare i dati)
    public void setNome(String nome) { this.nome = nome; }
    public void setCognome(String cognome) { this.cognome = cognome; }
    public void setUsername(String username) { this.username = username; }
    public void setPwd(String pwd) { this.pwd = pwd; }
    public void setDob(String dob) { this.dob = dob; }
    public void setLatitudine(Double latitudine) { this.latitudine = latitudine; }
    public void setLongitudine(Double longitudine) { this.longitudine = longitudine; }
    public void setRuolo(String ruolo) { this.ruolo = ruolo; }

    public void tostring(){
        System.out.println(this.nome + this.cognome);
    }
}