package database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public static List<Ristorante> ricercaRistoranti(String richiesta, String urlDB, String userDB, String passDB){
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
            //
        }

        return risultati;
    }
}