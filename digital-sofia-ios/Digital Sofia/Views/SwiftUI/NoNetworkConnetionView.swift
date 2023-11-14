//
//  NoNetworkConnetionView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.08.23.
//

import SwiftUI

struct NoNetworkConnetionView: View {
    var reload: (() -> ())?
    
    private let sidePadding = AppConfig.Dimensions.Padding.XXXL
    
    var body: some View {
        VStack {
            Text(AppConfig.UI.Text.noInternetTitleText.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.bold, size: DSFonts.FontSize.XXL))
                .foregroundColor(DSColors.Text.indigoDark)
                .frame(maxWidth: .infinity, alignment: .center)
                .padding([.leading, .trailing], sidePadding)
                .padding(.top, AppConfig.Dimensions.Padding.large)
                .padding(.bottom, AppConfig.Dimensions.Padding.XXL)
            
            Spacer()
            
            BlueBackgroundButton(title: AppConfig.UI.Titles.Button.reload.localized) {
                reload?()
            }
            
            Spacer()
        }
    }
}

struct NoNetworkConnetionView_Previews: PreviewProvider {
    static var previews: some View {
        NoNetworkConnetionView()
    }
}
