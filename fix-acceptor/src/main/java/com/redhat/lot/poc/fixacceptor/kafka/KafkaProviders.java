package com.redhat.lot.poc.fixacceptor.kafka;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class KafkaProviders {

    @Inject
    Logger log;

    @ConfigProperty(name = "kafka.topic")
    String kafkaTopic;

    @ConfigProperty(name = "kafka.bootstrap.servers")
    String kafkaServer;

    @ConfigProperty(name = "kafka.auto.offset.reset")
    String offSetReset;

    @ConfigProperty(name = "kafka.group.id")
    String groupId;

    @Produces
    KafkaProducer<String, String> getProducer() {
        // https://kafka.apache.org/documentation/#producerconfigs
        Map<String, Object> config = new HashMap<>();
        config.put("group.id", groupId);
        config.put("auto.offset.reset", offSetReset);
        config.put("topic", kafkaTopic);
        config.put("bootstrap.servers", kafkaServer);
        config.put("acks", "1"); // all | -1 | 0 | 1
        config.put("compression.type", "none"); // Valid values are none, gzip, snappy, lz4, or zstd.
        // batch.size
        // linger.ms
        
        log.info("Starting kafka producer");
        log.info("\t group.id: " + groupId);
        log.info("\t topic: " + kafkaTopic);
        log.info("\t bootstrap.servers: " + kafkaServer);
        log.info("\t auto.offset.reset: " + offSetReset);
        
        return new KafkaProducer<>(config,
                new StringSerializer(),
                new StringSerializer());
    }

}
