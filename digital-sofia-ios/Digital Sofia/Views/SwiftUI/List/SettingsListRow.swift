//
//  SettingsListRow.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 26.04.23.
//

import SwiftUI

struct SettingsListRow: View {
    var title: String = ""
    var subTitle: String = ""
    
    let padding = AppConfig.Dimensions.Padding.large
    let height = UIScreen.main.bounds.height * 0.08
    
    var body: some View {
        HStack {
            HStack(spacing: 0) {
                VStack(spacing: 2)  {
                    Text(title)
                        .lineLimit(1)
                        .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.XL))
                        .foregroundColor(DSColors.Text.indigoDark)
                        .padding(.leading, AppConfig.Dimensions.Padding.standart)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    
                    if subTitle.isEmpty == false {
                        Text(subTitle)
                            .lineLimit(1)
                            .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.light, size: DSFonts.FontSize.mediumSmall))
                            .foregroundColor(DSColors.Text.indigoDark)
                            .padding(.leading, AppConfig.Dimensions.Padding.standart)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                }
                
                Spacer()
                
                Image(ImageProvider.keyboardArrowRight)
                    .foregroundColor(DSColors.Indigo.regular.opacity(0.5))
            }
            .padding([.leading, .trailing], padding)
            .frame(width: UIScreen.main.bounds.width - padding * 2, height: height)
            .background {
                Color(.white)
                    .cornerRadius(AppConfig.Dimensions.CornerRadius.mini)
                    .clipped()
                    .shadow(color: .gray.opacity(0.3), radius: 2)
            }
        }
        .frame(width: UIScreen.main.bounds.width, height: height)
    }
}

struct SettingsListRow_Previews: PreviewProvider {
    static var previews: some View {
        SettingsListRow()
    }
}
