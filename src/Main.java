import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int fileCount = 0; // Счетчик верно указанных файлов

        while (true) {
            System.out.println("Введите путь к файлу:");
            String path = scanner.nextLine();

            File file = new File(path);

            // Проверяем существует ли файл
            boolean fileExists = file.exists();

            // Проверяем является ли путь файлом
            boolean isFile = file.isFile();

            // Проверяем условия
            if (!fileExists) {
                System.out.println("Файл не существует. Попробуйте снова.");
                continue;
            }

            if (!isFile) {
                System.out.println("Путь ведет к папке, а не к файлу. Попробуйте снова.");
                continue;
            }

            // Если путь указан верно
            System.out.println("Путь указан верно.");
            fileCount++;
            System.out.println("Это файл номер " + fileCount);

            // Добавляем код для чтения файла и анализа строк
            try {
                analyzeFile(path);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void analyzeFile(String path) throws IOException {
        FileReader fileReader = new FileReader(path);
        BufferedReader reader = new BufferedReader(fileReader);

        int lineCount = 0;
        int googlebotCount = 0;
        int yandexBotCount = 0;
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                lineCount++;

                // Проверяем длину строки
                if (line.length() > 1024) {
                    throw new LineTooLongException("Строка #" + lineCount + " превышает максимально допустимую длину 1024 символа");
                }

                // Анализируем User-Agent
                processUserAgent(line, lineCount);

                // Подсчет ботов
                if (isGooglebot(line)) {
                    googlebotCount++;
                } else if (isYandexBot(line)) {
                    yandexBotCount++;
                }
            }

            // Выводим результаты
            System.out.println("Общее количество строк в файле: " + lineCount);
            System.out.println("Запросов от Googlebot: " + googlebotCount +
                    " (" + calculatePercentage(googlebotCount, lineCount) + "%)");
            System.out.println("Запросов от YandexBot: " + yandexBotCount +
                    " (" + calculatePercentage(yandexBotCount, lineCount) + "%)");

        } finally {
            // Закрываем ресурсы в блоке finally
            reader.close();
            fileReader.close();
        }
    }

    private static boolean isGooglebot(String line) {
        return extractUserAgentFragment(line).equals("Googlebot");
    }

    private static boolean isYandexBot(String line) {
        return extractUserAgentFragment(line).equals("YandexBot");
    }

    private static String extractUserAgentFragment(String line) {
        // Находим часть строки с User-Agent
        int uaStart = line.indexOf("\" \"") + 3;
        int uaEnd = line.lastIndexOf("\"");
        if (uaStart < 3 || uaEnd < 0) return "";

        String userAgent = line.substring(uaStart, uaEnd);

        // Выделяем часть в первых скобках
        int bracketsStart = userAgent.indexOf('(');
        int bracketsEnd = userAgent.indexOf(')');
        if (bracketsStart < 0 || bracketsEnd < 0) return "";

        String firstBrackets = userAgent.substring(bracketsStart + 1, bracketsEnd);

        // Разделяем по точке с запятой
        String[] parts = firstBrackets.split(";");
        if (parts.length < 2) return "";

        // Берем второй фрагмент и очищаем от пробелов
        String fragment = parts[1].trim();

        // Отделяем часть до слэша
        int slashIndex = fragment.indexOf('/');
        return slashIndex > 0 ? fragment.substring(0, slashIndex) : fragment;
    }

    private static void processUserAgent(String line, int lineNumber) {
        String fragment = extractUserAgentFragment(line);
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