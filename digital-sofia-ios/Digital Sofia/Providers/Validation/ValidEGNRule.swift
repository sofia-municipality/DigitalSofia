//
//  ValidEGNRule.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 24.11.23.
//

import Foundation

class ValidEGNRule: ValidationRule {
    func isValid(for string: String) -> Bool {
        let emailRegEx = #"\b[0-9]{2}(?:0[1-9]|1[0-2]|2[1-9]|3[0-2]|4[1-9]|5[0-2])(?:0[1-9]|[1-2][0-9]|3[0-1])[0-9]{4}\b"#
        let emailPred = NSPredicate(format:"SELF MATCHES %@", emailRegEx)
        let passedRegex = emailPred.evaluate(with: string)
        
        if passedRegex {
            let checksums = [2,4,8,5,10,9,7,3,6]
            var sum = 0
            for (index, numberString) in string.enumerated() {
                if index != string.count - 1 {
                    let weight = checksums[index]
                    let number = Int(String(numberString)) ?? 0
                    sum += number * weight
                }
            }
            
            let division = sum.quotientAndRemainder(dividingBy: 11)
            var checksum = sum - (division.quotient * 11)
            
            if checksum == 10 {
                checksum = 0
            }
            
            if let checkNumberString = string.last {
                let checkNumber = Int(String(checkNumberString)) ?? 0
                let passChecksum = checkNumber == checksum
                return passChecksum
            }
        }
        
        return false
    }
}
