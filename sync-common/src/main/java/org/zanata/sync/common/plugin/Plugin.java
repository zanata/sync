package org.zanata.sync.common.plugin;

import org.zanata.sync.common.model.Field;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public interface Plugin extends Serializable {

    /**
     * @return name of this plugin
     */
    String getName();

    /**
     * @return description of this plugin
     */
    String getDescription();

    /**
     * Return fields for plugin
     */
    Map<String, Field> getFields();

    /**
     * Initialise fields needed for this plugin before constructor
     */
    void initFields();

}
