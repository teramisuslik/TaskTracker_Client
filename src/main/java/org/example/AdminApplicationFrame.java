package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class AdminApplicationFrame extends JFrame {
    private String authToken;
    private Map<String, Object> userInfo;

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
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 245, 245));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Приветствие для администратора
        JLabel welcomeLabel = new JLabel("Панель администратора", SwingConstants.LEFT);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        welcomeLabel.setForeground(new Color(44, 62, 80));

        // Информация о пользователе
        JLabel userInfoLabel = new JLabel("Вы вошли как: " + username + " (ADMIN)", SwingConstants.LEFT);
        userInfoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        userInfoLabel.setForeground(new Color(127, 140, 141));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.add(welcomeLabel);
        infoPanel.add(userInfoLabel);

        // Панель кнопок администратора
        JPanel adminPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        adminPanel.setOpaque(false);

        // Кнопки для администратора
        JButton manageUsersButton = new JButton("Управление пользователями");
        JButton viewTasksButton = new JButton("Просмотр всех задач");
        JButton createTaskButton = new JButton("Создать задачу");

        styleAdminButton(manageUsersButton);
        styleAdminButton(viewTasksButton);
        styleAdminButton(createTaskButton);

        // Добавляем обработчики для кнопок администратора
        manageUsersButton.addActionListener(e -> showUserManagement());
        viewTasksButton.addActionListener(e -> showAllTasks());
        createTaskButton.addActionListener(e -> createNewTask());

        adminPanel.add(manageUsersButton);
        adminPanel.add(viewTasksButton);
        adminPanel.add(createTaskButton);

        // Кнопка выхода
        JButton logoutButton = createRedLogoutButton();

        // Компоновка верхней панели
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(infoPanel, BorderLayout.NORTH);
        leftPanel.add(adminPanel, BorderLayout.SOUTH);

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(logoutButton, BorderLayout.EAST);

        // Центральная панель с таблицей или другой информацией
        JTextArea adminConsole = new JTextArea();
        adminConsole.setEditable(false);
        adminConsole.setFont(new Font("Consolas", Font.PLAIN, 12));
        adminConsole.setText("Добро пожаловать в панель администратора!\n\n" +
                "Доступные функции:\n" +
                "• Управление пользователями\n" +
                "• Просмотр всех задач\n" +
                "• Создание новых задач\n" +
                "• Отметка задач как выполненных\n" +
                "• Удаление пользователей");

        JScrollPane scrollPane = new JScrollPane(adminConsole);

        // Добавляем компоненты
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        add(contentPane);

        // Добавляем слушатель закрытия окна
        addWindowListener(new java.awt.event.WindowAdapter() {
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

    private void showAllTasks() {
        showInfoDialog("Просмотр всех задач", "Функция просмотра всех задач находится в разработке.");
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
}
