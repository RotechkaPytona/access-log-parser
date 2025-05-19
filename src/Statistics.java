import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class Statistics {
    private int totalTraffic;
    private LocalDateTime minTime;
    private LocalDateTime maxTime;
    private final Map<String, Integer> osUsage = new HashMap<>();
    private final Map<String, Integer> browserUsage = new HashMap<>();
    private final Map<HttpMethod, Integer> methodCount = new HashMap<>();
    private final Map<Integer, Integer> responseCodeCount = new HashMap<>();

    public Statistics() {
        this.totalTraffic = 0;
        this.minTime = null;
        this.maxTime = null;
    }

    public void addEntry(LogEntry entry) {
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

    // Методы для получения статистики
    public int getTotalTraffic() { return totalTraffic; }
    public Map<String, Integer> getOsUsage() { return new HashMap<>(osUsage); }
    public Map<String, Integer> getBrowserUsage() { return new HashMap<>(browserUsage); }
    public Map<HttpMethod, Integer> getMethodCount() { return new HashMap<>(methodCount); }
    public Map<Integer, Integer> getResponseCodeCount() { return new HashMap<>(responseCodeCount); }
}