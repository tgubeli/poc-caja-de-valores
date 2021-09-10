package com.redhat.lot.poc.fixacceptor;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkus.scheduler.Scheduled;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;
import io.vertx.kafka.client.consumer.KafkaConsumerRecords;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.UnsupportedMessageType;

@ApplicationScoped
public class ServerApplicationAdapter implements quickfix.Application {

    public static final Logger log = Logger.getLogger(ServerApplicationAdapter.class);

    @ConfigProperty(name = "kafka.topic")
    String kafkaTopic;

    @ConfigProperty(name = "kafka.bootstrap.servers")
    String kafkaServer;

    @ConfigProperty(name = "kafka.auto.offset.reset")
    String offSetReset;

    //thread or vertx
    @ConfigProperty(name = "fixacceptor.execution.mode", defaultValue = "thread")
    String executionMode;

    private static final StringDeserializer deserializer = new StringDeserializer();

    @Inject
    Vertx vertx;

    @Inject
    Metrics metrics;

    @Override
    public void onCreate(SessionID sessionID) {
        log.info("--------- onCreate ---------");
    }

    @Scheduled(every="5s")     
    void showMetrics() {
        metrics.logMetrics();
    }

    @Override
    public void onLogon(SessionID sessionID) {
        log.infof("--------- onLogon ------ %s", executionMode);
        if("thread".equals(executionMode)) {
            KafkaConsumer<String, String> consumer = buildConsumer(sessionID.toString());
            consumer.subscribe(Collections.singleton(kafkaTopic));
            new Thread( new ConsumerRunnable(sessionID, consumer, metrics)).start();
        }else {
            io.vertx.kafka.client.consumer.KafkaConsumer<String, String> consumerVertx = buildConsumerVertx(
                sessionID.toString());

            consumerVertx.subscribe(kafkaTopic).onSuccess(v -> {

                vertx.setPeriodic(10, timerId -> consumerVertx.poll(Duration.ofMillis(100))
                    .onSuccess(new VertxHandler(sessionID, timerId, metrics)).onFailure(cause -> {
                        log.info("Erro", cause);
                        // Stop polling if something went wrong
                        vertx.cancelTimer(timerId);
                    })
                );
            });
        }
    }

    private KafkaConsumer<String, String> buildConsumer(String session) {
        Map<String, Object> config = buildConfig(session);

        return new KafkaConsumer<>(config, deserializer, deserializer);
    }

    private Map<String, Object> buildConfig(String session) {
        Map<String, Object> config = new HashMap<>();

        String groupId = session.substring(session.lastIndexOf(">") + 1);
        config.put("group.id", groupId);
        config.put("topic", kafkaTopic);
        config.put("bootstrap.servers", kafkaServer);
        config.put("auto.offset.reset", offSetReset);

        log.info("Starting kafka consumer");
        log.info("\t group.id: " + groupId);
        log.info("\t topic: " + kafkaTopic);
        log.info("\t bootstrap.servers: " + kafkaServer);
        log.info("\t auto.offset.reset: " + offSetReset);
        return config;
    }

    private io.vertx.kafka.client.consumer.KafkaConsumer<String, String> buildConsumerVertx(String session) {
        Map<String, String> config = new HashMap<String, String>();

        String groupId = session.substring(session.lastIndexOf(">") + 1);
        config.put("group.id", groupId);
        config.put("topic", kafkaTopic);
        config.put("bootstrap.servers", kafkaServer);
        config.put("auto.offset.reset", offSetReset);

        log.info("Starting kafka consumer");
        log.info("\t group.id: " + groupId);
        log.info("\t topic: " + kafkaTopic);
        log.info("\t bootstrap.servers: " + kafkaServer);
        log.info("\t auto.offset.reset: " + offSetReset);

        return io.vertx.kafka.client.consumer.KafkaConsumer.create(vertx, config, deserializer, deserializer);
    }

    @Override
    public void onLogout(SessionID sessionID) {
        log.info("--------- onLogout ---------");
    }

    @Override
    public void toAdmin(quickfix.Message message, SessionID sessionID) {
        log.info("--------- toAdmin ---------");
    }

    @Override
    public void toApp(quickfix.Message message, SessionID sessionID) throws DoNotSend {
        // log.info("--------- toApp ---------");
    }

    @Override
    public void fromAdmin(quickfix.Message message, SessionID sessionID)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        log.info("--------- fromAdmin ---------");
    }

    @Override
    public void fromApp(quickfix.Message message, SessionID sessionID)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        log.info("--------- fromApp ---------");

    }

    static class ConsumerRunnable implements Runnable {

        private SessionID sessionID;
        private Session session;
        private KafkaConsumer<String, String> consumer;
        private Metrics m;

        public ConsumerRunnable(SessionID sessionID, KafkaConsumer<String, String> consumer, Metrics m) {
            this.sessionID = sessionID;
            this.session = Session.lookupSession(sessionID);
            this.consumer = consumer;
            this.m = m;
        }

        @Override
        public void run() {
            while (session.isLoggedOn()) {

                final ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));
                for (ConsumerRecord<String, String> record : consumerRecords) {

                    // log.info("Polled Record:");
                    // log.info("\t record.key: " + record.key() + " record.value: " + record.value());
                    // log.info("\t record.partition: " + record.partition() + " record.offset: " + record.offset());

                    try {
                        Message fixMessage = new Message();
                        fixMessage.fromString(record.value(), null, false);
                        Session.sendToTarget(fixMessage, sessionID);

                        // add this message metrics
                        m.addMetric(sessionID.toString(), fixMessage.getUtcTimeStamp(60), java.time.LocalDateTime.now());
                    } catch (InvalidMessage e) {
                        log.info("Erro ao enviar", e);
                    } catch (SessionNotFound e) {
                        log.debug("Erro", e);
                        break;
                    } catch (FieldNotFound e) {
                        e.printStackTrace();
                    }

                }

            }
            log.info("=======Matando a Thread: " + sessionID);
            consumer.close();
        }

    }

    private class VertxHandler implements Handler<KafkaConsumerRecords<String, String>> {

        private final Session session;
        private final long timerId;
        private final SessionID sessionID;
        private final Metrics m;

        public VertxHandler(SessionID sessionID, long timerId, Metrics m) {
            this.sessionID = sessionID;
            this.session = Session.lookupSession(sessionID);
            this.timerId = timerId;
            this.m = m;
        }

        @Override
        public void handle(KafkaConsumerRecords<String, String> records) {
            for (int i = 0; i < records.size(); i++) {
                KafkaConsumerRecord<String, String> record = records.recordAt(i);
                
                // log.info("Polled Record:");
                // log.info("\t record.key: " + record.key() + " record.value: " + record.value());
                // log.info("\t record.partition: " + record.partition() + " record.offset: " + record.offset());

                try {
                    Message fixMessage = new Message();
                    fixMessage.fromString(record.value(), null, false);
                    Session.sendToTarget(fixMessage, sessionID);

                    // add this message metrics
                    m.addMetric(sessionID.toString(), fixMessage.getUtcTimeStamp(60), java.time.LocalDateTime.now());
                } catch (InvalidMessage e) {
                    log.debug("Erro", e);
                } catch (SessionNotFound e) {
                    log.debug("Erro", e);
                    break;
                } catch (FieldNotFound e) {
                    e.printStackTrace();
                }
            }
            if (!session.isLoggedOn()) {
                vertx.cancelTimer(timerId);
            }
        }

    }

}
