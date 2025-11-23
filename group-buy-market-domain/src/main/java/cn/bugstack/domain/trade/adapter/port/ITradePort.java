package cn.bugstack.domain.trade.adapter.port;

import cn.bugstack.domain.trade.model.entity.NotifyTaskEntity;

public interface ITradePort {
    public String groupBuyNotify(NotifyTaskEntity notifyTask) throws Exception;
}
