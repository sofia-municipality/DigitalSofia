//
//  JSONUtilities.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.08.23.
//

import Foundation

final class JSONUtilities {
    static let shared = JSONUtilities()
    
    func encode<T: Codable>(object: T) -> String? {
        do {
            let encoder = JSONEncoder()
            let jsonData = try encoder.encode(object)
            return String(data: jsonData, encoding: .utf8)
        } catch let error {
#if DEBUG
            print(error.localizedDescription)
#endif
        }
        return nil
    }
    
    func decode<T: Decodable>(jsonString: String) -> T? {
        do {
            let decoder = JSONDecoder()
            if let data = jsonString.data(using: .utf8) {
                let decodedObject = try decoder.decode(T.self, from: data)
                return decodedObject
            }
        } catch {
#if DEBUG
            print("An error occurred while parsing JSON")
#endif
        }
        
        return nil
    }
}
