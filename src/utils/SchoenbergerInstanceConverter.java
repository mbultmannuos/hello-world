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
import javax.json.JsonArrayBuilder; 
import javax.json.JsonObject; 
import javax.json.JsonObjectBuilder; 
import javax.json.JsonWriter; 

import com.sun.org.apache.xerces.internal.impl.dv.dtd.NMTOKENDatatypeValidator;

public class SchoenbergerInstanceConverter {

	private static class SchoenbergerInstance{
		int teamzahl;
		int[] abstaende;
		int n_min;
		int n_max;
		int[] teamIDs;
		ArrayList<Integer>[] homeAvail;
		ArrayList<Integer>[] awaySuspend;
		
		public SchoenbergerInstance(String filename){
			try {
				BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
				String line = reader.readLine();
				teamzahl = Integer.parseInt(line.split("=")[1]);
				reader.readLine();reader.readLine();reader.readLine();reader.readLine();
				line = reader.readLine();
				String[] lineParts = line.split("=")[1].split(",");
				abstaende = new int[teamzahl];
				for(int i = 0; i < teamzahl; i++){
					abstaende[i] = Integer.parseInt(lineParts[i]);
				}
				reader.readLine();
				homeAvail = new ArrayList[teamzahl];
				awaySuspend = new ArrayList[teamzahl];
				teamIDs = new int[teamzahl];
				for(int i = 0; i < teamzahl; i++){
					line = reader.readLine();
					homeAvail[i] = new ArrayList<Integer>();
					awaySuspend[i] = new ArrayList<Integer>();
					teamIDs[i] = Integer.parseInt(line.split(":")[1].split("=")[0]);
					lineParts = line.split("=")[1].split(",");
					for(int j = 0; j < lineParts.length; j++){
						homeAvail[i].add(Integer.parseInt(lineParts[j]));
					}
					line = reader.readLine();
					lineParts = line.split("=")[1].split(",");
					for(int j = 0; j < lineParts.length; j++){
						awaySuspend[i].add(Integer.parseInt(lineParts[j]));
					}
				}
				line = reader.readLine();
				n_min = Integer.parseInt(line.split("=")[1]);
				line = reader.readLine();
				n_max = Integer.parseInt(line.split("=")[1]);
			} catch (FileNotFoundException e) {
				System.err.println("File " + filename + " existiert auf dem System nicht");
			} catch (IOException e) {
				System.err.println("Fehler beim Lesen des Files");

			}
		}
	}
	
	public static String DateToFormat(Date date){
		String formatDate = "";
		formatDate += (date.getYear() + 1900) + "-";
		formatDate += (date.getMonth() + 1) + "-";
		formatDate += (date.getDate());
		return formatDate;
	}
	
	public static void main(String[] args) { 
		SchoenbergerInstance instance = new SchoenbergerInstance("INSTANCES/s1_i1_a10_b0.ttga");
		Date startDate = new Date(2016 - 1900,06,04);
		//Long Differenz zum Aufaddieren von 100 Tagen
		long hundret_diff = 8553600000l;
		Date endDate = new Date(startDate.getTime() + hundret_diff);		
		
		JsonObjectBuilder builder = Json.createObjectBuilder(); 
		
		//build options
		JsonObjectBuilder optionsBuilder = Json.createObjectBuilder();
		optionsBuilder.add("season_name", "KKl_1");
		optionsBuilder.add("start_at", DateToFormat(startDate));
		optionsBuilder.add("end_at", DateToFormat(endDate));
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
            FileWriter fw = new FileWriter("test.json"); 
            JsonWriter jsonWriter = Json.createWriter(fw); 
            jsonWriter.writeObject(jo); 
            jsonWriter.close(); 
            fw.close(); 
        } catch (IOException e) { 
            e.printStackTrace(); 
        } 
    } 

}
