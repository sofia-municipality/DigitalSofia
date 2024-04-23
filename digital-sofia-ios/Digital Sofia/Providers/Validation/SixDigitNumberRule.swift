//
//  SixDigitNumberRule.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 24.11.23.
//

import Foundation

class SixDigitNumberRule: ValidationRule {
    func isValid(for string: String) -> Bool {
        return string.count == 6
    }
}
