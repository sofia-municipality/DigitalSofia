from formsflow_api.models import OtherFile
from datetime import datetime
from flask import current_app
import os
import hashlib


class OtherFileService():

    def __init__(self) -> None:
        self.save_path = current_app.config.get("OTHER_FILE_DESTINATION")
        if not self.save_path:
            raise ValueError("No OTHER_FILE_DESTINATION variable is set")


    def save_file(
            self, 
            user_id:str,  
            file,
            application_id:int = None,
            additional_path: str = None,
            created_at: datetime = None
        ) -> OtherFile:

        ### Get hash values
        current_datetime = datetime.now()
        current_datetime_isoformat = current_datetime.isoformat()
        hash_values = [
            user_id, 
            file.filename, 
            file.content_type,
            current_datetime_isoformat, 
            application_id
        ]

        salt = str(hash_values)
        data_to_hash = ''.join(map(str, hash_values)) + salt
        hash_object = hashlib.sha256(data_to_hash.encode())
        
        ### Generate hash key
        file_hash = int(hash_object.hexdigest(), 16)

        ### Save file
        if additional_path:
            save_path = self.save_path + additional_path
        else:
            save_path = self.save_path
        
        if not os.path.exists(save_path):
            os.makedirs(name=save_path, exist_ok=True)

        save_path = save_path + str(file_hash)
        file.save(save_path)
        current_app.logger.debug(f"File_hash - {file_hash}")
        current_app.logger.debug(f"Path - {save_path}")
        current_app.logger.debug(file.filename)
        file.seek(0, os.SEEK_END)
        file_size = file.tell()
        file.seek(0, os.SEEK_SET)
        ### 

        file_resource = OtherFile(
            file_hash=file_hash,
            file_name=file.filename,
            file_mimetype=file.content_type,
            file_size=file_size,
            path=additional_path,
            client_id=user_id,
            application_id=application_id
        )

        if created_at:
            file_resource.created_at = created_at

        file_resource.save()

        return file_resource

    def delete(self, hash:str) -> bool:
        other_file = OtherFile.query.filter_by(file_hash=hash).first()
        if not other_file:
            return False
        
        file_path = self.save_path + hash
        
        does_file_exist = os.path.exists(file_path)
        if does_file_exist:
            os.remove(file_path)

        other_file.delete()

        return True

    def get_file_by_hash(self, hash:str) -> OtherFile:
        other_file = OtherFile.query.filter_by(file_hash=hash).first()
        if not other_file:
            return False
        
        return other_file
    
    def get_files_by_hash(self, hash_list: list) -> list[OtherFile]:
        other_files = OtherFile.query.filter(OtherFile.file_hash.in_(hash_list)).all()

        return other_files