/**
 * =============================================
 * Copyright 2005 TransFar
 *
 * Change Revision
 * --------------------------------
 *   Date       Author      Remarks
 *   Nov 30, 2005
 * =============================================
 */
package com.metoo.core.tools;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * Date: Apr 22, 2005
 * Time: 10:49:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class SysPropertiesUtil
{
    private static SysPropertiesUtil oInstance = new SysPropertiesUtil();
    private static Properties oProperties;

    protected Log log = LogFactory.getLog(this.getClass());

    private SysPropertiesUtil()
    {
    }


    protected void loadProperties()
    {
        try
        {
            oProperties = new Properties();

            ClassLoader oClassLoader = Thread.currentThread().getContextClassLoader();

            if(oClassLoader == null)
            {
                oClassLoader = oInstance.getClass().getClassLoader();
            }

            InputStream is = oClassLoader.getResourceAsStream("config.properties");

            if(is != null)
            {
                oProperties.load(is);
                is.close();
            } else
            {
                log.error("SysPropertiesUtil can not load property files!");
            }

        } catch(Exception e)
        {
            log.error(e);
            e.printStackTrace();
        }
    }

    /**
     * Get the value of the a property
     *
     * @return the string value of the property
     */
    public static String getProperty(String key)
    {
        if(oProperties == null)
        {
            oInstance.loadProperties();
        }
        return oProperties.getProperty(key);
    }

    /**
     * Retrieves the property value as an integer for the specified
     * property name
     *
     * @param sPropertyName property name
     * @param iDefaultValue return this value if property not found
     * @return property value as an integer of property name
     */
    public static int getInt(String sPropertyName, int iDefaultValue)
    {
        try
        {
            String sProperty = getProperty(sPropertyName);
            return Integer.parseInt(sProperty);
        } catch(Exception e)
        {
            return iDefaultValue;
        }
    }

    /**
     * Retrieves the property value as a String for the specified
     * property name
     *
     * @param sPropertyName property name
     * @param sDefaultValue return this value if property not found
     * @return property value as a string of property name
     */
    public static String getString(String sPropertyName,
                                   String sDefaultValue)
    {
        try
        {
            return getProperty(sPropertyName);
        } catch(Exception e)
        {
            return sDefaultValue;
        }
    }

    /**
     * Get the map of properties
     *
     * @return the sub group of entries
     */
    public static HashMap getProperties(String keyGroup)
    {
        HashMap hashmap = new HashMap();
        if(oProperties == null)
        {
            oInstance.loadProperties();
        }
        Enumeration enumeration = oProperties.keys();
        while(enumeration.hasMoreElements())
        {
            String tempKey = (String) enumeration.nextElement();
            if(tempKey.startsWith(keyGroup))
            {
                hashmap.put(tempKey, oProperties.get(tempKey));
            }
        }
        return hashmap;
    }

    /**
     * Return the boolean value of the property. The value can be
     * true or t (any case) for true state and any other value will be false
     * including null values or no entry
     *
     * @param key the key name
     * @return the boolean value
     */
    public static boolean getBoolean(String key, boolean bDefaultValue)
    {
        try
        {
            String s = getProperty(key);
            if(s != null)
            {
                return s.equalsIgnoreCase("true") || s.equalsIgnoreCase("t");
            } else
            {
                return bDefaultValue;
            }
        } catch(Exception e)
        {
            return bDefaultValue;
        }
    }

    public static void main(String[] args)
    {
        String property = SysPropertiesUtil.getProperty("UC_KEY");
        System.out.println("property = " + property);
    }
}
