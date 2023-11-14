//
//  DSError.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 7.06.23.
//

import Foundation

protocol DSError: Swift.Error {
    var description: String { get }
}
