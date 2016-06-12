package org.zanata.sync.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.zanata.sync.component.AppConfiguration;
import org.zanata.sync.i18n.Messages;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("adminController")
@RequestScoped
@Slf4j
public class AdminController implements Serializable {

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private Messages msg;

    @Getter
    @Setter
    private boolean deleteJobDir;

    @Getter
    @Setter
    private String fieldsNeedEncryption;

    private Map<String, String> errors = new HashMap<>();

    @PostConstruct
    public void init() {
        fieldsNeedEncryption =
            StringUtils.join(appConfiguration.getFieldsNeedEncryption(), ',');
    }

    public String saveChanges() {
        validate();
        // TODO pahuang fix up admin controller
//        appConfiguration.updateSettingsAndSave(ImmutableList
//            .copyOf(Splitter.on(",").omitEmptyStrings().trimResults()
//                .split(fieldsNeedEncryption)));

//        FacesMessage message = new FacesMessage(SEVERITY_INFO,
//                msg.get("jsf.admin.settings.saved.message"), "");
//        FacesContext.getCurrentInstance().addMessage(null, message);
        return "/admin/settings.jsf";
    }

    private void validate() {
        // TODO validate fields
    }

    public boolean hasError(String fieldName) {
        return errors.containsKey(fieldName);
    }

    public String getErrorMessage(String fieldName) {
        return errors.get(fieldName);
    }
}
