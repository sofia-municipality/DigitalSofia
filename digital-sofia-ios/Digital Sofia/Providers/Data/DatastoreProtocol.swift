//
//  DatastoreProtocol.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 21.07.23.
//

import Foundation

protocol DatastoreProtocol {
    func save<T>(data: T, key: String)
    func read<T>(key: String) -> T?
    func update<T>(key: String, newValue: T)
    func delete(key: String)
}
