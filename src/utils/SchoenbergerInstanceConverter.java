package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

public class SchoenbergerInstanceConverter {

  private static class SchoenbergerInstance {

    int teamzahl;
    int[] abstaende;
    int nmin;
    int nmax;
    int[] teamIDs;
    ArrayList<Integer>[] homeAvail;
    ArrayList<Integer>[] awaySuspend;

    public SchoenbergerInstance(String filename) {
      try {
        BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
        String line = reader.readLine();
        teamzahl = Integer.parseInt(line.split("=")[1]);
        reader.readLine();
        reader.readLine();
        reader.readLine();
        reader.readLine();
        line = reader.readLine();
        String[] lineParts = line.split("=")[1].split(",");
        abstaende = new int[teamzahl];
        for (int i = 0; i < teamzahl; i++) {
          abstaende[i] = Integer.parseInt(lineParts[i]);
        }
        reader.readLine();
        homeAvail = new ArrayList[teamzahl];
        awaySuspend = new ArrayList[teamzahl];
        teamIDs = new int[teamzahl];
        for (int i = 0; i < teamzahl; i++) {
          line = reader.readLine();
          homeAvail[i] = new ArrayList<>();
          awaySuspend[i] = new ArrayList<>();
          teamIDs[i] = Integer.parseInt(line.split(":")[1].split("=")[0]);
          lineParts = line.split("=")[1].split(",");
          for (String linePart : lineParts) {
            homeAvail[i].add(Integer.parseInt(linePart));
          }
          line = reader.readLine();
          lineParts = line.split("=")[1].split(",");
          for (String linePart : lineParts) {
            awaySuspend[i].add(Integer.parseInt(linePart));
          }
        }
        line = reader.readLine();
        nmin = Integer.parseInt(line.split("=")[1]);
        line = reader.readLine();
        nmax = Integer.parseInt(line.split("=")[1]);
      } catch (FileNotFoundException fnfE) {
        System.err.println("File " + filename + " existiert auf dem System nicht");
      } catch (IOException ioE) {
        System.err.println("Fehler beim Lesen des Files");

      }
    }
  }

  /**
   * Methode bringt ein Datum vom Typ Date in das Programm YYYY-MM-DD.
   *
   * @param date Datum, das formatiert werden soll.
   * @return YYYY-MM-DD formatiertes Datum als String.
   */
  public static String dateToFormat(Date date) {
    String formatDate = "";
    formatDate += (date.getYear() + 1900) + "-";
    int month = date.getMonth() + 1;
    if (month <= 9) {
      formatDate += "0";
    }
    formatDate += month + "-";
    int day = date.getDate();
    if (day <= 9) {
      formatDate += "0";
    }
    formatDate += day;
    return formatDate;
  }

  /**
   * Gibt den nth Tag an, der nach dem Starttag 1 date kommt
   *
   * @param date 1. Tag ab dem berechnet werden soll
   * @param nth Tag der berechnet werden soll
   * @return nter Tag
   */
  private static Date getNthNext(Date date, int nth) {
    long milliDiff;
    milliDiff = 1000L * 60L * 60L * 24L * (long) (nth - 1);
    Date diffDate = new Date(date.getTime() + milliDiff);
    return diffDate;
  }

