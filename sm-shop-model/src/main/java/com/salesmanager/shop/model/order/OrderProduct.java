package com.salesmanager.shop.model.order;

import com.salesmanager.shop.model.entity.Entity;

import java.io.Serializable;


public class OrderProduct extends Entity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String sku;

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

}
