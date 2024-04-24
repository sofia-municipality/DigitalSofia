//
//  CustomNavigationBar.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 5.06.23.
//

import SwiftUI

struct CustomNavigationBar: View {
    @Environment(\.dismiss) var dismiss
    
    var title = AppConfig.UI.Titles.Button.back.localized
    var image = ""
    var bigTitle = false
    
    private var titleFontSize: DSFonts.FontSize {
        return bigTitle ? DSFonts.FontSize.XXXL : DSFonts.FontSize.XL
    }
    
    private var closeButtonSize: CGFloat {
        return bigTitle ? AppConfig.Dimensions.Standart.iconHeight * 2.5
        : AppConfig.Dimensions.Standart.iconHeight * 1.5
    }
    
    var body: some View {
        HStack {
            titleView
            
            Text(title)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans,
                                            weight: DSFonts.FontWeight.regular,
                                            size: titleFontSize))
                .foregroundColor(DSColors.Text.indigoDark)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding([.leading, .trailing], AppConfig.Dimensions.Padding.standart + AppConfig.Dimensions.Padding.large)
        .padding(.top, AppConfig.Dimensions.Padding.large)
        .padding(.bottom, AppConfig.Dimensions.Padding.medium)
        .backgroundAndNavigation()
    }
    
    @ViewBuilder
    var titleView: some View {
        if image.isEmpty {
            createCloseButton()
        } else {
            Image(image)
                .resizable()
                .renderingMode(.template)
                .foregroundColor(DSColors.Indigo.light)
                .frame(width: AppConfig.Dimensions.Standart.iconHeight,
                       height: AppConfig.Dimensions.Standart.iconHeight)
        }
    }
    
    private func createCloseButton() -> some View {
        Button {
            dismiss()
        } label: {
            Image(ImageProvider.backMenuButton)
                .resizable()
                .frame(width: closeButtonSize, height: closeButtonSize)
        }
    }
}

struct CustomNavigationBar_Previews: PreviewProvider {
    static var previews: some View {
        CustomNavigationBar()
    }
}
