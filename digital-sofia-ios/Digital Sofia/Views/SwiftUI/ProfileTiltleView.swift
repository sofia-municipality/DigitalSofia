//
//  ProfileTiltleView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 7.06.23.
//

import SwiftUI

struct ProfileTiltleView: View {
    var title = ""
    
    var body: some View {
        Text(title)
            .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.XXXL))
            .foregroundColor(DSColors.Text.indigoDark)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding([.leading, .trailing], AppConfig.Dimensions.Padding.XXXXL)
    }
}

struct ProfileTiltleView_Previews: PreviewProvider {
    static var previews: some View {
        ProfileTiltleView()
    }
}
