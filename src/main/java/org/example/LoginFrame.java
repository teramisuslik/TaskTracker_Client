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

    // Метод для открытия главного окна приложения
    private void openMainApplicationFrame(String token) {
        this.dispose(); // Закрываем окно входа
        MainApplicationFrame mainFrame = new MainApplicationFrame(token);
        mainFrame.setVisible(true);
    }

}





