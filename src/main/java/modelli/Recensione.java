package modelli;

import java.io.Serializable;

public class Recensione implements Serializable {
    private final long SerialVersionUID = 1L;

    private int idRecensione;
    private String autore;
    private int ristoranteId;
    private int stelle;
    private String testo;
    private String data;
    private String risposta;

    public Recensione(String autore, int ristoranteId, int stelle, String testo){
        this.autore = autore;
        this.ristoranteId = ristoranteId;
        this.stelle = stelle;
        this.testo = testo;
    }

    public Recensione(int idRecensione, String autore, int ristoranteId, int stelle, String testo, String data, String risposta) {
        this.idRecensione = idRecensione;
        this.autore = autore;
        this.ristoranteId = ristoranteId;
        this.stelle = stelle;
        this.testo = testo;
        this.data = data;
        this.risposta = risposta;
    }

    public int getIdRecensione() {
        return idRecensione;
    }

    public void setIdRecensione(int idRecensione) {
        this.idRecensione = idRecensione;
    }

    public String getIdUtente() {
        return autore;
    }

    public void setAutore(String autore) {
        this.autore = autore;
    }

    public int getRistoranteId() {
        return ristoranteId;
    }

    public void setRistoranteId(int ristoranteId) {
        this.ristoranteId = ristoranteId;
    }

    public int getStelle() {
        return stelle;
    }

    public void setStelle(int stelle) {
        this.stelle = stelle;
    }

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getRisposta() {
        return risposta;
    }

    public void setRisposta(String risposta) {
        this.risposta = risposta;
    }
}