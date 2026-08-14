package com.example.notification_service.config;

import com.example.notification_service.dto.kafka.QuizSubmittedNotificationMessage;
import com.example.notification_service.dto.kafka.QuizTakenNotificationMessage;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
//import java.util.Queue;

@Configuration
public class RabbitMQConfig {


    public static final String NOTIFICATION_EXCHANGE = "quiz.notification.exchange";
    public static final String QUIZ_TAKEN_QUEUE = "quiz.taken.queue";
    public static final String QUIZ_TAKEN_ROUTING_KEY = "quiz.taken";
    public static final String QUIZ_SUBMITTED_QUEUE = "quiz.submitted.queue";
    public static final String QUIZ_SUBMITTED_ROUTING_KEY = "quiz.submitted";

    @Bean
    public TopicExchange quizNotificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public Queue quizTakenQueue() {
        return QueueBuilder.durable(QUIZ_TAKEN_QUEUE).build();
    }

    @Bean
    public Queue quizSubmittedQueue() {
        return QueueBuilder.durable(QUIZ_SUBMITTED_QUEUE).build();
    }

    @Bean
    public Binding quizTakenBinding(Queue quizTakenQueue, TopicExchange quizNotificationExchange) {
        return BindingBuilder.bind(quizTakenQueue).to(quizNotificationExchange).with(QUIZ_TAKEN_ROUTING_KEY);
    }

    @Bean
    public Binding quizSubmittedBinding(Queue quizSubmittedQueue, TopicExchange quizNotificationExchange) {
        return BindingBuilder.bind(quizSubmittedQueue).to(quizNotificationExchange).with(QUIZ_SUBMITTED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("quizTakenNotification", QuizTakenNotificationMessage.class);
        idClassMapping.put("quizSubmittedNotification", QuizSubmittedNotificationMessage.class);
        classMapper.setIdClassMapping(idClassMapping);
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

}
