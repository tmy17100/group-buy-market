package cn.bugstack.domain.trade.service.refund.business;

/**
 * @author TanMengyang
 * @description
 * @create 2025-12-01 12:07
 */

import cn.bugstack.domain.trade.model.entity.TradeRefundCommandEntity;
import cn.bugstack.domain.trade.model.entity.TradeRefundOrderEntity;
import cn.bugstack.domain.trade.model.valobj.TeamRefundSuccess;

/**
 * 退单策略接口
 * 未支付，Unpaid
 * 未成团，UnformedTeam
 * 已成团，AlreadyFormedTeam
 *
 */
public interface IRefundOrderStrategy {

    void refundOrder(TradeRefundOrderEntity tradeRefundOrderEntity);

    void reverseStock(TeamRefundSuccess teamRefundSuccess);
}
