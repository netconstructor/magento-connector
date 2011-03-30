/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.magento.api;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.Validate;

/**
 * A delayed map that converts a magento object into a map. The logic is the
 * following: Null attributes are converted into null - FIXME error prone -, arrays
 * of magento object attributes are converted into collections of magento maps,
 * magento object attributes are converted into magento maps, and any other attribute
 * is left unchanged.
 */
public class MagentoMap extends BeanMap
{
    private static final Package MAGENTO_PACKAGE = Package.getPackage("org.mule.module.magento.api.internal");
    private static final Transformer TO_MAP = new ToMapTransformer();

    public MagentoMap(Object bean)
    {
        super(bean);
    }

    @Override
    public Object get(Object name)
    {
        Object value = super.get(name);
        if (value == null)
        {
            return null;
        }
        return transformValue(value, value.getClass());
    }

    private Object transformValue(Object value, Class<?> valueClazz)
    {
        if (value.getClass().isArray() && isMagentoClass(valueClazz.getComponentType()))
        {
            return toMap((Object[]) value);
        }
        if (!value.getClass().isArray() && isMagentoClass(valueClazz))
        {
            return toMap(value);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object magentoObject)
    {
        Validate.isTrue(isMagentoClass(magentoObject.getClass()));
        return new MagentoMap(magentoObject);
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> toMap(Object[] magentoObjects)
    {
        return (List<Map<String, Object>>) CollectionUtils.collect(Arrays.asList(magentoObjects), TO_MAP);
    }

    private static boolean isMagentoClass(Class<?> clazz)
    {
        return clazz.getPackage().equals(MAGENTO_PACKAGE);
    }

    private static final class ToMapTransformer implements Transformer
    {
        public Object transform(Object input)
        {
            return toMap(input);
        }
    }

}
