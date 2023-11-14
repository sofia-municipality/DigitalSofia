//
//  AppConfig.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.04.23.
//

import Foundation

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
            
            static let noPendingDocuments = "no_pending_documents"
            static let createPinTitle = "pin_create_view_title"
            static let createPinDetails = "pin_create_view_details"
            static let confirmPinTitle = "pin_confirm_view_title"
            static let confirmPinDetails = "pin_confirm_view_details"
            static let authenticationErrorText = "auth_error_text"
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
        }
        
        struct Alert {
            static let wrongPinAlertText = "wrong_pin_alert_text"
            static let blockPinAlertText = "block_pin_alert_text"
            
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
            
            static let openDocumentSdkSetupAlertText = "open_document_sdk_setup_alert_text"
            static let verifyPinErrorWrongCurrent = "verify_pin_error_wrong_current"
            
            static let verifyPinTryAgainAlertText = "verify_pin_try_again_alert_text"
            static let verifyPinCreateAgainAlertText = "verify_pin_create_again_alert_text"
        }
        
        struct Documents {
            static let unsigned = "document_status_unsigned"
            static let signing = "document_status_signing"
            static let signed = "document_status_signed"
            static let expired = "document_status_expired"
            static let rejected = "document_status_rejected"
            static let failed = "document_status_failed"
            static let withdrawn = "document_status_withdrawn"
            
            static let createdOn = "created_on_date"
            static let signedOn = "signed_on_date"
            static let expiredOn = "expired_on_date"
            static let rejectedOn = "rejected_on_date"
            
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
        }
    }
}
