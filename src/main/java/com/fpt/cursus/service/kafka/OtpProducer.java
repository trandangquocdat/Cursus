//package com.fpt.cursus.service.kafka;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//public class OtpProducer {
//
//    @Autowired
//    private KafkaTemplate<String, String> kafkaTemplate;
//
//    public void sendOtpEmail(String email, String otp) {
//        kafkaTemplate.send("service-topic", email + "," + otp);
//    }
//
//
//}