package com.redhat.lot.poc.fixacceptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.common.serialization.Deserializer;

import quickfix.Message;

public class FixMessageDeserializer implements Deserializer<Message[]> {

    @Override
    public Message[] deserialize(String topic, byte[] data) {
        ObjectMapper mapper = new ObjectMapper();
        Message[] messages = null;
        try {
            messages = mapper.readValue(new String(data, "UTF-8"), Message[].class);
        } catch (Exception e) {

            e.printStackTrace();
        }
        return messages;
    }



}