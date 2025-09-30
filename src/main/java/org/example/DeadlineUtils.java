package org.example;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.awt.Color;

public class DeadlineUtils {

    public static Color getDeadlineColor(String deadline) {
        if (deadline == null || deadline.isEmpty() || deadline.equals("нет дедлайна")) {
            return Color.GRAY; // Серый для отсутствующего дедлайна
        }

        try {
            // Парсим дату дедлайна
            LocalDate deadlineDate;
            if (deadline.contains("T")) {
                deadlineDate = LocalDate.parse(deadline.substring(0, deadline.indexOf("T")));
            } else {
                deadlineDate = LocalDate.parse(deadline);
            }

            LocalDate today = LocalDate.now();

            // Сравниваем даты
            if (deadlineDate.isBefore(today)) {
                return new Color(231, 76, 60); // Красный - дедлайн прошел
            } else {
                long daysBetween = ChronoUnit.DAYS.between(today, deadlineDate);

                if (daysBetween <= 3) {
                    return new Color(241, 196, 15); // Желтый - меньше 3 дней
                } else {
                    return new Color(46, 204, 113); // Зеленый - больше 3 дней
                }
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Error parsing deadline: " + deadline + " - " + e.getMessage());
            return Color.GRAY; // Серый при ошибке парсинга
        }
    }

    public static String formatDeadlineForDisplay(String deadline) {
        if (deadline == null || deadline.isEmpty()) {
            return "нет дедлайна";
        }

        try {
            if (deadline.contains("T")) {
                return deadline.substring(0, deadline.indexOf("T"));
            }
            return deadline;
        } catch (Exception e) {
            return deadline;
        }
    }

    public static String getDeadlineStatusText(String deadline) {
        if (deadline == null || deadline.isEmpty()) {
            return "Нет дедлайна";
        }

        try {
            LocalDate deadlineDate;
            if (deadline.contains("T")) {
                deadlineDate = LocalDate.parse(deadline.substring(0, deadline.indexOf("T")));
            } else {
                deadlineDate = LocalDate.parse(deadline);
            }

            LocalDate today = LocalDate.now();

            if (deadlineDate.isBefore(today)) {
                long daysOverdue = ChronoUnit.DAYS.between(deadlineDate, today);
                return "Просрочено на " + daysOverdue + " д.";
            } else {
                long daysLeft = ChronoUnit.DAYS.between(today, deadlineDate);
                return "Осталось " + daysLeft + " д.";
            }
        } catch (Exception e) {
            return "Некорректная дата";
        }
    }
}
