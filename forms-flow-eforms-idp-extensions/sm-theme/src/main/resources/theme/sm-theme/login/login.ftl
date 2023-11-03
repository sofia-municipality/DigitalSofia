<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "header">
        <span class="sm-login-title">${msg("loginTitle")}</span>
    <#elseif section = "form">
    <div class="sm-login-description">
        <p class="sm-login-first-paragraph">${msg("loginFirstParagraph")}</p>
        <p class="sm-login-second-paragraph">${msg("loginSecondParagraph")}</p>
    </div>
    <div id="kc-social-providers" class="${properties.kcFormSocialAccountSectionClass!}">
        <ul class="${properties.kcFormSocialAccountListClass!} <#if social.providers?size gt 3>${properties.kcFormSocialAccountListGridClass!}</#if>">
            <#list social.providers as p>
                <a id="social-${p.alias}" class="${properties.kcFormSocialAccountListButtonClass!} <#if social.providers?size gt 3>${properties.kcFormSocialAccountGridItem!}</#if>"
                        type="button" href="${p.loginUrl}">
                    <span class="${properties.kcFormSocialAccountNameClass!}">${msg("loginCta")}</span>
                </a>
            </#list>
        </ul>
    </div>
    <#elseif section = "info" >
        <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
            <div id="kc-registration-container">
                <div id="kc-registration">
                    <span>${msg("noAccount")} <a tabindex="6"
                                                 href="${url.registrationUrl}">${msg("doRegister")}</a></span>
                </div>
            </div>
        </#if>
    </#if>

</@layout.registrationLayout>