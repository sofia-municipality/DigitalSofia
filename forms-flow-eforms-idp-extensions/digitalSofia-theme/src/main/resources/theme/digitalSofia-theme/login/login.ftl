<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "header">
        <span class="sm-login-title" id="eAuth-login-title">${msg("loginTitle")}</span>
        <span class="sm-login-title" id="digitalSofia-login-title">${msg("loginWithDigitalSofia")}</span>
    <#elseif section = "form">
    <script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
    <script src="${url.resourcesPath}/js/login.js" type="text/javascript"></script>

    <label class="sm-choose-provider-label" for="sm-provider-select">${msg("chooseProviderLabel")}</label>
    <select class="sm-choose-provider" id="sm-provider-select" onchange="onProviderSelect()">
        <option value="digitalSofia" selected="selected">${msg("loginWithDigitalSofia")}</option>
        <option value="eAuth">${msg("loginWithEauth")}</option>
    </select>
    <section id="eAuth-section">
        <div id="kc-social-providers" class="${properties.kcFormSocialAccountSectionClass!}">
            <div class="${properties.kcFormSocialAccountListClass!} <#if social.providers?size gt 3>${properties.kcFormSocialAccountListGridClass!}</#if>">
                <#list social.providers as p>
                    <a id="social-${p.alias}" class="${properties.kcFormSocialAccountListButtonClass!} <#if social.providers?size gt 3>${properties.kcFormSocialAccountGridItem!}</#if>"
                            type="button" href="${p.loginUrl}">
                        <span class="${properties.kcFormSocialAccountNameClass!}">${msg("nextCta")}</span>
                    </a>
                </#list>
            </div>
        </div>
        <div class="sm-login-description">
            <p class="sm-login-first-paragraph">${msg("loginFirstParagraph")}</p>
            <p class="sm-login-second-paragraph">${msg("loginSecondParagraph")}</p>
        </div>
    </section>
    <section id="digitalSofia-section">
        <div class="digitalSofia-next-cta-wrapper">
            <div class="digitalSofia-next-cta">
                <button class="sm-login-cta" onclick="onDigitalSofiaNextClick()">${msg("nextCta")}</button>
            </div>
        </div>
        <div class="sm-login-description">
            <p class="digitalSofia-paragraph">${msg("downloadTheAppText")} <strong>${msg("mobileAppName")}</strong> ${msg("downloadTheAppText2")}</p>
        </div>
        <div class="mobile-app-links-wrapper">
           <a href="/">
              <img src="${url.resourcesPath}/img/googleplay.png" alt="Google Play icon" />
            </a>
            <a href="/">
              <img src="${url.resourcesPath}/img/appstore.png" alt="Apple store icon" />
            </a>
        </div>
    </section>
    <section id="personIdentifier-section">
        <div class="alert-error ${properties.kcAlertClass!} pf-m-danger" id="error-section">
            <div class="alert-heading">${msg("alertHeading-error")}</div>
            <p class="${properties.kcAlertTitleClass!}" id="error-message">${msg("digitalSofiaLoginError")}</p>
        </div>
        <form id="kc-form-login" action="${url.loginAction}" class="${properties.kcFormClass!}" method="post" onsubmit="onPersonIdentifierSubmit(this, event); return false;">
            <div class="${properties.kcFormGroupClass!} personIdentifierWrapper">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="personIdentifier" class="${properties.kcLabelClass!}">${msg("pleaseAddEgn")}</label>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <input required type="text" id="personIdentifier" name="personIdentifier"
                            pattern="^[0-9]{10}"
                            data-error-required='${msg("personIdentifierRequiredError")}'
                            data-error-invalid='${msg("personIdentifierInvalidError")}'
                            data-error-invalid-egn='${msg("personIdentifierInvalidEGNError")}'
                            class="${properties.kcInputClass!}"
                            aria-invalid="<#if messagesPerField.existsError('personIdentifier')>true</#if>"
                    />

                    <span id="input-error-personIdentifier" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                        ${kcSanitize(messagesPerField.get('personIdentifier'))?no_esc}
                    </span>
                </div>
            </div>
            <div class="digitalSofia-next-cta-wrapper">
                <div class="digitalSofia-next-cta">
                    <input type="hidden" id="id-hidden-input" name="credentialId"
                        <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
                    <div class="sm-login-cta" data-realmName=${realm.name} id="personIdentifier-submit-cta-wrapper">
                        <input id="personIdentifier-submit-cta" tabindex="0" name="save" type="submit" value="${msg("loginCta")}"/>
                    </div>
                </div>
            </div>
            <div id="error-section-mobileApp-links">
                <div class="sm-login-description">
                    <p class="digitalSofia-paragraph">${msg("downloadTheAppText")} <strong>${msg("mobileAppName")}</strong> ${msg("downloadTheAppText2")}</p>
                </div>
                <div class="mobile-app-links-wrapper">
                    <a href="/">
                        <img src="${url.resourcesPath}/img/googleplay.png" alt="Google Play icon" />
                    </a>
                    <a href="/">
                        <img src="${url.resourcesPath}/img/appstore.png" alt="Apple store icon" />
                    </a>
                </div>
            </div>
        </form>
    </section>
    <section id="loading-section">
        <div class="loading-section-img-wrapper">
            <div class="loading-section-img">
                <img class="loading-section-main-img" src="${url.resourcesPath}/img/mobile-app.png" alt="Mobile app" />
                <img class="loading-section-img-icon" src="${url.resourcesPath}/img/verified_user.svg" alt="" />
            </div>
        </div>
        <h4 class="loading-section-title">${msg("loadingSectionTitle")}</h4>
        <p class="loading-section-paragraph">${msg("loadingSectionDescription")}</p>
        <div class="timer-wrapper">
            <div class="timer-progress" id="timer-progress">
                <svg style="height: 0px; display: block;"><defs><linearGradient id="progress-bar-gradient" gradientTransform="rotate(90)"><stop stop-color="#F1471D"></stop><stop offset="0.1462" stop-color="#F1491D"></stop><stop offset="0.1989" stop-color="#F2501C"></stop><stop offset="0.2364" stop-color="#F35B19"></stop><stop offset="0.2668" stop-color="#F46C17"></stop><stop offset="0.2928" stop-color="#F68213"></stop><stop offset="0.3158" stop-color="#F89E0E"></stop><stop offset="0.3361" stop-color="#FABD09"></stop><stop offset="0.3473" stop-color="#FCD205"></stop><stop offset="0.7338" stop-color="#5E984B"></stop><stop offset="0.7428" stop-color="#5D9656"></stop><stop offset="0.7776" stop-color="#5B917A"></stop><stop offset="0.8143" stop-color="#5A8C98"></stop><stop offset="0.8529" stop-color="#5989AF"></stop><stop offset="0.8943" stop-color="#5886BF"></stop><stop offset="0.9402" stop-color="#5784C9"></stop><stop offset="1" stop-color="#5784CC"></stop></linearGradient></defs></svg>
                <div data-test-id="CircularProgressbarWithChildren">
                    <div style="position: relative; width: 100%; height: 100%;">
                        <svg class="CircularProgressbar " viewBox="0 0 100 100" data-test-id="CircularProgressbar">
                            <path class="CircularProgressbar-trail" d="
                                M 50,50
                                m 0,-46
                                a 46,46 1 1 1 0,92
                                a 46,46 1 1 1 0,-92
                                " stroke-width="8" fill-opacity="0" style="stroke: rgb(215, 212, 224); stroke-width: 2; stroke-dasharray: 289.027px, 289.027px; stroke-dashoffset: 0px;">
                                </path>
                            <path id="timer-progress-path" class="CircularProgressbar-path" d="
                                M 50,50
                                m 0,-46
                                a 46,46 1 1 1 0,92
                                a 46,46 1 1 1 0,-92
                                " stroke-width="8" fill-opacity="0" style="stroke: url('#progress-bar-gradient'); height: 100%; stroke-width: 2; stroke-dasharray: 289.027px, 289.027px; stroke-dashoffset: -19.1225px;">
                            </path>
                        </svg>
                        <div id="timer"></div>
                    </div>
                </div>
            </div>
        </div>
        <div id="try-again-cta-wrapper" class="try-again-cta">
            <button id="try-again-cta" class="sm-login-cta" onclick="onRetrySubmit()">${msg("tryAgainCta")}</button>
        </div>
    </section>
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