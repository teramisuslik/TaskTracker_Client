package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;


public class LoginFrame extends JFrame {

    private JPanel contentPane;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JLabel statusLabel;

    // URL вашего сервера
    private static final String LOGIN_URL = "http://localhost:8080/login";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // Для сохранения токена
    private static final Preferences prefs = Preferences.userNodeForPackage(LoginFrame.class);
    private static String jwtToken = null;

    public LoginFrame(){
        setTitle("Авторизация");
        setSize(500, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Настройка основного контента
        contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setBackground(new Color(245, 245, 245));
        contentPane.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        // Заголовок
        titleLabel = new JLabel("Вход в систему");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        // Подзаголовок
        subtitleLabel = new JLabel("Добро пожаловать!");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setHorizontalAlignment(JLabel.CENTER);

        // Панель для логина
        JPanel usernamePanel = createHorizontalInputPanel("Логин:");
        usernameField = new JTextField(15);
        styleTextField(usernameField);
        usernamePanel.add(usernameField);

        // Панель для пароля
        JPanel passwordPanel = createHorizontalInputPanel("Пароль:");
        passwordField = new JPasswordField(15);
        styleTextField(passwordField);
        passwordPanel.add(passwordField);

        // Кнопка входа
        loginButton = new JButton("Войти");
        styleLoginButton(loginButton);
        loginButton.addActionListener(new LoginButtonListener());

        // Статусная строка
        statusLabel = new JLabel("Готов к работе");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(127, 140, 141));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Кликабельная надпись "Регистрация"
        JLabel registrationLabel = createClickableRegistrationLabel();

        JPanel registrationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        registrationPanel.setBackground(new Color(245, 245, 245));
        registrationPanel.add(registrationLabel);

        // Добавление компонентов на форму
        contentPane.add(Box.createVerticalStrut(20));
        contentPane.add(titleLabel);
        contentPane.add(Box.createVerticalStrut(5));
        contentPane.add(subtitleLabel);
        contentPane.add(Box.createVerticalStrut(40));
        contentPane.add(usernamePanel);
        contentPane.add(Box.createVerticalStrut(20));
        contentPane.add(passwordPanel);
        contentPane.add(Box.createVerticalStrut(30));
        contentPane.add(loginButton);
        contentPane.add(Box.createVerticalStrut(10));
        contentPane.add(statusLabel);
        contentPane.add(Box.createVerticalStrut(15));
        contentPane.add(registrationPanel);

        add(contentPane);
    }

    // Проверка сохраненного токена - ТЕПЕРЬ В ОТДЕЛЬНОМ МЕТОДЕ
    public void checkAuthentication() {
        String savedToken = prefs.get("jwt_token", null);
        if (savedToken != null && !savedToken.isEmpty()) {
            jwtToken = savedToken;
            // Если есть сохраненный токен, сразу открываем главное окно
            // и НЕ показываем окно входа
            openMainApplicationFrame(jwtToken);
        } else {
            // Если токена нет, показываем окно входа
            setVisible(true);
        }
    }

