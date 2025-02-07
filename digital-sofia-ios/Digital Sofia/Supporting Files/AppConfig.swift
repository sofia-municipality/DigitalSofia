//
//  AppConfig.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.04.23.
//

import Foundation
import EvrotrustSDK

struct AppConfig {
    
    struct Dimensions {
        struct Padding {
            static let standart = 8 as CGFloat
            static let small = 10 as CGFloat
            static let medium = 12 as CGFloat
            static let large = 16 as CGFloat
            static let XL = 18 as CGFloat
            static let XXL = 20 as CGFloat
            static let XXXL = 24 as CGFloat
            static let XXXXL = 36 as CGFloat
        }
        
        struct CornerRadius {
            static let mini = 2 as CGFloat
            static let standart = 4 as CGFloat
            static let small = 8 as CGFloat
            static let medium = 12 as CGFloat
            static let large = 16 as CGFloat
            static let XL = 20 as CGFloat
        }
        
        struct Standart {
            static let rowHeight = 50 as CGFloat
            static let lineHeight = 2 as CGFloat
            static let iconHeight = 25 as CGFloat
            static let buttonHeight = 50 as CGFloat
        }
        
        struct Custom {
            static let numpadPadding = Padding.XXL * 2
        }
    }
    
    struct UI {
        struct Titles {
            struct Button {
                static let signIn = "sign_in_button_title"
                static let deleteProfile = "delete_profile"
                static let forward = "continue_button_title"
                static let back = "back_button_title"
                static let accept = "auth_confirm_agree_button"
                static let decline = "auth_confirm_disagree_button"
                static let reload = "relaod_button_title"
                static let delete = "delete_button_title"
                static let ok = "OK"
                static let pinCode = "enter_pin_button_title"
                static let forgottenPin = "forgotten_pin_button_title"
                static let yes = "yes_button_title"
                static let no = "no_button_title"
                static let restartRegistration = "restart_registration_button_title"
                static let done = "done_button_title"
                static let confirm = "confirm_button_title"
                static let deny = "reject_button_title"
                static let enter = "enter_button_title"
                static let understood = "understood_button_title"
            }
            
            struct Tabbar {
                static let home = "tab_bar_home_title"
                static let service = "tab_bar_services_title"
                static let myServices = "tab_bar_my_services_title"
                static let docs = "tab_bar_docs_title"
            }
            
            struct Screens {
                static let settings = "settings_title"
                static let homeListTitle = "home_view_table_title"
                static let authentication = "auth_confirm_title_text"
                static let login = "login_screen_title"
            }
        }
        
        struct Text {
            static let idNumberLabel = "id_number_label"
            static let idNumberPlaceholder = "id_number_placeholder"
            static let sofiaMunicipality = "sofia_municipality"
            
            static let createPinTitle = "pin_create_view_title"
            static let createPinDetails = "pin_create_view_details"
            static let confirmPinTitle = "pin_confirm_view_title"
            static let confirmPinDetails = "pin_confirm_view_details"
            static let somethingWentWrongErrorText = "something_went_wrong_error_text"
            static let authenticationConfirmDetailsText = "auth_confirm_details_text"
            static let authenticationRejectDetailsText = "auth_rejected_details_text"
            
            static let emptyScreenTitle = "empty_state_title"
            static let emptyScreenDetails = "empty_state_details"
            static let deleteProfileDetails = "delete_profile_details"
            static let deleteProfileRequestDetails = "delete_profile_request_details"
            
            static let loginTitleText = "login_title_text"
            static let loginGreetingText = "login_greeting_title_text"
            
            static let biometricDataTitle = "biometric_data_title_text"
            static let biometricDataDetails = "biometric_data_details_text"
            
            static let welcomeUserOkTitle = "welcome_user_ok_title_text"
            static let welcomeUserOkDetails = "welcome_user_ok_details_text"
            
            static let noInternetTitleText = "no_internet_title_text"
            
            static let welcomeToDigitalSofiaText = "welcome_to_digital_sofia_text"
            
            static let changePINOldConfirmText = "change_pin_old_pin_confirm_text"
            
            static let forgottenPasswordConfirmAuthText = "forgotten_password_confirm_auth_text"
            
            static let emailPlaceholder = "email_placeholder"
            static let emailLabel = "email_label"
            static let phonePlaceholder = "phone_placeholder"
            static let phoneLabel = "phone_label"
            
            static let shareDataTitleText = "confirm_share_data_screen_text"
            static let shareDataDetailsText = "confirm_share_data_screen_detail_text"
            
            static let loginFromPortalTitleText = "login_from_portal_title_text"
            static let loginFromPortalDetailsTitleText = "login_from_portal_detail_title_text"
            static let loginFromPortalDetailsText = "login_from_portal_details_text"
            
