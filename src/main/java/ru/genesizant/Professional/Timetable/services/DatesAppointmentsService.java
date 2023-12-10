package ru.genesizant.Professional.Timetable.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.controllers.specialist.calendar.StatusAdmissionTime;
import ru.genesizant.Professional.Timetable.dto.PersonFullName;
import ru.genesizant.Professional.Timetable.model.DatesAppointments;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.repositories.DatesAppointmentsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static ru.genesizant.Professional.Timetable.controllers.specialist.calendar.StatusAdmissionTime.RESERVED;

@Service
public class DatesAppointmentsService {

    private final DatesAppointmentsRepository datesAppointmentsRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public DatesAppointmentsService(DatesAppointmentsRepository datesAppointmentsRepository, ObjectMapper objectMapper) {
        this.datesAppointmentsRepository = datesAppointmentsRepository;
        this.objectMapper = objectMapper;
    }

    //Добавить в БД доступные даты и время на будущее
    public void addFreeDateSchedule(Person personSpecialist, String startDate, String endDate, String startTimeWork, String endTimeWork, String timeIntervalHour, StatusAdmissionTime status) {
        // Создаем объект LocalDate для начала даты
        LocalDate startDateObject = LocalDate.parse(startDate);
        // Создаем объект LocalDate для конца даты
        LocalDate endDateObject = LocalDate.parse(endDate);
        // Вычисляем количество дней между двумя датами
        int daysBetween = (int) ChronoUnit.DAYS.between(startDateObject, endDateObject);

        //Получить расписание в формате "Время":"Статус"
        Map<String, String> availableRecordingTime = availableRecordingTime(startTimeWork, endTimeWork, timeIntervalHour, status);

        for (int i = 0; i <= daysBetween; i++) {

            Optional<DatesAppointments> existingRecord = datesAppointmentsRepository.findByVisitDateAndSpecialistDateAppointmentsIdOrderById(startDateObject.plusDays(i), personSpecialist.getId());
            if (existingRecord.isPresent()) {
                // обновление существующей записи
                existingRecord.get().setScheduleTime(getScheduleJSON(availableRecordingTime));
                datesAppointmentsRepository.save(existingRecord.get());
            } else {
                // создание новой записи
                DatesAppointments newRecord = new DatesAppointments(startDateObject.plusDays(i), personSpecialist, getScheduleJSON(availableRecordingTime));
                datesAppointmentsRepository.save(newRecord);
            }

        }

    }


