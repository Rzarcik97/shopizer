package com.salesmanager.core.business.services.catalog.product.variant;

import com.salesmanager.core.business.services.common.generic.SalesManagerEntityService;
import com.salesmanager.core.model.catalog.product.variant.ProductVariantImage;
import com.salesmanager.core.model.merchant.MerchantStore;

import java.util.List;

public interface ProductVariantImageService extends SalesManagerEntityService<Long, ProductVariantImage> {


    List<ProductVariantImage> list(Long productVariantId, MerchantStore store);

    List<ProductVariantImage> listByProduct(Long productId, MerchantStore store);

    List<ProductVariantImage> listByProductVariantGroup(Long productVariantGroupId, MerchantStore store);

}
