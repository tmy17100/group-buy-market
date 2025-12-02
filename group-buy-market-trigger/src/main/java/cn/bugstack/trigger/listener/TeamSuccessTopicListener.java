package cn.bugstack.trigger.listener;

import cn.bugstack.domain.trade.model.valobj.TeamRefundSuccess;
import cn.bugstack.domain.trade.service.ITradeRefundOrderService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 结算完成消息监听
 * @create 2025-03-08 13:49
 */
@Slf4j
@Component
public class TeamSuccessTopicListener {
@Resource
private ITradeRefundOrderService tradeRefundOrderService;
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${spring.rabbitmq.config.producer.topic_team_success.queue}"),
                    exchange = @Exchange(value = "${spring.rabbitmq.config.producer.exchange}", type = ExchangeTypes.TOPIC),
                    key = "${spring.rabbitmq.config.producer.topic_team_success.routing_key}"
            )
    )
    public void listener(String message) {
        log.info("接收消息:{}", message);
        TeamRefundSuccess teamRefundSuccess= JSON.parseObject(message, TeamRefundSuccess.class);
        try {
            tradeRefundOrderService.restoreTeamLockStock(teamRefundSuccess);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
