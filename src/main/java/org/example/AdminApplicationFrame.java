package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.List;


public class AdminApplicationFrame extends JFrame {
    private String authToken;
    private Map<String, Object> userInfo;
    private JPanel centerPanel;
    private CardLayout cardLayout;
    private List<Task> allAdminTasks;
    private List<Task> originalAllAdminTasks;
    private JComboBox<String> adminStatusFilter;
    private JComboBox<String> adminImportanceFilter;
    private JComboBox<String> adminSortFilter;
    private JButton adminApplyFiltersButton;
    private JButton adminResetFiltersButton;

    private NotificationManager notificationManager;
    private AdminNotificationConsumer notificationConsumer;

    public AdminApplicationFrame(String token, Map<String, Object> userInfo) {
        this.authToken = token;
        this.userInfo = userInfo;

        String username = userInfo != null && userInfo.containsKey("username") ?
                userInfo.get("username").toString() : "Администратор";

        setTitle("Панель администратора - " + username);
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Создаем слоеную панель для фона и контента
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));

        // Фоновый градиент
        BackgroundPanel backgroundPanel = new BackgroundPanel();
        backgroundPanel.setBounds(0, 0, 1200, 800);

        // Основной контент
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setOpaque(false);

        // Панель для верхней части
        JPanel topPanel = createModernTopPanel(username);

        // Центральная панель с CardLayout для переключения между разными видами
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.setOpaque(false);

        // Панель приветствия
        JPanel welcomePanel = createModernWelcomePanel();

        // Панель для всех задач
        JPanel allTasksPanel = new JPanel(new BorderLayout());
        allTasksPanel.setOpaque(false);

        centerPanel.add(welcomePanel, "welcome");
        centerPanel.add(allTasksPanel, "alltasks");

        // Добавляем компоненты
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(centerPanel, BorderLayout.CENTER);

        // Добавляем все в слои
        layeredPane.add(backgroundPanel, Integer.valueOf(0));
        layeredPane.add(contentPane, Integer.valueOf(1));

        setContentPane(layeredPane);

        // Инициализация системы уведомлений
        initializeNotificationSystem();

        // Загружаем статистику при создании
        loadAdminStatistics();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                loadAdminStatistics();
            }

            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                onWindowClosing();
            }
        });
    }

    private void styleAdminButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(52, 152, 219));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void showInfoDialog(String title, String message) {
        JDialog infoDialog = new JDialog(this, title, true);
        infoDialog.setSize(400, 250);
        infoDialog.setLocationRelativeTo(this);
        infoDialog.setResizable(false);
        infoDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(255, 255, 255));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Иконка информации
        JLabel iconLabel = new JLabel("ℹ", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Arial", Font.BOLD, 48));
        iconLabel.setForeground(new Color(52, 152, 219));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Заголовок
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Сообщение
        JLabel messageLabel = new JLabel("<html><center>" + message + "</center></html>", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(127, 140, 141));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Кнопка OK
        JButton okButton = new JButton("Понятно");
        okButton.setFont(new Font("Arial", Font.BOLD, 14));
        okButton.setForeground(Color.WHITE);
        okButton.setBackground(new Color(52, 152, 219));
        okButton.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
        okButton.setFocusPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        okButton.addActionListener(e -> infoDialog.dispose());

        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                okButton.setBackground(new Color(41, 128, 185));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                okButton.setBackground(new Color(52, 152, 219));
            }
        });

        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(okButton);

        infoDialog.add(contentPanel, BorderLayout.CENTER);
        infoDialog.getRootPane().setDefaultButton(okButton);
        infoDialog.pack();
        infoDialog.setLocationRelativeTo(this);
        infoDialog.setVisible(true);
    }

    private JButton createRedLogoutButton() {
        JButton logoutButton = new JButton("Выйти");

        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        logoutButton.addActionListener(e -> {
            showLogoutConfirmationDialog();
        });

        return logoutButton;
    }

    private JPanel createTopPanel(String username) {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 245, 245));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        topPanel.setPreferredSize(new Dimension(getWidth(), 140));

        // Левая панель с информацией
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Панель администратора", SwingConstants.LEFT);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(44, 62, 80));
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userInfoLabel = new JLabel("Вы вошли как: " + username + " (ADMIN)", SwingConstants.LEFT);
        userInfoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userInfoLabel.setForeground(new Color(127, 140, 141));
        userInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(welcomeLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(userInfoLabel);
        leftPanel.add(Box.createVerticalStrut(15));

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton dashboardButton = new JButton("Главная");
        JButton allTasksButton = new JButton("Все задачи");
        JButton manageUsersButton = new JButton("Управление");
        JButton createTaskButton = new JButton("Создать задачу");

        styleAdminButton(dashboardButton);
        styleAdminButton(allTasksButton);
        styleAdminButton(manageUsersButton);
        styleAdminButton(createTaskButton);

        dashboardButton.addActionListener(e -> showDashboard());
        allTasksButton.addActionListener(e -> showAllTasks());
        manageUsersButton.addActionListener(e -> showUserManagement());
        createTaskButton.addActionListener(e -> createNewTask());

        buttonPanel.add(dashboardButton);
        buttonPanel.add(allTasksButton);
        buttonPanel.add(manageUsersButton);
        buttonPanel.add(createTaskButton);

        leftPanel.add(buttonPanel);

        // Правая панель с кнопкой выхода
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(createRedLogoutButton());

        topPanel.add(leftPanel, BorderLayout.CENTER);
        topPanel.add(rightPanel, BorderLayout.EAST);

        return topPanel;
    }

    private JPanel createWelcomePanel() {
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(new Color(248, 249, 250));
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 40, 40));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(248, 249, 250));

        JLabel titleLabel = new JLabel("Добро пожаловать в панель администратора!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(new Color(33, 37, 41));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 16));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(new Color(248, 249, 250));
        descriptionArea.setText("\nЗдесь вы можете управлять всеми задачами системы, создавать новые задания, " +
                "назначать их пользователям и отслеживать прогресс выполнения. " +
                "Используйте меню выше для навигации по разделам административной панели.\n");
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(20, 50, 30, 50));

        JPanel statsPanel = createAdminStatsPanel();

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(descriptionArea);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(statsPanel);

        welcomePanel.add(contentPanel, BorderLayout.CENTER);
        return welcomePanel;
    }

    private void styleAdminComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        comboBox.setFocusable(false);
        comboBox.setMaximumRowCount(10);
    }

    private void styleAdminFilterButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
    }

    private void applyAdminFilters() {
        if (originalAllAdminTasks == null || originalAllAdminTasks.isEmpty()) {
            System.out.println("DEBUG: No admin tasks to filter");
            return;
        }

        List<Task> filteredTasks = new ArrayList<>(originalAllAdminTasks);
        System.out.println("DEBUG: Starting with " + filteredTasks.size() + " admin tasks");

        // Фильтрация по статусу
        String selectedStatus = (String) adminStatusFilter.getSelectedItem();
        if (!"Все статусы".equals(selectedStatus)) {
            int before = filteredTasks.size();
            filteredTasks.removeIf(task ->
                    task.getStatus() == null || !task.getStatus().equals(selectedStatus)
            );
            System.out.println("DEBUG: Admin status filter '" + selectedStatus + "' removed " + (before - filteredTasks.size()) + " tasks");
        }

        // Фильтрация по важности
        String selectedImportance = (String) adminImportanceFilter.getSelectedItem();
        if (!"Все приоритеты".equals(selectedImportance)) {
            int before = filteredTasks.size();
            filteredTasks.removeIf(task ->
                    task.getImportance() == null || !task.getImportance().equals(selectedImportance)
            );
            System.out.println("DEBUG: Admin importance filter '" + selectedImportance + "' removed " + (before - filteredTasks.size()) + " tasks");
        }

        // Сортировка по дедлайну
        String selectedSort = (String) adminSortFilter.getSelectedItem();
        System.out.println("DEBUG: Admin selected sort: " + selectedSort);

        if ("Дедлайн ↑".equals(selectedSort)) {
            System.out.println("DEBUG: Sorting admin tasks by deadline ascending");
            filteredTasks.sort((t1, t2) -> {
                String deadline1 = t1.getDeadline();
                String deadline2 = t2.getDeadline();

                if (deadline1 == null && deadline2 == null) return 0;
                if (deadline1 == null) return 1;
                if (deadline2 == null) return -1;

                return deadline1.compareTo(deadline2);
            });
        } else if ("Дедлайн ↓".equals(selectedSort)) {
            System.out.println("DEBUG: Sorting admin tasks by deadline descending");
            filteredTasks.sort((t1, t2) -> {
                String deadline1 = t1.getDeadline();
                String deadline2 = t2.getDeadline();

                if (deadline1 == null && deadline2 == null) return 0;
                if (deadline1 == null) return 1;
                if (deadline2 == null) return -1;

                return deadline2.compareTo(deadline1);
            });
        }

        // Обновляем отображаемые задачи
        allAdminTasks = filteredTasks;
        System.out.println("DEBUG: Final admin task count: " + allAdminTasks.size());
        refreshAdminTasksDisplay();
    }

    private void resetAdminFilters() {
        adminStatusFilter.setSelectedIndex(0);
        adminImportanceFilter.setSelectedIndex(0);
        adminSortFilter.setSelectedIndex(0);

        if (originalAllAdminTasks != null) {
            allAdminTasks = new ArrayList<>(originalAllAdminTasks);
            refreshAdminTasksDisplay();
        }
    }

    private void refreshAdminTasksDisplay() {
        // Находим контейнер всех задач
        Component[] components = centerPanel.getComponents();
        JPanel allTasksPanel = null;

        for (Component comp : components) {
            if (comp instanceof JPanel && ((JPanel) comp).getComponentCount() > 0) {
                allTasksPanel = (JPanel) comp;
                break;
            }
        }

        if (allTasksPanel != null) {
            // Перезагружаем панель с задачами
            centerPanel.remove(allTasksPanel);
            JPanel newAllTasksPanel = loadAllTasksPanel();
            centerPanel.add(newAllTasksPanel, "alltasks");
            cardLayout.show(centerPanel, "alltasks");
            centerPanel.revalidate();
            centerPanel.repaint();
        }
    }

    private void showNoTasksMessage(JPanel tasksPanel) {
        JLabel noTasksLabel = new JLabel("Задачи не найдены", SwingConstants.CENTER);
        noTasksLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        noTasksLabel.setForeground(Color.GRAY);
        tasksPanel.add(noTasksLabel, BorderLayout.CENTER);
    }

    private void showErrorPanel(JPanel tasksPanel, String message) {
        JLabel errorLabel = new JLabel(message, SwingConstants.CENTER);
        errorLabel.setFont(new Font("Arial", Font.BOLD, 14));
        errorLabel.setForeground(Color.RED);
        tasksPanel.add(errorLabel, BorderLayout.CENTER);
    }

    private String getStatusDisplayName(String status) {
        if (status == null) return "";
        switch (status) {
            case "НЕ_НАЧАТА": return "Не начата";
            case "В_РАБОТЕ": return "В работе";
            case "ЗАВЕРШЕНА": return "Завершена";
            case "НА_ДОРАБОТКЕ": return "На доработке";
            default: return status;
        }
    }

    private String getImportanceDisplayName(String importance) {
        if (importance == null) return "";
        switch (importance) {
            case "СРОЧНАЯ": return "Срочная";
            case "НАДО_ПОТОРОПИТЬСЯ": return "Средняя";
            case "МОЖЕТ_ПОДОЖДАТЬ": return "Низкая";
            default: return importance;
        }
    }

    private Color getImportanceColor(String importance) {
        if (importance == null) return Color.BLACK;
        switch (importance) {
            case "СРОЧНАЯ": return new Color(231, 76, 60); // Красный
            case "НАДО_ПОТОРОПИТЬСЯ": return new Color(241, 196, 15); // Желтый
            case "МОЖЕТ_ПОДОЖДАТЬ": return new Color(46, 204, 113); // Зеленый
            default: return Color.BLACK;
        }
    }

    private Color getStatusColor(String status) {
        if (status == null) return Color.BLACK;
        switch (status) {
            case "ЗАВЕРШЕНА": return new Color(46, 204, 113); // Зеленый
            case "В_РАБОТЕ": return new Color(241, 196, 15); // Желтый
            case "НЕ_НАЧАТА": return new Color(52, 152, 219); // Синий
            case "НА_ДОРАБОТКЕ": return new Color(231, 76, 60); // Красный
            default: return Color.BLACK;
        }
    }

    private List<Comment> parseCommentsArray(String commentsJson) {
        List<Comment> comments = new ArrayList<>();
        try {
            if (commentsJson == null || commentsJson.trim().isEmpty()) return comments;

            List<String> commentObjects = splitJsonObjects(commentsJson);
            for (String commentObj : commentObjects) {
                Comment comment = parseSingleComment(commentObj);
                if (comment != null) {
                    comments.add(comment);
                }
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Admin - Error parsing comments: " + e.getMessage());
        }
        return comments;
    }

    private Comment parseSingleComment(String commentJson) {
        try {
            String idStr = extractValue(commentJson, "id");
            String description = extractValue(commentJson, "description");

            if (description != null) {
                Comment comment = new Comment();
                if (idStr != null) {
                    comment.setId(Long.parseLong(idStr));
                }
                comment.setDescription(description);
                return comment;
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Admin - Error parsing comment: " + e.getMessage());
        }
        return null;
    }

    private String extractValueImproved(String json, String key) {
        try {
            // Пробуем найти строковое значение в кавычках
            String searchStr = "\"" + key + "\":\"";
            int start = json.indexOf(searchStr);
            if (start != -1) {
                start += searchStr.length();
                int end = findStringEnd(json, start);
                if (end != -1) {
                    return json.substring(start, end);
                }
            }

            // Пробуем найти значение без кавычек (числа, null, boolean)
            searchStr = "\"" + key + "\":";
            start = json.indexOf(searchStr);
            if (start != -1) {
                start += searchStr.length();
                int end = findValueEnd(json, start);
                if (end != -1) {
                    String value = json.substring(start, end).trim();
                    // Убираем кавычки если они есть
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    return value;
                }
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Admin - Error extracting value for key " + key + ": " + e.getMessage());
        }
        return null;
    }

    private int findStringEnd(String json, int start) {
        boolean escaped = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\') {
                escaped = !escaped;
            } else if (c == '"' && !escaped) {
                return i;
            } else {
                escaped = false;
            }
        }
        return -1;
    }

    private int findValueEnd(String json, int start) {
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) {
                return i;
            }
        }
        return json.length();
    }

    private int findMatchingBracket(String json, int startIndex) {
        int bracketCount = 0;
        boolean inString = false;
        char escapeChar = '\\';

        for (int i = startIndex; i < json.length(); i++) {
            char c = json.charAt(i);

            // Обработка строк
            if (c == '"' && (i == 0 || json.charAt(i-1) != escapeChar)) {
                inString = !inString;
                continue;
            }

            if (!inString) {
                if (c == '[') {
                    bracketCount++;
                } else if (c == ']') {
                    bracketCount--;
                    if (bracketCount == 0) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private List<Comment> parseCommentsArrayImproved(String commentsJson) {
        List<Comment> comments = new ArrayList<>();
        try {
            if (commentsJson == null || commentsJson.trim().isEmpty()) return comments;

            List<String> commentObjects = splitJsonObjects(commentsJson);
            for (String commentObj : commentObjects) {
                Comment comment = parseSingleCommentImproved(commentObj);
                if (comment != null) {
                    comments.add(comment);
                }
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Admin - Error parsing comments: " + e.getMessage());
        }
        return comments;
    }

    private Comment parseSingleCommentImproved(String commentJson) {
        try {
            String idStr = extractValueImproved(commentJson, "id");
            String description = extractValueImproved(commentJson, "description");

            if (description != null) {
                Comment comment = new Comment();
                if (idStr != null) {
                    try {
                        comment.setId(Long.parseLong(idStr));
                    } catch (NumberFormatException e) {
                        System.out.println("DEBUG: Admin - Error parsing comment ID: " + idStr);
                    }
                }
                comment.setDescription(description);
                return comment;
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Admin - Error parsing comment: " + e.getMessage());
        }
        return null;
    }

    private List<String> splitJsonObjects(String json) {
        List<String> objects = new ArrayList<>();
        int start = -1;
        int braceCount = 0;
        boolean inString = false;
        char escapeChar = '\\';

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            // Обработка строк
            if (c == '"' && (i == 0 || json.charAt(i-1) != escapeChar)) {
                inString = !inString;
            }

            if (!inString) {
                if (c == '{') {
                    braceCount++;
                    if (braceCount == 1) {
                        start = i;
                    }
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0 && start != -1) {
                        objects.add(json.substring(start, i + 1));
                        start = -1;
                    }
                }
            }
        }
        return objects;
    }

    private String extractValue(String json, String key) {
        // Перенаправляем на улучшенную версию
        return extractValueImproved(json, key);
    }

    private JButton createReworkButton(Task task) {
        JButton button = new JButton("На доработку");
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(231, 76, 60)); // Красный цвет
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Добавляем эффекты при наведении
        Color originalColor = button.getBackground();
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor);
            }
        });

        // Обработчик для отправки задачи на доработку
        button.addActionListener(e -> showReworkDialog(task));

        return button;
    }

    private void showReworkDialog(Task task) {
        JDialog reworkDialog = new JDialog(this, "Отправка задачи на доработку", true);
        reworkDialog.setSize(500, 400);
        reworkDialog.setLocationRelativeTo(this);
        reworkDialog.setLayout(new BorderLayout());
        reworkDialog.setResizable(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Иконка
        JLabel iconLabel = new JLabel("🔄", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setForeground(new Color(241, 196, 15));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Заголовок
        JLabel titleLabel = new JLabel("Отправка задачи на доработку", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Информация о задаче
        JPanel taskInfoPanel = new JPanel();
        taskInfoPanel.setLayout(new BoxLayout(taskInfoPanel, BoxLayout.Y_AXIS));
        taskInfoPanel.setBackground(Color.WHITE);
        taskInfoPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        taskInfoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel taskTitleLabel = new JLabel("Задача: " + task.getTitle());
        taskTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        taskTitleLabel.setForeground(new Color(44, 62, 80));
        taskTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel currentStatusLabel = new JLabel("Текущий статус: " + getStatusDisplayName(task.getStatus()));
        currentStatusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        currentStatusLabel.setForeground(new Color(127, 140, 141));
        currentStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel newStatusLabel = new JLabel("Новый статус: На доработке");
        newStatusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        newStatusLabel.setForeground(getStatusColor("НА_ДОРАБОТКЕ"));
        newStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        taskInfoPanel.add(taskTitleLabel);
        taskInfoPanel.add(Box.createVerticalStrut(8));
        taskInfoPanel.add(currentStatusLabel);
        taskInfoPanel.add(Box.createVerticalStrut(5));
        taskInfoPanel.add(newStatusLabel);

        // Поле для комментария
        JLabel commentLabel = new JLabel("Комментарий к доработке:");
        commentLabel.setFont(new Font("Arial", Font.BOLD, 12));
        commentLabel.setForeground(new Color(44, 62, 80));
        commentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea commentTextArea = new JTextArea(5, 30);
        commentTextArea.setLineWrap(true);
        commentTextArea.setWrapStyleWord(true);
        commentTextArea.setFont(new Font("Arial", Font.PLAIN, 12));
        commentTextArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JScrollPane commentScrollPane = new JScrollPane(commentTextArea);
        commentScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        commentScrollPane.setMaximumSize(new Dimension(400, 120));

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton sendButton = new JButton("Отправить на доработку");
        JButton cancelButton = new JButton("Отмена");

        styleReworkDialogButton(sendButton, new Color(231, 76, 60)); // Красный
        styleReworkDialogButton(cancelButton, new Color(108, 117, 125)); // Серый

        sendButton.addActionListener(e -> {
            String commentText = commentTextArea.getText().trim();
            if (commentText.isEmpty()) {
                showErrorMessage("Пожалуйста, укажите комментарий к доработке");
                return;
            }
            reworkDialog.dispose();
            sendTaskToRework(task, commentText);
        });

        cancelButton.addActionListener(e -> reworkDialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(sendButton);

        // Собираем все компоненты
        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(taskInfoPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(commentLabel);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(commentScrollPane);
        contentPanel.add(Box.createVerticalStrut(25));
        contentPanel.add(buttonPanel);

        reworkDialog.add(contentPanel, BorderLayout.CENTER);
        reworkDialog.getRootPane().setDefaultButton(sendButton);
        reworkDialog.pack();
        reworkDialog.setLocationRelativeTo(this);
        reworkDialog.setVisible(true);
    }

    private void styleReworkDialogButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
    }

    private void sendTaskToRework(Task task, String commentText) {
        // Показываем индикатор загрузки
        JDialog loadingDialog = new JDialog(this, "Отправка на доработку", true);
        loadingDialog.setSize(350, 120);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setLayout(new BorderLayout());
        loadingDialog.setResizable(false);

        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(Color.WHITE);
        loadingPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel loadingLabel = new JLabel("Отправка задачи на доработку...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        loadingLabel.setForeground(new Color(44, 62, 80));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setBackground(Color.WHITE);

        loadingPanel.add(loadingLabel, BorderLayout.CENTER);
        loadingPanel.add(progressBar, BorderLayout.SOUTH);

        loadingDialog.add(loadingPanel, BorderLayout.CENTER);
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        // Запускаем отправку в отдельном потоке
        new Thread(() -> {
            try {
                boolean success = sendReworkRequestToServer(task, commentText);

                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();

                    if (success) {
                        // Обновляем статус задачи локально
                        task.setStatus("НА_ДОРАБОТКЕ");

                        // Показываем сообщение об успехе
                        showSuccessMessage("Задача успешно отправлена на доработку!");

                        // Обновляем интерфейс
                        refreshAllData();
                    } else {
                        showErrorMessage("Ошибка при отправке задачи на доработку");
                    }
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    showErrorMessage("Ошибка при отправке на доработку: " + e.getMessage());
                });
            }
        }).start();

        loadingDialog.setVisible(true);
    }

    private String convertCommentToJson(Comment comment) {
        // Простая реализация преобразования в JSON
        // В реальном проекте лучше использовать библиотеку типа Jackson
        try {
            StringBuilder json = new StringBuilder();
            json.append("{");

            if (comment.getDescription() != null) {
                json.append("\"description\":\"")
                        .append(escapeJsonString(comment.getDescription()))
                        .append("\"");
            }

            json.append("}");
            return json.toString();
        } catch (Exception e) {
            System.out.println("DEBUG: Error converting comment to JSON: " + e.getMessage());
            return "{\"description\":\"\"}";
        }
    }

    private void updateStatsInPanel(JPanel panel, int totalTasks, int notStarted, int completed, int totalUsers) {
        Component[] components = panel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel innerPanel = (JPanel) comp;
                Component[] innerComponents = innerPanel.getComponents();
                for (Component innerComp : innerComponents) {
                    if (innerComp instanceof JPanel) {
                        JPanel cardsContainer = (JPanel) innerComp;
                        if (cardsContainer.getComponentCount() >= 4) {
                            // Обновляем карточки статистики
                            updateStatCard((JPanel) cardsContainer.getComponent(0), String.valueOf(totalTasks));
                            updateStatCard((JPanel) cardsContainer.getComponent(1), String.valueOf(notStarted));
                            updateStatCard((JPanel) cardsContainer.getComponent(2), String.valueOf(completed));
                            updateStatCard((JPanel) cardsContainer.getComponent(3), String.valueOf(totalUsers));
                        }
                    }
                }
            }
        }
    }

    private void updateStatCard(JPanel card, String value) {
        Component[] components = card.getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label.getFont().getSize() == 36) { // Это значение
                    label.setText(value);
                    break;
                }
            }
        }
    }

    private JPanel createCommentPanel(Comment comment) {
        JPanel commentPanel = new JPanel(new BorderLayout());
        commentPanel.setBackground(new Color(248, 249, 250));
        commentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JTextArea commentText = new JTextArea(comment.getDescription() != null ? comment.getDescription() : "");
        commentText.setEditable(false);
        commentText.setLineWrap(true);
        commentText.setWrapStyleWord(true);
        commentText.setBackground(new Color(248, 249, 250));
        commentText.setFont(new Font("Arial", Font.PLAIN, 12));

        commentPanel.add(commentText, BorderLayout.CENTER);
        return commentPanel;
    }

    private void loadAdminStatistics() {
        new Thread(() -> {
            try {
                List<User> users = getAllUsersWithTasks();
                SwingUtilities.invokeLater(() -> {
                    updateAdminStatsPanel(users);
                });
            } catch (Exception e) {
                System.out.println("DEBUG: Error loading admin statistics: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void updateStatsCards(int totalTasks, int notStarted, int completed, int totalUsers) {
        // Находим панель welcome и обновляем карточки напрямую
        Component[] components = centerPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                updateStatsCardsInPanel(panel, totalTasks, notStarted, completed, totalUsers);
            }
        }
    }

    private void updateStatsCardsInPanel(JPanel panel, int totalTasks, int notStarted, int completed, int totalUsers) {
        // Рекурсивно ищем контейнер с карточками статистики
        Component[] components = panel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel innerPanel = (JPanel) comp;

                // Проверяем, является ли это контейнером карточек статистики
                if (isStatsCardsContainer(innerPanel)) {
                    updateStatsCardsValues(innerPanel, totalTasks, notStarted, completed, totalUsers);
                    return;
                }

                // Рекурсивный поиск во вложенных панелях
                updateStatsCardsInPanel(innerPanel, totalTasks, notStarted, completed, totalUsers);
            }
        }
    }

    private boolean isStatsCardsContainer(JPanel panel) {
        // Проверяем, содержит ли панель 4 карточки статистики
        if (panel.getComponentCount() == 4) {
            Component firstComp = panel.getComponent(0);
            return firstComp instanceof JPanel &&
                    ((JPanel) firstComp).getComponentCount() >= 3; // Карточка должна содержать несколько компонентов
        }
        return false;
    }

    private void updateStatsCardsValues(JPanel cardsContainer, int totalTasks, int notStarted, int completed, int totalUsers) {
        if (cardsContainer.getComponentCount() >= 4) {
            updateSingleStatCard((JPanel) cardsContainer.getComponent(0), String.valueOf(totalTasks));
            updateSingleStatCard((JPanel) cardsContainer.getComponent(1), String.valueOf(notStarted));
            updateSingleStatCard((JPanel) cardsContainer.getComponent(2), String.valueOf(completed));
            updateSingleStatCard((JPanel) cardsContainer.getComponent(3), String.valueOf(totalUsers));

            System.out.println("DEBUG: Stats cards updated successfully");
        }
    }

    // Упрощенный парсинг задач
    private List<Task> parseTasksArraySimple(String tasksJson) {
        List<Task> tasks = new ArrayList<>();
        try {
            if (tasksJson == null || tasksJson.trim().isEmpty()) {
                return tasks;
            }

            // Ищем отдельные задачи в JSON
            java.util.regex.Pattern taskPattern = java.util.regex.Pattern.compile(
                    "\\{\"title\":\"([^\"]+)\".*?\"status\":\"([^\"]*)\".*?\"importance\":\"([^\"]*)\".*?\\}",
                    java.util.regex.Pattern.DOTALL
            );

            java.util.regex.Matcher taskMatcher = taskPattern.matcher(tasksJson);

            while (taskMatcher.find()) {
                Task task = new Task();
                task.setTitle(taskMatcher.group(1));
                task.setStatus(taskMatcher.group(2));
                task.setImportance(taskMatcher.group(3));

                tasks.add(task);
            }

        } catch (Exception e) {
            System.out.println("DEBUG: Error parsing tasks with regex: " + e.getMessage());
        }
        return tasks;
    }

    private void debugPanelStructure(Container container, int depth) {
        String indent = "  ".repeat(depth);
        System.out.println(indent + container.getClass().getSimpleName() +
                " [Components: " + container.getComponentCount() + "]");

        for (Component comp : container.getComponents()) {
            if (comp instanceof Container) {
                debugPanelStructure((Container) comp, depth + 1);
            } else {
                System.out.println(indent + "  └─ " + comp.getClass().getSimpleName() +
                        (comp instanceof JLabel ? ": " + ((JLabel) comp).getText() : ""));
            }
        }
    }

    private List<User> getAllUsersWithTasks() {
        try {
            String url = "http://localhost:8080/allusers";
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("DEBUG: Admin getAllUsersWithTasks Status code: " + response.statusCode());
            System.out.println("DEBUG: Admin getAllUsersWithTasks Response body: " + response.body());

            if (response.statusCode() == 200) {
                List<User> users = parseUsersFromJson(response.body());
                System.out.println("DEBUG: Admin parsed " + (users != null ? users.size() : 0) + " users");
                if (users != null) {
                    for (User user : users) {
                        System.out.println("DEBUG: User: " + user.getUsername() +
                                " tasks: " + (user.getTasks() != null ? user.getTasks().size() : 0));
                    }
                }
                return users;
            } else {
                throw new RuntimeException("HTTP error: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Exception in admin getAllUsersWithTasks: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Ошибка при получении всех пользователей: " + e.getMessage());
        }
    }

    private List<User> parseUsersFromJson(String json) {
        try {
            List<User> users = new ArrayList<>();
            if (json == null || json.trim().isEmpty()) {
                System.out.println("DEBUG: Admin - Empty JSON response");
                return users;
            }

            System.out.println("DEBUG: Admin - Raw users JSON: " + json);

            // Если ответ - массив пользователей
            if (json.trim().startsWith("[")) {
                // Убираем внешние квадратные скобки
                String content = json.trim().substring(1, json.length() - 1).trim();

                if (!content.isEmpty()) {
                    // Используем улучшенный парсинг с учетом вложенных объектов
                    List<String> userObjects = splitJsonObjects(content);
                    System.out.println("DEBUG: Admin - Found " + userObjects.size() + " user objects");

                    for (String userStr : userObjects) {
                        User user = parseSingleUserFromJson(userStr);
                        if (user != null && user.getUsername() != null) {
                            users.add(user);
                            System.out.println("DEBUG: Admin - Added user: " + user.getUsername() + " with " +
                                    (user.getTasks() != null ? user.getTasks().size() : 0) + " tasks");
                        }
                    }
                }
            } else {
                // Если ответ - один пользователь
                User user = parseSingleUserFromJson(json);
                if (user != null && user.getUsername() != null) {
                    users.add(user);
                }
            }

            return users;
        } catch (Exception e) {
            System.out.println("DEBUG: Admin - Error parsing users: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private User parseSingleUserFromJson(String userJson) {
        try {
            System.out.println("DEBUG: Admin - Parsing single user: " + userJson);

            User user = new User();
            String username = extractValueImproved(userJson, "username");
            if (username == null) return null;

            user.setUsername(username);

            // Парсим задачи пользователя с улучшенным методом
            if (userJson.contains("\"tasks\":")) {
                int tasksStart = userJson.indexOf("\"tasks\":[") + 8;
                int tasksEnd = findMatchingBracket(userJson, tasksStart);
                if (tasksEnd > tasksStart) {
                    String tasksArray = userJson.substring(tasksStart + 1, tasksEnd).trim();
                    System.out.println("DEBUG: Admin - Tasks array for user " + username + ": " + tasksArray);
                    List<Task> tasks = parseTasksArray(tasksArray);
                    user.setTasks(tasks);
                }
            }
            return user;
        } catch (Exception e) {
            System.out.println("DEBUG: Admin - Error parsing single user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private List<Task> parseTasksArray(String tasksJson) {
        List<Task> tasks = new ArrayList<>();
        try {
            if (tasksJson == null || tasksJson.trim().isEmpty()) {
                System.out.println("DEBUG: Admin - Empty tasks array");
                return tasks;
            }

            System.out.println("DEBUG: Admin - Parsing tasks array: " + tasksJson);

            // Убираем пробелы и переносы строк для упрощения парсинга
            String cleanJson = tasksJson.replace("\n", "").replace("\r", "").trim();

            // Если массив пустой
            if (cleanJson.isEmpty()) {
                return tasks;
            }

            // Используем улучшенный парсинг из UserApplicationFrame
            List<String> taskObjects = splitJsonObjects(cleanJson);
            System.out.println("DEBUG: Admin - Found " + taskObjects.size() + " task objects");

            for (String taskStr : taskObjects) {
                Task task = parseSingleTaskImproved(taskStr);
                if (task != null && task.getTitle() != null) {
                    tasks.add(task);
                    System.out.println("DEBUG: Admin - Added task: " + task.getTitle());
                }
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Admin - Error parsing tasks array: " + e.getMessage());
            e.printStackTrace();
        }
        return tasks;
    }

    private void updateAdminStatsPanel(List<User> users) {
        if (users == null) {
            System.out.println("DEBUG: Users list is null");
            return;
        }

        int totalTasks = 0;
        int notStarted = 0;
        int inProgress = 0;
        int completed = 0;
        int totalUsers = users.size();

        for (User user : users) {
            if (user.getTasks() != null) {
                totalTasks += user.getTasks().size();
                for (Task task : user.getTasks()) {
                    if (task.getStatus() != null) {
                        switch (task.getStatus()) {
                            case "НЕ_НАЧАТА": notStarted++; break;
                            case "В_РАБОТЕ": inProgress++; break;
                            case "ЗАВЕРШЕНА": completed++; break;
                        }
                    }
                }
            }
        }

        System.out.println("DEBUG: Stats - Total: " + totalTasks +
                ", NotStarted: " + notStarted +
                ", InProgress: " + inProgress +
                ", Completed: " + completed +
                ", Users: " + totalUsers);

        // Обновляем статистику без рекурсивного поиска
        updateStatsDirectly(totalTasks, notStarted, completed, totalUsers);
    }

    private void updateStatsDirectly(int totalTasks, int notStarted, int completed, int totalUsers) {
        // Ищем welcome panel напрямую
        for (Component comp : centerPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                // Проверяем, содержит ли панель статистику
                if (containsStatsCards(panel)) {
                    updateStatsInWelcomePanel(panel, totalTasks, notStarted, completed, totalUsers);
                    break;
                }
            }
        }
    }

    private boolean containsStatsCards(JPanel panel) {
        // Проверяем, содержит ли панель элементы статистики
        return findComponentRecursive(panel, "Добро пожаловать в панель администратора!") != null;
    }

    private Component findComponentRecursive(Container container, String text) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (text.equals(label.getText())) {
                    return comp;
                }
            }
            if (comp instanceof Container) {
                Component found = findComponentRecursive((Container) comp, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void updateStatsInWelcomePanel(JPanel welcomePanel, int totalTasks, int notStarted, int completed, int totalUsers) {
        // Ищем контейнер с карточками статистики
        JPanel statsContainer = findStatsContainer(welcomePanel);
        if (statsContainer != null && statsContainer.getComponentCount() >= 4) {
            updateSingleStatCard((JPanel) statsContainer.getComponent(0), String.valueOf(totalTasks));
            updateSingleStatCard((JPanel) statsContainer.getComponent(1), String.valueOf(notStarted));
            updateSingleStatCard((JPanel) statsContainer.getComponent(2), String.valueOf(completed));
            updateSingleStatCard((JPanel) statsContainer.getComponent(3), String.valueOf(totalUsers));
            System.out.println("DEBUG: Stats updated successfully");
        } else {
            System.out.println("DEBUG: Stats container not found or has wrong number of cards");
        }
    }

    private JPanel findStatsContainer(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel innerPanel = (JPanel) comp;
                // Проверяем, содержит ли панель 4 карточки (статистика)
                if (innerPanel.getComponentCount() == 4) {
                    boolean allAreCards = true;
                    for (Component card : innerPanel.getComponents()) {
                        if (!(card instanceof JPanel)) {
                            allAreCards = false;
                            break;
                        }
                    }
                    if (allAreCards) return innerPanel;
                }
                // Рекурсивный поиск
                JPanel found = findStatsContainer(innerPanel);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void updateSingleStatCard(JPanel card, String value) {
        for (Component comp : card.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                // Ищем метку с большим шрифтом (значение статистики)
                if (label.getFont().getSize() >= 24) {
                    label.setText(value);
                    card.revalidate();
                    card.repaint();
                    break;
                }
            }
        }
    }


    private void showTaskComments(Task task) {
        JDialog commentsDialog = new JDialog(this, "Комментарии к задаче", true);
        commentsDialog.setSize(600, 500);
        commentsDialog.setLocationRelativeTo(this);
        commentsDialog.setLayout(new BorderLayout());
        commentsDialog.setResizable(true);

        // Основная панель
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Заголовок
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel iconLabel = new JLabel("💬", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setForeground(new Color(52, 152, 219));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Комментарии к задаче", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel taskTitleLabel = new JLabel("\"" + task.getTitle() + "\"", SwingConstants.CENTER);
        taskTitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        taskTitleLabel.setForeground(new Color(127, 140, 141));
        taskTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(iconLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(taskTitleLabel);

        // Панель с комментариями
        JPanel commentsContentPanel = new JPanel(new BorderLayout());
        commentsContentPanel.setBackground(Color.WHITE);
        commentsContentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JPanel commentsListPanel = new JPanel();
        commentsListPanel.setLayout(new BoxLayout(commentsListPanel, BoxLayout.Y_AXIS));
        commentsListPanel.setBackground(Color.WHITE);

        if (task.getComments() != null && !task.getComments().isEmpty()) {
            for (int i = 0; i < task.getComments().size(); i++) {
                Comment comment = task.getComments().get(i);
                JPanel commentPanel = createModernCommentPanel(comment, i + 1);
                commentsListPanel.add(commentPanel);
                commentsListPanel.add(Box.createVerticalStrut(10));
            }
        } else {
            JPanel noCommentsPanel = new JPanel(new BorderLayout());
            noCommentsPanel.setBackground(new Color(248, 249, 250));
            noCommentsPanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));

            JLabel noCommentsLabel = new JLabel("Комментарии отсутствуют", SwingConstants.CENTER);
            noCommentsLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            noCommentsLabel.setForeground(new Color(158, 158, 158));
            noCommentsLabel.setIcon(new ImageIcon(getClass().getResource("/icons/no-comments.png"))); // Можно добавить иконку

            noCommentsPanel.add(noCommentsLabel, BorderLayout.CENTER);
            commentsListPanel.add(noCommentsPanel);
        }

        JScrollPane scrollPane = new JScrollPane(commentsListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        commentsContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Панель статистики
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        int commentCount = task.getComments() != null ? task.getComments().size() : 0;
        JLabel statsLabel = new JLabel("Всего комментариев: " + commentCount);
        statsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statsLabel.setForeground(new Color(108, 117, 125));
        statsLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        statsPanel.add(statsLabel);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton closeButton = new JButton("Закрыть");
        styleDialogButton(closeButton, new Color(108, 117, 125));

        closeButton.addActionListener(e -> commentsDialog.dispose());

        buttonPanel.add(closeButton);

        // Собираем все компоненты
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(commentsContentPanel, BorderLayout.CENTER);
        mainPanel.add(statsPanel, BorderLayout.SOUTH);

        commentsDialog.add(mainPanel, BorderLayout.CENTER);
        commentsDialog.add(buttonPanel, BorderLayout.SOUTH);

        commentsDialog.getRootPane().setDefaultButton(closeButton);
        commentsDialog.pack();
        commentsDialog.setLocationRelativeTo(this);
        commentsDialog.setVisible(true);
    }

    private JPanel createModernCommentPanel(Comment comment, int number) {
        JPanel commentPanel = new JPanel(new BorderLayout());
        commentPanel.setBackground(new Color(248, 249, 250));
        commentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        commentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Заголовок комментария
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(248, 249, 250));

        JLabel numberLabel = new JLabel("Комментарий #" + number);
        numberLabel.setFont(new Font("Arial", Font.BOLD, 12));
        numberLabel.setForeground(new Color(52, 152, 219));

        // Если есть ID комментария, показываем его
        if (comment.getId() != null) {
            JLabel idLabel = new JLabel("ID: " + comment.getId());
            idLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            idLabel.setForeground(new Color(158, 158, 158));
            headerPanel.add(idLabel, BorderLayout.EAST);
        }

        headerPanel.add(numberLabel, BorderLayout.WEST);

        // Текст комментария
        JTextArea commentText = new JTextArea(comment.getDescription() != null ? comment.getDescription() : "");
        commentText.setEditable(false);
        commentText.setLineWrap(true);
        commentText.setWrapStyleWord(true);
        commentText.setBackground(new Color(248, 249, 250));
        commentText.setFont(new Font("Arial", Font.PLAIN, 13));
        commentText.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        commentText.setForeground(new Color(44, 62, 80));

        // Добавляем скролл для длинных комментариев
        JScrollPane textScroll = new JScrollPane(commentText);
        textScroll.setBorder(BorderFactory.createEmptyBorder());
        textScroll.setBackground(new Color(248, 249, 250));
        textScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        textScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        commentPanel.add(headerPanel, BorderLayout.NORTH);
        commentPanel.add(textScroll, BorderLayout.CENTER);

        return commentPanel;
    }

    private void showTaskCommentsCompact(Task task) {
        JDialog commentsDialog = new JDialog(this, "Комментарии к задаче", true);
        commentsDialog.setSize(500, 400);
        commentsDialog.setLocationRelativeTo(this);
        commentsDialog.setLayout(new BorderLayout());
        commentsDialog.setResizable(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Иконка и заголовок
        JLabel iconLabel = new JLabel("💬", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setForeground(new Color(52, 152, 219));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Комментарии", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel taskLabel = new JLabel("Задача: " + task.getTitle(), SwingConstants.CENTER);
        taskLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        taskLabel.setForeground(new Color(127, 140, 141));
        taskLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Панель комментариев
        JPanel commentsPanel = new JPanel();
        commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.Y_AXIS));
        commentsPanel.setBackground(Color.WHITE);
        commentsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        commentsPanel.setMaximumSize(new Dimension(450, 250));

        if (task.getComments() != null && !task.getComments().isEmpty()) {
            for (Comment comment : task.getComments()) {
                JPanel commentItem = createCompactCommentItem(comment);
                commentsPanel.add(commentItem);
                commentsPanel.add(Box.createVerticalStrut(8));
            }
        } else {
            JLabel noCommentsLabel = new JLabel("Нет комментариев", SwingConstants.CENTER);
            noCommentsLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            noCommentsLabel.setForeground(Color.GRAY);
            noCommentsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            commentsPanel.add(noCommentsLabel);
        }

        JScrollPane scrollPane = new JScrollPane(commentsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Кнопка закрытия
        JButton closeButton = new JButton("Закрыть");
        styleDialogButton(closeButton, new Color(52, 152, 219));
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.addActionListener(e -> commentsDialog.dispose());

        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(taskLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(scrollPane);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(closeButton);

        commentsDialog.add(contentPanel, BorderLayout.CENTER);
        commentsDialog.getRootPane().setDefaultButton(closeButton);
        commentsDialog.pack();
        commentsDialog.setLocationRelativeTo(this);
        commentsDialog.setVisible(true);
    }

    private JPanel createCompactCommentItem(Comment comment) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JTextArea textArea = new JTextArea(comment.getDescription() != null ? comment.getDescription() : "");
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(new Color(245, 245, 245));
        textArea.setFont(new Font("Arial", Font.PLAIN, 12));
        textArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Ограничиваем высоту одного комментария
        textArea.setRows(3);

        panel.add(textArea, BorderLayout.CENTER);
        return panel;
    }

    // Загрузка пользователей
    private void loadUsers(DefaultListModel<String> usersListModel, DefaultListModel<String> tasksListModel, JList<String> tasksList) {
        showLoadingDialog("Загрузка пользователей...");

        new Thread(() -> {
            try {
                List<User> users = getAllUsersWithTasks();
                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();
                    usersListModel.clear();
                    tasksListModel.clear();

                    if (users != null && !users.isEmpty()) {
                        for (User user : users) {
                            usersListModel.addElement(user.getUsername());
                        }
                        showSuccessMessage("Загружено " + users.size() + " пользователей");
                    } else {
                        usersListModel.addElement("Пользователи не найдены");
                        showInfoMessage("Пользователи не найдены");
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();
                    usersListModel.clear();
                    usersListModel.addElement("Ошибка загрузки: " + e.getMessage());
                    showErrorMessage("Ошибка загрузки пользователей: " + e.getMessage());
                });
            }
        }).start();
    }

    // Загрузка задач пользователя
    private void loadUserTasks(String username, DefaultListModel<String> tasksListModel) {
        showLoadingDialog("Загрузка задач пользователя...");

        new Thread(() -> {
            try {
                List<User> users = getAllUsersWithTasks();
                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();
                    tasksListModel.clear();

                    if (users != null) {
                        for (User user : users) {
                            if (user.getUsername().equals(username) && user.getTasks() != null) {
                                for (Task task : user.getTasks()) {
                                    String taskInfo = String.format("%s | %s | %s",
                                            task.getTitle(),
                                            getStatusDisplayName(task.getStatus()),
                                            task.getDeadline() != null ? task.getDeadline().split("T")[0] : "нет дедлайна"
                                    );
                                    tasksListModel.addElement(taskInfo);
                                }
                                break;
                            }
                        }
                    }

                    if (tasksListModel.isEmpty()) {
                        tasksListModel.addElement("Задачи не найдены");
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();
                    tasksListModel.clear();
                    tasksListModel.addElement("Ошибка загрузки: " + e.getMessage());
                    showErrorMessage("Ошибка загрузки задач: " + e.getMessage());
                });
            }
        }).start();
    }

    // Удаление пользователя
    private void deleteSelectedUser() {
        JList<String> usersList = findUsersList();
        if (usersList == null) return;

        String selectedUser = usersList.getSelectedValue();
        if (selectedUser == null || selectedUser.equals("Пользователи не найдены") || selectedUser.startsWith("Ошибка")) {
            showErrorMessage("Выберите пользователя для удаления");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите удалить пользователя '" + selectedUser + "'?\nЭто действие нельзя отменить.",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            performDeleteUser(selectedUser);
        }
    }

    private void performDeleteUser(String username) {
        showLoadingDialog("Удаление пользователя " + username + "...");

        new Thread(() -> {
            try {
                String url = "http://localhost:8080/deleteuser/" +
                        java.net.URLEncoder.encode(username, "UTF-8");

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
                        .header("Authorization", "Bearer " + authToken)
                        .DELETE()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();

                    if (response.statusCode() == 200) {
                        showSuccessMessage("Пользователь '" + username + "' успешно удален");
                        refreshUserManagement();
                    } else {
                        showErrorMessage("Ошибка при удалении пользователя: " + response.body());
                    }
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();
                    showErrorMessage("Ошибка при удалении пользователя: " + e.getMessage());
                });
            }
        }).start();
    }

    // Удаление задачи
    private void deleteSelectedTask() {
        JList<String> usersList = findUsersList();
        JList<String> tasksList = findTasksList();

        if (usersList == null || tasksList == null) return;

        String selectedUser = usersList.getSelectedValue();
        String selectedTask = tasksList.getSelectedValue();

        if (selectedUser == null || selectedUser.equals("Пользователи не найдены") || selectedUser.startsWith("Ошибка")) {
            showErrorMessage("Выберите пользователя");
            return;
        }

        if (selectedTask == null || selectedTask.equals("Задачи не найдены") || selectedTask.startsWith("Ошибка")) {
            showErrorMessage("Выберите задачу для удаления");
            return;
        }

        // Извлекаем название задачи из строки
        String taskTitle = selectedTask.split(" \\| ")[0];

        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите удалить задачу '" + taskTitle + "' у пользователя '" + selectedUser + "'?",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            performDeleteTask(selectedUser, taskTitle);
        }
    }

    private void performDeleteTask(String username, String taskTitle) {
        showLoadingDialog("Удаление задачи '" + taskTitle + "'...");

        new Thread(() -> {
            try {
                String url = "http://localhost:8080/deletetask/" +
                        java.net.URLEncoder.encode(username, "UTF-8") + "/" +
                        java.net.URLEncoder.encode(taskTitle, "UTF-8");

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
                        .header("Authorization", "Bearer " + authToken)
                        .DELETE()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();

                    if (response.statusCode() == 200) {
                        showSuccessMessage("Задача '" + taskTitle + "' успешно удалена");
                        refreshUserManagement();
                    } else {
                        showErrorMessage("Ошибка при удалении задачи: " + response.body());
                    }
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();
                    showErrorMessage("Ошибка при удалении задачи: " + e.getMessage());
                });
            }
        }).start();
    }

    // Переназначение задачи
    private void reassignSelectedTask() {
        JList<String> usersList = findUsersList();
        JList<String> tasksList = findTasksList();

        if (usersList == null || tasksList == null) return;

        String selectedUser = usersList.getSelectedValue();
        String selectedTask = tasksList.getSelectedValue();

        if (selectedUser == null || selectedTask == null ||
                selectedUser.equals("Пользователи не найдены") || selectedTask.equals("Задачи не найдены")) {
            showErrorMessage("Выберите пользователя и задачу");
            return;
        }

        // Извлекаем название задачи
        String taskTitle = selectedTask.split(" \\| ")[0];

        // Диалог выбора нового пользователя
        showReassignDialog(selectedUser, taskTitle);
    }

    private void showReassignDialog(String currentUser, String taskTitle) {
        JDialog reassignDialog = new JDialog(this, "Переназначение задачи", true);
        reassignDialog.setSize(400, 300);
        reassignDialog.setLocationRelativeTo(this);
        reassignDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Переназначение задачи", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel taskInfoLabel = new JLabel("Задача: " + taskTitle, SwingConstants.CENTER);
        taskInfoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        taskInfoLabel.setForeground(Color.GRAY);
        taskInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel currentUserLabel = new JLabel("Текущий пользователь: " + currentUser, SwingConstants.CENTER);
        currentUserLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        currentUserLabel.setForeground(Color.GRAY);
        currentUserLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel newUserLabel = new JLabel("Новый пользователь:");
        newUserLabel.setFont(new Font("Arial", Font.BOLD, 12));
        newUserLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> usersComboBox = new JComboBox<>();
        usersComboBox.setPreferredSize(new Dimension(300, 30));
        usersComboBox.setMaximumSize(new Dimension(300, 30));

        JButton reassignButton = new JButton("Переназначить");
        JButton cancelButton = new JButton("Отмена");

        styleManagementButton(reassignButton, new Color(52, 152, 219));
        styleManagementButton(cancelButton, new Color(108, 117, 125));

        reassignButton.addActionListener(e -> {
            String newUser = (String) usersComboBox.getSelectedItem();
            if (newUser != null && !newUser.equals(currentUser)) {
                reassignDialog.dispose();
                performReassignTask(currentUser, taskTitle, newUser);
            } else {
                showErrorMessage("Выберите другого пользователя");
            }
        });

        cancelButton.addActionListener(e -> reassignDialog.dispose());

        // Загрузка пользователей для выбора
        loadUsersForReassign(usersComboBox, currentUser);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(cancelButton);
        buttonPanel.add(reassignButton);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(taskInfoLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(currentUserLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(newUserLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(usersComboBox);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(buttonPanel);

        reassignDialog.add(contentPanel, BorderLayout.CENTER);
        reassignDialog.setVisible(true);
    }

    private void performReassignTask(String currentUser, String taskTitle, String newUser) {
        showLoadingDialog("Переназначение задачи '" + taskTitle + "' пользователю " + newUser + "...");

        new Thread(() -> {
            try {
                // Сначала получаем задачу
                Task task = findTaskByUsernameAndTitle(currentUser, taskTitle);
                if (task == null) {
                    throw new RuntimeException("Задача не найдена");
                }

                // Создаем задачу у нового пользователя
                String assignUrl = "http://localhost:8080/assigntask?username=" +
                        java.net.URLEncoder.encode(newUser, "UTF-8");

                String taskJson = convertTaskToJson(task);

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();

                HttpRequest assignRequest = HttpRequest.newBuilder()
                        .uri(java.net.URI.create(assignUrl))
                        .header("Authorization", "Bearer " + authToken)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                        .build();

                HttpResponse<String> assignResponse = client.send(assignRequest, HttpResponse.BodyHandlers.ofString());

                if (assignResponse.statusCode() == 200) {
                    // Удаляем задачу у старого пользователя
                    String deleteUrl = "http://localhost:8080/deletetask/" +
                            java.net.URLEncoder.encode(currentUser, "UTF-8") + "/" +
                            java.net.URLEncoder.encode(taskTitle, "UTF-8");

                    HttpRequest deleteRequest = HttpRequest.newBuilder()
                            .uri(java.net.URI.create(deleteUrl))
                            .header("Authorization", "Bearer " + authToken)
                            .DELETE()
                            .build();

                    HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());

                    SwingUtilities.invokeLater(() -> {
                        hideLoadingDialog();

                        if (deleteResponse.statusCode() == 200) {
                            showSuccessMessage("Задача '" + taskTitle + "' успешно переназначена от " + currentUser + " к " + newUser);
                            refreshUserManagement();
                        } else {
                            showErrorMessage("Задача назначена новому пользователю, но не удалена у старого: " + deleteResponse.body());
                        }
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        hideLoadingDialog();
                        showErrorMessage("Ошибка при назначении задачи новому пользователю: " + assignResponse.body());
                    });
                }

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();
                    showErrorMessage("Ошибка при переназначении задачи: " + e.getMessage());
                });
            }
        }).start();
    }

    // Редактирование задачи
    private void editSelectedTask() {
        JList<String> usersList = findUsersList();
        JList<String> tasksList = findTasksList();

        if (usersList == null || tasksList == null) return;

        String selectedUser = usersList.getSelectedValue();
        String selectedTask = tasksList.getSelectedValue();

        if (selectedUser == null || selectedTask == null ||
                selectedUser.equals("Пользователи не найдены") || selectedTask.equals("Задачи не найдены")) {
            showErrorMessage("Выберите пользователя и задачу");
            return;
        }

        // Находим полную информацию о задаче
        String taskTitle = selectedTask.split(" \\| ")[0];
        Task task = findTaskByUsernameAndTitle(selectedUser, taskTitle);

        if (task != null) {
            showEditTaskDialog(task, selectedUser);
        } else {
            showErrorMessage("Не удалось найти задачу для редактирования");
        }
    }

    private void showEditTaskDialog(Task task, String username) {
        JDialog editDialog = new JDialog(this, "Редактирование задачи", true);
        editDialog.setSize(500, 600);
        editDialog.setLocationRelativeTo(this);
        editDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Редактирование задачи", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLabel = new JLabel("Пользователь: " + username, SwingConstants.CENTER);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userLabel.setForeground(Color.GRAY);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Поля для редактирования
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setBackground(Color.WHITE);
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        fieldsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Название задачи
        JLabel titleFieldLabel = new JLabel("Название задачи:");
        titleFieldLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleFieldLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField titleField = new JTextField(task.getTitle());
        titleField.setMaximumSize(new Dimension(400, 35));
        titleField.setFont(new Font("Arial", Font.PLAIN, 12));

        // Описание
        JLabel descLabel = new JLabel("Описание:");
        descLabel.setFont(new Font("Arial", Font.BOLD, 12));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea descArea = new JTextArea(task.getDescription(), 4, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(new Font("Arial", Font.PLAIN, 12));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setMaximumSize(new Dimension(400, 100));

        // Статус
        JLabel statusLabel = new JLabel("Статус:");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] statusOptions = {"НЕ_НАЧАТА", "В_РАБОТЕ", "ЗАВЕРШЕНА", "НА_ДОРАБОТКЕ"};
        JComboBox<String> statusCombo = new JComboBox<>(statusOptions);
        statusCombo.setSelectedItem(task.getStatus());
        statusCombo.setMaximumSize(new Dimension(400, 35));

        // Приоритет
        JLabel priorityLabel = new JLabel("Приоритет:");
        priorityLabel.setFont(new Font("Arial", Font.BOLD, 12));
        priorityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] priorityOptions = {"СРОЧНАЯ", "НАДО_ПОТОРОПИТЬСЯ", "МОЖЕТ_ПОДОЖДАТЬ"};
        JComboBox<String> priorityCombo = new JComboBox<>(priorityOptions);
        priorityCombo.setSelectedItem(task.getImportance());
        priorityCombo.setMaximumSize(new Dimension(400, 35));

        // Дедлайн
        JLabel deadlineLabel = new JLabel("Дедлайн (гггг-мм-дд):");
        deadlineLabel.setFont(new Font("Arial", Font.BOLD, 12));
        deadlineLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String deadline = task.getDeadline() != null ? task.getDeadline().split("T")[0] : "";
        JTextField deadlineField = new JTextField(deadline);
        deadlineField.setMaximumSize(new Dimension(400, 35));
        deadlineField.setFont(new Font("Arial", Font.PLAIN, 12));

        // Добавляем поля
        fieldsPanel.add(titleFieldLabel);
        fieldsPanel.add(Box.createVerticalStrut(5));
        fieldsPanel.add(titleField);
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(descLabel);
        fieldsPanel.add(Box.createVerticalStrut(5));
        fieldsPanel.add(descScroll);
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(statusLabel);
        fieldsPanel.add(Box.createVerticalStrut(5));
        fieldsPanel.add(statusCombo);
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(priorityLabel);
        fieldsPanel.add(Box.createVerticalStrut(5));
        fieldsPanel.add(priorityCombo);
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(deadlineLabel);
        fieldsPanel.add(Box.createVerticalStrut(5));
        fieldsPanel.add(deadlineField);

        // Кнопки
        JButton saveButton = new JButton("Сохранить");
        JButton cancelButton = new JButton("Отмена");

        styleManagementButton(saveButton, new Color(46, 204, 113));
        styleManagementButton(cancelButton, new Color(108, 117, 125));

        saveButton.addActionListener(e -> {
            // Создаем обновленную задачу
            Task updatedTask = new Task();
            updatedTask.setTitle(titleField.getText());
            updatedTask.setDescription(descArea.getText());
            updatedTask.setStatus((String) statusCombo.getSelectedItem());
            updatedTask.setImportance((String) priorityCombo.getSelectedItem());
            updatedTask.setDeadline(deadlineField.getText() + "T00:00:00");

            performUpdateTask(updatedTask);
            editDialog.dispose();
        });

        cancelButton.addActionListener(e -> editDialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(userLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(fieldsPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(buttonPanel);

        editDialog.add(contentPanel, BorderLayout.CENTER);
        editDialog.setVisible(true);
    }

    private void performUpdateTask(Task task) {
        showLoadingDialog("Сохранение изменений задачи...");

        new Thread(() -> {
            try {
                String url = "http://localhost:8080/updatetask";

                String taskJson = convertTaskToJson(task);

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
                        .header("Authorization", "Bearer " + authToken)
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(taskJson))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();

                    if (response.statusCode() == 200) {
                        showSuccessMessage("Задача '" + task.getTitle() + "' успешно обновлена");
                        refreshUserManagement();
                    } else {
                        showErrorMessage("Ошибка при обновлении задачи: " + response.body());
                    }
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();
                    showErrorMessage("Ошибка при обновлении задачи: " + e.getMessage());
                });
            }
        }).start();
    }

    // Вспомогательные методы
    private JList<String> findUsersList() {
        return findComponentRecursive(JList.class, this);
    }

    private JList<String> findTasksList() {
        return findComponentRecursive(JList.class, this);
    }

    private void refreshUserManagement() {
        JList<String> usersList = findUsersList();
        JList<String> tasksList = findTasksList();

        if (usersList != null && tasksList != null) {
            DefaultListModel<String> usersModel = (DefaultListModel<String>) usersList.getModel();
            DefaultListModel<String> tasksModel = (DefaultListModel<String>) tasksList.getModel();
            loadUsers(usersModel, tasksModel, tasksList);
        }
    }

    // Методы для уведомлений
    private JDialog loadingDialog;


    private void performDeleteUser(String username, JDialog parentDialog) {
        showLoadingDialog("Удаление пользователя " + username + "...");

        new Thread(() -> {
            try {
                String url = "http://localhost:8080/deleteuser/" +
                        java.net.URLEncoder.encode(username, "UTF-8");

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
                        .header("Authorization", "Bearer " + authToken)
                        .DELETE()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();

                    if (response.statusCode() == 200) {
                        showSuccessMessage("Пользователь '" + username + "' успешно удален");
                        // Автоматическое обновление вместо кнопки
                        refreshAllData(); // ← ДОБАВИТЬ ЗДЕСЬ
                        parentDialog.dispose(); // Закрываем диалог
                    } else {
                        showErrorMessage("Ошибка при удалении пользователя: " + response.body());
                    }
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();
                    showErrorMessage("Ошибка при удалении пользователя: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showReassignDialog(String currentUser, String taskTitle, JDialog parentDialog) {
        JDialog reassignDialog = new JDialog(parentDialog, "Переназначение задачи", true);
        reassignDialog.setSize(400, 300);
        reassignDialog.setLocationRelativeTo(parentDialog);
        reassignDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Переназначение задачи", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel taskInfoLabel = new JLabel("Задача: " + taskTitle, SwingConstants.CENTER);
        taskInfoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        taskInfoLabel.setForeground(Color.GRAY);
        taskInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel currentUserLabel = new JLabel("Текущий пользователь: " + currentUser, SwingConstants.CENTER);
        currentUserLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        currentUserLabel.setForeground(Color.GRAY);
        currentUserLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel newUserLabel = new JLabel("Новый пользователь:");
        newUserLabel.setFont(new Font("Arial", Font.BOLD, 12));
        newUserLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> usersComboBox = new JComboBox<>();
        usersComboBox.setPreferredSize(new Dimension(300, 30));
        usersComboBox.setMaximumSize(new Dimension(300, 30));

        JButton reassignButton = new JButton("Переназначить");
        JButton cancelButton = new JButton("Отмена");

        styleManagementButton(reassignButton, new Color(52, 152, 219));
        styleManagementButton(cancelButton, new Color(108, 117, 125));

        reassignButton.addActionListener(e -> {
            String newUser = (String) usersComboBox.getSelectedItem();
            if (newUser != null && !newUser.equals(currentUser)) {
                reassignDialog.dispose();
                performReassignTask(currentUser, taskTitle, newUser, parentDialog);
            } else {
                showErrorMessage("Выберите другого пользователя");
            }
        });

        cancelButton.addActionListener(e -> reassignDialog.dispose());

        // Загрузка пользователей для выбора
        loadUsersForReassign(usersComboBox, currentUser);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(cancelButton);
        buttonPanel.add(reassignButton);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(taskInfoLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(currentUserLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(newUserLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(usersComboBox);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(buttonPanel);

        reassignDialog.add(contentPanel, BorderLayout.CENTER);
        reassignDialog.setVisible(true);
    }

    private void performUpdateTask(Task task, JDialog parentDialog) {
        showLoadingDialog("Сохранение изменений задачи...");

        new Thread(() -> {
            try {
                String url = "http://localhost:8080/updatetask";

                String taskJson = convertTaskToJson(task);

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
                        .header("Authorization", "Bearer " + authToken)
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(taskJson))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();

                    if (response.statusCode() == 200) {
                        showSuccessMessage("Задача '" + task.getTitle() + "' успешно обновлена");
                        refreshAllData(); // ← ДОБАВИТЬ ЗДЕСЬ
                        parentDialog.dispose();
                    } else {
                        showErrorMessage("Ошибка при обновлении задачи: " + response.body());
                    }
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();
                    showErrorMessage("Ошибка при обновлении задачи: " + e.getMessage());
                });
            }
        }).start();
    }

    private void loadUsersForReassign(JComboBox<String> comboBox, String excludeUser) {
        showLoadingDialog("Загрузка списка пользователей...");

        new Thread(() -> {
            try {
                List<User> users = getAllUsersWithTasks();
                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();
                    comboBox.removeAllItems();
                    if (users != null && !users.isEmpty()) {
                        for (User user : users) {
                            if (!user.getUsername().equals(excludeUser)) {
                                comboBox.addItem(user.getUsername());
                            }
                        }
                        if (comboBox.getItemCount() == 0) {
                            showErrorMessage("Нет других пользователей для переназначения");
                        }
                    } else {
                        showErrorMessage("Не удалось загрузить список пользователей");
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();
                    showErrorMessage("Ошибка загрузки пользователей: " + e.getMessage());
                });
            }
        }).start();
    }

    private Task findTaskByUsernameAndTitle(String username, String title) {
        try {
            List<User> users = getAllUsersWithTasks();
            for (User user : users) {
                if (user.getUsername().equals(username) && user.getTasks() != null) {
                    for (Task task : user.getTasks()) {
                        if (task.getTitle().equals(title)) {
                            return task;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void reassignSelectedTask(JDialog parentDialog) {
        JList<String> usersList = findUsersList(parentDialog);
        JTable tasksTable = findTasksTable(parentDialog);

        if (usersList == null || tasksTable == null) {
            showErrorMessage("Не удалось найти компоненты интерфейса");
            return;
        }

        String selectedUser = usersList.getSelectedValue();
        int selectedTaskRow = tasksTable.getSelectedRow();

        if (selectedUser == null || selectedUser.equals("Пользователи не найдены") || selectedUser.startsWith("Ошибка")) {
            showErrorMessage("Выберите пользователя");
            return;
        }

        if (selectedTaskRow == -1) {
            showErrorMessage("Выберите задачу для переназначения");
            return;
        }

        String taskTitle = (String) tasksTable.getValueAt(selectedTaskRow, 0);

        // Проверяем, что задача существует
        Task task = findTaskByUsernameAndTitle(selectedUser, taskTitle);
        if (task == null) {
            showErrorMessage("Не удалось найти задачу для переназначения");
            return;
        }

        showReassignDialog(selectedUser, taskTitle, parentDialog);
    }

    private void editSelectedTask(JDialog parentDialog) {
        JList<String> usersList = findUsersList(parentDialog);
        JTable tasksTable = findTasksTable(parentDialog);

        if (usersList == null || tasksTable == null) {
            showErrorMessage("Не удалось найти компоненты интерфейса");
            return;
        }

        String selectedUser = usersList.getSelectedValue();
        int selectedTaskRow = tasksTable.getSelectedRow();

        if (selectedUser == null || selectedUser.equals("Пользователи не найдены") || selectedUser.startsWith("Ошибка")) {
            showErrorMessage("Выберите пользователя");
            return;
        }

        if (selectedTaskRow == -1) {
            showErrorMessage("Выберите задачу для редактирования");
            return;
        }

        String taskTitle = (String) tasksTable.getValueAt(selectedTaskRow, 0);
        Task task = findTaskByUsernameAndTitle(selectedUser, taskTitle);

        if (task != null) {
            showEditTaskDialog(task, selectedUser, parentDialog);
        } else {
            showErrorMessage("Не удалось найти задачу для редактирования");
        }
    }


    private void hideLoadingDialog() {
        SwingUtilities.invokeLater(() -> {
            if (loadingDialog != null) {
                loadingDialog.dispose();
                loadingDialog = null;
            }
        });
    }

    private void refreshUserManagement(JDialog parentDialog) {
        JList<String> usersList = findUsersList(parentDialog);
        JTable tasksTable = findTasksTable(parentDialog);

        if (usersList != null && tasksTable != null) {
            DefaultListModel<String> usersModel = (DefaultListModel<String>) usersList.getModel();
            DefaultTableModel tasksModel = (DefaultTableModel) tasksTable.getModel();

            // Сохраняем текущее выделение
            String selectedUser = usersList.getSelectedValue();
            int selectedRow = tasksTable.getSelectedRow();

            // Очищаем и перезагружаем данные
            usersModel.clear();
            tasksModel.setRowCount(0);
            loadUsers(usersModel, tasksModel, tasksTable);

            // Восстанавливаем выделение
            if (selectedUser != null) {
                for (int i = 0; i < usersModel.size(); i++) {
                    if (selectedUser.equals(usersModel.getElementAt(i))) {
                        usersList.setSelectedIndex(i);
                        break;
                    }
                }
            }

            // Обновляем интерфейс
            usersList.repaint();
            tasksTable.repaint();

            System.out.println("DEBUG: User management refreshed");
        } else {
            System.out.println("DEBUG: Could not find components to refresh");
        }
    }

    @SuppressWarnings("unchecked")
    private JList<String> findUsersList(JDialog parentDialog) {
        return findComponentRecursive(JList.class, parentDialog);
    }

    @SuppressWarnings("unchecked")
    private JTable findTasksTable(JDialog parentDialog) {
        return findComponentRecursive(JTable.class, parentDialog);
    }

    @SuppressWarnings("unchecked")
    private <T extends Component> T findComponentRecursive(Class<T> clazz, Container container) {
        for (Component comp : container.getComponents()) {
            if (clazz.isInstance(comp)) {
                return (T) comp;
            }
            if (comp instanceof Container) {
                T found = findComponentRecursive(clazz, (Container) comp);
                if (found != null) return found;
            }
        }
        return null;
    }


    // Метод для назначения задачи пользователю (аналогично созданию новой задачи)
    private boolean assignTaskToUser(Task task, String username) {
        try {
            String url = "http://localhost:8080/assigntask?username=" +
                    java.net.URLEncoder.encode(username, "UTF-8");

            // Создаем копию задачи для нового пользователя
            Task taskForNewUser = new Task();
            taskForNewUser.setTitle(task.getTitle());
            taskForNewUser.setDescription(task.getDescription());
            taskForNewUser.setStatus(task.getStatus());
            taskForNewUser.setImportance(task.getImportance());
            taskForNewUser.setDeadline(task.getDeadline());

            // Устанавливаем assignee
            User assignee = new User();
            assignee.setUsername(username);
            taskForNewUser.setAssignee(assignee);

            String taskJson = convertTaskToJson(taskForNewUser);
            System.out.println("DEBUG: Assign task JSON: " + taskJson);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("DEBUG: Assign task response: " + response.statusCode());
            System.out.println("DEBUG: Assign task body: " + response.body());

            return response.statusCode() == 200;

        } catch (Exception e) {
            System.out.println("DEBUG: Error assigning task to user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String convertTaskToJson(Task task) {
        try {
            StringBuilder json = new StringBuilder();
            json.append("{");

            // Добавляем assignedUser если он есть
            if (task.getAssignee() != null && task.getAssignee().getUsername() != null) {
                json.append("\"assignedUser\":\"").append(escapeJsonString(task.getAssignee().getUsername())).append("\",");
            }

            if (task.getTitle() != null) {
                json.append("\"title\":\"").append(escapeJsonString(task.getTitle())).append("\",");
            }
            if (task.getDescription() != null) {
                json.append("\"description\":\"").append(escapeJsonString(task.getDescription())).append("\",");
            }
            if (task.getStatus() != null) {
                json.append("\"status\":\"").append(escapeJsonString(task.getStatus())).append("\",");
            }
            if (task.getImportance() != null) {
                json.append("\"importance\":\"").append(escapeJsonString(task.getImportance())).append("\",");
            }
            if (task.getDeadline() != null) {
                json.append("\"deadline\":\"").append(escapeJsonString(task.getDeadline())).append("\",");
            }

            // Убираем последнюю запятую
            if (json.charAt(json.length() - 1) == ',') {
                json.deleteCharAt(json.length() - 1);
            }

            json.append("}");
            return json.toString();
        } catch (Exception e) {
            System.out.println("DEBUG: Error converting task to JSON: " + e.getMessage());
            return "{}";
        }
    }

    private String escapeJsonString(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Метод для удаления задачи у пользователя (оставляем как есть, так как он работает)
    private boolean deleteTaskFromUser(String username, String taskTitle) {
        try {
            String url = "http://localhost:8080/deletetask/" +
                    java.net.URLEncoder.encode(username, "UTF-8") + "/" +
                    java.net.URLEncoder.encode(taskTitle, "UTF-8");

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Authorization", "Bearer " + authToken)
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("DEBUG: Delete task response: " + response.statusCode());
            System.out.println("DEBUG: Delete task body: " + response.body());

            return response.statusCode() == 200;

        } catch (Exception e) {
            System.out.println("DEBUG: Error deleting task from user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean deleteTaskViaKafka(String username, Long taskId) {
        try {
            KafkaTaskProducer kafkaTaskProducer = new KafkaTaskProducer();

            System.out.println("DEBUG: Отправка удаления в Kafka для пользователя: " + username + ", ID задачи: " + taskId);

            // Используем метод с ID задачи
            kafkaTaskProducer.sendTaskDeletion(username, taskId);

            // Ждем немного для обработки
            Thread.sleep(500);

            return true;

        } catch (Exception e) {
            System.out.println("DEBUG: Ошибка удаления задачи через Kafka: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String createDeletionJson(String username, String taskTitle) {
        try {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"username\":\"").append(escapeJsonString(username)).append("\",");
            json.append("\"taskTitle\":\"").append(escapeJsonString(taskTitle)).append("\"");
            json.append("}");

            System.out.println("DEBUG: Deletion JSON: " + json.toString());
            return json.toString();

        } catch (Exception e) {
            System.out.println("DEBUG: Error creating deletion JSON: " + e.getMessage());
            return "{}";
        }
    }

    private String convertTaskToJsonForKafka(Task task, String newUser) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> taskData = new HashMap<>();

            // Все основные поля задачи
            taskData.put("title", task.getTitle());
            taskData.put("description", task.getDescription() != null ? task.getDescription() : "");
            taskData.put("status", task.getStatus() != null ? task.getStatus() : "НЕ_НАЧАТА");
            taskData.put("importance", task.getImportance() != null ? task.getImportance() : "МОЖЕТ_ПОДОЖДАТЬ");

            if (task.getDeadline() != null) {
                taskData.put("deadline", task.getDeadline());
            } else {
                // Устанавливаем дедлайн по умолчанию (завтра)
                String defaultDeadline = java.time.LocalDateTime.now().plusDays(1)
                        .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                taskData.put("deadline", defaultDeadline);
            }

            taskData.put("assignedUser", newUser);

            String taskJson = objectMapper.writeValueAsString(taskData);
            System.out.println("DEBUG: Task JSON for Kafka: " + taskJson);
            return taskJson;

        } catch (Exception e) {
            System.out.println("DEBUG: Error converting task to JSON for Kafka: " + e.getMessage());
            e.printStackTrace();
            return "{}";
        }

    }

    private Task parseSingleTaskImproved(String taskJson) {
        try {
            System.out.println("DEBUG: Admin - Parsing task: " + taskJson);

            // Извлекаем все поля, включая id
            String idStr = extractValueImproved(taskJson, "id");
            String title = extractValueImproved(taskJson, "title");
            String description = extractValueImproved(taskJson, "description");
            String status = extractValueImproved(taskJson, "status");
            String importance = extractValueImproved(taskJson, "importance");
            String deadline = extractValueImproved(taskJson, "deadline");

            if (title != null) {
                Task task = new Task();

                // Устанавливаем taskId если он есть
                if (idStr != null && !idStr.isEmpty()) {
                    try {
                        task.setTaskId(Long.parseLong(idStr));
                        System.out.println("DEBUG: Admin - Set task ID: " + idStr + " for task: " + title);
                    } catch (NumberFormatException e) {
                        System.out.println("DEBUG: Admin - Error parsing task ID: " + idStr);
                    }
                }

                task.setTitle(title);
                task.setDescription(description);
                task.setStatus(status);
                task.setImportance(importance);
                task.setDeadline(deadline);

                // Парсинг комментариев (остальной код без изменений)
                if (taskJson.contains("\"comments\":")) {
                    int commentsStart = taskJson.indexOf("\"comments\":[") + 11;
                    int commentsEnd = findMatchingBracket(taskJson, commentsStart - 1);
                    if (commentsEnd > commentsStart) {
                        String commentsArray = taskJson.substring(commentsStart, commentsEnd).trim();
                        List<Comment> comments = parseCommentsArrayImproved(commentsArray);
                        task.setComments(comments);
                        System.out.println("DEBUG: Admin - Found " + comments.size() + " comments for task: " + title);
                    }
                }
                return task;
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Admin - Error parsing task: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void performReassignTask(String currentUser, String taskTitle, String newUser, JDialog parentDialog) {
        System.out.println("DEBUG: Starting reassign task - currentUser: " + currentUser +
                ", taskTitle: " + taskTitle + ", newUser: " + newUser);

        showLoadingDialog("Переназначение задачи '" + taskTitle + "' пользователю " + newUser + "...");

        new Thread(() -> {
            try {
                // 1. Получаем задачу у старого пользователя
                System.out.println("DEBUG: Finding task for user: " + currentUser + ", title: " + taskTitle);
                Task originalTask = findTaskByUsernameAndTitle(currentUser, taskTitle);
                if (originalTask == null) {
                    throw new RuntimeException("Задача не найдена у пользователя " + currentUser);
                }

                Long originalTaskId = originalTask.getTaskId();
                System.out.println("DEBUG: Original task found: " + originalTask.getTitle() + " (ID: " + originalTaskId + ")");

                // 2. Сохраняем данные задачи перед удалением
                Task taskCopy = new Task();
                taskCopy.setTitle(originalTask.getTitle());
                taskCopy.setDescription(originalTask.getDescription());
                taskCopy.setStatus(originalTask.getStatus());
                taskCopy.setImportance(originalTask.getImportance());
                taskCopy.setDeadline(originalTask.getDeadline());
                taskCopy.setComments(originalTask.getComments());

                // 3. УДАЛЯЕМ задачу у старого пользователя через Kafka
                System.out.println("DEBUG: Deleting task from old user via Kafka: " + currentUser + ", ID: " + originalTaskId);
                KafkaTaskProducer kafkaTaskProducer = new KafkaTaskProducer();
                kafkaTaskProducer.sendTaskDeletion(currentUser, originalTaskId);

                // 4. Ждем удаления (небольшая задержка)
                System.out.println("DEBUG: Waiting for deletion to process...");
                Thread.sleep(1500);

                // 5. СОЗДАЕМ задачу у нового пользователя через Kafka
                System.out.println("DEBUG: Creating task for new user: " + newUser);
                boolean createSuccess = createTaskForUser(taskCopy, newUser);
                if (!createSuccess) {
                    throw new RuntimeException("Не удалось создать задачу у нового пользователя");
                }

                // 6. Ждем создания
                System.out.println("DEBUG: Waiting for task creation...");
                Thread.sleep(1500);

                System.out.println("DEBUG: Reassign completed successfully");

                // УСПЕШНОЕ ЗАВЕРШЕНИЕ
                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();
                    showSuccessMessage("Задача '" + taskTitle + "' успешно переназначена от " + currentUser + " к " + newUser);
                    refreshAllData();
                });

            } catch (Exception e) {
                System.out.println("DEBUG: Exception in reassign: " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();
                    showErrorMessage("Ошибка при переназначении задачи: " + e.getMessage());
                });
            }
        }).start();
    }

    private boolean createTaskForUser(Task task, String username) {
        try {
            KafkaTaskProducer kafkaTaskProducer = new KafkaTaskProducer();

            // Создаем копию задачи для нового пользователя (без ID)
            Task taskForNewUser = new Task();
            taskForNewUser.setTitle(task.getTitle());
            taskForNewUser.setDescription(task.getDescription());
            taskForNewUser.setStatus(task.getStatus());
            taskForNewUser.setImportance(task.getImportance());
            taskForNewUser.setDeadline(task.getDeadline());

            // Комментарии тоже копируем
            if (task.getComments() != null) {
                taskForNewUser.setComments(new ArrayList<>(task.getComments()));
            }

            // Конвертируем задачу в JSON формат для Kafka
            String taskJson = convertTaskToJsonForKafka(taskForNewUser, username);
            System.out.println("DEBUG: Creating task for user: " + username + ", Task JSON: " + taskJson);

            // Отправляем задачу через Kafka
            kafkaTaskProducer.sendTask(username, taskJson);

            System.out.println("DEBUG: Task creation via Kafka initiated");
            return true;

        } catch (Exception e) {
            System.out.println("DEBUG: Error creating task for user via Kafka: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void performDeleteTask(String username, String taskTitle, JDialog parentDialog) {
        showLoadingDialog("Удаление задачи '" + taskTitle + "'...");

        new Thread(() -> {
            try {
                // 1. Находим задачу для получения ID
                System.out.println("DEBUG: Finding task for deletion - user: " + username + ", title: " + taskTitle);
                Task task = findTaskByUsernameAndTitle(username, taskTitle);
                if (task == null) {
                    throw new RuntimeException("Задача не найдена у пользователя " + username);
                }

                if (task.getTaskId() == null) {
                    throw new RuntimeException("У задачи отсутствует ID");
                }

                System.out.println("DEBUG: Task found for deletion: " + task.getTitle() + " (ID: " + task.getTaskId() + ")");

                // 2. Удаляем задачу через Kafka
                KafkaTaskProducer kafkaTaskProducer = new KafkaTaskProducer();
                System.out.println("DEBUG: Sending deletion to Kafka for user: " + username + ", taskId: " + task.getTaskId());
                kafkaTaskProducer.sendTaskDeletion(username, task.getTaskId());

                // 3. Ждем обработки
                System.out.println("DEBUG: Waiting for deletion to process...");
                Thread.sleep(1500);

                // 4. Проверяем, что задача удалилась
                System.out.println("DEBUG: Verifying task deletion...");
                Task deletedTask = findTaskByUsernameAndTitle(username, taskTitle);
                if (deletedTask != null) {
                    System.out.println("DEBUG: Task still exists, trying HTTP fallback...");
                }

                System.out.println("DEBUG: Task deletion completed successfully");

                // УСПЕШНОЕ ЗАВЕРШЕНИЕ
                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();
                    showSuccessMessage("Задача '" + taskTitle + "' успешно удалена");
                    refreshAllData();
                });

            } catch (Exception e) {
                System.out.println("DEBUG: Exception in task deletion: " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();
                    showErrorMessage("Ошибка при удалении задачи: " + e.getMessage());
                });
            }
        }).start();
    }

    // Обновленный метод для информационных сообщений
    private void showInfoMessage(String message) {
        JDialog infoDialog = createStyledDialog(
                "Информация",
                message,
                "ℹ️",
                new Color(52, 152, 219),
                "Понятно",
                new Color(52, 152, 219),
                false
        );
        infoDialog.setVisible(true);
    }

    // Универсальный метод создания красивого диалога
    private JDialog createStyledDialog(String title, String message, String icon, Color iconColor,
                                       String buttonText, Color buttonColor, boolean showCancel) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(255, 255, 255));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Иконка
        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setForeground(iconColor);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Заголовок
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Сообщение
        JLabel messageLabel = new JLabel("<html><center>" + message + "</center></html>", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(127, 140, 141));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(new Color(255, 255, 255));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton primaryButton = new JButton(buttonText);
        styleDialogButton(primaryButton, buttonColor);

        if (showCancel) {
            JButton cancelButton = new JButton("Отмена");
            styleDialogButton(cancelButton, new Color(108, 117, 125));
            cancelButton.addActionListener(e -> dialog.dispose());
            buttonPanel.add(cancelButton);
        }

        primaryButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(primaryButton);

        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(25));
        contentPanel.add(buttonPanel);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.getRootPane().setDefaultButton(primaryButton);
        dialog.pack();

        return dialog;
    }

    // Обновленный диалог подтверждения удаления пользователя
    private boolean showDeleteUserConfirmation(String username) {
        final boolean[] result = {false};

        JDialog confirmDialog = new JDialog(this, "Подтверждение удаления", true);
        confirmDialog.setSize(500, 350);
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.setResizable(false);
        confirmDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(255, 255, 255));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Иконка
        JLabel iconLabel = new JLabel("⚠️", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setForeground(new Color(231, 76, 60));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Заголовок
        JLabel titleLabel = new JLabel("Подтверждение удаления", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Сообщение
        JLabel messageLabel = new JLabel(
                "<html><center>" +
                        "Вы уверены, что хотите удалить пользователя <b>'" + username + "'</b>?<br><br>" +
                        "<font color='#e74c3c' size='3'>⚠️ Это действие нельзя отменить!</font>" +
                        "</center></html>",
                SwingConstants.CENTER
        );
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(127, 140, 141));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(new Color(255, 255, 255));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Кнопка Отмена
        JButton cancelButton = new JButton("Отмена");
        styleDialogButton(cancelButton, new Color(108, 117, 125));

        // Кнопка Удалить
        JButton deleteButton = new JButton("Удалить");
        styleDialogButton(deleteButton, new Color(231, 76, 60));

        cancelButton.addActionListener(e -> {
            result[0] = false;
            confirmDialog.dispose();
        });

        deleteButton.addActionListener(e -> {
            result[0] = true;
            confirmDialog.dispose();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(deleteButton);

        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(25));
        contentPanel.add(buttonPanel);

        confirmDialog.add(contentPanel, BorderLayout.CENTER);
        confirmDialog.getRootPane().setDefaultButton(cancelButton);
        confirmDialog.pack();
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.setVisible(true);

        return result[0];
    }

    // Обновленный диалог подтверждения удаления задачи
    private boolean showDeleteTaskConfirmation(String username, String taskTitle) {
        final boolean[] result = {false};

        JDialog confirmDialog = new JDialog(this, "Подтверждение удаления", true);
        confirmDialog.setSize(500, 350);
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.setResizable(false);
        confirmDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(255, 255, 255));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Иконка
        JLabel iconLabel = new JLabel("🗑️", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setForeground(new Color(231, 76, 60));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Заголовок
        JLabel titleLabel = new JLabel("Удаление задачи", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Сообщение
        JLabel messageLabel = new JLabel(
                "<html><center>" +
                        "Вы уверены, что хотите удалить задачу <b>'" + taskTitle + "'</b><br>" +
                        "у пользователя <b>'" + username + "'</b>?" +
                        "</center></html>",
                SwingConstants.CENTER
        );
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(127, 140, 141));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(new Color(255, 255, 255));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Кнопка Отмена
        JButton cancelButton = new JButton("Отмена");
        styleDialogButton(cancelButton, new Color(108, 117, 125));

        // Кнопка Удалить
        JButton deleteButton = new JButton("Удалить");
        styleDialogButton(deleteButton, new Color(231, 76, 60));

        cancelButton.addActionListener(e -> {
            result[0] = false;
            confirmDialog.dispose();
        });

        deleteButton.addActionListener(e -> {
            result[0] = true;
            confirmDialog.dispose();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(deleteButton);

        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(25));
        contentPanel.add(buttonPanel);

        confirmDialog.add(contentPanel, BorderLayout.CENTER);
        confirmDialog.getRootPane().setDefaultButton(cancelButton);
        confirmDialog.pack();
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.setVisible(true);

        return result[0];
    }

    // Обновленный метод для диалога загрузки
    private void showLoadingDialog(String message) {
        SwingUtilities.invokeLater(() -> {
            if (loadingDialog != null && loadingDialog.isVisible()) {
                loadingDialog.dispose();
            }

            loadingDialog = new JDialog(this, "", true);
            loadingDialog.setSize(400, 150);
            loadingDialog.setLocationRelativeTo(this);
            loadingDialog.setLayout(new BorderLayout());
            loadingDialog.setResizable(false);
            loadingDialog.setUndecorated(true);

            JPanel loadingPanel = new JPanel(new BorderLayout());
            loadingPanel.setBackground(Color.WHITE);
            loadingPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                    BorderFactory.createEmptyBorder(25, 25, 25, 25)
            ));

            JLabel loadingLabel = new JLabel(message, SwingConstants.CENTER);
            loadingLabel.setFont(new Font("Arial", Font.BOLD, 14));
            loadingLabel.setForeground(new Color(44, 62, 80));

            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setBackground(Color.WHITE);
            progressBar.setForeground(new Color(52, 152, 219));
            progressBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

            loadingPanel.add(loadingLabel, BorderLayout.CENTER);
            loadingPanel.add(progressBar, BorderLayout.SOUTH);

            loadingDialog.add(loadingPanel, BorderLayout.CENTER);
            loadingDialog.pack();
            loadingDialog.setLocationRelativeTo(this);
            loadingDialog.setVisible(true);
        });
    }

// Обновим методы в управлении пользователями для использования новых диалогов:

    private void deleteSelectedUser(JDialog parentDialog) {
        JList<String> usersList = findUsersList(parentDialog);
        if (usersList == null) {
            showErrorMessage("Не удалось найти список пользователей");
            return;
        }

        String selectedUser = usersList.getSelectedValue();
        if (selectedUser == null || selectedUser.equals("Пользователи не найдены") || selectedUser.startsWith("Ошибка")) {
            showErrorMessage("Выберите пользователя для удаления");
            return;
        }

        boolean confirm = showDeleteUserConfirmation(selectedUser);
        if (confirm) {
            performDeleteUser(selectedUser, parentDialog);
        }
    }

    private void deleteSelectedTask(JDialog parentDialog) {
        JList<String> usersList = findUsersList(parentDialog);
        JTable tasksTable = findTasksTable(parentDialog);

        if (usersList == null || tasksTable == null) {
            showErrorMessage("Не удалось найти компоненты интерфейса");
            return;
        }

        String selectedUser = usersList.getSelectedValue();
        int selectedTaskRow = tasksTable.getSelectedRow();

        if (selectedUser == null || selectedUser.equals("Пользователи не найдены") || selectedUser.startsWith("Ошибка")) {
            showErrorMessage("Выберите пользователя");
            return;
        }

        if (selectedTaskRow == -1) {
            showErrorMessage("Выберите задачу для удаления");
            return;
        }

        String taskTitle = (String) tasksTable.getValueAt(selectedTaskRow, 0);

        boolean confirm = showDeleteTaskConfirmation(selectedUser, taskTitle);
        if (confirm) {
            performDeleteTask(selectedUser, taskTitle, parentDialog);
        }
    }

    // Единый стиль для кнопок диалогов
    private void styleDialogButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
    }

    // Простой и надежный метод выбора даты
    private void showSimpleDatePicker(JTextField dateField) {
        JDialog dateDialog = new JDialog(this, "Выбор даты", true);
        dateDialog.setSize(300, 200);
        dateDialog.setLocationRelativeTo(this);
        dateDialog.setLayout(new BorderLayout());
        dateDialog.setResizable(false);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Панель с выбором даты
        JPanel datePanel = new JPanel(new GridLayout(3, 2, 10, 10));
        datePanel.setBackground(Color.WHITE);
        datePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        // Текущая дата для значений по умолчанию
        java.time.LocalDate currentDate = java.time.LocalDate.now();
        if (!dateField.getText().isEmpty()) {
            try {
                currentDate = java.time.LocalDate.parse(dateField.getText());
            } catch (Exception e) {
                // Если не удалось распарсить, используем текущую дату
            }
        }

        // Год
        JLabel yearLabel = new JLabel("Год:");
        yearLabel.setFont(new Font("Arial", Font.BOLD, 12));
        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(
                currentDate.getYear(), 2020, 2030, 1
        ));
        styleSpinner(yearSpinner);

        // Месяц
        JLabel monthLabel = new JLabel("Месяц:");
        monthLabel.setFont(new Font("Arial", Font.BOLD, 12));
        JSpinner monthSpinner = new JSpinner(new SpinnerNumberModel(
                currentDate.getMonthValue(), 1, 12, 1
        ));
        styleSpinner(monthSpinner);

        // День
        JLabel dayLabel = new JLabel("День:");
        dayLabel.setFont(new Font("Arial", Font.BOLD, 12));
        JSpinner daySpinner = new JSpinner(new SpinnerNumberModel(
                currentDate.getDayOfMonth(), 1, 31, 1
        ));
        styleSpinner(daySpinner);

        datePanel.add(yearLabel);
        datePanel.add(yearSpinner);
        datePanel.add(monthLabel);
        datePanel.add(monthSpinner);
        datePanel.add(dayLabel);
        datePanel.add(daySpinner);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton okButton = new JButton("Выбрать");
        JButton cancelButton = new JButton("Отмена");

        styleDialogButton(okButton, new Color(52, 152, 219));
        styleDialogButton(cancelButton, new Color(108, 117, 125));

        okButton.addActionListener(e -> {
            try {
                int year = (Integer) yearSpinner.getValue();
                int month = (Integer) monthSpinner.getValue();
                int day = (Integer) daySpinner.getValue();

                java.time.LocalDate selectedDate = java.time.LocalDate.of(year, month, day);
                dateField.setText(selectedDate.toString());
                dateDialog.dispose();
            } catch (Exception ex) {
                showErrorMessage("Неверная дата: " + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> dateDialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);

        contentPanel.add(datePanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dateDialog.add(contentPanel, BorderLayout.CENTER);
        dateDialog.setVisible(true);
    }

    // Стилизация спиннеров
    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(new Font("Arial", Font.PLAIN, 12));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            textField.setBackground(Color.WHITE);
            textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
        }
    }

    // Проверка формата даты
    private boolean isValidDateFormat(String date) {
        try {
            java.time.LocalDate.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }



    // Метод для стилизации спиннеров даты
    private void styleDateSpinner(JSpinner spinner) {
        spinner.setFont(new Font("Arial", Font.PLAIN, 12));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            textField.setBackground(Color.WHITE);
            textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
        }
    }

    private void showEditTaskDialog(Task task, String username, JDialog parentDialog) {
        JDialog editDialog = new JDialog(parentDialog, "Редактирование задачи", true);
        editDialog.setSize(700, 800);
        editDialog.setLocationRelativeTo(parentDialog);
        editDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Заголовок
        JLabel titleLabel = new JLabel("Редактирование задачи", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLabel = new JLabel("Пользователь: " + username, SwingConstants.CENTER);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userLabel.setForeground(Color.GRAY);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Название задачи как заголовок
        JLabel taskTitleLabel = new JLabel(task.getTitle(), SwingConstants.CENTER);
        taskTitleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        taskTitleLabel.setForeground(new Color(52, 152, 219));
        taskTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Статус как надпись (черный цвет)
        JLabel statusTitleLabel = new JLabel("Статус: " + getStatusDisplayName(task.getStatus()), SwingConstants.CENTER);
        statusTitleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusTitleLabel.setForeground(Color.BLACK);
        statusTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Основная панель с полями - используем GridBagLayout для выравнивания
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 15, 15);

        // Описание задачи
        JLabel descLabel = new JLabel("Описание задачи:");
        descLabel.setFont(new Font("Arial", Font.BOLD, 14));
        descLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        fieldsPanel.add(descLabel, gbc);

        JTextArea descArea = new JTextArea(task.getDescription() != null ? task.getDescription() : "", 20, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(new Font("Arial", Font.PLAIN, 13));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(400, 250));
        descScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        fieldsPanel.add(descScroll, gbc);

        // Приоритет
        JLabel priorityLabel = new JLabel("Приоритет:");
        priorityLabel.setFont(new Font("Arial", Font.BOLD, 14));
        priorityLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        fieldsPanel.add(priorityLabel, gbc);

        String[] priorityOptions = {"СРОЧНАЯ", "НАДО_ПОТОРОПИТЬСЯ", "МОЖЕТ_ПОДОЖДАТЬ"};
        JComboBox<String> priorityCombo = new JComboBox<>(priorityOptions);
        priorityCombo.setSelectedItem(task.getImportance());
        priorityCombo.setPreferredSize(new Dimension(400, 45));
        priorityCombo.setFont(new Font("Arial", Font.PLAIN, 13));
        priorityCombo.setBackground(Color.WHITE);
        priorityCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        fieldsPanel.add(priorityCombo, gbc);

        // Дедлайн
        JLabel deadlineLabel = new JLabel("Дедлайн:");
        deadlineLabel.setFont(new Font("Arial", Font.BOLD, 14));
        deadlineLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        fieldsPanel.add(deadlineLabel, gbc);

        JPanel deadlinePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        deadlinePanel.setBackground(Color.WHITE);
        deadlinePanel.setPreferredSize(new Dimension(400, 40));

        // Создаем спиннеры для даты
        String deadline = task.getDeadline() != null ? task.getDeadline().split("T")[0] : "";
        java.time.LocalDate currentDate;

        if (!deadline.isEmpty()) {
            try {
                currentDate = java.time.LocalDate.parse(deadline);
            } catch (Exception e) {
                currentDate = java.time.LocalDate.now();
            }
        } else {
            currentDate = java.time.LocalDate.now();
        }

        // Спиннер для года
        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(
                currentDate.getYear(), 2020, 2030, 1
        ));
        styleDateSpinner(yearSpinner);
        yearSpinner.setPreferredSize(new Dimension(80, 35));

        // Спиннер для месяца
        JSpinner monthSpinner = new JSpinner(new SpinnerNumberModel(
                currentDate.getMonthValue(), 1, 12, 1
        ));
        styleDateSpinner(monthSpinner);
        monthSpinner.setPreferredSize(new Dimension(60, 35));

        // Спиннер для дня
        JSpinner daySpinner = new JSpinner(new SpinnerNumberModel(
                currentDate.getDayOfMonth(), 1, 31, 1
        ));
        styleDateSpinner(daySpinner);
        daySpinner.setPreferredSize(new Dimension(60, 35));

        // Метки для спиннеров
        JLabel yearLabel = new JLabel("год");
        yearLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        JLabel monthLabel = new JLabel("мес");
        monthLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        JLabel dayTextLabel = new JLabel("день");
        dayTextLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        deadlinePanel.add(yearSpinner);
        deadlinePanel.add(yearLabel);
        deadlinePanel.add(monthSpinner);
        deadlinePanel.add(monthLabel);
        deadlinePanel.add(daySpinner);
        deadlinePanel.add(dayTextLabel);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        fieldsPanel.add(deadlinePanel, gbc);

        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton saveButton = new JButton("Сохранить изменения");
        JButton cancelButton = new JButton("Отмена");

        saveButton.setFont(new Font("Arial", Font.BOLD, 14));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(new Color(46, 204, 113));
        saveButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(39, 174, 96), 1),
                BorderFactory.createEmptyBorder(12, 25, 12, 25)
        ));
        saveButton.setFocusPainted(false);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(108, 117, 125));
        cancelButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(90, 98, 104), 1),
                BorderFactory.createEmptyBorder(12, 25, 12, 25)
        ));
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        saveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                saveButton.setBackground(new Color(39, 174, 96));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                saveButton.setBackground(new Color(46, 204, 113));
            }
        });

        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cancelButton.setBackground(new Color(90, 98, 104));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                cancelButton.setBackground(new Color(108, 117, 125));
            }
        });

        saveButton.addActionListener(e -> {
            if (descArea.getText().trim().isEmpty()) {
                showErrorMessage("Описание задачи не может быть пустым");
                return;
            }

            Task updatedTask = new Task();
            updatedTask.setTitle(task.getTitle());
            updatedTask.setDescription(descArea.getText().trim());
            updatedTask.setStatus(task.getStatus());
            updatedTask.setImportance((String) priorityCombo.getSelectedItem());

            // Получаем дату из спиннеров
            int year = (Integer) yearSpinner.getValue();
            int month = (Integer) monthSpinner.getValue();
            int day = (Integer) daySpinner.getValue();

            try {
                java.time.LocalDate selectedDate = java.time.LocalDate.of(year, month, day);
                updatedTask.setDeadline(selectedDate.toString() + "T00:00:00");
            } catch (Exception ex) {
                showErrorMessage("Неверная дата: " + ex.getMessage());
                return;
            }

            performUpdateTask(updatedTask, parentDialog);
            editDialog.dispose();
        });

        cancelButton.addActionListener(e -> editDialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(userLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(taskTitleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(statusTitleLabel);
        contentPanel.add(Box.createVerticalStrut(25));
        contentPanel.add(fieldsPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(buttonPanel);

        editDialog.add(contentPanel, BorderLayout.CENTER);
        editDialog.setVisible(true);
    }

    // Добавьте метод для корректного закрытия
    @Override
    public void dispose() {
        // Останавливаем consumer при закрытии окна
        if (notificationConsumer != null) {
            notificationConsumer.stop();
        }
        super.dispose();
    }

    private boolean sendReworkRequestToServer(Task task, String commentText) {
        try {
            // Проверяем, что taskId не null
            if (task.getTaskId() == null) {
                System.out.println("DEBUG: Task ID is null for task: " + task.getTitle());
                return false;
            }

            String url = "http://localhost:8080/markthetaskasonrework?taskId=" + task.getTaskId();

            System.out.println("DEBUG: Sending rework request to: " + url);
            System.out.println("DEBUG: Task ID: " + task.getTaskId() + ", Comment: " + commentText);

            // Создаем объект комментария
            Comment comment = new Comment();
            comment.setDescription(commentText);

            // Преобразуем комментарий в JSON
            String commentJson = convertCommentToJson(comment);
            System.out.println("DEBUG: Comment JSON: " + commentJson);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(commentJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("DEBUG: Rework response: " + response.statusCode());
            System.out.println("DEBUG: Rework body: " + response.body());

            return response.statusCode() == 200;

        } catch (Exception e) {
            System.out.println("DEBUG: Error sending rework request: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Task parseSingleTask(String taskJson) {
        try {
            System.out.println("DEBUG: Parsing task JSON: " + taskJson);

            String idStr = extractValue(taskJson, "id");
            String title = extractValue(taskJson, "title");
            String description = extractValue(taskJson, "description");
            String status = extractValue(taskJson, "status");
            String importance = extractValue(taskJson, "importance");
            String deadline = extractValue(taskJson, "deadline");

            System.out.println("DEBUG: Extracted values - ID: '" + idStr + "', Title: '" + title + "'");

            if (title != null) {
                Task task = new Task();

                // Устанавливаем taskId
                if (idStr != null && !idStr.isEmpty() && !idStr.equals("null")) {
                    try {
                        task.setTaskId(Long.parseLong(idStr));
                        System.out.println("DEBUG: Successfully set task ID: " + idStr + " for task: " + title);
                    } catch (NumberFormatException e) {
                        System.out.println("DEBUG: Error parsing task ID: '" + idStr + "' for task: " + title);
                        // Можно установить временный ID или оставить null
                        task.setTaskId(null);
                    }
                } else {
                    System.out.println("DEBUG: Task ID is null or empty for task: " + title);
                    task.setTaskId(null);
                }

                task.setTitle(title);
                task.setDescription(description);
                task.setStatus(status);
                task.setImportance(importance);
                task.setDeadline(deadline);

                // Парсинг комментариев
                if (taskJson.contains("\"comments\":")) {
                    int commentsStart = taskJson.indexOf("\"comments\":[") + 11;
                    int commentsEnd = taskJson.indexOf("]", commentsStart);
                    if (commentsEnd > commentsStart) {
                        String commentsArray = taskJson.substring(commentsStart, commentsEnd);
                        List<Comment> comments = parseCommentsArray(commentsArray);
                        task.setComments(comments);
                        System.out.println("DEBUG: Found " + comments.size() + " comments for task: " + title);
                    }
                }
                return task;
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Error parsing task: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void refreshAllData() {
        // Обновляем статистику на главной
        loadAdminStatistics();

        // Обновляем список всех задач, если он открыт
        refreshAllTasksIfOpen();

        // Обновляем управление пользователями, если оно открыто
        refreshUserManagementIfOpen();
    }

    private void refreshAllTasksIfOpen() {
        // Проверяем, открыта ли вкладка "Все задачи"
        for (Component comp : centerPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                // Ищем панель с заголовком "Все задачи системы"
                if (containsAllTasksTitle(panel)) {
                    // Перезагружаем панель всех задач
                    showAllTasks();
                    break;
                }
            }
        }
    }

    private boolean containsAllTasksTitle(JPanel panel) {
        // Рекурсивно ищем заголовок "Все задачи системы"
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if ("Все задачи системы".equals(label.getText())) {
                    return true;
                }
            }
            if (comp instanceof JPanel) {
                if (containsAllTasksTitle((JPanel) comp)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void refreshUserManagementIfOpen() {
        // Ищем открытые диалоги управления пользователями
        for (Window window : Window.getWindows()) {
            if (window instanceof JDialog) {
                JDialog dialog = (JDialog) window;
                if ("Управление пользователями".equals(dialog.getTitle()) && dialog.isVisible()) {
                    // Находим и обновляем данные в диалоге
                    refreshUserManagementInDialog(dialog);
                }
            }
        }
    }

    private void refreshUserManagementInDialog(JDialog dialog) {
        JList<String> usersList = findUsersList(dialog);
        JTable tasksTable = findTasksTable(dialog);

        if (usersList != null && tasksTable != null) {
            DefaultListModel<String> usersModel = (DefaultListModel<String>) usersList.getModel();
            DefaultTableModel tasksModel = (DefaultTableModel) tasksTable.getModel();

            // Сохраняем текущее выделение
            String selectedUser = usersList.getSelectedValue();
            int selectedRow = tasksTable.getSelectedRow();

            // Перезагружаем данные
            loadUsers(usersModel, tasksModel, tasksTable);

            // Восстанавливаем выделение
            if (selectedUser != null) {
                for (int i = 0; i < usersModel.size(); i++) {
                    if (selectedUser.equals(usersModel.getElementAt(i))) {
                        usersList.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    private void loadUserTasks(String username, DefaultTableModel tasksTableModel) {
        showLoadingDialog("Загрузка задач пользователя...");

        new Thread(() -> {
            try {
                List<User> users = getAllUsersWithTasks();
                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();
                    tasksTableModel.setRowCount(0);

                    if (users != null) {
                        for (User user : users) {
                            if (user.getUsername().equals(username) && user.getTasks() != null) {
                                for (Task task : user.getTasks()) {
                                    String deadline = task.getDeadline() != null ?
                                            DeadlineUtils.formatDeadlineForDisplay(task.getDeadline()) : "нет дедлайна";
                                    String statusWithDays = getStatusDisplayName(task.getStatus()) + " (" +
                                            DeadlineUtils.getDeadlineStatusText(task.getDeadline()) + ")";

                                    tasksTableModel.addRow(new Object[]{
                                            task.getTitle(),
                                            statusWithDays,
                                            getImportanceDisplayName(task.getImportance()),
                                            deadline
                                    });

                                    // Устанавливаем цвет для строки на основе дедлайна
                                    int rowIndex = tasksTableModel.getRowCount() - 1;
                                    // Цвет будет установлен через рендерер
                                }
                                break;
                            }
                        }
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();
                    showErrorMessage("Ошибка загрузки задач: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showTaskDescription(Task task) {
        JDialog descriptionDialog = new JDialog(this, "Описание задачи: " + task.getTitle(), true);
        descriptionDialog.setSize(600, 500);
        descriptionDialog.setLocationRelativeTo(this);
        descriptionDialog.setLayout(new BorderLayout());
        descriptionDialog.setResizable(true);

        // Основная панель
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Заголовок
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel iconLabel = new JLabel("📄", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setForeground(new Color(52, 152, 219));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Описание задачи", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel taskTitleLabel = new JLabel("\"" + task.getTitle() + "\"", SwingConstants.CENTER);
        taskTitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        taskTitleLabel.setForeground(new Color(127, 140, 141));
        taskTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(iconLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(taskTitleLabel);

        // Панель с описанием
        JPanel descriptionContentPanel = new JPanel(new BorderLayout());
        descriptionContentPanel.setBackground(Color.WHITE);
        descriptionContentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JTextArea descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 14));

        String description = task.getDescription();
        if (description != null && !description.trim().isEmpty()) {
            descriptionArea.setText(description);
            descriptionArea.setForeground(new Color(44, 62, 80));
        } else {
            descriptionArea.setText("Описание отсутствует");
            descriptionArea.setFont(new Font("Arial", Font.ITALIC, 14));
            descriptionArea.setForeground(Color.GRAY);
        }

        descriptionArea.setBackground(new Color(248, 249, 250));
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        descriptionContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Панель статистики
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        int descLength = description != null ? description.length() : 0;
        JLabel statsLabel = new JLabel("Длина описания: " + descLength + " символов");
        statsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statsLabel.setForeground(new Color(108, 117, 125));
        statsLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        statsPanel.add(statsLabel);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton closeButton = new JButton("Закрыть");
        styleDialogButton(closeButton, new Color(108, 117, 125));

        closeButton.addActionListener(e -> descriptionDialog.dispose());

        buttonPanel.add(closeButton);

        // Собираем все компоненты
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(descriptionContentPanel, BorderLayout.CENTER);
        mainPanel.add(statsPanel, BorderLayout.SOUTH);

        descriptionDialog.add(mainPanel, BorderLayout.CENTER);
        descriptionDialog.add(buttonPanel, BorderLayout.SOUTH);

        descriptionDialog.getRootPane().setDefaultButton(closeButton);
        descriptionDialog.pack();
        descriptionDialog.setLocationRelativeTo(this);
        descriptionDialog.setVisible(true);
    }

    private void addAdminTaskRow(JPanel parent, Task task, String username) {
        JPanel taskRow = new JPanel(new GridLayout(1, 8, 10, 5)); // Увеличили до 8 колонок
        taskRow.setBackground(Color.WHITE);
        taskRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        taskRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Колонка пользователя
        JLabel userLabel = new JLabel(username != null ? username : "");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        userLabel.setForeground(new Color(44, 62, 80));
        taskRow.add(userLabel);

        // Колонка названия задачи
        JLabel titleLabel = new JLabel(task.getTitle() != null ? task.getTitle() : "");
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(44, 62, 80));
        taskRow.add(titleLabel);

        // Колонка описания (кликабельная ссылка)
        String descriptionText = task.getDescription();
        boolean hasDescription = descriptionText != null && !descriptionText.trim().isEmpty();

        JLabel descriptionLabel = new JLabel(hasDescription ? "📝 Просмотр" : "Нет описания");
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descriptionLabel.setForeground(hasDescription ? new Color(52, 152, 219) : Color.GRAY);

        // Делаем кликабельным только если есть описание
        if (hasDescription) {
            descriptionLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            descriptionLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    showTaskDescription(task);
                }
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    descriptionLabel.setForeground(new Color(41, 128, 185));
                    descriptionLabel.setText("<html><u>📝 Просмотр</u></html>");
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    descriptionLabel.setForeground(new Color(52, 152, 219));
                    descriptionLabel.setText("📝 Просмотр");
                }
            });
        }
        taskRow.add(descriptionLabel);

        // Колонка статуса
        JLabel statusLabel = new JLabel(getStatusDisplayName(task.getStatus()));
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusLabel.setForeground(getStatusColor(task.getStatus()));
        taskRow.add(statusLabel);

        // Колонка приоритета
        JLabel priorityLabel = new JLabel(getImportanceDisplayName(task.getImportance()));
        priorityLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        priorityLabel.setForeground(getImportanceColor(task.getImportance()));
        taskRow.add(priorityLabel);

        // Колонка дедлайна
        String deadline = task.getDeadline() != null ? task.getDeadline().toString() : "";
        String formattedDeadline = DeadlineUtils.formatDeadlineForDisplay(deadline);
        JLabel deadlineLabel = new JLabel(formattedDeadline);
        deadlineLabel.setFont(new Font("Arial", Font.BOLD, 12));
        deadlineLabel.setForeground(DeadlineUtils.getDeadlineColor(deadline));
        taskRow.add(deadlineLabel);

        // Колонка комментариев (кликабельная)
        int commentCount = task.getComments() != null ? task.getComments().size() : 0;
        JLabel commentsLabel = new JLabel(commentCount + " коммент.");
        commentsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        commentsLabel.setForeground(commentCount > 0 ? new Color(52, 152, 219) : Color.GRAY);

        // Делаем кликабельным только если есть комментарии
        if (commentCount > 0) {
            commentsLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            commentsLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    showTaskComments(task);
                }
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    commentsLabel.setForeground(new Color(41, 128, 185));
                    commentsLabel.setText("<html><u>" + commentCount + " коммент.</u></html>");
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    commentsLabel.setForeground(new Color(52, 152, 219));
                    commentsLabel.setText(commentCount + " коммент.");
                }
            });
        }
        taskRow.add(commentsLabel);

        // Колонка действий
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        actionPanel.setBackground(Color.WHITE);

        // Кнопка "На доработку" только для завершенных задач
        if ("ЗАВЕРШЕНА".equals(task.getStatus())) {
            JButton reworkButton = createReworkButton(task);
            actionPanel.add(reworkButton);
        } else {
            JLabel noActionLabel = new JLabel("-");
            noActionLabel.setForeground(Color.GRAY);
            noActionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            actionPanel.add(noActionLabel);
        }

        taskRow.add(actionPanel);
        parent.add(taskRow);
    }


    // Класс для фоновой панели как в UserApplicationFrame
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
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Декоративные элементы (пузырьки)
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



    // Метод для создания кнопок в современном стиле
    private JButton createModernButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(30, 144, 255));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(180, 45));
        button.setMaximumSize(new Dimension(180, 45));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }



    // Обновленные методы для диалогов (аналогично UserApplicationFrame)
    private JDialog createStyledDialog(String title, int width, int height) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(0, 0, new Color(25, 25, 35),
                        getWidth(), getHeight(), new Color(45, 30, 60));
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
                g2.setColor(new Color(155, 89, 182));
                g2.fillOval(-50, -50, 200, 200);
                g2.fillOval(getWidth() - 100, getHeight() - 150, 300, 300);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }
        };
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        dialog.setContentPane(backgroundPanel);
        return dialog;
    }

    private JLabel createDialogTitle(String text) {
        JLabel titleLabel = new JLabel(text, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        return titleLabel;
    }

    private JLabel createDialogText(String text, int fontSize) {
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, fontSize));
        textLabel.setForeground(new Color(220, 220, 220));
        return textLabel;
    }

    private JButton createDialogButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(backgroundColor);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1),
                BorderFactory.createEmptyBorder(12, 25, 12, 25)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(true);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });

        return button;
    }

    // Обновленный метод подтверждения выхода
    private void showLogoutConfirmationDialog() {
        JDialog confirmDialog = createStyledDialog("Подтверждение выхода", 500, 400);
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        JLabel iconLabel = new JLabel("🚪", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setForeground(new Color(241, 196, 15));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = createDialogTitle("Подтверждение выхода");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = createDialogText("Вы уверены, что хотите выйти из системы?", 14);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 0, 0));

        JButton yesButton = createDialogButton("Да, выйти", new Color(220, 53, 69));
        JButton noButton = createDialogButton("Нет, остаться", new Color(108, 117, 125));

        yesButton.addActionListener(e -> {
            confirmDialog.dispose();
            performLogout();
        });
        noButton.addActionListener(e -> confirmDialog.dispose());

        buttonPanel.add(noButton);
        buttonPanel.add(yesButton);

        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(25));
        contentPanel.add(buttonPanel);

        confirmDialog.add(contentPanel, BorderLayout.CENTER);
        confirmDialog.getRootPane().setDefaultButton(noButton);
        confirmDialog.setVisible(true);
    }

    // Обновленные методы для информационных сообщений
    private void showSuccessMessage(String message) {
        JDialog successDialog = createStyledDialog("Успех", 450, 250);
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        JLabel iconLabel = new JLabel("✅", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setForeground(new Color(46, 204, 113));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = createDialogTitle("Успешно!");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = createDialogText(message, 14);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton okButton = createDialogButton("OK", new Color(46, 204, 113));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.addActionListener(e -> successDialog.dispose());

        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(25));
        contentPanel.add(okButton);

        successDialog.add(contentPanel, BorderLayout.CENTER);
        successDialog.getRootPane().setDefaultButton(okButton);
        successDialog.setVisible(true);
    }

    private void showErrorMessage(String message) {
        JDialog errorDialog = createStyledDialog("Ошибка", 450, 250);
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        JLabel iconLabel = new JLabel("❌", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setForeground(new Color(231, 76, 60));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = createDialogTitle("Ошибка");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = createDialogText(message, 14);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton okButton = createDialogButton("OK", new Color(231, 76, 60));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.addActionListener(e -> errorDialog.dispose());

        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(25));
        contentPanel.add(okButton);

        errorDialog.add(contentPanel, BorderLayout.CENTER);
        errorDialog.getRootPane().setDefaultButton(okButton);
        errorDialog.setVisible(true);
    }


    private JButton createModernFilterButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 30));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }



    private void performLogout() {
        LoginFrame.clearToken();
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    private void showDashboard() {
        centerPanel.removeAll();
        JPanel welcomePanel = createModernWelcomePanel();
        centerPanel.add(welcomePanel, "welcome");
        cardLayout.show(centerPanel, "welcome");
        centerPanel.revalidate();
        centerPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            loadAdminStatistics();
        });
    }

    private void showAllTasks() {
        centerPanel.removeAll();
        JPanel allTasksPanel = loadAllTasksPanel();
        centerPanel.add(allTasksPanel, "alltasks");
        cardLayout.show(centerPanel, "alltasks");
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    private void initializeNotificationSystem() {
        notificationManager = new NotificationManager(this);
        notificationConsumer = new AdminNotificationConsumer(notificationManager);
        notificationConsumer.startConsuming();
        System.out.println("Notification system initialized");
    }

    private void onWindowClosing() {
        if (notificationConsumer != null) {
            notificationConsumer.stop();
        }
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }

    // Обновленный метод для создания кнопок с возможностью указания ширины
    private JButton createModernButton(String text, int width) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(30, 144, 255));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(width, 45));
        button.setMaximumSize(new Dimension(width, 45));
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Добавляем перенос текста для длинных надписей
        button.addHierarchyListener(e -> {
            if (button.getText().length() > 15) {
                button.setText("<html><center>" + text + "</center></html>");
            }
        });

        return button;
    }


    // Альтернативное решение для очень длинного текста кнопки
    private JButton createWideModernButton(String text) {
        JButton button = new JButton();
        button.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Уменьшаем шрифт для длинного текста
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(30, 144, 255));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(280, 45)); // Еще больше ширина
        button.setMaximumSize(new Dimension(280, 45));
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Используем HTML для переноса текста
        button.setText("<html><center>" + text + "</center></html>");

        return button;
    }

    private JButton createWideButton(String text, int width) {
        JButton button = new JButton();

        // Используем HTML для правильного отображения длинного текста
        String buttonText = "<html><div style='text-align:center; padding:5px;'>" + text + "</div></html>";
        button.setText(buttonText);

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(30, 144, 255));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(width, 40));
        button.setMinimumSize(new Dimension(width, 40));
        button.setMaximumSize(new Dimension(width, 40));

        // Убираем стандартные отступы, так как используем HTML-отступы
        button.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Эффект при наведении
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(button.getBackground().darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(30, 144, 255));
            }
        });

        return button;
    }

    // НОВЫЙ МЕТОД ДЛЯ КНОПОК НАВИГАЦИИ
    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(30, 144, 255));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        button.setContentAreaFilled(true);
        button.setOpaque(true);

        // Для длинного текста используем HTML с переносом
        if (text.length() > 15) {
            button.setText("<html><center>" + text + "</center></html>");
            button.setPreferredSize(new Dimension(180, 40));
        } else {
            button.setPreferredSize(new Dimension(120, 40));
        }

        button.setMaximumSize(button.getPreferredSize());
        button.setMinimumSize(button.getPreferredSize());

        // Эффект при наведении
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(button.getBackground().brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.getText().contains("Выйти")) {
                    button.setBackground(new Color(220, 53, 69));
                } else {
                    button.setBackground(new Color(30, 144, 255));
                }
            }
        });

        return button;
    }


    // ВАРИАНТ С ПОЛНЫМ ТЕКСТОМ ДЛЯ КНОПКИ
    private JButton createWideSimpleButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Чуть меньше шрифт
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(30, 144, 255));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(180, 40));
        return button;
    }


    // Кнопка с переносом текста
    private JButton createMultiLineButton(String htmlText) {
        JButton button = new JButton();
        button.setText("<html><center>" + htmlText + "</center></html>");
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(30, 144, 255));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(140, 50)); // Выше для двух строк
        button.setVerticalTextPosition(SwingConstants.CENTER);
        button.setHorizontalTextPosition(SwingConstants.CENTER);

        return button;
    }

    // Обычная кнопка
    private JButton createSimpleButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(30, 144, 255));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(120, 40));

        return button;
    }

    // КАРТОЧКИ КАК У ПОЛЬЗОВАТЕЛЕЙ - ТОЧНАЯ КОПИЯ
    private JPanel createModernStatCard(String title, String value, Color color) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Полупрозрачный фон карточки как у пользователя
                g2.setColor(new Color(255, 255, 255, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Градиентная рамка как у пользователя
                GradientPaint gradient = new GradientPaint(0, 0, color, getWidth(), getHeight(),
                        new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
                g2.setPaint(gradient);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 20, 20);
            }
        };

        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(200, 120)); // Размер как у пользователя
        card.setMaximumSize(new Dimension(200, 120));
        card.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        // Заголовок как у пользователя
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(220, 220, 220));

        // Значение как у пользователя
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(Color.WHITE);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        // Эффекты при наведении как у пользователя
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return card;
    }

    // ПАНЕЛЬ СТАТИСТИКИ КАК У ПОЛЬЗОВАТЕЛЕЙ
    private JPanel createModernStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));

        JLabel statsTitle = new JLabel("Общая статистика системы", SwingConstants.CENTER);
        statsTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        statsTitle.setForeground(Color.WHITE);
        statsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel cardsContainer = new JPanel(new GridLayout(1, 4, 20, 0));
        cardsContainer.setOpaque(false);
        cardsContainer.setMaximumSize(new Dimension(900, 140));
        cardsContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Карточки как у пользователя
        cardsContainer.add(createModernStatCard("Всего задач", "0", new Color(70, 130, 180)));
        cardsContainer.add(createModernStatCard("Активных", "0", new Color(255, 165, 0)));
        cardsContainer.add(createModernStatCard("Завершено", "0", new Color(50, 205, 50)));
        cardsContainer.add(createModernStatCard("Пользователей", "0", new Color(155, 89, 182)));

        statsPanel.add(statsTitle);
        statsPanel.add(Box.createVerticalStrut(30));
        statsPanel.add(cardsContainer);

        return statsPanel;
    }

    // Обновленный метод createModernWelcomePanel для использования на темном фоне
    private JPanel createModernWelcomePanel() {
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setOpaque(false); // Прозрачный для темного градиента
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(60, 80, 60, 80));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Заголовок как в UserApplicationFrame
        JLabel titleLabel = new JLabel("Добро пожаловать в панель администратора!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Описание
        JTextArea descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setOpaque(false);
        descriptionArea.setForeground(new Color(220, 220, 220));
        descriptionArea.setText("\nЗдесь вы можете управлять всеми задачами системы, создавать новые задания, " +
                "назначать их пользователям и отслеживать прогресс выполнения. " +
                "Используйте меню выше для навигации по разделам административной панели.\n");
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(30, 80, 40, 80));
        descriptionArea.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Статистика
        JPanel statsPanel = createAdminStatsPanel();

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(descriptionArea);
        contentPanel.add(Box.createVerticalStrut(50));
        contentPanel.add(statsPanel);

        welcomePanel.add(contentPanel, BorderLayout.CENTER);
        return welcomePanel;
    }



    private JPanel createStatCard(String title, String value, Color color, String icon) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Полупрозрачный фон карточки
                g2.setColor(new Color(255, 255, 255, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Градиентная рамка
                GradientPaint gradient = new GradientPaint(0, 0, color, getWidth(), getHeight(),
                        new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
                g2.setPaint(gradient);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 20, 20);

                g2.dispose();
            }
        };

        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(200, 170));
        card.setMaximumSize(new Dimension(200, 150));
        card.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        card.setOpaque(false); // Прозрачный для корректной работы custom paintComponent

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setOpaque(false); // Прозрачный
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconLabel.setForeground(color);
        iconLabel.setOpaque(false); // Прозрачный

        topPanel.add(iconLabel);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 36));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setOpaque(false); // Прозрачный

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(108, 117, 125));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setOpaque(false); // Прозрачный

        card.add(topPanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);

        // Убрал MouseListener, чтобы избежать артефактов от изменений border/background

        return card;
    }



    private JPanel createStatCard(String title, String value, Color color) {  // Убрал параметр icon, так как смайлики лишние
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Полупрозрачный фон карточки
                g2.setColor(new Color(255, 255, 255, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Градиентная рамка
                GradientPaint gradient = new GradientPaint(0, 0, color, getWidth(), getHeight(),
                        new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
                g2.setPaint(gradient);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 20, 20);

                g2.dispose();
            }
        };

        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(200, 170));
        card.setMaximumSize(new Dimension(200, 150));
        card.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        card.setOpaque(false); // Прозрачный для корректной работы custom paintComponent

        // Заголовок наверху
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(108, 117, 125));
        titleLabel.setOpaque(false); // Прозрачный

        // Значение в центре
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 36));
        valueLabel.setForeground(color);
        valueLabel.setOpaque(false); // Прозрачный

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // Обновленный метод createAdminStatsPanel - убрал иконки из вызовов createStatCard
    private JPanel createAdminStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setOpaque(false); // Прозрачный для темного фона

        JLabel statsTitle = new JLabel("Общая статистика системы", SwingConstants.CENTER);
        statsTitle.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Изменен шрифт для consistency
        statsTitle.setForeground(Color.WHITE); // Белый текст на темном фоне
        statsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel cardsContainer = new JPanel();
        cardsContainer.setLayout(new GridLayout(1, 4, 20, 0));
        cardsContainer.setOpaque(false); // Прозрачный
        cardsContainer.setMaximumSize(new Dimension(1000, 190));
        cardsContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Заглушки для статистики - в реальной системе здесь будут реальные данные
        // Убрал иконки из вызовов
        cardsContainer.add(createStatCard("Всего задач", "0", new Color(70, 130, 180)));
        cardsContainer.add(createStatCard("Активных", "0", new Color(255, 165, 0)));
        cardsContainer.add(createStatCard("Завершено", "0", new Color(46, 204, 113)));
        cardsContainer.add(createStatCard("Пользователей", "0", new Color(155, 89, 182)));

        statsPanel.add(statsTitle);
        statsPanel.add(Box.createVerticalStrut(20));
        statsPanel.add(cardsContainer);

        return statsPanel;
    }


    private JPanel loadAllTasksPanel() {
        JPanel allTasksPanel = new JPanel(new BorderLayout());
        allTasksPanel.setBackground(Color.WHITE); // Белый фон основной панели
        allTasksPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JLabel titleLabel = new JLabel("Все задачи системы", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80)); // Темный цвет текста для белого фона
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel filtersPanel = createAdminFiltersPanel();
        allTasksPanel.add(filtersPanel, BorderLayout.NORTH);

        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(Color.WHITE); // Белый фон панели загрузки
        JLabel loadingLabel = new JLabel("Загрузка всех задач системы...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        loadingLabel.setForeground(new Color(100, 100, 100)); // Серый цвет текста
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);

        allTasksPanel.add(titleLabel, BorderLayout.NORTH);
        allTasksPanel.add(loadingPanel, BorderLayout.CENTER);
        loadAllTasksFromServer(allTasksPanel, loadingPanel);

        return allTasksPanel;
    }


    private JPanel createAdminFiltersPanel() {
        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filtersPanel.setBackground(Color.WHITE); // Белый фон панели фильтров
        filtersPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)), // Светлая серая линия снизу
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        filtersPanel.setPreferredSize(new Dimension(getWidth(), 80));

        JLabel statusLabel = createFilterLabel("Статус:");
        String[] statusOptions = {"Все статусы", "НЕ_НАЧАТА", "В_РАБОТЕ", "ЗАВЕРШЕНА", "НА_ДОРАБОТКЕ"};
        adminStatusFilter = createModernComboBox(statusOptions);

        JLabel importanceLabel = createFilterLabel("Важность:");
        String[] importanceOptions = {"Все приоритеты", "СРОЧНАЯ", "НАДО_ПОТОРОПИТЬСЯ", "МОЖЕТ_ПОДОЖДАТЬ"};
        adminImportanceFilter = createModernComboBox(importanceOptions);

        JLabel sortLabel = createFilterLabel("Сортировка:");
        String[] sortOptions = {"Без сортировки", "Дедлайн ↑", "Дедлайн ↓"};
        adminSortFilter = createModernComboBox(sortOptions);

        adminApplyFiltersButton = createModernFilterButton("Применить", new Color(52, 152, 219));
        adminResetFiltersButton = createModernFilterButton("Сбросить", new Color(108, 117, 125));

        adminApplyFiltersButton.addActionListener(e -> applyAdminFilters());
        adminResetFiltersButton.addActionListener(e -> resetAdminFilters());

        filtersPanel.add(statusLabel);
        filtersPanel.add(adminStatusFilter);
        filtersPanel.add(Box.createHorizontalStrut(10));
        filtersPanel.add(importanceLabel);
        filtersPanel.add(adminImportanceFilter);
        filtersPanel.add(Box.createHorizontalStrut(10));
        filtersPanel.add(sortLabel);
        filtersPanel.add(adminSortFilter);
        filtersPanel.add(Box.createHorizontalStrut(20));
        filtersPanel.add(adminApplyFiltersButton);
        filtersPanel.add(adminResetFiltersButton);

        return filtersPanel;
    }

    private JLabel createFilterLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(44, 62, 80)); // Темный цвет текста для белого фона
        return label;
    }

    private JComboBox<String> createModernComboBox(String[] options) {
        JComboBox<String> comboBox = new JComboBox<>(options);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboBox.setBackground(Color.WHITE); // Белый фон
        comboBox.setForeground(new Color(44, 62, 80)); // Темный текст
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), // Светлая серая рамка
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        comboBox.setFocusable(false);
        comboBox.setMaximumRowCount(10);
        comboBox.setPreferredSize(new Dimension(140, 30));
        return comboBox;
    }

    private void loadAllTasksFromServer(JPanel allTasksPanel, JPanel loadingPanel) {
        new Thread(() -> {
            try {
                List<User> users = getAllUsersWithTasks();
                SwingUtilities.invokeLater(() -> {
                    loadingPanel.removeAll();
                    allTasksPanel.removeAll();
                    allTasksPanel.setLayout(new BorderLayout());
                    allTasksPanel.setBackground(Color.WHITE); // Убедитесь, что фон белый

                    JLabel titleLabel = new JLabel("Все задачи системы", SwingConstants.CENTER);
                    titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
                    titleLabel.setForeground(new Color(44, 62, 80));
                    titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    allTasksPanel.add(titleLabel, BorderLayout.NORTH);

                    // Создаем панель фильтров
                    JPanel filtersPanel = createAdminFiltersPanel();
                    allTasksPanel.add(filtersPanel, BorderLayout.NORTH);

                    if (users != null && !users.isEmpty()) {
                        // ... остальной код без изменений
                        originalAllAdminTasks = new ArrayList<>();
                        for (User user : users) {
                            if (user.getTasks() != null) {
                                originalAllAdminTasks.addAll(user.getTasks());
                            }
                        }

                        allAdminTasks = new ArrayList<>(originalAllAdminTasks);
                        System.out.println("DEBUG: Loaded " + originalAllAdminTasks.size() + " tasks for admin");

                        // Создаем панель для отображения задач с белым фоном
                        JPanel tasksContentPanel = new JPanel(new BorderLayout());
                        tasksContentPanel.setBackground(Color.WHITE);
                        displayAllAdminTasks(tasksContentPanel, users);
                        allTasksPanel.add(tasksContentPanel, BorderLayout.CENTER);
                    } else {
                        showNoTasksMessage(allTasksPanel);
                        originalAllAdminTasks = new ArrayList<>();
                        allAdminTasks = new ArrayList<>();
                    }
                    allTasksPanel.revalidate();
                    allTasksPanel.repaint();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    showErrorPanel(allTasksPanel, "Ошибка загрузки: " + e.getMessage());
                    allTasksPanel.revalidate();
                    allTasksPanel.repaint();
                });
            }
        }).start();
    }

    private void displayAllAdminTasks(JPanel allTasksPanel, List<User> users) {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE); // Белый фон контента

        contentPanel.add(createAdminTableHeader());
        contentPanel.add(Box.createVerticalStrut(10));

        int totalTasks = 0;
        for (User user : users) {
            if (user.getTasks() != null) {
                System.out.println("DEBUG: Displaying tasks for user: " + user.getUsername() +
                        " (" + user.getTasks().size() + " tasks)");
                for (Task task : user.getTasks()) {
                    addAdminTaskRow(contentPanel, task, user.getUsername());
                    totalTasks++;
                    System.out.println("DEBUG: Added task: " + task.getTitle() +
                            " for user: " + user.getUsername());
                }
            }
        }

        System.out.println("DEBUG: Total admin tasks displayed: " + totalTasks);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Убираем рамку скролл-панели
        scrollPane.setBackground(Color.WHITE); // Белый фон скролл-панели
        scrollPane.getViewport().setBackground(Color.WHITE); // Белый фон области просмотра
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        allTasksPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createAdminTableHeader() {
        JPanel headerPanel = new JPanel(new GridLayout(1, 8, 10, 5));
        headerPanel.setBackground(new Color(240, 240, 240)); // Светло-серый фон заголовка
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        String[] headers = {"Пользователь", "Название задачи", "Описание", "Статус", "Срочность", "Дедлайн", "Комментарии", "Действия"};
        for (String header : headers) {
            JLabel headerLabel = new JLabel(header);
            headerLabel.setFont(new Font("Arial", Font.BOLD, 12));
            headerLabel.setForeground(new Color(44, 62, 80)); // Темный цвет текста
            headerPanel.add(headerLabel);
        }

        return headerPanel;
    }

    private void showUserManagement() {
        JDialog userManagementDialog = new JDialog(this, "Управление пользователями", true);
        userManagementDialog.setSize(1200, 800);
        userManagementDialog.setLocationRelativeTo(this);
        userManagementDialog.setLayout(new BorderLayout());
        userManagementDialog.setResizable(true);

        // Устанавливаем белый фон для всего диалога
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Заголовок
        JPanel headerPanel = createUserManagementHeader();

        // Панель с пользователями и задачами
        JSplitPane splitPane = createUserManagementSplitPane();

        // Панель действий
        JPanel actionPanel = createUserManagementActionPanel(userManagementDialog);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);

        userManagementDialog.add(mainPanel, BorderLayout.CENTER);
        userManagementDialog.setVisible(true);
    }

    private JPanel createUserManagementHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel iconLabel = new JLabel("👥", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setForeground(new Color(52, 152, 219)); // Синий цвет вместо фиолетового
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Управление пользователями", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80)); // Темный цвет текста
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Управление пользователями и их задачами", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(127, 140, 141)); // Серый цвет
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(iconLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(subtitleLabel);

        return headerPanel;
    }

    private JSplitPane createUserManagementSplitPane() {
        // Панель пользователей
        JPanel usersPanel = new JPanel(new BorderLayout());
        usersPanel.setBackground(Color.WHITE);
        usersPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), // Светлая серая рамка
                "Пользователи",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 12),
                new Color(44, 62, 80) // Темный цвет текста
        ));

        DefaultListModel<String> usersListModel = new DefaultListModel<>();
        JList<String> usersList = new JList<>(usersListModel);
        usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersList.setFont(new Font("Arial", Font.PLAIN, 12));
        usersList.setBackground(Color.WHITE);
        usersList.setForeground(new Color(44, 62, 80));
        usersList.setSelectionBackground(new Color(52, 152, 219));
        usersList.setSelectionForeground(Color.WHITE);

        JScrollPane usersScrollPane = new JScrollPane(usersList);
        usersScrollPane.setBorder(BorderFactory.createEmptyBorder());
        usersScrollPane.getViewport().setBackground(Color.WHITE);
        usersPanel.add(usersScrollPane, BorderLayout.CENTER);

        // Панель задач выбранного пользователя
        JPanel tasksPanel = new JPanel(new BorderLayout());
        tasksPanel.setBackground(Color.WHITE);
        tasksPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), // Светлая серая рамка
                "Задачи пользователя",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 12),
                new Color(44, 62, 80) // Темный цвет текста
        ));

        // Используем таблицу вместо списка для задач
        String[] columnNames = {"Название", "Статус", "Важность", "Дедлайн"};
        DefaultTableModel tasksTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tasksTable = new JTable(tasksTableModel);
        tasksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tasksTable.setFont(new Font("Arial", Font.PLAIN, 12));
        tasksTable.setBackground(Color.WHITE);
        tasksTable.setForeground(new Color(44, 62, 80));
        tasksTable.setRowHeight(25);
        tasksTable.getTableHeader().setBackground(new Color(240, 240, 240));
        tasksTable.getTableHeader().setForeground(new Color(44, 62, 80));
        tasksTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tasksTable.setSelectionBackground(new Color(52, 152, 219));
        tasksTable.setSelectionForeground(Color.WHITE);

        // Добавляем кастомный рендерер для цветов дедлайна
        tasksTable.setDefaultRenderer(Object.class, new DeadlineTableCellRenderer());

        // Настраиваем ширину колонок
        tasksTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Название
        tasksTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Статус
        tasksTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Важность
        tasksTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Дедлайн

        JScrollPane tasksScrollPane = new JScrollPane(tasksTable);
        tasksScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tasksScrollPane.getViewport().setBackground(Color.WHITE);
        tasksPanel.add(tasksScrollPane, BorderLayout.CENTER);

        // Загрузка пользователей
        loadUsers(usersListModel, tasksTableModel, tasksTable);

        // Обработчик выбора пользователя
        usersList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = usersList.getSelectedValue();
                if (selectedUser != null && !selectedUser.startsWith("Пользователи не найдены") && !selectedUser.startsWith("Ошибка")) {
                    loadUserTasks(selectedUser, tasksTableModel);
                }
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, usersPanel, tasksPanel);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.3);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        return splitPane;
    }


    private JPanel createUserManagementActionPanel(JDialog parentDialog) {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton deleteUserButton = new JButton("Удалить пользователя");
        JButton deleteTaskButton = new JButton("Удалить задачу");
        JButton reassignTaskButton = new JButton("Переназначить задачу");
        JButton editTaskButton = new JButton("Редактировать задачу");
        JButton closeButton = new JButton("Закрыть");

        // Стилизуем кнопки в соответствии с общим дизайном
        styleManagementButton(deleteUserButton, new Color(231, 76, 60)); // Красный
        styleManagementButton(deleteTaskButton, new Color(231, 76, 60)); // Красный
        styleManagementButton(reassignTaskButton, new Color(52, 152, 219)); // Синий
        styleManagementButton(editTaskButton, new Color(241, 196, 15));   // Желтый
        styleManagementButton(closeButton, new Color(108, 117, 125));     // Серый

        // Обработчики действий с передачей parentDialog
        deleteUserButton.addActionListener(e -> deleteSelectedUser(parentDialog));
        deleteTaskButton.addActionListener(e -> deleteSelectedTask(parentDialog));
        reassignTaskButton.addActionListener(e -> reassignSelectedTask(parentDialog));
        editTaskButton.addActionListener(e -> editSelectedTask(parentDialog));
        closeButton.addActionListener(e -> parentDialog.dispose());

        actionPanel.add(deleteUserButton);
        actionPanel.add(deleteTaskButton);
        actionPanel.add(reassignTaskButton);
        actionPanel.add(editTaskButton);
        actionPanel.add(closeButton);

        return actionPanel;
    }

    private void styleManagementButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
    }

    private void createNewTask() {
        // Создаем диалог как модальный
        AssignTaskDialog dialog = new AssignTaskDialog(this, authToken);

        // Обновляем стиль диалога создания задачи
        updateAssignTaskDialogStyle(dialog);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        refreshAllData();
    }

    private void updateAssignTaskDialogStyle(JDialog dialog) {
        // Устанавливаем белый фон для всего диалога
        dialog.getContentPane().setBackground(Color.WHITE);

        // Рекурсивно обновляем все компоненты
        updateComponentStyles(dialog.getContentPane());
    }

    private void updateComponentStyles(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                panel.setBackground(Color.WHITE);
                panel.setForeground(new Color(44, 62, 80));
                updateComponentStyles(panel);
            } else if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                label.setForeground(new Color(44, 62, 80));
            } else if (comp instanceof JTextField) {
                JTextField textField = (JTextField) comp;
                textField.setBackground(Color.WHITE);
                textField.setForeground(new Color(44, 62, 80));
                textField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200)),
                        BorderFactory.createEmptyBorder(5, 8, 5, 8)
                ));
            } else if (comp instanceof JTextArea) {
                JTextArea textArea = (JTextArea) comp;
                textArea.setBackground(Color.WHITE);
                textArea.setForeground(new Color(44, 62, 80));
                textArea.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200)),
                        BorderFactory.createEmptyBorder(5, 8, 5, 8)
                ));
            } else if (comp instanceof JComboBox) {
                JComboBox<?> comboBox = (JComboBox<?>) comp;
                comboBox.setBackground(Color.WHITE);
                comboBox.setForeground(new Color(44, 62, 80));
                comboBox.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200)),
                        BorderFactory.createEmptyBorder(5, 8, 5, 8)
                ));
            } else if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                if (!button.getBackground().equals(new Color(220, 53, 69))) { // Не перекрашиваем красные кнопки
                    button.setBackground(new Color(52, 152, 219));
                }
                button.setForeground(Color.WHITE);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(button.getBackground().darker(), 1),
                        BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
        }
    }

    private void loadUsers(DefaultListModel<String> usersListModel, DefaultTableModel tasksTableModel, JTable tasksTable) {
        showLoadingDialog("Загрузка пользователей...");

        new Thread(() -> {
            try {
                List<User> users = getAllUsersWithTasks();
                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();
                    usersListModel.clear();
                    tasksTableModel.setRowCount(0);

                    if (users != null && !users.isEmpty()) {
                        for (User user : users) {
                            usersListModel.addElement(user.getUsername());
                        }
                        System.out.println("DEBUG: Loaded " + users.size() + " users");
                    } else {
                        usersListModel.addElement("Пользователи не найдены");
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    hideLoadingDialog();
                    usersListModel.clear();
                    usersListModel.addElement("Ошибка загрузки: " + e.getMessage());
                    showErrorMessage("Ошибка загрузки пользователей: " + e.getMessage());
                });
            }
        }).start();
    }

    private JPanel createModernTopPanel(String username) {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Левая панель с информацией
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);

        // Панель информации
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Панель администратора");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userInfoLabel = new JLabel("Вы вошли как: " + username + " (ADMIN)");
        userInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userInfoLabel.setForeground(new Color(200, 200, 200));
        userInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(welcomeLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(userInfoLabel);

        // Панель кнопок - ИСПРАВЛЕННАЯ ВЕРСИЯ
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Создаем кнопки одинакового размера
        JButton dashboardButton = createUniformButton("Главная");
        JButton allTasksButton = createUniformButton("Все задачи");
        JButton manageUsersButton = createUniformButton("Управление ");
        JButton createTaskButton = createUniformButton("Создать задачу");

        dashboardButton.addActionListener(e -> showDashboard());
        allTasksButton.addActionListener(e -> showAllTasks());
        manageUsersButton.addActionListener(e -> showUserManagement());
        createTaskButton.addActionListener(e -> createNewTask());

        buttonPanel.add(dashboardButton);
        buttonPanel.add(allTasksButton);
        buttonPanel.add(manageUsersButton);
        buttonPanel.add(createTaskButton);

        leftPanel.add(infoPanel, BorderLayout.NORTH);
        leftPanel.add(buttonPanel, BorderLayout.CENTER);

        // Правая панель с кнопкой выхода
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);

        JButton logoutButton = createUniformButton("Выйти");
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        logoutButton.addActionListener(e -> showLogoutConfirmationDialog());

        rightPanel.add(logoutButton);

        topPanel.add(leftPanel, BorderLayout.CENTER);
        topPanel.add(rightPanel, BorderLayout.EAST);

        return topPanel;
    }

    // НОВЫЙ МЕТОД - создает кнопки одинакового размера
    private JButton createUniformButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(30, 144, 255));
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setContentAreaFilled(true);
        button.setOpaque(true);

        // Устанавливаем фиксированный размер для всех кнопок
        Dimension buttonSize = new Dimension(180, 42);
        button.setPreferredSize(buttonSize);
        button.setMinimumSize(buttonSize);
        button.setMaximumSize(buttonSize);

        // Красивая рамка с закругленными углами
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Эффекты при наведении
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(button.getBackground().brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.getText().equals("Выйти")) {
                    button.setBackground(new Color(220, 53, 69));
                } else {
                    button.setBackground(new Color(30, 144, 255));
                }
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(button.getBackground().darker());
            }
        });

        return button;
    }

    // АЛЬТЕРНАТИВНЫЙ ВАРИАНТ - если нужны кнопки с переносом текста для длинных названий
    private JButton createUniformButtonWithWrap(String text) {
        JButton button = new JButton();

        // Для длинного текста используем HTML с переносом
        if (text.length() > 15) {
            // Разбиваем длинный текст на две строки
            String[] words = text.split(" ");
            if (words.length >= 2) {
                // Пробуем разбить по первому пробелу
                String firstLine = words[0];
                String secondLine = text.substring(firstLine.length()).trim();
                button.setText("<html><center>" + firstLine + "<br>" + secondLine + "</center></html>");
            } else {
                button.setText("<html><center>" + text + "</center></html>");
            }
        } else {
            button.setText(text);
        }

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(30, 144, 255));
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setContentAreaFilled(true);
        button.setOpaque(true);

        // Немного выше для текста в две строки
        Dimension buttonSize = new Dimension(180, 48);
        button.setPreferredSize(buttonSize);
        button.setMinimumSize(buttonSize);
        button.setMaximumSize(buttonSize);

        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // Эффекты при наведении
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(button.getBackground().brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.getText().contains("Выйти")) {
                    button.setBackground(new Color(220, 53, 69));
                } else {
                    button.setBackground(new Color(30, 144, 255));
                }
            }
        });

        return button;
    }


}