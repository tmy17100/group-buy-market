package cn.bugstack.domain.trade.service.refund.business.impl;

import cn.bugstack.domain.trade.adapter.port.ITradePort;
import cn.bugstack.domain.trade.adapter.repository.ITradeRepository;
import cn.bugstack.domain.trade.model.aggregate.GroupBuyRefundAggregate;
import cn.bugstack.domain.trade.model.entity.NotifyTaskEntity;
import cn.bugstack.domain.trade.model.entity.TradeRefundOrderEntity;
import cn.bugstack.domain.trade.model.valobj.TeamRefundSuccess;
import cn.bugstack.domain.trade.service.ITradeTaskService;
import cn.bugstack.domain.trade.service.lock.factory.TradeLockRuleFilterFactory;
import cn.bugstack.domain.trade.service.refund.business.IRefundOrderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 发起退单（未成团&已支付），锁单量-1、完成量-1、组队订单状态更新、发送退单消息（MQ）
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/7/8 07:43
 */
@Slf4j
@Service("paid2RefundStrategy")
public class Paid2RefundStrategy implements IRefundOrderStrategy {
    @Resource
    private ITradeRepository repository;
    @Resource
    private ITradePort port;
    @Resource
    private ITradeTaskService tradeTaskService;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;


    @Override
    public void refundOrder(TradeRefundOrderEntity tradeRefundOrderEntity) {
        log.info("退单；已经支付，未成团 userId:{} teamId:{} orderId:{}", tradeRefundOrderEntity.getUserId(), tradeRefundOrderEntity.getTeamId(), tradeRefundOrderEntity.getOrderId());
        NotifyTaskEntity notifyTaskEntity=repository.paid2Refund(GroupBuyRefundAggregate.buildPaid2RefundAggregate(tradeRefundOrderEntity,-1,-1));
        if(null!=notifyTaskEntity){
            threadPoolExecutor.execute(() -> {
                Map<String,Integer>notifyresultMap=null;
                try {
                   notifyresultMap= tradeTaskService.execNotifyJob(notifyTaskEntity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }


    @Override
    public void reverseStock(TeamRefundSuccess teamRefundSuccess)  {
        log.info("退单；恢复锁单量 - 已支付，未成团，但有锁单记录，要恢复锁单库存 {} {} {}", teamRefundSuccess.getUserId(), teamRefundSuccess.getActivityId(), teamRefundSuccess.getTeamId());
        // 1. 恢复库存key
        String recoveryTeamStockKey = TradeLockRuleFilterFactory.generateRecoveryTeamStockKey(teamRefundSuccess.getActivityId(), teamRefundSuccess.getTeamId());
        // 2. 退单恢复「已支付，未成团，有锁单记录，要恢复锁单库存」
        repository.refund2AddRecovery(recoveryTeamStockKey, teamRefundSuccess.getOrderId());
    }

}
