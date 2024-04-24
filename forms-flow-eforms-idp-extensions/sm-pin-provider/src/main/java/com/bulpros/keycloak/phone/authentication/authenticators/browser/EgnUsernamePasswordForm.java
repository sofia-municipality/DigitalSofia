package com.bulpros.keycloak.phone.authentication.authenticators.browser;

import com.bulpros.keycloak.phone.Utils;
import com.bulpros.keycloak.phone.authentication.forms.SupportPhonePages;
import com.bulpros.keycloak.phone.providers.constants.SpiConstants;
import com.bulpros.keycloak.phone.providers.constants.TokenCodeType;
import com.bulpros.keycloak.phone.providers.exception.CustomProviderException;
import com.bulpros.keycloak.phone.providers.model.CodeStatusEnum;
import com.bulpros.keycloak.phone.providers.model.ConfirmStatusResponse;
import com.bulpros.keycloak.phone.providers.representations.TokenCodeRepresentation;
import com.bulpros.keycloak.phone.providers.spi.CodeCheckProvider;
import com.bulpros.keycloak.phone.providers.spi.PinVerificationCodeProvider;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.keycloak.authentication.AuthenticationFlowError.EXPIRED_CODE;
import static org.keycloak.authentication.AuthenticationFlowError.ACCESS_DENIED;

public class EgnUsernamePasswordForm extends UsernamePasswordForm implements Authenticator, AuthenticatorFactory {

    private static final Logger logger = Logger.getLogger(EgnUsernamePasswordForm.class);
    private static final String LOGIN_PAGE = "login.ftl";

    public static final String PROVIDER_ID = "auth-through-mobile-form";
    public int CHECK_STATUS_INTERVAL = 3000; //in milliseconds

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();
        Response challengeResponse = challenge(context, formData);
        context.challenge(challengeResponse);
    }

    @Override
    protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        LoginFormsProvider forms = context.form();
        if (formData.size() > 0)
            forms.setFormData(formData);
        if (Utils.isDuplicatePhoneAllowed(context.getSession())) {
            forms.setError("duplicatePhoneAllowedCantLogin");
            logger.warn("duplicate phone allowed! phone login is disabled!");
        } else {
            //            forms = assemblyForm(context, forms);
        }
        return forms.createLoginUsernamePassword();
    }

    @Override
    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
        return true;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String personIdentifier = SpiConstants.PNOBG_PREFIX + formData.getFirst(SpiConstants.PERSON_IDENTIFIER);
        Optional<UserModel> userModelOptional = Utils.findUserByPersonIdentifier(//
                context.getSession(), //
                context.getRealm(), //
                personIdentifier//
        );
        if (userModelOptional.isEmpty())
            throw new BadRequestException("Do not exist user with this egn");

        UserModel userModel = userModelOptional.get();

        while (true) {
            TokenCodeRepresentation ongoing = getTokenCodeService(context).ongoingProcess(personIdentifier,
                    TokenCodeType.AUTH);

            if (ongoing.getExpiresAt().before(new Date(System.currentTimeMillis()))) {
                context.failure(EXPIRED_CODE);
                return;
            }
            ConfirmStatusResponse confirmStatusResponse = null;
            try {
                confirmStatusResponse = getCodeCheckService(context).checkConfirmStatus(personIdentifier,
                        ongoing.getCode());
            } catch (CustomProviderException e) {
                logger.error("Could not check authentication code status! Reason: " + e.getMessage());
            }

            if (confirmStatusResponse == null) {
                continue;
            }
            if (CodeStatusEnum.CONFIRMED.equals(confirmStatusResponse.getCodeStatus())) {
                context.setUser(userModel);
                context.success();
                return;
            }
            if (CodeStatusEnum.CANCELLED.equals(confirmStatusResponse.getCodeStatus())) {
                context.form().setError(SupportPhonePages.Errors.AUTH_CANCELLED.message());
                Response challenge = context.form().createForm(LOGIN_PAGE);
                context.failureChallenge(ACCESS_DENIED, challenge);
                return;
            }

            try {
                Thread.sleep(CHECK_STATUS_INTERVAL);
            } catch (InterruptedException e) {
                logger.error("InterruptedException e " + e.getMessage());
                throw new RuntimeException(e);
            }
            logger.debug("Counter for thread: " + Thread.currentThread().getName());
        }
    }

    private PinVerificationCodeProvider getTokenCodeService(AuthenticationFlowContext context) {
        return context.getSession().getProvider(PinVerificationCodeProvider.class);
    }

    private CodeCheckProvider getCodeCheckService(AuthenticationFlowContext context) {
        return context.getSession().getProvider(CodeCheckProvider.class);
    }

    @Override
    public boolean validateUserAndPassword(AuthenticationFlowContext context,
            MultivaluedMap<String, String> inputData) {
        return true;
    }

    @Override
    public String getDisplayType() {
        return "EGN Username Password Form";
    }

    @Override
    public String getReferenceCategory() {
        return PasswordCredentialModel.TYPE;
    }

    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED };

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Validates a username and password or phone and verification code from login form.";
    }

    protected static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

    static {
        CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
                //                .property().name(CONFIG_IS_LOGIN_WITH_PHONE_VERIFY)
                //                .type(BOOLEAN_TYPE).label("Login with phone verify")
                //                .helpText("Input phone number and password, Duplicate phone must be false.").defaultValue(true).add()
                //                .property().name(CONFIG_IS_LOGIN_WITH_PHONE_NUMBER).type(BOOLEAN_TYPE).label("Login with phone number")
                //                .helpText("Input phone number and password,Duplicate phone must be false.").defaultValue(true).add()
                .build();
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
