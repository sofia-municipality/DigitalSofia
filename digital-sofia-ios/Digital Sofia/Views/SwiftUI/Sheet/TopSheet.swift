//
//  TopSheet.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 21.08.23.
//

import SwiftUI

struct TopSheet<SheetContent: View>: ViewModifier {
    @Binding var isPresented: Bool
    var frame: CGSize = .zero
    let sheetContent: () -> SheetContent
    
    func body(content: Content) -> some View {
        ZStack(alignment: .topTrailing) {
            content
            
            if isPresented {
                VStack {
                    VStack {
                        sheetContent()
                    }
                    
                    Spacer()
                }
                .zIndex(.infinity)
                .frame(width: frame.width, height: frame.height)
                .background {
                    Color(.white)
                        .cornerRadius(AppConfig.Dimensions.CornerRadius.mini)
                        .clipped()
                        .shadow(color: .gray.opacity(0.3), radius: AppConfig.Dimensions.CornerRadius.mini)
                }
                .offset(x: -AppConfig.Dimensions.Padding.XL, y: AppConfig.Dimensions.Padding.XL)
            }
        }
    }
}

extension View {
    func customMenuSheet<SheetContent: View>(
        isPresented: Binding<Bool>,
        frame: CGSize,
        sheetContent: @escaping () -> SheetContent
    ) -> some View {
        self.modifier(TopSheet(isPresented: isPresented, frame: frame, sheetContent: sheetContent))
    }
}
