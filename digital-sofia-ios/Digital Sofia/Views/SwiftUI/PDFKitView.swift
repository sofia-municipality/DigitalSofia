//
//  PDFKitView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 7.08.23.
//

import SwiftUI

struct PDFKitView: View {
    @Environment(\.dismiss) var dismiss
    @State private var showShareSheet = false
    
    let pdfUrl: URL?
    let font = DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.XL)
    
    var body: some View {
        VStack {
            HStack {
                Button(action: {
                    showShareSheet = true
                }) {
                    Image(systemName: ImageProvider.SystemImages.share) 
                        .font(font)
                        .padding([.leading, .trailing], AppConfig.Dimensions.Padding.XL)
                }
                
                Spacer()
                
                Button(action: {
                    dismiss()
                }) {
                    Text(AppConfig.UI.Titles.Button.done.localized)
                        .font(font)
                        .foregroundColor(.accentColor)
                        .padding([.leading, .trailing], AppConfig.Dimensions.Padding.XL)
                }
            }
            
            PDFKitRepresentedView(pdfUrl)
        }
        .sheet(isPresented: $showShareSheet) {
            if let pdf = pdfUrl {
                ShareActivityView(items: [pdf])
            }
        }
    }
}

struct PDFKitView_Previews: PreviewProvider {
    static var previews: some View {
        PDFKitView(pdfUrl: URL(string: NetworkConfig.TestData.dummyPDFFileURL))
    }
}

