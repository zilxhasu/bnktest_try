package com.example.bnk.account.infra.corebank;

import com.example.bnk.shared.corebank.CoreQueues;
import com.example.bnk.shared.corebank.CoreRequest;
import com.example.bnk.shared.corebank.CoreResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 真實 MQ 模式（bnk.corebank.mode=mq）。
 * 請求：exchange bnk.core，routing key core.req.{AREA} → 各地區主機
 * 回覆：主機送到 queue bnk.core.resp → 本服務 @RabbitListener 接收
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "bnk.corebank", name = "mode", havingValue = "mq")
public class MqCoreGateway implements CoreGateway {

    private final RabbitTemplate rabbitTemplate;
    private final CoreResponseHandler handler;

    @Override
    public void send(CoreRequest req) {
        rabbitTemplate.convertAndSend(CoreQueues.EXCHANGE, CoreQueues.REQ_ROUTING_PREFIX + req.getArea(), req);
    }

    @RabbitListener(queues = CoreQueues.RESP_QUEUE)
    public void receive(CoreResponse response) {
        handler.onResponse(response);
    }

    @Configuration
    @ConditionalOnProperty(prefix = "bnk.corebank", name = "mode", havingValue = "mq")
    static class RabbitTopology {

        @Bean
        TopicExchange coreExchange() {
            return new TopicExchange(CoreQueues.EXCHANGE, true, false);
        }

        @Bean
        Queue respQueue() {
            return new Queue(CoreQueues.RESP_QUEUE, true);
        }

        @Bean
        Binding respBinding(Queue respQueue, TopicExchange coreExchange) {
            return BindingBuilder.bind(respQueue).to(coreExchange).with(CoreQueues.RESP_ROUTING_KEY);
        }

        @Bean
        MessageConverter jsonMessageConverter() {
            return new Jackson2JsonMessageConverter();
        }
    }
}
