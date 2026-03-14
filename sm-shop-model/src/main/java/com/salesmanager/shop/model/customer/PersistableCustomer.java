package com.salesmanager.shop.model.customer;

import com.salesmanager.shop.model.customer.attribute.PersistableCustomerAttribute;
import com.salesmanager.shop.model.security.PersistableGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;


@ApiModel(value = "Customer", description = "Customer model object")
public class PersistableCustomer extends CustomerEntity {

    private static final long serialVersionUID = 1L;
    /**
     *
     */
    @ApiModelProperty(notes = "Customer password")
    private String password = null;
    private String repeatPassword = null;
    private List<PersistableCustomerAttribute> attributes;
    private List<PersistableGroup> groups;

    public List<PersistableCustomerAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<PersistableCustomerAttribute> attributes) {
        this.attributes = attributes;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<PersistableGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<PersistableGroup> groups) {
        this.groups = groups;
    }

    public String getRepeatPassword() {
        return repeatPassword;
    }

    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }


}
