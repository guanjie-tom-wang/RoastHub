package com.tomwang.roasthub.dao.pojo;

import lombok.Data;

// BreakfastItem是一个简单的Java类，用于封装早餐项目的数据
@Data
public class RecipeItem {
    private String name;
    private String detailUrl;
    private String pictureUrl;
    private String type;
    private String itemId;

}