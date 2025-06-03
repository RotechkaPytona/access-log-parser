import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final Set<String> uniqueUserIps = new HashSet<>();
    private final Set<String> refererDomains = new HashSet<>();
    private final Map<Integer, Integer> visitsPerSecond = new HashMap<>();
    private final Map<String, Integer> visitsPerUser = new HashMap<>();
    private int botRequestsCount = 0;
    private int errorRequestsCount = 0;
    private int userRequestsCount = 0;
    private static final Pattern DOMAIN_PATTERN = Pattern.compile("^(https?://)?([^/]+)");

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

        // Обработка referer для списка сайтов-источников
        if (entry.getReferer() != null && !entry.getReferer().isEmpty()) {
            String domain = extractDomain(entry.getReferer());
            if (domain != null) {
                refererDomains.add(domain);
            }
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

            // Подсчет посещений по пользователям (IP)
            visitsPerUser.merge(entry.getIpAddress(), 1, Integer::sum);

            // Подсчет посещений по секундам
            int second = entryTime.getSecond();
            visitsPerSecond.merge(second, 1, Integer::sum);
        }

        // Остальная статистика (ОС, браузеры, методы, коды ответа)
        String os = entry.getUserAgent().getOsType();
        osUsage.put(os, osUsage.getOrDefault(os, 0) + 1);

        String browser = entry.getUserAgent().getBrowser();
        browserUsage.put(browser, browserUsage.getOrDefault(browser, 0) + 1);

        HttpMethod method = entry.getMethod();
        methodCount.put(method, methodCount.getOrDefault(method, 0) + 1);

        int code = entry.getResponseCode();
        responseCodeCount.put(code, responseCodeCount.getOrDefault(code, 0) + 1);
    }

    // Метод расчёта пиковой посещаемости сайта (в секунду)
    public int getPeakVisitsPerSecond() {
        return visitsPerSecond.values().stream()
                .max(Integer::compare)
                .orElse(0);
    }

    // Метод возвращающий список сайтов-источников
    public Set<String> getRefererDomains() {
        return new HashSet<>(refererDomains);
    }

    // Метод расчёта максимальной посещаемости одним пользователем
    public int getMaxVisitsByUser() {
        return visitsPerUser.values().stream()
                .max(Integer::compare)
                .orElse(0);
    }

    // Вспомогательный метод для извлечения домена из URL
    private String extractDomain(String url) {
        try {
            Matcher matcher = DOMAIN_PATTERN.matcher(url);
            if (matcher.find()) {
                String domain = matcher.group(2);
                // Удаляем порт если есть
                return domain.split(":")[0];
            }
        } catch (Exception e) {
            System.err.println("Error parsing domain from: " + url);
        }
        return null;
    }

    // Остальные существующие методы остаются без изменений
    public double getAverageVisitsPerHour() {
        if (minTime == null || maxTime == null || userRequestsCount == 0) {
            return 0.0;
        }
        long hoursBetween = ChronoUnit.HOURS.between(minTime, maxTime);
        return hoursBetween == 0 ? userRequestsCount : (double) userRequestsCount / hoursBetween;
    }

    public double getAverageErrorRequestsPerHour() {
        if (minTime == null || maxTime == null || errorRequestsCount == 0) {
            return 0.0;
        }
        long hoursBetween = ChronoUnit.HOURS.between(minTime, maxTime);
        return hoursBetween == 0 ? errorRequestsCount : (double) errorRequestsCount / hoursBetween;
    }

    public double getAverageVisitsPerUser() {
        if (uniqueUserIps.isEmpty()) {
            return 0.0;
        }
        return (double) userRequestsCount / uniqueUserIps.size();
    }

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