import java.io.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int fileCount = 0;

        while (true) {
            System.out.println("Введите путь к файлу:");
            String path = scanner.nextLine();

            if ("exit".equalsIgnoreCase(path)) {
                break;
            }

            File file = new File(path);
            if (!file.exists()) {
                System.out.println("Файл не существует. Попробуйте снова.");
                continue;
            }

            if (!file.isFile()) {
                System.out.println("Путь ведет к папке, а не к файлу. Попробуйте снова.");
                continue;
            }

            System.out.println("Путь указан верно.");
            fileCount++;
            System.out.println("Это файл номер " + fileCount);

            try {
                processLogFile(file);
            } catch (IOException e) {
                System.err.println("Ошибка при чтении файла: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Ошибка: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static void processLogFile(File file) throws IOException {
        Statistics stats = new Statistics();
        int lineCount = 0;
        int errorCount = 0;
        int googlebotCount = 0;
        int yandexBotCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineCount++;

                try {
                    if (line.length() > 1024) {
                        throw new LineTooLongException("Строка #" + lineCount + " превышает максимальную длину 1024 символа");
                    }

                    LogEntry entry = new LogEntry(line);
                    stats.addEntry(entry);

                    // Подсчет ботов
                    if (isGooglebot(line)) {
                        googlebotCount++;
                    } else if (isYandexBot(line)) {
                        yandexBotCount++;
                    }
                } catch (IllegalArgumentException e) {
                    errorCount++;
                    System.err.println("Ошибка в строке " + lineCount + ": " + e.getMessage());
                }
            }
        }

        // Вывод результатов
        System.out.println("\nРезультаты анализа:");
        System.out.println("Всего строк: " + lineCount);
        System.out.println("Ошибок парсинга: " + errorCount);
        System.out.println("Запросов от Googlebot: " + googlebotCount +
                " (" + calculatePercentage(googlebotCount, lineCount) + "%)");
        System.out.println("Запросов от YandexBot: " + yandexBotCount +
                " (" + calculatePercentage(yandexBotCount, lineCount) + "%)");
        System.out.println("Общий трафик: " + stats.getTotalTraffic() + " байт");
        System.out.printf("Средний трафик в час: %.2f байт/час%n", stats.getTrafficRate());

        printDistribution("ОС", stats.getOsUsage(), lineCount);
        printDistribution("браузерам", stats.getBrowserUsage(), lineCount);
        printDistribution("HTTP методам", stats.getMethodCount(), lineCount);
        printDistribution("кодам ответа", stats.getResponseCodeCount(), lineCount);
    }

    private static void printDistribution(String title, Map<?, Integer> data, int total) {
        System.out.println("\nРаспределение по " + title + ":");
        data.forEach((key, count) ->
                System.out.printf("  %-10s: %d (%.1f%%)%n", key, count, 100.0 * count / total));
    }

    private static boolean isGooglebot(String line) {
        return extractUserAgentFragment(line).equals("Googlebot");
    }

    private static boolean isYandexBot(String line) {
        return extractUserAgentFragment(line).equals("YandexBot");
    }

    private static String extractUserAgentFragment(String line) {
        int uaStart = line.indexOf("\" \"") + 3;
        int uaEnd = line.lastIndexOf("\"");
        if (uaStart < 3 || uaEnd < 0) return "";

        String userAgent = line.substring(uaStart, uaEnd);
        int bracketsStart = userAgent.indexOf('(');
        int bracketsEnd = userAgent.indexOf(')');
        if (bracketsStart < 0 || bracketsEnd < 0) return "";

        String firstBrackets = userAgent.substring(bracketsStart + 1, bracketsEnd);
        String[] parts = firstBrackets.split(";");
        if (parts.length < 2) return "";

        String fragment = parts[1].trim();
        int slashIndex = fragment.indexOf('/');
        return slashIndex > 0 ? fragment.substring(0, slashIndex) : fragment;
    }

    private static double calculatePercentage(int count, int total) {
        return total == 0 ? 0 : (count * 100.0 / total);
    }
}

class LineTooLongException extends RuntimeException {
    public LineTooLongException(String message) {
        super(message);
    }
}