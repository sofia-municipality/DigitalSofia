//
//  DocumentsListViewRo.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 25.04.23.
//

import SwiftUI

struct DocumentsListViewRow: View {
    @EnvironmentObject var appState: AppState
    @State var document: DocumentModel
    
    var downloadDocument: (() -> ())?
    
    private let cardHeight = UIScreen.main.bounds.height * 0.4
    private let cardWidth = UIScreen.main.bounds.width
    private let padding = AppConfig.Dimensions.Padding.large
    
    var body: some View {
        HStack(spacing: 0) {
            VStack(spacing: 0) {
                VStack(spacing: 0) {
                    documentIdRow()
                    
                    logoRow()
                    
                    documentNameRow()
                    
                    createdAtRow()
                    
                    statusDateRow(status: document.docStatus)
                    
                    openFileRow()
                    
                    downloadFileRow()
                }
                .frame(width: cardWidth - padding * 4, height: cardHeight - padding * 2)
                
                Rectangle()
                    .fill(DSColors.Blue.regular)
                    .frame(width: cardWidth - padding * 2, height:  1)
                    .padding(.top, padding * 2)
                    .cornerRadius(AppConfig.Dimensions.CornerRadius.mini)
                    .clipped()
            }
            .frame(width: cardWidth - padding * 2, height: cardHeight)
            .background {
                Color(.white)
                    .cornerRadius(AppConfig.Dimensions.CornerRadius.mini)
                    .clipped()
                    .shadow(color: .gray.opacity(0.3), radius: AppConfig.Dimensions.CornerRadius.mini)
            }
        }
        .frame(width: cardWidth, height: cardHeight)
        .padding(.top, AppConfig.Dimensions.CornerRadius.mini)
        .padding([.leading, .trailing], padding)
    }
    
    private func documentIdRow() -> some View {
        Text("#" + (document.evrotrustTransactionId ?? ""))
            .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.light, size: DSFonts.FontSize.mediumSmall))
            .foregroundColor(DSColors.Text.indigoDark)
            .frame(maxWidth: .infinity, alignment: .trailing)
            .padding(.top, padding / 2)
    }
    
    private func logoRow() -> some View {
        HStack(spacing: 0) {
            Image(ImageProvider.logo)
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: AppConfig.Dimensions.Standart.rowHeight * 1.5, height: AppConfig.Dimensions.Standart.rowHeight)
            
            Text(AppConfig.UI.Text.sofiaMunicipality.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.XL))
                .foregroundColor(DSColors.Text.indigoDark)
                .padding(.leading, AppConfig.Dimensions.Padding.standart)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(.bottom, AppConfig.Dimensions.Padding.XXXL)
        .frame(maxWidth: .infinity)
    }
    
    private func documentNameRow() -> some View {
        HStack(spacing: 0) {
            Image(ImageProvider.draft)
                .foregroundColor(DSColors.Indigo.regular)
            Text(document.fileName ?? "")
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.medium))
                .foregroundColor(DSColors.Text.indigoDark)
                .padding(.leading, AppConfig.Dimensions.Padding.standart)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(.bottom, AppConfig.Dimensions.Padding.XXL)
    }
    
    private func createdAtRow() -> some View {
        let date = document.created.isoDate ?? Date()
        return dateRow(date: date, with: AppConfig.UI.Documents.createdOn.localized)
    }
    
    private func statusDateRow(status: DocumentStatus) -> some View {
        var dateString: String?
        var localisedDescription: String?
        
        switch status {
        case .signed:
            dateString = document.signed
            localisedDescription = AppConfig.UI.Documents.signedOn.localized
        case .expired:
            dateString = document.expired
            localisedDescription = AppConfig.UI.Documents.expiredOn.localized
        case .rejected:
            dateString = document.rejected
            localisedDescription = AppConfig.UI.Documents.rejectedOn.localized
        case .delivered:
            dateString = document.generated
            localisedDescription = AppConfig.UI.Documents.deliveredOn.localized
        default: break
        }
        
        if let statusString = dateString {
            let date = statusString.isoDate ?? Date()
            return AnyView(dateRow(date: date, with: localisedDescription ?? ""))
        } else {
            return AnyView(Spacer())
        }
    }
    
    private func dateRow(date: Date, with localisedStatus: String) -> some View {
        return HStack(spacing: 0) {
            Text(localisedStatus)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.small))
                .foregroundColor(DSColors.Indigo.regular)
                .frame(maxWidth: .infinity, alignment: .leading)
            
            Text(date.getFormattedDate(format: .ddMMyyyy))
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.small))
                .foregroundColor(DSColors.Indigo.regular)
                .padding(.trailing, padding)
            
            Text(date.getFormattedDate(format: .HHmm))
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.small))
                .foregroundColor(DSColors.Indigo.regular)
                .padding(.trailing, padding)
            
            Text(date.getFormattedDate(format: .ZZZ))
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.small))
                .foregroundColor(DSColors.Indigo.regular)
        }
        .padding(.bottom, padding)
    }
    
    private func openFileRow() -> some View {
        HStack {
            Image(ImageProvider.fileOpen)
                .foregroundColor(DSColors.Blue.regular)
            Text(AppConfig.UI.Documents.openFile.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.small))
                .foregroundColor(DSColors.Indigo.regular)
            Spacer()
        }
        .padding(.top, padding)
        .padding(.bottom, AppConfig.Dimensions.Padding.medium)
        .frame(maxWidth: .infinity)
    }
    
    private func downloadFileRow() -> some View {
        HStack {
            if document.docStatus != .unsigned {
                downloadButton()
            }
            
            Spacer()
            
            statusButton()
        }
    }
    
    private func downloadButton() -> some View {
        Button(action: {
            downloadDocument?()
        }) {
            Image(ImageProvider.download)
                .foregroundColor(DSColors.Blue.regular)
            
            Text(AppConfig.UI.Documents.download.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.small))
                .foregroundColor(DSColors.Indigo.regular)
        }
    }
    
    private func statusButton() -> some View {
        let buttonTitle = document.docStatus.localisedDescription
        let isActive = document.docStatus == .signed || document.docStatus == .delivered
        let buttonBgColor = isActive ? DSColors.green : DSColors.red
        let buttonIcon = isActive ? ImageProvider.check : ImageProvider.close
        
        return HStack {
            Image(buttonIcon)
                .foregroundColor(.white)
                .padding(.trailing, AppConfig.Dimensions.Padding.standart)
            
            Text(buttonTitle)
                .foregroundColor(.white)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.medium))
        }
        .buttonStyle(PlainButtonStyle())
        .padding([.top, .bottom], AppConfig.Dimensions.Padding.standart)
        .padding([.leading, .trailing], AppConfig.Dimensions.Padding.medium)
        .background(buttonBgColor)
        .cornerRadius(AppConfig.Dimensions.CornerRadius.mini)
    }
}