    // Остальные методы без изменений...
    private JLabel createClickableRegistrationLabel() {
        JLabel registrationLabel = new JLabel("<html><u>Регистрация</u></html>");
        registrationLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        registrationLabel.setForeground(new Color(52, 152, 219));
        registrationLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        registrationLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openRegistrationFrame();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                registrationLabel.setForeground(new Color(41, 128, 185));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                registrationLabel.setForeground(new Color(52, 152, 219));
            }
        });

        return registrationLabel;
    }

    private void openRegistrationFrame() {
        RegistrationFrame registrationFrame = new RegistrationFrame();
        registrationFrame.setVisible(true);
    }

    private JPanel createHorizontalInputPanel(String labelText) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panel.setBackground(new Color(245, 245, 245));
        panel.setMaximumSize(new Dimension(400, 50));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(44, 62, 80));
        label.setPreferredSize(new Dimension(80, 30));

        panel.add(label);
        return panel;
    }

    private void styleTextField(JTextField textField) {
        textField.setPreferredSize(new Dimension(250, 40));
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        textField.setBackground(Color.WHITE);
    }

    private void styleLoginButton(JButton button) {
        button.setPreferredSize(new Dimension(350, 45));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(52, 152, 219));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(41, 128, 185));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(52, 152, 219));
            }
        });
    }

    // Обработчик кнопки входа с HTTP запросом
    private class LoginButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                showStatus("Заполните все поля!", Color.RED);
                return;
            }

            loginButton.setEnabled(false);
            showStatus("Отправка запроса на сервер...", Color.ORANGE);

            new Thread(() -> {
                try {
                    String response = sendLoginRequest(username, password);
                    SwingUtilities.invokeLater(() -> {
                        handleServerResponse(response, username);
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        showStatus("Ошибка соединения: " + ex.getMessage(), Color.RED);
                        loginButton.setEnabled(true);
                    });
                }
            }).start();
        }

        private String sendLoginRequest(String username, String password) throws IOException, InterruptedException {
            String jsonBody = String.format(
                    "{\"username\": \"%s\", \"password\": \"%s\"}",
                    username, password
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(LOGIN_URL))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        }

        private void handleServerResponse(String response, String username) {
            loginButton.setEnabled(true);

            try {
                if (response.contains("\"token\"")) {
                    String token = extractTokenFromResponse(response);

                    if (token != null && !token.isEmpty()) {
                        saveToken(token);
                        showStatus("Успешная авторизация!", new Color(46, 204, 113));
                        openMainApplicationFrame(token);
                    } else {
                        showStatus("Токен не найден в ответе", Color.RED);
                        JOptionPane.showMessageDialog(LoginFrame.this,
                                "Ошибка: токен не получен от сервера",
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                } else if (response.contains("error") || response.contains("Unauthorized") ||
                        response.toLowerCase().contains("invalid")) {
                    showStatus("Неверный логин или пароль", Color.RED);
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "Неверные учетные данные!",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                } else {
                    showStatus("Неизвестный ответ от сервера", Color.RED);
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "Ошибка сервера: " + response,
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                showStatus("Ошибка обработки ответа", Color.RED);
                JOptionPane.showMessageDialog(LoginFrame.this,
                        "Ошибка: " + ex.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }

        private String extractTokenFromResponse(String response) {
            try {
                int tokenStart = response.indexOf("\"token\":\"") + 9;
                if (tokenStart < 9) {
                    tokenStart = response.indexOf("\"token\": \"") + 10;
                }

                if (tokenStart > 8) {
                    int tokenEnd = response.indexOf("\"", tokenStart);
                    if (tokenEnd > tokenStart) {
                        return response.substring(tokenStart, tokenEnd);
                    }
                }
            } catch (Exception e) {
                System.err.println("Ошибка извлечения токена: " + e.getMessage());
            }
            return null;
        }
    }

    private void saveToken(String token) {
        jwtToken = token;
        prefs.put("jwt_token", token);
        System.out.println("Токен сохранен: " + token.substring(0, Math.min(20, token.length())) + "...");
    }

    public static String getJwtToken() {
        if (jwtToken == null) {
            jwtToken = prefs.get("jwt_token", null);
        }
        return jwtToken;
    }

    public static void clearToken() {
        jwtToken = null;
        prefs.remove("jwt_token");
        System.out.println("Токен удален");
    }

    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    // В LoginFrame добавляем методы для плавных переходов
    private void openMainApplicationFrame(String token) {
        // Сначала создаем новое окно
        Map<String, Object> userInfo = getUserInfo(token);
        String role = (userInfo != null && userInfo.containsKey("role")) ?
                userInfo.get("role").toString() : "USER";

        JFrame newFrame;
        if ("ADMIN".equals(role.toUpperCase())) {
            newFrame = new AdminApplicationFrame(token, userInfo);
        } else {
            newFrame = new UserApplicationFrame(token, userInfo);
        }

        // Устанавливаем новое окно невидимым для анимации
        newFrame.setVisible(false);

        // Анимация исчезновения текущего окна
        Timer fadeOutTimer = new Timer(20, new ActionListener() {
            float opacity = 1f;
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity -= 0.05f; // Медленнее для плавности
                if (opacity <= 0f) {
                    opacity = 0f;
                    ((Timer) e.getSource()).stop();

                    // После завершения анимации скрытия
                    setVisible(false);
                    dispose();

                    // Показываем новое окно с анимацией
                    openNewFrameWithAnimation(newFrame);
                }
                // Устанавливаем прозрачность только если окно не декорировано
                try {
                    setOpacity(opacity);
                } catch (IllegalComponentStateException ex) {
                    // Если нельзя установить прозрачность, просто продолжаем
                    ((Timer) e.getSource()).stop();
                    setVisible(false);
                    dispose();
                    openNewFrameWithAnimation(newFrame);
                }
            }
        });
        fadeOutTimer.start();
    }

    private Map<String, Object> getUserInfo(String token) {
        try {
            // Сначала пробуем получить username из токена
            String username = getUsernameFromTokenDirect(token);
            if (username == null) {
                System.out.println("Не удалось извлечь username из токена");
                return null;
            }

            System.out.println("Username из токена: " + username);

            HttpClient client = HttpClient.newHttpClient();
            String url = "http://localhost:8080/userwithouttasks?username=" +
                    java.net.URLEncoder.encode(username, "UTF-8");

            System.out.println("Запрос к URL: " + url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Статус ответа: " + response.statusCode());
            System.out.println("Тело ответа: " + response.body());

            if (response.statusCode() == 200) {
                // Парсим JSON ответ
                return parseUserInfo(response.body());
            } else {
                System.out.println("Ошибка HTTP: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Ошибка получения информации о пользователе: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Прямое извлечение username из токена
    private String getUsernameFromTokenDirect(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                System.out.println("Payload токена: " + payload);

                // Ищем sub (subject) поле
                int subStart = payload.indexOf("\"sub\":\"");
                if (subStart != -1) {
                    subStart += 7;
                    int subEnd = payload.indexOf("\"", subStart);
                    if (subEnd > subStart) {
                        return payload.substring(subStart, subEnd);
                    }
                }

                // Ищем username поле
                int usernameStart = payload.indexOf("\"username\":\"");
                if (usernameStart != -1) {
                    usernameStart += 12;
                    int usernameEnd = payload.indexOf("\"", usernameStart);
                    if (usernameEnd > usernameStart) {
                        return payload.substring(usernameStart, usernameEnd);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка извлечения username из токена: " + e.getMessage());
        }
        return null;
    }

    private Map<String, Object> parseUserInfo(String jsonResponse) {
        try {
            Map<String, Object> userInfo = new HashMap<>();
            System.out.println("DEBUG: Parsing user info from: " + jsonResponse);

            // Простой и надежный способ извлечения username
            if (jsonResponse.contains("\"username\"")) {
                // Ищем начало username
                int usernameIndex = jsonResponse.indexOf("\"username\"");
                if (usernameIndex != -1) {
                    // Ищем двоеточие после username
                    int colonIndex = jsonResponse.indexOf(":", usernameIndex);
                    if (colonIndex != -1) {
                        // Ищем начало значения (может быть в кавычках или без)
                        int valueStart = colonIndex + 1;
                        // Пропускаем пробелы
                        while (valueStart < jsonResponse.length() && Character.isWhitespace(jsonResponse.charAt(valueStart))) {
                            valueStart++;
                        }

                        if (valueStart < jsonResponse.length()) {
                            if (jsonResponse.charAt(valueStart) == '"') {
                                // Значение в кавычках
                                valueStart++; // Пропускаем открывающую кавычку
                                int valueEnd = jsonResponse.indexOf("\"", valueStart);
                                if (valueEnd != -1) {
                                    String username = jsonResponse.substring(valueStart, valueEnd);
                                    userInfo.put("username", username);
                                    System.out.println("DEBUG: Extracted username: " + username);
                                }
                            } else {
                                // Значение без кавычек
                                int valueEnd = jsonResponse.indexOf(",", valueStart);
                                if (valueEnd == -1) valueEnd = jsonResponse.indexOf("}", valueStart);
                                if (valueEnd != -1) {
                                    String username = jsonResponse.substring(valueStart, valueEnd).trim();
                                    userInfo.put("username", username);
                                    System.out.println("DEBUG: Extracted username: " + username);
                                }
                            }
                        }
                    }
                }
            }

            // Извлекаем role
            String role = extractRoleFromJson(jsonResponse);
            userInfo.put("role", role);
            System.out.println("DEBUG: Extracted role: " + role);

            System.out.println("DEBUG: Final userInfo: " + userInfo);
            return userInfo;
        } catch (Exception e) {
            System.err.println("Ошибка парсинга информации о пользователе: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Новый метод для извлечения роли с учетом разных форматов
    private String extractRoleFromJson(String jsonResponse) {
        try {
            // Пробуем разные варианты названий полей
            String[] rolePatterns = {
                    "\"role\":\"([^\"]+)\"",      // "role":"ADMIN"
                    "\"role\": \"([^\"]+)\"",     // "role": "ADMIN"
                    "\"authority\":\"([^\"]+)\"", // "authority":"ROLE_ADMIN"
                    "\"authority\": \"([^\"]+)\"" // "authority": "ROLE_ADMIN"
            };

            for (String pattern : rolePatterns) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher m = p.matcher(jsonResponse);
                if (m.find()) {
                    String foundRole = m.group(1);
                    return normalizeRole(foundRole);
                }
            }

            // Если не нашли по шаблонам, пробуем простой поиск
            if (jsonResponse.contains("\"role\"")) {
                int roleStart = jsonResponse.indexOf("\"role\":\"") + 8;
                if (roleStart < 8) {
                    roleStart = jsonResponse.indexOf("\"role\": \"") + 9;
                }
                if (roleStart > 7) {
                    int roleEnd = jsonResponse.indexOf("\"", roleStart);
                    if (roleEnd > roleStart) {
                        String foundRole = jsonResponse.substring(roleStart, roleEnd);
                        return normalizeRole(foundRole);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка извлечения роли: " + e.getMessage());
        }
        return "USER"; // Значение по умолчанию
    }

    // Нормализация роли (приведение к формату ADMIN/USER)
    private String normalizeRole(String role) {
        if (role == null) return "USER";

        role = role.toUpperCase().trim();

        // Если роль в формате ROLE_ADMIN или ROLE_USER
        if (role.startsWith("ROLE_")) {
            role = role.substring(5); // Убираем ROLE_
        }

        // Проверяем, что роль соответствует нашим ожиданиям
        if ("ADMIN".equals(role) || "USER".equals(role)) {
            return role;
        }

        return "USER"; // Значение по умолчанию
    }

    // Также улучшим метод getUsernameFromToken для надежности
    private String getUsernameFromToken(String token) {
        try {
            // Сначала пробуем получить username из эндпоинта /userwithouttasks
            // Если не получится, тогда парсим из токена
            Map<String, Object> userInfo = getUserInfo(token);
            if (userInfo != null && userInfo.containsKey("username")) {
                return userInfo.get("username").toString();
            }

            // Если не получилось из API, парсим из токена
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));

                // Ищем username в разных полях
                String[] usernameFields = {"\"sub\":\"", "\"username\":\"", "\"preferred_username\":\""};

                for (String field : usernameFields) {
                    int usernameStart = payload.indexOf(field);
                    if (usernameStart != -1) {
                        usernameStart += field.length();
                        int usernameEnd = payload.indexOf("\"", usernameStart);
                        if (usernameEnd > usernameStart) {
                            return payload.substring(usernameStart, usernameEnd);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка извлечения username: " + e.getMessage());
        }
        return null;
    }

    private void openNewFrameWithAnimation(JFrame newFrame) {
        // Убедимся, что окно невидимо перед анимацией
        newFrame.setVisible(false);

        // Для окон без декораций можно использовать прозрачность
        // Для окон с декорациями используем альтернативный подход

        try {
            // Пробуем установить прозрачность (работает для undecorated окон)
            newFrame.setOpacity(0f);
            newFrame.setVisible(true);

            // Анимация появления
            Timer fadeInTimer = new Timer(20, new ActionListener() {
                float opacity = 0f;
                @Override
                public void actionPerformed(ActionEvent e) {
                    opacity += 0.05f; // Медленнее для плавности
                    if (opacity >= 1f) {
                        opacity = 1f;
                        ((Timer) e.getSource()).stop();
                    }
                    try {
                        newFrame.setOpacity(opacity);
                    } catch (IllegalComponentStateException ex) {
                        // Если нельзя установить прозрачность, просто показываем окно
                        ((Timer) e.getSource()).stop();
                        newFrame.setVisible(true);
                    }
                }
            });
            fadeInTimer.start();

        } catch (IllegalComponentStateException e) {
            // Если установка прозрачности не поддерживается, просто показываем окно
            newFrame.setVisible(true);
        }
    }
}





