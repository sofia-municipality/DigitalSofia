//
//  NoConsecutiveDigitsRule.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 24.11.23.
//

import Foundation

class NoConsecutiveDigitsRule: ValidationRule {
    func isValid(for string: String) -> Bool {
        let array = Array(string).map { Int("\($0)")!}
        
        let output = stride(from: 0, to: array.count - 1, by: 1).map{(array[$0], array[$0 + 1])}
        let differences = output.map({ $0.1 - $0.0 })
        
        let countOne = differences.consecutiveAppearances(of: 1)
        let countMinusOne = differences.consecutiveAppearances(of: -1)
        
        return max(countOne, countMinusOne) < 3
    }
}
