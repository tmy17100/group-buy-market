package cn.bugstack.domain.activity.service.trial.node;

import cn.bugstack.domain.activity.model.entity.MarketProductEntity;
import cn.bugstack.domain.activity.model.entity.TrialBalanceEntity;
import cn.bugstack.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.bugstack.domain.activity.model.valobj.SkuVO;
import cn.bugstack.domain.activity.service.discount.IDiscountCalculateService;
import cn.bugstack.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import cn.bugstack.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import cn.bugstack.domain.activity.service.trial.thread.QueryGroupBuyActivityDiscountVOThreadTask;
import cn.bugstack.domain.activity.service.trial.thread.QuerySkuVOFromDBThreadTask;
import cn.bugstack.types.design.framework.tree.StrategyHandler;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 营销优惠节点
 * @create 2024-12-14 14:30
 */
@Slf4j
@Service
public class MarketNode extends AbstractGroupBuyMarketSupport<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {

@Resource
private ThreadPoolExecutor threadPoolExecutor;
@Resource
private EndNode endNode;
@Resource
private Map<String, IDiscountCalculateService> discountCalculateServiceMap;
    @Override
    protected void mutiThread(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
     QueryGroupBuyActivityDiscountVOThreadTask queryGroupBuyActivityDiscountVOThreadTask=  new QueryGroupBuyActivityDiscountVOThreadTask(requestParameter.getSource(), requestParameter.getChannel(),repository);
        FutureTask < GroupBuyActivityDiscountVO>groupBuyActivityDiscountVOFutureTask=new FutureTask<>(queryGroupBuyActivityDiscountVOThreadTask);
        threadPoolExecutor.execute(groupBuyActivityDiscountVOFutureTask);
        QuerySkuVOFromDBThreadTask querySkuVOFromDBThreadTask=new QuerySkuVOFromDBThreadTask(requestParameter.getGoodsId(),repository);
FutureTask < SkuVO>skuVOFutureTask=new FutureTask<>(querySkuVOFromDBThreadTask);
        threadPoolExecutor.execute(skuVOFutureTask);
   dynamicContext.setGroupBuyActivityDiscountVO(groupBuyActivityDiscountVOFutureTask.get(timeout, TimeUnit.MINUTES));
   dynamicContext.setSkuVO(skuVOFutureTask.get(timeout, TimeUnit.MINUTES));

    }

    @Override
    public TrialBalanceEntity doApply(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
       log.info("拼团商品查询试算服务-MarketNode userId:{} requestParameter:{}",requestParameter.getUserId());
       GroupBuyActivityDiscountVO groupBuyActivityDiscountVO=dynamicContext.getGroupBuyActivityDiscountVO();
       GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount=groupBuyActivityDiscountVO.getGroupBuyDiscount();
       SkuVO skuVO=dynamicContext.getSkuVO();
       IDiscountCalculateService discountCalculateService=discountCalculateServiceMap.get(groupBuyDiscount.getMarketPlan());
         if(discountCalculateService==null){
             throw new AppException(ResponseCode.E0001.getCode(),ResponseCode.E0001.getInfo());
         }
        BigDecimal deductionPrice=discountCalculateService.calculate(requestParameter.getUserId(), skuVO.getOriginalPrice(),groupBuyDiscount);
          dynamicContext.setDeductionPrice(deductionPrice);

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext)  {
        return endNode;
    }
}
