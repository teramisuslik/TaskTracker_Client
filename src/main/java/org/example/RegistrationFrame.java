package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class RegistrationFrame extends JFrame {

    private JPanel contentPane;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JLabel statusLabel;

    // URL вашего сервера для регистрации
    private static final String REGISTER_URL = "http://localhost:8080/register";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public RegistrationFrame() {
        setTitle("Регистрация");
        setSize(500, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setBackground(new Color(245, 245, 245));
        contentPane.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        // Заголовок
        JLabel titleLabel = new JLabel("Регистрация");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Поле для логина
        JPanel usernamePanel = createInputPanel("Логин:");
        usernameField = new JTextField(15);
        styleTextField(usernameField);
        usernamePanel.add(usernameField);

        // Поле для пароля
        JPanel passwordPanel = createInputPanel("Пароль:");
        passwordField = new JPasswordField(15);
        styleTextField(passwordField);
        passwordPanel.add(passwordField);

        // Поле для подтверждения пароля
        JPanel confirmPasswordPanel = createInputPanel("Повторите пароль:");
        confirmPasswordField = new JPasswordField(15);
        styleTextField(confirmPasswordField);
        confirmPasswordPanel.add(confirmPasswordField);

        // Кнопка регистрации
        registerButton = new JButton("Зарегистрироваться");
        styleRegisterButton(registerButton);
        registerButton.addActionListener(new RegisterButtonListener());

        // Статусная строка
        statusLabel = new JLabel("Заполните все поля");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(127, 140, 141));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Добавление компонентов
        contentPane.add(Box.createVerticalStrut(20));
        contentPane.add(titleLabel);
        contentPane.add(Box.createVerticalStrut(20));
        contentPane.add(usernamePanel);
        contentPane.add(Box.createVerticalStrut(20));
        contentPane.add(passwordPanel);
        contentPane.add(Box.createVerticalStrut(20));
        contentPane.add(confirmPasswordPanel);
        contentPane.add(Box.createVerticalStrut(30));
        contentPane.add(registerButton);
        contentPane.add(Box.createVerticalStrut(10));
        contentPane.add(statusLabel);

        add(contentPane);
    }

    private JPanel createInputPanel(String labelText) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panel.setBackground(new Color(245, 245, 245));
        panel.setMaximumSize(new Dimension(400, 50));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(44, 62, 80));
        label.setPreferredSize(new Dimension(150, 30));

        panel.add(label);
        return panel;
    }

    private void styleTextField(JTextField textField) {
        textField.setPreferredSize(new Dimension(200, 40));
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        textField.setBackground(Color.WHITE);
    }

    private void styleRegisterButton(JButton button) {
        button.setPreferredSize(new Dimension(220, 45));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(46, 204, 113));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(39, 174, 96));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(46, 204, 113));
            }
        });
    }

    // Обработчик кнопки регистрации с HTTP запросом
    private class RegisterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

            // Валидация на клиенте
            if (!validateInput(username, password, confirmPassword)) {
                return;
            }

            // Отключаем кнопку на время запроса
            registerButton.setEnabled(false);
            showStatus("Отправка запроса на сервер...", Color.ORANGE);

            // Запускаем HTTP запрос в отдельном потоке
            new Thread(() -> {
                try {
                    // Отправляем запрос на сервер
                    String response = sendRegistrationRequest(username, password);

                    // Обрабатываем ответ в EDT
                    SwingUtilities.invokeLater(() -> {
                        handleServerResponse(response, username);
                    });

                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        showStatus("Ошибка соединения: " + ex.getMessage(), Color.RED);
                        registerButton.setEnabled(true);
                    });
                }
            }).start();
        }

        // Валидация данных на клиенте
        private boolean validateInput(String username, String password, String confirmPassword) {
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showStatus("Заполните все поля!", Color.RED);
                return false;
            }

            if (username.length() < 3) {
                showStatus("Логин должен быть не менее 3 символов!", Color.RED);
                return false;
            }

            if (password.length() < 6) {
                showStatus("Пароль должен быть не менее 6 символов!", Color.RED);
                return false;
            }

            if (!password.equals(confirmPassword)) {
                showStatus("Пароли не совпадают!", Color.RED);
                return false;
            }

            return true;
        }

        // Отправка HTTP запроса на сервер для регистрации
        private String sendRegistrationRequest(String username, String password)
                throws IOException, InterruptedException {

            String jsonBody = String.format(
                    "{\"username\": \"%s\", \"password\": \"%s\"}",
                    username, password
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(REGISTER_URL))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        }

        // Обработка ответа от сервера
        private void handleServerResponse(String response, String username) {
            registerButton.setEnabled(true);

            try {
                if (response.contains("\"message\":\"User registered successfully\"") ||
                        response.contains("success") ||
                        response.contains("200")) {

                    showStatus("Регистрация успешна!", new Color(46, 204, 113));

                    // Используем красивое кастомное окно вместо JOptionPane
                    showSuccessDialog(username);

                    // Закрываем окно регистрации
                    dispose();

                } else if (response.contains("username") && response.contains("already")) {
                    showStatus("Логин уже занят", Color.RED);
                    showErrorDialog("Пользователь с таким логином уже существует!", "Ошибка регистрации");

                } else if (response.contains("error") || response.contains("400") || response.contains("409")) {
                    showStatus("Ошибка регистрации", Color.RED);
                    showErrorDialog("Ошибка при регистрации: " + response, "Ошибка сервера");
                } else {
                    showStatus("Регистрация завершена", new Color(46, 204, 113));
                    showSuccessDialog(username);
                    dispose();
                }
            } catch (Exception ex) {
                showStatus("Ошибка обработки ответа", Color.RED);
                showErrorDialog("Ошибка: " + ex.getMessage(), "Ошибка");
            }
        }
    }

    // Красивое окно успешной регистрации
    private void showSuccessDialog(String username) {
        // Создаем кастомное диалоговое окно
        JDialog successDialog = new JDialog(this, "Успешная регистрация", true);
        successDialog.setSize(400, 300);
        successDialog.setLocationRelativeTo(this);
        successDialog.setResizable(false);
        successDialog.setLayout(new BorderLayout());

        // Панель содержимого
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(255, 255, 255));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Иконка успеха (зеленая галочка)
        JLabel iconLabel = new JLabel("✓", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Arial", Font.BOLD, 64));
        iconLabel.setForeground(new Color(46, 204, 113));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Заголовок
        JLabel titleLabel = new JLabel("Регистрация завершена!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Сообщение
        JLabel messageLabel = new JLabel("<html><center>Пользователь <b>" + username + "</b><br>успешно зарегистрирован!</center></html>", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(127, 140, 141));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Дополнительное сообщение
        JLabel infoLabel = new JLabel("Теперь вы можете войти в систему", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(169, 181, 183));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Кнопка OK
        JButton okButton = new JButton("Отлично!");
        okButton.setFont(new Font("Arial", Font.BOLD, 14));
        okButton.setForeground(Color.WHITE);
        okButton.setBackground(new Color(46, 204, 113));
        okButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        okButton.setFocusPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        okButton.addActionListener(e -> successDialog.dispose());

        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                okButton.setBackground(new Color(39, 174, 96));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                okButton.setBackground(new Color(46, 204, 113));
            }
        });

        // Добавляем компоненты
        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(infoLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(okButton);

        successDialog.add(contentPanel, BorderLayout.CENTER);
        successDialog.getRootPane().setDefaultButton(okButton);

        // Центрируем и показываем
        successDialog.pack();
        successDialog.setLocationRelativeTo(this);
        successDialog.setVisible(true);
    }

    // Красивое окно ошибки
    private void showErrorDialog(String message, String title) {
        JDialog errorDialog = new JDialog(this, title, true);
        errorDialog.setSize(400, 250);
        errorDialog.setLocationRelativeTo(this);
        errorDialog.setResizable(false);
        errorDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(255, 255, 255));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Иконка ошибки (красный крестик)
        JLabel iconLabel = new JLabel("✗", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Arial", Font.BOLD, 48));
        iconLabel.setForeground(new Color(220, 53, 69));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(220, 53, 69));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = new JLabel("<html><center>" + message + "</center></html>", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(127, 140, 141));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton okButton = new JButton("Понятно");
        okButton.setFont(new Font("Arial", Font.BOLD, 14));
        okButton.setForeground(Color.WHITE);
        okButton.setBackground(new Color(220, 53, 69));
        okButton.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
        okButton.setFocusPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        okButton.addActionListener(e -> errorDialog.dispose());

        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                okButton.setBackground(new Color(200, 35, 51));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                okButton.setBackground(new Color(220, 53, 69));
            }
        });

        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(okButton);

        errorDialog.add(contentPanel, BorderLayout.CENTER);
        errorDialog.getRootPane().setDefaultButton(okButton);
        errorDialog.pack();
        errorDialog.setLocationRelativeTo(this);
        errorDialog.setVisible(true);
    }

    // Показать статус с цветом
    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }
}
