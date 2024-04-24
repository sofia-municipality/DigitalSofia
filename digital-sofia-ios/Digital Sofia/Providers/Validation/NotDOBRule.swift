//
//  NotDOBRule.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 24.11.23.
//

import Algorithms

class NotDOBRule: ValidationRule {
    func isValid(for string: String) -> Bool {
        if let dob = UserProvider.currentUser?.personalIdentificationNumber?.prefix(6) {
            let numbers = Array(dob.chunks(ofCount: 2))
            let permutations = numbers.permutations().map({ $0.joined() })
            return permutations.contains(string) == false
        }
        
        return false
    }
}
