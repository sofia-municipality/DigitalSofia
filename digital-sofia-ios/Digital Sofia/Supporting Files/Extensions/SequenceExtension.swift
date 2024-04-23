//
//  SequenceExtension.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 12.01.24.
//

import Foundation

extension Sequence where Element: Equatable {
    func consecutiveAppearances(of element: Element) -> Int {
        var occurance = 0
        
        for item in self {
            if item == element {
                occurance += 1
            } else if occurance > 0 {
                break
            }
        }
        
        if occurance == 1 {
            return 0
        }
        
        return occurance
    }
}
