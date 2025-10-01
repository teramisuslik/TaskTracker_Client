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

public class RegistrationFrame extends JFrame {

    private JPanel contentPane;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JLabel statusLabel;
    private JLabel closeLabel;
    private JLabel minimizeLabel;
    private JPanel headerPanel;

    // URL вашего сервера для регистрации
    private static final String REGISTER_URL = "http://localhost:8080/register";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // Переменные для перетаскивания окна
    private int dragX, dragY;

    public RegistrationFrame() {
        setUndecorated(true);
        setTitle("Регистрация");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setShape(new RoundRectangle2D.Double(0, 0, 1000, 700, 40, 40));

        // Жестко фиксируем размер и позицию
        setMinimumSize(new Dimension(1000, 700));
        setMaximumSize(new Dimension(1000, 700));

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
        JLabel titleLabel = new JLabel("Создать аккаунт");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        // Подзаголовок
        JLabel subtitleLabel = new JLabel("Заполните данные для регистрации");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(200, 200, 200));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setHorizontalAlignment(JLabel.CENTER);

        // Панель для формы с неоморфным эффект
        JPanel formPanel = createNeomorphicPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setMaximumSize(new Dimension(400, 500));

        // Иконка регистрации - ИСПРАВЛЕННАЯ ВЕРСИЯ
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

        JPanel confirmPasswordPanel = createInputPanel("Повторите пароль");
        confirmPasswordField = createStyledPasswordField();
        confirmPasswordPanel.add(confirmPasswordField);

        // Кнопка регистрации - УПРОЩЕННАЯ ВЕРСИЯ БЕЗ АНИМАЦИЙ
        registerButton = new JButton("Создать аккаунт");
        styleModernRegisterButton(registerButton);
        registerButton.addActionListener(new RegisterButtonListener());

        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginPanel.setOpaque(false);
        loginPanel.setMaximumSize(new Dimension(350, 30));

        JLabel questionLabel = new JLabel("Уже есть аккаунт? ");
        questionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        questionLabel.setForeground(Color.WHITE);
        questionLabel.setOpaque(false);

        JLabel loginLabel = createClickableLoginLabel();

        loginPanel.add(questionLabel);
        loginPanel.add(loginLabel);

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
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(passwordPanel);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(confirmPasswordPanel);
        formPanel.add(Box.createVerticalStrut(30));
        formPanel.add(registerButton);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(statusLabel);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(loginPanel);

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
        label.setPreferredSize(new Dimension(140, 30));

