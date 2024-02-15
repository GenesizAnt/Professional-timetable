package ru.genesizant.Professional.Timetable.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NodeCalendarDay {

    /*

        String jsonExp = "{\"14:00\":{\"Забронировано\":\"Анна Лысенко Михайловна\"},\"10:00\":\"Доступно\",\"12:00\":\"Доступно\",\"16:00\":\"Доступно\"}";
        NodeCalendarDay calendarDay = new NodeCalendarDay();
        List<NodeCalendarDay> days = calendarDay.newNodeCalendarDay(nameM, jsonExp);
        System.out.println(days);

     */

    ObjectMapper objectMapper = new ObjectMapper();
    private String time;
    private String status;
    private String name;
    private List<NodeCalendarDay> calendarDays = new ArrayList<>();

    private final String recordAdmission = "Запись";
    private final String needApproved = "Требует подтверждения";
    private final String approved = "Запись подтверждена";

    public NodeCalendarDay() {

    }

    public NodeCalendarDay(String time, String status, String name) {
        this.time = time;
        this.status = status;
        this.name = name;
    }

    public List<NodeCalendarDay> newNodeCalendarDay(String nameClient, String json) {


        try {


            Map<String, Object> scheduleMap = objectMapper.readValue(json, new TypeReference<>() {});

            for (Map.Entry<String, Object> entry : scheduleMap.entrySet()) {
                NodeCalendarDay calendarDay = new NodeCalendarDay();
                calendarDay.setName(nameClient);

                String key = entry.getKey();
                Object value = entry.getValue();

                calendarDay.setTime(key);

                if (value instanceof String) {

                    calendarDay.setStatus((String) value);

                } else if (value instanceof Map) {

                    Map<String, String> valueMap = (Map<String, String>) value;

                    for (String statusValue : valueMap.keySet()) {
                        if (nameClient.equals(valueMap.get(statusValue))) {
                            if (statusValue.equals("Забронировано")) {
                                calendarDay.setStatus(approved);
                            }
                        }
                    }

                    // Если значение является объектом, получаем его в виде JSON-строки
//                    String nestedValue = objectMapper.writeValueAsString(value);
//                    JsonNode jsonNodeInput = objectMapper.readTree(nestedValue);

//                    String inputName = RESERVED.getStatus() + " : " + jsonNodeInput.get(RESERVED.getStatus()).asText();
//
//                    freeSchedule.put(key, inputName);
                }
                calendarDays.add(calendarDay);
            }



//            for (String time : scheduleMap.keySet()) {
//                this.time = time;
//                String s = scheduleMap.get(time);
//                if (scheduleMap.get(time))
//            }





        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return calendarDays;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<NodeCalendarDay> getCalendarDays() {
        return calendarDays;
    }

    public void setCalendarDays(List<NodeCalendarDay> calendarDays) {
        this.calendarDays = calendarDays;
    }

    public String getRecordAdmission() {
        return recordAdmission;
    }

    public String getNeedApproved() {
        return needApproved;
    }

    public String getApproved() {
        return approved;
    }

    @Override
    public String toString() {
        return "NodeCalendarDay{" +
                "time='" + time + '\'' +
                ", status='" + status + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
