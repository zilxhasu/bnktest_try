package com.example.bnk.emulator;

import com.example.bnk.shared.corebank.CoreQueues;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/** 宣告 exchange 與「每個地區一條請求 queue」，模擬多地區主機各自獨立。 */
@Configuration
public class RabbitTopologyConfig {

    public static final List<String> AREAS = List.of("TW", "HK", "SG", "VN");

    @Bean
    Declarables coreTopology() {
        TopicExchange exchange = new TopicExchange(CoreQueues.EXCHANGE, true, false);
        List<org.springframework.amqp.core.Declarable> list = new ArrayList<>();
        list.add(exchange);
        for (String area : AREAS) {
            Queue q = new Queue(CoreQueues.REQ_QUEUE_PREFIX + area, true);
            list.add(q);
            list.add(BindingBuilder.bind(q).to(exchange).with(CoreQueues.REQ_ROUTING_PREFIX + area));
        }
        Queue resp = new Queue(CoreQueues.RESP_QUEUE, true);
        list.add(resp);
        list.add(BindingBuilder.bind(resp).to(exchange).with(CoreQueues.RESP_ROUTING_KEY));
        return new Declarables(list);
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
