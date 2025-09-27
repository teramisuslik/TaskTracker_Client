package org.example;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// Главное окно приложения с использованием JWT токена
class MainApplicationFrame extends JFrame {
    private String authToken;

    public MainApplicationFrame(String token) {
        this.authToken = token;

        setTitle("Главное приложение - Авторизован");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Основная панель с BorderLayout
        JPanel contentPane = new JPanel(new BorderLayout());

        // Панель для верхней части (кнопка выхода справа)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 245, 245));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Панель для кнопок функций (слева)
        JPanel functionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        functionPanel.setOpaque(false);

        // Кнопка для тестирования запроса с токеном
        JButton testRequestButton = new JButton("Тестовый запрос с JWT");
        testRequestButton.addActionListener(e -> {
            if (authToken != null) {
                String result = makeAuthenticatedRequest("http://localhost:8080/api/user/profile");
                JOptionPane.showMessageDialog(this, "Результат запроса:\n" + result);
            } else {
                JOptionPane.showMessageDialog(this, "Токен отсутствует!");
            }
        });
        functionPanel.add(testRequestButton);

        // Кнопка выхода (справа) - ярко-красная
        JButton logoutButton = createRedLogoutButton();

        // Добавляем кнопки на верхнюю панель
        topPanel.add(functionPanel, BorderLayout.WEST);
        topPanel.add(logoutButton, BorderLayout.EAST);

        // Центральная часть - приветствие
        JLabel welcomeLabel = new JLabel("Добро пожаловать в приложение!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));

        // Нижняя часть - информация о токене
        JLabel tokenLabel = new JLabel("JWT Токен: " + (authToken != null ?
                authToken.substring(0, Math.min(30, authToken.length())) + "..." : "отсутствует"),
                SwingConstants.CENTER);
        tokenLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        tokenLabel.setForeground(Color.GRAY);

        // Добавляем все компоненты на основную панель
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(welcomeLabel, BorderLayout.CENTER);
        contentPane.add(tokenLabel, BorderLayout.SOUTH);

        add(contentPane);
    }

    // Создание ярко-красной кнопки выхода
    private JButton createRedLogoutButton() {
        JButton logoutButton = new JButton("Выйти");

        // Стилизация кнопки
        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(new Color(220, 53, 69)); // Ярко-красный цвет
        logoutButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 35, 51), 2),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Эффекты при наведении
        logoutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(new Color(200, 35, 51)); // Темнее красный
                logoutButton.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(180, 25, 41), 2),
                        BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(new Color(220, 53, 69)); // Обычный красный
                logoutButton.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 35, 51), 2),
                        BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }

            public void mousePressed(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(new Color(180, 25, 41)); // Еще темнее при нажатии
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(new Color(200, 35, 51)); // Возврат при отпускании
            }
        });

        // Обработчик нажатия
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Вы уверены, что хотите выйти?",
                    "Подтверждение выхода",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                LoginFrame.clearToken();
                this.dispose();
                new LoginFrame().setVisible(true);
            }
        });

        return logoutButton;
    }

    // Метод для выполнения запросов с JWT токеном
    private String makeAuthenticatedRequest(String url) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return "Статус: " + response.statusCode() + "\nОтвет: " + response.body();

        } catch (Exception e) {
            return "Ошибка: " + e.getMessage();
        }
    }
}