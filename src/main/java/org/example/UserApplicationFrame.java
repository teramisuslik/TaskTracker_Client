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

    public UserApplicationFrame(String token, Map<String, Object> userInfo) {
        this.authToken = token;
        this.userInfo = userInfo;
        this.username = extractUsernameFromUserInfo(userInfo);

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

    private String extractUsernameFromUserInfo(Map<String, Object> userInfo) {
        if (userInfo == null) return "user";

        if (userInfo.containsKey("username")) return userInfo.get("username").toString();
        if (userInfo.containsKey("sub")) return userInfo.get("sub").toString();
        if (userInfo.containsKey("preferred_username")) return userInfo.get("preferred_username").toString();

        return "user";
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
        JButton allTasksButton = new JButton("Все задачи");
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

    private JPanel createTableHeader(boolean showUsername) {
        int columns = showUsername ? 6 : 5; // Добавляем колонку для комментариев
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

        String[] headers = {"Название задачи", "Статус", "Приоритет", "Дедлайн", "Комментарии"};
        for (String header : headers) {
            JLabel headerLabel = new JLabel(header);
            headerLabel.setFont(new Font("Arial", Font.BOLD, 12));
            headerLabel.setForeground(new Color(44, 62, 80));
            headerPanel.add(headerLabel);
        }

        return headerPanel;
    }

    private void addTaskRow(JPanel parent, Task task, boolean showUsername, String username) {
        int columns = showUsername ? 6 : 5; // Добавляем колонку для комментариев
        JPanel taskRow = new JPanel(new GridLayout(1, columns, 10, 5));
        taskRow.setBackground(Color.WHITE);
        taskRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        taskRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        if (showUsername) {
            JLabel userLabel = new JLabel(username != null ? username : "");
            userLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            taskRow.add(userLabel);
        }

        JLabel titleLabel = new JLabel(task.getTitle() != null ? task.getTitle() : "");
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        taskRow.add(titleLabel);

        JLabel statusLabel = new JLabel(task.getStatus() != null ? task.getStatus() : "");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusLabel.setForeground(getStatusColor(task.getStatus()));
        taskRow.add(statusLabel);

        JLabel priorityLabel = new JLabel(task.getImportance() != null ? task.getImportance().toString() : "");
        priorityLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        taskRow.add(priorityLabel);

        String deadline = task.getDeadline() != null ? task.getDeadline().toString() : "";
        if (deadline.contains("T")) deadline = deadline.substring(0, deadline.indexOf("T"));
        JLabel deadlineLabel = new JLabel(deadline);
        deadlineLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        taskRow.add(deadlineLabel);

        // Колонка комментариев
        int commentCount = task.getComments() != null ? task.getComments().size() : 0;
        JLabel commentsLabel = new JLabel(commentCount + " коммент.");
        commentsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        commentsLabel.setForeground(commentCount > 0 ? new Color(52, 152, 219) : Color.GRAY);
        taskRow.add(commentsLabel);

        // Добавляем возможность просмотра комментариев
        if (commentCount > 0) {
            taskRow.setCursor(new Cursor(Cursor.HAND_CURSOR));
            taskRow.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    showTaskComments(task);
                }
            });
        }

        parent.add(taskRow);
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

    private Color getStatusColor(String status) {
        if (status == null) return Color.BLACK;
        switch (status) {
            case "ЗАВЕРШЕНА": return new Color(46, 204, 113);
            case "В_РАБОТЕ": return new Color(241, 196, 15);
            case "НЕ_НАЧАТА": return new Color(52, 152, 219);
            case "НА_ДОРАБОТКЕ": return new Color(231, 76, 60);
            default: return Color.BLACK;
        }
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

    private void showLogoutConfirmationDialog() {
        JDialog confirmDialog = new JDialog(this, "Подтверждение выхода", true);
        confirmDialog.setSize(400, 250);
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel iconLabel = new JLabel("?", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Arial", Font.BOLD, 48));
        iconLabel.setForeground(new Color(241, 196, 15));

        JLabel titleLabel = new JLabel("Подтверждение выхода", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(44, 62, 80));

        JLabel messageLabel = new JLabel("<html><center>Вы уверены, что хотите выйти из системы?</center></html>", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(127, 140, 141));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton yesButton = new JButton("Да, выйти");
        JButton noButton = new JButton("Нет, остаться");
        styleLogoutButton(yesButton, new Color(220, 53, 69));
        styleLogoutButton(noButton, new Color(52, 152, 219));

        yesButton.addActionListener(e -> {
            confirmDialog.dispose();
            performLogout();
        });
        noButton.addActionListener(e -> confirmDialog.dispose());

        buttonPanel.add(noButton);
        buttonPanel.add(yesButton);

        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(buttonPanel);

        confirmDialog.add(contentPanel, BorderLayout.CENTER);
        confirmDialog.pack();
        confirmDialog.setVisible(true);
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

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(new Color(248, 249, 250));

        JLabel statsTitle = new JLabel("Статистика всех задач", SwingConstants.CENTER);
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

        System.out.println("DEBUG: Statistics - Total: " + totalTasks +
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

    private int getTasksCountByStatusFromAll(String status) {
        if (allUsersTasks == null) return 0;
        return (int) allUsersTasks.stream()
                .filter(task -> task.getStatus() != null && task.getStatus().equals(status))
                .count();
    }

    private void showDashboard() {
        loadUserTasksForStatistics();
    }

    private User getUserWithTasks() {
        try {
            String encodedUsername = java.net.URLEncoder.encode(this.username, "UTF-8");
            String url = "http://localhost:8080/user?username=" + encodedUsername;

            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new java.net.URI(url))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

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

    // Обновите также метод showMyTasks для отладки
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
                    titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
                    tasksPanel.add(titleLabel, BorderLayout.NORTH);

                    if (user != null && user.getTasks() != null && !user.getTasks().isEmpty()) {
                        userTasks = user.getTasks();
                        System.out.println("DEBUG: Displaying " + userTasks.size() + " tasks");
                        displayTasks(tasksPanel, user.getTasks(), false);
                    } else {
                        System.out.println("DEBUG: No tasks found for user");
                        showNoTasksMessage(tasksPanel);
                        userTasks = new ArrayList<>();
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

    private void loadUserTasksForStatistics() {
        new Thread(() -> {
            try {
                System.out.println("DEBUG: Loading tasks for statistics...");
                List<User> allUsers = getAllUsersWithTasks();
                if (allUsers != null) {
                    List<Task> allTasks = new ArrayList<>();
                    for (User user : allUsers) {
                        if (user.getTasks() != null) {
                            allTasks.addAll(user.getTasks());
                        }
                    }
                    allUsersTasks = allTasks;
                    System.out.println("DEBUG: Loaded " + allTasks.size() + " tasks for statistics");

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
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Error loading tasks for statistics: " + e.getMessage());
                e.printStackTrace();
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

    private User parseSingleUserFromAllUsers(String userJson) {
        try {
            System.out.println("DEBUG: Parsing single user: " + userJson);

            User user = new User();
            String username = extractValue(userJson, "username");
            if (username == null) return null;

            user.setUsername(username);

            // Парсим задачи пользователя
            if (userJson.contains("\"tasks\":")) {
                int tasksStart = userJson.indexOf("\"tasks\":[") + 9;
                int tasksEnd = userJson.indexOf("]", tasksStart);
                if (tasksEnd > tasksStart) {
                    String tasksArray = userJson.substring(tasksStart, tasksEnd);
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

    private List<Task> parseTasksArray(String tasksJson) {
        List<Task> tasks = new ArrayList<>();
        try {
            if (tasksJson == null || tasksJson.trim().isEmpty()) {
                System.out.println("DEBUG: Empty tasks array");
                return tasks;
            }

            System.out.println("DEBUG: Parsing tasks array: " + tasksJson);

            // Разделяем задачи по },{
            String[] taskStrings = tasksJson.split("\\},\\s*\\{");
            System.out.println("DEBUG: Found " + taskStrings.length + " task strings");

            for (int i = 0; i < taskStrings.length; i++) {
                String taskStr = taskStrings[i];
                if (i > 0) taskStr = "{" + taskStr;
                if (i < taskStrings.length - 1) taskStr = taskStr + "}";

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

    // Обновите также метод extractValue для лучшего парсинга
    private String extractValue(String json, String key) {
        try {
            // Пробуем найти строковое значение в кавычках
            String searchStr = "\"" + key + "\":\"";
            int start = json.indexOf(searchStr);
            if (start != -1) {
                start += searchStr.length();
                int end = json.indexOf("\"", start);
                if (end != -1) {
                    return json.substring(start, end);
                }
            }

            // Пробуем найти числовое значение (без кавычек)
            searchStr = "\"" + key + "\":";
            start = json.indexOf(searchStr);
            if (start != -1) {
                start += searchStr.length();
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (end == -1) end = json.indexOf("]", start);
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

            // Парсим задачи
            if (json.contains("\"tasks\"")) {
                int tasksStart = json.indexOf("\"tasks\":[") + 9;
                int tasksEnd = json.indexOf("]", tasksStart);
                if (tasksEnd > tasksStart) {
                    String tasksArray = json.substring(tasksStart, tasksEnd);
                    System.out.println("DEBUG: Tasks array for current user: " + tasksArray);

                    // Разделяем задачи по },{
                    String[] taskStrings = tasksArray.split("\\},\\s*\\{");
                    System.out.println("DEBUG: Found " + taskStrings.length + " task strings for current user");

                    for (int i = 0; i < taskStrings.length; i++) {
                        String taskStr = taskStrings[i];
                        if (i > 0) taskStr = "{" + taskStr;
                        if (i < taskStrings.length - 1) taskStr = taskStr + "}";

                        Task task = parseSingleTask(taskStr);
                        if (task != null && task.getTitle() != null) {
                            tasks.add(task);
                        }
                    }
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
}