  /**
   * Methode generiert für die gegebene Schoenberger Instanz eine dem entsprechende JSON-Datei
   * mit den jeweiligen Verfügbarkeiten bzw Unverfügbarkeiten.
   * @param fileName Input Schoeneberger Instanz
   * @param jsonFileName Output JSON-File
   * @param startDate Datum, an dem die Liga startet
   */
  public static void schoenbergerToJson(String fileName, String jsonFileName, Date startDate) {
    SchoenbergerInstance instance;
    instance = new SchoenbergerInstance(fileName);
    Date endDate = getNthNext(startDate, 100);

    //build options
    JsonObjectBuilder optionsBuilder = Json.createObjectBuilder();
    optionsBuilder.add("season_name", "KKl_1");
    optionsBuilder.add("start_at", dateToFormat(startDate));
    optionsBuilder.add("end_at", dateToFormat(endDate));
    JsonObjectBuilder builder = Json.createObjectBuilder();
    builder.add("options", optionsBuilder);

    //build team_options
    JsonObjectBuilder teamOptionsBuilder = Json.createObjectBuilder();
    JsonObjectBuilder teamBuilder = Json.createObjectBuilder();
    for (int i = 0; i < instance.teamzahl; i++) {
      teamBuilder.add("strength", 2);
      teamBuilder.add("max_per_week", 3);
      teamBuilder.add("min_distance", instance.abstaende[i]);
      teamBuilder.add("club_id", instance.teamIDs[i]);
      teamOptionsBuilder.add(Integer.toString(instance.teamIDs[i]), teamBuilder);
    }
    builder.add("team_options", teamOptionsBuilder);

    //build home constraints
    JsonObjectBuilder homeConstraintBuilder = Json.createObjectBuilder();
    JsonObjectBuilder constraintBuilder = Json.createObjectBuilder();
    int nextConsId = 1;
    for (int i = 0; i < instance.teamzahl; i++) {
      for (Integer tmpHomeAvail : instance.homeAvail[i]) {
        if (tmpHomeAvail < 101) {
          homeConstraintBuilder.add("type", "home");
          homeConstraintBuilder.add("valid_at", dateToFormat(getNthNext(startDate, tmpHomeAvail)));
          homeConstraintBuilder.add("capacity", "");
          homeConstraintBuilder.add("team_id", instance.teamIDs[i]);
          homeConstraintBuilder.add("club_id", "");
          homeConstraintBuilder.add("home_team_id", "");
          homeConstraintBuilder.add("away_team_id", "");
          homeConstraintBuilder.add("sports_facility_id", "");
          constraintBuilder.add(Integer.toString(nextConsId), homeConstraintBuilder);
          nextConsId++;
        }
      }
    }

    //build home constraints
    JsonObjectBuilder awayConstraintBuilder = Json.createObjectBuilder();
    for (int i = 0; i < instance.teamzahl; i++) {
      for (Integer tmpAwaySuspend : instance.awaySuspend[i]) {
        if (tmpAwaySuspend < 101) {
          awayConstraintBuilder.add("type", "away");
          awayConstraintBuilder.add("valid_at",dateToFormat(getNthNext(startDate, tmpAwaySuspend)));
          awayConstraintBuilder.add("capacity", "");
          awayConstraintBuilder.add("team_id", instance.teamIDs[i]);
          awayConstraintBuilder.add("club_id", "");
          awayConstraintBuilder.add("home_team_id", "");
          awayConstraintBuilder.add("away_team_id", "");
          awayConstraintBuilder.add("sports_facility_id", "");
          constraintBuilder.add(Integer.toString(nextConsId), awayConstraintBuilder);
          nextConsId++;
        }
      }
    }
    builder.add("contraints", constraintBuilder);

    //build document
    JsonObject jo = builder.build();

    //write document
    try {
      try (FileWriter fw = new FileWriter(jsonFileName);
              JsonWriter jsonWriter = Json.createWriter(fw)) {
        jsonWriter.writeObject(jo);
      }
    } catch (IOException ioE) {
      System.err.println("Fehler beim Schreiben der JSON-Datei");
    }
  }

  /**
   * Methode generiert für alle Instanzen des Ordners die entsrechenden JSON-Files.
   * Das erste Kommandozeilenargument muss einen Verweis zu dem Ordner enthalten, 
   * in dem die Files liegen.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    String dirName = args[0];
    String outDirName = "";
    File dir = new File(dirName);
    if (!dir.isDirectory()) {
      System.err.println("Angegebener String ist nicht Name eines Verzeichnises");
    } else {
      outDirName = dirName + "_JSON";
      File outDir = new File(outDirName);
      if (!outDir.isDirectory()) {
        if (!outDir.mkdir()) {
          System.err.println("Neues Verzeichnis kann nicht angelegt werden");
          System.exit(0);
        }
      }
    }
    File[] fileList;
    fileList = dir.listFiles();
    for (File curFile : fileList) {
      String fileName = curFile.getName();
      String outFileName = fileName.replace("ttga", "json");
      if (fileName.matches(".*ttga")) {
        Date startDate = new Date(2016 - 1900, 06, 11);
        schoenbergerToJson(dirName + "/" + fileName, outDirName + "/" + outFileName, startDate);
      }
    }
  }
}
