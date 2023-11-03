//
//  KeychainDatastore.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 5.04.23.
//

import Foundation

final class KeychainDatastore: DatastoreProtocol {
    
    static let standard = KeychainDatastore()
    
    func save<T>(data: T, key: String) {
        switch data.self {
        case is String:
            if let string = data as? String {
                KeychainWrapper.standard.set(string, forKey: key)
            }
        case is Int:
            if let int = data as? Int {
                KeychainWrapper.standard.set(int, forKey: key)
            }
        case is Bool:
            if let bool = data as? Bool {
                KeychainWrapper.standard.set(bool, forKey: key)
            }
        case is Data:
            if let data = data as? Data {
                KeychainWrapper.standard.set(data, forKey: key)
            }
        default: break
        }
    }
    
    func read<T>(key: String) -> T? {
        switch T.self {
        case is String.Type:
            return KeychainWrapper.standard.string(forKey: key) as? T
        case is Int.Type:
            return KeychainWrapper.standard.integer(forKey: key) as? T
        case is Bool.Type:
            return KeychainWrapper.standard.bool(forKey: key) as? T
        case is Data.Type:
            return KeychainWrapper.standard.data(forKey: key) as? T
        default:
            return nil
        }
    }
    
    func update<T>(key: String, newValue: T) {
        KeychainWrapper.standard.removeObject(forKey: key)
        save(data: newValue, key: key)
    }
    
    func delete(key: String) {
        KeychainWrapper.standard.removeObject(forKey: key)
    }
}
