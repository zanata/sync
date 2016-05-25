package org.zanata.sync.dao;

import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.zanata.sync.model.SyncWorkConfig;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class SyncWorkConfigRepresenter extends Representer {
    @Override
    protected NodeTuple representJavaBeanProperty(Object javaBean, Property property,
        Object propertyValue, Tag customTag) {
        if (javaBean instanceof SyncWorkConfig && "currentProgress".equals(property.getName())) {
            return null;
        } else {
            return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
        }
    }
}
