//
//  HomeListViewRow.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 25.04.23.
//

import SwiftUI

struct HomeListViewRow: View {
    var text: String = ""
    
    var body: some View {
        HStack {
            HStack(spacing: 0) {
                Image(ImageProvider.logo)
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: AppConfig.Dimensions.Standart.rowHeight * 1.5, height: AppConfig.Dimensions.Standart.rowHeight)
                Text(text)
                    .lineLimit(2)
                    .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.large))
                    .foregroundColor(DSColors.Text.indigoDark)
                    .padding(.leading, AppConfig.Dimensions.Padding.standart)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(.all, AppConfig.Dimensions.Padding.standart)
            .frame(maxWidth: .infinity)
            .background {
                Color(.white)
                    .cornerRadius(AppConfig.Dimensions.CornerRadius.mini)
                    .clipped()
                    .shadow(color: .gray.opacity(0.3), radius: 2)
            }
        }
        .padding([.top, .bottom], AppConfig.Dimensions.Padding.standart)
        .padding([.leading, .trailing], AppConfig.Dimensions.Padding.large)
        .frame(width: .infinity)
    }
}

struct HomeListViewRow_Previews: PreviewProvider {
    static var previews: some View {
        HomeListViewRow()
    }
}
