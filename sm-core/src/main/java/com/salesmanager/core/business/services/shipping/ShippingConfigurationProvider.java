package com.salesmanager.core.business.services.shipping;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.shipping.ShippingConfiguration;

public interface ShippingConfigurationProvider {

    /**
     * ShippingType (NATIONAL, INTERNATIONSL)
     * ShippingBasisType (SHIPPING, BILLING)
     * ShippingPriceOptionType (ALL, LEAST, HIGHEST)
     * Packages
     * Handling
     * @param store
     * @return
     * @throws ServiceException
     */
    ShippingConfiguration getShippingConfiguration(MerchantStore store) throws ServiceException;

}
