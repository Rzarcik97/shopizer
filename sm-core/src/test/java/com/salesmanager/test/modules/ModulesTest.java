package com.salesmanager.test.modules;

import com.salesmanager.core.business.constants.Constants;
import com.salesmanager.core.business.services.system.ModuleConfigurationService;
import com.salesmanager.core.model.system.IntegrationModule;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ModulesTest extends com.salesmanager.test.common.AbstractSalesManagerCoreTestCase {

    @Autowired
    private ModuleConfigurationService moduleConfigurationService = null;


    @Test
    public void testModulesConfigurations() throws Exception {
        List<IntegrationModule> modules = moduleConfigurationService.getIntegrationModules(Constants.PAYMENT_MODULES);

        Assert.assertNotNull(modules);
    }


}
