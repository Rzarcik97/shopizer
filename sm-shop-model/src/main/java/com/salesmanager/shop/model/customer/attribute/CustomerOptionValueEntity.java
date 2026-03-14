package com.salesmanager.shop.model.customer.attribute;

import java.io.Serializable;

public class CustomerOptionValueEntity extends CustomerOptionValue implements
        Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private int order;
    private String code;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