            static let notificationPermissionAgreementTitleText = "notification_permission_title_text"
            static let notificationPermissionAgreementSubtitleText = "notification_permission_subtitle_text"
        }
        
        struct Alert {
            static let wrongPinAlertText = "wrong_pin_alert_text"
            
            static let welcomeUserErrorTitle = "welcome_user_error_title_text"
            
            static let pinMismatchAlertTitle = "pin_mismatch_title"
            static let pinMismatchAlertDetails = "pin_mismatch_details"
            static let successfullyChangedPinAlertText = "successfully_changed_pin_alert_title"
            
            static let wrongPINAlertTitle = "wrong_pin_alert_title"
            static let generalAlertTitle = "general_alert_title"
            
            static let registerMissingInfoAlertText = "register_missing_info_alert_text"
            static let enterValidEmailAlertText = "enter_valid_email_alert_text"
            static let enterValidEgnAlertText = "enter_valid_egn_alert_text"
            static let registerAcceptTermsAlertText = "register_accept_terms_alert_text"
            
            static let enterValidPhoneAlertText = "enter_valid_phone_alert_text"
            static let verifyPinOnLoginTitleText = "verify_pin_on_login_title_text"
            
            static let shareDataDocDeclineAlertText = "et_share_data_doc_decline_alert_text"
            
            static let successfullySignedDocumentAlertText = "successfully_signed_alert_title"
            static let successfullyRejectedDocumentAlertText = "successfully_rejected_alert_title"
            
            static let openDocumentSdkSetupAlertText = "open_document_sdk_setup_alert_text"
            static let verifyPinErrorWrongCurrent = "verify_pin_error_wrong_current"
            
            static let verifyPinTryAgainAlertText = "verify_pin_try_again_alert_text"
            static let verifyPinCreateAgainAlertText = "verify_pin_create_again_alert_text"
            
            static let successfullyDownloadedWebviewFileAlertText = "successfully_download_web_file_text"
            
            struct BlockPin {
                static let secondsAlertText = "block_pin_seconds_alert_text"
                static let minutesAlertText = "block_pin_minutes_alert_text"
                static let hoursAlertText = "block_pin_hours_alert_text"
            }
        }
        
        struct Permissions {
            static let biometricts = "biometrics_use_reson_description"
        }
        
        struct Documents {
            static let unsigned = "document_status_unsigned"
            static let signing = "document_status_signing"
            static let signed = "document_status_signed"
            static let expired = "document_status_expired"
            static let rejected = "document_status_rejected"
            static let failed = "document_status_failed"
            static let delivering = "document_status_delivering"
            static let delivered = "document_status_delivered"
            
            static let createdOn = "created_on_date"
            static let signedOn = "signed_on_date"
            static let expiredOn = "expired_on_date"
            static let rejectedOn = "rejected_on_date"
            static let deliveredOn = "delivered_on_date"
            
            static let openFile = "open_file"
            static let download = "download_file"
        }
        
        struct Profile {
            static let profile = "profile_option_profile"
            static let language = "profile_option_language"
            static let security = "profile_option_security"
            static let pin = "profile_option_PIN"
            
            struct Security {
                static let faceID = "security_face_id"
                static let touchID = "security_touch_id"
                static let pin = "security_pin"
            }
        }
        
        struct Register {
            static let personalIdLabelText = "register_title_text"
            static let termsAndConditions = "t_and_c_toggle_label_text"
            static let contactInformation = "contact_information_title_text"
        }
        
        struct Menu {
            static let faq = "menu_faq_title"
            static let contacts = "menu_contacts_title"
            static let exit = "menu_exit_title"
            static let privacyPolicy = "menu_pp_title"
        }
        
        struct Evrotrust {
            static let createPinError = "evrotrust_create_pin_error_easy"
            static let sdkAuthenticationFailedError = "st_sdk_authentication_failed_text"
            static let etUserNotReadyToSign = "evrotrust_documents_sign_user_is_paused"
        }
        
        struct Tag {
            static let title = "beta_tag_title_text"
            static let info = "beta_tag_info_text"
        }
    }
    
    struct LanguageDescrition {
        static let english = "english_language_text"
        static let bulgarian = "bulgarian_language_text"
    }
    
    struct Notifications {
        struct UserInfoKeys {
            static let evrotrustSDKSetup = "evrotrust-sdk-setup"
            static let evrotrustTransactionId = "evrotrust-transaction-id"
        }
        
        static let payload = "notification-payload"
    }
    
    struct WebViewPages {
        static let web = NetworkConfig.Addresses.web
        
        static let faq = "\(web)sm-faq"
        static let contacts = "\(web)contacts"
        static let privacyPolicy = "\(web)terms-and-conditions"
        static let myServices = "\(web)my-services"
        static let services = "\(web)request-service"
    }
    
