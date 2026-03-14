package com.salesmanager.shop.model.customer.attribute;

import java.io.Serializable;

public class CustomerAttributeEntity extends CustomerAttribute implements
        Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String textValue;

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }


}