    //ToDo сделать утилитный класс для методов этого сервиса?
    //Получить возможное расписание на день "с" "по" с определенным интервалом в формате "Время":"Статус"
    private Map<String, String> availableRecordingTime(String startTimeWork, String endTimeWork, String timeIntervalHour, StatusAdmissionTime status) {
        LocalTime startTime = LocalTime.parse(startTimeWork);
        LocalTime endTime = LocalTime.parse(endTimeWork);
        LocalTime timeString = LocalTime.parse(timeIntervalHour);
        int intervalMinutes = timeString.getHour() * 60 + timeString.getMinute();

        List<String> timeList = new ArrayList<>();
        LocalTime currentTime = startTime;
        while (currentTime.isBefore(endTime)) {
            timeList.add(currentTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            currentTime = currentTime.plusMinutes(intervalMinutes);
        }

        String[] timeArray = timeList.toArray(new String[0]);
        Map<String, String> availableScheduleTime = new HashMap<>();
        for (String time : timeArray) {
            availableScheduleTime.put(time, status.getStatus());
        }
        return availableScheduleTime;
    }

    //Получить расписание (даты и вермя) по ИД спеца и установить статус
    public Map<LocalDate, Map<String, String>> getCalendarFreeScheduleById(long id) {
        List<DatesAppointments> allById = datesAppointmentsRepository.findAllBySpecialistDateAppointmentsIdOrderById(id);
        Map<LocalDate, Map<String, String>> freeSchedule = new HashMap<>();

        for (DatesAppointments datesAppointments : allById) {
            freeSchedule.put(datesAppointments.getVisitDate(), getAvailableTime(datesAppointments.getScheduleTime()));
        }

//        for (DatesAppointments datesAppointments : allById) {
//            freeSchedule.put(datesAppointments.getVisitDate(), datesAppointments.getScheduleTime());
//        }
        return freeSchedule;
    }

    //Получить расписание в формате Map<String, String>
    private Map<String, String> getAvailableTime(String scheduleTime) {

        Map<String, String> freeSchedule = new HashMap<>();
        try {
            Map<String, Object> scheduleMap = objectMapper.readValue(scheduleTime, new TypeReference<>() {});

            for (Map.Entry<String, Object> entry : scheduleMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof String) {
                    freeSchedule.put(key, (String) value);
                } else if (value instanceof Map) {
                    // Если значение является объектом, получаем его в виде JSON-строки
                    String nestedValue = objectMapper.writeValueAsString(value);
                    freeSchedule.put(key, nestedValue);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return freeSchedule;



        //РАБОТАЕЕЕЕЕЕЕЕЕЕЕЕЕЕЕЕЕЕЕЕЕЕЕЕЕЕЕЕЕЕЕЕЕЕТ!!!!!!!!!!!!!
//        ObjectMapper mapper = new ObjectMapper();
//        Map<String, String> freeSchedule = null;
//
//        try {
//            freeSchedule = mapper.readValue(scheduleTime, new TypeReference<>() {
//            });
//
//            System.out.println(freeSchedule);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return freeSchedule;
    }

    //получить из формата Map<String, String> расписание в JSON формате
    private String getScheduleJSON(Map<String, String> availableRecordingTime) {
        try {
            return objectMapper.writeValueAsString(availableRecordingTime);
        } catch (Exception e) {
            e.printStackTrace();
            return null; //ToDo решить как убрать возврат нулл
        }
    }

    public void deleteVisitDate(LocalDate date) {
        datesAppointmentsRepository.deleteByVisitDate(date); //ToDo вернуть ошибку если неверные данные
    }

    public void deleteByVisitDateBetween(LocalDate startDateRange, LocalDate endDateRange) {
        datesAppointmentsRepository.deleteByVisitDateBetween(startDateRange, endDateRange); //ToDo вернуть ошибку если неверные данные
    }

    public void deleteTimeAdmission(LocalDate date, String selectedTimeAdmission) {
        Optional<DatesAppointments> visitDate = datesAppointmentsRepository.findByVisitDate(date);
        if (visitDate.isPresent()) {
            Map<String, String> time = getAvailableTime(visitDate.get().getScheduleTime());
            if (time.containsKey(selectedTimeAdmission)) {
                time.remove(selectedTimeAdmission);
                visitDate.get().setScheduleTime(getScheduleJSON(time));
                datesAppointmentsRepository.save(visitDate.get());
            } else {
                //ToDo вернуть ошибку про несуществующее время. Мб сделать через @Valid?
            }
        } else {
            //ToDo вернуть ошибку про несуществующую дату. Мб сделать через @Valid?
        }
    }

    public void deleteTimeRangeAdmission(LocalDate date, String startTimeAdmission, String endTimeAdmission) {
        Optional<DatesAppointments> visitDate = datesAppointmentsRepository.findByVisitDate(date);
        if (visitDate.isPresent()) {
            Map<String, String> time = getAvailableTime(visitDate.get().getScheduleTime());
            if (time.containsKey(startTimeAdmission) && time.containsKey(endTimeAdmission)) { //ToDo сделать метод для проверки на входит ли заданный диапазон времени в существующее время или просто удалить все что входит в диапазон который задал юзер

                List<String> timeAdmissionToRemove = getListTimeAdmission(startTimeAdmission, endTimeAdmission, time);

                for (String key : timeAdmissionToRemove) {
                    time.remove(key);
                }

                visitDate.get().setScheduleTime(getScheduleJSON(time));
                datesAppointmentsRepository.save(visitDate.get());
            } else {
                //ToDo вернуть ошибку про несуществующее время. Мб сделать через @Valid?
            }
        } else {
            //ToDo вернуть ошибку про несуществующую дату. Мб сделать через @Valid?
        }
    }

    private boolean isTimeInRange(String time, String startTimeAdmission, String endTimeAdmission) {
        return time.compareTo(startTimeAdmission) >= 0 && time.compareTo(endTimeAdmission) <= 0;
    }

    public void setStatusTimeAdmission(LocalDate date, String timeAdmission, StatusAdmissionTime status) {
        Optional<DatesAppointments> visitDate = datesAppointmentsRepository.findByVisitDate(date);
        if (visitDate.isPresent()) {
            Map<String, String> time = getAvailableTime(visitDate.get().getScheduleTime());
            if (time.containsKey(timeAdmission)) {
                time.put(timeAdmission, status.getStatus());
                visitDate.get().setScheduleTime(getScheduleJSON(time));
                datesAppointmentsRepository.save(visitDate.get());
            } else {
                //ToDo вернуть ошибку про несуществующее время. Мб сделать через @Valid?
            }
        } else {
            //ToDo вернуть ошибку про несуществующую дату. Мб сделать через @Valid?
        }
    }

    public void setStatusRangeTimeAdmission(LocalDate date, String startTimeAdmission, String endTimeStartAdmission, StatusAdmissionTime status) {
        Optional<DatesAppointments> visitDate = datesAppointmentsRepository.findByVisitDate(date);
        if (visitDate.isPresent()) {
            Map<String, String> time = getAvailableTime(visitDate.get().getScheduleTime());
            if (time.containsKey(startTimeAdmission) && time.containsKey(endTimeStartAdmission)) { //ToDo сделать метод для проверки на входит ли заданный диапазон времени в существующее время или просто удалить все что входит в диапазон который задал юзер

                List<String> timeAdmissionToChangeStatus = getListTimeAdmission(startTimeAdmission, endTimeStartAdmission, time);

                for (String key : timeAdmissionToChangeStatus) {
                    time.put(key, status.getStatus());
                }

                visitDate.get().setScheduleTime(getScheduleJSON(time));
                datesAppointmentsRepository.save(visitDate.get());

            } else {
                //ToDo вернуть ошибку про несуществующее время. Мб сделать через @Valid?
            }
        } else {
            //ToDo вернуть ошибку про несуществующую дату. Мб сделать через @Valid?
        }
    }

    private List<String> getListTimeAdmission(String startTimeAdmission, String endTimeStartAdmission, Map<String, String> time) {
        List<String> listTimeAdmission = new ArrayList<>();
        for (Map.Entry<String, String> entry : time.entrySet()) {
            String key = entry.getKey();
            if (isTimeInRange(key, startTimeAdmission, endTimeStartAdmission)) {
                listTimeAdmission.add(key);
            }
        }
        return listTimeAdmission;
    }

    public void clearDateVisitBefore(LocalDate date) {
        datesAppointmentsRepository.deleteByVisitDateBefore(date);
    }

    public void addTimeAvailability(LocalDate date, String timeAvailability, StatusAdmissionTime status) {
        Optional<DatesAppointments> visitDate = datesAppointmentsRepository.findByVisitDate(date);
        if (visitDate.isPresent()) {
            Map<String, String> time = getAvailableTime(visitDate.get().getScheduleTime());
            if (!time.containsKey(timeAvailability)) {
                time.put(timeAvailability, status.getStatus());
                visitDate.get().setScheduleTime(getScheduleJSON(time));
                datesAppointmentsRepository.save(visitDate.get());
            } else {
                //ToDo вернуть ошибку про существующее время. Мб сделать через @Valid?
            }
        } else {
            //ToDo вернуть ошибку про несуществующую дату. Мб сделать через @Valid?
        }
    }

    public void addRangeTimeAvailability(LocalDate date, String startTimeAvailability, String endTimeAvailability, String intervalHour, StatusAdmissionTime status) {
        Optional<DatesAppointments> visitDate = datesAppointmentsRepository.findByVisitDate(date);
        if (visitDate.isPresent()) {
            Map<String, String> time = getAvailableTime(visitDate.get().getScheduleTime()); //ToDo сделать проверку на добавление доступного диапазона времени
            time.putAll(availableRecordingTime(startTimeAvailability, endTimeAvailability, intervalHour, status));
            visitDate.get().setScheduleTime(getScheduleJSON(time));
            datesAppointmentsRepository.save(visitDate.get());
        } else {
            //ToDo вернуть ошибку про несуществующую дату. Мб сделать через @Valid?
        }
    }

    //Записать(обозначить как забронированное время) Клиента на прием
    public void enrollVisitorNewAppointments(LocalDateTime meeting, PersonFullName personFullName, Long specialistId) {

        Optional<DatesAppointments> datesAppointments = datesAppointmentsRepository.findByVisitDateAndSpecialistDateAppointmentsIdOrderById(meeting.toLocalDate(), specialistId);

        if (datesAppointments.isPresent()) {
            String scheduleTime = datesAppointments.get().getScheduleTime();
            try {
                // Преобразование JSON-строки в объект JsonNode
                JsonNode jsonNode = objectMapper.readTree(scheduleTime);

                // создаем новый объект ObjectNode для замены значения
                ObjectNode objectNode = objectMapper.createObjectNode();

                objectNode.put(RESERVED.getStatus(), personFullName.toString());

                // заменяем значение в объекте JsonNode
                ((ObjectNode) jsonNode).set(String.valueOf(meeting.toLocalTime()), objectNode);

                // преобразуем объект JsonNode обратно в строку JSON
                String updatedScheduleTime = objectMapper.writeValueAsString(jsonNode);

                datesAppointments.get().setScheduleTime(updatedScheduleTime);

                datesAppointmentsRepository.save(datesAppointments.get());

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

}