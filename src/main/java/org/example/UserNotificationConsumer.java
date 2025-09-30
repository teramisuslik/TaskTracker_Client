package org.example;

import java.util.Properties;

// Добавьте этот класс внутрь UserApplicationFrame или как отдельный файл
class UserNotificationConsumer {
    private static final String KAFKA_BROKER = "localhost:9092";
    private static final String TOPIC_USER_PREFIX = "notifications_for_user";
    private volatile boolean running = true;
    private NotificationManager notificationManager;
    private String username;

    public UserNotificationConsumer(NotificationManager notificationManager, String username) {
        this.notificationManager = notificationManager;
        this.username = username;
    }

    public void startConsuming() {
        new Thread(() -> {
            Properties props = new Properties();
            props.put("bootstrap.servers", KAFKA_BROKER);
            props.put("group.id", "user-notification-group-" + username);
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("auto.offset.reset", "latest");

            try (org.apache.kafka.clients.consumer.KafkaConsumer<String, String> consumer =
                         new org.apache.kafka.clients.consumer.KafkaConsumer<>(props)) {

                // Подписываемся на топик для конкретного пользователя
                String userTopic = TOPIC_USER_PREFIX + username;
                consumer.subscribe(java.util.Collections.singletonList(userTopic));
                System.out.println("User notification consumer started for topic: " + userTopic);

                while (running) {
                    try {
                        var records = consumer.poll(java.time.Duration.ofMillis(1000));

                        for (var record : records) {
                            String message = record.value();
                            System.out.println("Received user notification for " + username + ": " + message);

                            // Показываем уведомление
                            notificationManager.showNotification("Уведомление", message);
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing Kafka message: " + e.getMessage());
                        // Продолжаем работу после ошибки
                    }
                }
            } catch (Exception e) {
                System.err.println("Kafka consumer error: " + e.getMessage());
            }
        }).start();
    }

    public void stop() {
        running = false;
    }
}
