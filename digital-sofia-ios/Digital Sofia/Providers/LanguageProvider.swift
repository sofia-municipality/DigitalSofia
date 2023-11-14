//
//  LanguageProvider.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.04.23.
//

import Foundation
import EvrotrustSDK

class LanguageProvider {
    public static let shared = LanguageProvider()
    
    open var appLanguage: Languages? = Languages(rawValue: UserDefaults.standard.string(forKey: AppConfig.UserDefaultsKeys.language) ?? "") {
        didSet {
            Evrotrust.sdk()?.setLanguage(appLanguage?.short)
            UserDefaults.standard.set(appLanguage?.rawValue, forKey: AppConfig.UserDefaultsKeys.language)
        }
    }
}

enum Languages: String, CaseIterable {
    case english   = "en",
         bulgarian = "bg-BG"
    
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
