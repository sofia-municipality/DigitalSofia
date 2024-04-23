//
//  CustomAlertView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.04.24.
//

import SwiftUI

struct CustomAlertView: View {
    @State private var title: String
    @State private var message: Text
    @State private var isAnimating = false
    
    init(title: String, message: Text) {
        self.title = title
        self.message = message
    }
    
    var body: some View {
        VStack {
            HStack {
                Text(title)
                    .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans,
                                                weight: DSFonts.FontWeight.bold,
                                                size: DSFonts.FontSize.medium))
                    .foregroundColor(.white)
            }
            .padding([.top, .bottom], AppConfig.Dimensions.Padding.small)
            .frame(maxWidth: .infinity)
            .background(DSColors.tagRed)
            
            message
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans,
                                            weight: DSFonts.FontWeight.regular,
                                            size: DSFonts.FontSize.medium))
                .foregroundColor(DSColors.Text.indigoDark)
                .multilineTextAlignment(.center)
                .lineSpacing(5)
                .padding([.top, .bottom], AppConfig.Dimensions.Padding.XL)
        }
        .background(.white)
        .border(DSColors.tagRed, width: 2)
        .padding([.leading, .trailing], AppConfig.Dimensions.Padding.XXXL)
        .ignoresSafeArea()
    }
}
