import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Statistics {
    private int totalTraffic;
    private LocalDateTime minTime;
    private LocalDateTime maxTime;
    private final Map<String, Integer> osUsage = new HashMap<>();
    private final Map<String, Integer> browserUsage = new HashMap<>();
    private final Map<HttpMethod, Integer> methodCount = new HashMap<>();
    private final Map<Integer, Integer> responseCodeCount = new HashMap<>();
    private final Set<String> existingPages = new HashSet<>();
    private final Set<String> notFoundPages = new HashSet<>();
    private final Set<String> uniqueUserIps = new HashSet<>(); // Для уникальных IP пользователей (не ботов)
    private int botRequestsCount = 0; // Количество запросов от ботов
    private int errorRequestsCount = 0; // Количество ошибочных запросов (4xx и 5xx)
    private int userRequestsCount = 0; // Количество запросов от обычных пользователей (не ботов)

    public Statistics() {
        this.totalTraffic = 0;
        this.minTime = null;
        this.maxTime = null;
    }

    public void addEntry(LogEntry entry) {
        boolean isBot = entry.getUserAgent().getBrowser().toLowerCase().contains("bot") ||
                entry.getUserAgent().getOsType().toLowerCase().contains("bot");

        // Обновляем общий трафик
        this.totalTraffic += entry.getDataSize();

        // Обновляем временной диапазон
        LocalDateTime entryTime = entry.getTime();
        if (minTime == null || entryTime.isBefore(minTime)) {
            minTime = entryTime;
        }
        if (maxTime == null || entryTime.isAfter(maxTime)) {
            maxTime = entryTime;
        }

        // Добавляем страницу с кодом 200
        if (entry.getResponseCode() == 200) {
            existingPages.add(entry.getPath());
        }
        // Добавляем несуществующую страницу с кодом 404
        else if (entry.getResponseCode() == 404) {
            notFoundPages.add(entry.getPath());
        }

        // Проверяем на ошибочный запрос (4xx или 5xx)
        if (entry.getResponseCode() >= 400 && entry.getResponseCode() < 600) {
            errorRequestsCount++;
        }

        if (isBot) {
            botRequestsCount++;
        } else {
            userRequestsCount++;
            uniqueUserIps.add(entry.getIpAddress());
        }

        // Статистика по ОС
        String os = entry.getUserAgent().getOsType();
        osUsage.put(os, osUsage.getOrDefault(os, 0) + 1);

        // Статистика по браузерам
        String browser = entry.getUserAgent().getBrowser();
        browserUsage.put(browser, browserUsage.getOrDefault(browser, 0) + 1);

        // Статистика по методам HTTP
        HttpMethod method = entry.getMethod();
        methodCount.put(method, methodCount.getOrDefault(method, 0) + 1);

        // Статистика по кодам ответа
        int code = entry.getResponseCode();
        responseCodeCount.put(code, responseCodeCount.getOrDefault(code, 0) + 1);
    }

    // Метод подсчёта среднего количества посещений сайта за час (только обычные пользователи)
    public double getAverageVisitsPerHour() {
        if (minTime == null || maxTime == null || userRequestsCount == 0) {
            return 0.0;
        }
        long hoursBetween = ChronoUnit.HOURS.between(minTime, maxTime);
        return hoursBetween == 0 ? userRequestsCount : (double) userRequestsCount / hoursBetween;
    }

    // Метод подсчёта среднего количества ошибочных запросов в час
    public double getAverageErrorRequestsPerHour() {
        if (minTime == null || maxTime == null || errorRequestsCount == 0) {
            return 0.0;
        }
        long hoursBetween = ChronoUnit.HOURS.between(minTime, maxTime);
        return hoursBetween == 0 ? errorRequestsCount : (double) errorRequestsCount / hoursBetween;
    }

    // Метод расчёта средней посещаемости одним пользователем
    public double getAverageVisitsPerUser() {
        if (uniqueUserIps.isEmpty()) {
            return 0.0;
        }
        return (double) userRequestsCount / uniqueUserIps.size();
    }

    // Остальные методы остаются без изменений
    public Set<String> getNotFoundPages() {
        return new HashSet<>(notFoundPages);
    }

    public Map<String, Double> getBrowserStatistics() {
        Map<String, Double> browserStats = new HashMap<>();
        int total = browserUsage.values().stream().mapToInt(Integer::intValue).sum();
        for (Map.Entry<String, Integer> entry : browserUsage.entrySet()) {
            double ratio = total > 0 ? (double) entry.getValue() / total : 0;
            browserStats.put(entry.getKey(), ratio);
        }
        return browserStats;
    }

    public Set<String> getExistingPages() {
        return new HashSet<>(existingPages);
    }

    public Map<String, Double> getOsStatistics() {
        Map<String, Double> osStats = new HashMap<>();
        int total = osUsage.values().stream().mapToInt(Integer::intValue).sum();
        for (Map.Entry<String, Integer> entry : osUsage.entrySet()) {
            double ratio = total > 0 ? (double) entry.getValue() / total : 0;
            osStats.put(entry.getKey(), ratio);
        }
        return osStats;
    }

    public double getTrafficRate() {
        if (minTime == null || maxTime == null || totalTraffic == 0) {
            return 0.0;
        }
        long hoursBetween = ChronoUnit.HOURS.between(minTime, maxTime);
        return hoursBetween == 0 ? totalTraffic : (double) totalTraffic / hoursBetween;
    }

    public int getTotalTraffic() { return totalTraffic; }
    public Map<String, Integer> getOsUsage() { return new HashMap<>(osUsage); }
    public Map<String, Integer> getBrowserUsage() { return new HashMap<>(browserUsage); }
    public Map<HttpMethod, Integer> getMethodCount() { return new HashMap<>(methodCount); }
    public Map<Integer, Integer> getResponseCodeCount() { return new HashMap<>(responseCodeCount); }
}