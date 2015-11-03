/*
 * Copyright 2015 IIPC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.archive.cdxserver.settings;

import com.typesafe.config.Config;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

/**
 * Utilities used by the settings classes.
 */
public final class SettingUtil {

    /**
     * Private constructor to avoid instantiation.
     */
    private SettingUtil() {
    }

    /**
     * Create an object and configure it based on a config object.
     * <p>
     * The config object should contain parameters named according to the created objects bean
     * parameters.
     * <p>
     * @param <T>       the type of the object to create. This could be a superclass or an interface
     *                  implemented by the class given as the className parameter.
     * @param className the fully qualified class name of the object to create
     * @param type      the class of the type to create
     * @param config    the sub tree of the config containing configuration for the object to create
     * @return the new created and configured object
     */
    public static <T> T createObject(String className, Class<T> type, Config config) {
        try {
            Class<T> objectClass = (Class<T>) Class.forName(className);
            T object = objectClass.newInstance();

            BeanInfo classInfo = Introspector.
                    getBeanInfo(objectClass, Introspector.IGNORE_ALL_BEANINFO);

            // Loop thru all bean properties of the object and set the value if a corresponding
            // name is found in the config
            for (PropertyDescriptor pd : classInfo.getPropertyDescriptors()) {
                if (pd.getWriteMethod() != null) {
                    if (config.hasPath(pd.getName())) {
                        pd.getWriteMethod().
                                invoke(object, config.getValue(pd.getName()).unwrapped());
                    }
                }
            }

            return object;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | IntrospectionException | IllegalArgumentException
                | InvocationTargetException ex) {
            // Just throw an RuntimException to avoid the need to handle exceptions in
            // calling method. Justification is that an exception here should force startup
            // of the application to fail.
            throw new RuntimeException(ex);
        }

    }

}
