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
    private String domicilio;
    private String ruolo; // Deve essere "Cliente" o "Ristoratore"

    /**
     * Costruttore completo: utilizzato per creare un nuovo oggetto Utente
     * fornendo tutti i dati in un colpo solo.
     */
    public Utente(String nome, String cognome, String username, String pwd, String dob, String domicilio, String ruolo) {
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.pwd = pwd;
        this.dob = dob;
        this.domicilio = domicilio;
        this.ruolo = ruolo;
    }

    /**
     * Costruttore vuoto di default.
     * È una buona pratica includerlo in Java quando si lavora con database e serializzazione.
     */
    public Utente() {}

    // Metodi Getter (permettono di leggere i dati dall'esterno)
    public String getNome() { return nome; }
    public String getCognome() { return cognome; }
    public String getUsername() { return username; }
    public String getPwd() { return pwd; }
    public String getDob() { return dob; }
    public String getDomicilio() { return domicilio; }
    public String getRuolo() { return ruolo; }

    // Metodi Setter (permettono di modificare i dati)
    public void setNome(String nome) { this.nome = nome; }
    public void setCognome(String cognome) { this.cognome = cognome; }
    public void setUsername(String username) { this.username = username; }
    public void setPwd(String pwd) { this.pwd = pwd; }
    public void setDob(String dob) { this.dob = dob; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }
    public void setRuolo(String ruolo) { this.ruolo = ruolo; }
}