    struct UserDefaultsKeys {
        static let language = "i18n_language"
        static let blockLength = "block_length"
        static let blockTime = "block_time"
        static let enterBackgroundTime = "enter_background_time"
        static let lastTouchTime = "last_touch_time"
        static let selectedTab = "selected_tab"
        static let forceRefreshOldToken = "force-refresh-old-token"
        static let forceRefreshKeychainUser = "force_refresh_keychain_user"
        static let userInitiatedForgottenPasswordFlow = "user_initiated_forgotten_password_flow"
    }
    
    struct KeychainKeys {
        static let newUserPin = "DIGITAL_SOFIA_KEYCHAIN_USER_NEW_PIN"
    }
    
    struct Evrotrust {
        struct CountryCode3 {
            static let bulgaria = "BGR"
        }
    }
    
    struct ErrorLocalisations {
        struct Biometric {
            static let noBiometrics = "biometric_error_no_biometric"
            static let cancelled = "biometric_error_canceled"
            static let fallback = "biometric_error_fallback"
        }
        
        struct Evrotrust {
            static let errorInput = "et_error_wrong_input"
            static let userCancelled = "et_error_user_canceled"
            static let userNotSetUp = "et_error_user_not_set_up"
            static let sdkNotSetUp = "et_error_sdk_not_set_up"
        }
        
        struct Network {
            static let unknown = "network_error_unknown"
            static let internalServer = "network_error_internal"
            static let notFound = "network_error_not_found"
            static let forbidden = "network_error_forbidden"
            static let noJSONData = "network_error_no_JSON_data"
            static let unavailable = "network_error_unavailable"
            static let parsing = "network_error_parsing"
            static let badRequest = "network_error_bad_request"
            static let tokenExpired = "network_error_token_expired"
            static let noInternetConnection = "network_error_no_internet_connection"
            static let invalidUserData = "network_error_invalid_user_data"
            static let logoutCountExceeded = "network_error_logout_count_exceeded"
        }
    }
    
    struct FirebaseAnalytics {
        
        struct Events {
            static let openScreen = "opened_%@"
            
            struct Evrotrust {
                static let sdkSetupStart = "evrotrust_sdk_setup_started"
                static let sdkSetupResult = "evrotrust_sdk_setup_result"
                static let sdkSetLanguage = "evrotrust_sdk_set_language"
                static let sdkEditUserStart = "evrotrust_sdk_edit_user_started"
                static let sdkEditUserResult = "evrotrust_sdk_edit_user_result"
                static let sdkCheckUserStatusStart = "evrotrust_sdk_check_user_status_started"
                static let sdkCheckUserStatusResult = "evrotrust_sdk_check_user_status_result"
                static let sdkChangeSecurityContextStart = "evrotrust_sdk_change_security_context_started"
                static let sdkChangeSecurityContextResult = "evrotrust_sdk_change_security_context_result"
                static let sdkUserSetupStart = "evrotrust_sdk_user_setup_started"
                static let sdkUserSetupResult = "evrotrust_sdk_user_setup_result"
                static let sdkOpenDocumentStart = "evrotrust_sdk_open_document_started"
                static let sdkOpenDocumentResult = "evrotrust_sdk_open_document_result"
            }
        }
        
        struct Parameters {
            static let etEvent = "et_event"
            static let viewName = "view_name"
            static let deviceId = "device_id"
            static let personalIdentificationNumber = "personal_identification_number"
            static let timestamp = "timestamp"
            static let requestResponse = "request_response"
            
            struct Evrotrust {
                static let sdkIsSetUp = "sdk_is_set_up"
                static let sdkHasNewVersion = "sdk_has_new_version"
                static let sdkIsInMaintenance = "sdk_is_in_maintenance"
                static let sdkSecurityContext = "sdk_security_context"
                static let sdkOldSecurityContext = "old_sdk_security_context"
                static let sdkPersonalIdentificationNumber = "sdk_personal_identification_number"
                static let sdkLanguage = "sdk_language"
                static let sdkResult = "sdk_result"
                static let sdkTransactionId = "sdk_transaction_id"
            }
        }
    }
}

struct EvrotrustConfig {
    static var appNumber: String {
        let config = BuildConfiguration.getConfiguration()
        if config == .releaseDev || config == .debugDev {
            return "nDvKBf2Jb2nEVPmP"
        } else {
           return "DTEZ88eMd2UHg2cg"
        }
    }
    
    static var environment: EvrotrustEnvironment {
        let config = BuildConfiguration.getConfiguration()
        if config == .releaseDev || config == .debugDev {
            return .test
        } else {
            return .prod
        }
    }
}
