package org.example;

import java.util.Properties;

// Добавьте этот класс внутрь AdminApplicationFrame
class AdminNotificationConsumer {
    private static final String KAFKA_BROKER = "localhost:9092";
    private static final String TOPIC_ADMIN = "notifications_for_admin";
    private volatile boolean running = true;
    private NotificationManager notificationManager;

    public AdminNotificationConsumer(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public void startConsuming() {
        new Thread(() -> {
            Properties props = new Properties();
            props.put("bootstrap.servers", KAFKA_BROKER);
            props.put("group.id", "admin-notification-group");
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("auto.offset.reset", "latest");

            try (org.apache.kafka.clients.consumer.KafkaConsumer<String, String> consumer =
                         new org.apache.kafka.clients.consumer.KafkaConsumer<>(props)) {

                consumer.subscribe(java.util.Collections.singletonList(TOPIC_ADMIN));
                System.out.println("Admin notification consumer started...");

                while (running) {
                    try {
                        var records = consumer.poll(java.time.Duration.ofMillis(1000));

                        for (var record : records) {
                            String message = record.value();
                            System.out.println("Received admin notification: " + message);

                            // Показываем уведомление
                            notificationManager.showNotification("Новое уведомление", message);
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