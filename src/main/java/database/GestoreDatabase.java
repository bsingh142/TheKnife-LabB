package database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import modelli.Recensione;
import modelli.Ristorante;
import modelli.Utente;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe ausiliaria per la gestione delle operazioni sul Database.
 */
public class GestoreDatabase {

    /**
     * Esegue la query di inserimento. Ora restituisce una Stringa con l'esito preciso.
     */
    public static synchronized String registraUtente(Utente u, String urlDB, String userDB, String passDB) {
        String query = "INSERT INTO utenti (nome, cognome, username, pwd, dob, latitudine,longitudine, ruolo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(urlDB, userDB, passDB);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, u.getNome());
            pstmt.setString(2, u.getCognome());
            pstmt.setString(3, u.getUsername());
            pstmt.setString(4, hashPassword(u.getPwd()));

            String dobString = u.getDob();
            if (dobString == null || dobString.trim().isEmpty()) {
                pstmt.setNull(5, Types.DATE);
            } else {
                try {
                    DateTimeFormatter traduttore = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate dataLocale = LocalDate.parse(dobString.trim(), traduttore);
                    pstmt.setDate(5, java.sql.Date.valueOf(dataLocale));
                } catch (DateTimeParseException e) {
                    return "ERRORE: Formato data errato lato Server.";
                }
            }

            pstmt.setDouble(6, u.getLatitudine());
            pstmt.setDouble(7, u.getLongitudine());
            pstmt.setString(8, u.getRuolo());

            pstmt.executeUpdate();
            return "OK: Registrazione completata con successo!";

        } catch (SQLException e) {
            // Controllo specifico per l'errore di violazione del vincolo UNIQUE (Username duplicato)
            if ("23505".equals(e.getSQLState())) {
                return "ERRORE: Lo username '" + u.getUsername() + "' è già in uso. Scegline un altro.";
            }

            System.err.println("[DB] Errore SQL: " + e.getMessage());
            return "ERRORE: Problema interno del Database.";
        }
    }

    /**
     * Verifica il login e restituisce un testo con l'esito specifico.
     */
    public static String verificaLogin(String username, String passwordInChiaro, String urlDB, String userDB, String passDB) {
        String query = "SELECT pwd,latitudine,longitudine FROM utenti WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(urlDB, userDB, passDB);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String hashSalvato = rs.getString("pwd");
                if (BCrypt.checkpw(passwordInChiaro, hashSalvato)) {
                    return "OK: Login effettuato con successo!" + rs.getDouble("latitudine")+"/"+rs.getDouble("longitudine");
                } else {
                    return "ERRORE: La password inserita non è corretta.";
                }
            }
            return "ERRORE: L'utente '" + username + "' non esiste.";

        } catch (SQLException e) {
            System.err.println("[DB] Errore SQL durante il login: " + e.getMessage());
            return "ERRORE: Problema di comunicazione con il Database.";
        }
    }

    private static String hashPassword(String passwordInChiaro) {
        return BCrypt.hashpw(passwordInChiaro, BCrypt.gensalt());
    }

    public static String ricercaPosizione(String citta,String nazione){
        try {
            String url="https://nominatim.openstreetmap.org/search"
                    +"?city="+ URLEncoder.encode(citta, StandardCharsets.UTF_8)
                    +"&country="+URLEncoder.encode(nazione,StandardCharsets.UTF_8)
                    +"&format=jsonv2"
                    +"&limit=1";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "TheKnife/1.0")
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode risultati=mapper.readTree(response.body());
            if(risultati.isEmpty()) {
                return null;
            }

            double latitudine=risultati.get(0).get("lat").asDouble();
            double longitudine=risultati.get(0).get("lon").asDouble();
            return latitudine+"/"+longitudine;

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public static String ricercaPosizioneRistorante(String via, String citta, String nazione) {
        try {
            // Usiamo i parametri strutturati di Nominatim per la massima precisione
            String url = "https://nominatim.openstreetmap.org/search"
                    + "?street=" + URLEncoder.encode(via, StandardCharsets.UTF_8)
                    + "&city=" + URLEncoder.encode(citta, StandardCharsets.UTF_8)
                    + "&country=" + URLEncoder.encode(nazione, StandardCharsets.UTF_8)
                    + "&format=jsonv2"
                    + "&limit=1";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "TheKnife/1.0")
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode risultati = mapper.readTree(response.body());

            if (risultati.isEmpty()) {
                return null; // Indirizzo non trovato
            }

            double latitudine = risultati.get(0).get("lat").asDouble();
            double longitudine = risultati.get(0).get("lon").asDouble();
            return latitudine + "/" + longitudine;

        } catch (Exception e) {
            System.err.println("[DB] Errore Geocoding Ristorante: " + e.getMessage());
            return null;
        }
    }

    public static List<Ristorante> ricercaRistoranti(String richiesta, String posUtente, String urlDB, String userDB, String passDB){
        List<Ristorante> risultati = new ArrayList<>();
        if(richiesta.equalsIgnoreCase("TUTTI")){
            String query="SELECT * FROM ristorantitheknife ORDER BY id ASC";

            try(Connection conn=DriverManager.getConnection(urlDB,userDB,passDB);
                PreparedStatement pstmt=conn.prepareStatement(query)){

                ResultSet rs=pstmt.executeQuery();

                while(rs.next()){
                    Ristorante r=new Ristorante(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("indirizzo"),
                            rs.getString("citta"),
                            rs.getString("nazione"),
                            rs.getDouble("latitudine"),
                            rs.getDouble("longitudine"),
                            rs.getString("fascia_prezzo"),
                            rs.getBoolean("delivery"),
                            rs.getBoolean("prenotazione_online"),
                            rs.getString("tipo_cucina"),
                            rs.getString("proprietario"));
                    risultati.add(r);
                }
            }catch (SQLException e) {
                System.err.println("[DB] Errore SQL caricamento TUTTI i ristoranti: " + e.getMessage());
                return new ArrayList<>(); // Ritorna lista vuota invece di null per evitare blocchi UI
            }
        }else{
            try {
                String[] tmp=richiesta.split("=");
                String[] distanze=tmp[0].split("/");
                String[] prezzi=tmp[1].split("/");

                StringBuilder query=new StringBuilder("SELECT r.* FROM ristorantitheknife as r WHERE 1 = 1 ");
                List<Object> parametri = new ArrayList<>();

                // Nel DB fascia_prezzo è già integer, lo compariamo direttamente!
                query.append(" AND r.fascia_prezzo >= ?");
                parametri.add(Integer.parseInt(prezzi[0]));
                query.append(" AND r.fascia_prezzo <= ?");
                parametri.add(Integer.parseInt(prezzi[1]));

                // Ricerca semplificata ma sicura al 100% per il tipo di cucina
                if(!tmp[2].equals("Qualsiasi")){
                    query.append(" AND r.tipo_cucina LIKE ? ");
                    parametri.add("%" + tmp[2].trim() + "%");
                }

                if(tmp[3].equals("true")){
                    query.append(" AND r.delivery = TRUE ");
                }else if(tmp[3].equals("false")){
                    query.append(" AND r.delivery = FALSE ");
                }

                if(tmp[4].equals("true")){
                    query.append(" AND r.prenotazione_online = TRUE ");
                }else if(tmp[4].equals("false")){
                    query.append(" AND r.prenotazione_online = FALSE ");
                }

                if(!tmp[5].equals("Qualsiasi") && !tmp[5].equals("0")){
                    // Uso COALESCE per evitare crash se un ristorante ha 0 recensioni
                    query.append(" AND (SELECT COALESCE(AVG(rec.stelle), 0) FROM recensioni rec WHERE rec.ristorante_id = r.id) >= ? ");
                    parametri.add(Double.parseDouble(tmp[5]));
                }

                if(!distanze[0].equals("Qualsiasi")){
                    double latSicura = Double.parseDouble(distanze[1].replace(",", "."));
                    double lonSicura = Double.parseDouble(distanze[2].replace(",", "."));
                    int kmMax = Integer.parseInt(distanze[0]);

                    // FORMULA DI HAVERSINE IN PURO SQL
                    // Bypassa completamente PostGIS calcolando la distanza sferica della Terra in KM
                    String formulaDistanza = "( 6371 * acos( least(1.0, cos( radians(?) ) * cos( radians( r.latitudine ) ) * cos( radians( r.longitudine ) - radians(?) ) + sin( radians(?) ) * sin( radians( r.latitudine ) ) ) ) )";

                    query.append(" AND ").append(formulaDistanza).append(" <= ? ");

                    // Parametri per il WHERE (L'ordine è fondamentale: Lat, Lon, Lat, Km)
                    parametri.add(latSicura);
                    parametri.add(lonSicura);
                    parametri.add(latSicura);
                    parametri.add(kmMax);

                    query.append(" ORDER BY ").append(formulaDistanza).append(" ASC ");

                    // Parametri per l'ORDER BY (Lat, Lon, Lat)
                    parametri.add(latSicura);
                    parametri.add(lonSicura);
                    parametri.add(latSicura);
                }

                System.out.println("Query Filtri generata: \n" + query);

                try(Connection conn=DriverManager.getConnection(urlDB,userDB,passDB);
                    PreparedStatement ps=conn.prepareStatement(query.toString())){

                    for (int i = 0; i < parametri.size(); i++) {
                        ps.setObject(i+1,parametri.get(i));
                    }

                    try(ResultSet rs=ps.executeQuery()){
                        while(rs.next()){
                            Ristorante r=new Ristorante(
                                    rs.getInt("id"),
                                    rs.getString("nome"),
                                    rs.getString("indirizzo"),
                                    rs.getString("citta"),
                                    rs.getString("nazione"),
                                    rs.getDouble("latitudine"),
                                    rs.getDouble("longitudine"),
                                    rs.getString("fascia_prezzo"),
                                    rs.getBoolean("delivery"),
                                    rs.getBoolean("prenotazione_online"),
                                    rs.getString("tipo_cucina"),
                                    rs.getString("proprietario"));
                            risultati.add(r);
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("[DB] Errore SQL durante I FILTRI: " + e.getMessage());
                return new ArrayList<>();
            } catch (Exception ex) {
                System.err.println("[JAVA] Errore generico (es. conversione numeri) nei filtri: " + ex.getMessage());
                return new ArrayList<>();
            }
        }
        return risultati;
    }

    public static List<String> ricercaTipiCucina(String urlDB, String userDB, String passDB){
        List<String> ris=new ArrayList<>();
        String query="""
    SELECT DISTINCT TRIM(x.tipo) AS tipo
    FROM ristorantitheknife AS r
    CROSS JOIN LATERAL unnest(string_to_array(r.tipo_cucina, ',')) AS x(tipo)
    WHERE r.tipo_cucina IS NOT NULL
    ORDER BY TRIM(x.tipo)
    """;

        try (Connection conn = DriverManager.getConnection(urlDB, userDB, passDB);
             PreparedStatement ps=conn.prepareStatement(query)) {

            ResultSet rs=ps.executeQuery();
            while(rs.next()){
                ris.add(rs.getString("tipo"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ris;
    }

    // ===================================================================================
    // METODI PREFERITI
    // ===================================================================================

    public static synchronized String aggiungiPreferito(String user, int ristorante, String urlDB, String userDB, String passDB){
        String query = "INSERT INTO preferiti(username, ristorante_id)" + "VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(urlDB, userDB, passDB);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, user);
            pstmt.setInt(2, ristorante);
            pstmt.executeUpdate();
            return "OK Ristorante aggiunto ai preferiti!";
        }catch (SQLException e) {
            // Controllo per la chiave duplicata (se già presente tra i preferiti)
            if ("23505".equals(e.getSQLState())) {
                return "ERRORE: Questo ristorante è già presente nei tuoi preferiti.";
            }
            System.err.println("[DB] Errore SQL durante l'aggiunta ai preferiti: " + e.getMessage());
            return "ERRORE: Problema di comunicazione con il Database.";
        }
    }

    public static synchronized String rimuoviPreferito(String user, int ristorante, String urlDB, String userDB, String passDB){
        String query = "DELETE FROM preferiti WHERE ristorante_id = ? AND username = ?";
        try (Connection conn = DriverManager.getConnection(urlDB, userDB, passDB);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(2, user);
            pstmt.setInt(1, ristorante);

            int righeEliminate = pstmt.executeUpdate();

            return righeEliminate > 0 ? "Ristorante rimosso dai preferiti con successo!" : "ERRORE: Impossibile eliminare il ritorante dai preferiti.";
        }catch (SQLException e) {
            System.err.println("[DB] Errore SQL durante la rimozione dai preferiti: " + e.getMessage());
            return "ERRORE: Problema di comunicazione con il Database.";
        }

    }

    public static List<Ristorante> visualizzaPreferiti(String username, String urlDB, String userDB, String passDB) {
        List<Ristorante> lista = new ArrayList<>();

        String query = "SELECT r.* FROM preferiti p " +
                "JOIN ristorantitheknife r ON p.ristorante_id = r.id " +
                "WHERE p.username = ?";

        try (Connection conn = DriverManager.getConnection(urlDB, userDB, passDB);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Ristorante r = new Ristorante(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("indirizzo"),
                            rs.getString("citta"),
                            rs.getString("nazione"),
                            rs.getDouble("latitudine"),
                            rs.getDouble("longitudine"),
                            rs.getString("fascia_prezzo"),
                            rs.getBoolean("delivery"),
                            rs.getBoolean("prenotazione_online"),
                            rs.getString("tipo_cucina"),
                            rs.getString("proprietario"));
                    lista.add(r);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Errore SQL durante il recupero dei preferiti: " + e.getMessage());
        }

        return lista;
    }

    // ===================================================================================
    // METODI RECENSIONI
    // ===================================================================================

    public static synchronized String aggiungiRecensione(Recensione recensione, String urlDB, String userDB, String passDB) {
        if (recensione.getStelle() < 1 || recensione.getStelle() > 5) {
            return "ERRORE: La valutazione deve essere compresa tra 1 e 5 stelle.";
        }

        String query = "INSERT INTO recensioni (autore, ristorante_id, stelle, testo, data) " +
                "VALUES (?, ?, ?, ?, CURRENT_DATE)";

        try (Connection conn = DriverManager.getConnection(urlDB, userDB, passDB);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, recensione.getIdUtente());
            pstmt.setInt(2, recensione.getRistoranteId());
            pstmt.setInt(3, recensione.getStelle());
            pstmt.setString(4, recensione.getTesto());

            int righeInserite = pstmt.executeUpdate();
            return righeInserite > 0 ? "OK: Recensione aggiunta con successo!" : "ERRORE: Impossibile salvare la recensione.";

        } catch (SQLException e) {
            System.err.println("[DB] Errore SQL durante l'inserimento della recensione: " + e.getMessage());
            return "ERRORE: Problema di comunicazione con il Database.";
        }
    }

    public static List<Recensione> visualizzaRecensioni(int idRistorante, String urlDB, String userDB, String passDB) {
        List<Recensione> listaRecensioni = new ArrayList<>();

        // Selezioniamo tutti i campi necessari mantenendo le recensioni più recenti in alto
        String query = "SELECT idrecensione, autore, ristorante_id, stelle, testo, data, risposta " +
                "FROM recensioni " +
                "WHERE ristorante_id = ? " +
                "ORDER BY data DESC, idrecensione DESC";

        try (Connection conn = DriverManager.getConnection(urlDB, userDB, passDB);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, idRistorante);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Utilizza il costruttore completo definito nella tua classe Recensione
                    Recensione r = new Recensione(
                            rs.getInt("idrecensione"),
                            rs.getString("autore"),
                            rs.getInt("ristorante_id"),
                            rs.getInt("stelle"),
                            rs.getString("testo"),
                            rs.getString("data"),
                            rs.getString("risposta") // Può essere null se non ancora presente
                    );

                    listaRecensioni.add(r);
                }
            }

        } catch (SQLException e) {
            System.err.println("[DB] Errore SQL durante il recupero delle recensioni: " + e.getMessage());
        }

        return listaRecensioni;
    }

    public static synchronized String rispondiARecensione(int idRecensione, String testoRisposta, String urlDB, String userDB, String passDB) {
        if (testoRisposta == null || testoRisposta.trim().isEmpty()) {
            return "ERRORE: La risposta non può essere vuota.";
        }

        // AGGIUNTO 'AND risposta IS NULL' PER RISPETTARE LA SPECIFICA della singola risposta da parte del ristoratore!
        String query = "UPDATE recensioni SET risposta = ? WHERE idrecensione = ? AND risposta IS NULL";

        try (Connection conn = DriverManager.getConnection(urlDB, userDB, passDB);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, testoRisposta.trim());
            pstmt.setInt(2, idRecensione);

            int righeAggiornate = pstmt.executeUpdate();
            return righeAggiornate > 0 ? "OK: Risposta salvata con successo!" : "ERRORE: Recensione non trovata o hai già risposto.";

        } catch (SQLException e) {
            System.err.println("[DB] Errore SQL durante l'inserimento della risposta: " + e.getMessage());
            return "ERRORE: Problema di comunicazione con il Database.";
        }
    }

    public static synchronized String eliminaRecensione(int idRecensione, String autore, String urlDB, String userDB, String passDB) {
        String query = "DELETE FROM recensioni WHERE idrecensione = ? AND autore = ?";

        try (Connection conn = DriverManager.getConnection(urlDB, userDB, passDB);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, idRecensione);
            pstmt.setString(2, autore);

            int righeEliminate = pstmt.executeUpdate();

            return righeEliminate > 0 ? "OK: Recensione eliminata con successo!" : "ERRORE: Impossibile eliminare la recensione.";

        } catch (SQLException e) {
            System.err.println("[DB] Errore SQL durante l'eliminazione della recensione: " + e.getMessage());
            return "ERRORE: Problema di comunicazione con il Database.";
        }
    }

    public static String visualizzaRiepilogo(int idRistorante, String urlDB, String userDB, String passDB) {
        String query = "SELECT " +
                "COUNT(*) AS totale, " +
                "COALESCE(ROUND(AVG(stelle), 2), 0) AS media, " +
                "COUNT(CASE WHEN stelle = 5 THEN 1 END) AS s5, " +
                "COUNT(CASE WHEN stelle = 4 THEN 1 END) AS s4, " +
                "COUNT(CASE WHEN stelle = 3 THEN 1 END) AS s3, " +
                "COUNT(CASE WHEN stelle = 2 THEN 1 END) AS s2, " +
                "COUNT(CASE WHEN stelle = 1 THEN 1 END) AS s1 " +
                "FROM recensioni WHERE id_ristorante = ?";

        try (Connection conn = DriverManager.getConnection(urlDB, userDB, passDB);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, idRistorante);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int totale = rs.getInt("totale");

                    if (totale == 0) {
                        return "RIEPILOGO: Nessuna recensione presente per questo ristorante.";
                    }

                    double media = rs.getDouble("media");
                    int s5 = rs.getInt("s5");
                    int s4 = rs.getInt("s4");
                    int s3 = rs.getInt("s3");
                    int s2 = rs.getInt("s2");
                    int s1 = rs.getInt("s1");

                    return String.format(
                            "RIEPILOGO RISTORANTE (ID %d)\n" +
                                    "★ Media Valutazione: %.2f / 5.0 (Totale: %d recensioni)\n" +
                                    "----------------------------------------\n" +
                                    "5 Stelle: %d\n" +
                                    "4 Stelle: %d\n" +
                                    "3 Stelle: %d\n" +
                                    "2 Stelle: %d\n" +
                                    "1 Stella : %d",
                            idRistorante, media, totale, s5, s4, s3, s2, s1
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("[DB] Errore SQL durante il calcolo del riepilogo: " + e.getMessage());
            return "ERRORE: Impossibile recuperare il riepilogo dal Database.";
        }

        return "ERRORE: Ristorante non trovato.";
    }

    public static List<Recensione> visualizzaRecensioniUtente(String autore, String urlDB, String userDB, String passDB) {
        List<Recensione> lista = new ArrayList<>();

        String query = "SELECT idrecensione, autore, ristorante_id, stelle, testo, data, risposta " +
                "FROM recensioni WHERE autore = ? ORDER BY data DESC, idrecensione DESC";

        try (Connection conn = DriverManager.getConnection(urlDB, userDB, passDB);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, autore);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Recensione r = new Recensione(
                            rs.getInt("idrecensione"),
                            rs.getString("autore"),
                            rs.getInt("ristorante_id"),
                            rs.getInt("stelle"),
                            rs.getString("testo"),
                            rs.getString("data"),
                            rs.getString("risposta")
                    );
                    lista.add(r);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Errore SQL durante il recupero recensioni utente: " + e.getMessage());
        }

        return lista;
    }

    public static synchronized String modificaRecensione(int idRecensione, String autore, int stelle, String testo, String urlDB, String userDB, String passDB) {
        if (stelle < 1 || stelle > 5) {
            return "ERRORE: La valutazione deve essere compresa tra 1 e 5 stelle.";
        }
        if (testo == null || testo.trim().isEmpty()) {
            return "ERRORE: Il testo della recensione non può essere vuoto.";
        }

        // AND autore = ? Impedisce di modificare recensioni di altri utenti
        String query = "UPDATE recensioni SET stelle = ?, testo = ? WHERE idrecensione = ? AND autore = ?";

        try (Connection conn = DriverManager.getConnection(urlDB, userDB, passDB);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, stelle);
            pstmt.setString(2, testo.trim());
            pstmt.setInt(3, idRecensione);
            pstmt.setString(4, autore);

            int righe = pstmt.executeUpdate();
            return righe > 0 ? "OK: Recensione modificata con successo!" : "ERRORE: Recensione non trovata o non autorizzata.";

        } catch (SQLException e) {
            System.err.println("[DB] Errore SQL durante la modifica della recensione: " + e.getMessage());
            return "ERRORE: Problema di comunicazione con il Database.";
        }
    }

    // ===================================================================================
    // METODI RISTORATORI
    // ===================================================================================

    public static synchronized String aggiungiRistorante(Ristorante r, String urlDB, String userDB, String passDB) {
        String query = "INSERT INTO ristorantitheknife (nome, indirizzo, citta, nazione, latitudine, longitudine, fascia_prezzo, delivery, prenotazione_online, tipo_cucina, proprietario) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(urlDB, userDB, passDB);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, r.getNome());
            pstmt.setString(2, r.getIndirizzo());
            pstmt.setString(3, r.getCitta());
            pstmt.setString(4, r.getNazione());
            pstmt.setDouble(5, r.getLatitudine());
            pstmt.setDouble(6, r.getLongitudine());

            // Ripristinato a intero perché il DB richiede un integer!
            pstmt.setInt(7, Integer.parseInt(r.getFasciaPrezzo()));

            pstmt.setBoolean(8, r.isDelivery());
            pstmt.setBoolean(9, r.isPrenotazioneOnline());
            pstmt.setString(10, r.getTipoCucina());
            pstmt.setString(11, r.getProprietario());

            pstmt.executeUpdate();
            System.out.println("OK: Registrazione ristorante completata con successo!");
            return "OK: Registrazione ristorante completata con successo!";

        } catch (SQLException e) {
            System.err.println("[DB] Errore SQL: " + e.getMessage());
            return "ERRORE: Problema interno del Database.";
        }
    }

    public static Utente ricercaUtente(String user, String urlDB, String userDB, String passDB){
        String query = "SELECT * FROM utenti WHERE username = ?";
        Utente u=null;
        try (Connection conn = DriverManager.getConnection(urlDB, userDB, passDB);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, user.trim());

            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
                u = new Utente(
                        rs.getString("nome"),
                        rs.getString("cognome"),
                        rs.getString("username"),
                        rs.getString("dob"),
                        rs.getDouble("latitudine"),
                        rs.getDouble("longitudine"),
                        rs.getString("ruolo"));
                u.tostring();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Errore SQL durante il recupero utente " + e.getMessage());
        }
        return u;
    }

    public static List<Ristorante> ricercaProprietario(String proprietario, String urlDB, String userDB, String passDB){
        List<Ristorante> risultati = new ArrayList<>();

        String query="SELECT * FROM ristorantitheknife WHERE proprietario = ?";

        try(Connection conn=DriverManager.getConnection(urlDB,userDB,passDB);
            PreparedStatement pstmt=conn.prepareStatement(query)){
            pstmt.setString(1,proprietario.trim());
            ResultSet rs=pstmt.executeQuery();

            while(rs.next()){
                Ristorante r=new Ristorante(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("indirizzo"),
                        rs.getString("citta"),
                        rs.getString("nazione"),
                        rs.getDouble("latitudine"),
                        rs.getDouble("longitudine"),
                        rs.getString("fascia_prezzo"),
                        rs.getBoolean("delivery"),
                        rs.getBoolean("prenotazione_online"),
                        rs.getString("tipo_cucina"),
                        rs.getString("proprietario"));

                risultati.add(r);
            }
        }catch (SQLException e) {
            System.err.println("[DB] Errore SQL durante ricerca ristoranti: " + e.getMessage());
            return null;
        }
        return risultati;
    }

    public static synchronized int eliminaRistorante(String proprietario, long id, String urlDB, String userDB, String passDB){

        String query = "DELETE FROM ristorantitheknife WHERE id=? AND proprietario=?";

        try(Connection conn=DriverManager.getConnection(urlDB,userDB,passDB);
            PreparedStatement pstmt=conn.prepareStatement(query)){

            pstmt.setLong(1, id);
            pstmt.setString(2, proprietario.trim());

            return pstmt.executeUpdate();

        }catch (SQLException e) {
            System.err.println("[DB] Errore SQL durante elimazione ristorante: " + e.getMessage());
            return 0;
        }

    }

    public static Ristorante idRistorante(long id, String urlDB, String userDB, String passDB){
        String query="SELECT * FROM ristorantitheknife WHERE id = ?";

        try(Connection conn=DriverManager.getConnection(urlDB,userDB,passDB);
            PreparedStatement pstmt=conn.prepareStatement(query)){
            pstmt.setLong(1,id);
            ResultSet rs=pstmt.executeQuery();

            if(rs.next()){
                Ristorante risultato=new Ristorante(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("indirizzo"),
                        rs.getString("citta"),
                        rs.getString("nazione"),
                        rs.getDouble("latitudine"),
                        rs.getDouble("longitudine"),
                        rs.getString("fascia_prezzo"),
                        rs.getBoolean("delivery"),
                        rs.getBoolean("prenotazione_online"),
                        rs.getString("tipo_cucina"),
                        rs.getString("proprietario"));
                return risultato;
            }
        }catch (SQLException e) {
            System.err.println("[DB] Errore SQL durante ricerca ristoranti: " + e.getMessage());
            return null;
        }

        return null;
    }

    public static String getInfoRistorante(int idR,String urlDB, String userDB, String passDB){
        String ris="";
        String query= """
                SELECT COUNT(*) as numero_recensioni,
                ROUND(AVG(stelle), 2) as media_recensioni
                FROM recensioni
                WHERE ristorante_id = ?
                """;
        try(Connection conn=DriverManager.getConnection(urlDB,userDB,passDB);
            PreparedStatement pstmt=conn.prepareStatement(query)){

            pstmt.setInt(1, idR);

            ResultSet rs=pstmt.executeQuery();
            if(rs.next()){
                ris= "Valutazione media: " + rs.getDouble("media_recensioni") + " con " + rs.getInt("numero_recensioni") + " recensioni";
            }
        }catch (SQLException e) {
            System.err.println("[DB] Errore SQL durante elimazione ristorante: " + e.getMessage());

        }
        return ris;
    }
}