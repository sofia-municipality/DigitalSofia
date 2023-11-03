//
//  CustomToggleView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 6.06.23.
//

import SwiftUI

struct CustomToggleView: View {
    var title: String
    var fontSize: DSFonts.FontSize = DSFonts.FontSize.XXXL
    @Binding var toggle: Bool
    
    var body: some View {
        HStack {
            Text(title)
                .lineLimit(1)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.firaSans, weight: DSFonts.FontWeight.regular, size: fontSize))
                .layoutPriority(1)
            
            Toggle("", isOn: $toggle)
                .toggleStyle(SwitchToggleStyle(tint: .blue))
        }
    }
}
