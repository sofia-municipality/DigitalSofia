from marshmallow import Schema, fields


class KEPSignatureRequest(Schema):
    signingCertificate = fields.Str(required=True)
    certificateChain = fields.List(fields.Str(), required=True)
    encryptionAlgorithm = fields.Str(required=True)
    documentToSign = fields.Int(required=True)
    documentName = fields.Str(required=True)
    signingDate = fields.Str(required=True)
    signatureValue = fields.Str(required=False)
