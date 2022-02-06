package fr.alkanife.dbtohtml;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.sqlite.SQLiteConfig;

import java.io.FileWriter;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DBToHTML {

    public static void main(String[] args) {

        Connection connection = null;
        try {
            log("DB TO HTML V3");
            log("-------------");

            if (args.length < 1) {
                log("dbtohtml.jar <Nom du fichier> [v1/v2] [showauthors] [alternames]");
                log("- <Nom du fichier> : obligatoire, nom du fichier en .db à convertir (SANS LE .DB)");
                log("- [v1/v2] : 'v1' pour les db en content turns, 'v2' pour les db en message messages ('v2' est utilisé par défaut)");
                log("- [showauthors (boolean)] : montrer les auteurs et la date (true par défaut)");
                log("- [alternames (boolean)] : changer les noms (pour les vieilles db, true par défaut)");
                log("Exemple : dbtohtml.jar saison1 v1 true true");
                return;
            }

            String file = args[0];
            boolean v1 = false;
            boolean showauthors = true;
            boolean alternames = true;

            if (args.length >= 2)
                if (args[1].equalsIgnoreCase("v1"))
                    v1 = true;

            if (args.length >= 3)
                showauthors = Boolean.parseBoolean(args[2]);

            if (args.length >= 4)
                alternames = Boolean.parseBoolean(args[3]);

            log("Fichier : " + file);
            log("v1 ? " + v1);
            log("auteurs ? " + showauthors);
            log("changer les noms ? " + alternames);
            log("---- COMMENCEMENT ----");

            String mainPath = Paths.get("").toAbsolutePath().toString() + "/"+file+".db";

            SQLiteConfig config = new SQLiteConfig();
            config.setReadOnly(false);

            log("Getting connection");
            connection = DriverManager.getConnection("jdbc:sqlite:" + mainPath);

            log("Preparing statement");

            String sql = "SELECT date, author, content FROM turns";

            if (!v1)
                sql = "SELECT date, author, message FROM messages";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            log("Executing query");
            ResultSet resultSet = preparedStatement.executeQuery();

            List<Tour> tours = new ArrayList<>();

            while (resultSet.next()) {
                String author = resultSet.getString("author")
                        .replaceAll("Sheele-chan", "Sheele")
                        .replaceAll("๖̶̶̶ζ͜͡LamasticotWaifuOfSheele", "KuYu")
                        .replaceAll("Nutch’", "Sully")
                        .replaceAll("mel_chan!", "RelGainSlide")
                        .replaceAll("Izzumaki/Demiurge", "Izzumaki")
                        .replaceAll("Oxvox mieux que tes geox", "Gab")
                        .replaceAll("PATATHARDE", "Fate");
                String date = resultSet.getString("date");

                long epochMilli = OffsetDateTime.parse(date).toInstant().toEpochMilli();
                String s = new SimpleDateFormat("d MMM yyyy HH:mm:ss", Locale.FRANCE).format(new Date(epochMilli));

                String message = resultSet.getString(v1 ? "content" : "message");

                tours.add(new Tour(author, s, message));
            }

            log("Creating lines and writing file");

            //List<String> outLines = new ArrayList<>();

            FileWriter writer = new FileWriter(file+".txt");

            for (Tour tour : tours) {

                if (showauthors)
                    writer.write("<p id=\"tour_title\"><a id=\"tour_author\">" + tour.getAuthor() + "</a> <a id=\"tour_date\">" + tour.getDate() + "</a></p>\n");

                Parser parser = Parser.builder().build();
                Node document = parser.parse(tour.getMessage().replaceAll("\n", "<br>"));
                HtmlRenderer renderer = HtmlRenderer.builder().build();

                writer.write("<p id=\"tour_text\">" + renderer.render(document).replaceAll("<p>", "").replaceAll("</p>", "") + "</p>\n\n");

            }

            writer.close();

            log("Done");

        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }

    }

    private static void log(String m) {
        System.out.println("\u001B[36m" + m + "\u001B[0m");
    }

}