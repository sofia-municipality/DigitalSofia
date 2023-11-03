//
//  BlueBackgroundButton.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 27.04.23.
//

import SwiftUI

struct BlueBackgroundButton: View {
    var title = ""
    var disabled = false
    var action: (() -> ())?

    var body: some View {
        Button(action: {
            action?()
        }) {
            HStack {
                Text(title)
                    .foregroundColor(.white)
                    .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.medium))
            }
        }
        .buttonStyle(PlainButtonStyle())
        .padding([.top, .bottom], AppConfig.Dimensions.Padding.medium)
        .padding([.leading, .trailing], AppConfig.Dimensions.Padding.large)
        .background(disabled ? DSColors.Blue.blue4 : DSColors.Blue.blue6)
        .cornerRadius(AppConfig.Dimensions.CornerRadius.mini)
        .disabled(disabled)
    }
}

struct BlueBackgroundButton_Previews: PreviewProvider {
    static var previews: some View {
        BlueBackgroundButton()
    }
}
