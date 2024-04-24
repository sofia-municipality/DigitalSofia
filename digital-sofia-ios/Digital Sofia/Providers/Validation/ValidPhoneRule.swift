//
//  ValidPhoneRule.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 24.11.23.
//

import Foundation

class ValidPhoneRule: ValidationRule {
    func isValid(for string: String) -> Bool {
        let emailRegEx = #"(\+)?(359)8[789]\d{1}(|-| )\d{3}(|-| )\d{3}"#
        
        let emailPred = NSPredicate(format:"SELF MATCHES %@", emailRegEx)
        return emailPred.evaluate(with: string)
    }
}
