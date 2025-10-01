package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
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
    private JLabel closeLabel;
    private JLabel minimizeLabel;
    private JPanel headerPanel;

    // URL вашего сервера
    private static final String LOGIN_URL = "http://localhost:8080/login";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // Для сохранения токена
    private static final Preferences prefs = Preferences.userNodeForPackage(LoginFrame.class);
    private static String jwtToken = null;

    // Переменные для перетаскивания окна
    private int dragX, dragY;

    public LoginFrame(){
        setUndecorated(true);
        setTitle("Авторизация");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setShape(new RoundRectangle2D.Double(0, 0, 1000, 700, 40, 40));

        // Создаем стек для слоев
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));

        // Фоновый градиент
        BackgroundPanel backgroundPanel = new BackgroundPanel();
        backgroundPanel.setBounds(0, 0, 1000, 700);

        // Основной контент
        contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setOpaque(false);
        contentPane.setBorder(BorderFactory.createEmptyBorder(15, 50, 40, 50));

        // Заголовок окна с кнопками закрытия
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(1000, 45));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        controlsPanel.setOpaque(false);
        controlsPanel.setPreferredSize(new Dimension(80, 35));

        minimizeLabel = createControlLabel("−", new Color(155, 89, 182));
        closeLabel = createControlLabel("×", new Color(231, 76, 60));

        controlsPanel.add(minimizeLabel);
        controlsPanel.add(closeLabel);

        headerPanel.add(controlsPanel, BorderLayout.EAST);

        // Центральная панель с формой
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setMaximumSize(new Dimension(400, 600));

        // Заголовок формы
        titleLabel = new JLabel("Добро пожаловать");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        // Подзаголовок
        subtitleLabel = new JLabel("Войдите в свой аккаунт");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(200, 200, 200));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setHorizontalAlignment(JLabel.CENTER);

        // Панель для формы с неоморфным эффектом
        JPanel formPanel = createNeomorphicPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setMaximumSize(new Dimension(400, 400));

        // Иконка пользователя - ИСПРАВЛЕННАЯ ВЕРСИЯ
        JLabel userIcon = new JLabel("👤") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Фиолетовый градиент для иконки
                GradientPaint gradient = new GradientPaint(0, 0, new Color(155, 89, 182),
                        0, getHeight(), new Color(142, 68, 173));
                g2.setPaint(gradient);
                g2.fillOval(0, 0, getWidth(), getHeight());

                // Отображаем смайлик как текст
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                String icon = "👤";
                int x = (getWidth() - fm.stringWidth(icon)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(icon, x, y);
                g2.dispose();
            }
        };
        userIcon.setPreferredSize(new Dimension(80, 80));
        userIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        userIcon.setMaximumSize(new Dimension(80, 80));

        // Поля ввода
        JPanel usernamePanel = createInputPanel("Логин");
        usernameField = createStyledTextField();
        usernamePanel.add(usernameField);

        JPanel passwordPanel = createInputPanel("Пароль");
        passwordField = createStyledPasswordField();
        passwordPanel.add(passwordField);

        // Кнопка входа - УПРОЩЕННАЯ ВЕРСИЯ БЕЗ АНИМАЦИЙ
        loginButton = new JButton("Войти в систему");
        styleModernLoginButton(loginButton);
        loginButton.addActionListener(new LoginButtonListener());

        JPanel registrationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        registrationPanel.setOpaque(false);
        registrationPanel.setMaximumSize(new Dimension(350, 30));

        JLabel questionLabel = new JLabel("Нет аккаунта? ");
        questionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        questionLabel.setForeground(Color.WHITE);
        questionLabel.setOpaque(false);

        JLabel registerLabel = createClickableRegistrationLabel();

        registrationPanel.add(questionLabel);
        registrationPanel.add(registerLabel);

        // Статусная строка
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(255, 255, 255, 180));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setOpaque(false);

        // Сборка формы
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(userIcon);
        formPanel.add(Box.createVerticalStrut(30));
        formPanel.add(usernamePanel);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(passwordPanel);
        formPanel.add(Box.createVerticalStrut(30));
        formPanel.add(loginButton);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(statusLabel);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(registrationPanel);

        // Сборка центральной панели
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(subtitleLabel);
        centerPanel.add(Box.createVerticalStrut(40));
        centerPanel.add(formPanel);

        // Сборка основного контента
        contentPane.add(headerPanel);
        contentPane.add(centerPanel);

        // Добавляем все в слои
        layeredPane.add(backgroundPanel, Integer.valueOf(0));
        layeredPane.add(contentPane, Integer.valueOf(1));

        setContentPane(layeredPane);

        // Добавляем возможность перетаскивания окна
        addMouseListeners();
    }

    // Кастомная панель с ЧЕРНО-ФИОЛЕТОВЫМ градиентом
    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Черно-фиолетовый градиент
            GradientPaint mainGradient = new GradientPaint(0, 0, new Color(25, 25, 35),
                    getWidth(), getHeight(), new Color(45, 30, 60));
            g2.setPaint(mainGradient);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);

            // Декоративные элементы
            drawBubbles(g2);
        }

        private void drawBubbles(Graphics2D g2) {
            // Фиолетовые пузырьки на заднем плане
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
            g2.setColor(new Color(155, 89, 182));

            // Большой пузырь
            g2.fillOval(-50, -50, 200, 200);
            g2.fillOval(getWidth() - 100, getHeight() - 150, 300, 300);
            g2.fillOval(getWidth() - 250, 50, 150, 150);

            // Дополнительные мелкие пузырьки
            g2.fillOval(100, getHeight() - 200, 100, 100);
            g2.fillOval(getWidth() - 150, 200, 80, 80);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
    }

    private JPanel createNeomorphicPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Темный неоморфный эффект
                Color background = new Color(255, 255, 255, 10);
                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                // Бордер с фиолетовым оттенком
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(new Color(155, 89, 182, 80));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 25, 25);
            }
        };
    }

    // Метод для создания полей ввода
    private JPanel createInputPanel(String labelText) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(15, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(350, 70));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Метка
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        label.setPreferredSize(new Dimension(70, 30));

        panel.add(label, BorderLayout.WEST);
        return panel;
    }

    // Стилизованное текстовое поле
    private JTextField createStyledTextField() {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                // Сначала рисуем стандартный компонент
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Затем рисуем бордер поверх
                g2.setColor(new Color(155, 89, 182, 120));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2.dispose();
            }
        };

        field.setPreferredSize(new Dimension(250, 45));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(Color.WHITE);
        field.setCaretColor(new Color(155, 89, 182));
        field.setBackground(new Color(40, 40, 50));
        field.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        field.setOpaque(true);

        return field;
    }

    // Стилизованное поле пароля
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                // Сначала рисуем стандартный компонент
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Затем рисуем бордер поверх
                g2.setColor(new Color(155, 89, 182, 120));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2.dispose();
            }
        };

        field.setPreferredSize(new Dimension(250, 45));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(Color.WHITE);
        field.setCaretColor(new Color(155, 89, 182));
        field.setBackground(new Color(40, 40, 50));
        field.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        field.setOpaque(true);

        return field;
    }

    // ИСПРАВЛЕННЫЙ метод создания кнопок управления
    private JLabel createControlLabel(String text, Color color) {
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Круглая кнопка - УВЕЛИЧИЛИ РАЗМЕР
                g2.setColor(color);
                g2.fillOval(2, 2, getWidth()-4, getHeight()-4); // Добавили отступы

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
            }
        };
        label.setPreferredSize(new Dimension(28, 28)); // Увеличили размер
        label.setMinimumSize(new Dimension(28, 28));
        label.setMaximumSize(new Dimension(28, 28));
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (label == closeLabel) {
                    System.exit(0);
                } else if (label == minimizeLabel) {
                    setState(Frame.ICONIFIED);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                label.repaint();
            }
        });

        return label;
    }

    // ОБНОВЛЕННЫЙ МЕТОД СТИЛИЗАЦИИ КНОПКИ
    private void styleModernLoginButton(JButton button) {
        button.setPreferredSize(new Dimension(320, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(155, 89, 182));
        button.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        button.setContentAreaFilled(true);

        // Убираем все эффекты при наведении, кроме смены курсора
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                // Только меняем курсор на руку
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            public void mouseExited(MouseEvent evt) {
                // Возвращаем стандартный курсор
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    private void addMouseListeners() {
        headerPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                dragX = e.getX();
                dragY = e.getY();
            }
        });

        headerPanel.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();
                setLocation(x - dragX, y - dragY);
            }
        });
    }

    // Проверка сохраненного токена
    public void checkAuthentication() {
        String savedToken = prefs.get("jwt_token", null);
        if (savedToken != null && !savedToken.isEmpty()) {
            jwtToken = savedToken;
            openMainApplicationFrame(jwtToken);
        } else {
            setVisible(true);
            // Анимация появления
            setOpacity(0f);
            Timer fadeIn = new Timer(10, new ActionListener() {
                float opacity = 0f;
                @Override
                public void actionPerformed(ActionEvent e) {
                    opacity += 0.05f;
                    if (opacity >= 1f) {
                        opacity = 1f;
                        ((Timer)e.getSource()).stop();
                    }
                    setOpacity(opacity);
                }
            });
            fadeIn.start();
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

    // Анимации
    private void shakeAnimation() {
        Timer shakeTimer = new Timer(20, new ActionListener() {
            int count = 0;
            int originalX = contentPane.getX();

            @Override
            public void actionPerformed(ActionEvent e) {
                if (count < 10) {
                    int offset = (count % 2 == 0) ? 5 : -5;
                    contentPane.setLocation(originalX + offset, contentPane.getY());
                    count++;
                } else {
                    contentPane.setLocation(originalX, contentPane.getY());
                    ((Timer)e.getSource()).stop();
                }
            }
        });
        shakeTimer.start();
    }

    private void openRegistrationFrame() {
        RegistrationFrame registrationFrame = new RegistrationFrame();
        registrationFrame.setVisible(true);
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

    private void openMainApplicationFrame(String token) {
        Timer fadeOutTimer = new Timer(20, new ActionListener() {
            float opacity = 1f;
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity -= 0.05f;
                if (opacity <= 0f) {
                    opacity = 0f;
                    ((Timer) e.getSource()).stop();

                    setVisible(false);
                    dispose();

                    Map<String, Object> userInfo = getUserInfo(token);
                    String role = (userInfo != null && userInfo.containsKey("role")) ?
                            userInfo.get("role").toString() : "USER";

                    JFrame newFrame;
                    if ("ADMIN".equals(role.toUpperCase())) {
                        newFrame = new AdminApplicationFrame(token, userInfo);
                    } else {
                        newFrame = new UserApplicationFrame(token, userInfo);
                    }
                    newFrame.setVisible(true);
                }
                setOpacity(opacity);
            }
        });
        fadeOutTimer.start();
    }

    private Map<String, Object> getUserInfo(String token) {
        try {
            String username = getUsernameFromTokenDirect(token);
            if (username == null) return null;

            HttpClient client = HttpClient.newHttpClient();
            String url = "http://localhost:8080/userwithouttasks?username=" +
                    java.net.URLEncoder.encode(username, "UTF-8");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseUserInfo(response.body());
            }
        } catch (Exception e) {
            System.err.println("Ошибка получения информации о пользователе: " + e.getMessage());
        }
        return null;
    }

    private String getUsernameFromTokenDirect(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));

                int subStart = payload.indexOf("\"sub\":\"");
                if (subStart != -1) {
                    subStart += 7;
                    int subEnd = payload.indexOf("\"", subStart);
                    if (subEnd > subStart) {
                        return payload.substring(subStart, subEnd);
                    }
                }

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

            if (jsonResponse.contains("\"username\"")) {
                int usernameIndex = jsonResponse.indexOf("\"username\"");
                if (usernameIndex != -1) {
                    int colonIndex = jsonResponse.indexOf(":", usernameIndex);
                    if (colonIndex != -1) {
                        int valueStart = colonIndex + 1;
                        while (valueStart < jsonResponse.length() && Character.isWhitespace(jsonResponse.charAt(valueStart))) {
                            valueStart++;
                        }
                        if (valueStart < jsonResponse.length()) {
                            if (jsonResponse.charAt(valueStart) == '"') {
                                valueStart++;
                                int valueEnd = jsonResponse.indexOf("\"", valueStart);
                                if (valueEnd != -1) {
                                    String username = jsonResponse.substring(valueStart, valueEnd);
                                    userInfo.put("username", username);
                                }
                            }
                        }
                    }
                }
            }

            String role = extractRoleFromJson(jsonResponse);
            userInfo.put("role", role);
            return userInfo;
        } catch (Exception e) {
            System.err.println("Ошибка парсинга информации о пользователе: " + e.getMessage());
        }
        return null;
    }

    private String extractRoleFromJson(String jsonResponse) {
        try {
            String[] rolePatterns = {
                    "\"role\":\"([^\"]+)\"",
                    "\"role\": \"([^\"]+)\"",
                    "\"authority\":\"([^\"]+)\"",
                    "\"authority\": \"([^\"]+)\""
            };

            for (String pattern : rolePatterns) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher m = p.matcher(jsonResponse);
                if (m.find()) {
                    String foundRole = m.group(1);
                    return normalizeRole(foundRole);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка извлечения роли: " + e.getMessage());
        }
        return "USER";
    }

    private String normalizeRole(String role) {
        if (role == null) return "USER";
        role = role.toUpperCase().trim();
        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
        }
        if ("ADMIN".equals(role) || "USER".equals(role)) {
            return role;
        }
        return "USER";
    }

    private JLabel createClickableRegistrationLabel() {
        JLabel registrationLabel = new JLabel("Создайте его");
        registrationLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        registrationLabel.setForeground(new Color(155, 89, 182));
        registrationLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registrationLabel.setOpaque(false);

        registrationLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openRegistrationFrame();
            }
        });

        return registrationLabel;
    }

    private void showStatus(String message, Color color) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setForeground(color);

            // Всегда делаем метку видимой при установке текста
            statusLabel.setVisible(true);

            // Если сообщение пустое, скрываем метку
            if (message == null || message.trim().isEmpty()) {
                statusLabel.setVisible(false);
            }
        }
    }


    private void handleServerResponse(String response, String username) {
        loginButton.setEnabled(true);

        try {
            if (response.contains("\"token\"")) {
                String token = extractTokenFromResponse(response);

                if (token != null && !token.isEmpty()) {
                    saveToken(token);
                    onAuthorizationSuccess(); // Только один вызов

                    Timer timer = new Timer(1000, evt -> {
                        openMainApplicationFrame(token);
                    });
                    timer.setRepeats(false);
                    timer.start();
                } else {
                    showStatus("Токен не найден в ответе", new Color(231, 76, 60));
                    shakeAnimation();
                }
            } else if (response.contains("error") || response.contains("Unauthorized")) {
                showStatus("Неверный логин или пароль", new Color(231, 76, 60));
                shakeAnimation();
            } else {
                showStatus("Неизвестный ответ от сервера", new Color(231, 76, 60));
                shakeAnimation();
            }
        } catch (Exception ex) {
            showStatus("Ошибка обработки ответа", new Color(231, 76, 60));
            shakeAnimation();
        }
    }


    // ОБНОВЛЕННЫЙ ОБРАБОТЧИК УСПЕШНОЙ АВТОРИЗАЦИИ
    private void onAuthorizationSuccess() {
        // УБРАЛ вызов stopLoadingAnimation()
        showStatus("Авторизация успешна!", new Color(46, 204, 113));

        // Кнопка не меняет внешний вид, только становится неактивной
        if (loginButton != null) {
            loginButton.setEnabled(false);
        }
    }


    // ОБНОВЛЕННЫЙ МЕТОД ДЛЯ КНОПКИ ВХОДА (убираем анимацию загрузки на кнопке)
    private class LoginButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                showStatus("Заполните все поля!", new Color(231, 76, 60));
                shakeAnimation();
                return;
            }

            loginButton.setEnabled(false);
            // УБРАЛ надпись "Подключаемся к серверу..."

            new Thread(() -> {
                try {
                    String response = sendLoginRequest(username, password);
                    SwingUtilities.invokeLater(() -> {
                        handleServerResponse(response, username);
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        showStatus("Ошибка соединения: " + ex.getMessage(), new Color(231, 76, 60));
                        loginButton.setEnabled(true);
                        shakeAnimation();
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
}