<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('email','firstName','lastName'); section>
    <#if section = "header">
        ${msg("loginProfileTitle")}
    <#elseif section = "form">
        <script src="${url.resourcesPath}/js/login-update-profile.js" type="text/javascript"></script>
        <form id="kc-idp-review-profile-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <div class="sm-form-description">
                ${msg("loginProfileDescription")}
            </div>
            <#list profile.attributes as attribute>
                <#if attribute.name == "email">
                        <#if (attribute.value!'') == ''>
                            <div class="${properties.kcFormGroupClass!}">
                        <#else>
                            <div class="${properties.kcFormGroupClass!}" style="display:none;">
                        </#if>
                        <div class="${properties.kcLabelWrapperClass!}">
                            <label for="email" class="${properties.kcLabelClass!}">${msg("email")}</label>
                        </div>
                        <div class="${properties.kcInputWrapperClass!}">
                            <input required type="text" id="email" name="email" value="${(attribute.value!'')}"
                                pattern="^[\w\-\.]+@([\w\-]+\.)+[\w\-]{2,4}$"
                                class="${properties.kcInputClass!}"
                                data-error-required='${msg("emailRequiredError")}'
                                data-error-invalid='${msg("emailInvalidError")}'
                                aria-invalid="<#if messagesPerField.existsError('email')>true</#if>"
                            />

                            <span id="input-error-email" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                                ${kcSanitize(messagesPerField.get('email'))?no_esc}
                            </span>
                        </div>
                    </div>
                </#if>
                <#if attribute.name == "phoneNumber">
                        <#if (attribute.value!'') == ''>
                            <div class="${properties.kcFormGroupClass!}">
                        <#else>
                            <div class="${properties.kcFormGroupClass!}" style="display:none;">
                        </#if>
                        <div class="${properties.kcLabelWrapperClass!}">
                            <label for="phone" class="${properties.kcLabelClass!}">${msg("phone")}</label>
                        </div>
                        <div class="${properties.kcInputWrapperClass!}">
                            <input required type="text" id="phone" name="phoneNumber" value="${(attribute.value!'')}"
                                pattern="^\+359[1-9]{1}[0-9]{8}$"
                                data-error-required='${msg("phoneRequiredError")}'
                                data-error-invalid='${msg("phoneInvalidError")}'
                                class="${properties.kcInputClass!}"
                                aria-invalid="<#if messagesPerField.existsError('phone')>true</#if>"
                            />

                            <span id="input-error-phone" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                                ${kcSanitize(messagesPerField.get('phone'))?no_esc}
                            </span>
                        </div>
                    </div>
                </#if>
                <#if attribute.name == "firstName">
                         <#if (attribute.value!'') == ''>
                            <div class="${properties.kcFormGroupClass!}">
                        <#else>
                            <div class="${properties.kcFormGroupClass!}" style="display:none;">
                        </#if>
                        <div class="${properties.kcLabelWrapperClass!}">
                            <label for="firstName" class="${properties.kcLabelClass!}">${msg("firstName")}</label>
                        </div>
                        <div class="${properties.kcInputWrapperClass!}">
                            <input required type="text" id="firstName" name="firstName" value="${(attribute.value!'')}"
                                class="${properties.kcInputClass!}"
                                data-error-required='${msg("firstNameRequiredError")}'
                                aria-invalid="<#if messagesPerField.existsError('firstName')>true</#if>"
                            />

                            <#if messagesPerField.existsError('firstName')>
                                <span id="input-error-firstname" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                                    ${kcSanitize(messagesPerField.get('firstName'))?no_esc}
                                </span>
                            </#if>
                        </div>
                    </div>
                </#if>
                <#if attribute.name == "lastName">
                    <#if (attribute.value!'') == ''>
                        <div class="${properties.kcFormGroupClass!}">
                    <#else>
                        <div class="${properties.kcFormGroupClass!}" style="display:none;">
                    </#if>
                        <div class="${properties.kcLabelWrapperClass!}">
                            <label for="lastName" class="${properties.kcLabelClass!}">${msg("lastName")}</label>
                        </div>
                        <div class="${properties.kcInputWrapperClass!}">
                            <input required type="text" id="lastName" name="lastName" value="${(attribute.value!'')}"
                                class="${properties.kcInputClass!}"
                                aria-invalid="<#if messagesPerField.existsError('lastName')>true</#if>"
                            />

                            <#if messagesPerField.existsError('lastName')>
                                <span id="input-error-lastname" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                                    ${kcSanitize(messagesPerField.get('lastName'))?no_esc}
                                </span>
                            </#if>
                        </div>
                    </div>
                </#if>
            </#list>
            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}">
                    </div>
                </div>

                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <#if isAppInitiatedAction??>
                    <input id="submit-cta" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("doSignIn")}" />
                    <button class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}" type="submit" name="cancel-aia" value="true" />${msg("doCancel")}</button>
                    <#else>
                    <input id="submit-cta" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!} submitCta" type="submit" value="${msg("doSignIn")}" />
                    </#if>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>