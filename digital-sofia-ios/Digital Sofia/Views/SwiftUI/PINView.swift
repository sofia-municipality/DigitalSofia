//
//  PINView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 22.08.23.
//

import SwiftUI

struct PINView: View {
    @Binding var resetPin: Bool
    var details: String = ""
    var didReachedCodeLength: ((String) -> ())?
    
    private let padding = AppConfig.Dimensions.Padding.XXXL
    
    var body: some View {
        VStack(spacing: padding) {
            Text(details)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.medium))
                .foregroundColor(DSColors.Text.indigoDark)
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity, alignment: .center)
            
            Spacer()
            
            NumpadView(reset: $resetPin, didReachedCodeLength: { pin in
                let stringPIN = pin.map({ String($0) }).joined()
                didReachedCodeLength?(stringPIN)
            })
        }
        .padding([.leading, .trailing, .top], padding)
    }
}

struct PINView_Previews: PreviewProvider {
    static var previews: some View {
        PINView(resetPin: .constant(false))
    }
}
