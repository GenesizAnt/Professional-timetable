package ru.genesizant.Professional.Timetable.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.enums.StatusAdmissionTime;
import ru.genesizant.Professional.Timetable.dto.PersonFullName;
import ru.genesizant.Professional.Timetable.enums.StatusPerson;
import ru.genesizant.Professional.Timetable.model.DatesAppointments;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.repositories.DatesAppointmentsRepository;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static ru.genesizant.Professional.Timetable.enums.DayOfWeekRus.getRusDayWeek;
import static ru.genesizant.Professional.Timetable.enums.StatusAdmissionTime.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class DatesAppointmentsService {

    private final DatesAppointmentsRepository datesAppointmentsRepository;
    private final ObjectMapper objectMapper;

    //Добавить в БД доступные даты и время на будущее (заполнение календаря на будущий период)
    public void addFreeDateSchedule(Person personSpecialist, String startDate, String endDate, String startTimeWork,
                                    String endTimeWork, List<String> weekDays, String timeIntervalHour, StatusAdmissionTime status) {
        // Создаем объект LocalDate для начала даты
        LocalDate startDateObject = LocalDate.parse(startDate);
        // Создаем объект LocalDate для конца даты
        LocalDate endDateObject = LocalDate.parse(endDate);
        // Вычисляем количество дней между двумя датами
        int daysBetween = (int) ChronoUnit.DAYS.between(startDateObject, endDateObject);

        //Получить расписание в формате "Время":"Статус"
        Map<String, String> availableRecordingTime = availableRecordingTime(startTimeWork, endTimeWork, timeIntervalHour, status);

        for (int i = 0; i <= daysBetween; i++) {
            if (!weekDays.contains(getRusDayWeek(startDateObject.plusDays(i).getDayOfWeek().name()))) {
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
    }


    //ToDo сделать утилитный класс для методов этого сервиса?
    //Парсинг расписания в Map<String, String> из заданных параметров даты/время/интервал
    // Расписание на день "с" "по" с определенным интервалом в формате "Время":"Статус"
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

//        Map<LocalDate, Map<String, String>> datesNotSorted = datesAppointmentsService.getCalendarFreeScheduleById((long) request.getSession().getAttribute("id"));

        // Создаем новую Map для хранения отсортированных значений
        Map<LocalDate, Map<String, String>> datesNoSortedDate = new LinkedHashMap<>();

        // Сортировка значений внутренней Map и вставка их в отсортированную Map
        freeSchedule.forEach((key, value) -> {
            Map<String, String> sortedInnerMap = value.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
            datesNoSortedDate.put(key, sortedInnerMap);
        });

        // Сортировка ключей Map и вставка их в отсортированную Map
        Map<LocalDate, Map<String, String>> sortedFreeSchedule = datesNoSortedDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

        return sortedFreeSchedule;
    }

    //Получить расписание в формате Map<String, String> где Map<Время, Статус доступности>
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
                    JsonNode jsonNodeInput = objectMapper.readTree(nestedValue);
                    String fieldName = jsonNodeInput.fieldNames().next();
                    if (fieldName.equals(RESERVED.getStatus())) {
                        String inputName = RESERVED.getStatus() + " : " + jsonNodeInput.get(fieldName).asText();
                        freeSchedule.put(key, inputName);
                    } else if (fieldName.equals(CONFIRMED.getStatus())) {
                        String inputName = CONFIRMED.getStatus() + " : " + jsonNodeInput.get(fieldName).asText();
                        freeSchedule.put(key, inputName);
                    } else if (fieldName.equals(NEED_CONFIRMATION.getStatus())) {
                        String inputName = NEED_CONFIRMATION.getStatus() + " : " + jsonNodeInput.get(fieldName).asText();
                        freeSchedule.put(key, inputName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Ошибка получения расписания в формате Map<String, String> где Map<Время, Статус доступности>. Переданное расписание: " + scheduleTime + " Текст ошибки: " + e.getMessage());
        }
        return freeSchedule;
    }

    //Получить расписание (даты и вермя) по ИД спеца и установить статус ДЛЯ КЛИЕНТА
    //ToDo как убрать дублирующий код с предыдущим
    public Map<LocalDate, Map<String, String>> getCalendarFreeScheduleForVisitor(long id, String personFullNameRegistered) {
        List<DatesAppointments> allById = datesAppointmentsRepository.findAllBySpecialistDateAppointmentsIdOrderById(id);
        Map<LocalDate, Map<String, String>> freeSchedule = new HashMap<>();

        for (DatesAppointments datesAppointments : allById) {
            freeSchedule.put(datesAppointments.getVisitDate(), getAvailableTimeForVisitor(datesAppointments.getScheduleTime(), personFullNameRegistered));
        }

//        Map<LocalDate, Map<String, String>> datesNotSorted = datesAppointmentsService.getCalendarFreeScheduleById((long) request.getSession().getAttribute("id"));

        // Создаем новую Map для хранения отсортированных значений
        Map<LocalDate, Map<String, String>> datesNoSortedDate = new LinkedHashMap<>();

        // Сортировка значений внутренней Map и вставка их в отсортированную Map
        freeSchedule.forEach((key, value) -> {
            Map<String, String> sortedInnerMap = value.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
            datesNoSortedDate.put(key, sortedInnerMap);
        });

        // Сортировка ключей Map и вставка их в отсортированную Map
        Map<LocalDate, Map<String, String>> sortedFreeSchedule = datesNoSortedDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));


        return sortedFreeSchedule;
    }

    //Получить расписание в формате Map<String, String> для клиента
    private Map<String, String> getAvailableTimeForVisitor(String scheduleTime, String personFullNameRegistered) {

        Map<String, String> freeSchedule = new HashMap<>();
        try {
            Map<String, Object> scheduleMap = objectMapper.readValue(scheduleTime, new TypeReference<>() {
            });

            for (Map.Entry<String, Object> entry : scheduleMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof String) {
                    freeSchedule.put(key, (String) value);
                } else if (value instanceof Map) {
                    // Если значение является объектом, получаем его в виде JSON-строки
                    String nestedValue = objectMapper.writeValueAsString(value);
                    JsonNode jsonNode = objectMapper.readTree(nestedValue);
                    String nestedValueName = jsonNode.get(RESERVED.getStatus()).asText();
                    String nestedValueStatus = jsonNode.fieldNames().next();
                    if (nestedValueName.equals(personFullNameRegistered)) {
//                        JsonNode jsonNodeInput = objectMapper.readTree(nestedValue);
                        String inputName = RESERVED.getStatus() + " : " + jsonNode.get(RESERVED.getStatus()).asText();
                        freeSchedule.put(key, inputName);
                    } else {
                        freeSchedule.put(key, nestedValueStatus);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Ошибка получения расписания в формате Map<String, String> для клиента. Переданное расписание: " + scheduleTime + ". Для клиента: " + personFullNameRegistered + " Текст ошибки: " + e.getMessage());
        }
        return freeSchedule;
    }


    //получить из формата Map<String, String> расписание в JSON формате
    private String getScheduleJSON(Map<String, String> availableRecordingTime) {
        try {
            return objectMapper.writeValueAsString(availableRecordingTime);
        } catch (Exception e) {
            log.error("Ошибка получения расписание из формата Map<String, String> расписание в JSON формате. Переданное расписание: " + availableRecordingTime + " Текст ошибки: " + e.getMessage());
            return null; //ToDo решить как убрать возврат нулл
        }
    }

    //Удалить полный День из доступных для выбора дат
    public void deleteVisitDate(LocalDate date) {
        datesAppointmentsRepository.deleteByVisitDate(date); //ToDo вернуть ошибку если неверные данные
    }

    //Удалить Диапазон Дней из доступных для выбора дат
    public void deleteByVisitDateBetween(LocalDate startDateRange, LocalDate endDateRange) {
        datesAppointmentsRepository.deleteByVisitDateBetween(startDateRange, endDateRange); //ToDo вернуть ошибку если неверные данные
    }

    //Удалить конкретное Время из доступного дня
    public void deleteTimeAdmission(LocalDate date, String selectedTimeAdmission, long specialistId) {
        Optional<DatesAppointments> visitDate = datesAppointmentsRepository.findByVisitDateAndSpecialistDateAppointmentsIdOrderById(date, specialistId);
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

    //Удалить диапазон Времени из доступного дня
    public void deleteTimeRangeAdmission(LocalDate date, String startTimeAdmission, String endTimeAdmission, long specialistId) {
        Optional<DatesAppointments> visitDate = datesAppointmentsRepository.findByVisitDateAndSpecialistDateAppointmentsIdOrderById(date, specialistId);
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

    //Установить указанный статус консультации на конкретную дату и время
    public void setStatusTimeAdmission(LocalDate date, String timeAdmission, StatusAdmissionTime status, long specialistId) {
        Optional<DatesAppointments> visitDate = datesAppointmentsRepository.findByVisitDateAndSpecialistDateAppointmentsIdOrderById(date, specialistId);
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

    //Установить указанный статус консультации на диапазон дат и время
    public void setStatusRangeTimeAdmission(LocalDate date, String startTimeAdmission, String endTimeStartAdmission, StatusAdmissionTime status, long specialistId) {
        Optional<DatesAppointments> visitDate = datesAppointmentsRepository.findByVisitDateAndSpecialistDateAppointmentsIdOrderById(date, specialistId);
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

    // Получить встречи, которые в рамках указанного диапазона времени
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

    //Удалить все Даты из календаря до указанной даты
    public void clearDateVisitBefore(LocalDate date) {
        datesAppointmentsRepository.deleteByVisitDateBefore(date);
    }

    //Добавить время приема в определенный день
    public void addTimeAvailability(long specialistId, LocalDate date, String timeAvailability, StatusAdmissionTime status) {
        Optional<DatesAppointments> visitDate = datesAppointmentsRepository.findByVisitDateAndSpecialistDateAppointmentsIdOrderById(date, specialistId);
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

    //Добавить диапазон времени приема в определенный день
    public void addRangeTimeAvailability(LocalDate date, String startTimeAvailability, String endTimeAvailability, String intervalHour, StatusAdmissionTime status, Long specialistId) {
        Optional<DatesAppointments> visitDate = datesAppointmentsRepository.findByVisitDateAndSpecialistDateAppointmentsIdOrderById(date, specialistId);
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
    public void enrollVisitorNewAppointments(LocalDateTime meeting, PersonFullName personFullName, Long specialistId, StatusPerson statusPerson) {
        Optional<DatesAppointments> datesAppointments = datesAppointmentsRepository.findByVisitDateAndSpecialistDateAppointmentsIdOrderById(meeting.toLocalDate(), specialistId);
        if (datesAppointments.isPresent()) {
            String scheduleTime = datesAppointments.get().getScheduleTime();
            try {
                // Преобразование JSON-строки в объект JsonNode
                JsonNode jsonNode = objectMapper.readTree(scheduleTime);
                // создаем новый объект ObjectNode для замены значения
                ObjectNode objectNode = objectMapper.createObjectNode();
                switch (statusPerson) {
                    case VISITOR -> objectNode.put(NEED_CONFIRMATION.getStatus(), personFullName.toString());
                    case SPECIALIST -> objectNode.put(CONFIRMED.getStatus(), personFullName.toString());
                }
                // заменяем значение в объекте JsonNode
                ((ObjectNode) jsonNode).set(String.valueOf(meeting.toLocalTime()), objectNode);
                // преобразуем объект JsonNode обратно в строку JSON
                String updatedScheduleTime = objectMapper.writeValueAsString(jsonNode);
                datesAppointments.get().setScheduleTime(updatedScheduleTime);
                datesAppointmentsRepository.save(datesAppointments.get());
            } catch (JsonProcessingException e) {
                log.error("Ошибка записи (обозначить как забронированное время) Клиента на прием. Дата бронирования: " + meeting +
                        "Клиент: " + personFullName + "ИД спеца: " + specialistId + " Текст ошибки: " + e.getMessage());
            }
        }
    }

    //Отмена записи ранее записанного клиента
    public void cancellingBookingAppointments(LocalDateTime meeting, Long specialistId) {
        Optional<DatesAppointments> datesAppointments = datesAppointmentsRepository.findByVisitDateAndSpecialistDateAppointmentsIdOrderById(meeting.toLocalDate(), specialistId);
        if (datesAppointments.isPresent()) {
            String scheduleTime = datesAppointments.get().getScheduleTime();
            try {
                // Преобразование JSON-строки в объект JsonNode
                JsonNode jsonNode = objectMapper.readTree(scheduleTime);
                ((ObjectNode) jsonNode).remove(String.valueOf(meeting.toLocalTime()));
                ((ObjectNode) jsonNode).put(String.valueOf(meeting.toLocalTime()), AVAILABLE.getStatus());
                // преобразуем объект JsonNode обратно в строку JSON
                String updatedScheduleTime = objectMapper.writeValueAsString(jsonNode);
                datesAppointments.get().setScheduleTime(updatedScheduleTime);
                datesAppointmentsRepository.save(datesAppointments.get());
            } catch (JsonProcessingException e) {
                log.error("Ошибка отмены записи Клиента на прием. Дата бронирования: " + meeting + "ИД спеца: " + specialistId + " Текст ошибки: " + e.getMessage());
            }
        }
    }

    //Получить 5 ближайших дат из существующего расписания
    public List<String> getFiveNearestDates(Map<LocalDate, Map<String, String>> schedule, String personFullName) {
        List<String> fiveNearestDates = new ArrayList<>();
        LocalDate now = LocalDate.now();
        List<LocalDate> nearestDates = schedule.keySet().stream()
                .filter(date -> !date.isBefore(now)) // исключаем даты, предшествующие текущей дате
                .sorted(Comparator.comparingLong(date -> ChronoUnit.DAYS.between(now, date)))
                .limit(5).toList();
        for (LocalDate nearestDate : nearestDates) {
            String[][] calendarForView = getCalendarForClient(personFullName, nearestDate, schedule.get(nearestDate));
            try {
                fiveNearestDates.add(objectMapper.writeValueAsString(calendarForView));
            } catch (Exception e) {
                log.error("Ошибка формирования JSON из календаря:" + Arrays.deepToString(calendarForView) + ". Текст сообщения - " + e.getMessage());
            }
        }
        return fiveNearestDates;
    }

    // Получение данных по конкретной дате в виде двумерного массива, необходимо для корректного преобразования в json
    public String[][] getCalendarForClient(String namePerson, LocalDate date, Map<String, String> json) {
        String[][] calendarForClient = new String[20][2];
        calendarForClient[0][0] = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")); // первая строка таблицы - дата
        calendarForClient[0][1] = getRusDayWeek(date.getDayOfWeek().name()); // вторая строка таблицы - день недели
        int count = 2; // третья строка таблицы и далее - заполнение таблицы с третьей строки
        //ToDo попробовать такой вариант, только решить вопрос первой маленькой буквы LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ru"))
        calendarForClient[1] = new String[]{"Время", "Бронь", "Статус"};
        Map<String, String> sortedScheduleMap = new TreeMap<>(json);
        for (Map.Entry<String, String> entry : sortedScheduleMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value.contains(":")) {
                String[] values = value.split(":");
                String statusValue = values[0].trim();
                String nameVal = values[1].trim();
                if (namePerson.equals(nameVal) || namePerson.equals("specialist")) {
                    calendarForClient[count] = new String[]{key, nameVal, statusValue}; //ToDo "Забронировано" поменять на Запись подтверждена
                    count++;
                }
            } else if (value.equals("Доступно")) {
                calendarForClient[count] = new String[]{key, "---", "Доступно"}; //ToDo "Доступно" поменять на Доступно для записи
                count++; //ToDo нужно ли что то вставить вместо ""
            }
        }
        return Arrays.stream(calendarForClient)
                .filter(row -> Arrays.stream(row, 1, row.length).allMatch(Objects::nonNull))
                .toArray(String[][]::new);
    }

    //ToDo Удалить??
    public boolean isCheckAvailableCancellingBooking(LocalDateTime meeting, String personFullNameRegistered, Long specialistId) {

        Optional<DatesAppointments> datesAppointments = datesAppointmentsRepository.findByVisitDateAndSpecialistDateAppointmentsIdOrderById(meeting.toLocalDate(), specialistId);

        if (datesAppointments.isPresent()) {
            String scheduleTime = datesAppointments.get().getScheduleTime();
            try {
                // Преобразование JSON-строки в объект JsonNode
                JsonNode jsonNode = objectMapper.readTree(scheduleTime);

                return jsonNode.get(String.valueOf(meeting.toLocalTime())).get(RESERVED.getStatus()).asText().equals(personFullNameRegistered);

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    //Проверяет переданные даты от специалиста для создания расписания, на пересечение уже созданного расписания
    public boolean isDateWithinRangeOfAppointments(String startDate, String endDate, Long id) {
        List<DatesAppointments> allVisitDatesBySpecialistId = datesAppointmentsRepository.findAllVisitDatesBySpecialistDateAppointmentsId(id);
        LocalDate startDateObject = LocalDate.parse(startDate);
        LocalDate endDateObject = LocalDate.parse(endDate);
        for (DatesAppointments datesAppointments : allVisitDatesBySpecialistId) {
            LocalDate date = datesAppointments.getVisitDate();
            if (date.isEqual(startDateObject) || date.isEqual(endDateObject)
                    || (date.isAfter(startDateObject) && date.isBefore(endDateObject))) {
                return true;
            }
        }
        return false;
    }

//    public Optional<DatesAppointments> getAppointmentsByDate(@NotNull LocalDateTime meetingCancel) {
//        return datesAppointmentsRepository.findByVisitDate(meetingCancel.toLocalDate());
//    }
}