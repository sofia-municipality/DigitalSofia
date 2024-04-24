//
//  ValidEmailRule.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 24.11.23.
//

import Foundation

class ValidEmailRule: ValidationRule {
    func isValid(for string: String) -> Bool {
        let emailRegEx = #"^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$"#
        
        let emailPred = NSPredicate(format:"SELF MATCHES %@", emailRegEx)
        return emailPred.evaluate(with: string)
    }
}
