import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Введите путь к файлу лога (или 'exit' для выхода):");
            String path = scanner.nextLine();

            if ("exit".equalsIgnoreCase(path)) {
                break;
            }

            File file = new File(path);
            if (!file.exists() || !file.isFile()) {
                System.out.println("Файл не существует или это не файл. Попробуйте снова.");
                continue;
            }

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

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineCount++;

                try {
                    LogEntry entry = new LogEntry(line);
                    stats.addEntry(entry);
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
        System.out.println("Общий трафик: " + stats.getTotalTraffic() + " байт");
        System.out.printf("Средний трафик в час: %.2f байт/час%n", stats.getTrafficRate());

        // Новая статистика
        System.out.println("\nПиковая посещаемость (за секунду): " + stats.getPeakVisitsPerSecond());
        System.out.println("Максимальная посещаемость одним пользователем: " + stats.getMaxVisitsByUser());
        System.out.println("Сайты-источники трафика: " + stats.getRefererDomains());

        System.out.printf("\nСреднее количество посещений в час: %.2f%n", stats.getAverageVisitsPerHour());
        System.out.printf("Среднее количество ошибочных запросов в час: %.2f%n", stats.getAverageErrorRequestsPerHour());
        System.out.printf("Средняя посещаемость одним пользователем: %.2f%n", stats.getAverageVisitsPerUser());

        System.out.println("\nРаспределение по ОС:");
        int finalLineCount = lineCount;
        stats.getOsUsage().forEach((os, count) ->
                System.out.printf("  %-10s: %d (%.1f%%)%n", os, count, 100.0 * count / finalLineCount));

        System.out.println("\nРаспределение по браузерам:");
        int finalLineCount1 = lineCount;
        stats.getBrowserUsage().forEach((browser, count) ->
                System.out.printf("  %-10s: %d (%.1f%%)%n", browser, count, 100.0 * count / finalLineCount1));

        System.out.println("\nРаспределение по HTTP методам:");
        int finalLineCount2 = lineCount;
        stats.getMethodCount().forEach((method, count) ->
                System.out.printf("  %-10s: %d (%.1f%%)%n", method, count, 100.0 * count / finalLineCount2));

        System.out.println("\nРаспределение по кодам ответа:");
        int finalLineCount3 = lineCount;
        stats.getResponseCodeCount().forEach((code, count) ->
                System.out.printf("  %-10d: %d (%.1f%%)%n", code, count, 100.0 * count / finalLineCount3));

        System.out.println("\nНесуществующие страницы: " + stats.getNotFoundPages());
        System.out.println("Существующие страницы: " + stats.getExistingPages());
        System.out.println("Статистика браузеров: " + stats.getBrowserStatistics());
    }
}