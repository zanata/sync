package org.zanata.sync.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.zanata.sync.component.AppConfiguration;
import org.zanata.sync.i18n.Messages;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("adminController")
@ViewScoped
@Slf4j
public class AdminController implements Serializable {

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private Messages msg;

    @Getter
    private String dataDir;

    @Getter
    @Setter
    private boolean deleteJobDir;

    @Getter
    @Setter
    private String fieldsNeedEncryption;

    private Map<String, String> errors = new HashMap<>();

    @PostConstruct
    public void init() {
        dataDir = appConfiguration.getDataPath();
        fieldsNeedEncryption =
            StringUtils.join(appConfiguration.getFieldsNeedEncryption(), ',');
    }

    public String saveChanges() {
        validate();
        appConfiguration.updateSettingsAndSave(deleteJobDir, ImmutableList
            .copyOf(Splitter.on(",").omitEmptyStrings().trimResults()
                .split(fieldsNeedEncryption)));

        FacesMessage message = new FacesMessage(SEVERITY_INFO,
                msg.get("jsf.admin.settings.saved.message"), "");
        FacesContext.getCurrentInstance().addMessage(null, message);
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
