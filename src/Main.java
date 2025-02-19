
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        //введение чисел с консоли

        System.out.println("Введите первое число: ");
        int firstNumber = new Scanner(System.in).nextInt();

        System.out.println("Введите второе число: ");
        int secondNumber = new Scanner(System.in).nextInt();

        //объявление переменных для результата операций
        int sum = firstNumber + secondNumber;
        int diff = firstNumber - secondNumber;
        int mult = firstNumber * secondNumber;
        double quotient = (double) firstNumber/secondNumber;

        //вывод результатов операций
        System.out.println("Сумма: " + sum);
        System.out.println("Разность: " + diff);
        System.out.println("Произведение: " + mult);
        System.out.println("Частное: " + quotient);

    }
}