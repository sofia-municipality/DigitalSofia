//
//  FileUploader.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 13.02.24.
//

import Foundation
import Alamofire

private struct FileUploadInfo {
    var name: String
    var mimeType: String
    var fileName: String
    var url: URL?
    var data: Data?
    
    init(name: String, withFileURL url: URL, withMimeType mimeType: String? = nil ) {
        self.name = name
        self.url = url
        self.fileName = name
        self.mimeType = "application/octet-stream"
        self.fileName = url.lastPathComponent
    }
    
    init(name: String, withData data: Data, withMimeType mimeType: String ) {
        self.name = name
        self.data = data
        self.fileName = name
        self.mimeType = mimeType
    }
}

class FileUploader {
    private init() { }
    static let shared = FileUploader()
    
    private var parameters = [String:String]()
    private var files = [FileUploadInfo]()
    private var headers = [String:String]()
    
    func setValue(value: String, forParameter parameter: String ) {
        parameters[parameter] = value
    }
    
    func setValue(value: String, forHeader header: String ) {
        headers[header] = value
    }
    
    func addParametersFrom(map: [String:String] ) {
        for (key,value) in map {
            parameters[key] = value
        }
    }
    
    func addHeadersFrom(map: [String:String] ) {
        for (key,value) in map {
            headers[key] = value
        }
    }
    
    func addFileURL(url: URL, withName name: String, withMimeType mimeType:String? = nil ) {
        files.append( FileUploadInfo( name: name, withFileURL: url, withMimeType: mimeType ) )
    }
    
    func addFileData(data: Data, withName name: String, withMimeType mimeType: String = "application/octet-stream" ) {
        files.append(FileUploadInfo( name: name, withData: data, withMimeType: mimeType) )
    }
    
    private func uploadFile(request sourceRequest: NSURLRequest ) -> UploadRequest? {
        var request = sourceRequest.mutableCopy() as! URLRequest
        let boundary = "Boundary-\(arc4random())-\(arc4random())"
        request.setValue( "multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        
        var data = Data()
        
        for (name, value) in headers {
            request.setValue(value, forHTTPHeaderField: name)
        }
        
        for (key, value) in parameters {
            data.append("\r\n--\(boundary)\r\n".data(using: .utf8)!)
            data.append("Content-Disposition: form-data; name=\"\(key)\"\r\n\r\n\(value)".data(using: .utf8)!)
        }
        
        for fileUploadInfo in files {
            data.append( "\r\n--\(boundary)\r\n".data(using: .utf8)! )
            data.append( "Content-Disposition: form-data; name=\"files[]\"; filename=\"\(fileUploadInfo.fileName)\"\r\n".data(using: .utf8)!)
            data.append( "Content-Type: \(fileUploadInfo.mimeType)\r\n\r\n".data(using: .utf8)!)
            
            if fileUploadInfo.data != nil {
                data.append(fileUploadInfo.data!)
            } else if fileUploadInfo.url != nil, let fileData = try? Data(contentsOf: fileUploadInfo.url!) {
                data.append(fileData)
            } else {
                return nil
            }
        }
        
        data.append("\r\n--\(boundary)--\r\n".data(using: .utf8)!)
        return AF.upload(data, with: request)
    }
    
    static func upload(files: [String], service: ServiceProtocol) -> UploadRequest? {
        for file in files {
            let fullName = (file as NSString).lastPathComponent
            let fileName = (fullName as NSString).deletingPathExtension
            let fileURL = URL(fileURLWithPath: file)
            
            shared.addFileURL(url: fileURL, withName: fileName)
        }
        
        let request = NSMutableURLRequest(url: service.requestPath)
        request.httpMethod = service.method.rawValue
        return shared.uploadFile(request: request)
    }
}
