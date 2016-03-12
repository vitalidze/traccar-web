package org.traccar.web.server.reports;

import org.traccar.web.shared.model.*;

import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReportGraph extends ReportGenerator {
    @Override
    void generateImpl(Report report) throws IOException {
        h2(report.getName());

        //     List<Position> position = entityManager.createQuery("SELECT p FROM Position p WHERE p.device = :device AND p.speed > 0 ORDER BY time DESC", Position.class)

        List<Position> positions = entityManager.createQuery("SELECT p FROM Position p" +
                " WHERE p.device IN :selectedDevices AND p.time BETWEEN :from AND :to ORDER BY p.time", Position.class)
                .setParameter("selectedDevices", getDevices(report))
                .setParameter("from", report.getFromDate())
                .setParameter("to", report.getToDate())
                .getResultList();


        panelStart();

        // heading
        panelHeadingStart();
        text("Graph ...");
        panelHeadingEnd();

        // body
        panelBodyStart();
        // period
        paragraphStart();
        bold(message("timePeriod") + ": ");
        text(formatDate(report.getFromDate()) + " - " + formatDate(report.getToDate()));
        paragraphEnd();
        
        addGraph(getDevices(report), positions);


        panelBodyEnd();

        panelEnd();


    }

    static class Data {
        final boolean idle;

        Position start;
        Position end;
        double topSpeed;
        double totalSpeed;
        double distance;
        int positionsWithSpeed;

        Data(boolean idle, Position start) {
            this.start = start;
            this.idle = idle;
        }

        long getDuration() {
            return end.getTime().getTime() - start.getTime().getTime();
        }

        double getAverageSpeed() {
            return totalSpeed / positionsWithSpeed;
        }
    }

 


    public void addGraph(List<Device> list, List<Position> positions){
        
        text("<div id=\"graphdiv\" style=\"width:900px; height:400px;\"></div>");
        text("<script type=\"text/javascript\">");
        text("g = new Dygraph(");

        // containing div
        text("document.getElementById(\"graphdiv\"),");

        text("\"");
        text(getData(list, positions));
        text("\"");

        text(","
                + "{"
                + "   connectSeparatedPoints: true,"
                + "   showRangeSelector: true"
                + " }"
                + " );");
        text("</script>");
    }

    
    
    String getData(List<Device> list, List<Position> positions) {
        String output="time";
        
        
        List<String> devices =  new ArrayList<String>() ;
        for (Device device: list) {
            devices.add(device.getName());
        }

        for (String device: devices) {
            output += ","+device;
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        for (Position position : positions) {
            String[] arrayData = new String[devices.size()];
            arrayData[devices.indexOf(position.getDevice().getName())] = getParametar(position.getOther(), "io66");
            output += "\\n"+dateFormat.format(position.getTime());
            for(int i = 0; i < devices.size(); i++)
                output += ","+arrayData[i];
        }
        return output;
    }

    
    public static String getParametar(String otherString, String param){
        // TODO move this code from this class
        String regex = "(?:,|\\{)?([^:]*):(\"[^\"]*\"|\\{[^}]*\\}|[^},]*)";
        Pattern p = Pattern.compile(regex);
        Matcher match = p.matcher(otherString);
        while (match.find()) {
            String value = match.group(2).toString().trim();
            String name = match.group(1);
            if (!(value.equals("\"\"") || (value.equals("[]")))) {
                //System.out.print("Name="+ name);
                //System.out.println(" Value="+ value);
                if(name.compareTo("\""+param+"\"") == 0)
                    return value;
            } 
        }
        return null;
    }


}


