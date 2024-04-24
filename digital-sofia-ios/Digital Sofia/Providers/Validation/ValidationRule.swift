//
//  ValidationRule.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 24.11.23.
//

protocol ValidationRule {
    func isValid(for string: String) -> Bool
}

extension String {
    func isValid(rules: [ValidationRule]) -> Bool {
        for rule in rules {
            if !rule.isValid(for: self) { return false }
        }
        
        return true
    }
}
