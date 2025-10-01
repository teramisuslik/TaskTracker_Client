package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

class UserApplicationFrame extends JFrame {
    private String authToken;
    private Map<String, Object> userInfo;
    private JPanel centerPanel;
    private CardLayout cardLayout;
    private JPanel tasksPanel;
    private String username;
    private List<Task> userTasks;
    private List<Task> allUsersTasks;
    private JComboBox<String> statusFilter;
    private JComboBox<String> importanceFilter;
    private JComboBox<String> sortFilter;
    private JButton applyFiltersButton;
    private JButton resetFiltersButton;
    private List<Task> originalUserTasks; // сохраняем оригинальный список задач
    private JComboBox<String> allTasksStatusFilter;
    private JComboBox<String> allTasksImportanceFilter;
    private JComboBox<String> allTasksSortFilter;
    private JButton allTasksApplyFiltersButton;
    private JButton allTasksResetFiltersButton;
    private List<Task> originalAllUsersTasks;

    private NotificationManager notificationManager;
    private UserNotificationConsumer notificationConsumer;


    public UserApplicationFrame(String token, Map<String, Object> userInfo) {
        this.authToken = token;
        this.userInfo = userInfo;
        this.username = extractUsernameFromUserInfo(userInfo);

        System.out.println("DEBUG: UserApplicationFrame created for user: " + this.username);

        setTitle("Личный кабинет - " + username);
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        JPanel topPanel = createModernTopPanel();
        System.out.println("DEBUG: Top panel created with " + topPanel.getComponentCount() + " components");

        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.setOpaque(false);

        JPanel welcomePanel = createModernWelcomePanel();
        tasksPanel = new JPanel(new BorderLayout());
        tasksPanel.setOpaque(false);

        centerPanel.add(welcomePanel, "welcome");
        centerPanel.add(tasksPanel, "tasks");

        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(centerPanel, BorderLayout.CENTER);

        // Добавляем все в слои
        layeredPane.add(backgroundPanel, Integer.valueOf(0));
        layeredPane.add(contentPane, Integer.valueOf(1));

        setContentPane(layeredPane);

        // Инициализация системы уведомлений
        initializeNotificationSystem();

        loadUserTasksForStatistics();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                onWindowClosing();
            }
        });
    }


    private JPanel createWelcomePanel() {
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(new Color(248, 249, 250));
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 40, 40));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(248, 249, 250));

        JLabel titleLabel = new JLabel("Добро пожаловать в ваш личный кабинет!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(new Color(33, 37, 41));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 16));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(new Color(248, 249, 250));
        descriptionArea.setText("\nЗдесь вы можете управлять своими задачами, просматривать прогресс " +
                "и редактировать профиль. Система поможет вам эффективно организовать вашу работу " +
                "и отслеживать выполнение поставленных целей.\n");
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(20, 50, 30, 50));

        JPanel statsPanel = createStatsPanel();

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(descriptionArea);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(statsPanel);

        welcomePanel.add(contentPanel, BorderLayout.CENTER);
        return welcomePanel;
    }

    private JPanel createStatCard(String title, String value, Color color, String icon) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.brighter(), 2),
                BorderFactory.createEmptyBorder(20, 15, 20, 15)
        ));
        card.setPreferredSize(new Dimension(200, 170));
        card.setMaximumSize(new Dimension(200, 150));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconLabel.setForeground(color);
        topPanel.add(iconLabel);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 36));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(108, 117, 125));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(topPanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(color.brighter().brighter());
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(color.darker(), 2),
                        BorderFactory.createEmptyBorder(20, 15, 20, 15)
                ));
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(color.brighter(), 2),
                        BorderFactory.createEmptyBorder(20, 15, 20, 15)
                ));
            }
        });

        return card;
    }

    private JPanel createLeftInfoPanel() {
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Личный кабинет", SwingConstants.LEFT);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(44, 62, 80));
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userInfoLabel = new JLabel("Добро пожаловать, " + username + "!", SwingConstants.LEFT);
        userInfoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userInfoLabel.setForeground(new Color(127, 140, 141));
        userInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(welcomeLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(userInfoLabel);
        leftPanel.add(Box.createVerticalStrut(15));

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        userPanel.setOpaque(false);
        userPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton dashboardButton = new JButton("Главная");
        JButton myTasksButton = new JButton("Мои задачи");
        JButton allTasksButton = new JButton("Задачи других пользователей");
        JButton profileButton = new JButton("Мой профиль");

        styleDashboardButton(dashboardButton);
        styleUserButton(myTasksButton);
        styleUserButton(allTasksButton);
        styleUserButton(profileButton);

        dashboardButton.addActionListener(e -> showDashboard());
        myTasksButton.addActionListener(e -> showMyTasks());
        allTasksButton.addActionListener(e -> showAllUsersTasks());
        profileButton.addActionListener(e -> showMyProfile());

        userPanel.add(dashboardButton);
        userPanel.add(myTasksButton);
        userPanel.add(allTasksButton);
        userPanel.add(profileButton);

        leftPanel.add(userPanel);
        return leftPanel;
    }

    private void displayTasks(JPanel tasksPanel, List<Task> tasks, boolean showUsername) {
        JPanel tasksContentPanel = new JPanel();
        tasksContentPanel.setLayout(new BoxLayout(tasksContentPanel, BoxLayout.Y_AXIS));
        tasksContentPanel.setBackground(Color.WHITE);

        tasksContentPanel.add(createTableHeader(showUsername));
        tasksContentPanel.add(Box.createVerticalStrut(10));

        for (Task task : tasks) {
            addTaskRow(tasksContentPanel, task, showUsername, null);
        }

        JScrollPane scrollPane = new JScrollPane(tasksContentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tasksPanel.add(scrollPane, BorderLayout.CENTER);
    }



    private List<String> splitJsonObjects(String json) {
        List<String> objects = new ArrayList<>();
        int start = -1;
        int braceCount = 0;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                braceCount++;
                if (braceCount == 1) start = i;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0 && start != -1) {
                    objects.add(json.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return objects;
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
            System.out.println("DEBUG: Error parsing comments: " + e.getMessage());
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
            System.out.println("DEBUG: Error parsing comment: " + e.getMessage());
        }
        return null;
    }

    // UI Helper methods
    private void styleUserButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(46, 204, 113));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleDashboardButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(52, 152, 219));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }


    private void performLogout() {
        LoginFrame.clearToken();
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    private Map<String, Object> getUserInfo() {
        try {
            String encodedUsername = java.net.URLEncoder.encode(this.username, "UTF-8");
            String url = "http://localhost:8080/userwithouttasks?username=" + encodedUsername;

            System.out.println("DEBUG: Requesting user info from: " + url);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new java.net.URI(url))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("DEBUG: User info response status: " + response.statusCode());
            System.out.println("DEBUG: User info response body: " + response.body());

            if (response.statusCode() == 200) {
                return parseUserInfoFromJson(response.body());
            } else {
                throw new RuntimeException("HTTP error: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Exception in getUserInfo: " + e.getMessage());
            throw new RuntimeException("Ошибка при получении информации о пользователе: " + e.getMessage());
        }
    }

    private Map<String, Object> parseUserInfoFromJson(String json) {
        try {
            Map<String, Object> userInfo = new java.util.HashMap<>();

            if (json.contains("\"username\"")) {
                String username = extractValueFromJson(json, "username");
                String role = extractValueFromJson(json, "role");

                userInfo.put("username", username);
                userInfo.put("role", role);
            }

            return userInfo;
        } catch (Exception e) {
            System.out.println("DEBUG: Error parsing user info: " + e.getMessage());
            return new java.util.HashMap<>();
        }
    }

    private String extractValueFromJson(String json, String key) {
        try {
            String searchStr = "\"" + key + "\":\"";
            int start = json.indexOf(searchStr);
            if (start == -1) return null;

            start += searchStr.length();
            int end = json.indexOf("\"", start);
            if (end == -1) return null;

            return json.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }

    private String getRoleDisplayName(String role) {
        switch (role.toUpperCase()) {
            case "ADMIN": return "Администратор";
            case "USER": return "Пользователь";
            default: return role;
        }
    }

    private int getTasksCountByStatusFromAll(String status) {
        if (allUsersTasks == null) return 0;
        return (int) allUsersTasks.stream()
                .filter(task -> task.getStatus() != null && task.getStatus().equals(status))
                .count();
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

            System.out.println("DEBUG: getAllUsersWithTasks Status code: " + response.statusCode());
            System.out.println("DEBUG: getAllUsersWithTasks Response body: " + response.body());

            if (response.statusCode() == 200) {
                List<User> users = parseUsersFromJson(response.body());
                System.out.println("DEBUG: Parsed " + (users != null ? users.size() : 0) + " users");
                return users;
            } else {
                throw new RuntimeException("HTTP error: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Exception in getAllUsersWithTasks: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Ошибка при получении всех пользователей: " + e.getMessage());
        }
    }

    private List<User> parseUsersFromJson(String json) {
        try {
            List<User> users = new ArrayList<>();
            if (json == null || json.trim().isEmpty()) {
                System.out.println("DEBUG: Empty JSON response");
                return users;
            }

            System.out.println("DEBUG: Raw users JSON: " + json);

            // Убираем внешние квадратные скобки если они есть
            String content = json.trim();
            if (content.startsWith("[") && content.endsWith("]")) {
                content = content.substring(1, content.length() - 1).trim();
            }

            // Разделяем пользователей по },{
            String[] userStrings = content.split("\\},\\s*\\{");
            System.out.println("DEBUG: Found " + userStrings.length + " user strings");

            for (int i = 0; i < userStrings.length; i++) {
                String userStr = userStrings[i];
                if (i > 0) userStr = "{" + userStr;
                if (i < userStrings.length - 1) userStr = userStr + "}";

                User user = parseSingleUserFromAllUsers(userStr);
                if (user != null && user.getUsername() != null) {
                    users.add(user);
                    System.out.println("DEBUG: Added user: " + user.getUsername() + " with " +
                            (user.getTasks() != null ? user.getTasks().size() : 0) + " tasks");
                }
            }

            return users;
        } catch (Exception e) {
            System.out.println("DEBUG: Error parsing users: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private User getUserWithTasks() {
        try {
            // Используем текущего пользователя (this.username), а не захардкоженное значение
            String encodedUsername = java.net.URLEncoder.encode(this.username, "UTF-8");
            String url = "http://localhost:8080/user?username=" + encodedUsername;

            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new java.net.URI(url))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("DEBUG: getUserWithTasks for user: " + this.username);
            System.out.println("DEBUG: getUserWithTasks response: " + response.statusCode());
            System.out.println("DEBUG: getUserWithTasks body: " + response.body());

            if (response.statusCode() == 200) {
                return parseUserFromJson(response.body());
            } else {
                throw new RuntimeException("HTTP error: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Exception in getUserWithTasks: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Ошибка при получении задач: " + e.getMessage());
        }
    }

    private String extractUsernameFromUserInfo(Map<String, Object> userInfo) {
        if (userInfo == null) {
            System.out.println("DEBUG: userInfo is null");
            return "user";
        }

        System.out.println("DEBUG: userInfo contents: " + userInfo);

        if (userInfo.containsKey("username")) {
            String username = userInfo.get("username").toString();
            System.out.println("DEBUG: Found username in userInfo: " + username);
            return username;
        }
        if (userInfo.containsKey("sub")) {
            String username = userInfo.get("sub").toString();
            System.out.println("DEBUG: Found sub in userInfo: " + username);
            return username;
        }
        if (userInfo.containsKey("preferred_username")) {
            String username = userInfo.get("preferred_username").toString();
            System.out.println("DEBUG: Found preferred_username in userInfo: " + username);
            return username;
        }

        System.out.println("DEBUG: No username found in userInfo, using default");
        return "user";
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(new Color(248, 249, 250));

        // Изменили заголовок на "Статистика ваших задач"
        JLabel statsTitle = new JLabel("Статистика ваших задач", SwingConstants.CENTER);
        statsTitle.setFont(new Font("Arial", Font.BOLD, 24));
        statsTitle.setForeground(new Color(33, 37, 41));
        statsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel cardsContainer = new JPanel();
        cardsContainer.setLayout(new GridLayout(1, 4, 20, 0));
        cardsContainer.setBackground(new Color(248, 249, 250));
        cardsContainer.setMaximumSize(new Dimension(1000, 190));
        cardsContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        int totalTasks = allUsersTasks != null ? allUsersTasks.size() : 0;
        int notStarted = getTasksCountByStatusFromAll("НЕ_НАЧАТА");
        int inProgress = getTasksCountByStatusFromAll("В_РАБОТЕ");
        int completed = getTasksCountByStatusFromAll("ЗАВЕРШЕНА");
        int rework = getTasksCountByStatusFromAll("НА_ДОРАБОТКЕ");

        System.out.println("DEBUG: Statistics for current user - Total: " + totalTasks +
                ", Not Started: " + notStarted +
                ", In Progress: " + inProgress +
                ", Completed: " + completed +
                ", Rework: " + rework);

        cardsContainer.add(createStatCard("Всего задач", String.valueOf(totalTasks),
                new Color(52, 152, 219), "📋"));
        cardsContainer.add(createStatCard("Не начаты", String.valueOf(notStarted),
                new Color(241, 196, 15), "⏳"));
        cardsContainer.add(createStatCard("В работе", String.valueOf(inProgress),
                new Color(155, 89, 182), "🚀"));
        cardsContainer.add(createStatCard("Завершено", String.valueOf(completed),
                new Color(46, 204, 113), "✅"));

        statsPanel.add(statsTitle);
        statsPanel.add(Box.createVerticalStrut(20));
        statsPanel.add(cardsContainer);

        return statsPanel;
    }

    private void showDashboard() {
        // При переходе на главную обновляем статистику ТОЛЬКО текущего пользователя
        loadUserTasksForStatistics();
    }

    private void processStatusUpdate(Task task, String newStatus) {
        // Показываем индикатор загрузки
        JDialog loadingDialog = new JDialog(this, "Обновление статуса", true);
        loadingDialog.setSize(350, 120);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setLayout(new BorderLayout());
        loadingDialog.setResizable(false);

        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(Color.WHITE);
        loadingPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel loadingLabel = new JLabel("Обновление статуса задачи...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        loadingLabel.setForeground(new Color(44, 62, 80));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setBackground(Color.WHITE);

        loadingPanel.add(loadingLabel, BorderLayout.CENTER);
        loadingPanel.add(progressBar, BorderLayout.SOUTH);

        loadingDialog.add(loadingPanel, BorderLayout.CENTER);
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        // Запускаем обновление в отдельном потоке
        new Thread(() -> {
            try {
                boolean success = sendStatusUpdateToServer(task, newStatus);

                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();

                    if (success) {
                        // Обновляем статус задачи локально
                        task.setStatus(newStatus);

                        // Показываем красивое сообщение об успехе
                        showSuccessMessage("Статус задачи успешно обновлен!");

                        // Обновляем интерфейс
                        loadUserTasksForStatistics();
                        showMyTasks();
                    } else {
                        showErrorMessage("Ошибка при обновлении статуса задачи");
                    }
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    showErrorMessage("Ошибка при обновлении статуса: " + e.getMessage());
                });
            }
        }).start();

        loadingDialog.setVisible(true);
    }


    private List<Task> parseTasksArray(String tasksJson) {
        List<Task> tasks = new ArrayList<>();
        try {
            if (tasksJson == null || tasksJson.trim().isEmpty()) {
                System.out.println("DEBUG: Empty tasks array");
                return tasks;
            }

            System.out.println("DEBUG: Parsing tasks array: " + tasksJson);

            // Убираем пробелы и переносы строк для упрощения парсинга
            String cleanJson = tasksJson.replace("\n", "").replace("\r", "").trim();

            // Если массив пустой
            if (cleanJson.isEmpty()) {
                return tasks;
            }

            // Разделяем задачи по },{ но учитываем вложенные объекты
            List<String> taskObjects = new ArrayList<>();
            int start = 0;
            int braceCount = 0;
            boolean inObject = false;

            for (int i = 0; i < cleanJson.length(); i++) {
                char c = cleanJson.charAt(i);
                if (c == '{') {
                    braceCount++;
                    if (braceCount == 1) {
                        start = i;
                        inObject = true;
                    }
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0 && inObject) {
                        taskObjects.add(cleanJson.substring(start, i + 1));
                        inObject = false;
                    }
                }
            }

            System.out.println("DEBUG: Found " + taskObjects.size() + " task objects");

            for (String taskStr : taskObjects) {
                Task task = parseSingleTask(taskStr);
                if (task != null && task.getTitle() != null) {
                    tasks.add(task);
                    System.out.println("DEBUG: Added task: " + task.getTitle());
                }
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Error parsing tasks array: " + e.getMessage());
            e.printStackTrace();
        }
        return tasks;
    }

    private User parseUserFromJson(String json) {
        try {
            System.out.println("DEBUG: Parsing user JSON: " + json);

            User user = new User();
            List<Task> tasks = new ArrayList<>();

            // Извлекаем username
            String username = extractValue(json, "username");
            if (username != null) {
                user.setUsername(username);
            }

            // Парсим задачи - исправленная логика
            if (json.contains("\"tasks\"")) {
                int tasksStart = json.indexOf("\"tasks\":[") + 8;
                int tasksEnd = findMatchingBracket(json, tasksStart);

                if (tasksEnd > tasksStart) {
                    String tasksArray = json.substring(tasksStart + 1, tasksEnd).trim();
                    System.out.println("DEBUG: Tasks array for current user: " + tasksArray);

                    tasks = parseTasksArray(tasksArray);
                }
            }

            user.setTasks(tasks);
            System.out.println("DEBUG: Parsed current user: " + username + " with " + tasks.size() + " tasks");
            return user;
        } catch (Exception e) {
            System.out.println("DEBUG: Error parsing current user: " + e.getMessage());
            e.printStackTrace();
            User user = new User();
            user.setTasks(new ArrayList<>());
            return user;
        }
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

    private String extractValue(String json, String key) {
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
            System.out.println("DEBUG: Error extracting value for key " + key + ": " + e.getMessage());
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

    private User parseSingleUserFromAllUsers(String userJson) {
        try {
            System.out.println("DEBUG: Parsing single user: " + userJson);

            User user = new User();
            String username = extractValue(userJson, "username");
            if (username == null) return null;

            user.setUsername(username);

            // Парсим задачи пользователя
            if (userJson.contains("\"tasks\":")) {
                int tasksStart = userJson.indexOf("\"tasks\":[") + 8;
                int tasksEnd = findMatchingBracket(userJson, tasksStart);
                if (tasksEnd > tasksStart) {
                    String tasksArray = userJson.substring(tasksStart + 1, tasksEnd).trim();
                    System.out.println("DEBUG: Tasks array for user " + username + ": " + tasksArray);
                    List<Task> tasks = parseTasksArray(tasksArray);
                    user.setTasks(tasks);
                }
            }
            return user;
        } catch (Exception e) {
            System.out.println("DEBUG: Error parsing single user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        comboBox.setFocusable(false);
        comboBox.setMaximumRowCount(10);
    }

    private void resetFilters() {
        statusFilter.setSelectedIndex(0);
        importanceFilter.setSelectedIndex(0);
        sortFilter.setSelectedIndex(0);

        if (originalUserTasks != null) {
            userTasks = new ArrayList<>(originalUserTasks);
            refreshTasksDisplay();
        }
    }

    private void styleFilterButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15)); // увеличили отступы
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

    private void applyFilters() {
        if (originalUserTasks == null || originalUserTasks.isEmpty()) {
            System.out.println("DEBUG: No tasks to filter");
            return;
        }

        List<Task> filteredTasks = new ArrayList<>(originalUserTasks);
        System.out.println("DEBUG: Starting with " + filteredTasks.size() + " tasks");

        // Фильтрация по статусу
        String selectedStatus = (String) statusFilter.getSelectedItem();
        if (!"Все статусы".equals(selectedStatus)) {
            int before = filteredTasks.size();
            filteredTasks.removeIf(task ->
                    task.getStatus() == null || !task.getStatus().equals(selectedStatus)
            );
            System.out.println("DEBUG: Status filter '" + selectedStatus + "' removed " + (before - filteredTasks.size()) + " tasks");
        }

        // Фильтрация по важности
        String selectedImportance = (String) importanceFilter.getSelectedItem();
        if (!"Все приоритеты".equals(selectedImportance)) {
            int before = filteredTasks.size();
            filteredTasks.removeIf(task ->
                    task.getImportance() == null || !task.getImportance().equals(selectedImportance)
            );
            System.out.println("DEBUG: Importance filter '" + selectedImportance + "' removed " + (before - filteredTasks.size()) + " tasks");
        }

        // Сортировка по дедлайну
        String selectedSort = (String) sortFilter.getSelectedItem();
        System.out.println("DEBUG: Selected sort: " + selectedSort);

        if ("Дедлайн ↑".equals(selectedSort)) {
            System.out.println("DEBUG: Sorting by deadline ascending");
            filteredTasks.sort((t1, t2) -> {
                String deadline1 = t1.getDeadline();
                String deadline2 = t2.getDeadline();

                System.out.println("DEBUG: Comparing deadlines: '" + deadline1 + "' vs '" + deadline2 + "'");

                if (deadline1 == null && deadline2 == null) return 0;
                if (deadline1 == null) return 1;
                if (deadline2 == null) return -1;

                int result = deadline1.compareTo(deadline2);
                System.out.println("DEBUG: Comparison result: " + result);
                return result;
            });
        } else if ("Дедлайн ↓".equals(selectedSort)) {
            System.out.println("DEBUG: Sorting by deadline descending");
            filteredTasks.sort((t1, t2) -> {
                String deadline1 = t1.getDeadline();
                String deadline2 = t2.getDeadline();

                System.out.println("DEBUG: Comparing deadlines: '" + deadline1 + "' vs '" + deadline2 + "'");

                if (deadline1 == null && deadline2 == null) return 0;
                if (deadline1 == null) return 1;
                if (deadline2 == null) return -1;

                int result = deadline2.compareTo(deadline1);
                System.out.println("DEBUG: Comparison result: " + result);
                return result;
            });
        }

        // Обновляем отображаемые задачи
        userTasks = filteredTasks;
        System.out.println("DEBUG: Final task count: " + userTasks.size());
        refreshTasksDisplay();
    }

    private void applyAllTasksFilters() {
        if (originalAllUsersTasks == null || originalAllUsersTasks.isEmpty()) {
            System.out.println("DEBUG: No all tasks to filter");
            return;
        }

        List<Task> filteredTasks = new ArrayList<>(originalAllUsersTasks);
        System.out.println("DEBUG: Starting with " + filteredTasks.size() + " all tasks");

        // Фильтрация по статусу
        String selectedStatus = (String) allTasksStatusFilter.getSelectedItem();
        if (!"Все статусы".equals(selectedStatus)) {
            int before = filteredTasks.size();
            filteredTasks.removeIf(task ->
                    task.getStatus() == null || !task.getStatus().equals(selectedStatus)
            );
            System.out.println("DEBUG: All tasks status filter '" + selectedStatus + "' removed " + (before - filteredTasks.size()) + " tasks");
        }

        // Фильтрация по важности
        String selectedImportance = (String) allTasksImportanceFilter.getSelectedItem();
        if (!"Все приоритеты".equals(selectedImportance)) {
            int before = filteredTasks.size();
            filteredTasks.removeIf(task ->
                    task.getImportance() == null || !task.getImportance().equals(selectedImportance)
            );
            System.out.println("DEBUG: All tasks importance filter '" + selectedImportance + "' removed " + (before - filteredTasks.size()) + " tasks");
        }

        // Сортировка по дедлайну
        String selectedSort = (String) allTasksSortFilter.getSelectedItem();
        System.out.println("DEBUG: All tasks selected sort: " + selectedSort);

        if ("Дедлайн ↑".equals(selectedSort)) {
            System.out.println("DEBUG: Sorting all tasks by deadline ascending");
            filteredTasks.sort((t1, t2) -> {
                String deadline1 = t1.getDeadline();
                String deadline2 = t2.getDeadline();

                if (deadline1 == null && deadline2 == null) return 0;
                if (deadline1 == null) return 1;
                if (deadline2 == null) return -1;

                return deadline1.compareTo(deadline2);
            });
        } else if ("Дедлайн ↓".equals(selectedSort)) {
            System.out.println("DEBUG: Sorting all tasks by deadline descending");
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
        refreshAllTasksDisplay(filteredTasks);
    }

    private void resetAllTasksFilters() {
        allTasksStatusFilter.setSelectedIndex(0);
        allTasksImportanceFilter.setSelectedIndex(0);
        allTasksSortFilter.setSelectedIndex(0);

        if (originalAllUsersTasks != null) {
            refreshAllTasksDisplay(new ArrayList<>(originalAllUsersTasks));
        }
    }

    private void loadAllUsersTasksFromServer(JPanel allTasksPanel, JPanel loadingPanel) {
        new Thread(() -> {
            try {
                List<User> users = getAllUsersWithTasks();
                SwingUtilities.invokeLater(() -> {
                    loadingPanel.removeAll();
                    allTasksPanel.removeAll();
                    allTasksPanel.setLayout(new BorderLayout());

                    JLabel titleLabel = new JLabel("Задачи других пользователей", SwingConstants.CENTER);
                    titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
                    titleLabel.setForeground(new Color(44, 62, 80));
                    titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    allTasksPanel.add(titleLabel, BorderLayout.NORTH);

                    // Создаем панель фильтров
                    JPanel filtersPanel = createAllTasksFiltersPanel();
                    allTasksPanel.add(filtersPanel, BorderLayout.NORTH);

                    if (users != null && !users.isEmpty()) {
                        // Сохраняем оригинальный список всех задач с информацией о пользователях
                        originalAllUsersTasks = new ArrayList<>();
                        Map<Task, String> taskUserMap = new HashMap<>();

                        for (User user : users) {
                            if (user.getTasks() != null) {
                                for (Task task : user.getTasks()) {
                                    originalAllUsersTasks.add(task);
                                    taskUserMap.put(task, user.getUsername());
                                }
                            }
                        }

                        // Сохраняем карту связей для использования в фильтрации
                        // Можно сохранить как поле класса: private Map<Task, String> allTasksUserMap;

                        System.out.println("DEBUG: Loaded " + originalAllUsersTasks.size() + " tasks from all users");
                        displayAllUsersTasksWithFilters(allTasksPanel, users);
                    } else {
                        showNoTasksMessage(allTasksPanel);
                        originalAllUsersTasks = new ArrayList<>();
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



    private void initializeNotificationSystem() {
        // Создаем менеджер уведомлений
        notificationManager = new NotificationManager(this);

        // Создаем и запускаем consumer для уведомлений пользователя
        notificationConsumer = new UserNotificationConsumer(notificationManager, this.username);
        notificationConsumer.startConsuming();

        System.out.println("User notification system initialized for: " + this.username);
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

    private void onWindowClosing() {
        // Останавливаем consumer при закрытии окна
        if (notificationConsumer != null) {
            notificationConsumer.stop();
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    private boolean sendStatusUpdateToServer(Task task, String newStatus) {
        try {
            // Проверяем, что taskId не null
            if (task.getTaskId() == null) {
                System.out.println("DEBUG: Task ID is null for task: " + task.getTitle());
                return false;
            }

            String url;

            // Выбираем правильный endpoint в зависимости от нового статуса
            if ("В_РАБОТЕ".equals(newStatus)) {
                url = "http://localhost:8080/markthetaskasinwork?taskId=" + task.getTaskId();
            } else if ("ЗАВЕРШЕНА".equals(newStatus)) {
                url = "http://localhost:8080/markthetaskascompleted?taskId=" + task.getTaskId();
            } else {
                System.out.println("DEBUG: Unknown status for update: " + newStatus);
                return false;
            }

            System.out.println("DEBUG: Sending status update to: " + url);
            System.out.println("DEBUG: Task ID: " + task.getTaskId() + ", Title: " + task.getTitle());

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Authorization", "Bearer " + authToken)
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("DEBUG: Status update response: " + response.statusCode());
            System.out.println("DEBUG: Status update body: " + response.body());

            return response.statusCode() == 200;

        } catch (Exception e) {
            System.out.println("DEBUG: Error updating task status: " + e.getMessage());
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

    private JDialog loadingDialog;
    private JProgressBar progressBar;

    public void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isVisible()) {
            loadingDialog.setVisible(false);
        }
    }

    private JPanel createTableHeader(boolean showUsername) {
        int columns = showUsername ? 8 : 7; // Увеличиваем на 1 колонку для описания
        JPanel headerPanel = new JPanel(new GridLayout(1, columns, 10, 5));
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Колонка пользователя (только для всех задач)
        if (showUsername) {
            JLabel userHeaderLabel = new JLabel("Пользователь");
            userHeaderLabel.setFont(new Font("Arial", Font.BOLD, 12));
            userHeaderLabel.setForeground(new Color(44, 62, 80));
            headerPanel.add(userHeaderLabel);
        }

        // Новый порядок колонок: Название, Описание, Статус, Приоритет, Дедлайн, Комментарии, Действия
        String[] headers = {"Название задачи", "Описание", "Статус", "Срочность", "Дедлайн", "Комментарии", "Действия"};
        for (String header : headers) {
            JLabel headerLabel = new JLabel(header);
            headerLabel.setFont(new Font("Arial", Font.BOLD, 12));
            headerLabel.setForeground(new Color(44, 62, 80));
            headerPanel.add(headerLabel);
        }

        return headerPanel;
    }

    private void addTaskRow(JPanel parent, Task task, boolean showUsername, String username) {
        int columns = showUsername ? 8 : 7; // Увеличиваем на 1 колонку для описания
        JPanel taskRow = new JPanel(new GridLayout(1, columns, 10, 5));
        taskRow.setBackground(Color.WHITE);
        taskRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        taskRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Колонка пользователя (только для всех задач)
        if (showUsername) {
            JLabel userLabel = new JLabel(username != null ? username : "");
            userLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            userLabel.setForeground(new Color(44, 62, 80));
            taskRow.add(userLabel);
        }

        // Колонка названия задачи
        JLabel titleLabel = new JLabel(task.getTitle() != null ? task.getTitle() : "");
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(44, 62, 80));
        taskRow.add(titleLabel);

        // Колонка описания (кликабельная ссылка)
        String descriptionText = task.getDescription();
        boolean hasDescription = descriptionText != null && !descriptionText.trim().isEmpty();

        JLabel descriptionLabel = new JLabel(hasDescription ? "Просмотр" : "Нет описания");
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descriptionLabel.setForeground(hasDescription ? new Color(52, 152, 219) : Color.GRAY);

        // Делаем кликабельным только если есть описание
        if (hasDescription) {
            descriptionLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            descriptionLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    showTaskDescription(task); // Теперь использует новый красивый диалог
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
        JLabel statusLabel = new JLabel(task.getStatus() != null ? getStatusDisplayName(task.getStatus()) : "");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusLabel.setForeground(getStatusColor(task.getStatus()));
        taskRow.add(statusLabel);

        // Колонка приоритета
        JLabel priorityLabel = new JLabel(getImportanceDisplayName(task.getImportance()));
        priorityLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        priorityLabel.setForeground(getImportanceColor(task.getImportance()));
        taskRow.add(priorityLabel);

        // Колонка дедлайна с цветом
        String deadline = task.getDeadline() != null ? task.getDeadline().toString() : "";
        String formattedDeadline = DeadlineUtils.formatDeadlineForDisplay(deadline);
        JLabel deadlineLabel = new JLabel(formattedDeadline);
        deadlineLabel.setFont(new Font("Arial", Font.BOLD, 12));
        deadlineLabel.setForeground(DeadlineUtils.getDeadlineColor(deadline));
        taskRow.add(deadlineLabel);

        // Колонка комментариев
        int commentCount = task.getComments() != null ? task.getComments().size() : 0;
        JLabel commentsLabel = new JLabel(commentCount + " коммент.");
        commentsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        commentsLabel.setForeground(commentCount > 0 ? new Color(52, 152, 219) : Color.GRAY);

        // Делаем кликабельным только если есть комментарии
        if (commentCount > 0) {
            commentsLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            commentsLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    showTaskComments(task); // Теперь использует новый красивый диалог
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

        // Колонка действий (кнопка изменения статуса)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        actionPanel.setBackground(Color.WHITE);

        // Показываем кнопку только для текущего пользователя (не в разделе "Все задачи")
        if (!showUsername) {
            JButton statusButton = createStatusButton(task);
            if (statusButton != null) {
                actionPanel.add(statusButton);
            } else {
                // Если кнопки нет (например, для завершенных задач), показываем статус текстом
                JLabel statusText = new JLabel(getStatusDisplayName(task.getStatus()));
                statusText.setFont(new Font("Arial", Font.PLAIN, 11));
                statusText.setForeground(getStatusColor(task.getStatus()));
                actionPanel.add(statusText);
            }
        } else {
            // В разделе "Все задачи" показываем пустую ячейку или текст
            JLabel noActionLabel = new JLabel("-");
            noActionLabel.setForeground(Color.GRAY);
            noActionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            actionPanel.add(noActionLabel);
        }

        taskRow.add(actionPanel);

        parent.add(taskRow);
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

    private Color getImportanceColor(String importance) {
        if (importance == null) return Color.BLACK;
        switch (importance) {
            case "СРОЧНАЯ": return new Color(231, 76, 60); // Красный
            case "НАДО_ПОТОРОПИТЬСЯ": return new Color(241, 196, 15); // Желтый
            case "МОЖЕТ_ПОДОЖДАТЬ": return new Color(46, 204, 113); // Зеленый
            default: return Color.BLACK;
        }
    }

    private JButton createStatusButton(Task task) {
        if (task.getStatus() == null) return null;

        String currentStatus = task.getStatus();
        JButton button = new JButton();

        switch (currentStatus) {
            case "НЕ_НАЧАТА":
                button.setText("Начать работу");
                button.setBackground(new Color(52, 152, 219)); // Синий
                button.setForeground(Color.WHITE);
                button.addActionListener(e -> updateTaskStatus(task, "В_РАБОТЕ"));
                break;

            case "В_РАБОТЕ":
                button.setText("Завершить");
                button.setBackground(new Color(46, 204, 113)); // Зеленый
                button.setForeground(Color.WHITE);
                button.addActionListener(e -> updateTaskStatus(task, "ЗАВЕРШЕНА"));
                break;

            case "ЗАВЕРШЕНА":
                // Для завершенных задач не показываем кнопку
                return null;

            case "НА_ДОРАБОТКЕ":
                button.setText("Завершить");
                button.setBackground(new Color(46, 204, 113)); // Зеленый
                button.setForeground(Color.WHITE);
                button.addActionListener(e -> updateTaskStatus(task, "ЗАВЕРШЕНА"));
                break;

            default:
                return null;
        }

        button.setFont(new Font("Arial", Font.BOLD, 11));
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

        return button;
    }

    // Обновленные карточки статистики без смайликов
    private JPanel createModernStatCard(String icon, String title, String value, Color color) {
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
        card.setPreferredSize(new Dimension(200, 120));
        card.setMaximumSize(new Dimension(200, 120));
        card.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        // Верхняя панель с заголовком
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(220, 220, 220));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Центральная панель со значением
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JLabel createFilterLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Color.WHITE);
        return label;
    }

    // Правильные статические кнопки которые ВИДНЫ
    private JButton createStaticButton(String text, Color backgroundColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Рисуем скругленный прямоугольник
                g2.setColor(backgroundColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Белая текст
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        button.setPreferredSize(new Dimension(120, 40));
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Убираем ВСЕ эффекты при наведении
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            // Пустые методы - никакой реакции на наведение
        });

        return button;
    }

    // Класс для фоновой панели ТОЧНО как в авторизации
    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Черно-фиолетовый градиент ТОЧНО как в авторизации
            GradientPaint mainGradient = new GradientPaint(0, 0, new Color(25, 25, 35),
                    getWidth(), getHeight(), new Color(45, 30, 60));
            g2.setPaint(mainGradient);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Декоративные элементы (пузырьки) ТОЧНО как в авторизации
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

    // Обновленный конструктор с правильным фоно

    // Обновленная панель приветствия
    private JPanel createModernWelcomePanel() {
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setOpaque(false);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(60, 80, 60, 80));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Заголовок как в авторизации
        JLabel titleLabel = new JLabel("Добро пожаловать в ваш личный кабинет!", SwingConstants.CENTER);
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
        descriptionArea.setText("\nЗдесь вы можете управлять своими задачами, просматривать прогресс " +
                "и редактировать профиль. Система поможет вам эффективно организовать вашу работу " +
                "и отслеживать выполнение поставленных целей.\n");
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(30, 80, 40, 80));
        descriptionArea.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Статистика
        JPanel statsPanel = createModernStatsPanel();

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(descriptionArea);
        contentPanel.add(Box.createVerticalStrut(50));
        contentPanel.add(statsPanel);

        welcomePanel.add(contentPanel, BorderLayout.CENTER);
        return welcomePanel;
    }


    // ДОБАВЬТЕ ЭТОТ МЕТОД - он используется в loadUserTasksForStatistics
    private void updateWelcomePanel() {
        SwingUtilities.invokeLater(() -> {
            centerPanel.removeAll();
            JPanel welcomePanel = createModernWelcomePanel(); // ИСПРАВЛЕНИЕ: используем новую панель
            centerPanel.add(welcomePanel, "welcome");

            if (cardLayout != null) {
                cardLayout.show(centerPanel, "welcome");
            }

            centerPanel.revalidate();
            centerPanel.repaint();
        });
    }

    // ОБНОВИТЕ метод loadUserTasksForStatistics
    private void loadUserTasksForStatistics() {
        new Thread(() -> {
            try {
                System.out.println("DEBUG: Loading tasks for statistics for user: " + username);

                // Загружаем задачи ТОЛЬКО текущего пользователя
                User currentUser = getUserWithTasks();
                if (currentUser != null && currentUser.getTasks() != null) {
                    allUsersTasks = currentUser.getTasks(); // Теперь это задачи только текущего пользователя
                    System.out.println("DEBUG: Loaded " + allUsersTasks.size() + " tasks for statistics (current user only)");

                    updateWelcomePanel(); // ИСПРАВЛЕНИЕ: используем новый метод
                } else {
                    allUsersTasks = new ArrayList<>();
                    System.out.println("DEBUG: No tasks found for current user statistics");
                    updateWelcomePanel(); // ИСПРАВЛЕНИЕ: используем новый метод
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Error loading tasks for statistics: " + e.getMessage());
                e.printStackTrace();
                allUsersTasks = new ArrayList<>();
                updateWelcomePanel(); // ИСПРАВЛЕНИЕ: используем новый метод
            }
        }).start();
    }

    // ДОБАВЬТЕ ЭТОТ МЕТОД для создания кнопок навигации
    private JButton createStaticNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(155, 89, 182, 200));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 80), 1),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(true);

        return button;
    }

    // ДОБАВЬТЕ ЭТОТ МЕТОД для создания кнопки выхода
    private JButton createStaticLogoutButton() {
        JButton button = new JButton("Выйти");
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(220, 53, 69, 200));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 80), 1),
                BorderFactory.createEmptyBorder(12, 25, 12, 25)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(true);

        return button;
    }

    // КЛАСС ДЛЯ ФОНА ДИАЛОГОВЫХ ОКОН
    private class DialogBackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Черно-фиолетовый градиент ТОЧНО как в основном окне
            GradientPaint mainGradient = new GradientPaint(0, 0, new Color(25, 25, 35),
                    getWidth(), getHeight(), new Color(45, 30, 60));
            g2.setPaint(mainGradient);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Декоративные элементы (пузырьки) ТОЧНО как в основном окне
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

    // ПРОСТОЙ КОНСТРУКТОР ДИАЛОГОВ БЕЗ СЛОЖНОГО ФОНА
    private JDialog createSimpleDialog(String title, int width, int height) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        // Простой белый фон как в стандартных диалогах
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        dialog.setContentPane(contentPane);
        return dialog;
    }

    // ПРОСТАЯ КНОПКА БЕЗ ЭФФЕКТОВ НАВЕДЕНИЯ
    private JButton createSimpleButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(backgroundColor);
        button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // УБИРАЕМ ВСЕ ЭФФЕКТЫ ПРИ НАВЕДЕНИИ
        return button;
    }


    // ВОССТАНАВЛИВАЕМ СТАРЫЙ МЕТОД ДЛЯ ИНДИКАТОРА ЗАГРУЗКИ
    public void showLoadingDialog(String message) {
        if (loadingDialog == null) {
            loadingDialog = new JDialog(this, "Загрузка", true);
            loadingDialog.setSize(300, 120);
            loadingDialog.setLocationRelativeTo(this);
            loadingDialog.setLayout(new BorderLayout());
            loadingDialog.setResizable(false);
            loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

            JPanel loadingPanel = new JPanel(new BorderLayout());
            loadingPanel.setBackground(Color.WHITE);
            loadingPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

            JLabel loadingLabel = new JLabel("Загрузка...", SwingConstants.CENTER);
            loadingLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            loadingLabel.setForeground(new Color(44, 62, 80));

            progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setBackground(Color.WHITE);

            loadingPanel.add(loadingLabel, BorderLayout.CENTER);
            loadingPanel.add(progressBar, BorderLayout.SOUTH);

            loadingDialog.add(loadingPanel, BorderLayout.CENTER);
        }

        // Обновляем сообщение если нужно
        Component[] components = ((JPanel)loadingDialog.getContentPane().getComponent(0)).getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                ((JLabel)comp).setText(message);
                break;
            }
        }

        loadingDialog.setVisible(true);
    }

    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ СТИЛИЗАЦИИ
    private void styleConfirmButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleUserDialogButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void addInfoRow(JPanel panel, String label, String value) {
        JLabel labelField = new JLabel(label);
        labelField.setFont(new Font("Arial", Font.BOLD, 14));
        labelField.setForeground(new Color(44, 62, 80));

        JLabel valueField = new JLabel(value);
        valueField.setFont(new Font("Arial", Font.PLAIN, 14));
        valueField.setForeground(new Color(127, 140, 141));

        panel.add(labelField);
        panel.add(valueField);
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

        JScrollPane textScroll = new JScrollPane(commentText);
        textScroll.setBorder(BorderFactory.createEmptyBorder());
        textScroll.setBackground(new Color(248, 249, 250));
        textScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        textScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        commentPanel.add(headerPanel, BorderLayout.NORTH);
        commentPanel.add(textScroll, BorderLayout.CENTER);

        return commentPanel;
    }














    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ДЛЯ СТИЛИЗОВАННЫХ ДИАЛОГОВ
    private JDialog createStyledDialog(String title, int width, int height) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        // Создаем панель с градиентным фоном как в основном окне
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Черно-фиолетовый градиент как в основном окне
                GradientPaint gradient = new GradientPaint(0, 0, new Color(25, 25, 35),
                        getWidth(), getHeight(), new Color(45, 30, 60));
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Декоративные элементы
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

    private JPanel createStyledPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Полупрозрачный фон
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Рамка
                g2.setColor(new Color(255, 255, 255, 30));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    // ОБНОВЛЕННЫЕ МЕТОДЫ ДЛЯ ДИАЛОГОВ
    private void updateTaskStatus(Task task, String newStatus) {
        JDialog confirmDialog = createStyledDialog("Подтверждение изменения статуса", 500, 550);
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Иконка
        JLabel iconLabel = new JLabel("❓", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setForeground(new Color(155, 89, 182));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Заголовок
        JLabel titleLabel = createDialogTitle("Изменение статуса задачи");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Информация о задаче
        JPanel infoPanel = createStyledPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.setMaximumSize(new Dimension(400, 120));

        JLabel taskLabel = createDialogText("Задача: " + task.getTitle(), 16);
        taskLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JLabel currentStatusLabel = createDialogText("Текущий статус: " + getStatusDisplayName(task.getStatus()), 14);
        JLabel newStatusLabel = createDialogText("Новый статус: " + getStatusDisplayName(newStatus), 14);
        newStatusLabel.setForeground(getStatusColor(newStatus));
        newStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        infoPanel.add(taskLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(currentStatusLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(newStatusLabel);

        // Сообщение
        JLabel messageLabel = createDialogText("Вы уверены, что хотите изменить статус задачи?", 14);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 0, 0));

        JButton yesButton = createDialogButton("Да, изменить", new Color(46, 204, 113));
        JButton noButton = createDialogButton("Отмена", new Color(108, 117, 125));

        yesButton.addActionListener(e -> {
            confirmDialog.dispose();
            processStatusUpdate(task, newStatus);
        });
        noButton.addActionListener(e -> confirmDialog.dispose());

        buttonPanel.add(noButton);
        buttonPanel.add(yesButton);

        // Сборка компонентов
        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(infoPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(25));
        contentPanel.add(buttonPanel);

        confirmDialog.add(contentPanel, BorderLayout.CENTER);
        confirmDialog.getRootPane().setDefaultButton(noButton);
        confirmDialog.setVisible(true);
    }

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

    private void showTaskComments(Task task) {
        JDialog commentsDialog = createStyledDialog("Комментарии к задаче: " + task.getTitle(), 650, 650);
        commentsDialog.setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Заголовок
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel iconLabel = new JLabel("💬", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        iconLabel.setForeground(new Color(155, 89, 182));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = createDialogTitle("Комментарии к задаче");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel taskTitleLabel = createDialogText("\"" + task.getTitle() + "\"", 14);
        taskTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(iconLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(taskTitleLabel);

        // Панель с комментариями
        JPanel commentsContentPanel = createStyledPanel();
        commentsContentPanel.setLayout(new BorderLayout());
        commentsContentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel commentsListPanel = new JPanel();
        commentsListPanel.setLayout(new BoxLayout(commentsListPanel, BoxLayout.Y_AXIS));
        commentsListPanel.setOpaque(false);

        if (task.getComments() != null && !task.getComments().isEmpty()) {
            for (int i = 0; i < task.getComments().size(); i++) {
                Comment comment = task.getComments().get(i);
                JPanel commentPanel = createStyledCommentPanel(comment, i + 1);
                commentsListPanel.add(commentPanel);
                commentsListPanel.add(Box.createVerticalStrut(10));
            }
        } else {
            JPanel noCommentsPanel = createStyledPanel();
            noCommentsPanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));
            noCommentsPanel.setLayout(new BorderLayout());

            JLabel noCommentsLabel = createDialogText("Комментарии отсутствуют", 16);
            noCommentsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            noCommentsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));

            noCommentsPanel.add(noCommentsLabel, BorderLayout.CENTER);
            commentsListPanel.add(noCommentsPanel);
        }

        JScrollPane scrollPane = new JScrollPane(commentsListPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(550, 300));

        commentsContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Панель статистики и кнопок
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // Статистика
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        statsPanel.setOpaque(false);

        int commentCount = task.getComments() != null ? task.getComments().size() : 0;
        JLabel statsLabel = createDialogText("Всего комментариев: " + commentCount, 12);
        statsLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(155, 89, 182), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        statsPanel.add(statsLabel);

        // Кнопка закрытия
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton closeButton = createDialogButton("Закрыть", new Color(108, 117, 125));
        closeButton.addActionListener(e -> commentsDialog.dispose());

        buttonPanel.add(closeButton);

        bottomPanel.add(statsPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Сборка компонентов
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(commentsContentPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        commentsDialog.add(mainPanel, BorderLayout.CENTER);
        commentsDialog.getRootPane().setDefaultButton(closeButton);
        commentsDialog.setVisible(true);
    }

    private JPanel createStyledCommentPanel(Comment comment, int number) {
        JPanel commentPanel = createStyledPanel();
        commentPanel.setLayout(new BorderLayout());
        commentPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        commentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Заголовок комментария
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel numberLabel = createDialogText("Комментарий #" + number, 12);
        numberLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        numberLabel.setForeground(new Color(155, 89, 182));

        if (comment.getId() != null) {
            JLabel idLabel = createDialogText("ID: " + comment.getId(), 10);
            idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            headerPanel.add(idLabel, BorderLayout.EAST);
        }

        headerPanel.add(numberLabel, BorderLayout.WEST);

        // Текст комментария
        JTextArea commentText = new JTextArea(comment.getDescription() != null ? comment.getDescription() : "");
        commentText.setEditable(false);
        commentText.setLineWrap(true);
        commentText.setWrapStyleWord(true);
        commentText.setOpaque(false);
        commentText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        commentText.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        commentText.setForeground(new Color(220, 220, 220));

        JScrollPane textScroll = new JScrollPane(commentText);
        textScroll.setOpaque(false);
        textScroll.getViewport().setOpaque(false);
        textScroll.setBorder(BorderFactory.createEmptyBorder());
        textScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        textScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        commentPanel.add(headerPanel, BorderLayout.NORTH);
        commentPanel.add(textScroll, BorderLayout.CENTER);

        return commentPanel;
    }

    private void showTaskDescription(Task task) {
        JDialog descriptionDialog = createStyledDialog("Описание задачи: " + task.getTitle(), 600, 600);
        descriptionDialog.setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Заголовок
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel iconLabel = new JLabel("📄", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        iconLabel.setForeground(new Color(155, 89, 182));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = createDialogTitle("Описание задачи");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel taskTitleLabel = createDialogText("\"" + task.getTitle() + "\"", 14);
        taskTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(iconLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(taskTitleLabel);

        // Панель с описанием
        JPanel descriptionContentPanel = createStyledPanel();
        descriptionContentPanel.setLayout(new BorderLayout());
        descriptionContentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextArea descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        String description = task.getDescription();
        if (description != null && !description.trim().isEmpty()) {
            descriptionArea.setText(description);
            descriptionArea.setForeground(new Color(220, 220, 220));
        } else {
            descriptionArea.setText("Описание отсутствует");
            descriptionArea.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            descriptionArea.setForeground(new Color(180, 180, 180));
        }

        descriptionArea.setOpaque(false);
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(500, 250));

        descriptionContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Панель статистики и кнопок
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // Статистика
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        statsPanel.setOpaque(false);

        int descLength = description != null ? description.length() : 0;
        JLabel statsLabel = createDialogText("Длина описания: " + descLength + " символов", 12);
        statsLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(155, 89, 182), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        statsPanel.add(statsLabel);

        // Кнопка закрытия
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton closeButton = createDialogButton("Закрыть", new Color(108, 117, 125));
        closeButton.addActionListener(e -> descriptionDialog.dispose());

        buttonPanel.add(closeButton);

        bottomPanel.add(statsPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Сборка компонентов
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(descriptionContentPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        descriptionDialog.add(mainPanel, BorderLayout.CENTER);
        descriptionDialog.getRootPane().setDefaultButton(closeButton);
        descriptionDialog.setVisible(true);
    }

    private void showMyProfile() {
        try {
            Map<String, Object> userInfo = getUserInfo();

            JDialog profileDialog = createStyledDialog("Мой профиль", 500, 550);
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setOpaque(false);

            JLabel titleLabel = createDialogTitle("Информация о профиле");
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel iconLabel = new JLabel("👤", SwingConstants.CENTER);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Панель с информацией
            JPanel infoPanel = createStyledPanel();
            infoPanel.setLayout(new GridLayout(3, 2, 15, 20));
            infoPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
            infoPanel.setMaximumSize(new Dimension(400, 140));

            String username = userInfo.containsKey("username") ? userInfo.get("username").toString() : this.username;
            String role = userInfo.containsKey("role") ? userInfo.get("role").toString() : "USER";

            addStyledInfoRow(infoPanel, "Логин:", username);
            addStyledInfoRow(infoPanel, "Роль:", getRoleDisplayName(role));
            addStyledInfoRow(infoPanel, "Статус:", "Активен");

            // Кнопка закрытия
            JButton closeButton = createDialogButton("Закрыть", new Color(52, 152, 219));
            closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            closeButton.addActionListener(e -> profileDialog.dispose());

            contentPanel.add(titleLabel);
            contentPanel.add(Box.createVerticalStrut(20));
            contentPanel.add(iconLabel);
            contentPanel.add(Box.createVerticalStrut(20));
            contentPanel.add(infoPanel);
            contentPanel.add(Box.createVerticalStrut(30));
            contentPanel.add(closeButton);

            profileDialog.add(contentPanel, BorderLayout.CENTER);
            profileDialog.getRootPane().setDefaultButton(closeButton);
            profileDialog.setVisible(true);

        } catch (Exception e) {
            showErrorMessage("Ошибка загрузки информации о профиле: " + e.getMessage());
        }
    }

    private void addStyledInfoRow(JPanel panel, String label, String value) {
        JLabel labelField = createDialogText(label, 14);
        labelField.setFont(new Font("Segoe UI", Font.BOLD, 14));
        labelField.setForeground(new Color(155, 89, 182));

        JLabel valueField = createDialogText(value, 14);

        panel.add(labelField);
        panel.add(valueField);
    }




    // Измененный метод loadAllUsersTasksPanel() - делаем его аналогичным loadMyTasksPanel()
    private JPanel loadAllUsersTasksPanel() {
        JPanel allTasksPanel = new JPanel(new BorderLayout());
        allTasksPanel.setOpaque(false); // Прозрачный, чтобы темный градиент просвечивал
        allTasksPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JLabel titleLabel = new JLabel("Задачи других пользователей", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE); // Белый текст на темном фоне
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        allTasksPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel filtersPanel = createAllTasksFiltersPanel();
        allTasksPanel.add(filtersPanel, BorderLayout.NORTH);

        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setOpaque(false);
        JLabel loadingLabel = new JLabel("Загрузка задач...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        loadingLabel.setForeground(new Color(200, 200, 200));
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);

        allTasksPanel.add(loadingPanel, BorderLayout.CENTER);
        loadAllUsersTasksFromServer(allTasksPanel, loadingPanel);

        return allTasksPanel;
    }

    // Измененный метод displayAllUsersTasksWithFilters() - белый фон для панели задач
    private void displayAllUsersTasksWithFilters(JPanel allTasksPanel, List<User> users) {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(true); // Белый фон для списка задач
        contentPanel.setBackground(Color.WHITE);

        contentPanel.add(createTableHeader(true));
        contentPanel.add(Box.createVerticalStrut(10));

        int totalTasks = 0;
        for (User user : users) {
            if (user.getTasks() != null) {
                System.out.println("DEBUG: Displaying tasks for user: " + user.getUsername() +
                        " (" + user.getTasks().size() + " tasks)");
                for (Task task : user.getTasks()) {
                    addTaskRow(contentPanel, task, true, user.getUsername());
                    totalTasks++;
                }
            }
        }

        System.out.println("DEBUG: Total tasks displayed: " + totalTasks);

        // Создаем скролл панель с правильными настройками
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Настраиваем скролл панель чтобы избежать горизонтального скролла
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        allTasksPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void displayMyTasks(JPanel myTasksPanel, List<Task> tasks) {
        JPanel tasksContentPanel = new JPanel();
        tasksContentPanel.setLayout(new BoxLayout(tasksContentPanel, BoxLayout.Y_AXIS));
        tasksContentPanel.setOpaque(true); // Белый фон
        tasksContentPanel.setBackground(Color.WHITE);

        tasksContentPanel.add(createTableHeader(false));
        tasksContentPanel.add(Box.createVerticalStrut(10));

        for (Task task : tasks) {
            addTaskRow(tasksContentPanel, task, false, null);
        }

        JScrollPane scrollPane = new JScrollPane(tasksContentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        myTasksPanel.add(scrollPane, BorderLayout.CENTER);
    }


    private void showNoTasksMessage(JPanel containerPanel) {
        containerPanel.removeAll();
        containerPanel.setLayout(new BorderLayout());

        JLabel noTasksLabel = new JLabel("Задачи не найдены", SwingConstants.CENTER);
        noTasksLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        noTasksLabel.setForeground(Color.GRAY);
        containerPanel.add(noTasksLabel, BorderLayout.CENTER);

        containerPanel.revalidate();
        containerPanel.repaint();
    }

    private void showErrorPanel(JPanel containerPanel, String message) {
        containerPanel.removeAll();
        containerPanel.setLayout(new BorderLayout());

        JLabel errorLabel = new JLabel(message, SwingConstants.CENTER);
        errorLabel.setFont(new Font("Arial", Font.BOLD, 14));
        errorLabel.setForeground(Color.RED);
        containerPanel.add(errorLabel, BorderLayout.CENTER);

        containerPanel.revalidate();
        containerPanel.repaint();
    }


    private JPanel createFiltersPanel() {
        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filtersPanel.setBackground(new Color(248, 249, 250));
        filtersPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        filtersPanel.setPreferredSize(new Dimension(getWidth(), 80));

        // Фильтр по статусу
        JLabel statusLabel = new JLabel("Статус:");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusLabel.setForeground(new Color(44, 62, 80));

        String[] statusOptions = {"Все статусы", "НЕ_НАЧАТА", "В_РАБОТЕ", "ЗАВЕРШЕНА", "НА_ДОРАБОТКЕ"};
        statusFilter = new JComboBox<>(statusOptions);
        styleComboBox(statusFilter);
        statusFilter.setPreferredSize(new Dimension(140, 30));

        // Фильтр по важности
        JLabel importanceLabel = new JLabel("Важность:");
        importanceLabel.setFont(new Font("Arial", Font.BOLD, 12));
        importanceLabel.setForeground(new Color(44, 62, 80));

        String[] importanceOptions = {"Все приоритеты", "СРОЧНАЯ", "НАДО_ПОТОРОПИТЬСЯ", "МОЖЕТ_ПОДОЖДАТЬ"};
        importanceFilter = new JComboBox<>(importanceOptions);
        styleComboBox(importanceFilter);
        importanceFilter.setPreferredSize(new Dimension(140, 30));

        // Сортировка по дедлайну
        JLabel sortLabel = new JLabel("Сортировка:");
        sortLabel.setFont(new Font("Arial", Font.BOLD, 12));
        sortLabel.setForeground(new Color(44, 62, 80));

        String[] sortOptions = {"Без сортировки", "Дедлайн ↑", "Дедлайн ↓"};
        sortFilter = new JComboBox<>(sortOptions);
        styleComboBox(sortFilter);
        sortFilter.setPreferredSize(new Dimension(150, 30));

        // Кнопки
        applyFiltersButton = new JButton("Применить");
        resetFiltersButton = new JButton("Сбросить");

        styleFilterButton(applyFiltersButton, new Color(52, 152, 219));
        styleFilterButton(resetFiltersButton, new Color(108, 117, 125));

        applyFiltersButton.setPreferredSize(new Dimension(100, 30));
        resetFiltersButton.setPreferredSize(new Dimension(90, 30));

        applyFiltersButton.addActionListener(e -> applyFilters());
        resetFiltersButton.addActionListener(e -> resetFilters());

        // Добавляем компоненты на панель
        filtersPanel.add(statusLabel);
        filtersPanel.add(statusFilter);
        filtersPanel.add(Box.createHorizontalStrut(10));
        filtersPanel.add(importanceLabel);
        filtersPanel.add(importanceFilter);
        filtersPanel.add(Box.createHorizontalStrut(10));
        filtersPanel.add(sortLabel);
        filtersPanel.add(sortFilter);
        filtersPanel.add(Box.createHorizontalStrut(20));
        filtersPanel.add(applyFiltersButton);
        filtersPanel.add(resetFiltersButton);

        return filtersPanel;
    }

    private void refreshTasksDisplay() {
        // Находим компоненты в tasksPanel
        Component[] components = tasksPanel.getComponents();
        JScrollPane scrollPane = null;
        JLabel titleLabel = null;
        JPanel filtersPanel = null;

        for (Component comp : components) {
            if (comp instanceof JScrollPane) {
                scrollPane = (JScrollPane) comp;
            } else if (comp instanceof JLabel && ((JLabel) comp).getText().equals("Мои задачи")) {
                titleLabel = (JLabel) comp;
            } else if (comp instanceof JPanel) {
                // Проверяем, является ли это панелью фильтров
                JPanel panel = (JPanel) comp;
                if (panel.getComponentCount() > 0 && panel.getComponent(0) instanceof JLabel) {
                    JLabel firstLabel = (JLabel) panel.getComponent(0);
                    if ("Статус:".equals(firstLabel.getText())) {
                        filtersPanel = panel;
                    }
                }
            }
        }

        // Удаляем старую панель с задачами
        if (scrollPane != null) {
            tasksPanel.remove(scrollPane);
        }

        // Создаем обновленную панель задач
        JPanel tasksContentPanel = new JPanel();
        tasksContentPanel.setLayout(new BoxLayout(tasksContentPanel, BoxLayout.Y_AXIS));
        tasksContentPanel.setOpaque(true);
        tasksContentPanel.setBackground(Color.WHITE);

        tasksContentPanel.add(createTableHeader(false));
        tasksContentPanel.add(Box.createVerticalStrut(10));

        if (userTasks != null && !userTasks.isEmpty()) {
            for (Task task : userTasks) {
                addTaskRow(tasksContentPanel, task, false, null);
            }
        } else {
            JLabel noTasksLabel = new JLabel("Задачи не найдены по выбранным фильтрам", SwingConstants.CENTER);
            noTasksLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            noTasksLabel.setForeground(Color.GRAY);
            noTasksLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            tasksContentPanel.add(noTasksLabel);
        }

        // Создаем скролл панель
        JScrollPane newScrollPane = new JScrollPane(tasksContentPanel);
        newScrollPane.setBorder(BorderFactory.createEmptyBorder());
        newScrollPane.setBackground(Color.WHITE);

        // Настраиваем скролл панель
        newScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        newScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Добавляем компоненты обратно
        tasksPanel.removeAll();

        if (titleLabel != null) {
            tasksPanel.add(titleLabel, BorderLayout.NORTH);
        }

        // Добавляем панель фильтров
        JPanel newFiltersPanel = createFiltersPanel();
        tasksPanel.add(newFiltersPanel, BorderLayout.NORTH);

        tasksPanel.add(newScrollPane, BorderLayout.CENTER);

        tasksPanel.revalidate();
        tasksPanel.repaint();
    }

    private JPanel createAllTasksFiltersPanel() {
        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filtersPanel.setOpaque(true); // Белый фон
        filtersPanel.setBackground(Color.WHITE);
        filtersPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        filtersPanel.setPreferredSize(new Dimension(getWidth(), 80));

        // Фильтр по статусу
        JLabel statusLabel = new JLabel("Статус:");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusLabel.setForeground(Color.BLACK); // Черный текст

        String[] statusOptions = {"Все статусы", "НЕ_НАЧАТА", "В_РАБОТЕ", "ЗАВЕРШЕНА", "НА_ДОРАБОТКЕ"};
        allTasksStatusFilter = new JComboBox<>(statusOptions);
        styleComboBox(allTasksStatusFilter);
        allTasksStatusFilter.setPreferredSize(new Dimension(140, 30));

        // Фильтр по важности
        JLabel importanceLabel = new JLabel("Важность:");
        importanceLabel.setFont(new Font("Arial", Font.BOLD, 12));
        importanceLabel.setForeground(Color.BLACK); // Черный текст

        String[] importanceOptions = {"Все приоритеты", "СРОЧНАЯ", "НАДО_ПОТОРОПИТЬСЯ", "МОЖЕТ_ПОДОЖДАТЬ"};
        allTasksImportanceFilter = new JComboBox<>(importanceOptions);
        styleComboBox(allTasksImportanceFilter);
        allTasksImportanceFilter.setPreferredSize(new Dimension(140, 30));

        // Сортировка по дедлайну
        JLabel sortLabel = new JLabel("Сортировка:");
        sortLabel.setFont(new Font("Arial", Font.BOLD, 12));
        sortLabel.setForeground(Color.BLACK); // Черный текст

        String[] sortOptions = {"Без сортировки", "Дедлайн ↑", "Дедлайн ↓"};
        allTasksSortFilter = new JComboBox<>(sortOptions);
        styleComboBox(allTasksSortFilter);
        allTasksSortFilter.setPreferredSize(new Dimension(150, 30));

        // Кнопки
        allTasksApplyFiltersButton = new JButton("Применить");
        allTasksResetFiltersButton = new JButton("Сбросить");

        styleFilterButton(allTasksApplyFiltersButton, new Color(52, 152, 219));
        styleFilterButton(allTasksResetFiltersButton, new Color(108, 117, 125));

        allTasksApplyFiltersButton.setPreferredSize(new Dimension(100, 30));
        allTasksResetFiltersButton.setPreferredSize(new Dimension(90, 30));

        allTasksApplyFiltersButton.addActionListener(e -> applyAllTasksFilters());
        allTasksResetFiltersButton.addActionListener(e -> resetAllTasksFilters());

        // Добавляем компоненты на панель
        filtersPanel.add(statusLabel);
        filtersPanel.add(allTasksStatusFilter);
        filtersPanel.add(Box.createHorizontalStrut(10));
        filtersPanel.add(importanceLabel);
        filtersPanel.add(allTasksImportanceFilter);
        filtersPanel.add(Box.createHorizontalStrut(10));
        filtersPanel.add(sortLabel);
        filtersPanel.add(allTasksSortFilter);
        filtersPanel.add(Box.createHorizontalStrut(20));
        filtersPanel.add(allTasksApplyFiltersButton);
        filtersPanel.add(allTasksResetFiltersButton);

        return filtersPanel;
    }

    private void showMyTasks() {
        tasksPanel.removeAll();

        // Делаем панель непрозрачной с белым фоном
        tasksPanel.setOpaque(true);
        tasksPanel.setBackground(Color.WHITE);
        tasksPanel.setLayout(new BorderLayout());

        JLabel loadingLabel = new JLabel("Загрузка ваших задач...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        loadingLabel.setForeground(Color.GRAY);
        tasksPanel.add(loadingLabel, BorderLayout.CENTER);

        centerPanel.remove(tasksPanel);
        centerPanel.add(tasksPanel, "tasks");
        cardLayout.show(centerPanel, "tasks");
        centerPanel.revalidate();
        centerPanel.repaint();

        new Thread(() -> {
            try {
                System.out.println("DEBUG: Starting to load user tasks...");
                User user = getUserWithTasks();
                SwingUtilities.invokeLater(() -> {
                    tasksPanel.removeAll();
                    tasksPanel.setOpaque(true); // Делаем непрозрачной
                    tasksPanel.setBackground(Color.WHITE); // Белый фон
                    tasksPanel.setLayout(new BorderLayout());

                    JLabel titleLabel = new JLabel("Мои задачи", SwingConstants.CENTER);
                    titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
                    titleLabel.setForeground(Color.BLACK); // Черный текст на белом фоне
                    titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
                    tasksPanel.add(titleLabel, BorderLayout.NORTH);

                    if (user != null && user.getTasks() != null && !user.getTasks().isEmpty()) {
                        userTasks = user.getTasks();
                        originalUserTasks = new ArrayList<>(userTasks);
                        System.out.println("DEBUG: Displaying " + userTasks.size() + " tasks");

                        // Создаем панель с фильтрами
                        JPanel filtersPanel = createFiltersPanel();
                        tasksPanel.add(filtersPanel, BorderLayout.NORTH);

                        // Создаем панель для задач
                        JPanel tasksContentPanel = new JPanel();
                        tasksContentPanel.setLayout(new BoxLayout(tasksContentPanel, BoxLayout.Y_AXIS));
                        tasksContentPanel.setBackground(Color.WHITE);

                        tasksContentPanel.add(createTableHeader(false));
                        tasksContentPanel.add(Box.createVerticalStrut(10));

                        for (Task task : userTasks) {
                            addTaskRow(tasksContentPanel, task, false, null);
                        }

                        // Создаем скролл панель
                        JScrollPane scrollPane = new JScrollPane(tasksContentPanel);
                        scrollPane.setBorder(BorderFactory.createEmptyBorder());
                        scrollPane.setBackground(Color.WHITE);
                        scrollPane.getViewport().setBackground(Color.WHITE); // Важно: фон viewport

                        // Настраиваем скролл панель
                        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

                        tasksPanel.add(scrollPane, BorderLayout.CENTER);

                    } else {
                        System.out.println("DEBUG: No tasks found for user");

                        JPanel noTasksPanel = new JPanel(new BorderLayout());
                        noTasksPanel.setOpaque(true); // Непрозрачная
                        noTasksPanel.setBackground(Color.WHITE); // Белый фон
                        JLabel noTasksLabel = new JLabel("Задачи не найдены", SwingConstants.CENTER);
                        noTasksLabel.setFont(new Font("Arial", Font.ITALIC, 16));
                        noTasksLabel.setForeground(Color.GRAY);
                        noTasksPanel.add(noTasksLabel, BorderLayout.CENTER);

                        tasksPanel.add(noTasksPanel, BorderLayout.CENTER);
                        userTasks = new ArrayList<>();
                        originalUserTasks = new ArrayList<>();
                    }

                    tasksPanel.revalidate();
                    tasksPanel.repaint();
                });
            } catch (Exception e) {
                System.out.println("DEBUG: Error in showMyTasks: " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    tasksPanel.removeAll();
                    tasksPanel.setOpaque(true);
                    tasksPanel.setBackground(Color.WHITE);
                    showErrorPanel(tasksPanel, "Ошибка загрузки задач: " + e.getMessage());
                    tasksPanel.revalidate();
                    tasksPanel.repaint();
                });
            }
        }).start();
    }

    private void refreshAllTasksDisplay(List<Task> tasksToDisplay) {
        // Убеждаемся, что панель непрозрачная
        tasksPanel.setOpaque(true);
        tasksPanel.setBackground(Color.WHITE);

        // Находим контейнер всех задач
        Component[] components = tasksPanel.getComponents();
        JScrollPane scrollPane = null;
        JLabel titleLabel = null;
        JPanel filtersPanel = null;

        for (Component comp : components) {
            if (comp instanceof JScrollPane) {
                scrollPane = (JScrollPane) comp;
            } else if (comp instanceof JLabel && ((JLabel) comp).getText().contains("пользователей")) {
                titleLabel = (JLabel) comp;
            } else if (comp instanceof JPanel) {
                // Проверяем, является ли это панелью фильтров
                JPanel panel = (JPanel) comp;
                if (panel.getComponentCount() > 0 && panel.getComponent(0) instanceof JLabel) {
                    JLabel firstLabel = (JLabel) panel.getComponent(0);
                    if ("Статус:".equals(firstLabel.getText())) {
                        filtersPanel = panel;
                    }
                }
            }
        }

        // Удаляем старую панель с задачами
        if (scrollPane != null) {
            tasksPanel.remove(scrollPane);
        }

        // Создаем обновленную панель задач
        JPanel tasksContentPanel = new JPanel();
        tasksContentPanel.setLayout(new BoxLayout(tasksContentPanel, BoxLayout.Y_AXIS));
        tasksContentPanel.setOpaque(true);
        tasksContentPanel.setBackground(Color.WHITE);

        tasksContentPanel.add(createTableHeader(true));
        tasksContentPanel.add(Box.createVerticalStrut(10));

        if (tasksToDisplay != null && !tasksToDisplay.isEmpty()) {
            // Для отображения нам нужно знать, к какому пользователю принадлежит каждая задача
            // В этом упрощенном варианте просто отображаем задачи без информации о пользователе
            for (Task task : tasksToDisplay) {
                addTaskRow(tasksContentPanel, task, true, "Неизвестный пользователь");
            }
        } else {
            JLabel noTasksLabel = new JLabel("Задачи не найдены по выбранным фильтрам", SwingConstants.CENTER);
            noTasksLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            noTasksLabel.setForeground(Color.GRAY);
            noTasksLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            tasksContentPanel.add(noTasksLabel);
        }

        // Создаем скролл панель
        JScrollPane newScrollPane = new JScrollPane(tasksContentPanel);
        newScrollPane.setBorder(BorderFactory.createEmptyBorder());
        newScrollPane.setBackground(Color.WHITE);
        newScrollPane.getViewport().setBackground(Color.WHITE); // Фон viewport

        // Настраиваем скролл панель
        newScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        newScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Добавляем компоненты обратно
        tasksPanel.removeAll();

        if (titleLabel != null) {
            tasksPanel.add(titleLabel, BorderLayout.NORTH);
        }

        // Добавляем панель фильтров
        JPanel newFiltersPanel = createAllTasksFiltersPanel();
        tasksPanel.add(newFiltersPanel, BorderLayout.NORTH);

        tasksPanel.add(newScrollPane, BorderLayout.CENTER);

        tasksPanel.revalidate();
        tasksPanel.repaint();
    }

    private void showAllUsersTasks() {
        tasksPanel.removeAll();

        // Делаем панель непрозрачной с белым фоном
        tasksPanel.setOpaque(true);
        tasksPanel.setBackground(Color.WHITE);
        tasksPanel.setLayout(new BorderLayout());

        JLabel loadingLabel = new JLabel("Загрузка задач...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        loadingLabel.setForeground(Color.GRAY);
        tasksPanel.add(loadingLabel, BorderLayout.CENTER);

        centerPanel.remove(tasksPanel);
        centerPanel.add(tasksPanel, "alltasks");
        cardLayout.show(centerPanel, "alltasks");
        centerPanel.revalidate();
        centerPanel.repaint();

        new Thread(() -> {
            try {
                List<User> users = getAllUsersWithTasks();
                SwingUtilities.invokeLater(() -> {
                    tasksPanel.removeAll();
                    tasksPanel.setOpaque(true);
                    tasksPanel.setBackground(Color.WHITE);
                    tasksPanel.setLayout(new BorderLayout());

                    JLabel titleLabel = new JLabel("Задачи других пользователей", SwingConstants.CENTER);
                    titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
                    titleLabel.setForeground(Color.BLACK);
                    titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
                    tasksPanel.add(titleLabel, BorderLayout.NORTH);

                    if (users != null && !users.isEmpty()) {
                        // Создаем панель с фильтрами
                        JPanel filtersPanel = createAllTasksFiltersPanel();
                        tasksPanel.add(filtersPanel, BorderLayout.NORTH);

                        originalAllUsersTasks = new ArrayList<>();
                        Map<Task, String> taskUserMap = new HashMap<>();

                        for (User user : users) {
                            if (user.getTasks() != null) {
                                for (Task task : user.getTasks()) {
                                    originalAllUsersTasks.add(task);
                                    taskUserMap.put(task, user.getUsername());
                                }
                            }
                        }

                        System.out.println("DEBUG: Loaded " + originalAllUsersTasks.size() + " tasks from all users");

                        // Создаем панель для задач
                        JPanel tasksContentPanel = new JPanel();
                        tasksContentPanel.setLayout(new BoxLayout(tasksContentPanel, BoxLayout.Y_AXIS));
                        tasksContentPanel.setBackground(Color.WHITE);

                        tasksContentPanel.add(createTableHeader(true));
                        tasksContentPanel.add(Box.createVerticalStrut(10));

                        for (User user : users) {
                            if (user.getTasks() != null) {
                                for (Task task : user.getTasks()) {
                                    addTaskRow(tasksContentPanel, task, true, user.getUsername());
                                }
                            }
                        }

                        // Создаем скролл панель
                        JScrollPane scrollPane = new JScrollPane(tasksContentPanel);
                        scrollPane.setBorder(BorderFactory.createEmptyBorder());
                        scrollPane.setBackground(Color.WHITE);
                        scrollPane.getViewport().setBackground(Color.WHITE);

                        // Настраиваем скролл панель
                        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

                        tasksPanel.add(scrollPane, BorderLayout.CENTER);

                    } else {
                        System.out.println("DEBUG: No tasks found for all users");

                        JPanel noTasksPanel = new JPanel(new BorderLayout());
                        noTasksPanel.setOpaque(true);
                        noTasksPanel.setBackground(Color.WHITE);
                        JLabel noTasksLabel = new JLabel("Задачи не найдены", SwingConstants.CENTER);
                        noTasksLabel.setFont(new Font("Arial", Font.ITALIC, 16));
                        noTasksLabel.setForeground(Color.GRAY);
                        noTasksPanel.add(noTasksLabel, BorderLayout.CENTER);

                        tasksPanel.add(noTasksPanel, BorderLayout.CENTER);
                        originalAllUsersTasks = new ArrayList<>();
                    }

                    tasksPanel.revalidate();
                    tasksPanel.repaint();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    tasksPanel.removeAll();
                    tasksPanel.setOpaque(true);
                    tasksPanel.setBackground(Color.WHITE);
                    showErrorPanel(tasksPanel, "Ошибка загрузки: " + e.getMessage());
                    tasksPanel.revalidate();
                    tasksPanel.repaint();
                });
            }
        }).start();
    }


    private JPanel createModernTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false); // Сделали прозрачной - убираем затемненную полоску
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Левая часть...
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);

        // Информация о пользователе...
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Личный кабинет");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userInfoLabel = new JLabel("Добро пожаловать, " + username + "!");
        userInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userInfoLabel.setForeground(new Color(200, 200, 200));
        userInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(welcomeLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(userInfoLabel);

        // Панель с кнопками...
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        navPanel.setOpaque(false);
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton dashboardButton = createVisibleButton("Главная");
        JButton myTasksButton = createVisibleButton("Мои задачи");
        JButton allTasksButton = createVisibleButton("Все задачи");
        JButton profileButton = createVisibleButton("Мой профиль");

        dashboardButton.addActionListener(e -> showDashboard());
        myTasksButton.addActionListener(e -> showMyTasks());
        allTasksButton.addActionListener(e -> showAllUsersTasks());
        profileButton.addActionListener(e -> showMyProfile());

        navPanel.add(dashboardButton);
        navPanel.add(myTasksButton);
        navPanel.add(allTasksButton);
        navPanel.add(profileButton);

        leftPanel.add(infoPanel, BorderLayout.NORTH);
        leftPanel.add(navPanel, BorderLayout.CENTER);

        // Правая часть...
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);

        JButton logoutButton = createVisibleButton("Выйти");
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.addActionListener(e -> showLogoutConfirmationDialog());

        rightPanel.add(logoutButton);

        topPanel.add(leftPanel, BorderLayout.CENTER);
        topPanel.add(rightPanel, BorderLayout.EAST);

        return topPanel;
    }

    // Обновленная карточка статистики с цифрами того же цвета, что и рамки
    private JPanel createModernStatCard(String title, String value, Color color) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Полупрозрачный фон карточки
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Рамка цвета
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 180));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 20, 20);

                g2.dispose();
            }
        };

        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(200, 120));
        card.setMaximumSize(new Dimension(200, 120));
        card.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        // Заголовок
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(220, 220, 220));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Значение - теперь того же цвета, что и рамка
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(color); // Используем тот же цвет, что и для рамки
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // Обновленная панель статистики
    private JPanel createModernStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setOpaque(false);

        JLabel statsTitle = new JLabel("Статистика ваших задач", SwingConstants.CENTER);
        statsTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        statsTitle.setForeground(Color.WHITE);
        statsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel cardsContainer = new JPanel(new GridLayout(1, 4, 20, 0));
        cardsContainer.setOpaque(false);
        cardsContainer.setMaximumSize(new Dimension(900, 140));
        cardsContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        int totalTasks = allUsersTasks != null ? allUsersTasks.size() : 0;
        int notStarted = getTasksCountByStatusFromAll("НЕ_НАЧАТА");
        int inProgress = getTasksCountByStatusFromAll("В_РАБОТЕ");
        int completed = getTasksCountByStatusFromAll("ЗАВЕРШЕНА");

        // Используем те же цвета для цифр, что и для рамок
        cardsContainer.add(createModernStatCard("Всего задач", String.valueOf(totalTasks),
                new Color(70, 130, 180))); // Синий
        cardsContainer.add(createModernStatCard("Не начаты", String.valueOf(notStarted),
                new Color(255, 165, 0))); // Оранжевый
        cardsContainer.add(createModernStatCard("В работе", String.valueOf(inProgress),
                new Color(50, 205, 50))); // Зеленый
        cardsContainer.add(createModernStatCard("Завершено", String.valueOf(completed),
                new Color(46, 204, 113))); // Ярко-зеленый

        statsPanel.add(statsTitle);
        statsPanel.add(Box.createVerticalStrut(30));
        statsPanel.add(cardsContainer);

        return statsPanel;
    }

    private JButton createVisibleButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(30, 144, 255)); // Синий фон
        button.setFocusPainted(false);
        button.setBorderPainted(false); // Убираем стандартную рамку
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(180, 45)); // Увеличили ширину с 140 до 180 для длинного текста
        button.setMaximumSize(new Dimension(180, 45)); // Ограничиваем максимум, чтобы кнопки не растягивались
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Внутренний отступ без видимой рамки (10 сверху/снизу, 20 слева/справа)
        return button;
    }
}