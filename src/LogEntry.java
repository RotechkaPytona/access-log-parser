import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogEntry {
    private final String ipAddress;
    private final LocalDateTime time;
    private final HttpMethod method;
    private final String path;
    private final int responseCode;
    private final int dataSize;
    private final String referer;
    private final UserAgent userAgent;

    // Регулярное выражение для разбора строки лога
    private static final String LOG_ENTRY_PATTERN =
            "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+) \"([^\"]*)\" \"([^\"]*)\"";
    private static final Pattern PATTERN = Pattern.compile(LOG_ENTRY_PATTERN);

    public LogEntry(String logLine) throws IllegalArgumentException {
        Matcher matcher = PATTERN.matcher(logLine);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid log line format: " + logLine);
        }

        this.ipAddress = matcher.group(1);
        this.time = parseDateTime(matcher.group(4));

        String[] requestParts = matcher.group(5).split(" ");
        this.method = parseHttpMethod(requestParts.length > 0 ? requestParts[0] : "");
        this.path = requestParts.length > 1 ? requestParts[1] : "";

        this.responseCode = Integer.parseInt(matcher.group(6));
        this.dataSize = Integer.parseInt(matcher.group(7));
        this.referer = matcher.group(8);
        this.userAgent = new UserAgent(matcher.group(9));
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
        try {
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateTimeStr, e);
        }
    }

    private HttpMethod parseHttpMethod(String method) {
        try {
            return HttpMethod.valueOf(method);
        } catch (IllegalArgumentException e) {
            return HttpMethod.UNKNOWN;
        }
    }

    // Геттеры
    public String getIpAddress() { return ipAddress; }
    public LocalDateTime getTime() { return time; }
    public HttpMethod getMethod() { return method; }
    public String getPath() { return path; }
    public int getResponseCode() { return responseCode; }
    public int getDataSize() { return dataSize; }
    public String getReferer() { return referer; }
    public UserAgent getUserAgent() { return userAgent; }
}