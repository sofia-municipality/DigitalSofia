//
//  NotEGNRule.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 24.11.23.
//

import Foundation

class NotEGNRule: ValidationRule {
    func isValid(for string: String) -> Bool {
        let egn = UserProvider.currentUser?.personalIdentificationNumber
        if let dob = egn?.prefix(6),
           let lastEGN = egn?.suffix(6) {
            return string != dob && string != lastEGN
        }
        
        return false
    }
}
