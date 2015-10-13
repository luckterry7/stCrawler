package bean;

import java.math.BigDecimal;
import java.util.Map;

import com.alibaba.fastjson.JSON;

public class Stock {
	private String name;
	private String symbol;
	private BigDecimal price;
	private BigDecimal prev_weight;
	private BigDecimal target_weight;
	private String updateTime;
	
	public Map<String,String> getMapFromJSONString(String jsonString){
		return JSON.parseObject(jsonString,Map.class);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public BigDecimal getPrev_weight() {
		return prev_weight;
	}

	public void setPrev_weight(BigDecimal prev_weight) {
		this.prev_weight = prev_weight;
	}

	public BigDecimal getTarget_weight() {
		return target_weight;
	}

	public void setTarget_weight(BigDecimal target_weight) {
		this.target_weight = target_weight;
	}


	@Override
	public String toString() {
		return "股票 [名称=" + name + ", 代号=" + symbol + ", 买入价格="
				+ price + ", 原持仓=" + prev_weight + ", 目前持仓="
				+ target_weight + ", 更新日期=" + updateTime + "]";
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
		
}
