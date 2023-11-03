//
//  DSFonts.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 25.04.23.
//

import SwiftUI

struct DSFonts {
    
    enum FontSize: CGFloat {
        case tabbar      = 10
        case extraSmall  = 11
        case mediumSmall = 12
        case small       = 14
        case medium      = 16
        case large       = 18
        case XL          = 20
        case XXL         = 22
        case XXXL        = 24
        case XXXXL       = 28
        case XLarge      = 30
        case XXLarge     = 36
        case iPadLarge  = 44
    }
    
    
    enum FontFamily: String {
        case sofiaSans = "SofiaSans-"
        case firaSans  = "FiraSans-"
    }
    
    enum FontWeight: String {
        case light   = "Light"
        case medium  = "Medium"
        case bold    = "Bold"
        case regular = "Regular"
    }
    
    static func getCustomFont(family: FontFamily, weight: FontWeight, size: FontSize = .medium) -> Font {
        return Font.custom(family.rawValue + weight.rawValue, size: size.rawValue)
    }
    
    static func getCustomUIFont(family: FontFamily, weight: FontWeight, size: FontSize = .medium) -> UIFont {
        return UIFont(name: family.rawValue + weight.rawValue, size: size.rawValue) ?? .systemFont(ofSize: size.rawValue)
    }
}
