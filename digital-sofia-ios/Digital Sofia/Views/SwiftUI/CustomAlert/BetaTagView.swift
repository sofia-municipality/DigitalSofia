//
//  BetaTagView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.04.24.
//

import SwiftUI

struct BetaTagView: View {
    private let action: (() -> Void)?
    
    init(perform action: (() -> Void)? = nil) {
        self.action = action
    }
    
    var body: some View {
        Text(AppConfig.UI.Tag.title.localized)
            .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans,
                                        weight: DSFonts.FontWeight.bold,
                                        size: DSFonts.FontSize.tabbar))
            .foregroundColor(.white)
            .padding([.leading, .trailing], AppConfig.Dimensions.Padding.small)
            .padding([.top, .bottom], AppConfig.Dimensions.Padding.standart / 2)
            .background(DSColors.tagRed)
            .cornerRadius(AppConfig.Dimensions.CornerRadius.mini)
            .onTapGesture {
                withAnimation {
                    action?()
                }
            }
    }
}

#Preview {
    BetaTagView(perform: {})
}
