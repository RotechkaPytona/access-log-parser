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

    public Statistics() {
        this.totalTraffic = 0;
        this.minTime = null;
        this.maxTime = null;
    }

    public void addEntry(LogEntry entry) {
        // Обновление общего трафика
        this.totalTraffic += entry.getDataSize();

        // Обновление временного диапазона
        LocalDateTime entryTime = entry.getTime();
        if (minTime == null || entryTime.isBefore(minTime)) {
            minTime = entryTime;
        }
        if (maxTime == null || entryTime.isAfter(maxTime)) {
            maxTime = entryTime;
        }

        if (entry.getResponseCode() == 200) {
            existingPages.add(entry.getPath());
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

    // Новый метод: возвращает список всех существующих страниц (с кодом 200)
    public Set<String> getExistingPages() {
        return new HashSet<>(existingPages);
    }

    // Новый метод: возвращает статистику ОС в виде долей (от 0 до 1)
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
        if (hoursBetween == 0) {
            return totalTraffic;
        }

        return (double) totalTraffic / hoursBetween;
    }

    public int getTotalTraffic() { return totalTraffic; }
    public Map<String, Integer> getOsUsage() { return new HashMap<>(osUsage); }
    public Map<String, Integer> getBrowserUsage() { return new HashMap<>(browserUsage); }
    public Map<HttpMethod, Integer> getMethodCount() { return new HashMap<>(methodCount); }
    public Map<Integer, Integer> getResponseCodeCount() { return new HashMap<>(responseCodeCount); }
}