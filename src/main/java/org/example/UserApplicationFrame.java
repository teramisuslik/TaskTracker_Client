package org.example;

import javax.swing.*;
import java.awt.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
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

    public UserApplicationFrame(String token, Map<String, Object> userInfo) {
        this.authToken = token;
        this.userInfo = userInfo;
        this.username = extractUsernameFromUserInfo(userInfo);

        System.out.println("DEBUG: UserApplicationFrame created for user: " + this.username);
        System.out.println("DEBUG: UserInfo: " + userInfo);

        setTitle("Личный кабинет - " + username);
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel(new BorderLayout());
        JPanel topPanel = createTopPanel();

        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);

        JPanel welcomePanel = createWelcomePanel();
        tasksPanel = new JPanel(new BorderLayout());

        centerPanel.add(welcomePanel, "welcome");
        centerPanel.add(tasksPanel, "tasks");

        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(centerPanel, BorderLayout.CENTER);
        add(contentPane);

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

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 245, 245));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        topPanel.setPreferredSize(new Dimension(getWidth(), 140));

        topPanel.add(createLeftInfoPanel(), BorderLayout.CENTER);
        topPanel.add(createRightButtonsPanel(), BorderLayout.EAST);

        return topPanel;
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

    private JPanel createRightButtonsPanel() {
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(createRedLogoutButton());
        return rightPanel;
    }

    private JPanel loadTasksPanel() {
        JPanel tasksPanel = new JPanel(new BorderLayout());
        tasksPanel.setBackground(Color.WHITE);
        tasksPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Мои задачи", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(Color.WHITE);
        JLabel loadingLabel = new JLabel("Загрузка задач...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        loadingLabel.setForeground(Color.GRAY);
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);

        tasksPanel.add(titleLabel, BorderLayout.NORTH);
        tasksPanel.add(loadingPanel, BorderLayout.CENTER);
        loadTasksFromServer(tasksPanel, loadingPanel);

        return tasksPanel;
    }

    private void loadTasksFromServer(JPanel tasksPanel, JPanel loadingPanel) {
        new Thread(() -> {
            try {
                User user = getUserWithTasks();
                SwingUtilities.invokeLater(() -> {
                    loadingPanel.removeAll();
                    tasksPanel.removeAll();

                    JLabel titleLabel = new JLabel("Мои задачи", SwingConstants.CENTER);
                    titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
                    titleLabel.setForeground(new Color(44, 62, 80));
                    titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
                    tasksPanel.add(titleLabel, BorderLayout.NORTH);

                    if (user != null && user.getTasks() != null && !user.getTasks().isEmpty()) {
                        displayTasks(tasksPanel, user.getTasks(), false);
                    } else {
                        showNoTasksMessage(tasksPanel);
                    }
                    tasksPanel.revalidate();
                    tasksPanel.repaint();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    showErrorPanel(tasksPanel, "Ошибка загрузки задач: " + e.getMessage());
                    tasksPanel.revalidate();
                    tasksPanel.repaint();
                });
            }
        }).start();
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

    private void showTaskComments(Task task) {
        JDialog commentsDialog = new JDialog(this, "Комментарии к задаче: " + task.getTitle(), true);
        commentsDialog.setSize(500, 400);
        commentsDialog.setLocationRelativeTo(this);
        commentsDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Заголовок
        JLabel titleLabel = new JLabel("Комментарии к задаче: " + task.getTitle(), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        // Список комментариев
        JPanel commentsListPanel = new JPanel();
        commentsListPanel.setLayout(new BoxLayout(commentsListPanel, BoxLayout.Y_AXIS));
        commentsListPanel.setBackground(Color.WHITE);

        if (task.getComments() != null && !task.getComments().isEmpty()) {
            for (Comment comment : task.getComments()) {
                JPanel commentPanel = createCommentPanel(comment);
                commentsListPanel.add(commentPanel);
                commentsListPanel.add(Box.createVerticalStrut(5));
            }
        } else {
            JLabel noCommentsLabel = new JLabel("Комментарии отсутствуют", SwingConstants.CENTER);
            noCommentsLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            noCommentsLabel.setForeground(Color.GRAY);
            commentsListPanel.add(noCommentsLabel);
        }

        JScrollPane scrollPane = new JScrollPane(commentsListPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Кнопка закрытия
        JButton closeButton = new JButton("Закрыть");
        closeButton.addActionListener(e -> commentsDialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        commentsDialog.add(contentPanel);
        commentsDialog.setVisible(true);
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

    private void showAllUsersTasks() {
        tasksPanel.removeAll();
        JPanel newTasksPanel = loadAllUsersTasksPanel();
        centerPanel.remove(tasksPanel);
        tasksPanel = newTasksPanel;
        centerPanel.add(tasksPanel, "alltasks");
        cardLayout.show(centerPanel, "alltasks");
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    private JPanel loadAllUsersTasksPanel() {
        JPanel allTasksPanel = new JPanel(new BorderLayout());
        allTasksPanel.setBackground(Color.WHITE);
        allTasksPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Все задачи пользователей", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(Color.WHITE);
        JLabel loadingLabel = new JLabel("Загрузка всех задач...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        loadingLabel.setForeground(Color.GRAY);
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);

        allTasksPanel.add(titleLabel, BorderLayout.NORTH);
        allTasksPanel.add(loadingPanel, BorderLayout.CENTER);
        loadAllUsersTasksFromServer(allTasksPanel, loadingPanel);

        return allTasksPanel;
    }

    private void loadAllUsersTasksFromServer(JPanel allTasksPanel, JPanel loadingPanel) {
        new Thread(() -> {
            try {
                List<User> users = getAllUsersWithTasks();
                SwingUtilities.invokeLater(() -> {
                    loadingPanel.removeAll();
                    allTasksPanel.removeAll();
                    allTasksPanel.setLayout(new BorderLayout());

                    JLabel titleLabel = new JLabel("Все задачи пользователей", SwingConstants.CENTER);
                    titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
                    titleLabel.setForeground(new Color(44, 62, 80));
                    titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
                    allTasksPanel.add(titleLabel, BorderLayout.NORTH);

                    if (users != null && !users.isEmpty()) {
                        displayAllUsersTasks(allTasksPanel, users);
                    } else {
                        showNoTasksMessage(allTasksPanel);
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

    private void displayAllUsersTasks(JPanel allTasksPanel, List<User> users) {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        contentPanel.add(createTableHeader(true));
        contentPanel.add(Box.createVerticalStrut(10));

        for (User user : users) {
            if (user.getTasks() != null) {
                for (Task task : user.getTasks()) {
                    addTaskRow(contentPanel, task, true, user.getUsername());
                }
            }
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        allTasksPanel.add(scrollPane, BorderLayout.CENTER);
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

    private User parseSingleUser(String userJson) {
        try {
            User user = new User();
            String username = extractValue(userJson, "username");
            if (username == null) return null;

            user.setUsername(username);

            if (userJson.contains("\"tasks\":")) {
                int tasksStart = userJson.indexOf("\"tasks\":[") + 9;
                int tasksEnd = userJson.lastIndexOf("]");
                if (tasksEnd > tasksStart) {
                    String tasksArray = userJson.substring(tasksStart, tasksEnd);
                    user.setTasks(parseTasksArray(tasksArray));
                }
            }
            return user;
        } catch (Exception e) {
            return null;
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

    private JButton createRedLogoutButton() {
        JButton logoutButton = new JButton("Выйти");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> showLogoutConfirmationDialog());
        return logoutButton;
    }

    private void styleLogoutButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void performLogout() {
        LoginFrame.clearToken();
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    private void onWindowClosing() {
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

    private String getRoleDisplayName(String role) {
        switch (role.toUpperCase()) {
            case "ADMIN": return "Администратор";
            case "USER": return "Пользователь";
            default: return role;
        }
    }

    private void showMyProfile() {
        try {
            Map<String, Object> userInfo = getUserInfo();

            JDialog profileDialog = new JDialog(this, "Мой профиль", true);
            profileDialog.setSize(500, 400);
            profileDialog.setLocationRelativeTo(this);
            profileDialog.setLayout(new BorderLayout());
            profileDialog.setResizable(false);

            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setBackground(Color.WHITE);
            contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

            JLabel titleLabel = new JLabel("Информация о профиле", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(new Color(44, 62, 80));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel iconLabel = new JLabel("👤", SwingConstants.CENTER);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new GridLayout(4, 2, 10, 15));
            infoPanel.setBackground(Color.WHITE);
            infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
            infoPanel.setMaximumSize(new Dimension(400, 200));

            String username = userInfo.containsKey("username") ? userInfo.get("username").toString() : this.username;
            String role = userInfo.containsKey("role") ? userInfo.get("role").toString() : "USER";

            addInfoRow(infoPanel, "Логин:", username);
            addInfoRow(infoPanel, "Роль:", getRoleDisplayName(role));
            addInfoRow(infoPanel, "Статус:", "Активен");

            JButton closeButton = new JButton("Закрыть");
            closeButton.setFont(new Font("Arial", Font.BOLD, 14));
            closeButton.setForeground(Color.WHITE);
            closeButton.setBackground(new Color(52, 152, 219));
            closeButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
            closeButton.setFocusPainted(false);
            closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            closeButton.addActionListener(e -> profileDialog.dispose());

            closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    closeButton.setBackground(new Color(41, 128, 185));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    closeButton.setBackground(new Color(52, 152, 219));
                }
            });

            contentPanel.add(titleLabel);
            contentPanel.add(Box.createVerticalStrut(20));
            contentPanel.add(iconLabel);
            contentPanel.add(Box.createVerticalStrut(20));
            contentPanel.add(infoPanel);
            contentPanel.add(Box.createVerticalStrut(30));
            contentPanel.add(closeButton);

            profileDialog.add(contentPanel, BorderLayout.CENTER);
            profileDialog.getRootPane().setDefaultButton(closeButton);
            profileDialog.pack();
            profileDialog.setLocationRelativeTo(this);
            profileDialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка загрузки информации о профиле: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getUserTasksCount() {
        return userTasks != null ? userTasks.size() : 0;
    }

    private int getTasksCountByStatus(String status) {
        if (userTasks == null) return 0;

        return (int) userTasks.stream()
                .filter(task -> task.getStatus() != null && task.getStatus().equals(status))
                .count();
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

    private Task parseSingleTask(String taskJson) {
        try {
            System.out.println("DEBUG: Parsing task: " + taskJson);

            String title = extractValue(taskJson, "title");
            String description = extractValue(taskJson, "description");
            String status = extractValue(taskJson, "status");
            String importance = extractValue(taskJson, "importance");
            String deadline = extractValue(taskJson, "deadline");

            if (title != null) {
                Task task = new Task();
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

    private JPanel createTableHeader(boolean showUsername) {
        int columns = showUsername ? 7 : 6; // Добавляем колонку для действий
        JPanel headerPanel = new JPanel(new GridLayout(1, columns, 10, 5));
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        if (showUsername) {
            JLabel userHeaderLabel = new JLabel("Пользователь");
            userHeaderLabel.setFont(new Font("Arial", Font.BOLD, 12));
            userHeaderLabel.setForeground(new Color(44, 62, 80));
            headerPanel.add(userHeaderLabel);
        }

        String[] headers = {"Название задачи", "Статус", "Приоритет", "Дедлайн", "Комментарии", "Действия"};
        for (String header : headers) {
            JLabel headerLabel = new JLabel(header);
            headerLabel.setFont(new Font("Arial", Font.BOLD, 12));
            headerLabel.setForeground(new Color(44, 62, 80));
            headerPanel.add(headerLabel);
        }

        return headerPanel;
    }

    private void loadUserTasksForStatistics() {
        new Thread(() -> {
            try {
                System.out.println("DEBUG: Loading tasks for statistics for user: " + username);

                // Загружаем задачи ТОЛЬКО текущего пользователя
                User currentUser = getUserWithTasks();
                if (currentUser != null && currentUser.getTasks() != null) {
                    allUsersTasks = currentUser.getTasks(); // Теперь это задачи только текущего пользователя
                    System.out.println("DEBUG: Loaded " + allUsersTasks.size() + " tasks for statistics (current user only)");

                    SwingUtilities.invokeLater(() -> {
                        centerPanel.removeAll();
                        JPanel welcomePanel = createWelcomePanel();
                        centerPanel.add(welcomePanel, "welcome");

                        if (cardLayout != null) {
                            cardLayout.show(centerPanel, "welcome");
                        }

                        centerPanel.revalidate();
                        centerPanel.repaint();
                    });
                } else {
                    allUsersTasks = new ArrayList<>();
                    System.out.println("DEBUG: No tasks found for current user statistics");

                    SwingUtilities.invokeLater(() -> {
                        centerPanel.removeAll();
                        JPanel welcomePanel = createWelcomePanel();
                        centerPanel.add(welcomePanel, "welcome");
                        cardLayout.show(centerPanel, "welcome");
                        centerPanel.revalidate();
                        centerPanel.repaint();
                    });
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Error loading tasks for statistics: " + e.getMessage());
                e.printStackTrace();
                allUsersTasks = new ArrayList<>();

                // Даже при ошибке показываем welcome panel
                SwingUtilities.invokeLater(() -> {
                    centerPanel.removeAll();
                    JPanel welcomePanel = createWelcomePanel();
                    centerPanel.add(welcomePanel, "welcome");
                    cardLayout.show(centerPanel, "welcome");
                    centerPanel.revalidate();
                    centerPanel.repaint();
                });
            }
        }).start();
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

    private boolean sendStatusUpdateToServer(Task task, String newStatus) {
        try {
            String url;

            // Выбираем правильный endpoint в зависимости от нового статуса
            if ("В_РАБОТЕ".equals(newStatus)) {
                url = "http://localhost:8080/markthetaskasinwork?title=" +
                        java.net.URLEncoder.encode(task.getTitle(), "UTF-8");
            } else if ("ЗАВЕРШЕНА".equals(newStatus)) {
                url = "http://localhost:8080/markthetaskascompleted?title=" +
                        java.net.URLEncoder.encode(task.getTitle(), "UTF-8");
            } else {
                System.out.println("DEBUG: Unknown status for update: " + newStatus);
                return false;
            }

            System.out.println("DEBUG: Sending status update to: " + url);

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

    private void updateTaskStatus(Task task, String newStatus) {
        // Создаем красивое диалоговое окно
        JDialog confirmDialog = new JDialog(this, "Подтверждение изменения статуса", true);
        confirmDialog.setSize(450, 300);
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.setLayout(new BorderLayout());
        confirmDialog.setResizable(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Иконка вопроса
        JLabel iconLabel = new JLabel("❓", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setForeground(new Color(52, 152, 219));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Заголовок
        JLabel titleLabel = new JLabel("Изменение статуса задачи", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Информация о задаче
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel taskLabel = new JLabel("Задача: " + task.getTitle());
        taskLabel.setFont(new Font("Arial", Font.BOLD, 14));
        taskLabel.setForeground(new Color(44, 62, 80));
        taskLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel currentStatusLabel = new JLabel("Текущий статус: " + getStatusDisplayName(task.getStatus()));
        currentStatusLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        currentStatusLabel.setForeground(new Color(127, 140, 141));
        currentStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel newStatusLabel = new JLabel("Новый статус: " + getStatusDisplayName(newStatus));
        newStatusLabel.setFont(new Font("Arial", Font.BOLD, 13));
        newStatusLabel.setForeground(getStatusColor(newStatus));
        newStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(taskLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(currentStatusLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(newStatusLabel);

        // Сообщение
        JLabel messageLabel = new JLabel("Вы уверены, что хотите изменить статус задачи?", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(127, 140, 141));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton yesButton = new JButton("Да, изменить");
        JButton noButton = new JButton("Отмена");

        // Стилизация кнопок
        styleConfirmButton(yesButton, new Color(46, 204, 113)); // Зеленая
        styleConfirmButton(noButton, new Color(108, 117, 125)); // Серая

        yesButton.addActionListener(e -> {
            confirmDialog.dispose();
            processStatusUpdate(task, newStatus);
        });
        noButton.addActionListener(e -> confirmDialog.dispose());

        buttonPanel.add(noButton);
        buttonPanel.add(yesButton);

        // Собираем все компоненты
        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(infoPanel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(25));
        contentPanel.add(buttonPanel);

        confirmDialog.add(contentPanel, BorderLayout.CENTER);
        confirmDialog.getRootPane().setDefaultButton(noButton);
        confirmDialog.pack();
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.setVisible(true);
    }

    private void styleConfirmButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
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

    private void showSuccessMessage(String message) {
        JDialog successDialog = new JDialog(this, "Успех", true);
        successDialog.setSize(400, 200);
        successDialog.setLocationRelativeTo(this);
        successDialog.setLayout(new BorderLayout());
        successDialog.setResizable(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel iconLabel = new JLabel("✅", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setForeground(new Color(46, 204, 113));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(44, 62, 80));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton okButton = new JButton("OK");
        styleConfirmButton(okButton, new Color(46, 204, 113));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.addActionListener(e -> successDialog.dispose());

        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(okButton);

        successDialog.add(contentPanel, BorderLayout.CENTER);
        successDialog.getRootPane().setDefaultButton(okButton);
        successDialog.pack();
        successDialog.setLocationRelativeTo(this);
        successDialog.setVisible(true);
    }

    private void showErrorMessage(String message) {
        JDialog errorDialog = new JDialog(this, "Ошибка", true);
        errorDialog.setSize(400, 200);
        errorDialog.setLocationRelativeTo(this);
        errorDialog.setLayout(new BorderLayout());
        errorDialog.setResizable(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel iconLabel = new JLabel("❌", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setForeground(new Color(231, 76, 60));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(44, 62, 80));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton okButton = new JButton("OK");
        styleConfirmButton(okButton, new Color(231, 76, 60));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.addActionListener(e -> errorDialog.dispose());

        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(okButton);

        errorDialog.add(contentPanel, BorderLayout.CENTER);
        errorDialog.getRootPane().setDefaultButton(okButton);
        errorDialog.pack();
        errorDialog.setLocationRelativeTo(this);
        errorDialog.setVisible(true);
    }

    private void showLogoutConfirmationDialog() {
        JDialog confirmDialog = new JDialog(this, "Подтверждение выхода", true);
        confirmDialog.setSize(450, 280);
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.setLayout(new BorderLayout());
        confirmDialog.setResizable(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30)); // Исправленные отступы

        JLabel iconLabel = new JLabel("🚪", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setForeground(new Color(241, 196, 15));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Подтверждение выхода", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Исправленный текст - убрали лишние теги и центрирование
        JLabel messageLabel = new JLabel("Вы уверены, что хотите выйти из системы?");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(127, 140, 141));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton yesButton = new JButton("Да, выйти");
        JButton noButton = new JButton("Нет, остаться");

        styleConfirmButton(yesButton, new Color(220, 53, 69));
        styleConfirmButton(noButton, new Color(108, 117, 125));

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
        confirmDialog.pack();
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.setVisible(true);
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

    private void showMyTasks() {
        tasksPanel.removeAll();

        // Показываем loading
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
                    tasksPanel.setLayout(new BorderLayout());

                    JLabel titleLabel = new JLabel("Мои задачи", SwingConstants.CENTER);
                    titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
                    titleLabel.setForeground(new Color(44, 62, 80));
                    titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
                    tasksPanel.add(titleLabel, BorderLayout.NORTH);

                    if (user != null && user.getTasks() != null && !user.getTasks().isEmpty()) {
                        userTasks = user.getTasks();
                        originalUserTasks = new ArrayList<>(userTasks); // сохраняем оригинальный список
                        System.out.println("DEBUG: Displaying " + userTasks.size() + " tasks");

                        // Создаем панель с фильтрами
                        JPanel filtersPanel = createFiltersPanel();
                        tasksPanel.add(filtersPanel, BorderLayout.NORTH);

                        // Создаем панель для задач с кнопками
                        JPanel tasksContentPanel = new JPanel();
                        tasksContentPanel.setLayout(new BoxLayout(tasksContentPanel, BoxLayout.Y_AXIS));
                        tasksContentPanel.setBackground(Color.WHITE);

                        tasksContentPanel.add(createTableHeader(false));
                        tasksContentPanel.add(Box.createVerticalStrut(10));

                        for (Task task : userTasks) {
                            addTaskRow(tasksContentPanel, task, false, null);
                        }

                        JScrollPane scrollPane = new JScrollPane(tasksContentPanel);
                        scrollPane.setBorder(BorderFactory.createEmptyBorder());
                        tasksPanel.add(scrollPane, BorderLayout.CENTER);

                    } else {
                        System.out.println("DEBUG: No tasks found for user");
                        showNoTasksMessage(tasksPanel);
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
                    showErrorPanel(tasksPanel, "Ошибка загрузки задач: " + e.getMessage());
                    tasksPanel.revalidate();
                    tasksPanel.repaint();
                });
            }
        }).start();
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

    private void refreshTasksDisplay() {
        // Находим компоненты в tasksPanel
        Component[] components = tasksPanel.getComponents();
        JScrollPane scrollPane = null;

        for (Component comp : components) {
            if (comp instanceof JScrollPane) {
                scrollPane = (JScrollPane) comp;
                break;
            }
        }

        if (scrollPane != null) {
            tasksPanel.remove(scrollPane);
        }

        // Создаем обновленную панель задач
        JPanel tasksContentPanel = new JPanel();
        tasksContentPanel.setLayout(new BoxLayout(tasksContentPanel, BoxLayout.Y_AXIS));
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

        JScrollPane newScrollPane = new JScrollPane(tasksContentPanel);
        newScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tasksPanel.add(newScrollPane, BorderLayout.CENTER);

        tasksPanel.revalidate();
        tasksPanel.repaint();
    }

    private void addTaskRow(JPanel parent, Task task, boolean showUsername, String username) {
        int columns = showUsername ? 7 : 6; // Добавляем колонку для действий
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

        // Колонка дедлайна
        String deadline = task.getDeadline() != null ? task.getDeadline().toString() : "";
        if (deadline.contains("T")) deadline = deadline.substring(0, deadline.indexOf("T"));
        JLabel deadlineLabel = new JLabel(deadline);
        deadlineLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        deadlineLabel.setForeground(new Color(44, 62, 80));
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
                    showTaskComments(task);
                }
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    commentsLabel.setForeground(new Color(41, 128, 185));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    commentsLabel.setForeground(new Color(52, 152, 219));
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

    private JPanel createFiltersPanel() {
        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filtersPanel.setBackground(new Color(248, 249, 250));
        filtersPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(15, 20, 15, 20) // увеличили отступы
        ));
        filtersPanel.setPreferredSize(new Dimension(getWidth(), 80)); // увеличили высоту

        // Фильтр по статусу
        JLabel statusLabel = new JLabel("Статус:");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusLabel.setForeground(new Color(44, 62, 80));

        String[] statusOptions = {"Все статусы", "НЕ_НАЧАТА", "В_РАБОТЕ", "ЗАВЕРШЕНА", "НА_ДОРАБОТКЕ"};
        statusFilter = new JComboBox<>(statusOptions);
        styleComboBox(statusFilter);
        statusFilter.setPreferredSize(new Dimension(140, 30)); // увеличили высоту

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

        // Увеличим кнопки
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

    private void showFilterStatusMessage() {
        int totalTasks = originalUserTasks != null ? originalUserTasks.size() : 0;
        int filteredTasks = userTasks != null ? userTasks.size() : 0;

        String statusFilterText = (String) statusFilter.getSelectedItem();
        String importanceFilterText = (String) importanceFilter.getSelectedItem();
        String sortFilterText = (String) sortFilter.getSelectedItem();

        StringBuilder message = new StringBuilder();
        message.append("Показано ").append(filteredTasks).append(" из ").append(totalTasks).append(" задач");

        if (!"Все статусы".equals(statusFilterText)) {
            message.append(" • Статус: ").append(getStatusDisplayName(statusFilterText));
        }
        if (!"Все приоритеты".equals(importanceFilterText)) {
            message.append(" • Приоритет: ").append(getImportanceDisplayName(importanceFilterText));
        }
        if (!"Без сортировки".equals(sortFilterText)) {
            message.append(" • Сортировка: ").append(sortFilterText);
        }

        // Можно добавить временное сообщение в интерфейсе
        System.out.println("DEBUG: " + message.toString());
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
}