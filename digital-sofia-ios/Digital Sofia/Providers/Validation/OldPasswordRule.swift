//
//  OldPasswordRule.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 12.12.23.
//

import Foundation

class OldPasswordRule: ValidationRule {
    func isValid(for string: String) -> Bool {
        let pin = UserProvider.currentUser?.pin
        return string != pin
    }
}
