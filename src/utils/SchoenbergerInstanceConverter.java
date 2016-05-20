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
   * Methode bringt ein Datum vom Typ Date in das Progrämmchen YYYY-MM-DD.
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
   * main dient als Einstieg in das Programm.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    SchoenbergerInstance instance = new SchoenbergerInstance("INSTANCES/s1_i1_a10_b0.ttga");
    Date startDate = new Date(2016 - 1900, 06, 04);
    //Long Differenz zum Aufaddieren von 100 Tagen
    long hundretDiff = 8553600000L;
    Date endDate = new Date(startDate.getTime() + hundretDiff);

    //build options
    JsonObjectBuilder optionsBuilder = Json.createObjectBuilder();
    optionsBuilder.add("season_name", "KKl_1");
    optionsBuilder.add("start_at", dateToFormat(startDate));
    optionsBuilder.add("end_at", dateToFormat(endDate));
    JsonObjectBuilder builder = Json.createObjectBuilder();
    builder.add("options", optionsBuilder);

    //build teams
    /*JsonArrayBuilder arrb = Json.createArrayBuilder(); 

        JsonObjectBuilder job = Json.createObjectBuilder(); 
        job.add("stil", "rock"); 
        job.add("band", "U2"); 
        arrb.add(job); 

        job = Json.createObjectBuilder(); 
        job.add("stil", "metal"); 
        job.add("band", "Black Sabbath"); 
        arrb.add(job); 

        builder.add("musik", arrb); 
        builder.add("hungrig", true);*/
    JsonObject jo = builder.build();

    try {
      try (FileWriter fw = new FileWriter("test.json");
           JsonWriter jsonWriter = Json.createWriter(fw)) {
        jsonWriter.writeObject(jo);
      }
    } catch (IOException ioE) {
      System.err.println("Fehler beim Schreiben der JSON-Datei");
    }
  }

}
