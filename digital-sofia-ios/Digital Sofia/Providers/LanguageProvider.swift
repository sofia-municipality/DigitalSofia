//
//  LanguageProvider.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.04.23.
//

import Foundation
import EvrotrustSDK

class LanguageProvider {
    private init() { }
    
    public static let shared = LanguageProvider()
    
    open var appLanguage: Languages? = Languages(rawValue: UserDefaults.standard.string(forKey: AppConfig.UserDefaultsKeys.language) ?? "") {
        didSet {
            UserDefaults.standard.set(appLanguage?.rawValue, forKey: AppConfig.UserDefaultsKeys.language)
            Evrotrust.sdk()?.setLanguage(appLanguage?.short)
            LoggingHelper.logSDKSetLanguage()
        }
    }
}

enum Languages: String, CaseIterable {
    case english   = "en",
         bulgarian = "bg"
    
    var description: String {
        switch self {
        case .english:
            return AppConfig.LanguageDescrition.english.localized
        case .bulgarian:
            return AppConfig.LanguageDescrition.bulgarian.localized
        }
    }
    
    var short: String {
        switch self {
        case .english:
            return "en"
        case .bulgarian:
            return "bg"
        }
    }
}
