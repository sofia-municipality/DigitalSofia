//
//  PDFKitRepresentedView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 7.08.23.
//

import UIKit
import PDFKit
import SwiftUI

struct PDFKitRepresentedView: UIViewRepresentable {
    let url: URL?
    
    init(_ url: URL?) {
        self.url = url
    }
    
    func makeUIView(context: UIViewRepresentableContext<PDFKitRepresentedView>) -> PDFKitRepresentedView.UIViewType {
        let pdfView = PDFView()
        pdfView.displayMode = .singlePageContinuous
        pdfView.displayDirection = .vertical
        pdfView.autoScales = true
        
        if let url = url {
            pdfView.document = PDFDocument(url: url)
        } else {
            if let dummyPdf = URL(string: NetworkConfig.TestData.dummyMultiPagePDFFileURL) {
                pdfView.document = PDFDocument(url: dummyPdf)
            }
        }
        
        return pdfView
    }
    
    func updateUIView(_ uiView: UIView, context: UIViewRepresentableContext<PDFKitRepresentedView>) { }
}
