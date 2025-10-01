package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

public class KafkaTaskProducer {
    private final Producer<String, String> producer;
    private static final String TASK_ASSIGNMENTS_TOPIC = "task-assignments";
    private static final String TASK_DELETIONS_TOPIC = "task-deletions";

    public KafkaTaskProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Для надежности
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 10000); // Таймаут 10 секунд

        this.producer = new KafkaProducer<>(props);
    }

    // Метод для отправки задач
    public void sendTask(String username, String taskJson) throws Exception {
        ProducerRecord<String, String> record = new ProducerRecord<>(TASK_ASSIGNMENTS_TOPIC, username, taskJson);

        // Синхронная отправка с обработкой результата
        Future<RecordMetadata> future = producer.send(record);

        try {
            RecordMetadata metadata = future.get(); // Ждем подтверждения
            System.out.println("Задача отправлена в Kafka. Partition: " + metadata.partition() +
                    ", Offset: " + metadata.offset());
        } catch (Exception e) {
            throw new Exception("Ошибка отправки в Kafka: " + e.getMessage(), e);
        }
    }

    // Метод для отправки удаления задач по ID
    public void sendTaskDeletion(String username, Long taskId) throws Exception {
        // Создаем JSON для удаления с ID
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> deletionData = new HashMap<>();
        deletionData.put("username", username);
        deletionData.put("id", taskId); // Используем "id" как в TaskDeleteDto

        String deletionJson = objectMapper.writeValueAsString(deletionData);

        ProducerRecord<String, String> record = new ProducerRecord<>(TASK_DELETIONS_TOPIC, username, deletionJson);

        // Синхронная отправка с обработкой результата
        Future<RecordMetadata> future = producer.send(record);

        try {
            RecordMetadata metadata = future.get(); // Ждем подтверждения
            System.out.println("Запрос на удаление отправлен в Kafka. Partition: " + metadata.partition() +
                    ", Offset: " + metadata.offset() + ", User: " + username + ", Task ID: " + taskId);
        } catch (Exception e) {
            throw new Exception("Ошибка отправки удаления в Kafka: " + e.getMessage(), e);
        }
    }

    public void close() {
        if (producer != null) {
            producer.close();
        }
    }
}