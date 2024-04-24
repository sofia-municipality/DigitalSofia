//
//  EmptyListView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.08.23.
//

import SwiftUI

struct EmptyListView: View {
    var reload: (() -> ())?
    
    private let sidePadding = AppConfig.Dimensions.Padding.XXXL
    
    var body: some View {
        VStack {
            Text(AppConfig.UI.Text.emptyScreenTitle.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.bold, size: DSFonts.FontSize.XXL))
                .foregroundColor(DSColors.Text.indigoDark)
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity, alignment: .center)
                .padding([.leading, .trailing], sidePadding)
                .padding(.top, AppConfig.Dimensions.Padding.large)
            
            Text(AppConfig.UI.Text.emptyScreenDetails.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.large))
                .foregroundColor(DSColors.Text.indigoDark)
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity, alignment: .center)
                .padding([.leading, .trailing], sidePadding)
                .padding(.top, AppConfig.Dimensions.Padding.large)
                .padding(.bottom, sidePadding * 2)
            
            BlueBackgroundButton(title: AppConfig.UI.Titles.Button.reload.localized) {
                reload?()
            }
            .padding(.bottom, sidePadding * 2)
        }
        .backgroundAndNavigation()
    }
}

struct EmptyListView_Previews: PreviewProvider {
    static var previews: some View {
        EmptyListView()
    }
}
