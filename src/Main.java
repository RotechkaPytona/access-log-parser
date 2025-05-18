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
        int maxLength = 0;
        int minLength = Integer.MAX_VALUE;
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                lineCount++;
                int length = line.length();

                // Проверяем длину строки
                if (length > 1024) {
                    throw new LineTooLongException("Строка #" + lineCount + " превышает максимально допустимую длину 1024 символа");
                }

                // Обновляем максимальную длину
                if (length > maxLength) {
                    maxLength = length;
                }

                // Обновляем минимальную длину
                if (length < minLength) {
                    minLength = length;
                }
            }

            // Выводим результаты
            System.out.println("Общее количество строк в файле: " + lineCount);
            System.out.println("Длина самой длинной строки: " + maxLength);
            System.out.println("Длина самой короткой строки: " + (minLength == Integer.MAX_VALUE ? 0 : minLength));

        } finally {
            // Закрываем ресурсы в блоке finally
            reader.close();
            fileReader.close();
        }
    }
}

class LineTooLongException extends RuntimeException {
    public LineTooLongException(String message) {
        super(message);
    }
}