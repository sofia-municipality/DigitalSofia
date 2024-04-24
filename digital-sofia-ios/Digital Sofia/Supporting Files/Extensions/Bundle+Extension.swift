//
//  Bundle+Extension.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 24.11.23.
//

import Foundation

extension Bundle {
    static func getLanguageBundle() -> Bundle? {
        let appLanguage = LanguageProvider.shared.appLanguage
        
        guard let path = Bundle.main.path(forResource: appLanguage?.rawValue, ofType: "lproj") else {
            return nil
        }
        
        guard let bundle = Bundle(path: path) else {
            return nil
        }
        
        return bundle
    }
}
