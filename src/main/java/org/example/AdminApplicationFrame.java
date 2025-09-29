package org.example;

import javax.swing.*;
import java.awt.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
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

    public AdminApplicationFrame(String token, Map<String, Object> userInfo) {
        this.authToken = token;
        this.userInfo = userInfo;

        String username = userInfo != null && userInfo.containsKey("username") ?
                userInfo.get("username").toString() : "Администратор";

        setTitle("Панель администратора - " + username);
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Основная панель с BorderLayout
        JPanel contentPane = new JPanel(new BorderLayout());

        // Панель для верхней части
        JPanel topPanel = createTopPanel(username);

        // Центральная панель с CardLayout для переключения между разными видами
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);

        // Панель приветствия
        JPanel welcomePanel = createWelcomePanel();

        // Панель для всех задач
        JPanel allTasksPanel = new JPanel(new BorderLayout());

        centerPanel.add(welcomePanel, "welcome");
        centerPanel.add(allTasksPanel, "alltasks");

        // Добавляем компоненты
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(centerPanel, BorderLayout.CENTER);

        add(contentPane);

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

    private void onWindowClosing() {
        // При закрытии окна администратора возвращаемся к окну логина
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
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

    private void showUserManagement() {
        showInfoDialog("Функция управления пользователями", "Эта функция будет реализована в следующем обновлении.");
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

    private void showLogoutConfirmationDialog() {
        JDialog confirmDialog = new JDialog(this, "Подтверждение выхода", true);
        confirmDialog.setSize(400, 250);
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.setResizable(false);
        confirmDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(255, 255, 255));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Иконка вопроса
        JLabel iconLabel = new JLabel("?", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Arial", Font.BOLD, 48));
        iconLabel.setForeground(new Color(241, 196, 15));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Заголовок
        JLabel titleLabel = new JLabel("Подтверждение выхода", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Сообщение
        JLabel messageLabel = new JLabel("<html><center>Вы уверены, что хотите выйти из системы?</center></html>", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(127, 140, 141));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(new Color(255, 255, 255));

        // Кнопка Да
        JButton yesButton = new JButton("Да, выйти");
        yesButton.setFont(new Font("Arial", Font.BOLD, 12));
        yesButton.setForeground(Color.WHITE);
        yesButton.setBackground(new Color(220, 53, 69));
        yesButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        yesButton.setFocusPainted(false);
        yesButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Кнопка Нет
        JButton noButton = new JButton("Нет, остаться");
        noButton.setFont(new Font("Arial", Font.BOLD, 12));
        noButton.setForeground(Color.WHITE);
        noButton.setBackground(new Color(52, 152, 219));
        noButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        noButton.setFocusPainted(false);
        noButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

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
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.setVisible(true);
    }

    private void performLogout() {
        LoginFrame.clearToken();
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    private void createNewTask() {
        // Создаем диалог как модальный
        AssignTaskDialog dialog = new AssignTaskDialog(this, authToken);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
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
        JButton manageUsersButton = new JButton("Управление пользователями");
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

    private JPanel createAdminStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(new Color(248, 249, 250));

        JLabel statsTitle = new JLabel("Общая статистика системы", SwingConstants.CENTER);
        statsTitle.setFont(new Font("Arial", Font.BOLD, 24));
        statsTitle.setForeground(new Color(33, 37, 41));
        statsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel cardsContainer = new JPanel();
        cardsContainer.setLayout(new GridLayout(1, 4, 20, 0));
        cardsContainer.setBackground(new Color(248, 249, 250));
        cardsContainer.setMaximumSize(new Dimension(1000, 190));
        cardsContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Заглушки для статистики - в реальной системе здесь будут реальные данные
        cardsContainer.add(createStatCard("Всего задач", "0", new Color(52, 152, 219), "📋"));
        cardsContainer.add(createStatCard("Активных", "0", new Color(241, 196, 15), "⏳"));
        cardsContainer.add(createStatCard("Завершено", "0", new Color(46, 204, 113), "✅"));
        cardsContainer.add(createStatCard("Пользователей", "0", new Color(155, 89, 182), "👥"));

        statsPanel.add(statsTitle);
        statsPanel.add(Box.createVerticalStrut(20));
        statsPanel.add(cardsContainer);

        return statsPanel;
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

        return card;
    }

    private void showAllTasks() {
        centerPanel.removeAll();
        JPanel allTasksPanel = loadAllTasksPanel();
        centerPanel.add(allTasksPanel, "alltasks");
        cardLayout.show(centerPanel, "alltasks");
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    private JPanel loadAllTasksPanel() {
        JPanel allTasksPanel = new JPanel(new BorderLayout());
        allTasksPanel.setBackground(Color.WHITE);
        allTasksPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JLabel titleLabel = new JLabel("Все задачи системы", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Создаем панель фильтров
        JPanel filtersPanel = createAdminFiltersPanel();
        allTasksPanel.add(filtersPanel, BorderLayout.NORTH);

        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(Color.WHITE);
        JLabel loadingLabel = new JLabel("Загрузка всех задач системы...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        loadingLabel.setForeground(Color.GRAY);
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);

        allTasksPanel.add(titleLabel, BorderLayout.NORTH);
        allTasksPanel.add(loadingPanel, BorderLayout.CENTER);
        loadAllTasksFromServer(allTasksPanel, loadingPanel);

        return allTasksPanel;
    }

    private JPanel createAdminFiltersPanel() {
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
        adminStatusFilter = new JComboBox<>(statusOptions);
        styleAdminComboBox(adminStatusFilter);
        adminStatusFilter.setPreferredSize(new Dimension(140, 30));

        // Фильтр по важности
        JLabel importanceLabel = new JLabel("Важность:");
        importanceLabel.setFont(new Font("Arial", Font.BOLD, 12));
        importanceLabel.setForeground(new Color(44, 62, 80));

        String[] importanceOptions = {"Все приоритеты", "СРОЧНАЯ", "НАДО_ПОТОРОПИТЬСЯ", "МОЖЕТ_ПОДОЖДАТЬ"};
        adminImportanceFilter = new JComboBox<>(importanceOptions);
        styleAdminComboBox(adminImportanceFilter);
        adminImportanceFilter.setPreferredSize(new Dimension(140, 30));

        // Сортировка по дедлайну
        JLabel sortLabel = new JLabel("Сортировка:");
        sortLabel.setFont(new Font("Arial", Font.BOLD, 12));
        sortLabel.setForeground(new Color(44, 62, 80));

        String[] sortOptions = {"Без сортировки", "Дедлайн ↑", "Дедлайн ↓"};
        adminSortFilter = new JComboBox<>(sortOptions);
        styleAdminComboBox(adminSortFilter);
        adminSortFilter.setPreferredSize(new Dimension(150, 30));

        // Кнопки
        adminApplyFiltersButton = new JButton("Применить");
        adminResetFiltersButton = new JButton("Сбросить");

        styleAdminFilterButton(adminApplyFiltersButton, new Color(52, 152, 219));
        styleAdminFilterButton(adminResetFiltersButton, new Color(108, 117, 125));

        adminApplyFiltersButton.setPreferredSize(new Dimension(100, 30));
        adminResetFiltersButton.setPreferredSize(new Dimension(90, 30));

        adminApplyFiltersButton.addActionListener(e -> applyAdminFilters());
        adminResetFiltersButton.addActionListener(e -> resetAdminFilters());

        // Добавляем компоненты на панель
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

    private JPanel createAdminTableHeader() {
        JPanel headerPanel = new JPanel(new GridLayout(1, 7, 10, 5));
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        String[] headers = {"Пользователь", "Название задачи", "Статус", "Приоритет", "Дедлайн", "Комментарии", "Действия"};
        for (String header : headers) {
            JLabel headerLabel = new JLabel(header);
            headerLabel.setFont(new Font("Arial", Font.BOLD, 12));
            headerLabel.setForeground(new Color(44, 62, 80));
            headerPanel.add(headerLabel);
        }

        return headerPanel;
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

    private Task parseSingleTask(String taskJson) {
        try {
            System.out.println("DEBUG: Admin - Parsing task: " + taskJson);

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
                        refreshAdminTasksDisplay();
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

    private boolean sendReworkRequestToServer(Task task, String commentText) {
        try {
            String url = "http://localhost:8080/markthetaskasonrework?title=" +
                    java.net.URLEncoder.encode(task.getTitle(), "UTF-8");

            System.out.println("DEBUG: Sending rework request to: " + url);
            System.out.println("DEBUG: Comment: " + commentText);

            // Создаем объект комментария
            Comment comment = new Comment();
            comment.setDescription(commentText);
            // task не устанавливаем, так как сервер сам свяжет комментарий с задачей

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

    private String escapeJsonString(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
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
        styleReworkDialogButton(okButton, new Color(46, 204, 113));
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
        styleReworkDialogButton(okButton, new Color(231, 76, 60));
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

    private void addAdminTaskRow(JPanel parent, Task task, String username) {
        JPanel taskRow = new JPanel(new GridLayout(1, 7, 10, 5));
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
        if (deadline.contains("T")) deadline = deadline.substring(0, deadline.indexOf("T"));
        JLabel deadlineLabel = new JLabel(deadline);
        deadlineLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        deadlineLabel.setForeground(new Color(44, 62, 80));
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
                    showTaskComments(task); // Используем новый дизайн
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

    private void showDashboard() {
        centerPanel.removeAll();
        JPanel welcomePanel = createWelcomePanel();
        centerPanel.add(welcomePanel, "welcome");
        cardLayout.show(centerPanel, "welcome");
        centerPanel.revalidate();
        centerPanel.repaint();

        // Загружаем статистику после показа дашборда
        SwingUtilities.invokeLater(() -> {
            loadAdminStatistics();
        });
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

    private Task parseSingleTaskImproved(String taskJson) {
        try {
            System.out.println("DEBUG: Admin - Parsing task: " + taskJson);

            // Используем улучшенный метод extractValue
            String title = extractValueImproved(taskJson, "title");
            String description = extractValueImproved(taskJson, "description");
            String status = extractValueImproved(taskJson, "status");
            String importance = extractValueImproved(taskJson, "importance");
            String deadline = extractValueImproved(taskJson, "deadline");

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

    private void loadAllTasksFromServer(JPanel allTasksPanel, JPanel loadingPanel) {
        new Thread(() -> {
            try {
                List<User> users = getAllUsersWithTasks(); // Это использует оригинальный парсинг
                SwingUtilities.invokeLater(() -> {
                    loadingPanel.removeAll();
                    allTasksPanel.removeAll();
                    allTasksPanel.setLayout(new BorderLayout());

                    JLabel titleLabel = new JLabel("Все задачи системы", SwingConstants.CENTER);
                    titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
                    titleLabel.setForeground(new Color(44, 62, 80));
                    titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    allTasksPanel.add(titleLabel, BorderLayout.NORTH);

                    // Создаем панель фильтров
                    JPanel filtersPanel = createAdminFiltersPanel();
                    allTasksPanel.add(filtersPanel, BorderLayout.NORTH);

                    if (users != null && !users.isEmpty()) {
                        // Сохраняем оригинальный список всех задач
                        originalAllAdminTasks = new ArrayList<>();
                        for (User user : users) {
                            if (user.getTasks() != null) {
                                originalAllAdminTasks.addAll(user.getTasks());
                            }
                        }

                        allAdminTasks = new ArrayList<>(originalAllAdminTasks);
                        System.out.println("DEBUG: Loaded " + originalAllAdminTasks.size() + " tasks for admin");
                        displayAllAdminTasks(allTasksPanel, users);
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
        contentPanel.setBackground(Color.WHITE);

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
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        allTasksPanel.add(scrollPane, BorderLayout.CENTER);
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


}
