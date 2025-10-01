package org.example;

import javax.swing.*;
import java.awt.*;

// Добавьте этот класс внутрь AdminApplicationFrame или как отдельный файл
class NotificationManager {
    private static final int NOTIFICATION_DURATION = 5000; // 5 секунд
    private JFrame parentFrame;
    private java.util.List<JDialog> activeNotifications = new java.util.ArrayList<>();

    public NotificationManager(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    public void showNotification(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            // Создаем диалог для уведомления
            JDialog notificationDialog = new JDialog(parentFrame, "", JDialog.ModalityType.MODELESS);
            notificationDialog.setUndecorated(true);
            notificationDialog.setAlwaysOnTop(true);
            notificationDialog.setFocusableWindowState(false);

            // Позиция в правом нижнем углу
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = screenSize.width - 350 - 20; // 20px отступ от края
            int y = screenSize.height - 150 - 20;

            // Учитываем существующие уведомления
            y -= activeNotifications.size() * 160;

            notificationDialog.setLocation(x, y);
            notificationDialog.setSize(350, 140);

            // Содержимое уведомления
            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.setBackground(new Color(255, 255, 255));
            contentPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));

            // Заголовок
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
            titleLabel.setForeground(new Color(44, 62, 80));

            // Сообщение
            JLabel messageLabel = new JLabel("<html>" + message + "</html>");
            messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            messageLabel.setForeground(new Color(127, 140, 141));

            // Иконка (можно использовать эмодзи или иконки)
            JLabel iconLabel = new JLabel("🔔");
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
            iconLabel.setForeground(new Color(52, 152, 219));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel textPanel = new JPanel(new BorderLayout());
            textPanel.setBackground(Color.WHITE);
            textPanel.add(titleLabel, BorderLayout.NORTH);
            textPanel.add(messageLabel, BorderLayout.CENTER);

            contentPanel.add(iconLabel, BorderLayout.WEST);
            contentPanel.add(textPanel, BorderLayout.CENTER);

            notificationDialog.add(contentPanel);

            // Добавляем в список активных уведомлений
            activeNotifications.add(notificationDialog);

            // Показываем уведомление
            notificationDialog.setVisible(true);

            // Автоматическое закрытие через 5 секунд
            new Thread(() -> {
                try {
                    Thread.sleep(NOTIFICATION_DURATION);
                    SwingUtilities.invokeLater(() -> {
                        notificationDialog.dispose();
                        activeNotifications.remove(notificationDialog);
                        repositionNotifications();
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

            // Закрытие по клику
            notificationDialog.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    notificationDialog.dispose();
                    activeNotifications.remove(notificationDialog);
                    repositionNotifications();
                }
            });
        });
    }

    private void repositionNotifications() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int baseY = screenSize.height - 150 - 20;

        for (int i = 0; i < activeNotifications.size(); i++) {
            JDialog notification = activeNotifications.get(i);
            int y = baseY - (i * 160);
            notification.setLocation(screenSize.width - 350 - 20, y);
        }
    }
}
