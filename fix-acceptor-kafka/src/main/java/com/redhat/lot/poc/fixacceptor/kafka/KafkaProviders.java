package com.redhat.lot.poc.fixacceptor.kafka;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.redhat.lot.poc.fixacceptor.CircularList;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.common.annotation.Identifier;
import org.jboss.logging.Logger;

@ApplicationScoped
public class KafkaProviders {

    public static final Logger log = Logger.getLogger(KafkaProviders.class);

    @Inject
    @Identifier("default-kafka-broker")
    Map<String, Object> config;

    @ConfigProperty(name = "kafka.topic")
    String kafkaTopic;

    @ConfigProperty(name = "kafka.bootstrap.servers")
    String kafkaServer;

    @ConfigProperty(name = "kafka.auto.offset.reset")
    String offSetReset;

    KafkaConsumer<String, String> consumer;

    volatile boolean done = false;
    volatile String last;

    void onStart(@Observes StartupEvent ev) {               
        startConsumer();
    }

    @Produces
    AdminClient getAdmin() {
        Map<String, Object> copy = new HashMap<>();
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            if (AdminClientConfig.configNames().contains(entry.getKey())) {
                copy.put(entry.getKey(), entry.getValue());
            }
        }
        return KafkaAdminClient.create(copy);
    }

    void startConsumer() {
        Map<String, Object> config = new HashMap<>();

        config.put("group.id", "kafka-consumer");
        config.put("topic", kafkaTopic);
        config.put("bootstrap.servers", kafkaServer);
        config.put("auto.offset.reset", offSetReset);

        log.info("Starting kafka consumer");
        log.info("\t group.id: " + "kafka-producer");
        log.info("\t topic: " + kafkaTopic);
        log.info("\t bootstrap.servers: " + kafkaServer);
        log.info("\t auto.offset.reset: " + offSetReset);
        
        consumer = new KafkaConsumer<>(config,
                new StringDeserializer(),
                new StringDeserializer());

        consumer.subscribe(Collections.singleton(kafkaTopic));
        new Thread(() -> {
            while (true) {
                final ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(1));

                consumerRecords.forEach(record -> {
                    // System.out.printf("Received from Kafka, adding to circular list:(%s, %s, %d, %d)\n",
                    //         record.key(), record.value(),
                    //         record.partition(), record.offset());
                    // last = record.key() + "-" + record.value();
                    CircularList.getInstance().insert(record.value());
                });
            }
        }).start();
    }

    void onShutdown(@Observes ShutdownEvent env) {
        if (consumer != null)           
            consumer.close();
        this.done = false;
    }

}
