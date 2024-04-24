//
//  BlueTextButton.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 31.07.23.
//

import SwiftUI

struct BlueTextButton: View {
    var title = ""
    var action: (() -> ())?
    
    var body: some View {
        Button(action: {
            action?()
        }) {
            Text(title)
                .foregroundColor(DSColors.Indigo.regular)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.medium))
        }
        .padding(.trailing, AppConfig.Dimensions.Padding.large)
    }
}

struct BlueTextButton_Previews: PreviewProvider {
    static var previews: some View {
        BlueTextButton()
    }
}