        panel.add(label, BorderLayout.WEST);
        return panel;
    }

    // Стилизованное текстовое поле
    private JTextField createStyledTextField() {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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

    private JLabel createControlLabel(String text, Color color) {
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(color);
                g2.fillOval(2, 2, getWidth()-4, getHeight()-4);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
            }
        };
        label.setPreferredSize(new Dimension(28, 28));
        label.setMinimumSize(new Dimension(28, 28));
        label.setMaximumSize(new Dimension(28, 28));
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (label == closeLabel) {
                    disposeWithAnimation();
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
    private void styleModernRegisterButton(JButton button) {
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

    private void disposeWithAnimation() {
        Timer fadeOut = new Timer(10, new ActionListener() {
            float opacity = 1f;
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity -= 0.05f;
                if (opacity <= 0f) {
                    opacity = 0f;
                    ((Timer)e.getSource()).stop();
                    dispose();
                }
                setOpacity(opacity);
            }
        });
        fadeOut.start();
    }

    // ОБНОВЛЕННЫЙ ОБРАБОТЧИК КНОПКИ РЕГИСТРАЦИИ
    private class RegisterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

            if (!validateInput(username, password, confirmPassword)) {
                return;
            }

            registerButton.setEnabled(false);
            // УБРАЛ надпись "Отправка запроса на сервер..."

            new Thread(() -> {
                try {
                    String response = sendRegistrationRequest(username, password);
                    SwingUtilities.invokeLater(() -> {
                        handleServerResponse(response, username);
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        showStatus("Ошибка соединения: " + ex.getMessage(), new Color(231, 76, 60));
                        registerButton.setEnabled(true);
                        shakeAnimation();
                    });
                }
            }).start();
        }

        private boolean validateInput(String username, String password, String confirmPassword) {
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showStatus("Заполните все поля!", new Color(231, 76, 60));
                shakeAnimation();
                return false;
            }

            if (username.length() < 3) {
                showStatus("Логин должен быть не менее 3 символов!", new Color(231, 76, 60));
                shakeAnimation();
                return false;
            }

            if (password.length() < 6) {
                showStatus("Пароль должен быть не менее 6 символов!", new Color(231, 76, 60));
                shakeAnimation();
                return false;
            }

            if (!password.equals(confirmPassword)) {
                showStatus("Пароли не совпадают!", new Color(231, 76, 60));
                shakeAnimation();
                return false;
            }

            return true;
        }

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

        private void handleServerResponse(String response, String username) {
            registerButton.setEnabled(true);

            try {
                if (response.contains("\"message\":\"User registered successfully\"") ||
                        response.contains("success") ||
                        response.contains("200")) {

                    // Показываем диалог успеха без статуса под кнопкой
                    showSuccessDialog(username);

                } else if (response.contains("username") && response.contains("already")) {
                    showStatus("Логин уже занят", new Color(231, 76, 60));
                    showErrorDialog("Пользователь с таким логином уже существует!", "Ошибка регистрации");
                    shakeAnimation();

                } else if (response.contains("error") || response.contains("400") || response.contains("409")) {
                    showStatus("Ошибка регистрации", new Color(231, 76, 60));
                    showErrorDialog("Ошибка при регистрации: " + response, "Ошибка сервера");
                    shakeAnimation();
                } else {
                    // Показываем диалог успеха без статуса под кнопкой
                    showSuccessDialog(username);
                }
            } catch (Exception ex) {
                showStatus("Ошибка обработки ответа", new Color(231, 76, 60));
                showErrorDialog("Ошибка: " + ex.getMessage(), "Ошибка");
                shakeAnimation();
            }
        }
    }

    // Анимации
    private void shakeAnimation() {
        // Упрощенная анимация без смещения contentPane
        Timer shakeTimer = new Timer(20, new ActionListener() {
            int count = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (count < 8) {
                    // Просто меняем цвет статуса для эффекта
                    statusLabel.setForeground(count % 2 == 0 ? new Color(231, 76, 60) : new Color(255, 150, 150));
                    count++;
                } else {
                    statusLabel.setForeground(new Color(231, 76, 60));
                    ((Timer)e.getSource()).stop();
                }
            }
        });
        shakeTimer.start();
    }

    // Красивое окно ошибки
    private void showErrorDialog(String message, String title) {
        JDialog errorDialog = new JDialog(this, "", true);
        errorDialog.setUndecorated(true);
        errorDialog.setSize(400, 300);
        errorDialog.setLocationRelativeTo(this);
        errorDialog.setResizable(false);
        errorDialog.setShape(new RoundRectangle2D.Double(0, 0, 400, 300, 25, 25));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(30, 30, 40));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel iconLabel = new JLabel("✗") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(231, 76, 60));
                g2.fillOval(0, 0, getWidth(), getHeight());

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 36));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth("✗")) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString("✗", x, y);
                g2.dispose();
            }
        };
        iconLabel.setPreferredSize(new Dimension(80, 80));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(231, 76, 60));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = new JLabel("<html><center>" + message + "</center></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(200, 200, 200));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton okButton = new JButton("Понятно") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(231, 76, 60));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(text, x, y);
                g2.dispose();
            }
        };
        okButton.setPreferredSize(new Dimension(120, 40));
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        okButton.setForeground(Color.WHITE);
        okButton.setBorder(BorderFactory.createEmptyBorder());
        okButton.setContentAreaFilled(false);
        okButton.setFocusPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        okButton.addActionListener(e -> errorDialog.dispose());

        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(okButton);

        errorDialog.add(contentPanel);
        errorDialog.getRootPane().setDefaultButton(okButton);
        errorDialog.setLocationRelativeTo(this);
        errorDialog.setVisible(true);
    }

    private void showStatus(String message, Color color) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setForeground(color);
            statusLabel.setVisible(true);

            // Если сообщение пустое, скрываем метку
            if (message == null || message.trim().isEmpty()) {
                statusLabel.setVisible(false);
            }
        }
    }

    // УБРАЛ методы которые больше не нужны
    // private void startLoadingAnimation() { ... }
    // private void stopLoadingAnimation() { ... }

    // Красивое окно успешной регистрации с большой кнопкой внизу
    private void showSuccessDialog(String username) {
        // Очищаем статус под кнопкой
        showStatus("", Color.BLACK);

        JDialog successDialog = new JDialog(this, "", true);
        successDialog.setUndecorated(true);
        successDialog.setSize(450, 450);
        successDialog.setLocationRelativeTo(this);
        successDialog.setResizable(false);
        successDialog.setShape(new RoundRectangle2D.Double(0, 0, 450, 450, 25, 25));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBackground(new Color(30, 30, 40));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Панель для контента (все кроме кнопки)
        JPanel contentTopPanel = new JPanel();
        contentTopPanel.setLayout(new BoxLayout(contentTopPanel, BoxLayout.Y_AXIS));
        contentTopPanel.setOpaque(false);

        // Иконка успеха
        JLabel iconLabel = new JLabel("✓") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(0, 0, new Color(46, 204, 113),
                        0, getHeight(), new Color(39, 174, 96));
                g2.setPaint(gradient);
                g2.fillOval(0, 0, getWidth(), getHeight());

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 36));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth("✓")) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString("✓", x, y);
                g2.dispose();
            }
        };
        iconLabel.setPreferredSize(new Dimension(80, 80));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Успешная регистрация!");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = new JLabel("<html><center>Пользователь <b style='color:#9b59b6'>" + username + "</b><br>успешно зарегистрирован!</center></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        messageLabel.setForeground(new Color(200, 200, 200));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Собираем верхнюю часть
        contentTopPanel.add(iconLabel);
        contentTopPanel.add(Box.createVerticalStrut(25));
        contentTopPanel.add(titleLabel);
        contentTopPanel.add(Box.createVerticalStrut(20));
        contentTopPanel.add(messageLabel);

        // Кнопка "Понятно" - БОЛЬШАЯ и внизу
        JButton okButton = new JButton("ПОНЯТНО") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(0, 0, new Color(155, 89, 182),
                        0, getHeight(), new Color(142, 68, 173));
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(text, x, y);
                g2.dispose();
            }
        };
        okButton.setPreferredSize(new Dimension(300, 60));
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        okButton.setForeground(Color.WHITE);
        okButton.setBorder(BorderFactory.createEmptyBorder());
        okButton.setContentAreaFilled(false);
        okButton.setFocusPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        okButton.addActionListener(e -> {
            successDialog.dispose();
            disposeWithAnimation();
        });

        // Добавляем компоненты в основную панель
        contentPanel.add(contentTopPanel, BorderLayout.CENTER);
        contentPanel.add(okButton, BorderLayout.SOUTH);

        successDialog.add(contentPanel);
        successDialog.getRootPane().setDefaultButton(okButton);
        successDialog.setLocationRelativeTo(this);
        successDialog.setVisible(true);
    }

    private JLabel createClickableLoginLabel() {
        JLabel loginLabel = new JLabel("Войдите");
        loginLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        loginLabel.setForeground(new Color(155, 89, 182));
        loginLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLabel.setOpaque(false);

        loginLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                disposeWithAnimation();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                loginLabel.setForeground(new Color(142, 68, 173));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginLabel.setForeground(new Color(155, 89, 182));
            }
        });

        return loginLabel;
    }
}