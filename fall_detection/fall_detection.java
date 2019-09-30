import java.lang.Math;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;



public class fall_detection{


public static void main(String [] args) {

    String csvFile = "CompleteDataSet_training_competition.csv";
    csvFile = "sebi.csv";
    String line = "";
    String cvsSplitBy = ",";
    
    class Row{
        double accx;
        double accy;
        double accz;

        public Row(double x, double y, double z){
            this.accx = x;
            this.accy = y;
            this.accz = z;

        }
    }
    
    ArrayList<Row> acc = new ArrayList<Row>();

    try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

        while ((line = br.readLine()) != null) {

            // use comma as separator
        	
            String[] data = line.split(cvsSplitBy);
            
            if (data[0].compareTo("date") == 0){
            	continue;
            }
            
            
            double x = Double.parseDouble(data[4]);
            double y = Double.parseDouble(data[5]);
            double z = Double.parseDouble(data[6]);
            
            
            Row row= new Row(x,y,z);

            
            acc.add(row);

        }

    } catch (IOException e) {
        e.printStackTrace();
    }
    double [] accx = new double[acc.size()];
    double [] accy = new double[acc.size()];
    double [] accz = new double[acc.size()];
    
    for(int i = 0; i<acc.size(); i++){
    	Row curr = acc.get(i);
    	accx[i] = curr.accx;
    	accy[i] = curr.accy;
    	accz[i] = curr.accz;
    }
    
    System.out.println(fall_detections(accx,accy,accz,102.4));

}


public static boolean fall_detections(double [] acc_x, double [] acc_y, double [] acc_z, double fs){

    double peak_thr = 7.0;
    int time_diff = (int)(2*fs);
    int pause = (int)(0.5*fs);
    int time_b = (int)(1.5*fs);
    double sec_impact = 1.9;
    double [] norm = new double [acc_x.length];
    boolean fall = false;

    for(int i = 0; i < norm.length; i++){
        norm[i] = Math.sqrt((acc_x[i]*acc_x[i])+(acc_y[i]*acc_y[i]) + (acc_z[i]*acc_z[i]));     
    }

    // intervalle für winkel
    int start_engel = (int) Math.toRadians(60);
    int stop_engel = (int) Math.toRadians(120);
    
    class Event{
        int index;
        double norm;

        public Event(int i, double n){
            this.index = i;
            this.norm = n;

        }
    }
    
    ArrayList<Event> impact = new ArrayList<Event>();

    for (int i = time_b; i < norm.length-(time_diff+pause); i++){

        if (norm[i] >= peak_thr){
        	Event a = new Event(i, norm[i]);
            impact.add(a);
        }
    }

    for (int i = 0; i < impact.size(); i++){
    
        boolean cancel = false;
        for (int j = 0; j < time_diff; j++){
            int index = impact.get(i).index + pause + j;
            if(norm[index]>= sec_impact){
                cancel = true;
                break;

            }
        }
        if (cancel == true){
        	
            continue;
        }
        System.out.print(impact.get(i).norm);
        System.out.print(" Index: ");
        System.out.println(impact.get(i).index);
        int index_m = impact.get(i).index - time_b;
        double mean_accx = 0;
        double mean_accy = 0;
        double mean_accz = 0;
        int counter = 0;

        for (int k= index_m; k < impact.get(i).index - pause; k++){
            mean_accx+= acc_x[k];
            mean_accy+= acc_y[k];
            mean_accz+= acc_z[k];
            counter ++;
        }

        mean_accx = mean_accx/counter;
        mean_accy = mean_accy/counter;
        mean_accz = mean_accz/counter;

        double max = mean_accx;
        String max_axis = "x";
        double angle = 0;

        if (Math.abs(max) < Math.abs(mean_accy)){
            max = mean_accy;
            max_axis = "y";
        }
        if (Math.abs(max) < Math.abs(mean_accz)){
            max = mean_accz;
            max_axis = "z";
        }
        angle = Math.acos(max/1);
        int index_new = impact.get(i).index + pause;
        double mean_new = 0;
        counter = 0;
        double [] axis = acc_x;

        if ( 0 == max_axis.compareTo("y")){
            axis = acc_y;
        }
        if(0 == max_axis.compareTo("z")){
            axis = acc_z;
        }

        for (int k = index_new; k < index_new + time_diff; k++){
            mean_new += axis[k];
            counter ++;
        }
        mean_new = mean_new/counter;
        double engel_new = Math.acos(mean_new/1);
        
        if ((Math.abs(angle-engel_new) < start_engel) || (Math.abs(angle-engel_new)> stop_engel)){
            continue;
        }
        //System.out.println(impact.get(i).norm);
       // System.out.println(" Stunde: ");
        //System.out.println(impact.get(i).index / fs / 3600);
        fall = true;
    }
    return fall;
}
}


