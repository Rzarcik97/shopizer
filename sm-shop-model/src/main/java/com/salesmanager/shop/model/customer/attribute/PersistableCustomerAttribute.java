package com.salesmanager.shop.model.customer.attribute;

public class PersistableCustomerAttribute extends CustomerAttributeEntity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private CustomerOption customerOption;
    private CustomerOptionValue customerOptionValue;

    public CustomerOptionValue getCustomerOptionValue() {
        return customerOptionValue;
    }

    public void setCustomerOptionValue(CustomerOptionValue customerOptionValue) {
        this.customerOptionValue = customerOptionValue;
    }

    public CustomerOption getCustomerOption() {
        return customerOption;
    }

    public void setCustomerOption(CustomerOption customerOption) {
        this.customerOption = customerOption;
    }


}
