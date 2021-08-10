package com.redhat.lot.poc.fixacceptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.common.serialization.Serializer;

import quickfix.Message;

public class FixMessageSerializer implements Serializer<Message> {

    @Override
    public byte[] serialize(String topic, Message data) {
        if (data == null) {
            return null;
        }

        byte[] retVal = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            retVal = objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }


}