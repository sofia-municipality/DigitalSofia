from cryptography.fernet import Fernet


def encode_secret(client_secret, key):
    fernet = Fernet(key)
    return fernet.encrypt(client_secret.encode()).decode()


def decode_secret(encoded_secret, key):
    fernet = Fernet(key)
    return fernet.decrypt(encoded_secret.encode()).decode()