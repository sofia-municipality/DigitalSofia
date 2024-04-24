//
//  NonRepeatRule.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 24.11.23.
//

import Foundation

class NoRepeatRule: ValidationRule {
    func isValid(for string: String) -> Bool {
        let repetition = findRepetition(string)
        if repetition.count == 0 {
            return true
        } else {
            return repetition.count == 1 && repetition.string.count == 1
        }
    }
    
    private func findRepetition(_ s: String) -> (count: Int, string: String) {
        if s.isEmpty { return (0, "") }
        let pattern = "([0-9]+)\\1+"
        let regex = try? NSRegularExpression(pattern: pattern, options: [])
        let matches = regex?.matches(in: s, options: [], range: NSRange(location: 0, length: s.utf16.count)) ?? []
        var matchedString = ""
        if let unitRange = matches.first?.range(at: 1) {
            matchedString = (s as NSString).substring(with: unitRange)
        }
        
        return (matches.count, matchedString)
    }
}
