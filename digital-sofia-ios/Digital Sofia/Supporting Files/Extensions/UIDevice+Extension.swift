//
//  UIDevice+Extension.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 30.01.24.
//

import UIKit

extension UIDevice {
    static var logDeviceId: String {
        let devideId = current.identifierForVendor?.uuidString ?? ""
        return devideId
    }
}
