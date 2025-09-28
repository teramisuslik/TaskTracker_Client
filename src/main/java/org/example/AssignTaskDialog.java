package org.example;

import javax.swing.*;

import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.awt.event.*;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class AssignTaskDialog extends JDialog {
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<String> userComboBox;
    private JComboBox<Importance> importanceComboBox;
    private JSpinner deadlineSpinner;
    private JButton assignButton;
    private JButton cancelButton;
    private String authToken;

    // Enum для важности
    enum Importance {
        СРОЧНАЯ,
        НАДО_ПОТОРОПИТЬСЯ,
        МОЖЕТ_ПОДОЖДАТЬ
    }

    public AssignTaskDialog(JFrame parent, String token) {
        super(parent, "Назначение новой задачи", true);
        this.authToken = token;

        setSize(550, 500);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        initializeComponents();
        setupLayout();
        setupActions();

        loadUsers();
    }

    private void initializeComponents() {
        // Поле для названия задачи
        titleField = new JTextField(20);
        titleField.setFont(new Font("Arial", Font.PLAIN, 14));

        // Область для описания
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 14));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        // Выбор пользователя
        userComboBox = new JComboBox<>();
        userComboBox.setFont(new Font("Arial", Font.PLAIN, 14));

        // Выбор важности задачи
        importanceComboBox = new JComboBox<>(Importance.values());
        importanceComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        importanceComboBox.setSelectedItem(Importance.МОЖЕТ_ПОДОЖДАТЬ);

        // Календарь для выбора даты и времени
        SpinnerDateModel dateModel = new SpinnerDateModel();
        deadlineSpinner = new JSpinner(dateModel);
        deadlineSpinner.setEditor(new JSpinner.DateEditor(deadlineSpinner, "dd.MM.yyyy HH:mm"));
        JSpinner.DateEditor dateEditor = (JSpinner.DateEditor) deadlineSpinner.getEditor();
        dateEditor.getTextField().setFont(new Font("Arial", Font.PLAIN, 14));

        // Устанавливаем дату на завтра по умолчанию
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        dateModel.setValue(calendar.getTime());

        // Кнопки
        assignButton = new JButton("Назначить задачу");
        cancelButton = new JButton("Отмена");

        styleButtons();
    }

    private void styleButtons() {
        assignButton.setFont(new Font("Arial", Font.BOLD, 14));
        assignButton.setForeground(Color.WHITE);
        assignButton.setBackground(new Color(46, 204, 113));
        assignButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        assignButton.setFocusPainted(false);
        assignButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        cancelButton.setFont(new Font("Arial", Font.PLAIN, 14));
        cancelButton.setForeground(new Color(44, 62, 80));
        cancelButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Название задачи
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createLabel("Название задачи*:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        formPanel.add(titleField, gbc);

        // Описание
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createLabel("Описание:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        formPanel.add(new JScrollPane(descriptionArea), gbc);
        gbc.weighty = 0.0;

        // Пользователь
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createLabel("Назначить пользователю*:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        formPanel.add(userComboBox, gbc);

        // Важность
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createLabel("Важность задачи:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        formPanel.add(importanceComboBox, gbc);

        // Дедлайн
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(createLabel("Дедлайн*:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        formPanel.add(deadlineSpinner, gbc);

        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(cancelButton);
        buttonPanel.add(assignButton);

        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(buttonPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(new Color(44, 62, 80));
        return label;
    }

    private void setupActions() {
        assignButton.addActionListener(e -> assignTask());
        cancelButton.addActionListener(e -> dispose());

        titleField.addActionListener(e -> descriptionArea.requestFocus());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private void loadUsers() {
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return fetchUsersFromServer();
            }

            @Override
            protected void done() {
                try {
                    List<String> users = get();
                    userComboBox.removeAllItems();
                    for (String user : users) {
                        userComboBox.addItem(user);
                    }
                    if (!users.isEmpty()) {
                        userComboBox.setSelectedIndex(0);
                    }
                } catch (Exception e) {
                    showErrorDialog("Ошибка загрузки пользователей", "Не удалось загрузить список пользователей. Проверьте соединение с сервером.");
                    userComboBox.addItem("user1");
                    userComboBox.addItem("user2");
                    userComboBox.addItem("user3");
                }
            }
        };
        worker.execute();
    }

    private List<String> fetchUsersFromServer() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/allusersname"))
                .header("Authorization", "Bearer " + authToken)
                .header("Accept", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parseUsersFromJson(response.body());
        } else {
            throw new Exception("HTTP error: " + response.statusCode());
        }
    }

    private List<String> parseUsersFromJson(String json) {
        List<String> users = new ArrayList<>();
        try {
            if (json.startsWith("[") && json.endsWith("]")) {
                String cleanJson = json.substring(1, json.length() - 1);
                String[] userArray = cleanJson.split(",");
                for (String user : userArray) {
                    user = user.trim().replace("\"", "");
                    if (!user.isEmpty()) {
                        users.add(user);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка парсинга пользователей: " + e.getMessage());
        }

        if (users.isEmpty()) {
            users.add("user1");
            users.add("user2");
            users.add("user3");
        }

        return users;
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace("\r", "\\r");
    }

    private String formatDateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return sdf.format(date);
    }

    private void showErrorDialog(String title, String message) {
        JDialog errorDialog = new JDialog(this, title, true);
        errorDialog.setSize(400, 250);
        errorDialog.setLocationRelativeTo(this);
        errorDialog.setResizable(false);
        errorDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(255, 255, 255));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel iconLabel = new JLabel("✗", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Arial", Font.BOLD, 48));
        iconLabel.setForeground(new Color(220, 53, 69));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(220, 53, 69));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = new JLabel("<html><center>" + message + "</center></html>", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(127, 140, 141));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton okButton = new JButton("Понятно");
        okButton.setFont(new Font("Arial", Font.BOLD, 14));
        okButton.setForeground(Color.WHITE);
        okButton.setBackground(new Color(220, 53, 69));
        okButton.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
        okButton.setFocusPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        okButton.addActionListener(e -> errorDialog.dispose());

        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                okButton.setBackground(new Color(200, 35, 51));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                okButton.setBackground(new Color(220, 53, 69));
            }
        });

        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(okButton);

        errorDialog.add(contentPanel, BorderLayout.CENTER);
        errorDialog.getRootPane().setDefaultButton(okButton);
        errorDialog.pack();
        errorDialog.setLocationRelativeTo(this);
        errorDialog.setVisible(true);
    }

    private void showSuccessDialog(String message) {
        JDialog successDialog = new JDialog(this, "Успех", true);
        successDialog.setSize(400, 250);
        successDialog.setLocationRelativeTo(this);
        successDialog.setResizable(false);
        successDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(255, 255, 255));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel iconLabel = new JLabel("✓", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Arial", Font.BOLD, 48));
        iconLabel.setForeground(new Color(46, 204, 113));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Задача отправлена", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(46, 204, 113));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = new JLabel("<html><center>" + message + "</center></html>", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(127, 140, 141));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton okButton = new JButton("Отлично!");
        okButton.setFont(new Font("Arial", Font.BOLD, 14));
        okButton.setForeground(Color.WHITE);
        okButton.setBackground(new Color(46, 204, 113));
        okButton.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
        okButton.setFocusPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        okButton.addActionListener(e -> {
            successDialog.dispose();
            dispose();
        });

        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                okButton.setBackground(new Color(39, 174, 96));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                okButton.setBackground(new Color(46, 204, 113));
            }
        });

        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(okButton);

        successDialog.add(contentPanel, BorderLayout.CENTER);
        successDialog.getRootPane().setDefaultButton(okButton);
        successDialog.pack();
        successDialog.setLocationRelativeTo(this);
        successDialog.setVisible(true);
    }

    private void assignTask() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String assignedUser = (String) userComboBox.getSelectedItem();
        Importance importance = (Importance) importanceComboBox.getSelectedItem();
        Date deadline = (Date) deadlineSpinner.getValue();

        // Валидация
        if (title.isEmpty()) {
            showErrorDialog("Ошибка валидации", "Введите название задачи");
            titleField.requestFocus();
            return;
        }

        if (assignedUser == null || assignedUser.isEmpty()) {
            showErrorDialog("Ошибка валидации", "Выберите пользователя");
            return;
        }

        if (deadline.before(new Date())) {
            showErrorDialog("Ошибка валидации", "Дедлайн не может быть в прошлом");
            return;
        }

        assignButton.setEnabled(false);
        assignButton.setText("Отправка...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                String taskJson = createTaskJson(title, description, assignedUser, importance, deadline);
                sendToKafka(assignedUser, taskJson);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    showSuccessDialog("Задача успешно отправлена в обработку и будет назначена пользователю " + assignedUser);
                } catch (Exception e) {
                    showErrorDialog("Ошибка отправки", "Ошибка отправки задачи: " + e.getMessage());
                    assignButton.setEnabled(true);
                    assignButton.setText("Назначить задачу");
                }
            }
        };
        worker.execute();
    }

    private String createTaskJson(String title, String description, String user,
                                  Importance importance, Date deadline) {
        return String.format(
                "{\"title\": \"%s\", " +
                        "\"description\": \"%s\", " +
                        "\"assignedUser\": \"%s\", " +
                        "\"importance\": \"%s\", " +
                        "\"deadline\": \"%s\", " +
                        "\"status\": \"НЕ_НАЧАТА\"}",
                escapeJson(title),
                escapeJson(description),
                escapeJson(user),
                importance.name(),
                formatDateTime(deadline)
        );
    }

    private void sendToKafka(String username, String taskJson) throws Exception {
        KafkaTaskProducer kafkaProducer = new KafkaTaskProducer();
        try {
            kafkaProducer.sendTask(username, taskJson);
            Thread.sleep(500);
        } finally {
            kafkaProducer.close();
        }
    }
}