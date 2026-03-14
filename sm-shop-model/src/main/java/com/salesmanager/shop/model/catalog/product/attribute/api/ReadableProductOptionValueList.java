package com.salesmanager.shop.model.catalog.product.attribute.api;

import com.salesmanager.shop.model.entity.ReadableList;

import java.util.ArrayList;
import java.util.List;

public class ReadableProductOptionValueList extends ReadableList {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    List<ReadableProductOptionValue> optionValues = new ArrayList<ReadableProductOptionValue>();

    public List<ReadableProductOptionValue> getOptionValues() {
        return optionValues;
    }

    public void setOptionValues(List<ReadableProductOptionValue> optionValues) {
        this.optionValues = optionValues;
    }

}
