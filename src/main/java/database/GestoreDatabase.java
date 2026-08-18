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
    public static String registraUtente(Utente u, String urlDB, String userDB, String passDB) {
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

    public static List<Ristorante> ricercaRistoranti(String richiesta, String posUtente, String urlDB, String userDB, String passDB){
        List<Ristorante> risultati=new ArrayList<>();
        if(richiesta.equalsIgnoreCase("TUTTI")){
            String query="SELECT * FROM ristorantitheknife";

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
                System.err.println("[DB] Errore SQL durante il login: " + e.getMessage());
                return null;
            }
        }else{
            String[] tmp=richiesta.split("=");
            String[] distanze=tmp[0].split("/"); //Il formato è DistanzaScelta(5 o 10 o 20 o 50 o Qualsiasi)/latitudine/longitudine
            String[] prezzi=tmp[1].split("/"); //Il formato è prezzoMin/prezzoMax

            StringBuilder query=new StringBuilder("""
                SELECT r.*
                FROM ristorantitheknife as r
                WHERE 1 = 1
                """);

            List<Object> parametri = new ArrayList<>();

            query.append(" AND fascia_prezzo >= ?");
            parametri.add(Integer.parseInt(prezzi[0]));
            query.append(" AND fascia_prezzo <= ?");
            parametri.add(Integer.parseInt(prezzi[1]));

            if(!tmp[2].equals("Qualsiasi")){
                query.append("""
                     AND EXISTS (
                        SELECT 1
                        FROM unnest(string_to_array(r.tipo_cucina, ',')) AS x(tipo)
                        WHERE TRIM(x.tipo) = ?
                    )
                    """);
                parametri.add(tmp[2]);
            }
            if(tmp[3].equals("true")){
                query.append(" AND r.delivery = TRUE");
            }else if(tmp[3].equals("false")){
                query.append(" AND r.delivery = FALSE");
            }
            if(tmp[4].equals("true")){
                query.append(" AND r.prenotazione_online = TRUE");
            }else if(tmp[4].equals("false")){
                query.append(" AND r.prenotazione_online = FALSE");
            }

            //AGGIUNGERE IL CONTROLLO PER LA MEDIA DELLE STELLE DELLE RECENSIONI

            if(!distanze[0].equals("Qualsiasi")){
                query.append("""
                     AND ST_DistanceSphere(
                    ST_MakePoint(r.longitudine, r.latitudine),
                    ST_MakePoint(?, ?)
                    ) <= ?
                    """);

                parametri.add(Double.parseDouble(distanze[2]));
                parametri.add(Double.parseDouble(distanze[1]));
                parametri.add(Integer.parseInt(distanze[0]) * 1000);

                query.append("""
                    ORDER BY ST_DistanceSphere(
                    ST_MakePoint(r.longitudine, r.latitudine),
                    ST_MakePoint(?, ?)
                    ) ASC
                 """);

                parametri.add(Double.parseDouble(distanze[2]));
                parametri.add(Double.parseDouble(distanze[1]));
            }

            System.out.println(query);
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
            }catch (SQLException e) {
                System.err.println("[DB] Errore SQL durante il login: " + e.getMessage());
                return null;
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

    public static String aggiungiRecensione(Recensione recensione, String urlDB, String userDB, String passDB) {
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

    public static String rispondiARecensione(int idRecensione, String testoRisposta, String urlDB, String userDB, String passDB) {
        if (testoRisposta == null || testoRisposta.trim().isEmpty()) {
            return "ERRORE: La risposta non può essere vuota.";
        }

        String query = "UPDATE recensioni SET risposta = ? WHERE idrecensione = ?";

        try (Connection conn = DriverManager.getConnection(urlDB, userDB, passDB);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, testoRisposta.trim());
            pstmt.setInt(2, idRecensione);

            int righeAggiornate = pstmt.executeUpdate();
            return righeAggiornate > 0 ? "OK: Risposta salvata con successo!" : "ERRORE: Recensione non trovata.";


        } catch (SQLException e) {
            System.err.println("[DB] Errore SQL durante l'inserimento della risposta: " + e.getMessage());
            return "ERRORE: Problema di comunicazione con il Database.";
        }
    }

    public static String eliminaRecensione(int idRecensione, int idUtente, String urlDB, String userDB, String passDB) {
        String query = "DELETE FROM recensioni WHERE idrecensione = ? AND autore = ?";

        try (Connection conn = DriverManager.getConnection(urlDB, userDB, passDB);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, idRecensione);
            pstmt.setInt(2, idUtente);

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
}

