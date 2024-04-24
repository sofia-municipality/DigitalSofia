//
//  BottomSheet.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 28.04.23.
//

import SwiftUI

struct BottomSheet<SheetContent: View>: ViewModifier {
    @Binding var isPresented: Bool
    let sheetContent: () -> SheetContent
    
    func body(content: Content) -> some View {
        ZStack {
            content
            
            if isPresented {
                VStack {
                    Spacer()
                    
                    VStack {
                        Rectangle()
                            .fill(.red)
                            .frame(height: 1)
                            .edgesIgnoringSafeArea(.horizontal)
                        
                        sheetContent()
                    }
                    .padding()
                    .background(Color.white)
                }
                .zIndex(.infinity)
                .transition(.move(edge: .bottom))
                .edgesIgnoringSafeArea(.bottom)
            }
        }
    }
}

extension View {
    func customBottomSheet<SheetContent: View>(
        isPresented: Binding<Bool>,
        sheetContent: @escaping () -> SheetContent
    ) -> some View {
        self.modifier(BottomSheet(isPresented: isPresented, sheetContent: sheetContent))
    }
}



