package org.traccar.web.server.reports;

import org.traccar.web.shared.model.*;

import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        try {
            addGraph(getDevices(report), positions);
        } catch (TraccarException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


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




    public void addGraph(List<Device> list, List<Position> positions) throws TraccarException{

        text("<div id=\"graphdiv\" style=\"width:1100px; height:400px;\"></div>");
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



    String getData(List<Device> listDevices, List<Position> positions) throws TraccarException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String output="time";


        Map<Long, List<String>> keysMap = new HashMap<>();
        Map<String, String> deviceSensorMap = new HashMap<>();
        Map<String, Integer> deviceSensorOrderMap = new HashMap<>();
System.out.println("broj uredjaja"+listDevices.size());
        for (Device device: listDevices) {
            List<String> keys = new ArrayList<String>();
System.out.println("broj senzora"+device.getName()+" "+device.getSensors());

            if(device.getSensors()==null)
               device = dataService.loadSensors(device);
            
            for (Sensor sensor: device.getSensors()) {
                if(sensor.isOnGraph()){
                    String key = device.getName() + "("+sensor.getName()+")";
                    keys.add(key);
                    int j = deviceSensorMap.size();
                    deviceSensorMap.put(key, sensor.getParameterName());
                    if(deviceSensorMap.size()>j){
                        deviceSensorOrderMap.put(key, deviceSensorMap.size()-1);
                        output += ","+key;                        
                    }
                }
            }
            keysMap.put(device.getId(), keys);
        }


        String[] arrayData = null;
        for (Position position : positions) {
            for (Device device: listDevices) {
                if(device.getId()==position.getDevice().getId()){
                    arrayData = new String[deviceSensorMap.size()];
                    for(String key: keysMap.get(device.getId())){ 
                        int tmpPos = deviceSensorOrderMap.get(key);
                        System.out.println( key +"-"+ tmpPos);
                        arrayData[tmpPos] = getParametar(position.getOther(), deviceSensorMap.get(key));
                    }
                }
            }
            output += "\\n"+dateFormat.format(position.getTime());
            for(int i = 0; i < deviceSensorMap.size(); i++){
                output += ","+arrayData[i];
            }
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
                if(name.compareTo("\""+param+"\"") == 0)
                    return value;
            } 
        }
        return null;
    }


}


