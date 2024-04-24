//
//  LogoHeaderView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 27.04.23.
//

import SwiftUI

struct LogoHeaderView: View {
    var hideTitle = false
    
    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Image(ImageProvider.logo)
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: UIScreen.main.bounds.width * 0.3, height: UIScreen.main.bounds.width * 0.3)
                Spacer()
            }
            .padding([.top, .bottom], AppConfig.Dimensions.Padding.XL)
            
            if hideTitle == false {
                Text(AppConfig.UI.Text.welcomeToDigitalSofiaText.localized)
                    .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.XL))
                    .foregroundColor(DSColors.Text.indigoDark)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
        }
        .padding(.bottom, AppConfig.Dimensions.Padding.large)
    }
}

struct LogoHeaderView_Previews: PreviewProvider {
    static var previews: some View {
        LogoHeaderView()
    }
}
