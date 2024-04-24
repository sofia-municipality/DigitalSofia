//
//  GradientBorderButton.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 17.01.24.
//

import SwiftUI

struct GradientBorderButton: View {
    var title = ""
    var action: (() -> ())?
    
    var body: some View {
        Button(action: {
            action?()
        }) {
            Text(title)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans,
                                            weight: DSFonts.FontWeight.regular,
                                            size: DSFonts.FontSize.XL))
                .padding([.leading, .trailing], AppConfig.Dimensions.Padding.XXXL)
                .padding([.top, .bottom], AppConfig.Dimensions.Padding.large)
                .background(Color.white)
                .foregroundColor(DSColors.Indigo.regular)
                .border(LinearGradient(
                    colors: DSColors.TabbarGradient.list,
                    startPoint: .top,
                    endPoint: .bottom),
                        width: 1)
        }
    }
}

#Preview {
    GradientBorderButton()
}
