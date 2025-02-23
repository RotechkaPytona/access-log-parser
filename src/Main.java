import java.io.File;
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
        }
    }
}