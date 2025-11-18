package cn.bugstack.types.enums;

import javafx.scene.input.KeyCodeCombination;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ResponseCode {

    SUCCESS("0000", "成功"),
    UN_ERROR("0001", "未知失败"),
    ILLEGAL_PARAMETER("0002", "非法参数"),
    E0001("E0001","不存在对应的折扣计算服务"),
    E0002("E0002", "无拼团营销配置"),
    E0003("E0003", "拼团活动降级拦截"),
    E0004("E0004", "拼团活动切量拦截"),
    E0005("E0005", "组队失败，更新数量为0"),
    E0006("E0006", "拼团团队完结，锁订单量达标"),
    ;


    public static final String INDEX_EXCEPTION = "拼团记录写入错误";
    private String code;
    private String info;

}
